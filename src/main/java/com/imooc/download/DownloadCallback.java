package com.imooc.download;

import java.io.File;

public interface DownloadCallback {

    void onFailure(Exception e);

    void onSucceed(File file);
}
