package com.imooc.download.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.List;

public interface IDaoSupport<T> {

    /**
     * 创建数据库
     * @param sqLiteDatabase
     * @param clazz
     */
    void init(SQLiteDatabase sqLiteDatabase,Class<T> clazz);

    /**
     * 插入数据
     * @param t
     * @return
     */
    long insert(T t);

    /**
     * 批量插入，用于检测性能
     */
    public void insert(List<T> datas) ;

    //获取专门查询的支持类 按照语句查询
    QuerySupport<T> querySupport();

    int delete(String whereClause,String... whereArgs);
    int update(T obj,String whereClause,String... whereArgs);
}
