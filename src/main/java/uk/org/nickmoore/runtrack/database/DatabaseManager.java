/*
 *  Copyright (C) 2013 Nick Moore
 *
 *  This file is part of ANR Run Tracker
 *
 *  ANR Run Tracker is free software: you can redistribute it and/or modify
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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import uk.org.nickmoore.runtrack.model.Faction;
import uk.org.nickmoore.runtrack.model.Game;
import uk.org.nickmoore.runtrack.model.GameEnd;
import uk.org.nickmoore.runtrack.model.IdentityEnum;
import uk.org.nickmoore.runtrack.model.Match;
import uk.org.nickmoore.runtrack.model.Opponent;
import uk.org.nickmoore.runtrack.model.Role;

/**
 * A class to manage the SQLite database.
 */
public class DatabaseManager extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "NetrunnerTracker";
    public static final int DATABASE_VERSION = 14;
    public static final Class[] DATABASE_ENUMS = {Faction.class, GameEnd.class, IdentityEnum.class,
            Role.class};

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        SQLiteClassConverter converter = new SQLiteClassConverter(sqLiteDatabase);
        for (Class clazz : DATABASE_ENUMS) {
            converter.createTable(clazz);
            try {
                for (Object e : clazz.getEnumConstants()) {
                    converter.insert(e);
                }
            } catch (UnmanageableClassException ex) {
                // can this ever happen?
            }
        }
        converter.createTable(Opponent.class);
        converter.createTable(Game.class);
        converter.createTable(Match.class);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        SQLiteClassConverter converter = new SQLiteClassConverter(sqLiteDatabase);
        if (newVersion >= 2 && oldVersion < 2) {
            for (Class clazz : DATABASE_ENUMS) {
                try {
                    if (clazz.getField("shortTextId") != null) {
                        String sql = String.format(
                                "ALTER TABLE %s ADD COLUMN shortTextId INTEGER DEFAULT 0",
                                clazz.getSimpleName());
                        Log.i(getClass().getSimpleName(), sql);
                        sqLiteDatabase.execSQL(sql);
                    }
                } catch (NoSuchFieldException ex) {
                    // nevermind - we expect that on some classes
                }
            }
        }
        if (newVersion >= 3 && oldVersion < 3) {
            converter.createTable(Game.class, "temporary_");
            int startCount = converter.getCount(Game.class);
            String fields = "_id, playerIdentity, playerAgendaScore, opponent, opponentIdentity, " +
                    "opponentAgendaScore, gameEnd, notes";
            String copySql = String.format("INSERT INTO temporary_%1$s(%2$s) SELECT %2$s FROM %1$s",
                    Game.class.getSimpleName(), fields);
            Log.i(getClass().getSimpleName(), copySql);
            sqLiteDatabase.execSQL(copySql);
            int newCount = converter.getCount(Game.class, "temporary_");
            if (newCount != startCount) {
                Log.e(getClass().getSimpleName(),
                        String.format("Expected %d rows - query created %d rows",
                                startCount, newCount));
                // the upgrade has failed!
                // inform user to create a backup database and send it to me for debugging
            } else {
                String dropSql = String.format("DROP TABLE %s", Game.class.getSimpleName());
                Log.i(getClass().getSimpleName(), dropSql);
                sqLiteDatabase.execSQL(dropSql);
                String alterSql = String.format("ALTER TABLE temporary_%1$s RENAME TO %1$s",
                        Game.class.getSimpleName());
                Log.i(getClass().getSimpleName(), alterSql);
                sqLiteDatabase.execSQL(alterSql);
            }
        }
        if (newVersion >= 4 && oldVersion < 4) {
            for (Class clazz : DATABASE_ENUMS) {
                try {
                    if (clazz.getField("color") != null) {
                        String sql = String.format(
                                "ALTER TABLE %s ADD COLUMN color INTEGER DEFAULT 0",
                                clazz.getSimpleName());
                        Log.i(getClass().getSimpleName(), sql);
                        sqLiteDatabase.execSQL(sql);
                    }
                } catch (NoSuchFieldException ex) {
                    // nevermind - we expect that on some classes
                }
            }
        }
        if (newVersion >= 6 && oldVersion < 6 && oldVersion > 3) {
            String sql = String.format("ALTER TABLE %s ADD COLUMN match INTEGER DEFAULT NULL",
                    Game.class.getSimpleName());
            Log.i(getClass().getSimpleName(), sql);
            sqLiteDatabase.execSQL(sql);
        }
        if(oldVersion == 6) {
            // remove rogue matches and second games that should have been deleted - issue #10
            String sql = String.format("DELETE FROM %1$s " +
                    "WHERE (SELECT COUNT(*) FROM %2$s WHERE %1$s.firstGame == %2$s._id) == 0",
                    Match.class.getSimpleName(), Game.class.getSimpleName());
            Log.i(getClass().getSimpleName(), sql);
            sqLiteDatabase.execSQL(sql);
            sql = String.format("DELETE FROM %1$s " +
                    "WHERE (SELECT COUNT(*) FROM %2$s WHERE %1$s.match == %2$s._id) == 0 " +
                    "AND %1$s.match IS NOT NULL",
                    Game.class.getSimpleName(), Match.class.getSimpleName());
            Log.i(getClass().getSimpleName(), sql);
            sqLiteDatabase.execSQL(sql);
        }
        if (newVersion >= 8 && oldVersion < 8 && oldVersion > 3) {
            String sql = String.format("ALTER TABLE %s ADD COLUMN type INTEGER DEFAULT false",
                    Game.class.getSimpleName());
            Log.i(getClass().getSimpleName(), sql);
            sqLiteDatabase.execSQL(sql);
        }
        if(oldVersion < 8) {
            // remove rogue games where their opponent has been deleted
            String sql = String.format("DELETE FROM %1$s " +
                    "WHERE (SELECT COUNT(*) FROM %2$s WHERE %1$s.opponent == %2$s._id) == 0",
                    Game.class.getSimpleName(), Opponent.class.getSimpleName());
            Log.i(getClass().getSimpleName(), sql);
            sqLiteDatabase.execSQL(sql);
        }
        for (Class clazz : DATABASE_ENUMS) {
            try {
                for (Object object : clazz.getEnumConstants()) {
                    // update string references and insert new constants
                    converter.store(object);
                }
            } catch (UnmanageableClassException ex) {
                // oops... (huh?)
            }
        }
    }
}
