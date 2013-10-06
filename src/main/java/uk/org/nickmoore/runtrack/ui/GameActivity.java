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

package uk.org.nickmoore.runtrack.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.DateFormat;
import java.util.Calendar;

import uk.org.nickmoore.runtrack.R;
import uk.org.nickmoore.runtrack.database.DatabaseManager;
import uk.org.nickmoore.runtrack.database.NoSuchInstanceException;
import uk.org.nickmoore.runtrack.database.SQLiteClassConverter;
import uk.org.nickmoore.runtrack.database.UnmanageableClassException;
import uk.org.nickmoore.runtrack.model.Game;
import uk.org.nickmoore.runtrack.model.GameEnd;
import uk.org.nickmoore.runtrack.model.Identity;
import uk.org.nickmoore.runtrack.model.Match;
import uk.org.nickmoore.runtrack.model.Opponent;
import uk.org.nickmoore.runtrack.model.Role;

/**
 * An activity for editing and viewing games and matches.
 */
public class GameActivity extends FragmentActivity implements AdapterView.OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener, View.OnFocusChangeListener, Button.OnClickListener,
        DatePickerDialog.OnDateSetListener, SeekBar.OnSeekBarChangeListener {
    public final static int OPPONENT_REQUEST = 1;

    private boolean disableUpdates = false;
    private SQLiteClassConverter converter;
    private Match match;
    private Game game;
    private Button opponent;
    private ToggleButton playerRole;
    private Spinner playerIdentity;
    private SeekBar playerAgenda;
    private TextView playerAgendaView;
    private Spinner opponentIdentity;
    private SeekBar opponentAgenda;
    private TextView opponentAgendaView;
    private Spinner gameEnd;
    private EditText notes;
    private Button date;
    private Button save;
    private Button cancel;
    private boolean shortTitles;

    private void setupViews() {
        opponent = (Button) findViewById(R.id.opponent);
        opponent.setOnClickListener(this);
        playerRole = (ToggleButton) findViewById(R.id.playerRole);
        playerRole.setOnCheckedChangeListener(this);
        playerIdentity = (Spinner) findViewById(R.id.playerIdent);
        playerIdentity.setOnItemSelectedListener(this);
        playerAgenda = (SeekBar) findViewById(R.id.playerAP);
        playerAgenda.setOnSeekBarChangeListener(this);
        playerAgendaView = (TextView) findViewById(R.id.playerAPValue);
        opponentIdentity = (Spinner) findViewById(R.id.oppIdent);
        opponentIdentity.setOnItemSelectedListener(this);
        opponentAgenda = (SeekBar) findViewById(R.id.oppAP);
        opponentAgenda.setOnSeekBarChangeListener(this);
        opponentAgendaView = (TextView) findViewById(R.id.oppAPValue);
        gameEnd = (Spinner) findViewById(R.id.gameEnd);
        gameEnd.setOnItemSelectedListener(this);
        gameEnd.setAdapter(new StringableAdapter(this, GameEnd.values(), shortTitles) {
            @Override
            protected View display(int i, View view) {
                super.display(i, view);
                if (view == null || view.getId() != android.R.layout.simple_list_item_1) {
                    view = View.inflate(getApplicationContext(),
                            android.R.layout.simple_list_item_1, null);
                }
                GameEnd end = (GameEnd) getItem(i);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setText(end.toCharSequence(getApplicationContext(), shortTitles) + " " +
                        game.getPlayerResult(end).toCharSequence(getApplicationContext(),
                                shortTitles));
                return view;
            }
        });
        notes = (EditText) findViewById(R.id.notes);
        notes.setOnFocusChangeListener(this);
        date = (Button) findViewById(R.id.gameDate);
        date.setOnClickListener(this);
        save = (Button) findViewById(R.id.save);
        save.setOnClickListener(this);
        cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.game);
        Display display = getWindowManager().getDefaultDisplay();
        shortTitles = display.getWidth() <= 320;
        setupViews();
        converter = new SQLiteClassConverter(
                new DatabaseManager(getApplicationContext()).getWritableDatabase());
        if (getIntent().hasExtra("match")) {
            match = (Match) getIntent().getSerializableExtra("match");
            if(match == null) {
                match = new Match();
                match.firstGame = new Game();
                match.secondGame = new Game();
                match.setCurrentGame(match.firstGame);
            }
            if(match.getCurrentGame() == null) {
                match.setCurrentGame(match.firstGame);
            }
            if(game == null) {
                game = match.getCurrentGame();
            }
        }
        else if (getIntent().hasExtra("game")) {
            game = (Game) getIntent().getSerializableExtra("game");
            if (game == null) {
                game = new Game();
            } else if (!game.isInstantiated() && game.getId() != 0) {
                try {
                    converter.retrieve(game);
                } catch (UnmanageableClassException ex) {
                    // TODO handle exception
                } catch (NoSuchInstanceException ex) {
                    // TODO handle exception
                }
            }
        } else {
            game = new Game();
        }
        if (game.opponent.getId() == 0) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), OpponentActivity.class);
            startActivityForResult(intent, OPPONENT_REQUEST);
        }
        loadGame();
    }

    private void loadGame() {
        if(match == null) {
            setTitle(String.format(getString(R.string.game_activity_label),
                    game.getId() == 0 ? "Create" : "Edit"));
        }
        else {
            setTitle(String.format(getString(R.string.match_activity_label),
                    game.getId() == 0 ? "Create" : "Edit",
                    game == match.firstGame ? 1 : 2));
        }
        disableUpdates = true;
        opponent.setText(game.opponent.name);
        playerRole.setChecked(game.playerIdentity.faction.getRole().equals(Role.CORPORATION));
        updateIdentities(playerRole.isChecked());
        playerIdentity.setSelection(((StringableAdapter) playerIdentity.getAdapter())
                .getPositionForItem(game.playerIdentity), false);
        playerAgenda.setProgress(game.playerAgendaScore);
        playerAgendaView.setText(Integer.toString(game.playerAgendaScore));
        opponentIdentity.setSelection(((StringableAdapter) opponentIdentity.getAdapter())
                .getPositionForItem(game.opponentIdentity), false);
        opponentAgenda.setProgress(game.opponentAgendaScore);
        opponentAgendaView.setText(Integer.toString(game.opponentAgendaScore));
        gameEnd.setSelection(((StringableAdapter) gameEnd.getAdapter())
                .getPositionForItem(game.gameEnd), false);
        notes.setText(game.notes);
        date.setText(DateFormat.getDateInstance().format(game.getDate()));
        if(match != null) {
            // intentionally using memory references here
            if(match.firstGame == game) {
                cancel.setText(R.string.cancel);
                save.setText(R.string.next);
                opponent.setEnabled(true);
                playerRole.setEnabled(true);
                date.setEnabled(true);
            }
            if(match.secondGame == game) {
                cancel.setText(R.string.back);
                save.setText(R.string.save);
                opponent.setEnabled(false);
                playerRole.setEnabled(false);
                date.setEnabled(false);
            }
        }
        disableUpdates = false;
    }

    private void updateIdentities(boolean isChecked) {
        int playerPos = playerIdentity.getSelectedItemPosition();
        int opponentPos = opponentIdentity.getSelectedItemPosition();
        Role playerRole = isChecked ? Role.CORPORATION : Role.RUNNER;
        Role opponentRole = !isChecked ? Role.CORPORATION : Role.RUNNER;
        playerIdentity.setAdapter(new StringableAdapter(this, Identity.getIdentities(playerRole),
                shortTitles));
        opponentIdentity.setAdapter(new StringableAdapter(this,
                Identity.getIdentities(opponentRole), shortTitles));
        if(!disableUpdates) {
            playerIdentity.setSelection(opponentPos, false);
            opponentIdentity.setSelection(playerPos, false);
            game.playerIdentity = (Identity) playerIdentity.getSelectedItem();
            game.opponentIdentity = (Identity) opponentIdentity.getSelectedItem();
            if(game.playerAgendaScore != 0 || game.opponentAgendaScore != 0) {
                new AlertDialog.Builder(this)
                    .setTitle(R.string.swap_scores)
                    .setMessage(R.string.swap_scores_long)
                    .setCancelable(true)
                    .setPositiveButton(R.string.swap, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int swap = game.playerAgendaScore;
                            playerAgenda.setProgress(game.opponentAgendaScore);
                            opponentAgenda.setProgress(swap);
                        }
                    })
                    .setNegativeButton(R.string.dont_swap, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (disableUpdates) return;
        if (adapterView.equals(playerIdentity)) {
            game.playerIdentity = (Identity) playerIdentity.getItemAtPosition(i);
        } else if (adapterView.equals(opponentIdentity)) {
            game.opponentIdentity = (Identity) opponentIdentity.getItemAtPosition(i);
        } else if (adapterView.equals(gameEnd)) {
            game.gameEnd = (GameEnd) gameEnd.getItemAtPosition(i);
            if(match != null) {
                if(game.gameEnd == GameEnd.TIMEOUT && game == match.firstGame) {
                    save.setText(R.string.save);
                }
                else if(game == match.firstGame) {
                    save.setText(R.string.next);
                }
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // do nothing?
    }

    private void updateNotes() {
        Editable note = notes.getText();
        if (note != null) {
            game.notes = note.toString();
        } else {
            game.notes = "";
        }
    }

    private void updateAgendaScores(SeekBar seekBar) {
        if(disableUpdates) return;
        if (seekBar.equals(playerAgenda)) {
            game.playerAgendaScore = playerAgenda.getProgress();
            playerAgendaView.setText(Integer.toString(game.playerAgendaScore));
        }
        if (seekBar.equals(opponentAgenda)) {
            game.opponentAgendaScore = opponentAgenda.getProgress();
            opponentAgendaView.setText(Integer.toString(game.opponentAgendaScore));
        }
        // will notify DataSetObservers - TODO: can this be done better?
        ((StringableAdapter) gameEnd.getAdapter()).setItems(GameEnd.values());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        updateAgendaScores(seekBar);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // we don't need to do anything
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        updateAgendaScores(seekBar);
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (disableUpdates) return;
        if (view.equals(notes)) {
            updateNotes();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (disableUpdates) return;
        updateIdentities(b);
        // will notify DataSetObservers - TODO: can this be done better?
        ((StringableAdapter) gameEnd.getAdapter()).setItems(GameEnd.values());
    }

    @Override
    public void onClick(View view) {
        if (view.equals(save)) {
            updateNotes();
            updateAgendaScores(playerAgenda);
            updateAgendaScores(opponentAgenda);
            if(match == null) {
                try {
                    converter.store(game);
                    finish();
                } catch (UnmanageableClassException ex) {
                    //
                }
            }
            else {
                if(match.firstGame == game) {
                    if(game.gameEnd == GameEnd.TIMEOUT) {
                        if(match.getId() == 0) {
                            game.match = null;
                            try {
                                converter.store(game);
                                finish();
                            } catch (UnmanageableClassException ex) {
                                //
                            }
                        }
                        else {
                            new AlertDialog.Builder(this)
                                    .setTitle(R.string.delete_second_game_title)
                                    .setMessage(R.string.delete_second_game)
                                    .setPositiveButton(R.string.delete,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int i) {
                                                    try {
                                                        converter.delete(match.secondGame);
                                                        converter.delete(match);
                                                        game.match = null;
                                                        converter.store(game);
                                                        finish();
                                                    } catch (UnmanageableClassException ex) {
                                                        //
                                                    }
                                                }
                                            })
                                    .setNegativeButton(R.string.cancel,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int i) {
                                                    dialog.dismiss();
                                                }
                                            })
                                    .show();
                            return;
                        }
                    }
                    else {
                        match.opponent = match.firstGame.opponent;
                        match.secondGame.opponent = match.opponent;
                        match.secondGame.date = match.firstGame.date;
                        if(match.secondGame.getId() == 0) {
                            if(match.firstGame.playerIdentity.faction.getRole() == Role.CORPORATION) {
                                match.secondGame.playerIdentity =
                                        Identity.getIdentities(Role.RUNNER)[0];
                            }
                            else {
                                match.secondGame.playerIdentity =
                                        Identity.getIdentities(Role.CORPORATION)[0];
                            }
                        }
                        game = match.secondGame;
                        match.setCurrentGame(match.secondGame);
                        loadGame();
                    }
                }
                else if(match.secondGame == game) {
                    try {
                        converter.store(match.firstGame);
                        converter.store(match.secondGame);
                        converter.store(match);
                        // this is a nasty hack to set the Match - preferably the
                        // SQLiteClassConverter would do something clever here!
                        if(match.firstGame.match == null || match.firstGame.match.getId() == 0) {
                            match.firstGame.match = match;
                            converter.update(match.firstGame);
                            match.secondGame.match = match;
                            converter.update(match.secondGame);
                        }
                    } catch(UnmanageableClassException ex) {
                        //
                    }
                    finish();
                }
            }
        }
        if (view.equals(cancel)) {
            if(match != null && match.secondGame == game) {
                match.setCurrentGame(match.firstGame);
                game = match.firstGame;
                loadGame();
            }
            else {
                setResult(RESULT_CANCELED);
                finish();
            }
        }
        if (view.equals(opponent) && opponent.isEnabled()) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), OpponentActivity.class);
            startActivityForResult(intent, OPPONENT_REQUEST);
        }
        if (view.equals(date)) {
            final int oldDate = game.date;
            final GameActivity parent = this;
            DialogFragment datePicker = new DialogFragment() {
                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(game.getDate());
                    return new DatePickerDialog(getActivity(), parent,
                            c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                            c.get(Calendar.DAY_OF_MONTH));
                }

                @Override
                public void onCancel(DialogInterface dialog) {
                    super.onCancel(dialog);
                    game.date = oldDate;
                }

                @Override
                public void onDismiss(DialogInterface dialog) {
                    super.onDismiss(dialog);
                    loadGame();
                }
            };
            datePicker.show(getSupportFragmentManager(), "datePicker");
        }
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        game.setDate(c.getTime());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case GameActivity.OPPONENT_REQUEST:
                switch (resultCode) {
                    case RESULT_OK:
                        game.opponent = (Opponent) data.getSerializableExtra("opponent");
                        loadGame();
                        break;
                    default:
                        if (game.opponent.getId() == 0) {
                            finish();
                        }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        converter.close();
    }
}
