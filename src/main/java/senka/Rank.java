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
			runRankTask("76c73d1d3b775575e1ffc3116aec113835e8cd37", 8, 8156938);
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
		calsenka(server,token);
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
					String ranking = generateRankKey(userid);
					DBCollection cl_u_senka = Util.db.getCollection("cl_u_senka_"+server);
					Date now = new Date();
					int rankNo = Util.getRankDateNo(now);
					int year = now.getYear();
					int month = now.getMonth();
					String key = year + "_" + month + "_" + rankNo + "_" + server;
					BasicDBObject query = new BasicDBObject("_id",key);
					DBObject uData = cl_u_senka.findOne(query);
					if(uData==null){
						for(int i=1;i<100;i++){
							String param = "api%5Fpageno="+i+"&api%5Fverno=1&api%5Franking="+ranking+"&api%5Ftoken="+token;
							String ret = Lib.ApiPost(path, param, token, server);
							if(ret.startsWith("svdata=")){
								JSONObject j = new JSONObject(ret.substring(7));
								addData(j);
							}
						}
						calMagic();
						parseData();
						cl_u_senka.save(new BasicDBObject("_id",key).append("d", tmpdata.toString()).append("magic", magic));
					}else{
						String t1 = uData.get("d").toString();
						JSONArray j2 = new JSONArray(t1);
						for(int i=0;i<j2.length();i++){
							tmpdata.add(j2.getJSONObject(i));
						}
						magic = Integer.valueOf(uData.get("magic").toString());
					}
					handleSenkaList(server);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}).start();
	}
	


	public static void addData(JSONObject j)throws Exception{
		if(j.getInt("api_result")==1){
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
		}else{
			System.out.println("---------------error get senka----------------");
			System.out.println(j);
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
	
	public static void handleSenkaList(int server)throws Exception{
		DBCollection cl_n_senka = Util.db.getCollection("cl_n_senka_"+server);
		Date now = new Date();
		int dateNo = Util.getRankDateNo(now);
		for(int i=0;i<tmpdata.size();i++){
			JSONObject jdd = tmpdata.get(i);
			int no=jdd.getInt("api_mxltvkpyuklh");
			long key=jdd.getLong("api_wuhnhojjxmke");
			int lrate = (int)(key / MAGIC_R_NUMS[no%13]);
			int senka = lrate/magic - 91;
			String name = jdd.getString("api_mtjmdcwtvhdr");
			String cmt = jdd.getString("api_itbrdpdbkynm");
			BasicDBObject insert = new BasicDBObject();
			insert.append("cmt", cmt);
			insert.append("senka", senka);
			insert.append("no", no);
			insert.append("ts", dateNo);
			cl_n_senka.update(new BasicDBObject("_id",name),
					new BasicDBObject("$push",new BasicDBObject("d"+now.getMonth(),insert)).append("$set", new BasicDBObject("ts",now))
					,true,false);
		}
	}
	

	
	
	public static void calsenka(int server,String token)throws Exception{
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
			NameHandler.getIdByName(name, server);
//			try {
//				dbc = cl_senka.find(new BasicDBObject("name",name));
//				ArrayList<DBObject> userlist = new ArrayList<>();
//				while (dbc.hasNext()) {
//					DBObject userdata = dbc.next();
//					needUpdateUserDate(userdata,cl_senka);
//					userlist.add(userdata);
//				}
//				if(userlist.size()>1){
//					ArrayList<JSONObject> mayids = new ArrayList<>();
//					for(int k=0;k<userlist.size();k++){
//						DBObject ud = userlist.get(k);
//						String info = ud.get("info").toString();
//						JSONObject infoj = new JSONObject(info);
//						int rank = infoj.getInt("api_rank");
//						String tcmt = infoj.getString("api_cmt");
//						if((cmt.equals(tcmt)||(cmt.trim().length()>1&&tcmt.trim().length()>1))&&rank<=2){
//							JSONObject jd = new JSONObject();
//							jd.put("id", ud.get("_id"));
//							jd.put("senka", senka);
//							jd.put("name",name);
//							jd.put("n", i);
//							mayids.add(jd);
//						}
//					}
//					if(mayids.size()==1){
//						idlist.add(mayids.get(0));
//					}else if(mayids.size()==0){
//						System.out.println("++++++++++++++++++++++++++++");
//						System.out.println("can't find id:"+name+","+senka);
//						System.out.println(userlist);
//					}else if(mayids.size()<9){
//						ArrayList<JSONObject> mayids2 = new ArrayList<>();
//						for(int x=0;x<mayids.size();x++){
//							JSONObject jm = mayids.get(x);
//							int id = jm.getInt("id");
//							String path = "/kcsapi/api_req_member/get_practice_enemyinfo";
//							String param = "api%5Ftoken="+token+"&api%5Fmember%5Fid="+id+"&api%5Fverno=1";
//							String r = Lib.ApiPost(path, param, token, Integer.valueOf(server));
//							if(r.startsWith("svdata="));
//							JSONObject jddd = new JSONObject(r.substring(7));
//							JSONObject userdata = jddd.getJSONObject("api_data");
//							int trank = userdata.getInt("api_rank");
//							if(trank==1){
//								jm.put("data", userdata);
//								mayids2.add(jm);
//							}
//						}
//						if(mayids2.size()==1){
//							idlist.add(mayids2.get(0));
//						}else if(mayids2.size()==0){
//							System.out.println("************************");
//							System.out.println("can't find id:"+name+","+senka);
//							System.out.println(mayids);
//						}else{
//							ArrayList<JSONObject> mayids3= new ArrayList<>();
//							for(int y=0;y<mayids2.size();y++){
//								JSONObject userdata = mayids2.get(y).getJSONObject("data");
//								String ncmt = userdata.getString("api_cmt");
//								if(ncmt.equals(cmt)){
//									mayids3.add(mayids2.get(y));
//								}
//							}
//							if(mayids3.size()==1){
//								idlist.add(mayids3.get(0));
//							}else if(mayids3.size()==0){
//								System.out.println("=========================");
//								System.out.println("can't find id:"+name+","+senka);
//								for(int z=0;z>mayids2.size();z++){
//									idlist.add(mayids2.get(z));
//								}
//							}else{
//								for(int z=0;z>mayids3.size();z++){
//									idlist.add(mayids3.get(z));
//								}
//							}
//						}
//					}else{
//						System.out.println("+++++++++++++++++++++++++");
//						System.out.println("too many ids find:"+name+","+senka);
//						System.out.println(mayids);
//					}
//					
//					
//					
//				}else if(userlist.size()==1){
//					JSONObject jd = new JSONObject();
//					jd.put("id", userlist.get(0).get("_id"));
//					jd.put("senka", senka);
//					jd.put("name",name);
//					jd.put("n", i);
//					idlist.add(jd);
//				}else{
//					System.out.println("NO USER FOUND!"+name);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//				if(dbc!=null){
//					dbc.close();
//				}
//			}
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
			//cl_senka.update(new BasicDBObject("_id", id),update,true,false);
			dbl.add(new BasicDBObject("id",id).append("senka", senka).append("name", jrd.get("name")).append("n", jrd.get("n")));
		}
		//cl_tmp_senka.save(new BasicDBObject("_id",now).append("d", dbl));
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