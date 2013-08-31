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
import android.view.Display;
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
import uk.org.nickmoore.runtrack.database.UninstantiatedException;
import uk.org.nickmoore.runtrack.database.UnmanageableClassException;
import uk.org.nickmoore.runtrack.model.Game;
import uk.org.nickmoore.runtrack.model.Opponent;

/**
 * The activity that displays a list of games.
 */
public class GameHistoryActivity extends ListActivity implements DialogInterface.OnClickListener {
    public final static int VIEW_GAME = 1;
    private SQLiteClassConverter converter;
    private Cursor recentGames;
    private CursorAdapter adapter;
    private AlertDialog deleteDialog;
    private Game game;
    private boolean shortTitles;

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        shortTitles = display.getWidth() <= 320;
        converter = new SQLiteClassConverter(
                new DatabaseManager(getApplicationContext()).getWritableDatabase());
        recentGames = converter.findAll(Game.class, "Game.date DESC", "10",
                new SQLiteClassConverter.Join(Opponent.class, "opponent"));
        adapter = new InstantiableCursorAdapter<Game>(getApplicationContext(), recentGames,
                R.layout.game_menu, converter, Game.class,
                new InstantiableCursorAdapter.ViewRenderer<Game>() {
                    @Override
                    public void populateView(Context context, View view, Game instance) {
                        String result = instance.opponent.toString();
                        try {
                            result = String.format(getString(R.string.game_short_title),
                                    instance.gameEnd.toCharSequence(context, false),
                                    instance.getPlayerResult().toCharSequence(context, false),
                                    instance.opponent.name);
                        } catch (UninstantiatedException ex) {
                            // Just use the default toString
                        }
                        String player = String.format(getString(R.string.a_bracket_b),
                                instance.playerIdentity.toCharSequence(context, shortTitles),
                                instance.playerAgendaScore);
                        String opponent = String.format(getString(R.string.a_bracket_b),
                                instance.opponentIdentity.toCharSequence(context, shortTitles),
                                instance.opponentAgendaScore);
                        ((TextView) view.findViewById(R.id.opponent_name))
                                .setText(result);
                        ((TextView) view.findViewById(R.id.player_identity))
                                .setText(player);
                        ((TextView) view.findViewById(R.id.opponent_identity))
                                .setText(opponent);
                    }
                });
        deleteDialog = new AlertDialog.Builder(this)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.cancel, this)
                .create();
        setListAdapter(adapter);
        registerForContextMenu(getListView());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        recentGames.moveToPosition(position);
        Game game = converter.readCursor(Game.class, recentGames);
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), GameActivity.class);
        intent.putExtra("game", game);
        startActivityForResult(intent, VIEW_GAME);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case VIEW_GAME:
                recentGames.requery();
                adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.equals(getListView())) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            recentGames.moveToPosition(info.position);
            game = converter.readCursor(Game.class, recentGames);
            Log.v(getClass().getSimpleName(), game.toString());
            getMenuInflater().inflate(R.menu.game_long, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                deleteDialog.setTitle(String.format(getString(R.string.delete_game_title),
                        game.opponent.name));
                deleteDialog.setMessage(String.format(getString(R.string.delete_game),
                        game.opponent.name));
                deleteDialog.show();
                return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onClick(DialogInterface dialogInterface, int whichButton) {
        if (dialogInterface.equals(deleteDialog)) {
            switch (whichButton) {
                case DialogInterface.BUTTON_POSITIVE:
                    try {
                        converter.delete(game);
                        recentGames.requery();
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
        game = null;
        dialogInterface.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        converter.close();
    }
}