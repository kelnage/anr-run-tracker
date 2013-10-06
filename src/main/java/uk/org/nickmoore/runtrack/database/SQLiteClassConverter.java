/*
 *  Copyright (C) 2013 Nick Moore
 *
 *  This file is part of RunTrack
 *
 *  RunTrack is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.org.nickmoore.runtrack.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Converts classes into database entries and back again.
 */
public class SQLiteClassConverter {
    public static class Join {
        final Class<? extends Instantiable> table;
        final String field;
        String joinType = "JOIN";

        public Join(Class<? extends Instantiable> table, String field) {
            this.table = table;
            this.field = field;
        }

        public Join(Class<? extends Instantiable> table, String field, String joinType) {
            this.table = table;
            this.field = field;
            this.joinType = joinType;
        }
    }

    private class FieldName implements Comparable<FieldName> {
        final Class clazz;
        final String name;
        final String alias;
        final Field parent;

        public FieldName(Class clazz, String name, Field parentField) {
            this.clazz = clazz;
            this.name = name;
            if (name.equals("_id")) {
                alias = clazz.getSimpleName() + "_id";
            } else {
                alias = clazz.getSimpleName() + "_" + name;
            }
            parent = parentField;
        }

        public FieldName(Class clazz, String name, String aliasPrefix, Field parentField) {
            this.clazz = clazz;
            this.name = name;
            if (name.equals("_id")) {
                alias = (aliasPrefix.equals("") ? "" : aliasPrefix + "_") + clazz.getSimpleName()
                        + "_id";
            } else {
                alias = (aliasPrefix.equals("") ? "" : aliasPrefix + "_") + clazz.getSimpleName()
                        + "_" + name;
            }
            parent = parentField;
        }

        @Override
        public String toString() {
            return clazz.getSimpleName() + ":" + name + " (" + alias + ", child of " +
                    (parent == null ? "this class" : parent.getName()) + ")";
        }

        public String getAlias() {
            return alias;
        }

        public String getSql() {
            return clazz.getSimpleName() + "." + name + " AS " + getAlias();
        }

        @Override
        public int compareTo(FieldName fieldName) {
            int c = clazz.getSimpleName().compareTo(fieldName.clazz.getSimpleName());
            if (c == 0) {
                return name.compareTo(fieldName.name);
            }
            return c;
        }
    }

    private class FieldType {
        final String databaseType;
        final Class<?> clazz;
        boolean foreignKey;

        public FieldType(String databaseType, Class<?> clazz) {
            this.databaseType = databaseType;
            this.clazz = clazz;
        }

        public boolean isForeignKey() {
            return foreignKey;
        }

        public void setForeignKey(boolean foreignKey) {
            this.foreignKey = foreignKey;
        }
    }

    private final SQLiteDatabase db;

    public SQLiteClassConverter(SQLiteDatabase db) {
        this.db = db;
    }

    private void log(String message, Object... args) {
        Log.v(getClass().getSimpleName(), String.format(message, args));
    }

    private String[] getFieldNames(Set<FieldName> fieldNames) {
        String[] result = new String[fieldNames.size()];
        int i = 0;
        for (FieldName fieldName : fieldNames) {
            result[i] = fieldName.name;
            i++;
        }
        return result;
    }

    private String[] getFieldSql(Set<FieldName> fieldNames, Class exclude) {
        String[] result = new String[fieldNames.size()];
        int i = 0;
        for (FieldName fieldName : fieldNames) {
            if (fieldName.clazz.equals(exclude)) {
                result[i] = fieldName.clazz.getSimpleName() + "." + fieldName.name;
            } else {
                result[i] = fieldName.getSql();
            }
            i++;
        }
        return result;
    }

