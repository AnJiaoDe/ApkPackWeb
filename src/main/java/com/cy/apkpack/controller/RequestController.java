package com.cy.apkpack.controller;

import com.cy.apkpack.Constants;
import com.cy.apkpack.bean.GenerateKeystoreBean;
import com.cy.apkpack.bean.ResponseBaseBean;
import com.cy.apkpack.utils.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@org.springframework.stereotype.Controller
@RestController
@RequestMapping("/apkPack")
public class RequestController {
    /**
     * 上传file
     *
     * @param file
     * @return
     */
    @PostMapping("/upload_file")
    @ResponseBody
    @Async
    public ResponseBaseBean upload_file(@RequestParam("fileType") String fileType, @RequestParam("file") MultipartFile file) {
        ResponseBaseBean responseBaseBean = new ResponseBaseBean();
        String path = file.getOriginalFilename();

        String suffixName = path.substring(path.lastIndexOf(".") + 1, path.length());
        final String uuid = UUIDUtils.getUUID();
        String fileName = path.substring(path.lastIndexOf("\\") + 1, path.lastIndexOf(".")) + uuid + "." + suffixName;
        LogUtils.log("fileName", fileName);
        File dest = null;
        switch (fileType) {
            case Constants.FILE_TYPE_APK:
                dest = new File(Constants.PATH_APK + File.separator + fileName);

                break;
        }

        try {
            file.transferTo(dest);
            responseBaseBean.setCode(Constants.CODE_SUCCESS)
                    .setMsg("上传成功")
                    .setData(fileName);

        } catch (IOException e) {
            responseBaseBean.setCode(Constants.CODE_FAIL)
                    .setMsg("上传失败" + e.getMessage())
                    .setData("");

        }
        return responseBaseBean;
    }

    /**
     * 压缩文件夹
     *
     * @param fileName
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/compress_file")

    public ResponseBaseBean compress_file(@RequestParam("fileType") String fileType, @RequestParam("fileName") String fileName, HttpServletRequest request, HttpServletResponse response) {

        String inPath = "", outPath = "";
        String fileNameSend = "";
        switch (fileType) {
//            case Constants.FILE_TYPE_APK:
//
//                break;
            case Constants.FILE_TYPE_DCODEAPK:
                inPath = Constants.PATH_DAPK + File.separator + FileUtils.subNameNoSuffix(fileName);
                outPath = Constants.PATH_DAPK + File.separator + FileUtils.subNameNoSuffix(fileName) + ".rar";
                fileNameSend = FileUtils.subNameNoSuffix(fileName) + ".rar";
                break;
//            case Constants.FILE_TYPE_RESSRC:
//                break;
            case Constants.FILE_TYPE_PLUGIN:
                inPath = Constants.PATH_PLUGIN + File.separator + fileName;
                outPath = Constants.PATH_PLUGIN + File.separator + fileName + ".rar";
                fileNameSend = fileName + ".rar";
                break;
        }
        final String fileNameS = fileNameSend;
        ResponseBaseBean responseBaseBean = new ResponseBaseBean();
        responseBaseBean.setCode(Constants.CODE_DOING)
                .setMsg("压缩中...")
                .setData("");
        FileCompressUtils zipUtils = new FileCompressUtils();
        zipUtils.compressThread(inPath, outPath, new FileCompressCallback() {
            @Override
            public void onCompleted(long time) {
                responseBaseBean.setCode(Constants.CODE_SUCCESS)
                        .setMsg("压缩文件成功")
                        .setData(fileNameS);

            }


            @Override
            public void onInterrupted() {
                responseBaseBean.setCode(Constants.CODE_FAIL)
                        .setMsg("压缩文件失败，压缩被中断或者取消")
                        .setData("");
            }

            @Override
            public void onFail(String errorMsg) {
                responseBaseBean.setCode(Constants.CODE_FAIL)
                        .setMsg("压缩文件失败" + errorMsg)
                        .setData("");

            }
        });


        return responseBaseBean;

    }


    /**
     * 下载文件
     *
     * @param fileName 必须带后缀
     * @param request
     * @param response
     */
    @PostMapping("/download_file")
    @Async

    public void download_file(@RequestParam("fileType") String fileType, @RequestParam("fileName") String fileName, HttpServletRequest request, HttpServletResponse response) {
        try {
            File file = null;
//            String suffixName = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
            switch (fileType) {
                case Constants.FILE_TYPE_KEYSTORE:
                    file = new File(Constants.PATH_KEYSTORE + File.separator + fileName);

                    break;
                case Constants.FILE_TYPE_APK:
                    file = new File(Constants.PATH_APK + File.separator + fileName);

                    break;
                case Constants.FILE_TYPE_DCODEAPK:
                    file = new File(Constants.PATH_DAPK + File.separator + fileName);
//                    if (file == null || file.length() == 0) {
//                        ResponseBaseBean r = compress_file(Constants.FILE_TYPE_DCODEAPK, fileName, request, response);
//                        if (r.getCode() != Constants.CODE_SUCCESS) break;
//                    }
                    break;
                case Constants.FILE_TYPE_PLUGIN:
                    file = new File(Constants.PATH_PLUGIN + File.separator + fileName);
//                    if (file == null || file.length() == 0) {
//                        ResponseBaseBean r = compress_file(Constants.FILE_TYPE_PLUGIN, fileName, request, response);
//                        if (r.getCode() != Constants.CODE_SUCCESS) break;
//                    }
                    break;
            }

            //下载此文件
            InputStream inputStream = new FileInputStream(file);
            OutputStream outputStream = response.getOutputStream();
            //指明为下载

            response.setContentType("application/force-download");// 设置强制下载不打开

            response.addHeader("Content-Disposition", "attachment;fileName=" + file.getName());   // 设置文件名

            new IOUtils().read2File(inputStream, outputStream, new IOListener() {
                @Override
                public void onCompleted(Object result) {

                }

                @Override
                public void onLoding(Object readedPart, long current, long length) {

                }

                @Override
                public void onInterrupted() {

                }

                @Override
                public void onFail(String errorMsg) {

                }
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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





    /**
     * socket连接地址
     *
     * @param sid
     * @return
     */
    @GetMapping("/socket/{sid}")
    public String socket(@PathVariable String sid) {
        return sid;
    }
    //推送数据接口
//    @ResponseBody
//    @RequestMapping("/springbootwebsocket/socket/push/{cid}")
//    public String pushToWeb(@PathVariable String cid,String message) {
//        try {
//            WebSocketServer.sendInfo(message,cid);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return cid+"#"+e.getMessage();
//        }
//
//        return cid;
//    }

}