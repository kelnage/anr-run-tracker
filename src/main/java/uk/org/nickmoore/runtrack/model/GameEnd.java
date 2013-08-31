package uk.org.nickmoore.runtrack.model;

import android.content.Context;

import uk.org.nickmoore.runtrack.R;

/**
 * An enum representing the possible game end states in Android: Netrunner.
 */
@SuppressWarnings("WeakerAccess")
public enum GameEnd implements Stringable {
    AGENDAS(R.string.agendas), FLATLINE(R.string.flatline), DECKOUT(R.string.deckout),
    TIMEOUT(R.string.timeout);

    public final int textId;

    private GameEnd(int textId) {
        this.textId = textId;
    }

    @Override
    public CharSequence toCharSequence(Context context, boolean shortVersion) {
        return context.getText(textId);
    }
}
