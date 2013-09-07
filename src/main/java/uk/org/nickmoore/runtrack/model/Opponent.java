package uk.org.nickmoore.runtrack.model;

import android.content.Context;

import java.io.Serializable;

import uk.org.nickmoore.runtrack.database.IndexedField;
import uk.org.nickmoore.runtrack.database.Instantiable;

/**
 * A class representing opponent.
 */
public class Opponent extends Instantiable implements Serializable {
    @IndexedField
    public String name;

    public Opponent() {
        setId(0);
    }

    public Opponent(long id) {
        setId(id);
    }

    public Opponent(long id, String name) {
        setId(id);
        this.name = name;
        instantiate();
    }

    @Override
    public String toString() {
        if (isInstantiated()) {
            return String.format("ID: %d, Name: %s", getId(), name);
        }
        return String.format("ID: %d", getId());
    }

    @Override
    public CharSequence toCharSequence(Context context, boolean shortVersion) {
        if (isInstantiated()) {
            return name;
        }
        return String.format("%d", getId());
    }
}
