package com.cy.apkpack.utils;

import java.io.*;
import java.nio.channels.FileChannel;

public class FileUtils {
    public static String subNameNoSuffix(String fileName) {
        if (!fileName.contains(".")) return fileName;
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public static String subNameBeforDot(String fileName) {
        if (!fileName.contains(".")) return fileName;
        return fileName.substring(0, fileName.indexOf("."));
    }

    public static String subPathNoSuffix(String path) {
        if (!path.contains(".")) return path;
        return path.substring(0, path.lastIndexOf("."));
    }

    public static String getFileName(String pathFile) {
        return pathFile.substring(pathFile.lastIndexOf(File.separator) + 1, pathFile.length());
    }

    public static String getParentPath(String pathFile) {
        return pathFile.substring(0, pathFile.lastIndexOf(File.separator));
    }

    public static String getParentName(String pathFile) {
        String str = pathFile.substring(0, pathFile.lastIndexOf(File.separator));
        ;
        return str.substring(str.lastIndexOf(File.separator) + 1, str.length());
    }

    /**
     * 根据路径 创建文件
     *
     * @param pathFile
     * @return
     * @throws IOException
     */
    public static File createFile(String pathFile) throws IOException {
        File fileDir = new File(pathFile.substring(0, pathFile.lastIndexOf(File.separator)));
        File file = new File(pathFile);
        if (!fileDir.exists()) fileDir.mkdirs();
        if (!file.exists()) file.createNewFile();
        return file;
    }

    /**
     * 删除指定目录下文件及目录
     *
     * @return
     */
    public static boolean deleteFolder(String filePath) {
        File dirFile = new File(filePath);
        if (!dirFile.exists()) return false;
        if (dirFile.isDirectory()) {
            // 如果下面还有文件
            for (File file : dirFile.listFiles()) {
                deleteFolder(file.getAbsolutePath());
            }
        }
        return dirFile.delete();
    }

    /**
     * 寻找指定文件夹下的指定文件夹的路径
     *
     * @param pathFolder levels 遍历几层
     */
    public static String findFolder(String pathFolder, String folderName, int levels) {

        if (levels <= 0) return "";
        File fileRoot = new File(pathFolder);
        if (fileRoot.exists() && fileRoot.isDirectory()) {
            for (File file : fileRoot.listFiles()) {
                if (file.isDirectory() && file.getName().equals(folderName)) {
                    return file.getAbsolutePath();
                } else {
                    findFolder(file.getAbsolutePath(), folderName, --levels);

                }

            }
        }

        return "";

    }

    /**
     * 查找指定文件
     *
     * @param path
     * @param fileName
     * @return
     */
    public static String findFile(String path, String fileName) {

        File fileRoot = new File(path);
        if (!fileRoot.exists()) return "";
        if (fileRoot.getName().equals(fileName)) return fileRoot.getAbsolutePath();
        if (fileRoot.isDirectory()) {
            for (File file : fileRoot.listFiles()) {
                if (file.getName().equals(fileName)) {
                    return file.getAbsolutePath();
                } else {
                    String str = findFile(file.getAbsolutePath(), fileName);
                    if (!str.equals("")) return str;
                }

            }
        }

        return "";

    }

    /**
     * 复制、替换文件
     *
     * @param fromPath
     * @param toPath
     * @throws IOException
     */

    public static void copyFile(String fromPath, String toPath) throws IOException {
        File fileFrom = new File(fromPath);
        if (!fileFrom.exists()) return;
        File fileTo = createFile(toPath);
        FileChannel inputChannel = new FileInputStream(fileFrom).getChannel();
        FileChannel outputChannel = new FileOutputStream(fileTo).getChannel();
        outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        outputChannel.close();
        inputChannel.close();
    }

    /**
     * 复制、替换文件夹
     *
     * @param sourcePath
     * @param toPath
     * @throws IOException
     */

    public static void copyDir(String sourcePath, String toPath) throws IOException {
        File file = new File(sourcePath);
        if (!file.exists()) return;
        if (file.isFile()) {
            copyFile(sourcePath, toPath);

            return;
        }
        String[] names = file.list();

        if (!(new File(toPath)).exists()) {
            (new File(toPath)).mkdirs();
        }

        for (int i = 0; i < names.length; i++) {
            if ((new File(sourcePath + file.separator + names[i])).isDirectory()) {

                copyDir(sourcePath + file.separator + names[i], toPath + file.separator + names[i]);
                continue;
            }

            if (new File(sourcePath + file.separator + names[i]).isFile()) {
                copyFile(sourcePath + file.separator + names[i], toPath + file.separator + names[i]);
            }

        }
    }


    /**
     * 获取添加的文件
     *
     * @return
     */
    public static void getFileAdded(String pathSource, String pathAdded, String pathSeparate) throws IOException {

        File fileSource = new File(pathSource);
        File fileAdded = new File(pathAdded);
        if (!fileSource.exists() || !fileAdded.exists()) return;
        if (fileAdded.isDirectory()) {
            if (fileSource.isDirectory()) {

                String[] names_Added = fileAdded.list();
                String[] names_source = fileSource.list();

                for (String name : names_Added) {
                    if (names_source == null || names_source.length == 0) {
                        copyDir(pathAdded + File.separator + name, pathSeparate + File.separator + name);
                        continue;
                    }
                    for (int i = 0; i < names_source.length; i++) {
                        if (names_source[i].equals(name)) {

                            getFileAdded(pathSource + File.separator + name, pathAdded + File.separator + name, pathSeparate + File.separator + name);
                            break;
                        }
                        //同目录下，原来没有此文件，说明此文件是添加的文件，复制出去
                        if (i == names_source.length - 1)
                            copyDir(pathAdded + File.separator + name, pathSeparate + File.separator + name);
                    }


                }
            } else {

                copyDir(fileAdded.getAbsolutePath(), pathSeparate + File.separator + fileAdded.getName());
            }
        } else {

            if (!fileSource.getName().equals(fileAdded.getName()))
                copyFile(fileAdded.getAbsolutePath(), pathSeparate + File.separator + fileAdded.getName());

        }


    }


    /**
     * 添加文件的内容到另一个文件
     *
     * @param pathSource
     * @param pathPlugin
     * @param encodeType
     * @param strDivide
     * @param beforeOrAfter
     * @throws IOException
     */
    public static void appendFileToFile(String pathSource, String pathPlugin, String encodeType, String strDivide, boolean beforeOrAfter) throws IOException {
        File fileSource = new File(pathSource);
        if (!fileSource.exists()) return;
        File filePlugin = new File(pathPlugin);
        if (!filePlugin.exists()) return;
        FileInputStream fileInputStream = new FileInputStream(filePlugin);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, encodeType);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder sbPlugin = new StringBuilder();
        String strPlugin;
        while ((strPlugin = bufferedReader.readLine()) != null) {
            sbPlugin.append(strPlugin + "\n");
        }
        bufferedReader.close();
        inputStreamReader.close();
        fileInputStream.close();

        //??????????????????????????????????????????????????
        FileInputStream fileInputStreamSource = new FileInputStream(fileSource);
        InputStreamReader inputStreamReaderSource = new InputStreamReader(fileInputStreamSource, encodeType);
        BufferedReader bufferedReaderSource = new BufferedReader(inputStreamReaderSource);
        StringBuilder sbSource = new StringBuilder();
        String strSource;
        while ((strSource = bufferedReaderSource.readLine()) != null) {
            if (strSource.contains(strDivide)) {
                //前面添加
                if (beforeOrAfter) {
                    sbSource.append("\n" + sbPlugin.toString() + "\n");
                    sbSource.append(strSource + "\n");
                    //后面添加
                } else {
                    sbSource.append("\n" + strSource + "\n");
                    sbSource.append(sbPlugin.toString() + "\n");
                }

            } else {
                sbSource.append(strSource + "\n");

            }
        }

        bufferedReaderSource.close();
        inputStreamReaderSource.close();
        fileInputStreamSource.close();


        // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
        FileWriter writer = new FileWriter(pathSource, false);
        writer.write(sbSource.toString());
        writer.flush();
        writer.close();

    }

