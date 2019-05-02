package com.cy.apkpack.utils;


import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 最近碰到个需要下载zip压缩包的需求，于是我在网上找了下别人写好的zip工具类。但找了好多篇博客，总是发现有bug。因此就自己来写了个工具类。
 * 这个工具类的功能为：
 * （1）可以压缩文件，也可以压缩文件夹
 * （2）同时支持压缩多级文件夹，工具内部做了递归处理
 * （3）碰到空的文件夹，也可以压缩
 * （4）可以选择是否保留原来的目录结构，如果不保留，所有文件跑压缩包根目录去了，且空文件夹直接舍弃。注意：如果不保留文件原来目录结构，在碰到文件名相同的文件时，会压缩失败。
 * （5）代码中提供了2个压缩文件的方法，一个的输入参数为文件夹路径，一个为文件列表，可根据实际需求选择方法。
 */
public class FileCompressUtils {


    private FileCompressThread fileCompressThread;
    private UnZipThread unZipThread;
    private UnRarThread unRarThread;

    public FileCompressUtils() {

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

    public void stopCompress() {
        if (fileCompressThread != null) fileCompressThread.stop();
    }

    public void stopUnZip() {
        if (unZipThread != null) unZipThread.stop();
    }

    public void stopUnRar() {
        if (unRarThread != null) unRarThread.stop();
    }


    /**
     * 压缩成任意格式
     *
     * @param inPath
     * @param outPath
     */
    public void compressThread(String inPath, String outPath, FileCompressCallback fileCompressCallback) {


        fileCompressThread = new FileCompressThread(inPath, outPath, fileCompressCallback);
        fileCompressThread.run();
    }

    /**
     * 解压zip
     *
     * @param inPath
     * @param outPath
     */
    public void unZipThread(String inPath, String outPath, FileCompressCallback fileCompressCallback) {
        unZipThread = new UnZipThread(inPath, outPath, fileCompressCallback);
        unZipThread.run();
    }

    /**
     * 解压rar
     *
     * @param inPath
     * @param outPath
     */
    public void unRarThread(String inPath, String outPath, FileCompressCallback fileCompressCallback) {
        unRarThread = new UnRarThread(inPath, outPath, fileCompressCallback);
        unRarThread.run();
    }

    public void unRarOrZipThread(String inPath, String outPath, FileCompressCallback fileCompressCallback) {
        if (inPath.toLowerCase().endsWith(".rar")) {
            unRarThread = new UnRarThread(inPath, outPath, fileCompressCallback);
            unRarThread.run();
        } else if (inPath.toLowerCase().endsWith(".zip")) {
            unZipThread = new UnZipThread(inPath, outPath, fileCompressCallback);
            unZipThread.run();
        }

    }

    /**
     * 压缩文件成任意格式
     */
    private static class FileCompressThread implements Runnable {
        private boolean isRunning = true, isFailed = false;


        private String inPath, outPath;
        private FileCompressCallback fileCompressCallback;

        public FileCompressThread(String inPath, String outPath, FileCompressCallback fileCompressCallback) {
            this.inPath = inPath;
            this.outPath = outPath;
            this.fileCompressCallback = fileCompressCallback;
        }

        public void stop() {
            this.isRunning = false;
        }

        @Override
        public void run() {

            long timeStampBegin = System.currentTimeMillis();
            try {
                File file = new File(outPath);
//                if (file != null && file.length() > 0) {
//                    fileCompressCallback.onCompleted((System.currentTimeMillis() - timeStampBegin));
//                    return;
//                }
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                ZipOutputStream zos = new ZipOutputStream(fileOutputStream);
                File sourceFile = new File(inPath);
                compress(sourceFile, zos, sourceFile.getName(), fileCompressCallback);

                //一定要记得关闭，否则压缩失败
                close(zos);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                fileCompressCallback.onFail(e.getMessage());
            }

            if (!isFailed) fileCompressCallback.onCompleted((System.currentTimeMillis() - timeStampBegin));


        }

        /**
         * 递归压缩方法
         *
         * @param sourceFile 源文件
         * @param zos        zip输出流
         * @param name       压缩后的名称
         *                   //         * @param   是否保留原来的目录结构,true:保留目录结构;
         *                   false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
         * @throws Exception
         */
        private void compress(File sourceFile, ZipOutputStream zos, String name, FileCompressCallback fileCompressCallback) {
            try {


                byte[] buf = new byte[1024];


                if (sourceFile.isFile()) {
                    // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
                    zos.putNextEntry(new ZipEntry(name));
                    // copy文件到zip输出流中
                    int len = 0;

                    FileInputStream in = new FileInputStream(sourceFile);
                    while (isRunning && (len = in.read(buf)) != -1) {
                        zos.write(buf, 0, len);
                    }
                    //中断

                    if (len != -1) {
                        fileCompressCallback.onInterrupted();
                        return;
                    }

                    // Complete the entry
                    zos.closeEntry();
                    in.close();


                } else {
                    File[] listFiles = sourceFile.listFiles();
                    if (listFiles == null || listFiles.length == 0) {
                        // 需要保留原来的文件结构时,需要对空文件夹进行处理
//                        if (KeepDirStructure) {
                        // 空文件夹的处理
                        zos.putNextEntry(new ZipEntry(name + File.separator));
                        // 没有文件，不需要文件的copy
                        zos.closeEntry();
//                        }

                    } else {

                        for (File file : listFiles) {
                            // 判断是否需要保留原来的文件结构
//                            if (KeepDirStructure) {
                            // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                            // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                            compress(file, zos, name + File.separator + file.getName(), fileCompressCallback);
//                            } else {
//                                compress(file, zos, file.getName(), KeepDirStructure, ioZipListener);
//                            }

                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                isFailed = true;
                fileCompressCallback.onFail(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                isFailed = true;

                fileCompressCallback.onFail(e.getMessage());

            }


        }
    }

    /**
     * 解压zip
     */
    private static class UnZipThread implements Runnable {
        private String inPath, outPath;
        private boolean isRunning = true, isFailed = false;

        private long current = 0, contentLength;
        private FileCompressCallback fileCompressCallback;

        public UnZipThread(String inPath, String outPath, FileCompressCallback fileCompressCallback) {
            this.inPath = inPath;
            this.outPath = outPath;
            this.fileCompressCallback = fileCompressCallback;
        }

        public void stop() {
            this.isRunning = false;
        }

        @Override
        public void run() {
            long timeStampBegin = System.currentTimeMillis();
            if (!inPath.toLowerCase().endsWith(".zip")) {

                fileCompressCallback.onFail("压缩文件不是zip格式，无法解压");

                return;
            }
            try {
                File pathFile = new File(outPath);
                if (!pathFile.exists()) {
                    pathFile.mkdirs();
                }
                ZipFile zip = new ZipFile(new File(inPath), Charset.forName("GBK"));
                for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    String zipEntryName = entry.getName();
                    InputStream in = zip.getInputStream(entry);
                    String path = (outPath + File.separator + zipEntryName).replaceAll("\\*", File.separator);
                    //判断路径是否存在,不存在则创建文件路径
                    File file = new File(path.substring(0, path.lastIndexOf('/')));
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
                    if (new File(path).isDirectory()) {
                        continue;
                    }
                    //输出文件路径信息
//            System.out.println(outPath);

                    OutputStream out = new FileOutputStream(path);
                    byte[] buf = new byte[1024];
                    int len = 0;

                    while (isRunning && (len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                        current += len;
                    }
                    //中断

                    if (len != -1) {
                        fileCompressCallback.onInterrupted();
                        return;
                    }
                    out.close();
                    in.close();
                }

                fileCompressCallback.onCompleted((System.currentTimeMillis() - timeStampBegin));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                fileCompressCallback.onFail(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                fileCompressCallback.onFail(e.getMessage());

            }

        }
    }

    /**
     * 解压rar
     */
    private static class UnRarThread implements Runnable {
        private String inPath, outPath;
        private boolean isRunning = true;

        private FileCompressCallback fileCompressCallback;

        public UnRarThread(String inPath, String outPath, FileCompressCallback fileCompressCallback) {
            this.inPath = inPath;
            this.outPath = outPath;
            this.fileCompressCallback = fileCompressCallback;
        }

        public void stop() {
            this.isRunning = false;
        }

        @Override
        public void run() {
            long timeStampBegin = System.currentTimeMillis();
            if (!inPath.toLowerCase().endsWith(".rar")) {
                fileCompressCallback.onFail("压缩文件不是rar格式，无法解压");

                return;
            }
            try {

                File dstDiretory = new File(outPath);
                if (!dstDiretory.exists()) {// 目标目录不存在时，创建该文件夹
                    dstDiretory.mkdirs();
                }
                Archive archive = new Archive(new File(inPath), null);
                if (archive != null) {
                    FileHeader fileHeader = archive.nextFileHeader();
                    while (fileHeader != null) {
                        if (!isRunning) {
                            fileCompressCallback.onInterrupted();
                            return;
                        }
                        // 防止文件名中文乱码问题的处理
                        String fileName = fileHeader.getFileNameW().isEmpty() ? fileHeader.getFileNameString() : fileHeader.getFileNameW();
                        if (fileHeader.isDirectory()) { // 文件夹
                            File fol = new File(outPath + File.separator + fileName);
                            fol.mkdirs();
                        } else { // 文件
                            File out = new File(outPath + File.separator + fileName.trim());
                            try {
                                if (!out.exists()) {
                                    if (!out.getParentFile().exists()) {// 相对路径可能多级，可能需要创建父目录.
                                        out.getParentFile().mkdirs();
                                    }
                                    out.createNewFile();
                                }
                                FileOutputStream os = new FileOutputStream(out);
                                archive.extractFile(fileHeader, os);
                                os.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        fileHeader = archive.nextFileHeader();
                    }
                    archive.close();
                }
                fileCompressCallback.onCompleted((System.currentTimeMillis() - timeStampBegin));

            } catch (IOException e) {
                e.printStackTrace();
                fileCompressCallback.onFail(e.getMessage());

            } catch (RarException e) {
                e.printStackTrace();
                fileCompressCallback.onFail(e.getMessage());

            }


        }
    }


}
