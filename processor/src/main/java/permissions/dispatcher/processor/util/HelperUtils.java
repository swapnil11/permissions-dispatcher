package permissions.dispatcher.processor.util;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.processor.PermissionsProcessor;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public final class HelperUtils {

    public static TypeMirror typeMirrorOf(String className) {
        return PermissionsProcessor.getElementUtils().getTypeElement(className).asType();
    }

    public static TypeName typeNameOf(Element it) {
        return TypeName.get(it.asType());
    }

    public static String withCheckMethodName(ExecutableElement e) {
        return ExtensionsUtils.getSimpleString(e) + Constants.GEN_WITHCHECK_SUFFIX;
    }

    public static String permissionRequestTypeName(ExecutableElement e) {
        return (ExtensionsUtils.getSimpleString(e) + Constants.GEN_PERMISSIONREQUEST_SUFFIX);
    }

    public static CodeBlock varargsParametersCodeBlock(ExecutableElement needsElement) {
        CodeBlock.Builder varargsCall = CodeBlock.builder();
        List<? extends VariableElement> params = needsElement.getParameters();
        for (int i = 0; i < params.size(); i++) {
            varargsCall.add("$L", ExtensionsUtils.getSimpleString(params.get(i)));
            if (i < needsElement.getParameters().size() - 1) {
                varargsCall.add(", ");
            }
        }
        return varargsCall.build();
    }

    public static String requestCodeFieldName(ExecutableElement e) {
        return (Constants.GEN_REQUESTCODE_PREFIX + ExtensionsUtils.getSimpleString(e)).toUpperCase();
    }

    public static String permissionFieldName(ExecutableElement e) {
        return (Constants.GEN_PERMISSION_PREFIX + ExtensionsUtils.getSimpleString(e)).toUpperCase();
    }

    public static String pendingRequestFieldName(ExecutableElement e) {
        return (Constants.GEN_PENDING_PREFIX + ExtensionsUtils.getSimpleString(e)).toUpperCase();
    }

    public static <A extends Annotation> ExecutableElement findMatchingMethodForNeeds
            (ExecutableElement needsElement, List<? extends ExecutableElement> otherElements,
             Class<A> annotationType) {
        List<String> value = ExtensionsUtils.getPermissionValue(needsElement.getAnnotation(NeedsPermission.class));
        for (ExecutableElement otherElement : otherElements) {
            if (ExtensionsUtils.getPermissionValue(otherElement.getAnnotation(annotationType)).equals(value)) {
                return otherElement;
            }
        }
        return null;
    }
}
