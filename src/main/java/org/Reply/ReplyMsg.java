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
			if(content.startsWith("`")){
				String str = content.substring(1);
				if(str.equals("")){
					ret = "翻译成中文：`1+要翻译的内容\n翻译成日文：`2+要翻译的内容\n翻译成英文：`3+要翻译的内容\n翻译成中文：`+要翻译的内容\n";
				}else if(str.startsWith("1")){
					ret = Translate.translate(str.substring(1), "zh");
				}else if(str.startsWith("2")){
					ret = Translate.translate(str.substring(1), "ja");
				}else if(str.startsWith("3")){
					ret = Translate.translate(str.substring(1), "en");
				}else{
					ret = Translate.translate(str, "zh");
				}
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
