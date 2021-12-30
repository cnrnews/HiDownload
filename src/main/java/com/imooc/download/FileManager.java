package com.imooc.download;

import android.content.Context;

import java.io.File;

public final class FileManager {

    // 下载目录
    private File mRootDir;
    private Context mContext;
    private static final FileManager sManager = new FileManager();

    private FileManager() {
    }

    public static FileManager manager(){
        return sManager;
    }

    public void init(Context context){
        mContext = context.getApplicationContext();
    }

    /**
     * 初始化文件下载目录
     * @param file
     */
    public void rootDir(File file){
        if (!file.exists()){
            file.mkdirs();
        }
        if (file.exists() && file.isDirectory()){
            mRootDir = file;
        }
    }

    /**
     * 通过网络路径获取本地文件路径
     * @param url
     * @return
     */
    public File getFile(String url){
        String fileName = Utils.md5Url(url);
        if (mRootDir == null){
            mRootDir = mContext.getCacheDir();
        }
        File file = new File(mRootDir,fileName);
        return file;
    }
}
