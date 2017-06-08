package org.b3log.xiaov.service;

import java.util.Date;

public class Reply {
	public static String reply(String content,String username){
		System.out.println(content);
		String ret="";
		if(content.contains("几点了")){
			ret = new Date().toLocaleString();
		}
		
		
		return ret;
	}
}
