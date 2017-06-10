package Api;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;



public class ApiUtil {
	public static String RC4(String aInput,String aKey)  { 
		try {
			byte [] key = aKey.getBytes("iso-8859-1");
			Cipher rc4 = Cipher.getInstance("RC4");
			SecretKeySpec rc4Key = new SecretKeySpec(key, "RC4");
			rc4.init(Cipher.ENCRYPT_MODE, rc4Key);
			byte [] cipherText = rc4.update(aInput.getBytes("iso-8859-1"));
			return new String(cipherText, "iso-8859-1");
		} catch (Exception e) {
			return "";
		}
	}
	
	public static HashMap<String, String[]> getParamsMap(String queryString, String enc) {  
	        HashMap<String, String[]> paramsMap = new HashMap<String,String[]>();
	        if (queryString != null && queryString.length() > 0) {     
	        	  int ampersandIndex, lastAmpersandIndex = 0;     
		          String subStr, param, value;     
		          String[] paramPair, values, newValues;     
		          do {     
		            ampersandIndex = queryString.indexOf('&', lastAmpersandIndex) + 1;     
		            if (ampersandIndex > 0) {     
		              subStr = queryString.substring(lastAmpersandIndex, ampersandIndex - 1);     
		              lastAmpersandIndex = ampersandIndex;     
		            } else {     
		              subStr = queryString.substring(lastAmpersandIndex);     
		            }     
		            paramPair = subStr.split("=");     
		            param = paramPair[0];     
		            value = paramPair.length == 1 ? "" : paramPair[1];     
		            try {     
		              value = URLDecoder.decode(value, enc);     
		            } catch (UnsupportedEncodingException ignored) {     
		            }     
		            if (paramsMap.containsKey(param)) {     
		              values = (String[])paramsMap.get(param);     
		              int len = values.length;     
		              newValues = new String[len + 1];     
		              System.arraycopy(values, 0, newValues, 0, len);     
		              newValues[len] = value;     
		            } else {     
		              newValues = new String[] { value };     
		            }     
		            paramsMap.put(param, newValues);     
		          } while (ampersandIndex > 0);     
		        }     
		        return paramsMap;     
	      }  
	
	
	static String REQ_KEY="aaabbb";
	public static String LOGIN_REQ_KEY =  "cccddd";

	public static Map<String, String[]> getQueryByEd(String ed){
		try {
			
			String b64d = new String(DatatypeConverter.parseBase64Binary(ed),"iso-8859-1");
			String decrypteddata = RC4(b64d, REQ_KEY);
			Map<String, String[]> data = getParamsMap(decrypteddata, "utf-8");
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<String, String[]>();
		}
	}
	
	public static Map<String, String[]> getQueryByLoginKey(String ed){
		try {
			String b64d = new String(DatatypeConverter.parseBase64Binary(ed),"iso-8859-1");
			String decrypteddata = RC4(b64d, LOGIN_REQ_KEY);
			Map<String, String[]> data = getParamsMap(decrypteddata, "utf-8");
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<String, String[]>();
		}
	}
	
	
	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		long length = file.length();
		if (length > Integer.MAX_VALUE) {
			is.close();
			throw new IOException("File is to large " + file.getName());
		}
		byte[] bytes = new byte[(int) length];
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
		&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}
		if (offset < bytes.length) {
			is.close();
			throw new IOException("Could not completely read file "
					+ file.getName());
		}
		is.close();
		return bytes;
	}
	
	
	 public static String MD5(String s) {
		 char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};       
		 try {
			 byte[] btInput = s.getBytes();
			 MessageDigest mdInst = MessageDigest.getInstance("MD5");
			 mdInst.update(btInput);
			 byte[] md = mdInst.digest();
			 int j = md.length;
			 char str[] = new char[j * 2];
			 int k = 0;
			 for (int i = 0; i < j; i++) {
				 byte byte0 = md[i];
				 str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				 str[k++] = hexDigits[byte0 & 0xf];
			 }
			 return new String(str);
		 } catch (Exception e) {
			 e.printStackTrace();
			 return null;
		 }
	 }
	 
	 
	 static Random random = new Random();  
	 public static String randomString(int length){  
		 String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";  
		 StringBuffer sb = new StringBuffer();  
		 for(int i = 0 ; i < length; ++i){  
			 int number = random.nextInt(62);//[0,62)  
			 sb.append(str.charAt(number));  
		 }
		 return sb.toString();  
	 }  
	 
	 public static int randomInt(int end){
		 return random.nextInt(end);
	 }
}

