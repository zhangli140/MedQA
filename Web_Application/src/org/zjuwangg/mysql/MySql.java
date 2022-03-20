package org.zjuwangg.mysql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

/**
 * Created by wanggang on 2015/4/26.
 */
public class MySql {
	private static String driver = "com.mysql.jdbc.Driver";
    private static String uname = "root";
    private static String pwd = "";
    private static String database = "medical";
    private static String durl = "localhost";
    public  Connection con = null;
    
    static {
    	
    	Properties properties = new Properties();
    	try {
			properties.load(MySql.class.getClassLoader().getResourceAsStream("jdbc.properties"));
			driver = properties.getProperty("driver");
			uname = properties.getProperty("uname");
			pwd = properties.getProperty("pwd");
			database = properties.getProperty("database");
			durl = properties.getProperty("durl");
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }

    public  Connection getConnection(){
        try{
            if(con != null && !con.isClosed())
                return con;
            if(connect());
                return con;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    /**
     * @return Is connect correctly?true:false
     */
    public  boolean connect() {
        try {
            if (con == null || con.isClosed() || !con.isValid(0)) {
                Class.forName(driver).newInstance();
                //建立到MySQL的连接
                String url = "jdbc:mysql://" + durl + "/" + database + "?useUnicode=true&characterEncoding=utf8";
                //System.out.println(url);
                con = DriverManager.getConnection(url, uname, pwd);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Close the connection to the database
     */
    public  void closeConnection() {
        if (con == null)
            return;
        try {
            if (!con.isClosed()) {
                con.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Before call this function ,you should make sure that the connection
     * to the database has been built correctly.And call after this method
     * remember to release the connection manually!
     *
     * @param item
     * @return
     * @table table name
     */
    public  boolean insertItem(String table, Map<String, String> item) {
        try {
            Statement st = con.createStatement();
            if (item.isEmpty())
                return true;
            String sql = "INSERT INTO " + table + " ( ";
            for (String tmp : item.keySet()) {
                sql += tmp + ',';
            }
            /*Erase the last ','    */
            sql = sql.substring(0,sql.length() - 1);
            sql += ") VALUES ( ";
            for (String tmp : item.values()) {
                sql += "\'" + tmp + "\',";
            }
            /* Erase the last ',' */
            sql = sql.substring(0,sql.length() - 1);
            sql += ")";
            System.out.println(sql);
                st.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * This method is used to insert many records
     * Still remember to open and close connection manually.
     * @param table
     * @param items
     * @return  unsuccessful insert itmes
     */
    public  Vector<Map<String,String>> insertManyItmes(String table,Vector<Map<String,String>> items)
    {
        Vector<Map<String,String>> res = new Vector<Map<String,String>>();
        for(Map<String,String> e: items){
            if(!insertItem(table,e))
                res.add(e);
        }
        return res;
    }

}
