package uk.org.nickmoore.runtrack.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import uk.org.nickmoore.runtrack.database.ForeignKey;
import uk.org.nickmoore.runtrack.database.Instantiable;
import uk.org.nickmoore.runtrack.database.UninstantiatedException;

/**
 * A class encapsulating a Game of Android Netrunner.
 */
public class Game extends Instantiable implements Serializable {
    @ForeignKey
    public Opponent opponent;
    public Identity playerIdentity;
    public int playerAgendaScore;
    public Identity opponentIdentity;
    public int opponentAgendaScore;
    public GameEnd gameEnd;
    public String notes;
    public int date;
    @ForeignKey
    public Match match;

    public Game() {
        setId(0);
        init();
    }

    public Game(long id) {
        setId(id);
        init();
    }

    private void init() {
        opponent = new Opponent();
        playerIdentity = Identity.NOISE;
        playerAgendaScore = 0;
        opponentIdentity = Identity.ENGINEERING_THE_FUTURE;
        opponentAgendaScore = 0;
        gameEnd = GameEnd.AGENDAS;
        notes = "";
        date = Math.round(new Date().getTime() / 1000f);
        match = null;
    }

    public Result getPlayerResult(GameEnd gameEnd) {
        if (gameEnd == GameEnd.AGENDAS && playerAgendaScore >= 7) {
            return Result.WIN;
        }
        if (gameEnd == GameEnd.TIMEOUT) {
            if (playerAgendaScore > opponentAgendaScore) {
                return Result.WIN;
            } else if (playerAgendaScore == opponentAgendaScore) {
                return Result.DRAW;
            }
        }
        if (playerIdentity.getRole() == Role.CORPORATION && gameEnd == GameEnd.FLATLINE) {
            return Result.WIN;
        }
        if (playerIdentity.getRole() == Role.RUNNER && gameEnd == GameEnd.DECKOUT) {
            return Result.WIN;
        }
        return Result.LOSE;
    }

    public Result getPlayerResult() throws UninstantiatedException {
        return getPlayerResult(gameEnd);
    }

    public int getEffectivePlayerScore() throws UninstantiatedException {
        throwIfNotInstantiated("getEffectivePlayerScore");
        switch (getPlayerResult()) {
            case WIN:
                return 7;
            default:
                return playerAgendaScore;
        }
    }

    public int getEffectiveOpponentScore() throws UninstantiatedException {
        throwIfNotInstantiated("getEffectiveOpponentScore");
        switch (getPlayerResult()) {
            case LOSE:
                return 7;
            default:
                return opponentAgendaScore;
        }
    }

    @Override
    public String toString() {
        if (isInstantiated()) {
            return String.format("ID: %d, Player Identity: %s, Player Score: %d" +
                    "\nOpponent: %s, Opponent Identity: %s, Opponent Score: %d," +
                    " Game End: %s, Notes: %s\nMatch: %s",
                    getId(), playerIdentity.name(), playerAgendaScore,
                    opponent.toString(), opponentIdentity.name(),
                    opponentAgendaScore, gameEnd.name(), notes, String.valueOf(match));
        }
        return String.format("ID: %d", getId());
    }

    public void setDate(Date date) {
        this.date = Math.round((float) date.getTime() / 1000f);
    }

    public Date getDate() {
        return new Date(date * 1000l);
    }

    @Override
    public CharSequence toCharSequence(Context context, boolean shortVersion) {
        if (isInstantiated()) {
            return "";
        }
        return String.format("%d", getId());
    }

    public static WinRatio getWinRatio(SQLiteDatabase db) {
        String totalSql = "SELECT COUNT(*) FROM Game";
        String winSql = "SELECT COUNT(*) FROM Game AS g " +
                "JOIN Identity AS i ON playerIdentity = i._id " +
                "JOIN Faction AS f ON i.faction = f._id " +
                "WHERE (gameEnd = 0 AND playerAgendaScore >= 7) " +
                "OR (gameEnd = 1 AND f.role = 0) " +
                "OR (gameEnd = 2 AND f.role = 1)";
        Cursor total = db.rawQuery(totalSql, new String[]{});
        if (!total.moveToFirst()) {
            return new WinRatio(0, 0);
        }
        Cursor wins = db.rawQuery(winSql, new String[]{});
        if (!wins.moveToFirst()) {
            return new WinRatio(0, total.getInt(0));
        }
        return new WinRatio(wins.getInt(0), total.getInt(0));
    }

    public static <T extends Enum> Map<T, WinRatio> getWinRatiosBy(SQLiteDatabase db,
                                                                   Class<T> pivotClass) {
        Map<T, WinRatio> result = new HashMap<T, WinRatio>();
        String winSql;
        String totalSql;
        String totalSqlTemplate = "SELECT %1$s, COUNT(*) FROM Game AS g " +
                "JOIN Identity AS i ON playerIdentity = i._id " +
                "JOIN Faction AS f ON i.faction = f._id " +
                "GROUP BY %1$s ORDER BY %1$s";
        String winSqlTemplate = "SELECT %1$s, COUNT(*) FROM Game AS g " +
                "JOIN Identity AS i ON playerIdentity = i._id " +
                "JOIN Faction AS f ON i.faction = f._id " +
                "WHERE (gameEnd = 0 AND playerAgendaScore >= 7) " +
                "OR (gameEnd = 1 AND f.role = 0) " +
                "OR (gameEnd = 2 AND f.role = 1) " +
                "GROUP BY %1$s ORDER BY %1$s";
        if (pivotClass.equals(Role.class)) {
            winSql = String.format(winSqlTemplate, "f.role");
            totalSql = String.format(totalSqlTemplate, "f.role");
        } else if (pivotClass.equals(Faction.class)) {
            winSql = String.format(winSqlTemplate, "f._id");
            totalSql = String.format(totalSqlTemplate, "f._id");
        } else if (pivotClass.equals(Identity.class)) {
            winSql = String.format(winSqlTemplate, "i._id");
            totalSql = String.format(totalSqlTemplate, "i._id");
        } else {
            throw new UnsupportedOperationException("getWinRatiosBy is not yet defined for " +
                    pivotClass.getSimpleName());
        }
        Cursor wins = db.rawQuery(winSql, new String[]{});
        wins.moveToFirst();
        if (!wins.isAfterLast())
            Log.d(Game.class.getSimpleName(), String.format("Wins %d %d",
                    wins.getInt(0), wins.getInt(1)));
        Cursor total = db.rawQuery(totalSql, new String[]{});
        total.moveToFirst();
        if (!total.isAfterLast())
            Log.d(Game.class.getSimpleName(), String.format("Total %d %d",
                    total.getInt(0), total.getInt(1)));
        while (!wins.isAfterLast() || !total.isAfterLast()) {
            T instance = pivotClass.getEnumConstants()[total.getInt(0)];
            if (wins.isAfterLast() || wins.getInt(0) > total.getInt(0)) {
                result.put(instance, new WinRatio(0, total.getInt(1)));
            } else {
                result.put(instance, new WinRatio(wins.getInt(1), total.getInt(1)));
                wins.moveToNext();
                if (!wins.isAfterLast())
                    Log.d(Game.class.getSimpleName(), String.format("Wins %d %d",
                            wins.getInt(0), wins.getInt(1)));
            }
            total.moveToNext();
            if (!total.isAfterLast())
                Log.d(Game.class.getSimpleName(), String.format("Total %d %d",
                        total.getInt(0), total.getInt(1)));
        }
        wins.close();
        total.close();
        return result;
    }
}
