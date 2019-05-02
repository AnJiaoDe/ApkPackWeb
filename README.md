@[toc]
[先看完Android反编译、签名、重打包、zipalign一条龙](https://www.jianshu.com/p/78758112206f)

**小编不是专业做JAVA后台和前端的，所以这2方面的代码肯定不够专业，小编是用Springboot搭建的后台**
![在这里插入图片描述](http://upload-images.jianshu.io/upload_images/11866078-83d9417d1556df8b.gif?imageMogr2/auto-orient/strip)
[GitHub源码](https://github.com/AnJiaoDe/ApkPackWeb)
要实现在java web中使用apktool等工具进行反编译、签名、重打包、zipalign,必须先实现java代码中嵌入CMD命令

## 1.JAVA代码嵌入CMD命令
使用 Runtime的这个函数

```
/**
第一个参数，CMD命令，第三个参数，能够执行CMD命令的文件所在目录，这里也就是apktool.jar和apktool.bat所在的目录
*/
public Process exec(String command, String[] envp, File dir)
        throws IOException {
        if (command.length() == 0)
            throw new IllegalArgumentException("Empty command");

        StringTokenizer st = new StringTokenizer(command);
        String[] cmdarray = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++)
            cmdarray[i] = st.nextToken();
        return exec(cmdarray, envp, dir);
    }
```
可以从执行CMD命令后返回的process得到inputstream,获取日志信息
![在这里插入图片描述](http://upload-images.jianshu.io/upload_images/11866078-3cd2afaaf8a575f6.gif?imageMogr2/auto-orient/strip)
```
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
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

```

![在这里插入图片描述](http://upload-images.jianshu.io/upload_images/11866078-d9e8fcd6e7db0401.gif?imageMogr2/auto-orient/strip)
问题来了，process.getInputStream()获取的只是info级别的日志信息，如何输出info级别日志信息的同时输出error级别的日志信息呢？
用：process.getErrorStream()，
![在这里插入图片描述](http://upload-images.jianshu.io/upload_images/11866078-9e0f77c3536d2a0d?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
其实以上写法不太好，最好的写法是，process.getInputStream()，process.getErrorStream()，都分别开启一个子线程，同时读取日志信息，才是符合常理的。
代码如下：

```
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

```
关键部分：

```
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
```

## 2.使用WebSocket

**1.配置WebSocket**

```
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
```

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.4.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.cy</groupId>
    <artifactId>apkpack</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>ApkPack</name>
    <description>Demo project for Spring Boot</description>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>com.github.junrar</groupId>
            <artifactId>junrar</artifactId>
            <version>3.1.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>

```

```
@Configuration
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
```
**2.创建WebSocketServer类（名字随便）**
注意地址：@ServerEndpoint("/apkPack/websocket/{sid}")
将信息封装于ResponseBaseBean中，获取JSON数据，并发送

```
public class ResponseBaseBean<T> {
    private int code;
    private String msg;
    private T data;

    public ResponseBaseBean() {
    }

    public ResponseBaseBean(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public ResponseBaseBean setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public ResponseBaseBean setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public T getData() {
        return data;
    }

    public ResponseBaseBean setData(T data) {
        this.data = data;
        return this;
    }


    public ResponseBaseBean send(String sid) {
        WebSocketServerManager.sendMessage(sid, toJson());

        return this;
    }

    public String toJson() {
        return JSON.toJSONString(this);
    }
}

```

```
@ServerEndpoint("/apkPack/websocket/{sid}")
@Component
public class WebSocketServer {


    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    //接收sid
    private String sid = "";

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        this.session = session;
        this.sid = sid;
        WebSocketServerManager.addWebSocketServer(this); //加入set中
        LogUtils.log("有新窗口开始监听:" + sid + ",当前在线人数为" + WebSocketServerManager.getMap_wbserver().size());
        sendMessage(new ResponseBaseBean(Constants.CODE_WEBSOCKET_OPEN,"WebSocket连接成功","WebSocket连接成功").toJson());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        WebSocketServerManager.removeWebSocketServer(this); //从set中删除
        LogUtils.log("有一连接关闭！当前在线人数为" + WebSocketServerManager.getMap_wbserver().size());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(Session session, String message) {
        LogUtils.log("收到来自窗口" + sid + "的信息:" + message);
    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        LogUtils.log("发生错误");
        error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     */
    public synchronized void sendMessage(String msg) {
        try {
            session.getBasicRemote().sendText(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Session getSession() {
        return session;
    }


    public String getSid() {
        return sid;
    }


}

```
**3.WebSocket管理类**
```
public class WebSocketServerManager {
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static ConcurrentMap<String, WebSocketServer> map_wbserver = new ConcurrentHashMap();

    private  WebSocketServerManager(){

    }
//    private static class WebSocketServerManagerFactory{
//        private static WebSocketServerManager instance=new WebSocketServerManager();
//    }
//    public static WebSocketServerManager getInstance(){
//        return WebSocketServerManagerFactory.instance;
//    }
    public static void addWebSocketServer(WebSocketServer webSocketServer){
        map_wbserver.put(webSocketServer.getSid(),webSocketServer);

    }
    public static void removeWebSocketServer(WebSocketServer webSocketServer){
        map_wbserver.remove(webSocketServer.getSid());
    }
    public static void sendMessage(String sid, String msg){

        map_wbserver.get(sid).sendMessage(msg);
    }
    public static ConcurrentMap<String, WebSocketServer> getMap_wbserver() {
        return map_wbserver;
    }


}
```
**4.web前端使用**
注意前文地址：@ServerEndpoint("/apkPack/websocket/{sid}")
sid此处使用UUID，为了唯一标识客户端
获取到信息，读取JSON并解析
```
 //websocket 打开推送消息功能
            function openLog() {

                if (typeof (WebSocket) == "undefined") {
                    console.log("您的浏览器不支持WebSocket");
                } else {
                    console.log("您的浏览器支持WebSocket");
//实现化WebSocket对象，指定要连接的服务器地址与端口 建立连接
//等同于socket = new WebSocket("ws://localhost:8083/checkcentersys/websocket/20");
//socket = new WebSocket("http://localhost:8080/websocket/${cid}".replace("http","ws"));
                    var socket = new WebSocket("ws://localhost:8080/apkPack/websocket/" + UUID);

//获得消息事件
                    socket.onmessage = function (data) {
                        console.log(data.data);
                        var response = jQuery.parseJSON(data.data);

                        switch (response.code) {
                            //socket连接成功
                            case CODE_WEBSOCKET_OPEN:
                                console.log("Socket 已打开");
                                // 开始反编译
                                $.ajax({
                                    url: "/apkPack/decode_apk",
                                    type: 'POST',
                                    async: true,
                                    data: {uuid: UUID, fileName: fileName},
                                    error: function (data) {

                                        $("#span_decode_suc").removeClass("displaynone");
                                        $("#span_decode_suc").text(data.message);
                                    }
                                });
                                break
                            case CODE_DOING://反编译进行中
                                $("#textarea_log_dapk").append(response.msg + "\n");


                                break
                            case CODE_SUCCESS://反编译成功
                                $("#textarea_log_dapk").append(response.msg + "\n");
                                $("#a_download").addClass("color_theme");
                                $("#a_download").removeClass("tab_unenable");

                                socket.close();
                                break
                            case CODE_FAIL://反编译失败
                                $("#textarea_log_dapk").append(response.msg + "\n");
                                socket.close();

                                break
                        }
//发现消息进入 开始处理前端触发逻辑
                    };
//关闭事件
                    socket.onclose = function () {
                        console.log("Socket已关闭");
                    };
//发生了错误事件
                    socket.onerror = function () {
                        console.log("Socket发生了错误");
//此时可以尝试刷新页面
                    }
//离开页面时，关闭socket
//$1.8中已经被废弃，3.0中已经移除
// $(window).unload(function(){
// socket.close();
//});
                }
            }
```

## 3.JAVA代码执行CMD命令获取日志信息利用WebSocket实时推送到Web前端
页面配置：

```
@RequestMapping("/apkPack")
public class HtmlController {
    @GetMapping("/index")
    public String index() {
        return "/html/index.html";
    }
    @GetMapping("/apk_decode")
    public String apk_decode() {

        return "/html/apk_decode.html";
    }

}
```

```

    /**
     * 反编译APK
     *
     * @param fileName ,带后缀
     */
    @PostMapping("/decode_apk")

    public ResponseBaseBean decode_apk(@RequestParam("uuid") String uuid, @RequestParam("fileName") String fileName) {
        ResponseBaseBean responseBaseBean = new ResponseBaseBean();


        try {
            Process process = Runtime.getRuntime().exec(Constants.CMD_DAPK_D + Constants.PATH_APK + File.separator + fileName + Constants.CMD_O +
                    Constants.PATH_DAPK + File.separator + fileName.substring(0, fileName.lastIndexOf(".")), null, new File(Constants.PATH_APKJAR));

            responseBaseBean = new SendCMDLog().setProcess(process)
                    .setSid(uuid)
                    .setOpt("反编译")
                    .setSuccessFlag("Copying original files")
                    .setData("")
                    .sendLog();
        } catch (IOException e) {
            e.printStackTrace();
            responseBaseBean.setCode(Constants.CODE_FAIL)
                    .setMsg("反编译失败" + e.getMessage())
                    .setData("")
                    .send(uuid);
        }

        return responseBaseBean;
    }
```
效果图如下：
![在这里插入图片描述](http://upload-images.jianshu.io/upload_images/11866078-2384502cc86c0646.gif?imageMogr2/auto-orient/strip)
**java中重打包CMD**

```
 Process process = Runtime.getRuntime().exec(Constants.CMD_DAPK_B + Constants.PATH_DAPK + File.separator + fileName.substring(0, fileName.lastIndexOf(".")) + Constants.CMD_O +
                    Constants.PATH_APK + File.separator + fileName, null, new File(Constants.PATH_APKJAR));
```
**java中生成签名文件CMD**
```
String[] arstringCommand = new String[]{

                "keytool",
                "-genkey", // -genkey表示生成密钥
                "-validity", // -validity指定证书有效期(单位：天)，这里是36000天
                String.valueOf(generateKeystoreBean.getValidity()),

                "-keysize",//     指定密钥长度
                "1024",
                "-alias", // -alias指定别名，这里是ss
                generateKeystoreBean.getAlias(),
                // -keyalg 指定密钥的算法 (如 RSA DSA（如果不指定默认采用DSA）)

                "-keyalg", // -keyalg 指定密钥的算法 (如 RSA DSA（如果不指定默认采用DSA）)
                "RSA",
                "-keystore", // -keystore指定存储位置，这里是d:/demo.keystore
                Constants.PATH_KEYSTORE + File.separator + generateKeystoreBean.getKeyName() + ".jks",

                "-dname",// CN=(名字与姓氏), OU=(组织单位名称), O=(组织名称), L=(城市或区域名称),
                // ST=(州或省份名称), C=(单位的两字母国家代码)"
                "CN=(" + generateKeystoreBean.getOrganization() + "), OU=(" + generateKeystoreBean.getOrganization() + "), O=(" + generateKeystoreBean.getOrganization() + "), L=(" + generateKeystoreBean.getCity() + "), ST=(" + generateKeystoreBean.getProvince() + "), C=(CN)",
                "-storepass", // 指定密钥库的密码(获取keystore信息所需的密码)
                generateKeystoreBean.getKeyPwd(),

                "-keypass",// 指定别名条目的密码(私钥的密码)
                generateKeystoreBean.getAliasPwd(),

                "-v"// -v 显示密钥库中的证书详细信息
        };
```

**java中签名CMD**

```
 String[] arstringCommand = new String[]{

                    "jarsigner",
                    "-verbose",
                    "-keystore",
                    Constants.JKS_PATH,
                    "-storepass",
                    Constants.JKS_PWD,
                    "-signedjar",
                    Constants.PATH_APK + File.separator + StringUtils.subFileName(fileName) + "signed.apk",
                    Constants.PATH_APK + File.separator + fileName,
                    Constants.JKS_ALIAS

            };
```
**java中zipalignCMD**
```
 String[] arstringCommand = new String[]{

                    "jarsigner",
                    "-verbose",
                    "-keystore",
                    Constants.JKS_PATH,
                    "-storepass",
                    Constants.JKS_PWD,
                    "-signedjar",
                    Constants.PATH_APK + File.separator + StringUtils.subFileName(fileName) + "signed.apk",
                    Constants.PATH_APK + File.separator + fileName,
                    Constants.JKS_ALIAS

            };
```

## 小编此处只透露反编译过程，其他过程不便透露
文章前面已提供源码

## 各位老铁有问题欢迎及时联系、指正、批评、撕逼

[GitHub](https://github.com/AnJiaoDe)

关注专题[Android开发常用开源库](https://www.jianshu.com/c/3ff4b3951dc5)

[简书](https://www.jianshu.com/u/b8159d455c69)

 微信公众号
 ![这里写图片描述](http://upload-images.jianshu.io/upload_images/11866078-fcfbb45175f99de0?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

QQ群
![这里写图片描述](http://upload-images.jianshu.io/upload_images/11866078-a31ff40ac6850a6d?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
