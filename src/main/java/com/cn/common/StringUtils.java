package com.cn.common;
/**
 * 
 * @author songzhili
 * 2016年11月10日上午9:02:53
 */
public class StringUtils {
    
	public static boolean isEmpty(String source){
		
		if(source == null || source.trim().length() == 0){
			return true;
		}
		return false;
	}
}
