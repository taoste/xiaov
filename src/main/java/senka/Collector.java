package senka;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

public class Collector {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(123123);
		try {
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("finish");
	}
	
	public static void runCollector(Map<String, String[]> data)throws Exception{
		final String type = data.get("type")[0];
		final String token = data.get("token")[0];
		final String server = data.get("server")[0];
		if(type.equals("init")){
			final int from = Integer.valueOf(data.get("from")[0]); 
			final int to = Integer.valueOf(data.get("to")[0]); 
			final int thread = Integer.valueOf(data.get("thread")[0]); 
			if(from<700000||to-from>500000||thread>8){
				return;
			}else{
				for(int i=0;i<thread;i++){
					final int begin = i*(to-from)/thread;
					final int end = (i+1)*(to-from)/thread-1;
					new Thread(new Runnable() {
						public void run() {
							int f = 0;
							for(int j=begin;j<end;j++){
								String url = "http://203.104.248.135/kcsapi/api_req_member/get_practice_enemyinfo";
								int id=j;
								String param = "api%5Ftoken="+token+"&api%5Fmember%5Fid="+id+"&api%5Fverno=1";
								try {
									String r = Post(url, param);
									if(r.startsWith("svdata="));
									JSONObject jd = new JSONObject(r.substring(7));
									Thread.sleep(100);
									if(j%100==0){
										System.out.println(j);
									}
									int ret = save(jd,server);
									if(ret == 1){
										f++;
										System.out.print("failed get info:"+j);
										System.out.print(jd);
									}else{
										f=0;
									}
									if(f>5){
										break;
									}
									Thread.sleep(100);
								} catch (Exception e) {
									e.printStackTrace();
								}
								
							}							
						}
					}).start();
				}
			}
		}

	}
	
	public static int save(JSONObject j,String server)throws Exception{
		String result = j.getString("api_result_msg");
		if(result.equals("成功")){
			Date now = new Date();
			JSONObject data = j.getJSONObject("api_data");
			String name = data.getString("api_nickname");
			JSONArray expa = data.getJSONArray("api_experience");
			int exp = expa.getInt(0);
			int id = data.getInt("api_member_id");
			BasicDBObject update = new BasicDBObject();
			BasicDBObject expdata = new BasicDBObject();
			expdata.append("d", exp);
			expdata.append("ts",now);
			int lv = data.getInt("api_level");
			update.append("$set", new BasicDBObject("name", name).append("e", exp).append("lv", lv).append("info",data.toString()));
			update.append("$push", new BasicDBObject("exp",expdata));
			BasicDBObject query = new BasicDBObject();
			query.append("_id", id);
			DBCollection cl_senka=Util.db.getCollection("cl_senka_"+server);
			//System.out.println(update);
			cl_senka.update(query, update,true,false);
			return 0;
		}else{
			return 1;
		}
	}
	
	
	
	 public static String  Post(String urlStr,String param) throws Exception{
	         URL url = new URL(urlStr);
	         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	         conn.setRequestMethod("POST");
	         conn.setConnectTimeout(10000);
	         conn.setReadTimeout(12000);
	         conn.setDoInput(true);
	         conn.setDoOutput(true);
	         conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
	         conn.setRequestProperty("X-Powered-By","PHP/5.3.3");
	         conn.setRequestProperty("Accept-Language","zh-CN,zh;q=0.8");
	         conn.setRequestProperty("Referer","http://203.104.248.135/kcs/mainD2.swf?api_token=35c234835c146e3c8aa30700d10ee732b6051117&api_starttime=1497062500846/[[DYNAMIC]]/1");
	         conn.setRequestProperty("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.75 Safari/537.36");
	         conn.setRequestProperty("X-Requested-With","ShockwaveFlash/25.0.0.171");
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

}
