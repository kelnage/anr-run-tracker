package uk.org.nickmoore.runtrack.database;

/**
 * An exception thrown by the ClassConverter when classes are provided that cannot be handled.
 */
public class UnmanageableClassException extends Exception {
    private final Class clazz;

    public UnmanageableClassException(Class clazz) {
        this.clazz = clazz;
    }

    public Class getClazz() {
        return clazz;
    }

    @Override
    public String toString() {
        return String.format("Class %s could not be managed automatically", clazz.getName());
    }
}
