package permissions.dispatcher.processor.impl.helper;

import com.squareup.javapoet.MethodSpec;

public interface SensitivePermissionInterface {
    void addHasSelfPermissionsCondition(MethodSpec.Builder paramBuilder, String activityVar, String permissionField);

    void addRequestPermissionsStatement(MethodSpec.Builder paramBuilder, String activityVar, String requestCodeField);
}