    private Map<FieldName, FieldType> getDatabaseFields(Class<?> c, Field parent) {
        TreeMap<FieldName, FieldType> databaseFields = new TreeMap<FieldName, FieldType>();
        if (c.isEnum()) {
            // we'll be using the ordinal value of enums, which isn't a field so needs to be added
            String name = "_id";
            databaseFields.put(new FieldName(c, name, null), new FieldType("INTEGER", c));
        }
        for (Field field : c.getFields()) {
            if (field.isEnumConstant()) {
                continue;
            }
            FieldName databaseName;
            boolean foreignKey = false;
            if (field.isAnnotationPresent(ForeignKey.class)) {
                databaseName = new FieldName(c, field.getName(),
                        field.getAnnotation(ForeignKey.class).aliasPrefix(), parent);
                foreignKey = true;
            } else {
                databaseName = new FieldName(c, field.getName(), parent);
            }
            String databaseType = "";
            Class<?> fieldType = field.getType();
            if (fieldType.getName().equals("boolean") || fieldType.equals(Boolean.class) ||
                    fieldType.getName().equals("byte") || fieldType.equals(Byte.class) ||
                    fieldType.getName().equals("short") || fieldType.equals(Short.class) ||
                    fieldType.getName().equals("int") || fieldType.equals(Integer.class) ||
                    fieldType.getName().equals("long") || fieldType.equals(Long.class) ||
                    fieldType.isEnum()) {
                databaseType = "INTEGER";
            } else if (fieldType.getName().equals("float") || fieldType.equals(Float.class) ||
                    fieldType.getName().equals("double") || fieldType.equals(Double.class)) {
                databaseType = "REAL";
            } else if (fieldType.getName().equals("char") || fieldType.equals(Character.class) ||
                    fieldType.equals(String.class)) {
                databaseType = "TEXT";
            } else {
                try {
                    if (fieldType.getField("_id").getType().getName().equals("long")) {
                        databaseType = "INTEGER";
                    }
                } catch (NoSuchFieldException ex) {
                    databaseType = "TEXT"; // if there's no id, we'll store the toString rep.
                }
            }
            if (!databaseType.equals("")) {
                FieldType ft = new FieldType(databaseType, fieldType);
                ft.setForeignKey(foreignKey);
                databaseFields.put(databaseName, ft);
            }
        }
        return databaseFields;
    }

    public void createTable(Class<?> c) {
        createTable(c, "");
    }

    public void createTable(Class<?> c, String tablePrefix) {
        StringBuilder sql = new StringBuilder();
        sql.append(String.format("CREATE TABLE %s%s (", tablePrefix, c.getSimpleName()));
        Map<FieldName, FieldType> fields = getDatabaseFields(c, null);
        Map<String, String> indexedFields = new HashMap<String, String>();
        boolean first = true;
        for (Map.Entry<FieldName, FieldType> entry : fields.entrySet()) {
            if (!first) {
                sql.append(", ");
            }
            sql.append(String.format("%s %s", entry.getKey().name, entry.getValue().databaseType));
            if (entry.getKey().name.equals("_id")) {
                sql.append(" PRIMARY KEY");
            }
            try {
                Field field = c.getField(entry.getKey().name);
                if (field.isAnnotationPresent(AutoIncrement.class)) {
                    sql.append(" AUTOINCREMENT");
                }
                if (field.isAnnotationPresent(IndexedField.class)) {
                    IndexedField indexedField = field.getAnnotation(IndexedField.class);
                    if (indexedField.indexName().equals("")) {
                        indexedFields.put(
                                String.format("%s_%s", c.getSimpleName(), entry.getKey().name),
                                entry.getKey().name);
                    } else if (indexedFields.containsKey(indexedField.indexName())) {
                        indexedFields.put(indexedField.indexName(),
                                String.format("%s, %s", indexedFields.get(indexedField.indexName()),
                                        entry.getKey().name));
                    } else {
                        indexedFields.put(indexedField.indexName(), entry.getKey().name);
                    }
                }
            } catch (NoSuchFieldException ex) {
                // ignore
            }
            first = false;
        }
        sql.append(")");
        log(sql.toString());
        db.execSQL(sql.toString());
        if (!indexedFields.isEmpty()) {
            for (Map.Entry entry : indexedFields.entrySet()) {
                db.execSQL(String.format("CREATE INDEX %s ON %s(%s)", entry.getKey(),
                        c.getSimpleName(), entry.getValue()));
            }
        }
    }

