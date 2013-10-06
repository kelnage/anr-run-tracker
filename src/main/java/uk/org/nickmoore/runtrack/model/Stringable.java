/*
 *  Copyright (C) 2013 Nick Moore
 *
 *  This file is part of RunTrack
 *
 *  RunTrack is free software: you can redistribute it and/or modify
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

/**
 * An interface for objects that can be displayed in the user interface - often using resource IDs
 * (hence the Context) and they may provide a shortVersion for small screens (<300px wide).
 */
public interface Stringable {
    public CharSequence toCharSequence(Context context, boolean shortVersion);
}
