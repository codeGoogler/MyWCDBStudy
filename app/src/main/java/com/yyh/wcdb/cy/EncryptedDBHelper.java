package com.yyh.wcdb.cy;

/**
 * Created by Administrator on 2017/6/25 0025.
 */

import android.content.Context;
import android.util.Log;

import com.tencent.wcdb.DatabaseUtils;
import com.tencent.wcdb.database.SQLiteDatabase;
import com.tencent.wcdb.database.SQLiteOpenHelper;
import com.tencent.wcdb.repair.RepairKit;

import java.io.File;


/**
 * 类功能描述：</br>
 * 新数据处理帮助类
 * @author 于亚豪
 *  博客地址： http://blog.csdn.net/androidstarjack
 * 公众号： 终端研发部
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class EncryptedDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "EncryptedDBHelper";

    private static final String DATABASE_NAME = "encrypted.db";
    private static final String OLD_DATABASE_NAME = "plain-text.db";
    private static final int DATABASE_VERSION = 2;

    private Context mContext;
    private String mPassphrase;

    public EncryptedDBHelper(Context context, String passphrase) {

        // 调用“加密”版本的超类构造函数。
        super(context, DATABASE_NAME, passphrase.getBytes(), null, null, DATABASE_VERSION,
                null);
        // 保存上下文对象供以后使用。
        mContext = context;
        mPassphrase = passphrase;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 检查数据库plain-text.db是否存在 ，存在 如果是这样，将其导出到新的加密库中的。
        File oldDbFile = mContext.getDatabasePath(OLD_DATABASE_NAME);
        if (oldDbFile.exists()) {

            Log.i(TAG, "Migrating plain-text database to encrypted one.");

            //SQLiteOpenHelper在调用onCreate（）之前开始一个事务。 我们必须结束事务才能附加一个新的数据库。
            db.endTransaction();

            // 将旧数据库附加到新创建的加密数据库。
            String sql = String.format("ATTACH DATABASE %s AS old KEY '';",
                    DatabaseUtils.sqlEscapeString(oldDbFile.getPath()));
            db.execSQL(sql);

            // 导出旧数据库。
            db.beginTransaction();
            DatabaseUtils.stringForQuery(db, "SELECT sqlcipher_export('main', 'old');", null);
            db.setTransactionSuccessful();
            db.endTransaction();

            // 获取旧的数据库版本供以后升级。
            int oldVersion = (int) DatabaseUtils.longForQuery(db, "PRAGMA old.user_version;", null);

            // 分离旧数据库并输入新的事务。
            db.execSQL("DETACH DATABASE old;");

            // 旧数据库现在可以删除。
            oldDbFile.delete();

            // 在进一步的操作之前，还原事务。
            db.beginTransaction();

            // 检查我们是否需要升级架构。
            if (oldVersion > DATABASE_VERSION) {
                onDowngrade(db, oldVersion, DATABASE_VERSION);
            } else if (oldVersion < DATABASE_VERSION) {
                onUpgrade(db, oldVersion, DATABASE_VERSION);
            }
        } else {
            Log.i(TAG, "Creating new encrypted database.");

            // 如果旧数据库不存在，请进行真正的初始化。
            db.execSQL("CREATE TABLE message (content TEXT, "
                    + "sender TEXT);");
        }

        // 损坏恢复的备份主信息。
        RepairKit.MasterInfo.save(db, db.getPath() + "-mbak", mPassphrase.getBytes());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.i(TAG, String.format("Upgrading database from version %d to version %d.",
                oldVersion, newVersion));

        //将新列添加到数据库升级的消息表中。
        db.execSQL("ALTER TABLE message ADD COLUMN sender TEXT;");

        //损坏恢复的备份主信息
        RepairKit.MasterInfo.save(db, db.getPath() + "-mbak", mPassphrase.getBytes());
    }
}