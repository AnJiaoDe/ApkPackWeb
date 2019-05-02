package com.cy.apkpack.utils;

/**
 * Created by Administrator on 2018/12/25 0025.
 */

public interface IOListener<T> {
    public void onCompleted(T result);
    public void onLoding(T readedPart, long current, long length);
    public void onInterrupted();
    public void onFail(String errorMsg);
}
