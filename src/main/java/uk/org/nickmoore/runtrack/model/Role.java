package uk.org.nickmoore.runtrack.model;

import android.content.Context;
import android.graphics.Color;

import uk.org.nickmoore.runtrack.R;

/**
 * Represents one of the two roles in Android Netrunner - the Runner and the Corporation.
 */
@SuppressWarnings("WeakerAccess")
public enum Role implements Stringable {
    CORPORATION(R.string.corporation, Color.argb(255, 79, 79, 143)),
    RUNNER(R.string.runner, Color.argb(255, 198, 33, 33));

    public final int textId;
    public final int color;

    private Role(int textId, int color) {
        this.textId = textId;
        this.color = color;
    }

    @Override
    public CharSequence toCharSequence(Context context, boolean shortVersion) {
        return context.getText(textId);
    }
}