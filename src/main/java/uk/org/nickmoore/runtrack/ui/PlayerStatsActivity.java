package uk.org.nickmoore.runtrack.ui;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;

import java.util.Map;

import uk.org.nickmoore.runtrack.R;
import uk.org.nickmoore.runtrack.database.DatabaseManager;
import uk.org.nickmoore.runtrack.model.Faction;
import uk.org.nickmoore.runtrack.model.Game;
import uk.org.nickmoore.runtrack.model.Role;
import uk.org.nickmoore.runtrack.model.WinRatio;

/**
 * Displays statistics for a player by role, faction, identity
 */
public class PlayerStatsActivity extends Activity {
    private PieChart roleChart;
    private PieChart runnerChart;
    private PieChart corpChart;
    private TextView overallRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_stats);
        roleChart = (PieChart) findViewById(R.id.role_win_rate);
        runnerChart = (PieChart) findViewById(R.id.runner_faction_win_rate);
        corpChart = (PieChart) findViewById(R.id.corp_faction_win_rate);
        overallRate = (TextView) findViewById(R.id.overall_win_rate);

        DatabaseManager manager = new DatabaseManager(getApplicationContext());
        SQLiteDatabase db = manager.getWritableDatabase();
        WinRatio overall = Game.getWinRatio(db);
        Map<Role, WinRatio> roleStats = Game.getWinRatiosBy(db, Role.class);
        Map<Faction, WinRatio> factionStats = Game.getWinRatiosBy(db, Faction.class);

        overallRate.setText(String.format("%d%% (%d/%d)",
                Math.round(((float) overall.getWins()) / ((float) overall.getTotal()) * 100f),
                overall.getWins(), overall.getTotal()));

        for (Map.Entry<Role, WinRatio> entry : roleStats.entrySet()) {
            Role r = entry.getKey();
            WinRatio wr = entry.getValue();
            roleChart.addSegment(new Segment(
                    r.toCharSequence(getApplicationContext(), false).toString(),
                    wr.getWins()), new SegmentFormatter(r.color));
            Log.i(getClass().getSimpleName(), String.format("Added %s (%d)",
                    r.toCharSequence(this, false), wr.getWins()));
        }
        roleChart.getBorderPaint().setColor(Color.TRANSPARENT);
        roleChart.getBackgroundPaint().setColor(Color.TRANSPARENT);

        for (Map.Entry<Faction, WinRatio> entry : factionStats.entrySet()) {
            Faction faction = entry.getKey();
            WinRatio wr = entry.getValue();
            if (faction.getRole().equals(Role.RUNNER)) {
                runnerChart.addSegment(new Segment(
                        faction.toCharSequence(getApplicationContext(), false).toString(),
                        wr.getWins()), new SegmentFormatter(faction.color));
            } else {
                corpChart.addSegment(new Segment(
                        faction.toCharSequence(getApplicationContext(), false).toString(),
                        wr.getWins()), new SegmentFormatter(faction.color));
            }
            Log.d(getClass().getSimpleName(), String.format("Added %s (%d)",
                    faction.toCharSequence(this, false), wr.getWins()));
        }
        runnerChart.getBorderPaint().setColor(Color.TRANSPARENT);
        runnerChart.getBackgroundPaint().setColor(Color.TRANSPARENT);
        corpChart.getBorderPaint().setColor(Color.TRANSPARENT);
        corpChart.getBackgroundPaint().setColor(Color.TRANSPARENT);

        if (db != null) {
            db.close();
        }
    }
}
