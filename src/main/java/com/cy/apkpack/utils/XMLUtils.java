package com.cy.apkpack.utils;

import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XMLUtils {
    /**
     * 根据属性名称抽取添加的xml,写入插件
     *
     * @param pathSource
     * @param pathAdded
     * @param pathSeparate
     * @param compareAttribute
     * @throws DocumentException
     * @throws IOException
     */
    public static void comparAddedXML(String pathSource, String pathAdded, String pathSeparate, String compareAttribute) throws DocumentException, IOException {
        File file = new File(pathSource);//根据指定的路径创建file对象
        File fileAdded = new File(pathAdded);//根据指定的路径创建file对象
        if (!file.exists() || !fileAdded.exists()) return;

        SAXReader saxReader = new SAXReader();//创建一个SAXReader对象
        Element root = saxReader.read(file).getRootElement();//获取根节点
        List<Element> listElement = root.elements();
        List<String> list = new ArrayList<>();
        for (Element element : listElement) {
            list.add(element.attributeValue(compareAttribute));
        }

        root = saxReader.read(fileAdded).getRootElement();
        List<Element> listElementAdded = root.elements();
        File filePlugin = null;
        FileWriter fileWriter = null;

        StringBuilder stringBuilder = new StringBuilder();

        for (Element element : listElementAdded) {
            if (!list.contains(element.attributeValue(compareAttribute))) {
                if (fileWriter == null) {
                    filePlugin = FileUtils.createFile(pathSeparate);//要创建文件
                    fileWriter = new FileWriter(filePlugin);
                    if (FileUtils.getFileName(pathAdded).equals("public.xml")) stringBuilder.append("<resources>\n");
                }
                appendXmlToSB(element, stringBuilder);
            }
        }

        if (fileWriter == null) return;
        if (FileUtils.getFileName(pathAdded).equals("public.xml")) stringBuilder.append("</resources>");

        fileWriter.write(stringBuilder.toString());

        fileWriter.flush();

        fileWriter.close();

    }

    /**
     * 读取xml所有属性和子集的属性，添加到sb
     *
     * @param element
     * @param stringBuilder
     * @throws IOException
     */
    public static void appendXmlToSB(Element element, StringBuilder stringBuilder) throws IOException {
        List<Attribute> listAttribut = element.attributes();
        stringBuilder.append("\n<" + element.getName());
        for (Attribute attribute : listAttribut) {
            stringBuilder.append(" " + attribute.getName() + "=\"" + attribute.getValue() + "\"");
        }
        stringBuilder.append(">");
        stringBuilder.append(element.getTextTrim());
        for (Element element1 : element.elements()) {
            appendXmlToSB(element1, stringBuilder);
        }
        stringBuilder.append("</" + element.getName() + ">");
    }
}
