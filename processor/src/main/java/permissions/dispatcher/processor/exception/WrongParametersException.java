package permissions.dispatcher.processor.exception;

import permissions.dispatcher.processor.util.ExtensionsUtils;

import javax.lang.model.element.ExecutableElement;

public final class WrongParametersException
        extends RuntimeException {
    public WrongParametersException(ExecutableElement e, String requiredTypes[]) {
        super("Method '" + ExtensionsUtils.getSimpleString(e) +
                "()' must declare parameters of type " + String.join(",", requiredTypes));
    }
}
