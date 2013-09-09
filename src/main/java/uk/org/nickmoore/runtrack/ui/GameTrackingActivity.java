package uk.org.nickmoore.runtrack.ui;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import uk.org.nickmoore.runtrack.R;

public class GameTrackingActivity extends Activity {
    private Integer orientation = null;
    private boolean keepScreenOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_tracking);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if(savedInstanceState.containsKey("orientation")) {
            orientation = savedInstanceState.getInt("orientation");
        } else {
            orientation = null;
        }
        keepScreenOn = savedInstanceState.getBoolean("keepScreenOn");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(orientation != null) {
            outState.putInt("orientation", orientation);
        }
        outState.putBoolean("keepScreenOn", keepScreenOn);
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
