package com.yyh.wcdb.cy;

import android.content.Context;

import java.lang.reflect.Field;

/**
 * 实现代码的模拟入侵
 */

public class MyAmsUtils {

    private  Class<?> proxyActivity;
    private Context context;
    private Object AActivityThredValues;

    public MyAmsUtils(Class<?> proxyActivity,Context context){
        this.proxyActivity = proxyActivity;
        this.context = context;
    }

    /*private void hookAms(){
        try {
            Class<?> myClass =   Class.forName("android.app.ActivityManagerNAtive");
            Field gDefault = myClass.getDeclaredField("gDefault");
            gDefault.set
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
