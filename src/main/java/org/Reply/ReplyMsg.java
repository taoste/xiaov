package org.Reply;

import java.util.Date;
import java.util.Random;


public class ReplyMsg {
	private static String[] defaultErrorReply = new String[]{"出错了喵～","爆炸了咻！","艾-啦-哎-辣-E-R-R-O-R","警告！生命值不足！生命值不足！"};
	private static Random rd = new Random();
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
					ret = Translate.translate(str.substring(1).trim(), "zh-CHS");
				}else if(str.startsWith("2")){
					ret = Translate.translate(str.substring(1).trim(), "ja");
				}else if(str.startsWith("3")){
					ret = Translate.translate(str.substring(1).trim(), "EN");
				}else{
					ret = Translate.translate(str.trim(), "zh-CHS");
				}
			}
			
		} catch (Exception e) {
			ret = defaultErrorReply[rd.nextInt(defaultErrorReply.length)];
			e.printStackTrace();
		}
		return ret;
	}
	
	public static void main(String[] args){
		System.out.println(reply("天津天气",""));
	}
}
