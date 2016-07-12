package permissions.dispatcher.processor.impl;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import permissions.dispatcher.processor.RuntimePermissionsElement;
import permissions.dispatcher.processor.util.HelperUtils;

import javax.lang.model.type.TypeMirror;

/**
 * ProcessorUnit implementation for Activity classes.
 */
public final class ActivityProcessorUnit extends BaseProcessorUnit {
    private final ClassName ACTIVITY_COMPAT =
            ClassName.get("android.support.v4.app", "ActivityCompat");


    public TypeMirror getTargetType() {
        return HelperUtils.typeMirrorOf("android.app.Activity");
    }

    public void checkPrerequisites(RuntimePermissionsElement rpe) {
        // Nothing to check
    }

    public String getActivityName(String targetParam) {
        return targetParam;
    }

    public void addShouldShowRequestPermissionRationaleCondition(MethodSpec.Builder builder, String targetParam, String permissionField, boolean isPositiveCondition) {
        builder.beginControlFlow("if ($N$T.shouldShowRequestPermissionRationale($N, $N))", isPositiveCondition ? "" : "!",
                getPERMISSION_UTILS(), targetParam, permissionField);
    }

    public void addRequestPermissionsStatement(MethodSpec.Builder builder, String targetParam, String permissionField, String requestCodeField) {
        builder.addStatement("$T.requestPermissions($N, $N, $N)", this.ACTIVITY_COMPAT, targetParam, permissionField, requestCodeField);
    }
}
