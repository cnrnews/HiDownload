package com.imooc.download.db;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class DaoSupportFactory {

    private SQLiteDatabase mSqLiteDatabase;

    private static volatile DaoSupportFactory mFactory;

    private DaoSupportFactory() {
        File dbRoot = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                +File.separator+"nhdz"
                +File.separator+"database"
        )       ;

        // 如果目录不存在，就去创建
        if (!dbRoot.exists())
        {
            boolean isCreate = dbRoot.mkdirs();
            Log.e("TAG","isCreate>>"+isCreate);
        }
        File dbFile = new File(dbRoot,"nhdz.db");
        // 打开或创建一个数据库
        mSqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(dbFile,null);
    }

    public static DaoSupportFactory getFactory(){
        if (mFactory == null){
            synchronized (DaoSupportFactory.class){
                if (mFactory==null){
                    mFactory = new DaoSupportFactory();
                }
            }
        }
        return mFactory;
    }
    /**
     * 这里是使用 自己写的创建表的init()方法，如果以后使用第三方方便切换
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> IDaoSupport<T> getDao(Class<T> clazz){
        IDaoSupport<T> daoSupport = new DaoSupport();
        daoSupport.init(mSqLiteDatabase,clazz);
        return daoSupport;
    }
}
