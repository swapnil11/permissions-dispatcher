package permissions.dispatcher.processor.impl;

import com.squareup.javapoet.MethodSpec;
import permissions.dispatcher.processor.RuntimePermissionsElement;
import permissions.dispatcher.processor.util.HelperUtils;

import javax.lang.model.type.TypeMirror;

/**
 * ProcessorUnit implementation for Fragments defined in the support-v4 library.
 */
public final class SupportFragmentProcessorUnit extends BaseProcessorUnit {

    public TypeMirror getTargetType() {
        return HelperUtils.typeMirrorOf("android.support.v4.app.Fragment");
    }

    public void checkPrerequisites(RuntimePermissionsElement rpe) {
        // Nothing to check
    }

    public String getActivityName(String targetParam) {
        return targetParam + ".getActivity()";
    }

    public void addShouldShowRequestPermissionRationaleCondition(MethodSpec.Builder builder, String targetParam, String permissionField, boolean isPositiveCondition) {
        builder.beginControlFlow("if ($N$T.shouldShowRequestPermissionRationale($N.getActivity(), $N))", isPositiveCondition ? "" : "!", getPERMISSION_UTILS(), targetParam, permissionField);
    }

    public void addRequestPermissionsStatement(MethodSpec.Builder builder, String targetParam, String permissionField, String requestCodeField) {
        builder.addStatement("$N.requestPermissions($N, $N)", targetParam, permissionField, requestCodeField);
    }
}
