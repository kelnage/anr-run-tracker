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
    public void bindView(View view, Context context, Cursor cursor) {
        T instance = converter.readCursor(clazz, cursor);
        // Log.d(getClass().getSimpleName(), instance.toString());
        viewRenderer.populateView(context, view, instance, cursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(this.view, viewGroup, false);
    }
}
