package permissions.dispatcher.processor.impl;


import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.processor.ProcessorUnit;
import permissions.dispatcher.processor.RequestCodeProvider;
import permissions.dispatcher.processor.RuntimePermissionsElement;
import permissions.dispatcher.processor.impl.helper.SensitivePermissionInterface;
import permissions.dispatcher.processor.impl.helper.SystemAlertWindowHelper;
import permissions.dispatcher.processor.impl.helper.WriteSettingsHelper;
import permissions.dispatcher.processor.util.Constants;
import permissions.dispatcher.processor.util.ExtensionsUtils;
import permissions.dispatcher.processor.util.HelperUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

/**
 * Base class for ProcessorUnit implementations.
 * <p>
 * This generates the parts of code independent from specific permission method signatures for different target objects.
 */
public abstract class BaseProcessorUnit implements ProcessorUnit {

    private final ClassName PERMISSION_UTILS = ClassName.get(Constants.PACKAGE_NAME, "PermissionUtils");
    private final String MANIFEST_WRITE_SETTING = "android.permission.WRITE_SETTINGS";
    private final String MANIFEST_SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW";
    private final HashMap<String, SensitivePermissionInterface> ADD_WITH_CHECK_BODY_MAP =
            new HashMap<String, SensitivePermissionInterface>() {{
                put(MANIFEST_SYSTEM_ALERT_WINDOW, new SystemAlertWindowHelper());
                put(MANIFEST_WRITE_SETTING, new WriteSettingsHelper());
            }};

    /**
     * Creates the JavaFile for the provided @RuntimePermissions element.
     * <p>
     * This will delegate to other methods that compose generated code.
     */
    public final JavaFile createJavaFile(RuntimePermissionsElement rpe,
                                         RequestCodeProvider requestCodeProvider) {
        checkPrerequisites(rpe);
        return JavaFile.builder(rpe.getPackageName(), createTypeSpec(rpe, requestCodeProvider))
                .addFileComment(Constants.FILE_COMMENT)
                .build();
    }

    /* Begin abstract */
    protected abstract void checkPrerequisites(RuntimePermissionsElement paramRuntimePermissionsElement);

    protected abstract void addRequestPermissionsStatement(MethodSpec.Builder paramBuilder, String targetParam, String permissionField, String requestCodeField);

    protected abstract void addShouldShowRequestPermissionRationaleCondition(MethodSpec.Builder paramBuilder, String targetParam, String permissionField, boolean isPositiveCondition);


    protected abstract String getActivityName(String paramString);

    /* Begin private */
    private TypeSpec createTypeSpec(RuntimePermissionsElement rpe,
                                    RequestCodeProvider requestCodeProvider) {
        return TypeSpec.classBuilder(rpe.getGeneratedClassName())
                .addModifiers(Modifier.FINAL)
                .addFields(createFields(rpe.getNeedsElements(), requestCodeProvider))
                .addMethod(createConstructor())
                .addMethods(createWithCheckMethods(rpe))
                .addMethods(createPermissionHandlingMethods(rpe))
                .addTypes(createPermissionRequestClasses(rpe))
                .build();
    }

    private List<FieldSpec> createFields(List<? extends ExecutableElement> needsElements,
                                         RequestCodeProvider requestCodeProvider) {
        ArrayList<FieldSpec> fields = new ArrayList<>();
        // For each method annotated with @NeedsPermission, add REQUEST integer and PERMISSION String[] fields
        for (ExecutableElement element : needsElements) {
            fields.add(createRequestCodeField(element, requestCodeProvider.nextRequestCode()));
            fields.add(createPermissionField(element));

            if (!element.getParameters().isEmpty()) {
                fields.add(createPendingRequestField(element));
            }
        }
        return fields;
    }

    private FieldSpec createRequestCodeField(ExecutableElement e, int index) {
        return FieldSpec.builder(int.class, HelperUtils.requestCodeFieldName(e))
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$L", index)
                .build();
    }

    private FieldSpec createPermissionField(ExecutableElement e) {
        List<String> permissionValue = ExtensionsUtils.getPermissionValue(e.getAnnotation(NeedsPermission.class));
        for (int i = 0; i < permissionValue.size(); i++) {
            permissionValue.set(i, "\"" + permissionValue.get(i) + "\"");
        }
        String formattedValue = "{" + String.join(",", (String[]) permissionValue.toArray()) + "}";
        return FieldSpec.builder(ArrayTypeName.of(String.class), HelperUtils.permissionFieldName(e))
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$N", "new String[] " + formattedValue)
                .build();
    }

