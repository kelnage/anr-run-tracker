package uk.org.nickmoore.runtrack.database;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by Nick on 04/06/2014.
 */
public class NullChoiceAdapter extends BaseAdapter {
    private Context context;
    private BaseAdapter adapter;
    private int viewId;

    public NullChoiceAdapter(Context context, BaseAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
        viewId = android.R.layout.simple_list_item_1;
    }

    public NullChoiceAdapter(Context context, BaseAdapter adapter, int viewId) {
        this.context = context;
        this.adapter = adapter;
        this.viewId = viewId;
    }

    @Override
    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        adapter.notifyDataSetInvalidated();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return adapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int position) {
        if (position == 0) {
            return true;
        }
        return adapter.isEnabled(position);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if(position == 0) {
            return LayoutInflater.from(context).inflate(viewId, parent, false);
        }
        return super.getDropDownView(position, convertView, parent);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        adapter.registerDataSetObserver(dataSetObserver);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        adapter.unregisterDataSetObserver(dataSetObserver);
    }

    @Override
    public int getCount() {
        return adapter.getCount() + 1;
    }

    @Override
    public Object getItem(int i) {
        if (i == 0) {
            return null;
        }
        return adapter.getItem(i - 1);
    }

    @Override
    public long getItemId(int i) {
        if (i == 0) {
            return Long.MIN_VALUE;
        }
        return adapter.getItemId(i);
    }

    @Override
    public boolean hasStableIds() {
        return adapter.hasStableIds();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (i == 0) {
            return LayoutInflater.from(context).inflate(viewId, viewGroup, false);
        }
        return adapter.getView(i - 1, view, viewGroup);
    }

    @Override
    public int getItemViewType(int i) {
        if (i == 0) {
            return viewId;
        }
        return adapter.getItemViewType(i - 1);
    }

    @Override
    public int getViewTypeCount() {
        return adapter.getViewTypeCount();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public BaseAdapter getAdapter() {
        return adapter;
    }
}
