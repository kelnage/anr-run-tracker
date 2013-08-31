package uk.org.nickmoore.runtrack.database;

import java.io.Serializable;

import uk.org.nickmoore.runtrack.model.Stringable;

/**
 * An abstract class for objects that can be instantiated by the database.
 */
@SuppressWarnings("WeakerAccess")
public abstract class Instantiable implements Stringable, Comparable<Instantiable>,
        Serializable {
    private boolean instantiated = false;
    @AutoIncrement
    public long _id;

    public long getId() {
        return _id;
    }

    public void setId(long id) {
        this._id = id;
    }

    public boolean isInstantiated() {
        return instantiated;
    }

    public void instantiate() {
        instantiated = true;
    }

    public void throwIfNotInstantiated(String method) throws UninstantiatedException {
        if (!isInstantiated()) {
            throw new UninstantiatedException(method, this);
        }
    }

    @Override
    public int compareTo(Instantiable instantiable) {
        return Long.valueOf(_id).compareTo(instantiable.getId());
    }
}
