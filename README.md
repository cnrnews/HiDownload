# HiDownload
OkHttp 多线程下载

## 添加依赖
```
implementation 'com.squareup.okhttp3:okhttp:4.9.1'
```

## 使用
1. 首先在 Application 入口进行初始化操作
```
DownloadFacade.getInstance().init();
```
2.调用文件下载
```
DownloadFacade.getInstance()
                .startDownload("", new DownloadCallback() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("TAG",e.getMessage());
                    }

                    @Override
                    public void onSucceed(File file) {
                        // 文件下载成功
                    }
                });
```
