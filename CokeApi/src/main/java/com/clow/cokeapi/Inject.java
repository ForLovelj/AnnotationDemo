package com.clow.cokeapi;

import android.view.View;

import androidx.annotation.NonNull;

/**
 * 每一个生成的类都应该实现这个接口
 */
public interface Inject<T> {

    /**
     * 注入控件
     *
     * @param target 目标界面
     */
    void inject(@NonNull T target, View view);

}
