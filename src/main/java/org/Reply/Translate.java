package org.Reply;

import java.io.BufferedReader;
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
			String r = translate("发车", "zh-CHS");
			System.out.println(r);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String appKey = "5f360d88ba913e65";
	private static String appSecret = "ulOnvJEjzWosTDZL5YVxBGbKjk56mN6m";
 	
	private static Random rd = new Random();
	
	public static String translateWord(String text){
		try {
			String url = "https://api.shanbay.com/bdc/search/?word="+text;
			String s = HttpGet(url, "");
			JSONObject j = new JSONObject(s);
			System.out.println(j);
			String transed = " "+text+"\n";
			return transed+j.getJSONObject("data").getString("definition")+"";
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

	}
	
	
	
	public static String translate(String text,String tolan)throws Exception{
		if(tolan.equals("zh-CHS")&&text.indexOf(" ")<0){
			String wordTrans = translateWord(text);
			if(!wordTrans.equals("")){
				return wordTrans;
			}
		}
		String url = "https://openapi.youdao.com/api";
		int ran = 10000+rd.nextInt(88888);
		String param = 	""+
						"q="+URLEncoder.encode(text, "utf-8") +
						"&from=auto"+
						"&to="+tolan+
						"&appKey="+appKey+
						"&salt="+ran+
						"&sign="+calSign(text,ran);
		String urlStr = url+"?"+param;
		String s = HttpGet(urlStr, "");
		System.out.println(s);
		JSONObject j = new JSONObject(s);
		JSONArray translation = j.getJSONArray("translation");
		String transed = " "+text+"\n";

		if(j.has("basic")){
			JSONObject basic = j.getJSONObject("basic");
			System.out.println(basic);
			
			JSONArray explain =basic.getJSONArray("explains");
			for(int i=0;i<explain.length();i++){
				transed=transed+explain.getString(i)+"\n";
			}
		}
		transed = transed+"          ↓\n";
		for(int i=0;i<translation.length();i++){
			transed = transed + translation.getString(i)+"\n";
		}
		System.out.println(transed);
		return transed;
	}
	
	private static String calSign(String text,int ran)throws Exception{
		String strToSign = appKey+text+ran+appSecret;
		MessageDigest mdInst = MessageDigest.getInstance("MD5");
		mdInst.update(strToSign.getBytes("utf-8"));
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
	        if(conn.getResponseCode() ==200){
	        	BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
	            String line ;
	            String result ="";
	            while( (line =br.readLine()) != null ){
	                result += line + "\n";
	            }
	            return result;
	        }else{
	            System.out.println("failed"+conn.getResponseCode()+conn.getResponseMessage());
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	 
	
	
	
	
}
