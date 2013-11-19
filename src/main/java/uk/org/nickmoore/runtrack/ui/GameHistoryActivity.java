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
import uk.org.nickmoore.runtrack.database.NoSuchInstanceException;
import uk.org.nickmoore.runtrack.database.SQLiteClassConverter;
import uk.org.nickmoore.runtrack.database.UninstantiatedException;
import uk.org.nickmoore.runtrack.database.UnmanageableClassException;
import uk.org.nickmoore.runtrack.model.Game;
import uk.org.nickmoore.runtrack.model.Match;
import uk.org.nickmoore.runtrack.model.Opponent;

/**
 * The activity that displays a list of games and matches.
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
        recentGames = converter.findAll(Game.class,
                "Game.match IS NULL OR Game.match = 0 OR Game._id = First_Match_firstGame",
                null, "Game.date DESC, Game.match ASC", "10",
                new SQLiteClassConverter.Join(Opponent.class, "opponent"),
                new SQLiteClassConverter.Join(Match.class, "match", "LEFT JOIN"));
        adapter = new InstantiableCursorAdapter<Game>(getApplicationContext(), recentGames,
                R.layout.game_menu, converter, Game.class,
                new InstantiableCursorAdapter.ViewRenderer<Game>() {
                    @Override
                    public void populateView(Context context, View view, Game game,
                                             Cursor cursor) {
                        Match match = null;
                        Game secondGame = null;
                        if(game.match != null && game.match.getId() != 0) {
                            view.findViewById(R.id.second_game).setVisibility(View.VISIBLE);
                            try {
                                converter.retrieve(game.match.secondGame);
                            } catch (UnmanageableClassException ex) {
                                Log.e(getClass().getSimpleName(), ex.toString());
                            } catch (NoSuchInstanceException ex) {
                                Log.e(getClass().getSimpleName(), ex.toString());
                            }
                            secondGame = game.match.secondGame;
                            match = game.match;
                            match.firstGame = game;
                            match.instantiate();
                        }
                        else {
                            // we won't have a second game, so hide it
                            view.findViewById(R.id.second_game).setVisibility(View.GONE);
                        }
                        String result = game.opponent.toString();
                        try {
                            result = String.format(getString(R.string.game_short_title),
                                    game.gameEnd.toCharSequence(context, false),
                                    game.getPlayerResult().toCharSequence(context, false),
                                    game.opponent.name);
                        } catch (UninstantiatedException ex) {
                            // Just use the default toString
                        }
                        String player = String.format(getString(R.string.a_bracket_b),
                                game.playerIdentity.toCharSequence(context, shortTitles),
                                game.playerAgendaScore);
                        String opponent = String.format(getString(R.string.a_bracket_b),
                                game.opponentIdentity.toCharSequence(context, shortTitles),
                                game.opponentAgendaScore);
                        if(secondGame != null) {
                            try {
                                result = String.format(getString(R.string.match_short_title),
                                        match.getPlayerResult().toCharSequence(context, false),
                                        game.opponent.name);
                            } catch(UninstantiatedException ex) {
                                Log.i(getClass().getSimpleName(), match.toString());
                                Log.e(getClass().getSimpleName(), ex.toString());
                            }
                            ((TextView) view.findViewById(R.id.player_identity2)).setText(
                                    String.format(getString(R.string.a_bracket_b),
                                            secondGame.playerIdentity.toCharSequence(context,
                                                    shortTitles),
                                            secondGame.playerAgendaScore));
                            ((TextView) view.findViewById(R.id.opponent_identity2)).setText(
                                    String.format(getString(R.string.a_bracket_b),
                                            secondGame.opponentIdentity.toCharSequence(context,
                                                    shortTitles),
                                            secondGame.opponentAgendaScore));
                        }
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
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), GameActivity.class);
        recentGames.moveToPosition(position);
        Game game = converter.readCursor(Game.class, recentGames);
        if(game.match != null && game.match.getId() != 0) {
            game.match.firstGame = game;
            try {
                converter.retrieve(game.match.secondGame);
                converter.retrieve(game.match.opponent);
            } catch (UnmanageableClassException e) {
                Log.e(getClass().getSimpleName(), e.toString());
            } catch (NoSuchInstanceException e) {
                Log.e(getClass().getSimpleName(), e.toString());
            }
            game.match.setCurrentGame(game);
            game.match.instantiate();
            intent.putExtra("match", game.match);
        }
        else {
            intent.putExtra("game", game);
        }
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
            getMenuInflater().inflate(R.menu.long_delete, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                if(game.match != null && game.match.getId() != 0) {
                    deleteDialog.setTitle(String.format(getString(R.string.delete_match_title),
                            game.opponent.name));
                    deleteDialog.setMessage(String.format(getString(R.string.delete_match),
                            game.opponent.name));
                }
                else {
                    deleteDialog.setTitle(String.format(getString(R.string.delete_game_title),
                            game.opponent.name));
                    deleteDialog.setMessage(String.format(getString(R.string.delete_game),
                            game.opponent.name));
                }
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
                        if(game.match != null && game.match.getId() != 0) {
                            converter.delete(game.match.secondGame);
                            converter.delete(game.match);
                        }
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