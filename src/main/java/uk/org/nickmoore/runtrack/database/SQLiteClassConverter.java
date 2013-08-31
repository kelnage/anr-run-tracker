package uk.org.nickmoore.runtrack.database;

import android.content.ContentValues;
import android.database.Cursor;
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

        public Join(Class<? extends Instantiable> table, String field) {
            this.table = table;
            this.field = field;
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
                alias = aliasPrefix + "_" + clazz.getSimpleName() + "_id";
            } else {
                alias = aliasPrefix + "_" + clazz.getSimpleName() + "_" + name;
            }
            parent = parentField;
        }

        @Override
        public String toString() {
            return clazz.getSimpleName() + ":" + name + " (" + alias + ")";
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

    private String[] getFieldAlias(FieldName[] fieldNames) {
        String[] result = new String[fieldNames.length];
        int i = 0;
        for (FieldName fieldName : fieldNames) {
            result[i] = fieldName.getAlias();
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
                log("Examining class %s in field %s", field.getType().getSimpleName(),
                        field.getName());
                try {
                    if (fieldType.getField("_id").getType().getName().equals("long")) {
                        log("Class %s is a foreign key", fieldType.getSimpleName());
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
                        indexedFields.put(String.format("%s_%s", c.getSimpleName(), entry.getKey().name),
                                entry.getKey().name);
                    } else if (indexedFields.containsKey(indexedField.indexName())) {
                        indexedFields.put(indexedField.indexName(), String.format("%s, %s",
                                indexedFields.get(indexedField.indexName()), entry.getKey().name));
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
            // TODO
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
                        try {
                            if (field.getType().getField("_id") != null) {
                                log("Class %s is a foreign key", field.getType().getSimpleName());
                                data.put(field.getName(),
                                        field.getType().getField("_id")
                                                .get(field.get(instance)).toString());
                            }
                        } catch (NoSuchFieldException ex) {
                            // log("NoSuchFieldException %s", field.getName());
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

    public void store(Instantiable instance) throws UnmanageableClassException {
        if (instance.getId() == -1) {
            insert(instance);
        } else {
            update(instance);
        }
    }

    public void insert(Object instance) throws UnmanageableClassException {
        Class clazz = instance.getClass();
        String table = clazz.getSimpleName();
        long id = db.insert(table, null, insertValues(instance));
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
    }

    public void update(Object instance) throws UnmanageableClassException {
        String table = instance.getClass().getSimpleName();
        db.update(table, insertValues(instance), "_id=?", new String[]{getId(instance)});
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
        T instance = instantiateNew(clazz, -1);
        readCursor(instance, cursor, fields);
        return instance;
    }

    @SuppressWarnings("unchecked")
    private <T extends Instantiable> void readCursor(T instance, Cursor cursor,
                                                     Map<FieldName, FieldType> fields) {
        int i = 0;
        for (FieldName fieldName : fields.keySet()) {
            Log.v(getClass().getSimpleName(), String.format("Checking %s against column %s",
                    fieldName, cursor.getColumnName(i)));
            if (fieldName.parent == null) {
                if (fieldName.name.equals("_id") && instance.getId() != -1) {
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
                    // TODO
                }
                if (value == null) {
                    value = instantiateNew(
                            (Class<? extends Instantiable>) fieldName.parent.getType(), -1);
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
            } else if (fieldType.clazz.isAssignableFrom(Instantiable.class)) {
                Class<? extends Instantiable> fieldClass =
                        (Class<? extends Instantiable>) fieldType.clazz;
                Instantiable value = instantiateNew(fieldClass, cursor.getLong(i));
                field.set(instance, value);
            }
        } catch (IllegalAccessException ex) {
            // TODO
        } catch (NoSuchFieldException ex) {
            // TODO
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

    public Cursor findAll(Class clazz, String orderBy, String limit,
                          Join... joins) {
        Map<FieldName, FieldType> fields = getDatabaseFields(clazz, null);
        StringBuilder joinString = new StringBuilder(clazz.getSimpleName());
        for (Join join : joins) {
            try {
                fields.putAll(getDatabaseFields(join.table, clazz.getField(join.field)));
                joinString.append(String.format(" JOIN %1$s ON %2$s.%3$s = %1$s._id",
                        join.table.getSimpleName(), clazz.getSimpleName(), join.field));
            } catch (NoSuchFieldException ex) {
                // TODO
            }
        }
        String[] fieldNames = getFieldSql(fields.keySet(), clazz);
        Log.i(getClass().getSimpleName(),
                String.format("SQL: SELECT %s FROM %s ORDER BY %s LIMIT %s",
                        TextUtils.join(", ", fieldNames), joinString.toString(), orderBy, limit));
        return db.query(joinString.toString(), fieldNames, null, null, null, null, orderBy, limit);
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
        String[] fieldNames = getFieldAlias(fields.keySet().toArray(new FieldName[fields.size()]));
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
