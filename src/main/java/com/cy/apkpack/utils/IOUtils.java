package com.cy.apkpack.utils;

import java.io.*;

/**
 * Created by cy on 2018/12/24.
 */

public class IOUtils {
    private boolean isRunning = true;

    private long contentLength = 0;
    private String encodeType = "utf-8";

    public IOUtils() {
        isRunning = true;
    }

    public IOUtils setContentLength(long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public IOUtils setEncodeType(String encodeType) {
        this.encodeType = encodeType;
        return this;

    }

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        this.isRunning = false;
    }

    public void read(boolean isLine, InputStream inputStream, IOListener ioListener) {
        if (isLine) {
            readLine2String(inputStream, ioListener);
        } else {
            read2String(inputStream, ioListener);

        }
    }

    /**
     * @param inputStream
     * @param ioListener
     */


    public void read2String(InputStream inputStream, IOListener ioListener) {

        if (!(inputStream instanceof BufferedInputStream)) {
            inputStream = new BufferedInputStream(inputStream);
        }
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, encodeType);
            bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder sb = new StringBuilder();

            char[] buf = new char[1024];
            int len = 0;
            long current = 0;

            while (isRunning && (len = bufferedReader.read(buf)) != -1) {
                sb.append(buf, 0, len);
                current += len;
                ioListener.onLoding("", current, contentLength);
            }

            //中断
            if (len != -1) {
                ioListener.onInterrupted();
            } else {

                ioListener.onCompleted(sb.toString());

            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            ioListener.onFail(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            ioListener.onFail(e.getMessage());

        } finally {
            close(bufferedReader);
            close(inputStreamReader);
            close(inputStream);
        }
    }

    /**
     * 一行一行地读
     *
     * @param inputStream
     * @param ioListener
     */
    public void readLine2String(InputStream inputStream, IOListener ioListener) {

        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, encodeType);
            bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder sb = new StringBuilder();
            long current = 0;

            String str;
            while (isRunning && (str = bufferedReader.readLine()) != null) {
                sb.append(str);
                current += str.length();

                ioListener.onLoding(str, current, contentLength);

            }

            //中断
            if ((str = bufferedReader.readLine()) != null) {
                ioListener.onInterrupted();
            } else {

                ioListener.onCompleted(sb.toString());

            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            ioListener.onFail(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            ioListener.onFail(e.getMessage());

        } finally {
            close(bufferedReader);
            close(inputStreamReader);
            close(inputStream);
        }
    }
    /**
     * 一行一行地读，不拼接
     *
     * @param inputStream
     * @param ioListener
     */
    public  void readL2StrNoBuffer(InputStream inputStream, IOListener ioListener) {

        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, encodeType);
            bufferedReader = new BufferedReader(inputStreamReader);

            long current = 0;

            String str;
            while (isRunning && (str = bufferedReader.readLine()) != null) {
                current += str.length();

                ioListener.onLoding(str, current, contentLength);

            }

            //中断
            if ((str = bufferedReader.readLine()) != null) {
                ioListener.onInterrupted();
            } else {

                ioListener.onCompleted("");

            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            ioListener.onFail(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            ioListener.onFail(e.getMessage());

        } finally {
            close(bufferedReader);
            close(inputStreamReader);
            close(inputStream);
        }
    }
    /**
     * 一行一行地读，\n拼接
     *
     * @param inputStream
     * @param ioListener
     */
    public void readL_N2String(InputStream inputStream, IOListener ioListener) {

        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, encodeType);
            bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder sb = new StringBuilder();
            long current = 0;

            String str;
            while (isRunning && (str = bufferedReader.readLine()) != null) {
                sb.append(str);
                sb.append("\n");
                current += str.length();

                ioListener.onLoding(str, current, contentLength);

            }

            //中断
            if ((str = bufferedReader.readLine()) != null) {
                ioListener.onInterrupted();
            } else {

                ioListener.onCompleted(sb.toString());

            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            ioListener.onFail(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            ioListener.onFail(e.getMessage());

        } finally {
            close(bufferedReader);
            close(inputStreamReader);
            close(inputStream);
        }
    }

    /**
     * 读取到文件
     *
     * @param inputStream
     * @param outputStream
     * @param ioListener
     */
    public void read2File(InputStream inputStream, OutputStream outputStream, IOListener ioListener) {
        try {

            byte[] buffer = new byte[1024];
            int len = 0;
            long current = 0;

            while (isRunning && (len = inputStream.read(buffer)) != -1) {

                outputStream.write(buffer, 0, len);
                current += len;
                ioListener.onLoding(new String(buffer), current, contentLength);
            }

            outputStream.flush();
            //中断
            if (len != -1) {
                ioListener.onInterrupted();
            } else {

                ioListener.onCompleted(null);

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(outputStream);
            close(inputStream);
        }
    }


}
