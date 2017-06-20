package senka;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import freemarker.core.ReturnInstruction.Return;
import lib.HttpMethod;

public class Login {
	public static void main(String[] args){
		System.out.println("start");
		try {
			String userid="1127805853@qq.com";
			String pwd="9876543210";
		        String url = "http://ooi.moe/";
		        String param = "login_id="+userid+"&password="+pwd+"&mode=1";
		        String cookie = getOoiCookie(url,param);
			System.out.println(cookie);
			String url2= "http://ooi.moe/kancolle";
		        String s = HttpGet2(url2,param,cookie);
			String str = "api_token=";
			int n = s.indexOf(str);
			String s1 = s.substring(n+str.length());
			int n2 = s1.indexOf("&");
			String token = s1.substring(0,n2);
			System.out.println(token);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String login(String userid,String pwd){
		try {
		        String url = "http://ooi.moe/";
		        String param = "login_id="+userid+"&password="+pwd+"&mode=1";
		        String cookie = getOoiCookie(url,param);
			System.out.println(cookie);
			String url2= "http://ooi.moe/kancolle";
		        String s = HttpGet2(url2,param,cookie);
			String str = "api_token=";
			int n = s.indexOf(str);
			String s1 = s.substring(n+str.length());
			int n2 = s1.indexOf("&");
			String token = s1.substring(0,n2);
			System.out.println(token);
			return token;
		} catch (Exception e) {
			return "";
		}

	}
	

	

	 
	 public static String HttpGet2(String urlStr,String param,String cookie){
		if(param == null || param.trim().length()<1){
	            
	        }else{
	            urlStr +="?"+param;
	        }   
		try {
			URL url = new URL(urlStr);
	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	        conn.setConnectTimeout(10000);
	        conn.setReadTimeout(12000);
	        conn.setRequestProperty("Cookie", cookie);
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
	        	System.out.println(conn);
	            System.out.println("failed"+conn.getResponseCode()+conn.getResponseMessage());
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	 
	 
	 
	 public static String  getOoiCookie(String urlStr,String param) throws Exception{
	         URL url = new URL(urlStr);
	         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	         conn.setRequestMethod("POST");
	         conn.setConnectTimeout(10000);
	         conn.setReadTimeout(12000);
	         conn.setInstanceFollowRedirects(false);
	         conn.setDoInput(true);
	         conn.setDoOutput(true);
	         OutputStream os = conn.getOutputStream();     
	         os.write(param.toString().getBytes("utf-8"));     
	         os.close();         
	         BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	         String line ;
	         String result ="";
	         while( (line =br.readLine()) != null ){
	             result += line;
	         }
	         br.close();
	         return conn.getHeaderField("Set-Cookie");
	 }
	 
	    
}
