package uk.org.nickmoore.runtrack.ui;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import uk.org.nickmoore.runtrack.R;
import uk.org.nickmoore.runtrack.model.Game;
import uk.org.nickmoore.runtrack.model.Opponent;
import uk.org.nickmoore.runtrack.model.Role;

public class GameTrackingActivity extends Activity {
    private CounterView topClickCounter;
    private CounterView topCreditCounter;
    private CounterView bottomClickCounter;
    private CounterView bottomCreditCounter;
    private InvertibleTextView topLabel;
    private InvertibleTextView bottomLabel;
    private InvertibleTextView turnLabel;
    private LinearLayout topArea;
    private LinearLayout bottomArea;
    private int turn;
    private Integer orientation = null;
    private boolean keepScreenOn = false;
    private Game game;
    private Role currentPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_tracking);
        // initialise views
        topClickCounter = (CounterView) findViewById(R.id.top_click_counter);
        topCreditCounter = (CounterView) findViewById(R.id.top_credit_counter);
        bottomClickCounter = (CounterView) findViewById(R.id.bottom_click_counter);
        bottomCreditCounter = (CounterView) findViewById(R.id.bottom_credit_counter);
        topLabel = (InvertibleTextView) findViewById(R.id.top_label);
        bottomLabel = (InvertibleTextView) findViewById(R.id.bottom_label);
        turnLabel = (InvertibleTextView) findViewById(R.id.turn_label);
        topArea = (LinearLayout) findViewById(R.id.top_area);
        bottomArea = (LinearLayout) findViewById(R.id.bottom_area);
        // initial state at beginning of a game
        game = (Game) getIntent().getSerializableExtra("game");
        currentPlayer = Role.CORPORATION;
        turn = 1;
        topCreditCounter.setValue(5);
        bottomCreditCounter.setValue(5);
        loadGame();
    }
    
    private void loadGame() {
        turnLabel.setText(String.format(getString(R.string.turn_n), turn));
        if(currentPlayer != game.playerIdentity.getRole()) {
            turnLabel.setInverted(true);

        } else {
            turnLabel.setInverted(false);
        }
        switch(game.playerIdentity.getRole()) {
            case CORPORATION:
                topLabel.setText(R.string.runner);
                bottomLabel.setText(R.string.corporation);
                if(currentPlayer == Role.RUNNER) {
                    // topLabel.setBackgroundColor(Role.RUNNER.color);
                    topArea.setBackgroundResource(R.drawable.active_runner_background);
                    bottomArea.setBackground(null);
                } else {
                    // bottomLabel.setBackgroundColor(Role.CORPORATION.color);
                    bottomArea.setBackgroundResource(R.drawable.active_corporation_background);
                    topArea.setBackground(null);
                }
                break;
            case RUNNER:
                topLabel.setText(R.string.corporation);
                bottomLabel.setText(R.string.runner);
                if(currentPlayer == Role.RUNNER) {
                    // topLabel.setBackgroundColor(Role.CORPORATION.color);
                    topArea.setBackgroundResource(R.drawable.active_corporation_background);
                    bottomArea.setBackground(null);
                } else {
                    // bottomLabel.setBackgroundColor(Role.RUNNER.color);
                    bottomArea.setBackgroundResource(R.drawable.active_runner_background);
                    topArea.setBackground(null);
                }
                break;
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if(savedInstanceState.containsKey("orientation")) {
            orientation = savedInstanceState.getInt("orientation");
        } else {
            orientation = null;
        }
        keepScreenOn = savedInstanceState.getBoolean("keepScreenOn");
        game = (Game) savedInstanceState.get("game");
        currentPlayer = Role.values()[savedInstanceState.getInt("currentPlayer")];
        topCreditCounter.setValue(savedInstanceState.getInt("topCredits"));
        topClickCounter.setValue(savedInstanceState.getInt("topClicks"));
        bottomCreditCounter.setValue(savedInstanceState.getInt("bottomCredits"));
        bottomClickCounter.setValue(savedInstanceState.getInt("bottomClicks"));
        loadGame();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(orientation != null) {
            outState.putInt("orientation", orientation);
        }
        outState.putBoolean("keepScreenOn", keepScreenOn);
        outState.putSerializable("game", game);
        outState.putInt("currentPlayer", currentPlayer.ordinal());
        outState.putInt("topCredits", topCreditCounter.getValue());
        outState.putInt("topClicks", topClickCounter.getValue());
        outState.putInt("bottomCredits", bottomCreditCounter.getValue());
        outState.putInt("bottomClicks", bottomClickCounter.getValue());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.game_tracking, menu);
        return true;
    }

    @SuppressWarnings("MagicConstant")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_disable_rotation) {
            if(orientation == null) {
                orientation = getRequestedOrientation();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                item.setChecked(true);
            } else {
                setRequestedOrientation(orientation);
                orientation = null;
                item.setChecked(false);
            }
            return true;
        }
        if(item.getItemId() == R.id.action_keep_alive) {
            keepScreenOn = !keepScreenOn;
            findViewById(android.R.id.content).setKeepScreenOn(keepScreenOn);
            item.setChecked(keepScreenOn);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
