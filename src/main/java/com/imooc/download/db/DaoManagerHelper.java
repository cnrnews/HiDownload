package com.imooc.download.db;

import android.content.Context;

import com.imooc.download.DownloadEntity;

import java.util.List;

/**
 * 文件缓存处理类
 */
public class DaoManagerHelper {

    private static DaoManagerHelper managerHelper = new DaoManagerHelper();

    private DaoManagerHelper() {
    }

    public static DaoManagerHelper getInstance(){
        return managerHelper;
    }

    public void addEntity(DownloadEntity downloadEntity) {
        DaoSupportFactory.getFactory().getDao(DownloadEntity.class)
                .insert(downloadEntity);
    }

    public List<DownloadEntity> queryAll(String mUrl) {
        return DaoSupportFactory.getFactory().getDao(DownloadEntity.class)
                .querySupport()
                .selection("url=?")
                .selectionArgs(mUrl)
                .query();
    }

    public void init(Context context) {
    }
}
