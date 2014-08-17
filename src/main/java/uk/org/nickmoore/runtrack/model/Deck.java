/*
 *  Copyright (C) 2013 Nick Moore
 *
 *  This file is part of RunTrack
 *
 *  RunTrack is free software: you can redistribute
 *  it and/or modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
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

import uk.org.nickmoore.runtrack.database.Instantiable;

/**
 *
 */
public class Deck extends Instantiable implements Serializable {
    public String name = "";
    public Identity identity = Identity.NOISE;

    public Deck() {
        setId(0);
    }

    public Deck(long id) {
        setId(id);
    }

    @Override
    public CharSequence toCharSequence(Context context, boolean shortVersion) {
        if(isInstantiated()) {
            return name;
        }
        return Long.toString(getId());
    }

    @Override
    public String toString() {
        if (isInstantiated()) {
            return name;
        }
        return String.format("ID: %d", getId());
    }
}
