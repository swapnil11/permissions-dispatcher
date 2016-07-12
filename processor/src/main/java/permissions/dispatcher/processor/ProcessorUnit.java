package permissions.dispatcher.processor;

import com.squareup.javapoet.JavaFile;

import javax.lang.model.type.TypeMirror;

public interface ProcessorUnit {
    TypeMirror getTargetType();

    JavaFile createJavaFile(RuntimePermissionsElement paramRuntimePermissionsElement,
                            RequestCodeProvider paramRequestCodeProvider);
}
