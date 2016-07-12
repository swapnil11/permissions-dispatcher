package permissions.dispatcher.processor.util;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.processor.PermissionsProcessor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class ExtensionsUtils {
    /**
     * Returns the simple name of an Element as a string.
     */
    public static String getSimpleString(Element receiver) {
        return receiver.getSimpleName().toString();
    }

    /**
     * Returns whether or not an Element is annotated with the provided Annotation class.
     */
    private static <A extends Annotation> boolean hasAnnotation(Element receiver, Class<A> annotationType) {
        return receiver.getAnnotation(annotationType) != null;
    }

    /**
     * Returns the inherent value() of a permission Annotation.
     * <p>
     * If this is invoked on an Annotation that's not defined by PermissionsDispatcher, this returns an empty list.
     */
    public static List<String> getPermissionValue(Annotation receiver) {
        if ((receiver instanceof NeedsPermission)) {
            return Arrays.asList(((NeedsPermission) receiver).value());
        }
        if ((receiver instanceof OnShowRationale)) {
            return Arrays.asList(((OnShowRationale) receiver).value());
        }
        if ((receiver instanceof OnPermissionDenied)) {
            return Arrays.asList(((OnPermissionDenied) receiver).value());
        }
        if ((receiver instanceof OnNeverAskAgain)) {
            return Arrays.asList(((OnNeverAskAgain) receiver).value());
        }
        return new ArrayList<>();
    }

    /**
     * Returns whether or not a TypeMirror is a subtype of the provided other TypeMirror.
     */
    public static boolean isSubtypeOf(TypeMirror receiver, TypeMirror ofType) {
        return PermissionsProcessor.getTypeUtils().isSubtype(receiver, ofType);
    }

    /**
     * Returns the package name of a TypeElement.
     */
    public static String getPackageName(TypeElement receiver) {
        String qualifiedName = receiver.getQualifiedName().toString();
        return qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
    }

    /**
     * Returns the simple name of a TypeMirror as a string.
     */
    private static String getSimpleString(TypeMirror receiver) {
        return receiver.toString();
    }

    /**
     * Returns a list of enclosed elements annotated with the provided Annotations.
     */
    public static <A extends Annotation> List<ExecutableElement> getChildElementsAnnotatedWith(Element receiver, Class<A> annotationClass) {
        List<ExecutableElement> executableElements = new ArrayList<>();
        for (Element element : receiver.getEnclosedElements()) {
            if (hasAnnotation(element, annotationClass)) {
                executableElements.add((ExecutableElement) element);
            }
        }
        return executableElements;
    }

    public static Object[] getStringArray(TypeMirror... requiredTypes) {
        ArrayList<String> paramsTypes = new ArrayList<>();
        for (TypeMirror type : requiredTypes) {
            paramsTypes.add(ExtensionsUtils.getSimpleString(type));
        }
        return paramsTypes.toArray();
    }
}
