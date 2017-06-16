package senka;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class Collector {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(123123);
		long t1 = new Date().getTime();
		try {
			collectByLastSenka("8c3f8fa5533a18f92ac54c65022491eb2900125e", 8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		long t2 = new Date().getTime();
		System.out.println("finish:"+(t2-t1));
	}
	
	public static void test()throws Exception{
		String path = "/kcsapi/api_req_member/get_practice_enemyinfo";
		String id = "8156938";
		String token = "8c3f8fa5533a18f92ac54c65022491eb2900125e";
		String param = "api%5Ftoken="+token+"&api%5Fmember%5Fid="+id+"&api%5Fverno=1";
		String s = Lib.ApiPost(path, param, token,8);
		System.out.println(s);
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
					final int begin = from+i*(to-from)/thread;
					final int end = from+(i+1)*(to-from)/thread;
					new Thread(new Runnable() {
						public void run() {
							int f = 0;
							for(int j=begin;j<end;j++){
								String path = "/kcsapi/api_req_member/get_practice_enemyinfo";
								int id=j;
								String param = "api%5Ftoken="+token+"&api%5Fmember%5Fid="+id+"&api%5Fverno=1";
								try {
									String r = Lib.ApiPost(path, param, token, Integer.valueOf(server));
									if(r.startsWith("svdata="));
									JSONObject jd = new JSONObject(r.substring(7));
									if(j%100==0){
										System.out.println(j);
									}
									int ret = save(jd,server);
									if(ret == 1){
										f++;
										System.out.println(param);
										System.out.print("\nfailed get info:"+j+"\n");
										System.out.print(jd);
									}else{
										f=0;
									}
									if(f>5){
										break;
									}
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
	
	public static JSONObject collectById(int id,String token,int server)throws Exception{
		String path = "/kcsapi/api_req_member/get_practice_enemyinfo";
		String param = "api%5Ftoken="+token+"&api%5Fmember%5Fid="+id+"&api%5Fverno=1";
		try {
			String r = Lib.ApiPost(path, param, token, server);
			if(r.startsWith("svdata="));
			JSONObject jd = new JSONObject(r.substring(7));
			int ret = save(jd,server+"");
			if(ret == 1){
				System.out.println(param);
				System.out.print("\nfailed get info:"+id+"\n");
				System.out.print(jd);
				return null;
			}else{
				return jd.getJSONObject("api_data");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void collectByLastSenka(String token,int server){
		DBCollection cl_n_senka = Util.db.getCollection("cl_n_senka_"+server);
		DBCursor dbc = null;
		Date now = new Date();
		int rankNo = Util.getRankDateNo(now);
		try {
			dbc = cl_n_senka.find(new BasicDBObject("ts",new BasicDBObject("$gt",new Date(now.getTime()-60000000L)))); //1000 min
			int all=0;
			while (dbc.hasNext()) {
				all++;
				DBObject dbObject = (DBObject) dbc.next();
				String name = dbObject.get("_id").toString();
				String key = "d"+now.getMonth();
				BasicDBList dbl = (BasicDBList)dbObject.get(key);
				ArrayList<DBObject> matched = new ArrayList<>();
				for(int i=dbl.size();i>0;i--){
					DBObject senkainfo = (DBObject)dbl.get(i-1);
					int thenDateNo = Integer.valueOf(senkainfo.get("ts").toString());
					if(thenDateNo==rankNo){
						matched.add(senkainfo);
					}else{
						break;
					}
				}
				if(matched.size()==0){
					
				}else if(matched.size()==1){
					DBObject nameData = matched.get(0);
					Object tido = nameData.get("id");
					int tid=0;
					if(tido!=null){
						tid = Integer.valueOf(tido.toString());
					}
					NameHandler.handleName(name,matched.get(0),tid,1,server,token);
				}else if(matched.size()>1){
					System.out.println(matched);
					System.out.println(name);

				}
			}
			System.out.println("all:"+all);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(dbc!=null){
				dbc.close();
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
			update.append("$set", new BasicDBObject("name", name).append("e", exp).append("lv", lv).append("info",data.toString()).append("ts", now));
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
	
	
	
	 public static String  Post(String urlStr,String param,String token) throws Exception{
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
	         conn.setRequestProperty("Referer","http://203.104.248.135/kcs/mainD2.swf?api_token="+token+"&api_starttime="+new Date().getTime()+"/[[DYNAMIC]]/1");
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
