package permissions.dispatcher.processor.impl.helper;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

public final class SystemAlertWindowHelper implements SensitivePermissionInterface {
    private final ClassName PERMISSION_UTILS = ClassName.get("permissions.dispatcher", "PermissionUtils");
    private final ClassName SETTINGS = ClassName.get("android.provider", "Settings");
    private final ClassName INTENT = ClassName.get("android.content", "Intent");
    private final ClassName URI = ClassName.get("android.net", "Uri");

    public void addHasSelfPermissionsCondition(MethodSpec.Builder builder, String activityVar, String permissionField) {
        builder.beginControlFlow("if ($T.hasSelfPermissions($N, $N) || $T.canDrawOverlays($N))",
                PERMISSION_UTILS, activityVar, permissionField, SETTINGS, activityVar);
    }

    public void addRequestPermissionsStatement(MethodSpec.Builder builder, String activityVar, String requestCodeField) {
        builder.addStatement("$T intent = new $T($T.ACTION_MANAGE_OVERLAY_PERMISSION, $T.parse(\"package:\" + $N.getPackageName()))",
                this.INTENT, this.INTENT, this.SETTINGS, this.URI, activityVar);
        builder.addStatement("$N.startActivityForResult(intent, $N)", activityVar, requestCodeField);
    }
}
