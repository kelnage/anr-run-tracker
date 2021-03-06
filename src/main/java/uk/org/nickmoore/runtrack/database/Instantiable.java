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

package uk.org.nickmoore.runtrack.database;

import java.io.Serializable;

import uk.org.nickmoore.runtrack.model.Stringable;

/**
 * An abstract class for objects that can be instantiated by the database.
 */
@SuppressWarnings("WeakerAccess")
public abstract class Instantiable implements Stringable, Comparable<Instantiable>,
        Serializable {
    private boolean instantiated = false;
    @AutoIncrement
    public long _id;

    public long getId() {
        return _id;
    }

    public void setId(long id) {
        this._id = id;
    }

    public boolean isInstantiated() {
        return instantiated;
    }

    public void instantiate() {
        instantiated = true;
    }

    public void throwIfNotInstantiated(String method) throws UninstantiatedException {
        if (!isInstantiated()) {
            throw new UninstantiatedException(method, this);
        }
    }

    @Override
    public int compareTo(Instantiable instantiable) {
        return Long.valueOf(_id).compareTo(instantiable.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Instantiable that = (Instantiable) o;

        if (_id != that._id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (_id ^ (_id >>> 32));
    }
}
