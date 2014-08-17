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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.TreeMap;

import uk.org.nickmoore.runtrack.R;

/**
 * An instantiable cursor adapter that groups items by a given column.
 */
public class DividedCursorAdapter<T extends Instantiable> extends InstantiableCursorAdapter<T> {
    public interface GroupingFunction {
        public String group(Cursor cursor);
    }

    private TreeMap<Integer, Object> positions;
    private Context context;
    private GroupingFunction grouper;

    public DividedCursorAdapter(Context context, Cursor cursor, int view,
                                SQLiteClassConverter converter, Class<T> clazz,
                                ViewRenderer<T> viewRenderer, GroupingFunction grouper) {
        super(context, cursor, view, converter, clazz, viewRenderer);
        this.context = context;
        this.grouper = grouper;
        positions = new TreeMap<Integer, Object>();
        loadPositions();
    }

    private void loadPositions() {
        Cursor cursor = getCursor();
        if(cursor.getCount() == 0) {
            return;
        }
        cursor.moveToFirst();
        int position = 0;
        int cursorPosition = 0;
        String lastValue = null;
        do {
            String currentValue = grouper.group(cursor);
            if(lastValue == null ||
                    !currentValue.equals(lastValue)) {
                positions.put(position, currentValue);
                position++;
            }
            lastValue = currentValue;
            positions.put(position, cursorPosition);
            cursorPosition++;
            position++;
        } while(cursor.moveToNext());
    }

    @Override
    public int getCount() {
        return positions.size();
    }

    @Override
    public Object getItem(int position) {
        Object pos = positions.get(position);
        if(pos instanceof String) {
            return pos;
        }
        return super.getItem((Integer) pos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object pos = positions.get(position);
        if(pos instanceof String) {
            View view = LayoutInflater.from(context).inflate(R.layout.game_divider, parent, false);
            ((TextView) view.findViewById(android.R.id.text1)).setText((String) pos);
            return view;
        }
        return super.getView((Integer) pos, convertView, parent);
    }

    @Override
    public int getItemViewType(int position) {
        Object pos = positions.get(position);
        if(pos instanceof String) {
            return 1;
        }
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int position) {
        Object pos = positions.get(position);
        if(pos instanceof String) {
            return pos.hashCode();
        }
        return super.getItemId((Integer) pos);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
    }

    @Override
    public void notifyDataSetChanged() {
        positions.clear();
        loadPositions();
        super.notifyDataSetChanged();
    }
}
