package com.clow.CokeCompiler;

import com.clow.CokeAnnotation.AnnoConstants;
import com.clow.CokeAnnotation.anno.BindView;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created by dth
 * Des:
 * Date: 2019/10/16.
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({AnnoConstants.BINDVIEW_NAME})
public class ViewProcessor extends BaseProcessor{

    private TypeName injectTypeName;
    private ClassName injectClassName;
    private TypeMirror activityTypeMirror;
    private TypeMirror fragmentTypeMirror;
    private TypeMirror viewMirror;

    @Override
    protected void init() {
        TypeElement injectTypeElement = mElements.getTypeElement(AnnoConstants.INJECT_CLASS_NAME);
        injectTypeName = TypeName.get(injectTypeElement.asType());
        injectClassName = ClassName.get(injectTypeElement);
        //镜像类型
        activityTypeMirror = mElements.getTypeElement(AnnoConstants.ANDROID_ACTIVITY).asType();
        fragmentTypeMirror = mElements.getTypeElement(AnnoConstants.ANDROID_V4_FRAGMENT).asType();
        viewMirror = mElements.getTypeElement(AnnoConstants.ANDROID_VIEW).asType();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        if (CollectionUtils.isNotEmpty(set)) {
            final Set<VariableElement> fieldElements = new HashSet<>();
            for (Element element : elements) {
                // 判断Element的类型是否是变量类型Element
                if((element instanceof VariableElement)) {
                    // 强转
                    VariableElement variableElement = (VariableElement)element;
                    fieldElements.add(variableElement);
                }
            }
            findTargetElement(fieldElements);
            createImpl();
        }
        return false;
    }

    /**
     * 这里面存的是每一个类针对的所有标记的字段
     */
    private Map<TypeElement, Set<VariableElement>> map = new HashMap<>();

    /**
     * 寻找每个类相同标记的注解，分开储存
     * @param fieldElements
     */
    private void findTargetElement(final Set<VariableElement> fieldElements) {
        for (VariableElement variableElement : fieldElements) {
            // 获取封装Element，即变量所属的类的Element
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            Set<VariableElement> variableElements = map.get(typeElement);
            if (variableElements == null) {
                variableElements = new HashSet<>();
                map.put(typeElement, variableElements);
            }
            variableElements.add(variableElement);
        }
    }

    private void createImpl() {
        Set<Map.Entry<TypeElement, Set<VariableElement>>> entrySet = map.entrySet();
        for (Map.Entry<TypeElement, Set<VariableElement>> entry : entrySet) {
            // 标记注解的类
            TypeElement targetClass = entry.getKey();
            // 这个类标记了注解的字段集合
            Set<VariableElement> parameterFieldSet = entry.getValue();
            createInjectClass(targetClass, parameterFieldSet);
        }
    }

    /**
     * 创建某个类的Inject实现
     *
     * @param targetClass   标记注解的类
     * @param parameterFieldSet 这个类标记了注解的字段集合
     */
    private void createInjectClass(TypeElement targetClass, Set<VariableElement> parameterFieldSet) {
        // 拿到注解类的全类名
        String fullClassName = targetClass.getQualifiedName().toString();
        int lastPointIndex = fullClassName.lastIndexOf('.');
        //pkg
        String pkg = fullClassName.substring(0, lastPointIndex);
        //simpleName
        String className = fullClassName.substring(lastPointIndex + 1);
        //生成方法
        MethodSpec methodSpec = injectMethod(targetClass, parameterFieldSet);
        TypeSpec.Builder classBuilder = TypeSpec
                //生成类名
                .classBuilder(className + AnnoConstants.INJECT_SUFFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                //添加继承接口和泛型
                .addSuperinterface(ParameterizedTypeName.get(injectClassName, TypeName.get(mElements.getTypeElement(fullClassName).asType())))
                .addMethod(methodSpec);
        try {
            // 与目标Class放在同一个包下，解决Class属性的可访问性
            JavaFile.builder(pkg, classBuilder.build()).build().writeTo(mFiler);
        } catch (IOException ignore) {
            // ignore
        }
    }

    /**
     * 生成方法
     * @param targetClass   标记注解的类
     * @param parameterFieldSet 这个类标记了注解的字段集合
     * @return
     */
    private MethodSpec injectMethod(TypeElement targetClass, Set<VariableElement> parameterFieldSet) {
        MethodSpec.Builder methodBuilder = MethodSpec
                .methodBuilder("inject")
                .addAnnotation(Override.class)
                .addParameter(ParameterSpec
                                .builder(TypeName.get(targetClass.asType()), "target")
                                .build())
                .addParameter(ParameterSpec
                        .builder(TypeName.get(viewMirror), "view")
                        .build())
                .addModifiers(Modifier.PUBLIC);
        boolean isActivity = false;
        if (mTypes.isSubtype(targetClass.asType(), activityTypeMirror)) {
            isActivity = true;
        } else if (mTypes.isSubtype(targetClass.asType(), fragmentTypeMirror)) {

        } else {

        }
        for (VariableElement variableElement : parameterFieldSet) {
            generateParameter(variableElement,methodBuilder);
        }
        return methodBuilder.build();
    }

    private void generateParameter(VariableElement variableElement, MethodSpec.Builder methodBuilder) {
        // 变量名称(比如：TextView tv 的 tv)
        String variableName = variableElement.getSimpleName().toString();
        // 变量类型的完整类路径（比如：android.widget.TextView）
        String variableFullName = variableElement.asType().toString();
        BindView bindView = variableElement.getAnnotation(BindView.class);
        //添加代码语句
        methodBuilder.addStatement("target.$L=($L)view.findViewById($L)", variableName, variableFullName, bindView.value());
    }
}
