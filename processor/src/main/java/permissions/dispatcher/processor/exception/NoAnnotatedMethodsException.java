package permissions.dispatcher.processor.exception;

import permissions.dispatcher.processor.RuntimePermissionsElement;

public final class NoAnnotatedMethodsException
        extends RuntimeException {
    public NoAnnotatedMethodsException(RuntimePermissionsElement rpe, Class<?> type) {
        super("Annotated class '" + rpe.getInputClassName() + "' doesn't have any method annotated with '@" + type.getSimpleName() + "'");
    }
}
