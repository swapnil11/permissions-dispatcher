package permissions.dispatcher.processor.util;


import permissions.dispatcher.processor.PermissionsProcessor;
import permissions.dispatcher.processor.ProcessorUnit;
import permissions.dispatcher.processor.RuntimePermissionsElement;
import permissions.dispatcher.processor.exception.DuplicatedValueException;
import permissions.dispatcher.processor.exception.MixPermissionTypeException;
import permissions.dispatcher.processor.exception.NoAnnotatedMethodsException;
import permissions.dispatcher.processor.exception.NoParametersAllowedException;
import permissions.dispatcher.processor.exception.PrivateMethodException;
import permissions.dispatcher.processor.exception.WrongClassException;
import permissions.dispatcher.processor.exception.WrongParametersException;
import permissions.dispatcher.processor.exception.WrongReturnTypeException;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public final class Validators {
    private static final String WRITE_SETTINGS = "android.permission.WRITE_SETTINGS";
    private static final String SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW";

    /**
     * Obtains the ProcessorUnit implementation for the provided element.
     * Raises an exception if no suitable implementation exists
     */
    public static ProcessorUnit findAndValidateProcessorUnit(List<? extends ProcessorUnit> units, Element e) {
        TypeMirror typeMirror = e.asType();
        try {
            for (ProcessorUnit processorUnit : units) {
                if (ExtensionsUtils.isSubtypeOf(typeMirror, processorUnit.getTargetType())) {
                    return processorUnit;
                }
            }
        } catch (NoSuchElementException ex) {
            throw new WrongClassException(typeMirror);
        }
        return null;
    }

    /**
     * Checks the elements in the provided list annotated with an annotation against duplicate values.
     * <p>
     * Raises an exception if any annotation value is found multiple times.
     */
    public static <A extends Annotation> void checkDuplicatedValue
    (List<? extends ExecutableElement> items, Class<A> annotationClass) {
        ArrayList<List<String>> allItems = new ArrayList<>();
        for (ExecutableElement executableElement : items) {
            List<String> permissionValue =
                    ExtensionsUtils.getPermissionValue(executableElement
                            .getAnnotation(annotationClass));
            Collections.sort(permissionValue);
            for (List<String> item : allItems) {
                if (item.equals(permissionValue)) {
                    throw new DuplicatedValueException(permissionValue, executableElement,
                            annotationClass);
                }
            }
            allItems.add(permissionValue);
        }
    }

    /**
     * Checks the elements in the provided list for elements.
     * <p>
     * Raises an exception if it doesn't contain any elements.
     */
    public static <A extends Annotation> void checkNotEmpty(List<? extends ExecutableElement> items, RuntimePermissionsElement rpe, Class<A> annotationClass) {
        if (items.isEmpty()) {
            throw new NoAnnotatedMethodsException(rpe, annotationClass);
        }
    }

    /**
     * Checks the elements in the provided list annotated with an annotation
     * against private modifiers.
     * <p>
     * Raises an exception if any element contains the "private" modifier.
     */
    public static <A extends Annotation> void checkPrivateMethods(List<? extends ExecutableElement> items, Class<A> annotationClass) {
        for (ExecutableElement element : items) {
            if (element.getModifiers().contains(Modifier.PRIVATE)) {
                throw new PrivateMethodException(element, annotationClass);
            }
        }
    }

    /**
     * Checks the return type of the elements in the provided list.
     * <p>
     * Raises an exception if any element specifies a return type other than 'void'.
     */
    public static void checkMethodSignature(List<? extends ExecutableElement> items) {
        for (ExecutableElement element : items) {
            if (element.getReturnType().getKind() != TypeKind.VOID) {
                throw new WrongReturnTypeException(element);
            }
        }
    }

    public static void checkMethodParameters(List<? extends ExecutableElement> items,
                                                   int numParams, TypeMirror... requiredTypes) {
        // Check each element's parameters against the requirements
        for (ExecutableElement element : items) {
            List<? extends VariableElement> params = element.getParameters();
            if (numParams == 0 && !params.isEmpty()) {
                throw new NoParametersAllowedException(element);
            }
            if (numParams != params.size()) {
                throw new WrongParametersException(element, (String[])
                        ExtensionsUtils.getStringArray(requiredTypes));
            }
            for (int i = 0; i < params.size(); i++) {
                VariableElement param = params.get(i);
                TypeMirror requiredType = requiredTypes[i];
                if (!PermissionsProcessor.getTypeUtils().isSameType(param.asType(), requiredType)) {
                    throw new WrongParametersException
                            (element, (String[]) ExtensionsUtils.getStringArray(requiredTypes));
                }
            }
        }
    }

    public static <A extends Annotation> void checkMixPermissionType
            (List<? extends ExecutableElement> items, Class<A> annotationClass) {
        for (ExecutableElement element : items) {
            List<String> permissionValue =
                    ExtensionsUtils.getPermissionValue(element
                            .getAnnotation(annotationClass));
            if (permissionValue.size() > 1) {
                if (permissionValue.contains(WRITE_SETTINGS)) {
                    throw new MixPermissionTypeException(element, WRITE_SETTINGS);
                } else if (permissionValue.contains(SYSTEM_ALERT_WINDOW)) {
                    throw new MixPermissionTypeException(element, SYSTEM_ALERT_WINDOW);
                }
            }
        }
    }
}
