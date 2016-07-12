package permissions.dispatcher.processor.exception;

import permissions.dispatcher.processor.util.ExtensionsUtils;

import java.util.List;

import javax.lang.model.element.ExecutableElement;

public final class DuplicatedValueException
        extends RuntimeException {
    public DuplicatedValueException(List<String> value, ExecutableElement e, Class<?> annotation) {
        super(value + " is duplicated in '" + ExtensionsUtils.getSimpleString(e) + "()' annotated with '@" + annotation.getSimpleName() + "'");
    }
}
