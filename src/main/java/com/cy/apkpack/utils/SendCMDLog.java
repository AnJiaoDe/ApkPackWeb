package com.cy.apkpack.utils;

import com.cy.apkpack.Constants;
import com.cy.apkpack.bean.ResponseBaseBean;

import java.io.InputStream;

public class SendCMDLog {
    private Process process;
    private String sid="";
    private String opt="";
    private String successFlag="";
    private String encodeType = "gbk";
    private Object data="";
    private boolean isReadLine = true;

    public SendCMDLog setProcess(Process process) {
        this.process = process;
        return this;
    }

    public SendCMDLog setSid(String sid) {
        this.sid = sid;
        return this;
    }

    public SendCMDLog setOpt(String opt) {
        this.opt = opt;
        return this;
    }

    public SendCMDLog setSuccessFlag(String successFlag) {
        this.successFlag = successFlag;
        return this;
    }

    public SendCMDLog setEncodeType(String encodeType) {
        this.encodeType = encodeType;
        return this;
    }

    public SendCMDLog setReadLine(boolean readLine) {
        isReadLine = readLine;
        return this;
    }

    public SendCMDLog setData(Object data) {
        this.data = data;
        return this;
    }

    /**
     * 发送CMD LOG 到网页
     *
     * @return
     */
    public ResponseBaseBean sendLog() {
        return optSendLog(process, sid, opt, successFlag, data);
    }


    private ResponseBaseBean optSendLog(Process process, String sid, String opt, String successFlag, Object data) {
        ResponseBaseBean responseBaseBean = new ResponseBaseBean();
        responseBaseBean.setCode(Constants.CODE_DOING)
                .setMsg(opt + "...")
                .setData("")
                .send(sid);
        IOUtils ioUtils = new IOUtils();
        ioUtils.setEncodeType(encodeType);

        ProcessLogRunnable infoLogRunnable = new ProcessLogRunnable();
        ProcessLogRunnable errorLogRunnable = new ProcessLogRunnable();
        infoLogRunnable.setInputStream(process.getInputStream())
                .setSid(sid)
                .setOpt(opt)
                .setSuccessFlag(successFlag)
                .setData(data)
                .setReadLine(isReadLine)
                .setIoUtils(ioUtils)
                .setLogCallback(new LogCallback() {
                    @Override
                    public void onResult(ResponseBaseBean response) {
                        if (response.getCode() == Constants.CODE_SUCCESS) {

                            responseBaseBean.setCode(response.getCode())
                                    .setMsg(response.getMsg())
                                    .setData(response.getData());
                        }
                    }
                });
        Thread thread_info = new Thread(infoLogRunnable);

        //????????????????????????????????????????????????????????????????????????????????????
        errorLogRunnable.setInputStream(process.getErrorStream())
                .setSid(sid)
                .setReadLine(false)
                .setOpt(opt)
                .setSuccessFlag(successFlag)
                .setData(data)
                .setIoUtils(ioUtils)
                .setLogCallback(new LogCallback() {
                    @Override
                    public void onResult(ResponseBaseBean response) {
                    }
                });
        Thread thread_error = new Thread(errorLogRunnable);

        thread_info.start();
        thread_error.start();

        try {
            thread_info.join();
            thread_error.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LogUtils.log("都完成");
        if (responseBaseBean.getCode() != Constants.CODE_SUCCESS) {

            responseBaseBean.setCode(Constants.CODE_FAIL)
                    .setMsg(opt + "失败")
                    .setData(data)
                    .send(sid);
        }

        return responseBaseBean;

    }


    private static class ProcessLogRunnable implements Runnable {
        private InputStream inputStream;
        private IOUtils ioUtils;
        private String sid;
        private String opt;
        private String successFlag;
        private Object data;
        private boolean isReadLine = true;
        private ResponseBaseBean responseBaseBean = new ResponseBaseBean();
        private LogCallback logCallback;


        public ProcessLogRunnable() {

        }

        public ProcessLogRunnable setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }

        public ProcessLogRunnable setIoUtils(IOUtils ioUtils) {
            this.ioUtils = ioUtils;
            return this;
        }


        public ProcessLogRunnable setSid(String sid) {
            this.sid = sid;
            return this;
        }

        public ProcessLogRunnable setOpt(String opt) {
            this.opt = opt;
            return this;
        }

        public ProcessLogRunnable setSuccessFlag(String successFlag) {
            this.successFlag = successFlag;
            return this;
        }

        public ProcessLogRunnable setData(Object data) {
            this.data = data;
            return this;
        }

        public ProcessLogRunnable setReadLine(boolean readLine) {
            isReadLine = readLine;
            return this;
        }

        public void setLogCallback(LogCallback logCallback) {
            this.logCallback = logCallback;
        }

        @Override
        public void run() {
            StringBuilder stringBuilder = new StringBuilder();
            ioUtils.readL2StrNoBuffer(inputStream, new IOListener<String>() {
                @Override
                public void onCompleted(String result) {

                    result=stringBuilder.toString();
                    if (result.contains(successFlag)) {
                        responseBaseBean.setCode(Constants.CODE_SUCCESS);
                        if (isReadLine) {
                            responseBaseBean.setMsg(opt + "成功");

                        } else {
                            responseBaseBean.setMsg(result  + opt + "成功");

                        }
                        responseBaseBean.setData(data)
                                .send(sid);

                    } else {
                        if (!isReadLine)
                            responseBaseBean.setCode(Constants.CODE_DOING)
                                    .setMsg(result )
                                    .setData(data)
                                    .send(sid);

                    }

                    logCallback.onResult(responseBaseBean);
                }

                @Override
                public void onLoding(String readedPart, long current, long length) {

                    if (isReadLine) {

                        responseBaseBean.setCode(Constants.CODE_DOING)
                                .setMsg(readedPart)
                                .setData("")
                                .send(sid);
                        logCallback.onResult(responseBaseBean);

                    }
                    stringBuilder.append(readedPart + "\n");
                }

                @Override
                public void onInterrupted() {
                    responseBaseBean.setCode(Constants.CODE_FAIL)
                            .setMsg(opt + "失败，过程被打断")
                            .setData("")
                            .send(sid);
                    logCallback.onResult(responseBaseBean);


                }

                @Override
                public void onFail(String errorMsg) {
                    responseBaseBean.setCode(Constants.CODE_FAIL)
                            .setMsg(opt + "失败" + errorMsg)
                            .setData("")
                            .send(sid);
                    logCallback.onResult(responseBaseBean);


                }
            });
            LogUtils.log("完成");
        }
    }


}
