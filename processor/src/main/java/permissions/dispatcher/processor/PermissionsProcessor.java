package permissions.dispatcher.processor;


import com.squareup.javapoet.JavaFile;
import permissions.dispatcher.RuntimePermissions;
import permissions.dispatcher.processor.impl.ActivityProcessorUnit;
import permissions.dispatcher.processor.impl.NativeFragmentProcessorUnit;
import permissions.dispatcher.processor.impl.SupportFragmentProcessorUnit;
import permissions.dispatcher.processor.util.Validators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;


public final class PermissionsProcessor extends AbstractProcessor {
    /* Processing Environment helpers */
    private Filer filer;
    private Messager messager;
    /* List of available ProcessorUnits */
    private List<ProcessorUnit> processorUnits;
    /**
     * Element Utilities, obtained from the processing environment
     */
    private static Elements ELEMENT_UTILS;
    /**
     * Type Utilities, obtained from the processing environment
     */
    private static Types TYPE_UTILS;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        // Setup helper objects
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        ELEMENT_UTILS = processingEnv.getElementUtils();
        TYPE_UTILS = processingEnv.getTypeUtils();

        // Setup the list of ProcessorUnits to handle code generation with
        processorUnits = new ArrayList<>();
        processorUnits.add(new ActivityProcessorUnit());
        processorUnits.add(new SupportFragmentProcessorUnit());
        processorUnits.add(new NativeFragmentProcessorUnit());

    }

    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    public Set<String> getSupportedAnnotationTypes() {
        List<String> className = Arrays.asList(RuntimePermissions.class.getCanonicalName());
        HashSet<String> set = new HashSet<>();
        for (String s : className) {
            set.add(s);
        }
        return set;
    }

    /**
     * Main processing method
     */
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // Create a RequestCodeProvider which guarantees unique request codes for each permission request
        RequestCodeProvider requestCodeProvider = new RequestCodeProvider();
        for (Element element : roundEnv.getElementsAnnotatedWith(RuntimePermissions.class)) {
            // Find a suitable ProcessorUnit for this element
            ProcessorUnit processorUnit = Validators.findAndValidateProcessorUnit(processorUnits, element);

            // Create a RuntimePermissionsElement for this value
            RuntimePermissionsElement rpe = new RuntimePermissionsElement((TypeElement) element);

            // Create a JavaFile for this element and write it out
            try {
                JavaFile javaFile = processorUnit.createJavaFile(rpe, requestCodeProvider);
                javaFile.writeTo(filer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public Filer getFiler() {
        return filer;
    }

    public void setFiler(Filer filer) {
        this.filer = filer;
    }

    public Messager getMessager() {
        return messager;
    }

    public void setMessager(Messager messager) {
        this.messager = messager;
    }

    public List<ProcessorUnit> getProcessorUnits() {
        return processorUnits;
    }

    public void setProcessorUnits(List<ProcessorUnit> processorUnits) {
        this.processorUnits = processorUnits;
    }

    public static Elements getElementUtils() {
        return ELEMENT_UTILS;
    }

    public static void setElementUtils(Elements elementUtils) {
        ELEMENT_UTILS = elementUtils;
    }

    public static Types getTypeUtils() {
        return TYPE_UTILS;
    }

    public static void setTypeUtils(Types typeUtils) {
        TYPE_UTILS = typeUtils;
    }
}
