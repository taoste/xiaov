package org.Reply;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import javax.xml.bind.DatatypeConverter;

import org.json.JSONArray;
import org.json.JSONObject;


public class Translate {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("start");
		try {
			translate("芙兰是笨蛋", "ja");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String appKey = "5f360d88ba913e65";
	private static String appSecret = "ulOnvJEjzWosTDZL5YVxBGbKjk56mN6m";
 	
	private static Random rd = new Random();
	
	public static String translate(String text,String tolan)throws Exception{
		String url = "http://openapi.youdao.com/api";
		int ran = 10000+rd.nextInt(88888);
		String param = 	""+
						"q="+URLEncoder.encode(text, "utf-8") +
						"&from=auto"+
						"&to="+tolan+
						"&appKey="+appKey+
						"&salt="+ran+
						"&sign="+calSign(text,ran);
		String s = HttpGet(url, param);
		System.out.println(param);

		System.out.println(s);
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
		System.out.println(transed);
		return transed;
	}
	
	private static String calSign(String text,int ran)throws Exception{
		String strToSign = appKey+text+ran+appSecret;
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
	        conn.setConnectTimeout(2000);
	        conn.setReadTimeout(3000);
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
