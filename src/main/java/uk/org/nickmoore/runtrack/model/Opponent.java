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

import uk.org.nickmoore.runtrack.database.IndexedField;
import uk.org.nickmoore.runtrack.database.Instantiable;

/**
 * A class representing opponent.
 */
public class Opponent extends Instantiable implements Serializable {
    @IndexedField
    public String name;

    public Opponent() {
        setId(0);
    }

    public Opponent(long id) {
        setId(id);
    }

    public Opponent(long id, String name) {
        setId(id);
        this.name = name;
        instantiate();
    }

    @Override
    public String toString() {
        if (isInstantiated()) {
            return String.format("ID: %d, Name: %s", getId(), name);
        }
        return String.format("ID: %d", getId());
    }

    @Override
    public CharSequence toCharSequence(Context context, boolean shortVersion) {
        if (isInstantiated()) {
            return name;
        }
        return String.format("%d", getId());
    }
}
