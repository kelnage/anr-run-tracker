package uk.org.nickmoore.runtrack.model;

import uk.org.nickmoore.runtrack.database.ForeignKey;
import uk.org.nickmoore.runtrack.database.PrimaryKey;

/**
 * A class to track a click and how much credits a player has during a game of Android: Netrunner.
 */
public class GameClick {
    @PrimaryKey
    @ForeignKey
    public Game game;
    @PrimaryKey
    public boolean playersTurn;
    @PrimaryKey
    public int turn;
    @PrimaryKey
    public int click;
    public int credits;
}
