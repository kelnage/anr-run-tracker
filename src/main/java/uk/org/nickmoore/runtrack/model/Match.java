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
    @ForeignKey
    public Opponent opponent;
    @ForeignKey(aliasPrefix = "First")
    public Game firstGame;
    @ForeignKey(aliasPrefix = "Second")
    public Game secondGame;
    private Game currentGame;

    public Match() {
        setId(0);
        init();
    }

    public Match(long id) {
        setId(id);
        init();
    }

    private void init() {
        opponent = new Opponent();
        firstGame = new Game();
        secondGame = new Game();
    }

    public Result getPlayerResult() throws UninstantiatedException {
        throwIfNotInstantiated("getPlayerResult");
        firstGame.throwIfNotInstantiated("getPlayerResult");
        secondGame.throwIfNotInstantiated("getPlayerResult");
        if (firstGame.getPlayerResult().equals(secondGame.getPlayerResult())) {
            return firstGame.getPlayerResult();
        }
        int playerScore = firstGame.getEffectivePlayerScore() +
                secondGame.getEffectivePlayerScore();
        int opponentScore = firstGame.getEffectiveOpponentScore() +
                secondGame.getEffectiveOpponentScore();
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
            return String.format("ID: %d, Opponent: %s, First Game: %s, Second Game: %s",
                    getId(), opponent.toString(), firstGame.toString(), secondGame.toString());
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
