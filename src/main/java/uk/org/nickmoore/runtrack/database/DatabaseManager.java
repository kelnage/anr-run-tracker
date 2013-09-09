package uk.org.nickmoore.runtrack.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import uk.org.nickmoore.runtrack.model.Faction;
import uk.org.nickmoore.runtrack.model.Game;
import uk.org.nickmoore.runtrack.model.GameEnd;
import uk.org.nickmoore.runtrack.model.Identity;
import uk.org.nickmoore.runtrack.model.Match;
import uk.org.nickmoore.runtrack.model.Opponent;
import uk.org.nickmoore.runtrack.model.Role;

/**
 * A class to manage the SQLite database.
 */
public class DatabaseManager extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "NetrunnerTracker";
    public static final int DATABASE_VERSION = 6;
    public static final Class[] DATABASE_ENUMS = {Faction.class, GameEnd.class, Identity.class,
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
                    "opponentAgendaScore, gameEnd, notes, date";
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
        if (newVersion >= 6 && oldVersion < 6) {
            String sql = String.format("ALTER TABLE %s ADD COLUMN match INTEGER DEFAULT NULL",
                    Game.class.getSimpleName());
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
