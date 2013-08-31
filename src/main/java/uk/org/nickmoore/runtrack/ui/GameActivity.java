package uk.org.nickmoore.runtrack.ui;

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
import uk.org.nickmoore.runtrack.model.Opponent;
import uk.org.nickmoore.runtrack.model.Role;

/**
 * An activity for editing and viewing games.
 */
public class GameActivity extends FragmentActivity implements AdapterView.OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener, View.OnFocusChangeListener, Button.OnClickListener,
        DatePickerDialog.OnDateSetListener, SeekBar.OnSeekBarChangeListener {
    public final static int OPPONENT_REQUEST = 1;

    private boolean disableUpdates = false;
    private SQLiteClassConverter converter;
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
        gameEnd.setAdapter(new StringableAdapter(this, GameEnd.values(), shortTitles));
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
        if (getIntent().hasExtra("game")) {
            game = (Game) getIntent().getSerializableExtra("game");
            if (game == null) {
                game = new Game();
            } else if (!game.isInstantiated() && game.getId() != -1) {
                try {
                    converter.retrieve(game);
                } catch (UnmanageableClassException ex) {
                    // TODO
                } catch (NoSuchInstanceException ex) {
                    // TODO
                }
            }
        } else {
            game = new Game();
        }
        if (game.opponent.getId() == -1) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), OpponentActivity.class);
            startActivityForResult(intent, OPPONENT_REQUEST);
        }
        loadGame();
    }

    private void loadGame() {
        disableUpdates = true;
        Log.i(getClass().getSimpleName(), game.opponent.toString());
        opponent.setText(game.opponent.name);
        playerRole.setChecked(game.playerIdentity.faction.getRole().equals(Role.CORPORATION));
        updateIdentities(playerRole.isChecked());
        playerIdentity.setSelection(((StringableAdapter) playerIdentity.getAdapter())
                .getPositionForItem(game.playerIdentity), false);
        playerAgenda.setProgress(game.playerAgendaScore);
        opponentIdentity.setSelection(((StringableAdapter) opponentIdentity.getAdapter())
                .getPositionForItem(game.opponentIdentity), false);
        opponentAgenda.setProgress(game.opponentAgendaScore);
        gameEnd.setSelection(((StringableAdapter) gameEnd.getAdapter())
                .getPositionForItem(game.gameEnd), false);
        notes.setText(game.notes);
        date.setText(DateFormat.getDateInstance().format(game.getDate()));
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
        if (!disableUpdates) {
            playerIdentity.setSelection(opponentPos, false);
            opponentIdentity.setSelection(playerPos, false);
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
        if (seekBar.equals(playerAgenda)) {
            game.playerAgendaScore = playerAgenda.getProgress();
            playerAgendaView.setText(Integer.toString(game.playerAgendaScore));
        }
        if (seekBar.equals(opponentAgenda)) {
            game.opponentAgendaScore = opponentAgenda.getProgress();
            opponentAgendaView.setText(Integer.toString(game.opponentAgendaScore));
        }
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
    }

    @Override
    public void onClick(View view) {
        if (view.equals(save)) {
            updateNotes();
            updateAgendaScores(playerAgenda);
            updateAgendaScores(opponentAgenda);
            try {
                converter.store(game);
                finish();
            } catch (UnmanageableClassException ex) {
                // WTF?
            }
        }
        if (view.equals(cancel)) {
            finish();
        }
        if (view.equals(opponent)) {
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
                            c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
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
                        if (game.opponent.getId() == -1) {
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
