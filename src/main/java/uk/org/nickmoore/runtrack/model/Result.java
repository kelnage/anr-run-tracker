package uk.org.nickmoore.runtrack.model;

import android.content.Context;

import uk.org.nickmoore.runtrack.R;

/**
 * Represents the result of a game or match.
 */
@SuppressWarnings("WeakerAccess")
public enum Result implements Stringable {
    WIN(R.string.win), DRAW(R.string.draw), LOSE(R.string.lose);

    public final int textId;

    Result(int textId) {
        this.textId = textId;
    }

    @Override
    public CharSequence toCharSequence(Context context, boolean shortVersion) {
        return context.getText(textId);
    }
}