    public <T extends Instantiable> T instantiateNew(Class<T> clazz, long id) {
        T value = null;
        try {
            try {
                Constructor<T> c = clazz.getConstructor(Long.class);
                value = c.newInstance(id);
            } catch (NoSuchMethodException ex) {
                value = clazz.newInstance();
                clazz.getField("_id").set(value, id);
            }
        } catch (InstantiationException ex) {
            Log.e(getClass().getSimpleName(), "Cannot instantiate " + clazz.getSimpleName()
                    + " due to " + ex.getMessage());
        } catch (InvocationTargetException ex) {
            Log.e(getClass().getSimpleName(), ex.toString());
        } catch (IllegalAccessException ex) {
            Log.e(getClass().getSimpleName(), "Nick forgot to make a member variable public in "
                    + clazz.getSimpleName());
        } catch (NoSuchFieldException ex) {
            Log.e(getClass().getSimpleName(), "Wot, no _id in " + clazz.getSimpleName() + "???");
        }
        return value;
    }

    public ContentValues insertValues(Object instance) {
        Class<?> c = instance.getClass();
        ContentValues data = new ContentValues();
        if (c.isEnum()) {
            data.put("_id", Integer.toString(((Enum<?>) instance).ordinal()));
        }
        for (Field field : c.getFields()) {
            // do not add - the database will autoincrement it anyway and ignore enum constants
            if (!field.isAnnotationPresent(AutoIncrement.class) && !field.isEnumConstant()) {
                try {
                    if (field.getType().isEnum()) {
                        data.put(field.getName(),
                                Integer.toString(((Enum<?>) field.get(instance)).ordinal()));
                    } else {
                        log("Examining class %s in field %s",
                                field.getType().getSimpleName(), field.getName());
                        Object fieldValue = field.get(instance);
                        if(fieldValue == null) {
                            data.putNull(field.getName());
                        }
                        else if (fieldValue instanceof Instantiable) {
                            data.put(field.getName(), ((Instantiable) fieldValue).getId());
                        }
                        else {
                            data.put(field.getName(), field.get(instance).toString());
                        }
                    }
                } catch (IllegalAccessException ex) {
                    log("IllegalAccessException %s", field.getName());
                }
            }
        }
        return data;
    }

    public void store(Object object) throws UnmanageableClassException {
        if(object instanceof Enum<?>) {
            try {
                insert(object);
            } catch(SQLiteConstraintException ex) {
                update(object);
            }
        }
        else if(object instanceof Instantiable) {
            Instantiable instance = (Instantiable) object;
            if(instance.getId() == 0) {
                insert(instance);
            } else {
                update(instance);
            }
        }
    }

    public long insert(Object instance) throws UnmanageableClassException {
        Class clazz = instance.getClass();
        String table = clazz.getSimpleName();
        ContentValues values = insertValues(instance);
        Log.v(getClass().getSimpleName(), String.format("Inserting new %s: %s", table,
                values.toString()));
        long id = db.insert(table, null, values);
        try {
            Field idField = clazz.getField("_id");
            if (idField != null) {
                idField.set(instance, id);
                if (instance instanceof Instantiable) {
                    ((Instantiable) instance).instantiate();
                }
            }
        } catch (NoSuchFieldException ex) {
            // id must exist on non-Enum classes
            if (!clazz.isEnum()) {
                throw new UnmanageableClassException(clazz);
            }
        } catch (IllegalAccessException ex) {
            // id must be public
            if (!clazz.isEnum()) {
                throw new UnmanageableClassException(clazz);
            }
        }
        return id;
    }

    public void update(Object instance) throws UnmanageableClassException {
        String table = instance.getClass().getSimpleName();
        ContentValues values = insertValues(instance);
        Log.v(getClass().getSimpleName(), String.format("Updating %s %s: %s", table,
                getId(instance), values.toString()));
        db.update(table, values, "_id=?", new String[]{getId(instance)});
    }

