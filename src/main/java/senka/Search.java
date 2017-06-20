package senka;

import java.net.URLDecoder;
import java.util.Date;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class Search {

	public static void main(String[] args) {
		try {
			searchByName("クロサワ　ソウイチ", "82686f39ebbfd68bc8ee6d3fc29b0c28a952aa59", 8);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String seekByName(Map<String, String[]> data)throws Exception{
		String token = data.get("token")[0];
		String name = URLDecoder.decode(data.get("name")[0],"utf-8");
		int server = Integer.valueOf(data.get("server")[0]);
		String ret = searchByName(name, token, server);
		return name+"\n当前战果值："+ret+"\n"+"";
	}
	
	public static String searchByName(String name,String token,int server)throws Exception{
		DBCollection cl_senka = Util.db.getCollection("cl_senka_"+server);
		DBCollection cl_n_senka = Util.db.getCollection("cl_n_senka_"+server);
		DBObject senkaData = cl_n_senka.findOne(new BasicDBObject("_id",name));
		Date now = new Date();
		if(senkaData!=null){
			Object ido = senkaData.get("id");
			if(ido!=null){
				String ids = ido.toString();
				String[] ida = ids.split(",");
				if(ida.length==1){
					String id = ida[0];
					DBObject userdata = cl_senka.findOne(new BasicDBObject("_id",Integer.valueOf(id)));
					BasicDBList explist = (BasicDBList)userdata.get("exp");
					BasicDBList senkalist = (BasicDBList)senkaData.get("d"+now.getMonth());
					int pointer1 = 0;
					int pointer2 = 0;
					JSONObject front = null;
					while(pointer1<explist.size()&&pointer2<senkalist.size()){
						DBObject expdata = (DBObject)explist.get(pointer1);
						DBObject senkadata = (DBObject)senkalist.get(pointer2);
						int exp = Integer.valueOf(expdata.get("d").toString());
						int senka = Integer.valueOf(senkadata.get("senka").toString());
						Date expts = (Date)expdata.get("ts");
						int senkano = Integer.valueOf(senkadata.get("ts").toString());
						if(isExpKeyTs(expts)){
							int expno = Util.getRankDateNo(new Date(expts.getTime()+3600000*2));
							if(expno==senkano){
								front = new JSONObject();
								front.put("ts", expts);
								front.put("senka", senka);
								front.put("exp", exp);
								break;
							}else if(expno>senkano){
								pointer2++;
							}else{
								pointer1++;
							}
						}else{
							pointer1++;
						}
					}
					pointer1 = 0;
					pointer2 = 0;
					JSONObject tail = null;
					while(pointer1<explist.size()&&pointer2<senkalist.size()){
						DBObject expdata = (DBObject)explist.get(explist.size()-1-pointer1);
						DBObject senkadata = (DBObject)senkalist.get(senkalist.size()-1-pointer2);
						int exp = Integer.valueOf(expdata.get("d").toString());
						int senka = Integer.valueOf(senkadata.get("senka").toString());
						Date expts = (Date)expdata.get("ts");
						int senkano = Integer.valueOf(senkadata.get("ts").toString());
						if(isExpKeyTs(expts)){
							int expno = Util.getRankDateNo(new Date(expts.getTime()+3600000*2));
							if(expno==senkano){
								tail = new JSONObject();
								tail.put("ts", expts);
								tail.put("senka", senka);
								tail.put("exp", exp);
								break;
							}else if(expno>senkano){
								pointer1++;
							}else{
								pointer2++;
							}
						}else{
							pointer1++;
						}
					}
					int nowexp = getNowExp(Integer.valueOf(id), token, server);
					String addsenka = "("+tail.getInt("no")+")   "+tail.getString("senka")+"+"+Math.round((nowexp-tail.getInt("exp"))/1000.0*7.0)/10.0+"   ("+now.toLocaleString()+")\n";
					Date frontts = (Date)front.get("ts");
					Date tailts = (Date)tail.get("ts");
					if(tailts.getTime()-frontts.getTime()>40000000){
						int senkasub = tail.getInt("senka")-front.getInt("senka");
						int expsub = tail.getInt("exp")-front.getInt("exp");
						addsenka = addsenka + "EX:"+(int)(senkasub-expsub/10000.0*7.0)+"    ("+frontts.toLocaleString()+"-----"+tailts.toLocaleString()+")";
					}
					System.out.println(addsenka);
					return addsenka;
				}
			}
		}else{
			System.out.println("no user found:"+name);
		}
		return "something error";
	}
	
	public static int getNowExp(int id,String token,int server)throws Exception{
		String path = "/kcsapi/api_req_member/get_practice_enemyinfo";
		String param = "api%5Ftoken="+token+"&api%5Fmember%5Fid="+id+"&api%5Fverno=1";
		try {
			String r = Lib.ApiPost(path, param, token, server);
			if(r.startsWith("svdata="));
			JSONObject jd = new JSONObject(r.substring(7));
			System.out.println(jd);
			JSONObject data = jd.getJSONObject("api_data");
			JSONArray expa = data.getJSONArray("api_experience");
			int exp = expa.getInt(0);
			return exp;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	
	
	private static boolean isExpKeyTs(Date dat){
		Date  n1 = new Date(dat.getTime()+(dat.getTimezoneOffset()+480)*60000);
		int left = (int)(43200000-(n1.getTime()-18000000)%43200000)/1000;
		if(left<1200||left>43200-600){
			return true;
		}else{
			return false;
		}
	}

}
