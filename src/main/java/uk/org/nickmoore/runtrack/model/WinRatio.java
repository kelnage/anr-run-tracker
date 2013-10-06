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