    /**
     * 添加string到文件
     *
     * @param pathSource
     * @param encodeType
     * @param strDivide
     * @param beforeOrAfter
     * @throws IOException
     */
    public static void appendStrToFile(String pathSource, String contentAdd, String encodeType, String strDivide, boolean beforeOrAfter) throws IOException {
        File fileSource = new File(pathSource);
        if (!fileSource.exists()) return;

        //??????????????????????????????????????????????????
        FileInputStream fileInputStreamSource = new FileInputStream(fileSource);
        InputStreamReader inputStreamReaderSource = new InputStreamReader(fileInputStreamSource, encodeType);
        BufferedReader bufferedReaderSource = new BufferedReader(inputStreamReaderSource);
        StringBuilder sbSource = new StringBuilder();
        String strSource;
        while ((strSource = bufferedReaderSource.readLine()) != null) {
            if (strSource.contains(strDivide)) {
                //前面添加
                if (beforeOrAfter) {
                    sbSource.append("\n" + contentAdd + "\n");
                    sbSource.append(strSource + "\n");
                    //后面添加
                } else {
                    sbSource.append("\n" + strSource + "\n");
                    sbSource.append(contentAdd + "\n");
                }

            } else {
                sbSource.append(strSource + "\n");

            }
        }

        bufferedReaderSource.close();
        inputStreamReaderSource.close();
        fileInputStreamSource.close();


        // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
        FileWriter writer = new FileWriter(pathSource, false);
        writer.write(sbSource.toString());
        writer.flush();
        writer.close();


    }

