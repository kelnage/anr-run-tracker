package uk.org.nickmoore.runtrack.database;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.TreeMap;

import uk.org.nickmoore.runtrack.R;

/**
 * Created by Nick on 07/06/2014.
 */
public class DateDividedCursorAdapter<T extends Instantiable> extends InstantiableCursorAdapter<T> {
    private TreeMap<Integer, Object> positions;
    private Context context;

    public DateDividedCursorAdapter(Context context, Cursor cursor, int view,
                                    SQLiteClassConverter converter, Class<T> clazz,
                                    ViewRenderer<T> viewRenderer, String dateColumn) {
        super(context, cursor, view, converter, clazz, viewRenderer);
        this.context = context;
        positions = new TreeMap<Integer, Object>();
        DateFormat dateFormat = DateFormat.getDateInstance();
        int dateColumnIndex = cursor.getColumnIndex(dateColumn);
        cursor.moveToFirst();
        int position = 0;
        int cursorPosition = 0;
        Date lastDate = null;
        do {
            Date currentDate = new Date(cursor.getInt(dateColumnIndex) * 1000l);
            if(lastDate == null ||
                    !dateFormat.format(currentDate).equals(dateFormat.format(lastDate))) {
                positions.put(position, currentDate);
                position++;
            }
            lastDate = currentDate;
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
        if(pos instanceof Date) {
            return pos;
        }
        return super.getItem((Integer) pos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object pos = positions.get(position);
        if(pos instanceof Date) {
            return LayoutInflater.from(context).inflate(R.layout.game_divider, parent, false);
        }
        return super.getView((Integer) pos, convertView, parent);
    }

    @Override
    public int getItemViewType(int position) {
        Object pos = positions.get(position);
        if(pos instanceof Date) {
            return R.layout.game_divider;
        }
        return super.getItemViewType((Integer) pos);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int position) {
        Object pos = positions.get(position);
        if(pos instanceof Date) {
            return ((Date) pos).getTime();
        }
        return super.getItemId((Integer) pos);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if(view.getId() == R.layout.game_divider) {
            ((TextView) view.findViewById(android.R.id.text1)).setText("TESTING");
            return;
        }
        super.bindView(view, context, cursor);
    }
}
