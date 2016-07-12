package permissions.dispatcher.processor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helper class providing app-level unique request codes
 * for a round trip of the annotation processor.
 */
public final class RequestCodeProvider {

    public final AtomicInteger getCurrentCode() {
        return this.currentCode;
    }


    private final AtomicInteger currentCode = new AtomicInteger(0);

    /**
     * Obtains the next unique request code.
     * This method atomically increments the value
     * returned upon the next invocation.
     */
    public final int nextRequestCode() {
        return this.currentCode.getAndIncrement();
    }
}
