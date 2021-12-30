package com.imooc.download;


import com.imooc.download.db.DaoManagerHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DownloadTask {

    private String mUrl;
    private long mContentLength;
    private List<DownloadRunnable> mRunnables;
    private DownloadCallback mCallback;

    // 计算核心线程数
    private static final int CPU_CPUNT = Runtime.getRuntime().availableProcessors();
    private static final int THREAD_SIZE = Math.max(2,Math.min(CPU_CPUNT-1,4));

    private ExecutorService executorService;
    private volatile int mSucceedNumber;

    // 线程池
    private synchronized ExecutorService executorService(){
        if (executorService == null)
        {
            executorService = new ThreadPoolExecutor(0, THREAD_SIZE,
                    30, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r,"DownloadTask");
                    thread.setDaemon(false);
                    return thread;
                }
            });
        }
        return executorService;
    }



    public DownloadTask(String mUrl, long mContentLength,DownloadCallback callback) {
        this.mUrl = mUrl;
        this.mContentLength = mContentLength;
        mRunnables = new ArrayList<>();
        this.mCallback = callback;
    }

    public void init(){
        for (int i = 0; i < THREAD_SIZE; i++) {
            // 计算每个线程下载的大小和位置
            long threadSize = mContentLength / THREAD_SIZE;

            long start = i*threadSize;
            long end = (i+threadSize)-1;

            if (i == THREAD_SIZE -1){
                end = mContentLength-1;
            }


            // 缓存下载进度
            List<DownloadEntity> downloadEntities =
                    DaoManagerHelper.getInstance().queryAll(mUrl);
            DownloadEntity entity = getEntity(i, downloadEntities);
            if (entity==null){
                entity = new DownloadEntity(start,end,mUrl,i,0,mContentLength);
            }

            DownloadRunnable downloadRunnable = new DownloadRunnable(
                    mUrl,i,start,end,entity.getProgress(),entity,new DownloadCallback(){
                @Override
                public void onFailure(Exception e) {
                    mCallback.onFailure(e);
                }

                @Override
                public void onSucceed(File file) {
                    // 线程同步一下
                    synchronized (DownloadTask.this){
                        mSucceedNumber +=1;
                        // 文件下载成功回调
                        if (mSucceedNumber == THREAD_SIZE){
                            mCallback.onSucceed(file);
                            DownloadDispatcher.getInstance().recyclerTask(DownloadTask.this);
                        }
                    }
                }
            }
            );
            // 通知线程池去执行
            executorService().execute(downloadRunnable);
        }
    }

    /**
     * 获取指定的缓存实体
     * @param threadId
     * @param entites
     * @return
     */
    private DownloadEntity getEntity(int threadId,List<DownloadEntity> entites){
        for (DownloadEntity entity : entites) {
            if (threadId == entity.getThreadId()){
                return entity;
            }
        }
        return null;
    }
    /**
     * 停止所有线程
     */
    public void stop(){
        for (DownloadRunnable runnable : mRunnables) {
            runnable.stop();
        }
    }


}
