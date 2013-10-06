package uk.org.nickmoore.runtrack.ui;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import uk.org.nickmoore.runtrack.R;
import uk.org.nickmoore.runtrack.database.DatabaseManager;
import uk.org.nickmoore.runtrack.database.SQLiteClassConverter;
import uk.org.nickmoore.runtrack.database.UnmanageableClassException;
import uk.org.nickmoore.runtrack.model.Opponent;

/**
 * A ListActivity that displays the available Opponents and allows the creation of new Opponents.
 */
public class OpponentActivity extends ListActivity implements DialogInterface.OnClickListener {
    private Opponent opponent;
    private EditText nameInput;
    private SQLiteClassConverter converter;
    private SimpleCursorAdapter adapter;
    private AlertDialog editDialog;
    private AlertDialog deleteDialog;
    private Cursor opponents;

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nameInput = new EditText(this);
        nameInput.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        converter = new SQLiteClassConverter(
                new DatabaseManager(getApplicationContext()).getWritableDatabase());
        opponents = converter.findAll(Opponent.class, "name");
        adapter = new SimpleCursorAdapter(getApplicationContext(),
                android.R.layout.simple_list_item_1,
                opponents,
                new String[]{"name"}, new int[]{android.R.id.text1});
        setListAdapter(adapter);
        editDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.add_opponent)
                .setMessage(R.string.add_opponent)
                .setView(nameInput)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.cancel, this)
                .create();
        deleteDialog = new AlertDialog.Builder(this)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.cancel, this)
                .create();
        if (converter.getCount(Opponent.class) == 0) {
            editOpponent(new Opponent());
        }
        registerForContextMenu(getListView());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.opponent, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_opponent) {
            editOpponent(new Opponent());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.equals(getListView())) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            opponents.moveToPosition(info.position);
            opponent = converter.readCursor(Opponent.class, opponents);
            Log.v(getClass().getSimpleName(), opponent.toString());
            getMenuInflater().inflate(R.menu.opponent_long, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                editOpponent(opponent);
                return true;
            case R.id.delete:
                deleteDialog.setTitle(String.format(getString(R.string.delete_opponent_title),
                        opponent.name));
                deleteDialog.setMessage(String.format(getString(R.string.delete_opponent),
                        opponent.name));
                deleteDialog.show();
                return true;
        }
        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        opponents.moveToPosition(position);
        Log.i(getClass().getSimpleName(), String.format("%s: %d, %s: %s", opponents.getColumnName(0),
                opponents.getLong(0), opponents.getColumnName(1), opponents.getString(1)));
        Intent result = new Intent();
        result.putExtra("opponent", converter.readCursor(Opponent.class, opponents));
        setResult(RESULT_OK, result);
        finish();
    }

    private void editOpponent(Opponent opponent) {
        this.opponent = opponent;
        nameInput.setText(this.opponent.name);
        editDialog.show();
        nameInput.requestFocus();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onClick(DialogInterface dialogInterface, int whichButton) {
        if (dialogInterface.equals(editDialog)) {
            switch (whichButton) {
                case DialogInterface.BUTTON_POSITIVE:
                    Editable nameValue = nameInput.getText();
                    if (nameValue == null || nameValue.toString() == null) {
                        Toast.makeText(getApplication(), R.string.provide_name, Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                    opponent.name = nameValue.toString();
                    try {
                        converter.store(opponent);
                        opponents.requery();
                        adapter.notifyDataSetChanged();
                    } catch (UnmanageableClassException ex) {
                        // TODO handle exception
                        Log.e(getClass().getSimpleName(), "Could not store the instance");
                        ex.printStackTrace();
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
        if (dialogInterface.equals(deleteDialog)) {
            switch (whichButton) {
                case DialogInterface.BUTTON_POSITIVE:
                    try {
                        converter.delete(opponent);
                        opponents.requery();
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
        opponent = null;
        dialogInterface.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        converter.close();
    }
}