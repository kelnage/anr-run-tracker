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
import android.graphics.Color;

import uk.org.nickmoore.runtrack.R;

/**
 * Represents one of the two roles in Android Netrunner - the Runner and the Corporation.
 */
@SuppressWarnings("WeakerAccess")
public enum Role implements Stringable {
    CORPORATION(R.string.corporation, Color.argb(255, 79, 79, 143)),
    RUNNER(R.string.runner, Color.argb(255, 198, 33, 33));

    public final int textId;
    public final int color;

    private Role(int textId, int color) {
        this.textId = textId;
        this.color = color;
    }

    @Override
    public CharSequence toCharSequence(Context context, boolean shortVersion) {
        return context.getText(textId);
    }
}