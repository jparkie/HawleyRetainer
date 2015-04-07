package com.jparkie.hawleyretainer;

import android.app.Activity;

import com.jparkie.hawleyretainer.internal.Retainer;
import com.jparkie.hawleyretainer.internal.RetainerFragmentMap;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

/*
 * Example:
 *
 * package com.jparkie.example;
 *
 * import android.app.Activity;
 * import com.jparkie.hawleyretainer.internal.Retainer;
 * import com.jparkie.hawleyretainer.internal.RetainerFragmentMap;
 * import java.io.File;
 * import java.lang.Override;
 * import rx.Observable;
 *
 * public class ExampleActivity$$HawleyRetainer<T extends com.jparkie.example.ExampleActivity> extends Retainer.Object<T> {
 *  @Override
 *  public void restoreRetainedObjectMap(T target, Activity activity) {
 *      final RetainerFragmentMap retainedMap = RetainerFragmentMap.<T>findOrCreateRetainerFragmentMap(target, activity);
 *      if (target.mObservable == null) {
 *          if (retainedMap.containsKey("mObservable")) {
 *              target.mObservable = (Observable<File>)retainedMap.get("mObservable");
 *          }
 *      }
 *  }
 *
 *  @Override
 *  public void saveRetainedObjectMap(T target, Activity activity) {
 *      final RetainerFragmentMap retainedMap = RetainerFragmentMap.<T>findOrCreateRetainerFragmentMap(target, activity);
 *      if (target.mObservable != null) {
 *          retainedMap.put("mObservable", target.mObservable);
 *      }
 *  }
 * }
 */
public final class HawleyRetainerDentist {
    public static final String TAG = HawleyRetainerDentist.class.getSimpleName();

    private final String mClassPackage;
    private final String mClassName;
    private final String mClassTarget;

    private String mClassParent;

    private final List<FieldBinding> mFieldBindings;

    public HawleyRetainerDentist(String classPackage, String className, String classTarget) {
        mClassPackage = classPackage;
        mClassName = className;
        mClassTarget = classTarget;

        mFieldBindings = new LinkedList<>();
    }

    public void setClassParent(String classParent) {
        mClassParent = classParent;
    }

    public void addFieldBinding(Element element) {
        final String name = element.getSimpleName().toString();
        final TypeMirror type = element.asType();

        final FieldBinding fieldBinding = new FieldBinding(name, type);

        mFieldBindings.add(fieldBinding);
    }

    public void writeToFiler(Filer filer) throws IOException {
        final ClassName targetClassName = ClassName.get(mClassPackage, mClassTarget);

        final TypeSpec.Builder builder = TypeSpec.classBuilder(mClassName)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(TypeVariableName.get("T", targetClassName))
                .addMethod(generateRestoreRetainedObjectMapMethod())
                .addMethod(generateSaveRetainedObjectMapMethod());

        if (mClassParent != null) {
            builder.superclass(ParameterizedTypeName.get(ClassName.bestGuess(mClassParent), TypeVariableName.get("T")));
        } else {
            builder.superclass(ParameterizedTypeName.get(ClassName.get(Retainer.Object.class), TypeVariableName.get("T")));
        }

        final JavaFile retainerFile = JavaFile.builder(mClassPackage, builder.build()).build();

        retainerFile.writeTo(filer);
    }

    private MethodSpec generateRestoreRetainedObjectMapMethod() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("restoreRetainedObjectMap")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(TypeVariableName.get("T"), "target")
                .addParameter(Activity.class, "activity");

        if (mClassParent != null) {
            builder.addStatement("super.restoreRetainedObjectMap(target, activity)");
        }

        builder.addStatement("final $T retainedMap = RetainerFragmentMap.<$T>findOrCreateRetainerFragmentMap(target, activity)", ClassName.get(RetainerFragmentMap.class), TypeVariableName.get("T"));

        for (FieldBinding fieldBinding : mFieldBindings) {
            builder.beginControlFlow("if (retainedMap.containsKey($S))", fieldBinding.mName)
                    .addStatement("target.$N = ($T)retainedMap.get($S)", fieldBinding.mName, fieldBinding.mType, fieldBinding.mName)
                    .endControlFlow();
        }

        return builder.build();
    }

    private MethodSpec generateSaveRetainedObjectMapMethod() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("saveRetainedObjectMap")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(TypeVariableName.get("T"), "target")
                .addParameter(Activity.class, "activity");

        if (mClassParent != null) {
            builder.addStatement("super.saveRetainedObjectMap(target, activity)");
        }

        builder.addStatement("final $T retainedMap = RetainerFragmentMap.<$T>findOrCreateRetainerFragmentMap(target, activity)", ClassName.get(RetainerFragmentMap.class), TypeVariableName.get("T"));

        for (FieldBinding fieldBinding : mFieldBindings) {
            builder.beginControlFlow("if (target.$N != null)", fieldBinding.mName)
                    .addStatement("retainedMap.put($S, target.$N)", fieldBinding.mName, fieldBinding.mName)
                    .endControlFlow();
        }

        return builder.build();
    }

    public static final class FieldBinding {
        private final String mName;
        private final TypeMirror mType;

        public FieldBinding(String name, TypeMirror type) {
            mName = name;
            mType = type;
        }
    }
}
