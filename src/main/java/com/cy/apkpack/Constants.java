package com.cy.apkpack;

public class Constants {

    public static final int CODE_SUCCESS = 200;//成功
    public static final int CODE_FAIL = -200;//失败
    public static final int CODE_DOING = 201;//任务进行中
    public static final int CODE_WEBSOCKET_OPEN = 300;//websocket连接成功
    public static final int CODE_PACK_SUCCESS = 400;//打包成功
    public static final int CODE_GENERATE_PLUGIN_SUCCESS = 500;//生成插件成功


    public static final String PATH_RES_ROOT="C:/Users/cy/Desktop/ApkPackResources/";
    public static final String PATH_APK = PATH_RES_ROOT+"apk";
    public static final String PATH_DAPK = PATH_RES_ROOT+"dcodeapk";
    public static final String PATH_APKJAR = PATH_RES_ROOT+"apkjar";
    public static final String PATH_KEYSTORE = PATH_RES_ROOT+"keystore";
    public static final String JKS_PATH =PATH_KEYSTORE+"/sign.jks";
    public static final String PATH_PLUGIN = PATH_RES_ROOT+"plugin";

    public static final String JKS_ALIAS = "sign";
    public static final String JKS_PWD = "123456";


    public static final String FILE_TYPE_APK = "apk";//文件目录
    public static final String FILE_TYPE_DCODEAPK = "dcodeapk";
    public static final String FILE_TYPE_PLUGIN = "plugin";
    public static final String FILE_TYPE_KEYSTORE= "keystore";
    public static final String CMD_DAPK_D = PATH_RES_ROOT+"apkjar\\apktool.bat    -f d ";
    public static final String CMD_DAPK_B = PATH_RES_ROOT+"apkjar\\apktool.bat    -f b ";
    public static final String CMD_ZIPALIGN = PATH_RES_ROOT+"apkjar\\zipalign -f -v 4 ";
    public static final String CMD_O = " -o ";

    public static final String MANIFEST_MANIFEST="AndroidManifestManifest.xml";
    public static final String MANIFEST_APPLICATION="AndroidManifestApplication.xml";

    public static final String NAME_APPLICATIONNAME= "applicationName.txt";


}