    private String getId(Object instance) throws UnmanageableClassException {
        Class clazz = instance.getClass();
        String id;
        if (clazz.isEnum()) {
            id = Integer.toString(((Enum<?>) instance).ordinal());
        } else {
            try {
                Field idField = clazz.getField("_id");
                id = Long.toString((Long) idField.get(instance));
            } catch (NoSuchFieldException ex) {
                throw new UnmanageableClassException(clazz);
            } catch (IllegalAccessException ex) {
                throw new UnmanageableClassException(clazz);
            }
        }
        return id;
    }

    public <T extends Instantiable> T readCursor(Class<T> clazz, Cursor cursor) {
        Map<FieldName, FieldType> fields = getDatabaseFields(clazz, null);
        for (Map.Entry<FieldName, FieldType> entry :
                new LinkedHashSet<Map.Entry<FieldName, FieldType>>(fields.entrySet())) {
            if (entry.getValue().isForeignKey()) {
                String field = entry.getKey().name;
                try {
                    fields.putAll(getDatabaseFields(clazz.getField(field).getType(),
                            clazz.getField(field)));
                } catch (NoSuchFieldException ex) {
                    // ignore
                }
            }
        }
        T instance = instantiateNew(clazz, 0);
        readCursor(instance, cursor, fields);
        return instance;
    }

    @SuppressWarnings("unchecked")
    private <T extends Instantiable> void readCursor(T instance, Cursor cursor,
                                                     Map<FieldName, FieldType> fields) {
        int i = 0;
        for (FieldName fieldName : fields.keySet()) {
            if (fieldName.parent == null) {
                if (fieldName.name.equals("_id") && instance.getId() != 0) {
                    // we already have an ID field value
                    if (cursor.getLong(i) != instance.getId()) {
                        Log.w(getClass().getSimpleName(),
                                String.format("overwriting existing ID %d with new ID %d",
                                        instance.getId(), cursor.getLong(i)));
                        instance.setId(cursor.getLong(i));
                    }
                } else {
                    FieldType fieldType = fields.get(fieldName);
                    updateField(instance, fieldName, fieldType, cursor, i);
                }
            } else {
                Instantiable value = null;
                try {
                    value = (Instantiable) fieldName.parent.get(instance);
                } catch (IllegalAccessException ex) {
                    Log.w(getClass().getSimpleName(), "Cannot access " + fieldName.parent.getName());
                }
                if (value == null) {
                    Log.v(getClass().getSimpleName(), "Instantiating new instance of " +
                            fieldName.parent.getName());
                    value = instantiateNew(
                            (Class<? extends Instantiable>) fieldName.parent.getType(), 0);
                    try {
                        fieldName.parent.set(instance, value);
                    } catch (IllegalAccessException ex) {
                        Log.e(getClass().getSimpleName(), ex.toString());
                    }
                }
                updateField(value, fieldName, fields.get(fieldName), cursor, i);
                value.instantiate();
            }
            i++;
        }
        instance.instantiate();
    }

