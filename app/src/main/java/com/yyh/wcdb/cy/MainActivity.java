package com.yyh.wcdb.cy;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.tencent.wcdb.Cursor;
import android.os.AsyncTask;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;

import com.tencent.wcdb.database.SQLiteDatabase;
import com.tencent.wcdb.database.SQLiteOpenHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
/**
 * 类功能描述：</br>
 * 主类测试
 * @author 于亚豪
 * 博客地址： http://blog.csdn.net/androidstarjack
 * 公众号： 终端研发部
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WCDB.EncryptDBSample";

    private SQLiteDatabase mDB;
    private SQLiteOpenHelper mDBHelper;
    private int mDBVersion;

    private ListView mListView;
    private Adapter mAdapter;
    private List<Student> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mListView = (ListView) findViewById(R.id.list);
        list = new ArrayList<>();
        mAdapter = new Adapter(list, MainActivity.this);
         mListView.setAdapter(mAdapter);
        mListView.setAdapter(mAdapter);
    }
    @OnClick({R.id.btn_init_plain,R.id.btn_init_encrypted,R.id.btn_insert,R.id.btn_delete})
    public void onClick(View view){
        switch (view.getId()) {
            // 在版本1中创建或打开数据库，然后刷新适配器。
            case R.id.btn_init_plain:
                new AsyncTask<Void, Void, Cursor>() {
                    @Override
                    protected void onPreExecute() {
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    protected Cursor doInBackground(Void... params) {
                        if (mDBHelper != null && mDB != null && mDB.isOpen()) {
                            mDBHelper.close();
                            mDBHelper = null;
                            mDB = null;
                        }

                        mDBHelper = new PlainTextDBHelper(MainActivity.this);
                        mDBHelper.setWriteAheadLoggingEnabled(true);
                        mDB = mDBHelper.getWritableDatabase();
                        mDBVersion = mDB.getVersion();
                        return mDB.rawQuery("SELECT rowid as _id, content, '???' as sender FROM message;",
                                null);
                    }

                    @Override
                    protected void onPostExecute(Cursor cursor) {
                        list = getAllStudent(cursor);
                        mAdapter.changeCursor(list);
                    }
                }.execute();
                break;
            case R.id.btn_init_encrypted:
                //在版本2中创建或打开数据库，然后刷新适配器。
                //如果纯文本数据库存在并加密，则不传送全部数据
                //从纯文本数据库（在版本1中）的数据，然后升级它 到版本2。
                // 有关数据传输和模式升级的详细信息，请参阅EncryptedDBHelper.java。
                new AsyncTask<Void, Void, Cursor>() {
                    @Override
                    protected void onPreExecute() {
                        mAdapter.changeCursor(list);
                    }

                    @Override
                    protected Cursor doInBackground(Void... params) {
                        if (mDBHelper != null && mDB != null && mDB.isOpen()) {
                            mDBHelper.close();
                            mDBHelper = null;
                            mDB = null;
                        }

                        String passphrase = "passphrase";
                        mDBHelper = new EncryptedDBHelper(MainActivity.this, passphrase);
                        mDBHelper.setWriteAheadLoggingEnabled(true);
                        mDB = mDBHelper.getWritableDatabase();
                        mDBVersion = mDB.getVersion();
                        return mDB.rawQuery("SELECT rowid as _id, content, sender FROM message;",
                                null);
                    }

                    @Override
                    protected void onPostExecute(Cursor cursor) {
                        list = getAllStudent(cursor);
                        mAdapter.changeCursor(list);
                    }
                }.execute();
                break;
            //将消息插入数据库。
            //要测试数据传输，初始化纯文本数据库，插入消息，
            //然后init加密数据库。
            case R.id.btn_insert:
                final DateFormat DATE_FORMAT = SimpleDateFormat.getDateTimeInstance();
                new AsyncTask<Void, Void, Cursor>() {
                    @Override
                    protected void onPreExecute() {
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    protected Cursor doInBackground(Void... params) {
                        if (mDB == null || !mDB.isOpen())
                            return null;

                        String message = "Message inserted on " + DATE_FORMAT.format(new Date());

                        if (mDBVersion == 1) {
                            mDB.execSQL("INSERT INTO message VALUES (?);",
                                    new Object[]{"yyh"});
                            return mDB.rawQuery("SELECT rowid as _id, content, '???' as sender FROM message;",
                                    null);
                        } else {
                            mDB.execSQL("INSERT INTO message VALUES (?, ?);",
                                    new Object[]{"yyh", "男"});
                            return mDB.rawQuery("SELECT rowid as _id, content, sender FROM message;",
                                    null);
                        }
                    }

                    @Override
                    protected void onPostExecute(Cursor cursor) {
                        if (cursor == null)
                            return;
                        list = getAllStudent(cursor);
                        mAdapter.changeCursor(list);
                    }
                }.execute();
                break;
            case R.id.btn_delete:
                if (mDB == null || !mDB.isOpen()){
                    return  ;
                 }
                mDB.execSQL("DELETE FROM message WHERE content"+"=?",
                        new Object[]{"yyh"});
               com.tencent.wcdb.Cursor cursor =  mDB.rawQuery("SELECT rowid as _id, content, sender FROM message;",
                        null);
               list = getAllStudent(cursor);
                mAdapter.changeCursor(list);
                break;
        }
    }
    public List<Student> getAllStudent(com.tencent.wcdb.Cursor cursor) {
        List<Student> pointList = new ArrayList<Student>();
        Student student;
        while (cursor.moveToNext()) {
            student = new Student();
            student.setName(cursor.getString(cursor
                    .getColumnIndex("content")));
            student.setSex(cursor.getString(cursor
                    .getColumnIndex("sender")));
            pointList.add(student);
        }
        return pointList;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

}