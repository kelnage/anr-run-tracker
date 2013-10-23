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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import uk.org.nickmoore.runtrack.R;
import uk.org.nickmoore.runtrack.database.DatabaseManager;
import uk.org.nickmoore.runtrack.database.SQLiteClassConverter;
import uk.org.nickmoore.runtrack.model.Game;
import uk.org.nickmoore.runtrack.model.Match;

/**
 * The main startup activity.
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private SQLiteClassConverter converter;
    private Button newGame;
    private Button newMatch;
    private Button statistics;
    private Button history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        newGame = (Button) findViewById(R.id.new_game);
        newGame.setOnClickListener(this);
        newMatch = (Button) findViewById(R.id.new_match);
        newMatch.setOnClickListener(this);
        statistics = (Button) findViewById(R.id.statistics);
        statistics.setOnClickListener(this);
        history = (Button) findViewById(R.id.history);
        history.setOnClickListener(this);
        converter = new SQLiteClassConverter(
                new DatabaseManager(getApplicationContext()).getWritableDatabase());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.about:
                new AlertDialog.Builder(this)
                        .setMessage(R.string.about_text)
                        .setPositiveButton(R.string.close,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                                        dialogInterface.dismiss();
                                    }
                                }
                        )
                        .show();
                return true;
            case R.id.backup_restore:
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), BackupActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        converter.close();
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        if (view.equals(newGame)) {
            intent.setClass(getApplicationContext(), GameActivity.class);
            intent.putExtra("game", new Game());
            startActivity(intent);
        } else if(view.equals(newMatch)) {
            intent.setClass(getApplicationContext(), GameActivity.class);
            Match match = new Match();
            match.firstGame = new Game();
            match.secondGame = new Game();
            match.setCurrentGame(match.firstGame);
            intent.putExtra("match", match);
            startActivity(intent);
        } else if (view.equals(history)) {
            intent.setClass(getApplicationContext(), GameHistoryActivity.class);
            startActivity(intent);
        } else if (view.equals(statistics)) {
            intent.setClass(getApplicationContext(), PlayerStatsActivity.class);
            startActivity(intent);
        }
    }
}