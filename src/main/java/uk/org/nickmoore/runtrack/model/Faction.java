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
import android.graphics.Color;

import uk.org.nickmoore.runtrack.R;

/**
 * An enum representing the Factions found in Android: Netrunner.
 */
@SuppressWarnings("WeakerAccess")
public enum Faction implements Stringable {
    ANARCH(R.string.anarch, R.string.anarch, Role.RUNNER, Color.argb(255, 225, 93, 49)),
    CRIMINAL(R.string.criminal, R.string.criminal_short, Role.RUNNER, Color.argb(255, 74, 81, 131)),
    SHAPER(R.string.shaper, R.string.shaper, Role.RUNNER, Color.argb(255, 106, 147, 65)),
    HAAS_BIOROID(R.string.haas_bioroid, R.string.haas_bioroid_short, Role.CORPORATION, Color.argb(255, 100, 74, 105)),
    JINTEKI(R.string.jinteki, R.string.jinteki_short, Role.CORPORATION, Color.argb(255, 202, 80, 54)),
    NBN(R.string.nbn, R.string.nbn, Role.CORPORATION, Color.argb(255, 238, 171, 45)),
    WEYLAND(R.string.weyland, R.string.weyland, Role.CORPORATION, Color.argb(255, 105, 114, 99)),
    NEUTRAL_CORP(R.string.neutral, R.string.neutral, Role.CORPORATION, Color.GRAY),
    NEUTRAL_RUNNER(R.string.neutral, R.string.neutral, Role.RUNNER, Color.GRAY);

    public final int textId;
    public final int shortTextId;
    public final Role role;
    public final int color;

    private Faction(int textId, int shortTextId, Role role, int color) {
        this.textId = textId;
        this.shortTextId = shortTextId;
        this.role = role;
        this.color = color;
    }

    public Role getRole() {
        return this.role;
    }

    @Override
    public CharSequence toCharSequence(Context context, boolean shortVersion) {
        if (shortVersion) {
            return context.getText(shortTextId);
        }
        return context.getText(textId);
    }

    public static Faction[] getFaction(Role role) {
        switch (role) {
            case CORPORATION:
                return new Faction[]{HAAS_BIOROID, JINTEKI, NBN, WEYLAND};
            case RUNNER:
                return new Faction[]{ANARCH, CRIMINAL, SHAPER};
        }
        return Faction.values();
    }
}
