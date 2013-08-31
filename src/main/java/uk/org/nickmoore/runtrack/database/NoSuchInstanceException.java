package uk.org.nickmoore.runtrack.database;

/**
 * An exception to be thrown by code that extracts Instantiable objects from the database.
 */
public class NoSuchInstanceException extends Exception {
    private final String id;
    private final Class clazz;

    public NoSuchInstanceException(String id, Class clazz) {
        this.id = id;
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        return String.format("No such instance of %s with ID %s", clazz.getName(), id);
    }
}
