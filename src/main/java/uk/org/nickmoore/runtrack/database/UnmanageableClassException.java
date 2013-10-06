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

/**
 * An exception thrown by the ClassConverter when classes are provided that cannot be handled.
 */
public class UnmanageableClassException extends Exception {
    private final Class clazz;

    public UnmanageableClassException(Class clazz) {
        this.clazz = clazz;
    }

    public Class getClazz() {
        return clazz;
    }

    @Override
    public String toString() {
        return String.format("Class %s could not be managed automatically", clazz.getName());
    }
}
