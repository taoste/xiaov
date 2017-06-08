package org.Reply;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;


public class Weather {
	public static String  handlerWeatherReply(String city)throws Exception{
		String url = "http://wthrcdn.etouch.cn/weather_mini?city="+URLEncoder.encode(city,"utf-8");
		String s = HttpGet(url, "");
		String ret = "";
		JSONObject j  = new JSONObject(s);
		if(j.has("data")){
			JSONObject jd = j.getJSONObject("data");
			if(jd.has("city")){
				ret = ret + jd.getString("city") + " ";
			}
			if(jd.has("wendu")){
				ret = ret + jd.getString("wendu") + "℃ ";
			}
			if(jd.has("aqi")){
				ret = ret + "空气质量指数:"+jd.getString("aqi")+"\n";
			}
			if(jd.has("ganmao")){
				ret = ret + jd.getString("ganmao")+"\n\n";
			}
			if(jd.has("forecast")){
				JSONArray ja = jd.getJSONArray("forecast");
				for(int i=0;i<ja.length();i++){
					JSONObject jdd = ja.getJSONObject(i);
					ret = ret + jdd.getString("date")+" "+jdd.getString("type")+" "+jdd.getString("low")+" "+jdd.getString("high")+"\n";
				}
			}
		}else{
			ret = "";
		}
		
		return ret;
	}
	
	
	 public static String HttpGet(String urlStr,String param){
		if(param == null || param.trim().length()<1){
	            
	        }else{
	            urlStr +="?"+param;
	        }   
		try {
			URL url = new URL(urlStr);
	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	        conn.setConnectTimeout(10000);
	        conn.setReadTimeout(12000);
	        conn.setDoOutput(true);
	        conn.setRequestMethod("GET");
	        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.75 Safari/537.36");
	        conn.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
	        conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
	        if(conn.getResponseCode() ==200){
	                InputStreamReader reader = new InputStreamReader(new GZIPInputStream(conn.getInputStream()), "utf-8");  
	                char[] data = new char[100];  
	                int readSize;  
	                StringBuffer sb = new StringBuffer();  
	                while ((readSize = reader.read(data)) > 0) {  
	                    sb.append(data, 0, readSize);  
	                }  
	                return sb.toString();  

	        }else{
	            System.out.println("failed"+conn.getResponseCode()+conn.getResponseMessage());
	        }
		} catch (Exception e) {
			
		}
		return "";
	}
	 
	 public static void main(String[] args){
		 try {
			 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	 }
}
