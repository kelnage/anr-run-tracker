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

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import uk.org.nickmoore.runtrack.R;
import uk.org.nickmoore.runtrack.database.DatabaseManager;
import uk.org.nickmoore.runtrack.database.NoSuchInstanceException;
import uk.org.nickmoore.runtrack.database.SQLiteClassConverter;
import uk.org.nickmoore.runtrack.database.UnmanageableClassException;
import uk.org.nickmoore.runtrack.model.Deck;
import uk.org.nickmoore.runtrack.model.Identity;

/**
 *
 */
public class DeckActivity extends Activity implements Button.OnClickListener,
        EditText.OnFocusChangeListener, Spinner.OnItemSelectedListener {
    private boolean disableUpdates = false;
    private Deck deck;
    private Button cancel;
    private Button confirm;
    private EditText deckName;
    private EditText notes;
    private Spinner identity;
    private SQLiteClassConverter converter;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deck);
        Display display = getWindowManager().getDefaultDisplay();
        boolean shortTitles = display.getWidth() <= 320;
        cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
        confirm = (Button) findViewById(R.id.save);
        confirm.setOnClickListener(this);
        deckName = (EditText) findViewById(R.id.deck_name);
        deckName.setOnFocusChangeListener(this);
        notes = (EditText) findViewById(R.id.notes);
        notes.setOnFocusChangeListener(this);
        identity = (Spinner) findViewById(R.id.identity);
        identity.setOnItemSelectedListener(this);
        identity.setAdapter(new StringableAdapter(getApplicationContext(), Identity.getIdentities(),
                shortTitles));
        converter = new SQLiteClassConverter(
                new DatabaseManager(getApplicationContext()).getWritableDatabase());
        if(getIntent().hasExtra("deck")) {
            deck = (Deck) getIntent().getSerializableExtra("deck");
            if(deck == null) {
                deck = new Deck();
            }
            else if(!deck.isInstantiated()) {
                try {
                    converter.retrieve(deck);
                } catch(UnmanageableClassException ex) {
                    // TODO
                } catch(NoSuchInstanceException ex) {
                    // TODO
                }
            }
        }
        else {
            deck = new Deck();
        }
        loadDeck();
    }

    private void loadDeck() {
        disableUpdates = true;
        deckName.setText(deck.name);
        identity.setSelection(
                ((StringableAdapter) identity.getAdapter()).getPositionForItem(deck.identity),
                false);
        notes.setText(deck.notes);
        disableUpdates = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(getString(R.string.deck_activity_label,
                (deck == null || deck.getId() == 0) ? "Create" : "Edit"));
    }

    @Override
    public void onClick(View view) {
        if(view.equals(cancel)) {
            finish();
        }
        if(view.equals(confirm)) {
            updateDeckName();
            updateNotes();
            try {
                converter.store(deck);
                finish();
            } catch(UnmanageableClassException ex) {
                // TODO
            }
        }
    }

    private void updateDeckName() {
        if(deckName.getText() != null) {
            deck.name = deckName.getText().toString();
        }
        else {
            deck.name = "";
        }
    }

    private void updateNotes() {
        if(notes.getText() != null) {
            deck.notes = notes.getText().toString();
        }
        else {
            deck.notes = "";
        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if(disableUpdates) return;
        if(view.equals(deckName)) {
            updateDeckName();
        }
        if(view.equals(notes)) {
            updateNotes();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(disableUpdates) return;
        if(adapterView.equals(identity)) {
            deck.identity = (Identity) identity.getItemAtPosition(i);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // TODO
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
