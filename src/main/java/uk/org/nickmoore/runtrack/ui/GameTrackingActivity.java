package uk.org.nickmoore.runtrack.ui;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import uk.org.nickmoore.runtrack.R;

public class GameTrackingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_tracking);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.game_tracking, menu);
        return true;
    }
    
}
