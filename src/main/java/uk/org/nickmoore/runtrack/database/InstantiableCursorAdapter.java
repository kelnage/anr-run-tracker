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

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

/**
 * A cursor adapter for Instantiable objects from the database.
 */
public class InstantiableCursorAdapter<T extends Instantiable> extends CursorAdapter {
    public static interface ViewRenderer<T> {
        public void populateView(Context context, View view, T instance, Cursor cursor);
    }

    private final SQLiteClassConverter converter;
    private final ViewRenderer<T> viewRenderer;
    private final Class<T> clazz;
    private final int view;

    @SuppressWarnings("deprecation")
    public InstantiableCursorAdapter(Context context, Cursor cursor, int view,
                                     SQLiteClassConverter converter, Class<T> clazz,
                                     ViewRenderer<T> viewRenderer) {
        super(context, cursor);
        this.view = view;
        this.viewRenderer = viewRenderer;
        this.converter = converter;
        this.clazz = clazz;
    }

    @Override
    public Object getItem(int position) {
        return converter.readCursor(clazz, (Cursor) super.getItem(position));
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        T instance = converter.readCursor(clazz, cursor);
        // Log.d(getClass().getSimpleName(), instance.toString());
        viewRenderer.populateView(context, view, instance, cursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(this.view, viewGroup, false);
    }

    public int getPositionForItem(T needle) {
        Log.i(getClass().getSimpleName(), "searching for " + needle.toString());
        Cursor cursor = getCursor();
        cursor.moveToFirst();
        int index = 1;
        do {
            if(cursor.getLong(cursor.getColumnIndex("_id")) == needle.getId()) {
                Log.i(getClass().getSimpleName(), "found " + needle.toString() + " at " + Integer.toString(index));
                return index;
            }
            index++;
        } while(cursor.move(1));
        Log.i(getClass().getSimpleName(), "could not find " + needle.toString());
        return -1;
    }
}
