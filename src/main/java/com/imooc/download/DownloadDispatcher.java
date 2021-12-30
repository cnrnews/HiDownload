package com.imooc.download;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public final class DownloadDispatcher {

    private final Deque<DownloadTask> readyTasks = new ArrayDeque<>();
    private final Deque<DownloadTask> runingTasks = new ArrayDeque<>();
    private final Deque<DownloadTask> stopTasks = new ArrayDeque<>();

    private static final DownloadDispatcher  mInstance = new DownloadDispatcher();

    private DownloadDispatcher() {
    }

    public static DownloadDispatcher getInstance(){
        return mInstance;
    }
    /**
     * 下载任务
     * @param url
     * @param callback
     */
    public void startDownload(String url,DownloadCallback callback){
        Call call =
                OkHttpManager.getManager().asyncCall(url);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // 获取文件大小
                long contentLength = response.body().contentLength();
                if (contentLength<=-1){
                    return;
                }

                // 计算每个线程负责哪一块
                DownloadTask downloadTask = new DownloadTask(url,contentLength,callback);
                downloadTask.init();

                runingTasks.add(downloadTask);
            }
        });
    }

    /**
     * 回收线程任务
     * @param downloadTask
     */
    public void recyclerTask(DownloadTask downloadTask) {
        runingTasks.remove(downloadTask);
    }
}
