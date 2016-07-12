package permissions.dispatcher.processor.exception;

import permissions.dispatcher.processor.util.ExtensionsUtils;

import javax.lang.model.element.ExecutableElement;

public final class NoParametersAllowedException
        extends RuntimeException {
    public NoParametersAllowedException(ExecutableElement e) {
        super("Method '" + ExtensionsUtils.getSimpleString(e) + "()' must not have any parameters");
    }
}
