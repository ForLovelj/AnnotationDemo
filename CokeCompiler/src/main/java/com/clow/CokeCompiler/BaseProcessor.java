package com.clow.CokeCompiler;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by dth
 * Des:
 * Date: 2019/10/16.
 */
public abstract class BaseProcessor extends AbstractProcessor {

    // 用来生成java源文件工具类
    protected Filer    mFiler;
    // 用来处理注解器的异常信息
    protected Messager mMessager;
    // 用来操作类型数据
    protected Types    mTypes;
    // 用来处理被注解的Element，获取Element信息
    protected Elements mElements;



    @Override
    public final synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        mMessager = processingEnv.getMessager();
        mTypes = processingEnv.getTypeUtils();
        mElements = processingEnv.getElementUtils();
        init();
    }

    protected abstract void init();

//    @Override
//    public SourceVersion getSupportedSourceVersion() {
//        return SourceVersion.latestSupported();
//    }
}
