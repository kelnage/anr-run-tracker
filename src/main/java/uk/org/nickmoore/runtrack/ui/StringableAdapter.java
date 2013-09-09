package uk.org.nickmoore.runtrack.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import uk.org.nickmoore.runtrack.database.Instantiable;
import uk.org.nickmoore.runtrack.model.Stringable;

/**
 * An adapter for Spinners to display String-ables created from the database.
 */
public class StringableAdapter implements SpinnerAdapter {
    private Context context;
    private Stringable[] items;
    private int view;
    private Set<DataSetObserver> observers;
    private boolean shortTitles;

    public StringableAdapter(Context context, Stringable[] items) {
        construct(context, items, android.R.layout.simple_list_item_1, false);
    }

    public StringableAdapter(Context context, Stringable[] items, boolean shortTitles) {
        construct(context, items, android.R.layout.simple_list_item_1, shortTitles);
    }

    private void construct(Context context, Stringable[] items, int view, boolean shortTitles) {
        Arrays.sort(items);
        this.context = context;
        this.view = view;
        observers = new HashSet<DataSetObserver>();
        setItems(items);
        this.shortTitles = shortTitles;
    }

    public Stringable[] getItems() {
        return items;
    }

    public void setItems(Stringable[] items) {
        Arrays.sort(items, new Comparator<Stringable>() {
            @Override
            public int compare(Stringable stringable, Stringable stringable2) {
                return stringable.toCharSequence(context, shortTitles).toString().compareTo(
                        stringable2.toCharSequence(context, shortTitles).toString());
            }
        });
        this.items = items;
        for (DataSetObserver dso : observers) {
            dso.onChanged();
        }
    }

    protected View display(int i, View view) {
        if (view == null || view.getId() != this.view) {
            view = View.inflate(context, this.view, null);
        }
        TextView text = (TextView) view.findViewById(android.R.id.text1);
        text.setText(items[i].toCharSequence(context, shortTitles));
        return view;
    }

    @Override
    public View getDropDownView(int i, View view, ViewGroup viewGroup) {
        return display(i, view);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        observers.add(dataSetObserver);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        observers.remove(dataSetObserver);
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int i) {
        return items[i];
    }

    @Override
    public long getItemId(int i) {
        // if we're looking at enums, use ordinal otherwise use the id field
        Stringable item = items[i];
        if (item instanceof Enum<?>) {
            return (long) ((Enum<?>) item).ordinal();
        }
        if (item instanceof Instantiable) {
            return ((Instantiable) item).getId();
        }
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return display(i, view);
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.length == 0;
    }

    public int getPositionForItem(Stringable needle) {
        for (int i = 0; i < items.length; i++) {
            Stringable item = items[i];
            if (item.equals(needle)) {
                return i;
            }
        }
        return -1;
    }
}
