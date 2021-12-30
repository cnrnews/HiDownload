package com.imooc.download;


import com.imooc.download.db.DaoManagerHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.Response;

/**
 * 下载线程
 */
public class DownloadRunnable implements Runnable {

    // 正在下载
    private static final int STATUS_DOWNLOADING = 1;
    // 停止下载
    private static final int STATUS_STOP = 2;
    private final String url;
    private final  int threadId;
    private final long start;
    private final  long end;
    private final DownloadCallback mCallback;

    private int mStatus = STATUS_DOWNLOADING;
    // 下载进度
    private long mProgress = 0;
    private DownloadEntity mDownloadEntity;


    public DownloadRunnable(String url, int threadId, long start, long end,
                            long progress,DownloadEntity entity,DownloadCallback callback) {
        this.url = url;
        this.threadId = threadId;
        this.start = start;
        this.end = end;
        this.mCallback = callback;
        this.mProgress = progress;
        this.mDownloadEntity = entity;
    }

    @Override
    public void run() {

        InputStream inputStream=null;
        RandomAccessFile accessFile=null;
        try {
            // 只读取自己的内容
            Response response = OkHttpManager.getManager().asyncResponse(url, start, end);

            inputStream= response.body().byteStream();
            // 写数据
            File file = FileManager.manager().getFile(url);
            accessFile  = new RandomAccessFile(file, "rwd");
            // 从哪里开始
            accessFile.seek(start);

            int len = 0;
            byte[] buffer = new byte[1024*10];
            while ((len = inputStream.read(buffer))!=-1){
                if (mStatus == STATUS_STOP){
                    break;
                }
                // 保存进度
                mProgress+=len;
                accessFile.write(buffer,0,len);
            }
            // 回调文件下载完成
            mCallback.onSucceed(file);
        } catch (IOException e) {
            e.printStackTrace();
            mCallback.onFailure(e);
        }finally {
            Utils.close(inputStream);
            Utils.close(accessFile);

            // 下载进度缓存
            mDownloadEntity.setProgress(mProgress);
            DaoManagerHelper.getInstance().addEntity(mDownloadEntity);
        }
    }

    /**
     * 停止线程
     */
    public void stop() {
        mStatus = STATUS_STOP;
    }
}
