package permissions.dispatcher.processor.exception;

import permissions.dispatcher.processor.util.ExtensionsUtils;

import javax.lang.model.element.ExecutableElement;

public final class WrongReturnTypeException
        extends RuntimeException {
    public WrongReturnTypeException(ExecutableElement e) {
        super("Method '" + ExtensionsUtils.getSimpleString(e) + "()' must specify return type 'void', not '" + e.getReturnType() + "'");
    }
}
