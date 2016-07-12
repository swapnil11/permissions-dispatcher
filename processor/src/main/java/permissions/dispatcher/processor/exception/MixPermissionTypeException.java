package permissions.dispatcher.processor.exception;

import permissions.dispatcher.processor.util.ExtensionsUtils;

import javax.lang.model.element.ExecutableElement;

public final class MixPermissionTypeException
        extends RuntimeException {
    public MixPermissionTypeException(ExecutableElement e, String permissionName) {
        super("Method '" + ExtensionsUtils.getSimpleString(e) + "()' defines '" + permissionName + "' with other permissions at the same time.");
    }
}