    private FieldSpec createPendingRequestField(ExecutableElement e) {
        return FieldSpec.builder(ClassName.get(Constants.PACKAGE_NAME, "GrantableRequest"), HelperUtils.pendingRequestFieldName(e))
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .build();
    }

    private MethodSpec createConstructor() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private List<MethodSpec> createWithCheckMethods(RuntimePermissionsElement rpe) {
        ArrayList<MethodSpec> methods = new ArrayList<>();
        for (ExecutableElement executableElement : rpe.getNeedsElements()) {
            // For each @NeedsPermission method, create the "WithCheck" equivalent
            methods.add(createWithCheckMethod(rpe, executableElement));
        }
        return methods;
    }

    private MethodSpec createWithCheckMethod(RuntimePermissionsElement rpe, ExecutableElement method) {
        String targetParam = "target";
        MethodSpec.Builder builder = MethodSpec.methodBuilder(HelperUtils.withCheckMethodName(method))
                .addTypeVariables((Iterable) rpe.getTypeVariables())
                .addModifiers(Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(rpe.getTypeName(), targetParam);
        // If the method has parameters, add those as well
        for (VariableElement element : method.getParameters()) {
            builder.addParameter(HelperUtils.typeNameOf(element), ExtensionsUtils.getSimpleString(element));
        }
        // Delegate method body generation to implementing classes
        addWithCheckBody(builder, method, rpe, targetParam);
        return builder.build();
    }

    private void addWithCheckBody(MethodSpec.Builder builder, ExecutableElement needsMethod, RuntimePermissionsElement rpe, String targetParam) {
        String requestCodeField = HelperUtils.requestCodeFieldName(needsMethod);
        String permissionField = HelperUtils.permissionFieldName(needsMethod);

        // Add the conditional for when permission has already been granted
        String needsPermissionParameter = needsMethod.getAnnotation(NeedsPermission.class).value()[0];
        String activityVar = getActivityName(targetParam);
        SensitivePermissionInterface sensitivePermissionInterface = ADD_WITH_CHECK_BODY_MAP.get(needsPermissionParameter);
        if (sensitivePermissionInterface != null) {
            sensitivePermissionInterface.addHasSelfPermissionsCondition(builder, activityVar, permissionField);
        } else {
            builder.beginControlFlow("if ($T.hasSelfPermissions($N, $N))", PERMISSION_UTILS, activityVar, permissionField);
        }
        builder.addCode(CodeBlock.builder()
                .add("$N.$N(", targetParam, ExtensionsUtils.getSimpleString(needsMethod))
                .add(HelperUtils.varargsParametersCodeBlock(needsMethod))
                .addStatement(")")
                .build()
        );
        builder.nextControlFlow("else");

        // Add the conditional for "OnShowRationale", if present
        ExecutableElement onRationale = rpe.findOnRationaleForNeeds(needsMethod);
        Boolean hasParameters = !needsMethod.getParameters().isEmpty();
        if (hasParameters) {
            // If the method has parameters, precede the potential OnRationale call with
            // an instantiation of the temporary Request object
            CodeBlock.Builder varargsCall = CodeBlock.builder()
                    .add("$N = new $N($N, ",
                            HelperUtils.pendingRequestFieldName(needsMethod),
                            upperCaseFirst(HelperUtils.permissionRequestTypeName(needsMethod)),
                            targetParam
                    )
                    .add(HelperUtils.varargsParametersCodeBlock(needsMethod))
                    .addStatement(")");
            builder.addCode(varargsCall.build());
        }
        if (onRationale != null) {
            addShouldShowRequestPermissionRationaleCondition(builder, targetParam, permissionField, true);
            if (hasParameters) {
                // For methods with parameters, use the PermissionRequest instantiated above
                builder.addStatement("$N.$N($N)", targetParam, ExtensionsUtils.getSimpleString(onRationale), HelperUtils.pendingRequestFieldName(needsMethod));
            } else {
                // Otherwise, create a new PermissionRequest on-the-fly
                builder.addStatement("$N.$N(new $N($N))", targetParam, ExtensionsUtils.getSimpleString(onRationale), upperCaseFirst(HelperUtils.permissionRequestTypeName(needsMethod)), targetParam);
            }
            builder.nextControlFlow("else");
        }

        // Add the branch for "request permission"
        SensitivePermissionInterface requestPermissionInterfaceObj = ADD_WITH_CHECK_BODY_MAP.get(needsPermissionParameter);
        if (requestPermissionInterfaceObj != null) {
            requestPermissionInterfaceObj.addRequestPermissionsStatement(builder, activityVar, requestCodeField);
        } else {
            addRequestPermissionsStatement(builder, targetParam, permissionField, requestCodeField);
        }

        if (onRationale != null) {
            builder.endControlFlow();
        }
        builder.endControlFlow();
    }

    private List<MethodSpec> createPermissionHandlingMethods(RuntimePermissionsElement rpe) {
        ArrayList<MethodSpec> methods = new ArrayList<>();
        if (hasNormalPermission(rpe)) {
            methods.add(createPermissionResultMethod(rpe));
        }
        if ((hasSystemAlertWindowPermission(rpe)) || (hasWriteSettingPermission(rpe))) {
            methods.add(createOnActivityResultMethod(rpe));
        }
        return methods;
    }

    private MethodSpec createOnActivityResultMethod(RuntimePermissionsElement rpe) {
        String targetParam = "target";
        String requestCodeParam = "requestCode";
        String grantResultsParam = "grantResults";
        MethodSpec.Builder builder = MethodSpec.methodBuilder("onActivityResult")
                .addTypeVariables((Iterable) rpe.getTypeVariables())
                .addModifiers(Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(rpe.getTypeName(), targetParam)
                .addParameter(TypeName.INT, requestCodeParam);

        builder.beginControlFlow("switch ($N)", requestCodeParam);
        for (ExecutableElement needsMethod : rpe.getNeedsElements()) {
            String needsPermissionParameter = (needsMethod.getAnnotation(NeedsPermission.class)).value()[0];
            if (this.ADD_WITH_CHECK_BODY_MAP.containsKey(needsPermissionParameter)) {
                continue;
            }
            builder.addCode("case $N:\n", HelperUtils.requestCodeFieldName(needsMethod));
            addResultCaseBody(builder, needsMethod, rpe, targetParam, grantResultsParam);
        }
        builder
                .addCode("default:\n")
                .addStatement("break")
                .endControlFlow();
        return builder.build();
    }

    private MethodSpec createPermissionResultMethod(RuntimePermissionsElement rpe) {
        String targetParam = "target";
        String requestCodeParam = "requestCode";
        String grantResultsParam = "grantResults";
        MethodSpec.Builder builder = MethodSpec.methodBuilder("onRequestPermissionsResult")
                .addTypeVariables((Iterable) rpe.getTypeVariables())
                .addModifiers(Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(rpe.getTypeName(), targetParam)
                .addParameter(TypeName.INT, requestCodeParam)
                .addParameter(ArrayTypeName.of(TypeName.INT), grantResultsParam);
        // For each @NeedsPermission method, add a switch case
        builder.beginControlFlow("switch ($N)", requestCodeParam);
        for (ExecutableElement needsMethod : rpe.getNeedsElements()) {
            String needsPermissionParameter = (needsMethod.getAnnotation(NeedsPermission.class)).value()[0];
            if (this.ADD_WITH_CHECK_BODY_MAP.containsKey(needsPermissionParameter)) {
                continue;
            }
            builder.addCode("case $N:\n", HelperUtils.requestCodeFieldName(needsMethod));

            // Delegate switch-case generation to implementing classes
            addResultCaseBody(builder, needsMethod, rpe, targetParam, grantResultsParam);
        }
        // Add the default case
        builder
                .addCode("default:\n")
                .addStatement("break")
                .endControlFlow();

        return builder.build();
    }

    private void addResultCaseBody(MethodSpec.Builder builder, ExecutableElement needsMethod, RuntimePermissionsElement rpe, String targetParam, String grantResultsParam) {
        // just workaround, see https://github.com/hotchemi/PermissionsDispatcher/issues/45
        ExecutableElement onDenied = rpe.findOnDeniedForNeeds(needsMethod);
        Boolean hasDenied = onDenied != null;
        String needsPermissionParameter = needsMethod.getAnnotation(NeedsPermission.class).value()[0];
        String permissionField = HelperUtils.permissionFieldName(needsMethod);
        if (!ADD_WITH_CHECK_BODY_MAP.containsKey(needsPermissionParameter)) {
            builder.beginControlFlow("if ($T.getTargetSdkVersion($N) < 23 && !$T.hasSelfPermissions($N, $N))",
                    PERMISSION_UTILS, getActivityName(targetParam), PERMISSION_UTILS, getActivityName(targetParam), permissionField);
            if (hasDenied) {
                builder.addStatement("$N.$N()", targetParam, ExtensionsUtils.getSimpleString(onDenied));
            }
            builder.addStatement("return");
            builder.endControlFlow();
        }

        // Add the conditional for "permission verified"
        SensitivePermissionInterface permissionInterface = ADD_WITH_CHECK_BODY_MAP.get(needsPermissionParameter);
        if (permissionInterface != null) {
            permissionInterface.addHasSelfPermissionsCondition(builder, getActivityName(targetParam), permissionField);
        } else {
            builder.beginControlFlow("if ($T.verifyPermissions($N))", PERMISSION_UTILS, grantResultsParam);
        }
        // Based on whether or not the method has parameters, delegate to the "pending request" object or invoke the method directly
        Boolean hasParameters = !needsMethod.getParameters().isEmpty();
        if (hasParameters) {
            String pendingField = HelperUtils.pendingRequestFieldName(needsMethod);
            builder.beginControlFlow("if ($N != null)", pendingField);
            builder.addStatement("$N.grant()", pendingField);
            builder.endControlFlow();
        } else {
            builder.addStatement("target.$N()", ExtensionsUtils.getSimpleString(needsMethod));
        }

        // Add the conditional for "permission denied" and/or "never ask again", if present
        ExecutableElement onNeverAsk = rpe.findOnNeverAskForNeeds(needsMethod);
        Boolean hasNeverAsk = onNeverAsk != null;

        if (hasDenied || hasNeverAsk) {
            builder.nextControlFlow("else");
        }
        if (hasNeverAsk) {
            // Split up the "else" case with another if condition checking for "never ask again" first
            addShouldShowRequestPermissionRationaleCondition(builder, targetParam, HelperUtils.permissionFieldName(needsMethod), false);
            builder.addStatement("target.$N()", ExtensionsUtils.getSimpleString(onNeverAsk));

            // If a "permission denied" is present as well, go into an else case, otherwise close this temporary branch
            if (hasDenied) {
                builder.nextControlFlow("else");
            } else {
                builder.endControlFlow();
            }
        }
        if (hasDenied) {
            // Add the "permissionDenied" statement
            builder.addStatement("$N.$N()", targetParam, ExtensionsUtils.getSimpleString(onDenied));

            // Close the additional control flow potentially opened by a "never ask again" method
            if (hasNeverAsk) {
                builder.endControlFlow();
            }
        }
        // Close the "switch" control flow
        builder.endControlFlow();

        // Remove the temporary pending request field, in case it was used for a method with parameters
        if (hasParameters) {
            builder.addStatement("$N = null", HelperUtils.pendingRequestFieldName(needsMethod));
        }
        builder.addStatement("break");
    }

    private boolean hasNormalPermission(RuntimePermissionsElement rpe) {
        for (ExecutableElement element : rpe.getNeedsElements()) {
            List<String> permissionValue = ExtensionsUtils.getPermissionValue(element.getAnnotation(NeedsPermission.class));
            if (!permissionValue.contains(MANIFEST_SYSTEM_ALERT_WINDOW) && !permissionValue.contains(MANIFEST_WRITE_SETTING)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSystemAlertWindowPermission(RuntimePermissionsElement rpe) {
        return isDefinePermission(rpe, this.MANIFEST_SYSTEM_ALERT_WINDOW);
    }

    private boolean hasWriteSettingPermission(RuntimePermissionsElement rpe) {
        return isDefinePermission(rpe, this.MANIFEST_WRITE_SETTING);
    }

    private boolean isDefinePermission(RuntimePermissionsElement rpe, String permissionName) {
        for (ExecutableElement element : rpe.getNeedsElements()) {
            List<String> permissionValue = ExtensionsUtils.getPermissionValue(element.getAnnotation(NeedsPermission.class));
            if (permissionValue.contains(permissionName)) {
                return true;
            }
        }
        return false;
    }

    private List<TypeSpec> createPermissionRequestClasses(RuntimePermissionsElement rpe) {
        ArrayList<TypeSpec> classes = new ArrayList<>();
        for (ExecutableElement element : rpe.getNeedsElements()) {
            ExecutableElement onRationale = rpe.findOnRationaleForNeeds(element);
            if (onRationale != null || !element.getParameters().isEmpty()) {
                classes.add(createPermissionRequestClass(rpe, element));
            }
        }
        return classes;
    }

    private String upperCaseFirst(String value) {
        // Convert String to char array.
        if (value != null) {
            char[] array = value.toCharArray();
            // Modify first element in array.
            array[0] = Character.toUpperCase(array[0]);
            // Return string.
            return new String(array);
        }
        return "";
    }

    private TypeSpec createPermissionRequestClass(RuntimePermissionsElement rpe, ExecutableElement needsMethod) {
        // Select the superinterface of the generated class
        // based on whether or not the annotated method has parameters
        Boolean hasParameters = !needsMethod.getParameters().isEmpty();
        String superInterfaceName = (hasParameters) ? "GrantableRequest" : "PermissionRequest";

        TypeName targetType = rpe.getTypeName();
        TypeSpec.Builder builder = TypeSpec.classBuilder(upperCaseFirst(HelperUtils.permissionRequestTypeName(needsMethod)))
                .addTypeVariables(rpe.getTypeVariables())
                .addSuperinterface(ClassName.get(Constants.PACKAGE_NAME, superInterfaceName))
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

        // Add required fields to the target
        String weakFieldName = "weakTarget";
        ParameterizedTypeName weakFieldType = ParameterizedTypeName.get(ClassName.get("java.lang.ref", "WeakReference"), targetType);
        builder.addField(weakFieldType, weakFieldName, Modifier.PRIVATE, Modifier.FINAL);
        for (VariableElement element : needsMethod.getParameters()) {
            builder.addField(HelperUtils.typeNameOf(element), ExtensionsUtils.getSimpleString(element), Modifier.PRIVATE, Modifier.FINAL);
        }

        // Add constructor
        String targetParam = "target";
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(targetType, targetParam)
                .addStatement("this.$L = new WeakReference<>($N)", weakFieldName, targetParam);
        for (VariableElement element : needsMethod.getParameters()) {
            String fieldName = ExtensionsUtils.getSimpleString(element);
            constructorBuilder
                    .addParameter(HelperUtils.typeNameOf(element), fieldName)
                    .addStatement("this.$L = $N", fieldName, fieldName);
        }
        builder.addMethod(constructorBuilder.build());

        // Add proceed() override
        MethodSpec.Builder proceedMethod = MethodSpec.methodBuilder("proceed")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addStatement("$T target = $N.get()", targetType, weakFieldName)
                .addStatement("if (target == null) return");
        String requestCodeField = HelperUtils.requestCodeFieldName(needsMethod);
        SensitivePermissionInterface permissionInterface = ADD_WITH_CHECK_BODY_MAP.get(needsMethod.getAnnotation(NeedsPermission.class).value()[0]);
        if (permissionInterface != null) {
            permissionInterface.addRequestPermissionsStatement(proceedMethod, getActivityName(targetParam), requestCodeField);
        } else {
            addRequestPermissionsStatement(proceedMethod, targetParam, HelperUtils.permissionFieldName(needsMethod), requestCodeField);
        }
        builder.addMethod(proceedMethod.build());

        // Add cancel() override method
        MethodSpec.Builder cancelMethod = MethodSpec.methodBuilder("cancel")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID);
        ExecutableElement onDenied = rpe.findOnDeniedForNeeds(needsMethod);
        if (onDenied != null) {
            cancelMethod
                    .addStatement("$T target = $N.get()", targetType, weakFieldName)
                    .addStatement("if (target == null) return")
                    .addStatement("target.$N()", ExtensionsUtils.getSimpleString(onDenied));
        }
        builder.addMethod(cancelMethod.build());

        // For classes with additional parameters, add a "grant()" method
        if (hasParameters) {
            MethodSpec.Builder grantMethod = MethodSpec.methodBuilder("grant")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.VOID);
            grantMethod
                    .addStatement("$T target = $N.get()", targetType, weakFieldName)
                    .addStatement("if (target == null) return");

            // Compose the call to the permission-protected method;
            // since the number of parameters is variable, utilize the low-level CodeBlock here
            // to compose the method call and its parameters
            grantMethod.addCode(
                    CodeBlock.builder()
                            .add("target.$N(", ExtensionsUtils.getSimpleString(needsMethod))
                            .add(HelperUtils.varargsParametersCodeBlock(needsMethod))
                            .addStatement(")")
                            .build()
            );
            builder.addMethod(grantMethod.build());
        }

        return builder.build();
    }

    protected ClassName getPERMISSION_UTILS() {
        return this.PERMISSION_UTILS;
    }
}
