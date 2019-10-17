package com.clow.cokeapi;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by dth
 * Des:
 * Date: 2019/10/17.
 */
public class Coke {

    public static void bind(Object target,Object view) {
        String name = target.getClass().getName();
        try {
            Class<?> aClass = Class.forName(name + "_ViewInject");
            if (view instanceof Activity) {
                view = ((Activity) view).getWindow().getDecorView();
            }
            Method method = aClass.getMethod("inject", target.getClass(), View.class);
            method.invoke(aClass.newInstance(),target,view);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }
}
