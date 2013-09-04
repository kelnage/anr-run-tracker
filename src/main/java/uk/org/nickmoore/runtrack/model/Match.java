package uk.org.nickmoore.runtrack.model;

import android.content.Context;

import java.io.Serializable;

import uk.org.nickmoore.runtrack.database.ForeignKey;
import uk.org.nickmoore.runtrack.database.Instantiable;
import uk.org.nickmoore.runtrack.database.UninstantiatedException;

/**
 * Represents a match (two games, each player taking each role) of Android: Netrunner.
 */
@SuppressWarnings("WeakerAccess")
public class Match extends Instantiable implements Serializable {
    public Opponent opponent;
    @ForeignKey(aliasPrefix = "First")
    public Game firstGame;
    @ForeignKey(aliasPrefix = "Second")
    public Game secondGame;
    private Game currentGame;

    public Match() {
        setId(-1);
    }

    public Match(long id) {
        setId(id);
    }

    public Result getResult() throws UninstantiatedException {
        throwIfNotInstantiated("getPlayerResult");
        firstGame.throwIfNotInstantiated("getPlayerResult");
        secondGame.throwIfNotInstantiated("getPlayerResult");
        if (firstGame.getPlayerResult().equals(secondGame.getPlayerResult())) {
            return firstGame.getPlayerResult();
        }
        int playerScore = firstGame.getEffectivePlayerScore() + secondGame.getEffectivePlayerScore();
        int opponentScore = firstGame.getEffectiveOpponentScore() + secondGame.getEffectiveOpponentScore();
        if (playerScore > opponentScore) {
            return Result.WIN;
        } else if (playerScore < opponentScore) {
            return Result.LOSE;
        }
        return Result.DRAW;
    }

    @Override
    public String toString() {
        if (isInstantiated()) {
            return String.format("ID: %d, Opponent: %d, First Game: %d, Second Game: %d",
                    getId(), opponent.getId(), firstGame.getId(), secondGame.getId());
        }
        return String.format("ID: %d", getId());
    }

    @Override
    public CharSequence toCharSequence(Context context, boolean shortVersion) {
        if (isInstantiated()) {
            return "";
        }
        return String.format("%d", getId());
    }

    public Game getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(Game currentGame) {
        if(currentGame != firstGame && currentGame != secondGame) {
            throw new IllegalArgumentException("currentGame must be either the first or second game");
        }
        this.currentGame = currentGame;
    }
}
