package org.Reply;

import java.util.Date;


public class ReplyMsg {
	@SuppressWarnings("deprecation")
	public static String reply(String content,String username){
		String ret="";
		try {
			if(content.contains("几点")){
				ret = "现在时间是:" +new Date().toLocaleString();
			}
			int weatherindex;
			if((weatherindex=content.indexOf("天气"))>=0){
				String city="北京";
				if(content.equals("天气")){
					city="北京";
				}else if(content.startsWith("明天天气")){
					city="北京";
				}else{
					if(weatherindex>0){
						String fcity = content.substring(0,weatherindex);
						 if(fcity.length()<2&&fcity.length()>4){
							return "";
						 }else{
							 city = fcity;
						 }
					}
				}
				ret = Weather.handlerWeatherReply(city);
			}
		} catch (Exception e) {
			ret = "出错了喵";
			e.printStackTrace();
		}

		
		
		return ret;
	}
	
	public static void main(String[] args){
		System.out.println(reply("天津天气",""));
	}
}
