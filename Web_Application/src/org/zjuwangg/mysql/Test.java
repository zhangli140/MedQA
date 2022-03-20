package org.zjuwangg.mysql;
import java.sql.*;

/**
 * Created by wanggang on 2015/4/23.
 */
public class Test
{
    public static void main(String[] args)
    {
        try
        {
            String url="jdbc:mysql://127.0.0.1/medical";
            String user="root";
            String pwd="";

            //加载驱动，这一句也可写为：Class.forName("com.mysql.jdbc.Driver");
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //建立到MySQL的连接
            Connection conn = DriverManager.getConnection(url,user, pwd);

            //执行SQL语句
            Statement stmt = conn.createStatement();//创建语句对象，用以执行sql语言
//            ResultSet rs = stmt.executeQuery("select * from jbk39symptom where Symptom ='咳嗽'");
            ResultSet rs = stmt.executeQuery("select * from jbk39disease where 1");


            //处理结果集
            while (rs.next())
            {
                String name = rs.getString("BriefIntroduction");
                String rd = rs.getString("RelatedSymptoms   ");
                System.out.println(name);
                System.out.println(rd);
            }
            rs.close();//关闭数据库
            conn.close();
        }
        catch (Exception ex)
        {
            System.out.println("Error : " + ex.toString());
        }
    }
}
