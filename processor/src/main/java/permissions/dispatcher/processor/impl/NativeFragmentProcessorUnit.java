package permissions.dispatcher.processor.impl;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import permissions.dispatcher.processor.RuntimePermissionsElement;
import permissions.dispatcher.processor.exception.SupportV13MissingException;
import permissions.dispatcher.processor.util.Constants;
import permissions.dispatcher.processor.util.HelperUtils;

import javax.lang.model.type.TypeMirror;

public final class NativeFragmentProcessorUnit extends BaseProcessorUnit {
    private final ClassName PERMISSION_UTILS_V13;

    public NativeFragmentProcessorUnit() {
        PERMISSION_UTILS_V13 = ClassName.get(Constants.PACKAGE_NAME + ".v13", "PermissionUtilsV13");
    }

    public TypeMirror getTargetType() {
        return HelperUtils.typeMirrorOf("android.app.Fragment");
    }

    public void checkPrerequisites(RuntimePermissionsElement rpe) {
        try {
            // Check if FragmentCompat can be accessed; if not, throw an exception
            Class.forName("android.support.permission.dispatcher.v13.app.FragmentCompat");
        } catch (ClassNotFoundException ex) {
            // Thrown if support-permission.dispatcher.v13 is missing on the classpath
            throw new SupportV13MissingException(rpe);
        } catch (NoClassDefFoundError ex) {
            // Expected in success cases, because the Android environment is still missing
            // when this is called from within the Annotation processor. 'FragmentCompat' itself
            // can be resolved, but accessing it requires an Android environment, which doesn't exist
            // since this is an annotation processor
        }
    }

    public String getActivityName(String targetParam) {
        return targetParam + ".getActivity()";
    }

    public void addShouldShowRequestPermissionRationaleCondition(MethodSpec.Builder builder, String targetParam, String permissionField, boolean isPositiveCondition) {
        builder.beginControlFlow("if ($N$T.getInstance().shouldShowRequestPermissionRationale($N, $N))",
                isPositiveCondition ? "" : "!", this.PERMISSION_UTILS_V13, targetParam, permissionField);
    }

    public void addRequestPermissionsStatement(MethodSpec.Builder builder, String targetParam, String permissionField, String requestCodeField) {
        builder.addStatement("$T.getInstance().requestPermissions($N, $N, $N)", this.PERMISSION_UTILS_V13, targetParam, permissionField, requestCodeField);
    }
}
