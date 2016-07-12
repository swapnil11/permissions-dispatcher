package permissions.dispatcher.processor.exception;

import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;

public final class WrongClassException
        extends RuntimeException {
    public WrongClassException(TypeMirror type) {
        super("Class '" + TypeName.get(type).toString() + "' can't be annotated with '@RuntimePermissions'");
    }
}
