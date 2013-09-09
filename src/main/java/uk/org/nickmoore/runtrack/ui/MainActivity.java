package uk.org.nickmoore.runtrack.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
    private Button about;

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
        about = (Button) findViewById(R.id.about);
        about.setOnClickListener(this);
        converter = new SQLiteClassConverter(
                new DatabaseManager(getApplicationContext()).getWritableDatabase());
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
        } else if (view.equals(about)) {
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
        }
    }
}