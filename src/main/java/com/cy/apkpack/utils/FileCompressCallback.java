package com.cy.apkpack.utils;

public interface FileCompressCallback {
    public void onCompleted(long time);
    public void onInterrupted();
    public void onFail(String errorMsg);
}
