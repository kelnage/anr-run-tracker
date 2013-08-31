package uk.org.nickmoore.runtrack.model;

import android.content.Context;
import android.graphics.Color;

import uk.org.nickmoore.runtrack.R;

/**
 * An enum representing the Factions found in Android: Netrunner.
 */
@SuppressWarnings("WeakerAccess")
public enum Faction implements Stringable {
    ANARCH(R.string.anarch, R.string.anarch_short, Role.RUNNER, Color.argb(255, 225, 93, 49)),
    CRIMINAL(R.string.criminal, R.string.criminal_short, Role.RUNNER, Color.argb(255, 74, 81, 131)),
    SHAPER(R.string.shaper, R.string.shaper_short, Role.RUNNER, Color.argb(255, 106, 147, 65)),
    HAAS_BIOROID(R.string.haas_bioroid, R.string.haas_bioroid_short, Role.CORPORATION, Color.argb(255, 100, 74, 105)),
    JINTEKI(R.string.jinteki, R.string.jinteki_short, Role.CORPORATION, Color.argb(255, 202, 80, 54)),
    NBN(R.string.nbn, R.string.nbn_short, Role.CORPORATION, Color.argb(255, 238, 171, 45)),
    WEYLAND(R.string.weyland, R.string.weyland_short, Role.CORPORATION, Color.argb(255, 105, 114, 99));

    public final int textId;
    public final int shortTextId;
    public final Role role;
    public final int color;

    private Faction(int textId, int shortTextId, Role role, int color) {
        this.textId = textId;
        this.shortTextId = shortTextId;
        this.role = role;
        this.color = color;
    }

    public Role getRole() {
        return this.role;
    }

    @Override
    public CharSequence toCharSequence(Context context, boolean shortVersion) {
        if (shortVersion) {
            return context.getText(shortTextId);
        }
        return context.getText(textId);
    }

    public static Faction[] getFaction(Role role) {
        switch (role) {
            case CORPORATION:
                return new Faction[]{HAAS_BIOROID, JINTEKI, NBN, WEYLAND};
            case RUNNER:
                return new Faction[]{ANARCH, CRIMINAL, SHAPER};
        }
        return Faction.values();
    }
}
