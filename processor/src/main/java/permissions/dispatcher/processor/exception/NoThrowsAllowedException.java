package permissions.dispatcher.processor.exception;

import permissions.dispatcher.processor.util.ExtensionsUtils;

import javax.lang.model.element.ExecutableElement;


public final class NoThrowsAllowedException
        extends RuntimeException {
    public NoThrowsAllowedException(ExecutableElement e) {
        super("Method '" + ExtensionsUtils.getSimpleString(e) + "()' must not have any 'throws' declaration in its signature");
    }
}
