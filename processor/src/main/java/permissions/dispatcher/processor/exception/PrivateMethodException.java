package permissions.dispatcher.processor.exception;

import permissions.dispatcher.processor.util.ExtensionsUtils;

import javax.lang.model.element.ExecutableElement;


public final class PrivateMethodException
        extends RuntimeException {
    public PrivateMethodException(ExecutableElement e, Class<?> annotationType) {
        super("Method '" + ExtensionsUtils.getSimpleString(e) + "()' annotated with '@" + annotationType.getSimpleName() + "' must not be private");
    }
}
