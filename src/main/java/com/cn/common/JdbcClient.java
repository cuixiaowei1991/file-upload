package com.cn.common;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
/**
 * 
 * @author songzhili
 * 2016年11月1日下午2:02:02
 */
public class JdbcClient {
  
	 Connection oracle_conn = null;  
     Statement oracle_stmt = null;  
     ResultSet oracle_rs = null; 
     /****/
     private String tableName;
     /****/
     private String domainOrAppId;
     /****/
     private String randomString;
     /****/
     private String secretString;
     /****/
     private String status;
     /****/
     private String enable;
     /****/
     private String dataFile;
     /****/
     private String change;
     
     public JdbcClient(String dataFile){
    	 this.dataFile = dataFile;
    	 init();
     }
     
     private void init(){
    	 
    	 try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			String[] props = readProperties();
			if(props != null){
				oracle_conn = DriverManager.getConnection(props[0],props[1], props[2]);
				oracle_stmt = oracle_conn.createStatement();
				tableName = props[3];
				domainOrAppId = props[4];
				randomString = props[5];
				secretString = props[6];
				status = props[7];
				enable = props[8];
				change = props[9];
			}
		} catch (ClassNotFoundException e) {
			//ignore
		} catch (SQLException e) {
			//ignore
		} 
     }
     
     private String[] readProperties(){
     	
     	Properties properties = new Properties();  
        InputStream is = JdbcClient.class
         		.getClassLoader().getResourceAsStream(this.dataFile);  
         try {
 			properties.load(is);
 		} catch (IOException e) {
 			//ignore
 		}finally{
 			if(is != null){
 				try {
 					is.close();
 				} catch (IOException e) {
 					//ignore
 				}
 			}
 		}
     	return new String[]{properties.getProperty("url")
     			,properties.getProperty("username")
     			,properties.getProperty("password")
     			,properties.getProperty("tableName")
     			,properties.getProperty("domainOrAppId")
     			,properties.getProperty("randomString")
     			,properties.getProperty("secretString")
     			,properties.getProperty("status")
     			,properties.getProperty("enable")
     			,properties.getProperty("change")
     			};
     }
     /**
      * 
      * @return
      */
     public List<Map<String, String>> queryAuth(){
    	 
    	 List<Map<String, String>> list = new ArrayList<Map<String,String>>();
    	 try {
    		String sql = "select * from "+tableName;
 			oracle_rs = oracle_stmt.executeQuery(sql);
 			while(oracle_rs.next()){
 				Map<String, String> map = new HashMap<String, String>(8,0.75f);
 				map.put("domainOrAppId", oracle_rs.getString(domainOrAppId));
 				map.put("randomString", oracle_rs.getString(randomString));
 				map.put("secretString", oracle_rs.getString(secretString));
 				map.put("status", oracle_rs.getString(status));
 				map.put("change", oracle_rs.getString(change));
 				map.put("enable", oracle_rs.getString(enable));
 				list.add(map);
 			}
		} catch (SQLException e) {
			//ignore
		}
    	return list;
     }
     /**
      * 
      */
	public void close() {
		try {
			if (oracle_conn != null) {
				oracle_conn.close();
			}
			if (oracle_stmt != null) {
				oracle_stmt.close();
			}
			if (oracle_rs != null) {
				oracle_rs.close();
			}
		} catch (SQLException se) {
			// ignore
		}
	}
}
