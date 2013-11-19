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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import uk.org.nickmoore.runtrack.R;
import uk.org.nickmoore.runtrack.database.DatabaseManager;
import uk.org.nickmoore.runtrack.database.InstantiableCursorAdapter;
import uk.org.nickmoore.runtrack.database.SQLiteClassConverter;
import uk.org.nickmoore.runtrack.database.UnmanageableClassException;
import uk.org.nickmoore.runtrack.model.Deck;

/**
 *
 */
public class DeckListActivity extends ListActivity implements DialogInterface.OnClickListener {
    private SQLiteClassConverter converter;
    private Cursor decks;
    private CursorAdapter adapter;
    private AlertDialog deleteDialog;
    private Deck deck;

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
        deleteDialog = new AlertDialog.Builder(this)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.cancel, this)
                .create();
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
        if (v.equals(getListView())) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            decks.moveToPosition(info.position);
            deck = converter.readCursor(Deck.class, decks);
            Log.v(getClass().getSimpleName(), deck.toString());
            getMenuInflater().inflate(R.menu.long_delete, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                deleteDialog.setTitle(getString(R.string.delete_deck_title, deck.name));
                deleteDialog.setMessage(getString(R.string.delete_deck, deck.name));
                deleteDialog.show();
                return true;
        }
        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        decks.moveToPosition(position);
        Intent result = new Intent();
        result.putExtra("deck", converter.readCursor(Deck.class, decks));
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int whichButton) {
        if (dialogInterface.equals(deleteDialog)) {
            switch (whichButton) {
                case DialogInterface.BUTTON_POSITIVE:
                    try {
                        // TODO: update all games using this deck to set their deck to null
                        converter.delete(deck);
                        decks.requery();
                        adapter.notifyDataSetChanged();
                    } catch (UnmanageableClassException ex) {
                        // ???
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // do nothing :)
                    break;
            }
        }
        deck = null;
        dialogInterface.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        converter.close();
    }
}
