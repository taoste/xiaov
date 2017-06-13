package senka;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class Rank {

	public static void main(String[] args) {
		System.out.println("start");
		try {
			getRank();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("finish");
	}
	private static int[] MAGIC_R_NUMS = new int[]{ 8931, 1201, 1156, 5061, 4569, 4732, 3779, 4568, 5695, 4619, 4912, 5669, 6586 };
	private static int magic=0;
	private static ArrayList<Integer> larray = new ArrayList<>();
	private static ArrayList<JSONObject> tmpdata = new ArrayList<>();
	public static void getRank()throws Exception{
		String path = "/kcsapi/api_req_ranking/mxltvkpyuklh";
		int page=1;
		String token = "4ae20460456d03f790d7bf2a1576a13036654ae2";
		magic=0;
		tmpdata=new ArrayList<>();
		larray = new ArrayList<>();
		int server = 8;
		
		String ranking = "2693827598888527872694683859309";
		String param = "api%5Fpageno="+page+"&api%5Fverno=1&api%5Franking="+ranking+"&api%5Ftoken="+token;
		System.out.println(param);
		String ret = Lib.ApiPost(path, param, token, server);
		System.out.println(ret);
		if(ret.startsWith("svdata=")){
			JSONObject j = new JSONObject(ret.substring(7));
			addData(j);
		}
		calMagic();
		parseData();
		calsenka(server);
	}
	


	public static void addData(JSONObject j)throws Exception{
		JSONArray list = j.getJSONObject("api_data").getJSONArray("api_list");
		for(int i=0;i<list.length();i++){
			JSONObject jd = list.getJSONObject(i);
			int no=jd.getInt("api_mxltvkpyuklh");
			long key=jd.getLong("api_wuhnhojjxmke");
			if(key%MAGIC_R_NUMS[no%13]==0){
				int lrate = (int)(key / MAGIC_R_NUMS[no%13]);
				if(magic==0&&larray.size()<8){
					larray.add(lrate);
					tmpdata.add(jd);
				}else{
					tmpdata.add(jd);
				}
			}
		}
	}
	
	public static void parseData()throws Exception{
		for(int k=0;k<tmpdata.size();k++){
			JSONObject jd = tmpdata.get(k);
			int no=jd.getInt("api_mxltvkpyuklh");
			long key=jd.getLong("api_wuhnhojjxmke");

			if(key%MAGIC_R_NUMS[no%13]==0){
				int lrate = (int)(key / MAGIC_R_NUMS[no%13]);
				int senka = lrate/magic - 91;
				String name = jd.getString("api_mtjmdcwtvhdr");
				String cmt = jd.getString("api_itbrdpdbkynm");
				System.out.println(name);
				System.out.println(cmt);
				System.out.println(senka);
			}
		}
	}
	
	public static void calMagic(){
		if(larray.size()==8){
			magic = EA(larray);
		}else{
			System.out.println(larray);
			System.out.println("error");
		}
	}
	
	public static void calsenka(int server)throws Exception{
		ArrayList<JSONObject> idlist = new ArrayList<>();
		DBCollection cl_senka = Util.db.getCollection("cl_senka_"+server);
		DBCollection cl_tmp_senka = Util.db.getCollection("cl_tmp_senka_"+server);
		ArrayList<Integer> senkaRank = new ArrayList<>();
		for(int i=0;i<tmpdata.size();i++){
			JSONObject jdd = tmpdata.get(i);
			int no=jdd.getInt("api_mxltvkpyuklh");
			long key=jdd.getLong("api_wuhnhojjxmke");
			int lrate = (int)(key / MAGIC_R_NUMS[no%13]);
			int senka = lrate/magic - 91;
			System.out.println(no);
			System.out.println(senkaRank.size());
			if(no==senkaRank.size()+1){
				senkaRank.add(senka);
			}
			String name = jdd.getString("api_mtjmdcwtvhdr");
			String cmt = jdd.getString("api_itbrdpdbkynm");
			DBCursor dbc = null;
			try {
				dbc = cl_senka.find(new BasicDBObject("name",name));
				ArrayList<DBObject> userlist = new ArrayList<>();
				while (dbc.hasNext()) {
					DBObject userdata = dbc.next();
					userlist.add(userdata);
				}
				if(userlist.size()>1){
					for(int k=0;k<userlist.size();k++){
						DBObject ud = userlist.get(i);
						String info = ud.get("info").toString();
						JSONObject infoj = new JSONObject(info);
						String cmti = infoj.getString("api_cmt");
						if(cmti.equals(cmt)){
							JSONObject jd = new JSONObject();
							jd.put("id", ud.get("_id"));
							jd.put("senka", senka);
							idlist.add(jd);
						}
					}
				}else if(userlist.size()==1){
					JSONObject jd = new JSONObject();
					jd.put("id", userlist.get(0).get("_id"));
					jd.put("senka", senka);
					idlist.add(jd);
				}else{
					//TODO need fetch user data
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if(dbc!=null){
					dbc.close();
				}
			}
		}
		System.out.println(idlist);
		BasicDBList dbl = new BasicDBList();
		Date now = new Date();
		for(int i=0;i<idlist.size();i++){
			JSONObject jrd = idlist.get(i);
			int id = jrd.getInt("id");
			int senka = jrd.getInt("senka");
			BasicDBObject update = new BasicDBObject();
			update.append("$push", new BasicDBObject("senka",new BasicDBObject("senka",senka).append("ts", now)));
			cl_senka.update(new BasicDBObject("_id", id),update,true,false);
			dbl.add(id);
		}
		cl_tmp_senka.save(new BasicDBObject("_id",now).append("d", dbl).append("rank", senkaRank));
		
		
	}
	
	
	public static int EA(int max,int min){
		if(max%min==0){
			return min;
		}else{
			return EA(min, max%min);
		}
	}
	
	public static int EA(ArrayList<Integer> list){
		if(list.size()==0){
			return 1;
		}
		int ret = list.get(0);
		for(int i=1;i<list.size();i++){
			ret = EA(ret,list.get(i));
		}
		return ret;
	}
	
	 public static String  ApiPost(String path,String param,String token,int server) throws Exception{
		 String urlStr ="http://ooi.moe"+path;
		 System.out.println(urlStr);
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
	         conn.setRequestProperty("Referer","http://"+"ooi.moe"+"/kcs/mainD2.swf?api_token="+token+"&api_starttime="+new Date().getTime()+"/[[DYNAMIC]]/1");
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
