package org.Reply;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.zip.GZIPInputStream;

import javax.xml.bind.DatatypeConverter;

import org.json.JSONArray;
import org.json.JSONObject;

import lib.HttpMethod;

public class Translate {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("start");
		try {
			translate("hello", "zh");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String appKey = "5f360d88ba913e65";
	private static String appSecret = "ulOnvJEjzWosTDZL5YVxBGbKjk56mN6m";
 	
	public static String translate(String text,String tolan)throws Exception{
		String url = "http://openapi.youdao.com/api";
		String param = 	""+
						"q="+URLEncoder.encode(text, "utf-8") +
						"&from=auto"+
						"&to="+tolan+
						"&appKey="+appKey+
						"&salt="+123+
						"&sign="+calSign(text);
		String s = HttpGet(url, param);
		JSONObject j = new JSONObject(s);
		JSONArray translation = j.getJSONArray("translation");
		String transed = text+"\n";

		if(j.has("basic")){
			JSONObject basic = j.getJSONObject("basic");
			System.out.println(basic);
			
			JSONArray explain =basic.getJSONArray("explains");
			for(int i=0;i<explain.length();i++){
				transed=transed+explain.getString(i)+"\n";
			}
			transed=transed+"\n";
		}
		transed = transed+"翻译：\n";
		for(int i=0;i<translation.length();i++){
			transed = transed + translation.getString(i)+"\n";
		}
		
		return transed;
	}
	
	private static String calSign(String text)throws Exception{
		String strToSign = appKey+URLEncoder.encode(text, "utf-8")+123+appSecret;
		MessageDigest mdInst = MessageDigest.getInstance("MD5");
		mdInst.update(strToSign.getBytes());
		String sign = DatatypeConverter.printHexBinary(mdInst.digest()).toLowerCase();
		return sign;
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
	
	
	
	
}
