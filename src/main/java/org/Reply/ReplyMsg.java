package org.Reply;

import java.util.Date;
import java.util.Random;



public class ReplyMsg {
	private static String[] defaultErrorReply = new String[]{"出错了喵","爆炸了咻！","艾-啦-哎-辣-E-R-R-O-R","警告！生命值不足！生命值不足！",
			"那个～对不起的说～百百不能理解的说","哇呜，又出错了，不要打我","老娘不就是出错一次么，再来上我啊","出错了，恩，（翘起屁股"};
	private static Random rd = new Random();
	private static String[] stars = new String[]{"白羊座","金牛座","双子座","巨蟹座","狮子座","处女座","天秤座","天蝎座","射手座","摩羯座","水瓶座","双鱼座"};
	
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
			if(content.startsWith("`")||content.startsWith("·")||content.startsWith("ˋ")){
				String str = content.substring(1);
				if(str.equals("")){
					ret = "翻译成中文：`+要翻译的内容\n翻译成日文：`2+要翻译的内容\n翻译成英文：`3+要翻译的内容\n天气预报：城市名+天气\n虾扯蛋：``+对话\n";
				}else if(str.startsWith("1")){
					ret = Translate.translate(str.substring(1).trim(), "zh-CHS");
				}else if(str.startsWith("2")){
					ret = Translate.translate(str.substring(1).trim(), "ja");
				}else if(str.startsWith("3")){
					ret = Translate.translate(str.substring(1).trim(), "EN");
				}else if(str.startsWith("`")||str.startsWith("·")||str.startsWith("ˋ")){
					String cstr = str.substring(1);
					if(str.equals("`")||str.equals("·")||str.startsWith("ˋ")){
						ret = defaultErrorReply[rd.nextInt(defaultErrorReply.length)];
					}else{
						if(cstr.equals("运势")||cstr.equals("今日运势")){
							ret = TuringBot.chat(username, stars[rd.nextInt(stars.length)]+"运势");
						}else{
							ret = TuringBot.chat(username, cstr);
						}
					}
				}else{
					ret = Translate.translate(str.trim(), "zh-CHS");
				}
				if(ret.equals("")){
					ret = "纳尼～喵！";
				}
			}
		} catch (Exception e) {
			ret = defaultErrorReply[rd.nextInt(defaultErrorReply.length)];
			e.printStackTrace();
		}
		return " "+ret;
	}
	
	public static void main(String[] args){
		System.out.println(reply("``你是谁","1"));
	}
}
