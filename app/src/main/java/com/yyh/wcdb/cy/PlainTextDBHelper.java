package com.yyh.wcdb.cy;

import android.content.Context;

import com.tencent.wcdb.database.SQLiteDatabase;
import com.tencent.wcdb.database.SQLiteOpenHelper;

/**
 * 类功能描述：</br>
 * 旧数据处理帮助类
 * @author 于亚豪
 *  博客地址： http://blog.csdn.net/androidstarjack
 * 公众号： 终端研发部
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class PlainTextDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "plain-text.db";
    private static final int DATABASE_VERSION = 1;

    public PlainTextDBHelper(Context context) {

        // Call "plain-text" version of the superclass constructor.
        super(context, DATABASE_NAME, null, DATABASE_VERSION, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE message (content TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Do nothing.
    }
}