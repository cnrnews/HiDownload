package com.imooc.download;

import android.content.Context;

import com.imooc.download.db.DaoManagerHelper;


/**
 * 下载文件门面模式：初始化以及文件下载
 */
public class DownloadFacade {
    private static final DownloadFacade mInstance = new DownloadFacade();

    private DownloadFacade() {
    }

    public static DownloadFacade getInstance() {
        return mInstance;
    }

    /**
     * 初始化
     * @param context
     */
    public void init(Context context){
        // 初始化文件下载目录
        FileManager.manager().init(context);
        // 初始化缓存数据库
        DaoManagerHelper.getInstance().init(context);
    }

    /**
     * 开启端点下载，需要回调
     * @param url
     * @param callback
     */
    public void startDownload(String url,DownloadCallback callback){
        DownloadDispatcher.getInstance().startDownload(url,callback);
    }
    /**
     * 开启端点下载，不需要回调
     * @param url
     */
    public void startDownload(String url){
//        DownloadDispatcher.getInstance().startDownload(url);
    }
}
