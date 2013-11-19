/*
 *  Copyright (C) 2013 Nick Moore
 *
 *  This file is part of RunTrack
 *
 *  RunTrack is free software: you can redistribute
 *  it and/or modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.org.nickmoore.runtrack.ui;

import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import uk.org.nickmoore.runtrack.R;
import uk.org.nickmoore.runtrack.database.DatabaseManager;
import uk.org.nickmoore.runtrack.database.InstantiableCursorAdapter;
import uk.org.nickmoore.runtrack.database.SQLiteClassConverter;
import uk.org.nickmoore.runtrack.model.Deck;

/**
 *
 */
public class DeckListActivity extends ListActivity implements DialogInterface.OnClickListener {
    private SQLiteClassConverter converter;
    private Cursor decks;
    private CursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        converter = new SQLiteClassConverter(
                new DatabaseManager(getApplicationContext()).getWritableDatabase());
        decks = converter.findAll(Deck.class, "name");
        adapter = new InstantiableCursorAdapter<Deck>(getApplicationContext(), decks,
                android.R.layout.simple_list_item_2, converter, Deck.class,
                new InstantiableCursorAdapter.ViewRenderer<Deck>() {
                    @Override
                    public void populateView(Context context, View view, Deck instance, Cursor cursor) {
                        ((TextView) view.findViewById(android.R.id.text1)).setText(instance.name);
                        ((TextView) view.findViewById(android.R.id.text2)).setText(
                                instance.identity.toCharSequence(context, false));
                    }
                });
        setListAdapter(adapter);
        if(decks.getCount() == 0) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), DeckActivity.class);
            intent.putExtra("deck", new Deck());
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        decks.requery();
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.decklist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.add_deck) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), DeckActivity.class);
            intent.putExtra("deck", new Deck());
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        decks.moveToPosition(i);
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), DeckActivity.class);
        intent.putExtra("deck", converter.readCursor(Deck.class, decks));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        converter.close();
    }
}
