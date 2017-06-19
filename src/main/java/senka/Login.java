package senka;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import lib.HttpMethod;

public class Login {
	public static void main(String[] args){
		System.out.println("start");
		try {
			step1();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void step1(){
		String url = "https://www.dmm.com/my/-/login/";
		String s = HttpGet(url, "");
		System.out.println(s);
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