    @SuppressWarnings("unchecked")
    private void updateField(Instantiable instance, FieldName fieldName, FieldType fieldType,
                             Cursor cursor, int i) {
        Log.v(getClass().getSimpleName(), String.format(
                "Assigning %s in cursor field %s to the field %s (%s) in a %s", cursor.getString(i),
                cursor.getColumnName(i), fieldName.name, fieldType.clazz.getSimpleName(),
                instance.getClass().getSimpleName()));
        try {
            Field field = instance.getClass().getField(fieldName.name);
            if (fieldType.clazz.isEnum()) {
                Enum<?> value = ((Class<? extends Enum>) fieldType.clazz)
                        .getEnumConstants()[cursor.getInt(i)];
                field.set(instance, value);
            } else if (fieldType.clazz.getName().equals("int")
                    || fieldType.clazz.equals(Integer.class)) {
                field.set(instance, cursor.getInt(i));
            } else if (fieldType.clazz.getName().equals("float")
                    || fieldType.clazz.equals(Float.class)) {
                field.set(instance, cursor.getFloat(i));
            } else if (fieldType.clazz.getName().equals("long")
                    || fieldType.clazz.equals(Long.class)) {
                field.set(instance, cursor.getLong(i));
            } else if (fieldType.clazz.equals(String.class)) {
                field.set(instance, cursor.getString(i));
            } else if (Instantiable.class.isAssignableFrom(fieldType.clazz)) {
                Class<? extends Instantiable> fieldClass =
                        (Class<? extends Instantiable>) fieldType.clazz;
                Instantiable value = instantiateNew(fieldClass, cursor.getLong(i));
                field.set(instance, value);
            } else {
                Log.e(getClass().getSimpleName(), String.format("I don't know what to do with %s",
                        fieldType.clazz.getSimpleName()));
            }
        } catch (IllegalAccessException ex) {
            Log.e(getClass().getSimpleName(), ex.toString());
        } catch (NoSuchFieldException ex) {
            Log.e(getClass().getSimpleName(), ex.toString());
        }
    }

    public Cursor findAll(Class clazz, String orderBy) {
        return findAll(clazz, orderBy, null);
    }

    public Cursor findAll(Class clazz, String orderBy, String limit) {
        Map<FieldName, FieldType> fields = getDatabaseFields(clazz, null);
        String[] fieldNames = getFieldNames(fields.keySet());
        return db.query(clazz.getSimpleName(), fieldNames, null, null, null, null, orderBy, limit);
    }

    public Cursor findAll(Class clazz,  String selection, String[] selectionArgs, String orderBy,
                          String limit, Join... joins) {
        Map<FieldName, FieldType> fields = getDatabaseFields(clazz, null);
        StringBuilder joinString = new StringBuilder(clazz.getSimpleName());
        for (Join join : joins) {
            try {
                fields.putAll(getDatabaseFields(join.table, clazz.getField(join.field)));
                joinString.append(String.format(" %4$s %1$s ON %2$s.%3$s = %1$s._id",
                        join.table.getSimpleName(), clazz.getSimpleName(), join.field,
                        join.joinType));
            } catch (NoSuchFieldException ex) {
                Log.e(getClass().getSimpleName(), ex.toString());
            }
        }
        String[] fieldNames = getFieldSql(fields.keySet(), clazz);
        Log.i(getClass().getSimpleName(),
                String.format("SQL: SELECT %s FROM %s ORDER BY %s LIMIT %s",
                        TextUtils.join(", ", fieldNames), joinString.toString(), orderBy, limit));
        return db.query(joinString.toString(), fieldNames, selection, selectionArgs, null, null,
                orderBy, limit);
    }

    public <T> int getCount(Class<T> clazz) {
        return getCount(clazz, "");
    }

    public <T> int getCount(Class<T> clazz, String prefix) {
        Cursor cursor = db.query(prefix + clazz.getSimpleName(), new String[]{"count(*)"}, null,
                new String[]{}, null, null, null);
        cursor.moveToFirst();
        int result = cursor.getInt(0);
        cursor.close();
        return result;
    }

    public void retrieve(Instantiable instance) throws UnmanageableClassException,
            NoSuchInstanceException {
        Class clazz = instance.getClass();
        Map<FieldName, FieldType> fields = getDatabaseFields(clazz, null);
        String[] fieldNames = getFieldNames(fields.keySet());
        String id = getId(instance);
        Cursor cursor = db.query(clazz.getSimpleName(), fieldNames, "_id=?",
                new String[]{id}, null, null, null);
        final boolean results = cursor.moveToFirst();
        if (!results) {
            throw new NoSuchInstanceException(getId(instance), clazz);
        }
        readCursor(instance, cursor, fields);
        instance.instantiate();
    }

    public void delete(Object instance) throws UnmanageableClassException {
        String table = instance.getClass().getSimpleName();
        db.delete(table, "_id=?", new String[]{getId(instance)});
    }

    public void close() {
        db.close();
    }
}
