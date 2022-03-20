package org.zjuwangg.nlp;

import java.io.UnsupportedEncodingException;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class NlpirProcess {

    public static int CHARSET = 1;// "UTF_8";
    // 定义接口CLibrary，继承自com.sun.jna.Library
    public static interface CLibrary extends Library {
        // 定义并初始化接口的静态变量
        CLibrary Instance = (CLibrary) Native.loadLibrary(
                "NLPIR", CLibrary.class);
        //CLibrary Instance = (CLibrary)Native.loadLibrary(System.getProperty("user.dir")+"\\lib\\win32\\NLPIR", CLibrary.class);

        public int NLPIR_Init(String sDataPath, int encoding,
                              String sLicenceCode);

        public String NLPIR_ParagraphProcess(String sSrc, int bPOSTagged);

        public String NLPIR_GetKeyWords(String sLine, int nMaxKeyLimit,
                                        boolean bWeightOut);
        public String NLPIR_GetFileKeyWords(String sLine, int nMaxKeyLimit,
                                            boolean bWeightOut);
        public int NLPIR_AddUserWord(String sWord);//add by qp 2008.11.10
        public int NLPIR_DelUsrWord(String sWord);//add by qp 2008.11.10
        public String NLPIR_GetLastErrorMsg();
        public void NLPIR_Exit();
        public int NLPIR_ImportUserDict(String fileName,boolean isOverwrite);
        public int NLPIR_SaveTheUsrDic();
    }

    /**
     * 字符编码转换
     * @param aidString     待转换字符串
     * @param ori_encoding  原始字符编码集
     * @param new_encoding  新的字符编码集
     * @return  转化后以新的码制编码的字符串
     */
    public static String transString(String aidString, String ori_encoding,
                                     String new_encoding) {
        try {
            return new String(aidString.getBytes(ori_encoding), new_encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void init()throws Exception{
    	String classpath = NlpirProcess.class.getClassLoader().getResource("").getPath();
    	if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1 
    			&& classpath.startsWith("/")) { // Windows下获取到的路径以“/”开头，下面初始化的时候会报错
    		classpath = classpath.substring(1);
    	}
        int init_flag = CLibrary.Instance.NLPIR_Init(classpath, CHARSET, "0");
        String nativeBytes = null;

        if (0 == init_flag) {
            nativeBytes = CLibrary.Instance.NLPIR_GetLastErrorMsg();
            System.err.println("初始化失败！fail reason is "+nativeBytes);
            throw new Exception("初始化失败！fail reason is "+nativeBytes);
        }
    }
    public static String spilt(String source){
        return CLibrary.Instance.NLPIR_ParagraphProcess(source,1);
    }
    public static void exit(){
        CLibrary.Instance.NLPIR_Exit();
    }

    public static void main(String[] args) throws Exception {
//        String system_charset = "GBK";//GBK----0
//        int charset_type = 1;

        String sInput = "我婆婆有高血压，现在吃兰迪降压药，吃多了会有副作用吗？";
        String nativeBytes = null;
        try {
            init();
            nativeBytes = spilt(sInput);
            System.out.println("分词结果为： " + nativeBytes);
            exit();
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }

    }
}
