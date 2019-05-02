package test;

import com.cy.apkpack.Constants;
import com.cy.apkpack.utils.IOListener;
import com.cy.apkpack.utils.IOUtils;
import com.cy.apkpack.utils.LogUtils;
import com.cy.apkpack.utils.SendCMDLog;

import java.io.File;
import java.io.IOException;

public class CmdTest {
    public static void main(String[] args) {
        try {
            Process process = Runtime.getRuntime().exec("C:/Users/cy/Desktop/ApkPackResources/apkjar/apktool.bat -f d " +
                    "C:/Users/cy/Desktop/ApkPackResources/apk/app-debug.apk -o C:/Users/cy/Desktop/ApkPackResources/dcodeapk/app-debug",
                    null, new File("C:/Users/cy/Desktop/ApkPackResources/apkjar"));

            new IOUtils().readLine2String(process.getInputStream(), new IOListener<String>() {
                @Override
                public void onCompleted(String result) {
                }

                @Override
                public void onLoding(String readedPart, long current, long length) {
                    LogUtils.log(readedPart);

                }

                @Override
                public void onInterrupted() {

                }

                @Override
                public void onFail(String errorMsg) {

                }
            });
            new IOUtils().readLine2String(process.getErrorStream(), new IOListener<String>() {
                @Override
                public void onCompleted(String result) {
                }

                @Override
                public void onLoding(String readedPart, long current, long length) {
                    LogUtils.log(readedPart);

                }

                @Override
                public void onInterrupted() {

                }

                @Override
                public void onFail(String errorMsg) {

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
