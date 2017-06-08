package lib;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;



public class HttpMethod {
	
	
	 public static void Delete(String urlStr,String param) throws Exception{
	         if(param == null || param.trim().length()<1){
	             
	         }else{
	             urlStr +="?"+param;
	         }   
	         URL url = new URL(urlStr);
	         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	         conn.setConnectTimeout(10000);
	         conn.setReadTimeout(12000);
	         conn.setDoOutput(true);
	         conn.setRequestMethod("DELETE");
	         if(conn.getResponseCode() ==200){
	             System.out.println("delete success");
	         }else{
	             System.out.println("failed"+conn.getResponseCode());
	         }
     }
	 
	 public static void Put(String urlStr,String param) throws Exception{
	         URL url = new URL(urlStr);
	         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	         conn.setConnectTimeout(10000);
	         conn.setReadTimeout(12000);
	         conn.setRequestMethod("PUT");
	         conn.setDoInput(true);
	         conn.setDoOutput(true);
	         conn.setRequestProperty("Content-Type","application/json");
	         OutputStream os = conn.getOutputStream();     
	         os.write(param.toString().getBytes("utf-8"));     
	         os.close();         
	         
	         BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	         String line ;
	         String result ="";
	         while( (line =br.readLine()) != null ){
	             result += "/n"+line;
	         }
	         System.out.println(result);
	         br.close();
     }
	 
	 public static String  Post(String urlStr,String param) throws Exception{
	         URL url = new URL(urlStr);
	         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	         conn.setRequestMethod("POST");
	         conn.setConnectTimeout(10000);
	         conn.setReadTimeout(12000);
	         conn.setDoInput(true);
	         conn.setDoOutput(true);
//	         conn.setRequestProperty("Content-Type","application/json");
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
	         return result;
     }
	 
	 
	 public static String  PostJson(String urlStr,String param) throws Exception{
	         URL url = new URL(urlStr);
	         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	         conn.setRequestMethod("POST");
	         conn.setConnectTimeout(10000);
	         conn.setReadTimeout(12000);
	         conn.setDoInput(true);
	         conn.setDoOutput(true);
	         conn.setRequestProperty("Content-Type","application/json");
//	         conn.setRequestProperty("Authorization",Authorization);
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
	         return result;
     }
	 
	 public static String  PostDpns(String urlStr,String param) throws Exception{
	         URL url = new URL(urlStr);
	         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	         conn.setRequestMethod("POST");
	         conn.setConnectTimeout(10000);
	         conn.setReadTimeout(12000);
	         conn.setDoInput(true);
	         conn.setDoOutput(true);
	         conn.setRequestProperty("Content-Type","application/json");
	         conn.setRequestProperty("Authorization","key=AIzaSyA27i1V6HgHxH-mStDcpba3refqaeen9jk");
	         OutputStream os = conn.getOutputStream();     
	         os.write(param.toString().getBytes("utf-8"));     
	         os.close();         
	         if(conn.getResponseCode() ==200){
	        	 BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		         String line ;
		         String result ="";
		         while( (line =br.readLine()) != null ){
		             result += line;
		         }
		         br.close();
		         return result;
	         }else{
	        	 System.out.println(conn.getResponseCode()+","+conn.getResponseMessage());
	        	 return null;
	         }
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
	        	BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
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
			
		}
		return "";
	}
	 
	 
	 public static String HttpGet(String urlStr,String param,String[] headerkey,String[] headerv){
		if(param == null || param.trim().length()<1){
	            
	        }else{
	            urlStr +="?"+param;
	        }   
		try {
			URL url = new URL(urlStr);
		        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		        conn.setConnectTimeout(10000);
		        conn.setReadTimeout(12000);
		        if(headerkey.length==0){
		        	
		        }else{
		        	for(int i=0;i<headerkey.length;i++){
		        		conn.setRequestProperty(headerkey[i], headerv[i]);
		        	}
		        }
		        conn.setDoOutput(true);
		        conn.setRequestMethod("GET");
		        if(conn.getResponseCode() ==200){
		        	BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		            String line ;
		            String result ="";
		            while( (line =br.readLine()) != null ){
		                result += line + "\n";
		            }
		            return result;
		        }else{
		            System.out.println("failed"+conn.getResponseCode()+conn.getResponseMessage());
		            return conn.getResponseCode()+"";
		        }
		} catch (Exception e) {
			
		}
		return "";
	}
	 
}
	
	 
	 
	 
	 
	 
	 
	 
	 
	