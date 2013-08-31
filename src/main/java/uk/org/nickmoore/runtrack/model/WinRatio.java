package uk.org.nickmoore.runtrack.model;

/**
 * Created by Nick on 30/08/13.
 */
public class WinRatio {
    private int wins;
    private int total;

    public WinRatio(int wins, int total) {
        this.wins = wins;
        this.total = total;
    }

    public int getTotal() {
        return total;
    }

    public int getWins() {
        return wins;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }
}
