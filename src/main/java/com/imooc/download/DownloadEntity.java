package com.imooc.download;

public class DownloadEntity {

    private int threadId;
    private long progress;
    private long start;
    private long end;
    private String mUrl;
    private long contentLength;

    public DownloadEntity(long start, long end, String url, int threadId, int progress, long contentLength) {
        this.start = start;
        this.end = end;
        this.mUrl = url;
        this.threadId = threadId;
        this.progress = progress;
        this.contentLength = contentLength;
    }

    public long getProgress() {
        return progress;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public String getmUrl() {
        return mUrl;
    }

    public long getContentLength() {
        return contentLength;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }
}
