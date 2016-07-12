package permissions.dispatcher.processor;


import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.processor.util.Constants;
import permissions.dispatcher.processor.util.ExtensionsUtils;
import permissions.dispatcher.processor.util.HelperUtils;
import permissions.dispatcher.processor.util.Validators;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;


public final class RuntimePermissionsElement {
    private final TypeName typeName;

    private final List<TypeVariableName> typeVariables;

    private final String packageName;

    private final String inputClassName;

    private final String generatedClassName;

    private final List<ExecutableElement> needsElements;

    private final List<ExecutableElement> onRationaleElements;

    private final List<ExecutableElement> onDeniedElements;

    private final List<ExecutableElement> onNeverAskElements;

    public RuntimePermissionsElement(TypeElement e) {
        this.typeName = TypeName.get(e.asType());
        this.typeVariables = new ArrayList<>();
        for (TypeParameterElement parameterElement : e.getTypeParameters()) {
            this.typeVariables.add(TypeVariableName.get(parameterElement));
        }
        this.packageName = ExtensionsUtils.getPackageName(e);
        this.inputClassName = ExtensionsUtils.getSimpleString(e);
        this.generatedClassName = inputClassName + Constants.GEN_CLASS_SUFFIX;
        this.needsElements = ExtensionsUtils.getChildElementsAnnotatedWith(e, NeedsPermission.class);
        this.onRationaleElements = ExtensionsUtils.getChildElementsAnnotatedWith(e, OnShowRationale.class);
        this.onDeniedElements = ExtensionsUtils.getChildElementsAnnotatedWith(e, OnPermissionDenied.class);
        this.onNeverAskElements = ExtensionsUtils.getChildElementsAnnotatedWith(e, OnNeverAskAgain.class);
        validateNeedsMethods();
        validateRationaleMethods();
        validateDeniedMethods();
        validateNeverAskMethods();
    }

    public TypeName getTypeName() {
        return this.typeName;
    }


    public List<TypeVariableName> getTypeVariables() {
        return this.typeVariables;
    }


    public String getPackageName() {
        return this.packageName;
    }


    public String getInputClassName() {
        return this.inputClassName;
    }


    public String getGeneratedClassName() {
        return this.generatedClassName;
    }


    public List<ExecutableElement> getNeedsElements() {
        return this.needsElements;
    }


    public List<ExecutableElement> getOnRationaleElements() {
        return this.onRationaleElements;
    }


    public List<ExecutableElement> getOnDeniedElements() {
        return this.onDeniedElements;
    }


    public List<ExecutableElement> getOnNeverAskElements() {
        return this.onNeverAskElements;
    }

    private void validateNeedsMethods() {
        Validators.checkNotEmpty(this.needsElements, this, NeedsPermission.class);
        Validators.checkPrivateMethods(this.needsElements, NeedsPermission.class);
        Validators.checkMethodSignature(this.needsElements);
        Validators.checkMixPermissionType(this.needsElements, NeedsPermission.class);
    }

    private void validateRationaleMethods() {
        Validators.checkDuplicatedValue(this.onRationaleElements, OnShowRationale.class);
        Validators.checkPrivateMethods(this.onRationaleElements, OnShowRationale.class);
        Validators.checkMethodSignature(this.onRationaleElements);
        Validators.checkMethodParameters(this.onRationaleElements, 1,
                HelperUtils.typeMirrorOf(Constants.PACKAGE_NAME + "." + Constants.GEN_PERMISSIONREQUEST_SUFFIX));
    }

    private void validateDeniedMethods() {
        Validators.checkDuplicatedValue(this.onDeniedElements, OnPermissionDenied.class);
        Validators.checkPrivateMethods(this.onDeniedElements, OnPermissionDenied.class);
        Validators.checkMethodSignature(this.onDeniedElements);
        Validators.checkMethodParameters(this.onDeniedElements, 0);
    }

    private void validateNeverAskMethods() {
        Validators.checkDuplicatedValue(this.onNeverAskElements, OnNeverAskAgain.class);
        Validators.checkPrivateMethods(this.onNeverAskElements, OnNeverAskAgain.class);
        Validators.checkMethodSignature(this.onNeverAskElements);
        Validators.checkMethodParameters(this.onNeverAskElements, 0);
    }

    public ExecutableElement findOnRationaleForNeeds(ExecutableElement needsElement) {
        return HelperUtils.findMatchingMethodForNeeds(needsElement, this.onRationaleElements, OnShowRationale.class);
    }

    public ExecutableElement findOnDeniedForNeeds(ExecutableElement needsElement) {
        return HelperUtils.findMatchingMethodForNeeds(needsElement, this.onDeniedElements, OnPermissionDenied.class);
    }

    public ExecutableElement findOnNeverAskForNeeds(ExecutableElement needsElement) {
        return HelperUtils.findMatchingMethodForNeeds(needsElement, this.onNeverAskElements, OnNeverAskAgain.class);
    }


}
