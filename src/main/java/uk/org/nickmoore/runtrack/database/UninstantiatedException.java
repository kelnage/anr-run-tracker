package uk.org.nickmoore.runtrack.database;

/**
 * An exception thrown by Instantiable objects when they require further instantiation to proceed.
 */
public class UninstantiatedException extends Exception {
    private final String method;
    private final Instantiable instance;

    public UninstantiatedException(String method, Instantiable instance) {
        this.method = method;
        this.instance = instance;
    }

    public String getMethod() {
        return method;
    }

    public Instantiable getInstance() {
        return instance;
    }

    @Override
    public String getMessage() {
        return String.format(
                "Method %s is only defined for instantiated objects - %s is uninstantiated",
                method, instance.toString());
    }
}