    /**
     * 替换字符串
     */
    public static void replaceStrInFile(String pathFile, String contentSource, String contentNew, String encodeType) throws IOException {
        File fileSource = new File(pathFile);
        if (!fileSource.exists()) return;

        //??????????????????????????????????????????????????
        FileInputStream fileInputStreamSource = new FileInputStream(fileSource);
        InputStreamReader inputStreamReaderSource = new InputStreamReader(fileInputStreamSource, encodeType);
        BufferedReader bufferedReaderSource = new BufferedReader(inputStreamReaderSource);
        StringBuilder sbSource = new StringBuilder();
        String strSource;
        while ((strSource = bufferedReaderSource.readLine()) != null) {
            if (strSource.contains(contentSource)) {

                sbSource.append(strSource.replace(contentSource, contentNew) + "\n");
            } else {
                sbSource.append(strSource + "\n");

            }
        }

        bufferedReaderSource.close();
        inputStreamReaderSource.close();
        fileInputStreamSource.close();


        // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
        FileWriter writer = new FileWriter(pathFile, false);
        writer.write(sbSource.toString());
        writer.flush();
        writer.close();


    }

    /**
     * 替换整行
     */
    public static void replaceLineInFile(String pathFile, String flag, String lineNew, String encodeType) throws IOException {
        File fileSource = new File(pathFile);
        if (!fileSource.exists()) return;

        //??????????????????????????????????????????????????
        FileInputStream fileInputStreamSource = new FileInputStream(fileSource);
        InputStreamReader inputStreamReaderSource = new InputStreamReader(fileInputStreamSource, encodeType);
        BufferedReader bufferedReaderSource = new BufferedReader(inputStreamReaderSource);
        StringBuilder sbSource = new StringBuilder();
        String strSource;
        while ((strSource = bufferedReaderSource.readLine()) != null) {
            if (strSource.contains(flag)) {

                sbSource.append(lineNew + "\n");
            } else {
                sbSource.append(strSource + "\n");

            }
        }

        bufferedReaderSource.close();
        inputStreamReaderSource.close();
        fileInputStreamSource.close();


        // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
        FileWriter writer = new FileWriter(pathFile, false);
        writer.write(sbSource.toString());
        writer.flush();
        writer.close();


    }

    /**
     * 替换整行
     */
    public static void replaceLineInFile(String pathFile, String[] flag, String[] lineNew, String encodeType) throws IOException {
        File fileSource = new File(pathFile);
        if (!fileSource.exists()) return;

        //??????????????????????????????????????????????????
        FileInputStream fileInputStreamSource = new FileInputStream(fileSource);
        InputStreamReader inputStreamReaderSource = new InputStreamReader(fileInputStreamSource, encodeType);
        BufferedReader bufferedReaderSource = new BufferedReader(inputStreamReaderSource);
        StringBuilder sbSource = new StringBuilder();
        String strSource;
        while ((strSource = bufferedReaderSource.readLine()) != null) {
            boolean continu = false;
            for (int i = 0; i < flag.length; i++) {
                if (strSource.contains(flag[i])) {
                    sbSource.append(lineNew[i] + "\n");
                    continu = true;
                    break;
                }
            }
            if (continu) continue;
            sbSource.append(strSource + "\n");


        }

        bufferedReaderSource.close();
        inputStreamReaderSource.close();
        fileInputStreamSource.close();


        // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
        FileWriter writer = new FileWriter(pathFile, false);
        writer.write(sbSource.toString());
        writer.flush();
        writer.close();


    }


}