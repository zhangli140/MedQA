package org.zjuwangg.tools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wanggang on 2015/4/27.
 */
public class FileOperator {
    public static boolean createFile(String filePath) {
        boolean result = false;
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                result = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static boolean createDirectory(String directory) {
        boolean result = false;
        File file = new File(directory);
        if (!file.exists()) {
            result = file.mkdirs();
        }
        return result;
    }

    public static boolean deleteFile(String filePath) {
        boolean result = false;
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            result = file.delete();
        }

        return result;
    }

    public static void deleteDirectory(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File myfile : files) {
                deleteDirectory(filePath + "/" + myfile.getName());
            }

            file.delete();
        }
    }


    public static String readFileByBytes(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        StringBuffer content = new StringBuffer();

        try {
            byte[] temp = new byte[1024];
            FileInputStream fileInputStream = new FileInputStream(file);
            while (fileInputStream.read(temp) != -1) {
                content.append(new String(temp));
                temp = new byte[1024];
            }
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public static String readFileByChars(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        StringBuffer content = new StringBuffer();
        try {
            char[] temp = new char[1024];
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "GBK");
            while (inputStreamReader.read(temp) != -1) {
                content.append(new String(temp));
                temp = new char[1024];
            }

            fileInputStream.close();
            inputStreamReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public static List<String> readFileByLines(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        List<String> content = new ArrayList<String>();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String lineContent = "";
            while ((lineContent = reader.readLine()) != null) {
                content.add(lineContent);
//                System.out.println(lineContent);
            }
            fileInputStream.close();
            inputStreamReader.close();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
    public static void appendFileByNextLine(String filePath,String content) throws IOException{
        File file = new File(filePath);
        synchronized (file){
            FileWriter fos = new FileWriter(file,true);
            fos.write(content + "\n");
            fos.close();
        }
    }
    public static void writeFileByFileOutputStream(String filePath, String content) throws IOException {
        File file = new File(filePath);
        synchronized (file) {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(content.getBytes("UTF-8"));
            fos.close();
        }
    }

    public static void writeFileByBufferedOutputStream(String filePath, String content) throws IOException {
        File file = new File(filePath);
        synchronized (file) {
            BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(filePath));
            fos.write(content.getBytes("UTF-8"));
            fos.flush();
            fos.close();
        }
    }

    public static void writeFileByFileWriter(String filePath, String content) throws IOException {
        File file = new File(filePath);
        synchronized (file) {
            FileWriter fw = new FileWriter(filePath);
            fw.write(content);
            fw.close();
        }
    }

}
