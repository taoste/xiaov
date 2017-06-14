package senka;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Random;

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
			generateRankKey(8156938);
			getRank();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("finish");
	}
	private static int[] MAGIC_R_NUMS = new int[]{ 8931, 1201, 1156, 5061, 4569, 4732, 3779, 4568, 5695, 4619, 4912, 5669, 6586 };
	private static int magic=0;
	private static Random rd = new Random();
	private static ArrayList<Integer> larray = new ArrayList<>();
	private static ArrayList<JSONObject> tmpdata = new ArrayList<>();
	public static void getRank()throws Exception{
		String path = "/kcsapi/api_req_ranking/mxltvkpyuklh";
		int page=1;
		String token = "76c73d1d3b775575e1ffc3116aec113835e8cd37";
		magic=0;
		tmpdata=new ArrayList<>();
		larray = new ArrayList<>();
		int server = 8;
		int userid = 8156938;
		String ranking = generateRankKey(userid);
		
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
	
	
//	String token = "76c73d1d3b775575e1ffc3116aec113835e8cd37";
//	int server = 8;
//	int userid = 8156938;
	
	private static int calMagicNum = 28;
	
	public static void runRank(Map<String, String[]> data)throws Exception{
		String token = data.get("token")[0];
		int server = Integer.valueOf(data.get("server")[0]);
		int userid = Integer.valueOf(data.get("uid")[0]);
		runRankTask(token, server, userid);
	}
	
	public static void runRankTask(final String token,final int server,final int userid)throws Exception{
		magic=0;
		tmpdata=new ArrayList<>();
		larray = new ArrayList<>();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String path = "/kcsapi/api_req_ranking/mxltvkpyuklh";
					int page=1;
					String ranking = generateRankKey(userid);
					for(int i=1;i<100;i++){
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
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});

	}
	


	public static void addData(JSONObject j)throws Exception{
		JSONArray list = j.getJSONObject("api_data").getJSONArray("api_list");
		for(int i=0;i<list.length();i++){
			JSONObject jd = list.getJSONObject(i);
			int no=jd.getInt("api_mxltvkpyuklh");
			long key=jd.getLong("api_wuhnhojjxmke");
			if(key%MAGIC_R_NUMS[no%13]==0){
				int lrate = (int)(key / MAGIC_R_NUMS[no%13]);
				if(magic==0&&larray.size()<calMagicNum){
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
//				System.out.println(name);
//				System.out.println(cmt);
//				System.out.println(senka);
			}
		}
	}
	
	public static void calMagic(){
		if(larray.size()==calMagicNum){
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
					needUpdateUserDate(userdata,cl_senka);
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
							jd.put("name",name);
							idlist.add(jd);
						}
					}
				}else if(userlist.size()==1){
					JSONObject jd = new JSONObject();
					jd.put("id", userlist.get(0).get("_id"));
					jd.put("senka", senka);
					jd.put("name",name);
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
			dbl.add(new BasicDBObject("id",id).append("senka", senka).append("name", jrd.get("name")));
		}
		cl_tmp_senka.save(new BasicDBObject("_id",now).append("d", dbl));
	}
	
	public static void needUpdateUserDate(DBObject user,DBCollection cl_senka){
		Object us = user.get("senka");
		Object ud = user.get("exp");
		boolean needupdate = false;
		Date now = new Date();
		BasicDBObject update = new BasicDBObject();
		if(us!=null){
			BasicDBList dbls = (BasicDBList) us;
			for(int i=0;i<dbls.size();i++){
				DBObject senkad = (DBObject)dbls.get(i);
				Date then = (Date)senkad.get("ts");
				if(now.getTime()-then.getTime()>86400000L*60){
					dbls.remove(i);
					needupdate = true;
				}else{
					break;
				}
			}
			if(needupdate){
				update.append("$set", new BasicDBObject("senka",dbls));
			}
		}
		boolean needupdate2 = false;
		if(ud!=null){
			BasicDBList dbld = (BasicDBList) ud;
			for(int i=0;i<dbld.size();i++){
				DBObject senkad = (DBObject)dbld.get(i);
				Date then = (Date)senkad.get("ts");
				if(now.getTime()-then.getTime()>86400000L*60){
					dbld.remove(i);
					needupdate2 = true;
				}else{
					break;
				}
			}
			if(needupdate2){
				update.append("$set", new BasicDBObject("exp",dbld));
			}
		}
		if(needupdate||needupdate2){
			cl_senka.update(user, update);
		}
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
	
	private static long[] Il = new long[]{9137, 9740, 4458, 2139, 2130, 4132653, 1033183, 3749, 3601, 4294, 13, 6523, 3791, 10, 5424, 7481, 1000, 1875979};
	private static int[] I1 = new int[]{3, 7, 8, 0, 11, 4, 2, 9, 15, 14};
	private static String generateRankKey(int userid){
		String ret = "";
		long s3 = 32768L+rd.nextInt(32767);
		long frontuserid = Long.valueOf((userid+"").substring(0, 4));
		String f1 = userid%Il[16] + rd.nextInt(9)*Il[16]+Il[16]+"";
		String f2 = (long)((Il[5]+s3)*(frontuserid+Il[16])-new Date().getTime()/1000+Il[17]+s3*9-userid) * Il[I1[userid%10]]+"";
		String f3 = rd.nextInt(9*(int)Il[16])+Il[16]+"";
		String f = f1+f2+f3;
		ret = rd.nextInt(10)+f.substring(0, 7)+rd.nextInt(10)+f.substring(7, 16)+rd.nextInt(10)+f.substring(16)+s3;
		return ret;
	}

}
