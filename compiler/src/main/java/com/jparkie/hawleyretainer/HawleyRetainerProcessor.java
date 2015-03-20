package com.jparkie.hawleyretainer;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public final class HawleyRetainerProcessor extends AbstractProcessor {
    public static final String TAG = HawleyRetainerProcessor.class.getSimpleName();

    private Elements mElementUtils;
    private Filer mFiler;
    private Messager mMessager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
        mMessager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Map<TypeElement, HawleyRetainerDentist> targetClassMap = new LinkedHashMap<>();
        final Set<String> targetClassNameSet = new LinkedHashSet<>();

        final Set<? extends Element> retainElements = roundEnv.getElementsAnnotatedWith(HawleyRetain.class);
        for (Element element : retainElements) {
            try {
                if (element.getKind() != ElementKind.FIELD) {
                    mMessager.printMessage(Diagnostic.Kind.ERROR, TAG + ": HawleyRetain annotations can only be applied to fields.", element);

                    return false;
                }

                final TypeElement classElement = (TypeElement)element.getEnclosingElement();

                final HawleyRetainerDentist hawleyRetainerDentist = findOrCreateHawleyRetainerDentist(targetClassMap, targetClassNameSet, classElement);

                hawleyRetainerDentist.addFieldBinding(element);
            } catch (Exception e) {
                mMessager.printMessage(Diagnostic.Kind.ERROR, String.format(TAG + ": %s", e.getMessage()), element);
            }
        }

        for (Map.Entry<TypeElement, HawleyRetainerDentist> entry : targetClassMap.entrySet()) {
            final HawleyRetainerDentist correspondingDentist = entry.getValue();
            correspondingDentist.setClassParent(findHawleyRetainerDentistParent(entry.getKey(), targetClassNameSet));
        }

        for (HawleyRetainerDentist hawleyRetainerDentist : targetClassMap.values()) {
            try {
                hawleyRetainerDentist.writeToFiler(mFiler);
            } catch (IOException e) {
                mMessager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        }

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(HawleyRetain.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private HawleyRetainerDentist findOrCreateHawleyRetainerDentist(Map<TypeElement, HawleyRetainerDentist> targetClassMap, Set<String> targetClassNameSet, TypeElement classElement) {
        HawleyRetainerDentist hawleyRetainerDentist = targetClassMap.get(classElement);
        if (hawleyRetainerDentist == null) {
            final String classPackage = mElementUtils.getPackageOf(classElement).getQualifiedName().toString();
            final String className = classElement.getQualifiedName().toString().substring(classPackage.length() + 1).replace(".", "$") + HawleyRetainer.RETAINER_SUFFIX;
            final String classTarget = classElement.getQualifiedName().toString();

            hawleyRetainerDentist = new HawleyRetainerDentist(classPackage, className, classTarget);

            targetClassMap.put(classElement, hawleyRetainerDentist);
            targetClassNameSet.add(classElement.toString());
        }

        return hawleyRetainerDentist;
    }

    private String findHawleyRetainerDentistParent(TypeElement classElement, Set<String> possibleClassParentNameSet) {
        TypeMirror typeMirror = null;

        TypeElement currentElement = classElement;

        do {
            typeMirror = currentElement.getSuperclass();
            if (typeMirror.getKind() == TypeKind.NONE) {
                break;
            }

            currentElement = (TypeElement)((DeclaredType)typeMirror).asElement();
            if (possibleClassParentNameSet.contains(currentElement.toString())) {
                final String classPackage = mElementUtils.getPackageOf(currentElement).getQualifiedName().toString();
                final String className = currentElement.getQualifiedName().toString().substring(classPackage.length() + 1).replace(".", "$") + HawleyRetainer.RETAINER_SUFFIX;

                return classPackage + "." + className;
            }

        } while (typeMirror != null && typeMirror.getKind() != TypeKind.NONE);

        return null;
    }
}
