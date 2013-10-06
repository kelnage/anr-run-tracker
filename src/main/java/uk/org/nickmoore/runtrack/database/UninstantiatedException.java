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
 * An exception thrown by Instantiable objects when they require further instantiation to proceed.
 */
public class UninstantiatedException extends Exception {
    private final String method;
    private final Instantiable instance;

    public UninstantiatedException(String method, Instantiable instance) {
        this.method = method;
        this.instance = instance;
    }

    public String getMethod() {
        return method;
    }

    public Instantiable getInstance() {
        return instance;
    }

    @Override
    public String getMessage() {
        return String.format(
                "Method %s is only defined for instantiated objects - %s is uninstantiated",
                method, instance.toString());
    }
}
