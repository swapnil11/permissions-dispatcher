package permissions.dispatcher.processor.exception;

import permissions.dispatcher.processor.RuntimePermissionsElement;

public final class SupportV13MissingException
        extends RuntimeException {
    public SupportV13MissingException(RuntimePermissionsElement e) {
        super("PermissionsDispatcher for annotated class '" + e.getInputClassName() + "' cannot be generated, because the support-permission.dispatcher.v13 dependency is missing on your project");
    }
}
