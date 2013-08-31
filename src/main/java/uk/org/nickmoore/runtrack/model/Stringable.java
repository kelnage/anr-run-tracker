package uk.org.nickmoore.runtrack.model;

import android.content.Context;

/**
 * An interface for objects that can be displayed in the user interface - often using resource IDs
 * (hence the Context) and they may provide a shortVersion for small screens (<300px wide).
 */
public interface Stringable {
    public CharSequence toCharSequence(Context context, boolean shortVersion);
}
