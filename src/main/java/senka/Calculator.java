package senka;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.enterprise.inject.New;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import lib.TimerTask;

public class Calculator {


	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Date d = new Date();
			Date d2 = new Date(d.getTime()+3600000*3);
			System.out.println(Util.getRankDateNo(d2));
			System.out.println(d);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public static String easyRank(Map<String, String[]> data)throws Exception{
		int server = Integer.valueOf(data.get("server")[0]);
		JSONObject jret = calculateRank(server);
		JSONArray ret = jret.getJSONArray("d");
		String result = "";
		result = result + "-------------战果简报-------------\n";
		result = result + "5位："+ret.getJSONObject(4).getInt("senka")+"\n";
		result = result + "20位："+ret.getJSONObject(19).getInt("senka")+"\n";
		result = result + "100位："+ret.getJSONObject(99).getInt("senka")+"\n";
		result = result + "500位："+ret.getJSONObject(499).getInt("senka")+"\n";
		Date calts = (Date)jret.get("ts");
		result = result + "统计时间："+calts.toLocaleString();
		return result;
	}
	
	
	public static String calculator(Map<String, String[]> data)throws Exception{
		int server = Integer.valueOf(data.get("server")[0]);
		JSONObject j = calculateRank(server);
		j.put("r", 0);
		return j.toString();
	}

	public static JSONObject calculateRank(int server){
		return calculateRank_D(server);
	}
	
	public static JSONObject calculateRank_D(int server){
		DBCollection cl_n_senka = Util.db.getCollection("cl_n_senka_"+server);
		DBCollection cl_senka = Util.db.getCollection("cl_senka_"+server);
		DBCursor dbc = null;
		DBCursor dbc2 = null;
		Date now = new Date();
		int rankNo = Util.getRankDateNo(now);
		long updatets = 0;
		try {
			ArrayList<JSONObject> resultlist = new ArrayList<>();
			BasicDBList dbl = new BasicDBList(); 
			dbc = cl_n_senka.find(new BasicDBObject("ts",new BasicDBObject("$gt",new Date(now.getTime()-60000000L)))); //1000 min
			Map<Integer, DBObject> id2senka = new HashMap<>();
			while (dbc.hasNext()) {
				DBObject senkaData = dbc.next();
				Object ido = senkaData.get("id");
				if(ido==null){
					System.out.println(senkaData);
					continue;
				}
				String idstr = senkaData.get("id").toString();
				if(idstr.equals("")){
					System.out.println(senkaData);
					continue;
				}
				String[] ida = idstr.split(",");
				if(ida.length==1){
					dbl.add(Integer.valueOf(ida[0]));
					id2senka.put(Integer.valueOf(ida[0]), senkaData);
				}else if(ida.length<5){
					int c = Integer.valueOf(senkaData.get("c").toString());
					if(c==1){
						for(int i=0;i<ida.length;i++){
							dbl.add(Integer.valueOf(ida[i]));
							id2senka.put(Integer.valueOf(ida[i]), senkaData);
						}
					}else{
						ArrayList<JSONObject> dupNameResult = handleDuplicateNames(senkaData,server);
						resultlist.addAll(dupNameResult);
					}
				}else{
					
				}
			}
			dbc2 = cl_senka.find(new BasicDBObject("_id",new BasicDBObject("$in",dbl)));
			long exfrom = 1999123456789L;
			long exto = 0L;
			long expfrom = 1999123456789L;
			long expto = 0L;
			int latestsenkats = 0;
			while (dbc2.hasNext()) {
				DBObject userData = (DBObject) dbc2.next();
				int id = Integer.valueOf(userData.get("_id").toString());
				String name = userData.get("name").toString();
				DBObject senkaData = id2senka.get(id);
				Object ido = senkaData.get("id");
				Object co = senkaData.get("c");
				if(ido==null||co==null){
					continue;
				}
				String idstr = senkaData.get("id").toString();
				int c = Integer.valueOf(senkaData.get("c").toString());
				String[] ida = idstr.split(",");
				BasicDBList explist = (BasicDBList)userData.get("exp");
				BasicDBList senkalist = (BasicDBList)senkaData.get("d"+now.getMonth());
				int pointer1 = 0;
				int pointer2 = 0;
				ArrayList<JSONObject> pairlist = new ArrayList<>();
				int lastexp=0;
				int lastsenka=0;
				int drop=0;
				while(pointer1<explist.size()&&pointer2<senkalist.size()){
					DBObject exp = (DBObject)explist.get(pointer1);
					DBObject senka = (DBObject)senkalist.get(pointer2);
					Date expts = (Date)exp.get("ts");
					if(!isExpKeyTs(expts)){
						pointer1++;
					}else{
						int expno = Util.getRankDateNo(new Date(expts.getTime()+3600000*2));
						int senkano = Integer.valueOf(senka.get("ts").toString());
						if(expts.getMonth()<now.getMonth()){
							pointer1++;
						}else if(expno>senkano){
							pointer2++;
						}else if(expno<senkano){
							pointer1++;
						}else{
							JSONObject j = new JSONObject();
							int expthen = Integer.valueOf(exp.get("d").toString());
							int senkathen = Integer.valueOf(senka.get("senka").toString());
							j.put("exp", expthen);
							j.put("ts", expts);
							j.put("senka", senkathen);
							if(lastexp>0){
								int addexpsenka = (expthen-lastexp)*7/10000;
								int addsenka = senkathen-lastsenka;
								if(Math.abs(addexpsenka-addsenka)>10&&Math.abs(addexpsenka-addsenka)<60){
									if(c==1&&ida.length>1){
										drop=1;
									}
								}
								j.put("subexp", addsenka);
								j.put("subsenka",addexpsenka);
							}
							lastexp= expthen;
							lastsenka = senkathen;
							pairlist.add(j);
							pointer1++;
							pointer2++;
						}
					}
				}

				if(drop==0){
					DBObject latestexpdata = (DBObject)explist.get(explist.size()-1);
					int latestexp = Integer.valueOf(latestexpdata.get("d").toString());
					Date latestts = (Date)latestexpdata.get("ts");
					if(latestts.getTime()>updatets){
						updatets = latestts.getTime();
					}
					DBObject senkaD = (DBObject)senkalist.get(senkalist.size()-1);
					DBObject senkaF = (DBObject)senkalist.get(0);
					int fsenka = Integer.valueOf(senkaF.get("senka").toString());
					int fsenkats = Integer.valueOf(senkaF.get("ts").toString()); 
					int senka = Integer.valueOf(senkaD.get("senka").toString());
					int senkats = Integer.valueOf(senkaD.get("ts").toString()); 
					int lastno = Integer.valueOf(senkaD.get("no").toString()); 
					DBObject firstExpData  = getFirstExpData(explist);
					int firstexp = Integer.valueOf(firstExpData.get("d").toString());
					Date firstts = (Date)firstExpData.get("ts");
					int subsenka = (latestexp-firstexp)*7/10000;
					if(latestsenkats<senkats){
						latestsenkats=senkats;
					}
					if(expfrom>firstts.getTime()){
						expfrom=firstts.getTime();
					}
					if(expto<latestts.getTime()){
						expto=latestts.getTime();
					}
					JSONObject retj = getResultByPairlist(latestexp,latestts, pairlist, name);
					
					if(retj!=null){
						Date pairexfrom = (Date)pairlist.get(0).get("ts");
						Date pairexto = (Date)pairlist.get(pairlist.size()-1).get("ts");
						
						if(exfrom>pairexfrom.getTime()){
							exfrom = pairexfrom.getTime();
						}
						if(exto>pairexto.getTime()){
							exto = pairexto.getTime();
						}
						retj.put("lsenka", senka);
						retj.put("lsenkats", senkats);
						retj.put("lno", lastno);
						retj.put("expfrom", firstts.getTime());
						retj.put("expto", latestts.getTime());
						retj.put("subsenka", subsenka);
						resultlist.add(retj);
					}else{
						JSONObject ret = new JSONObject();


						ret.put("type", 3);
						ret.put("fsenka", fsenka);
						ret.put("fsenkats", fsenkats);
						ret.put("name", name);
						ret.put("senka", senka);
						ret.put("senkats", senkats);
						ret.put("lsenka", senka);
						ret.put("lsenkats", senkats);
						ret.put("lno", lastno);
						ret.put("expfrom", firstts.getTime());
						ret.put("expto", latestts.getTime());
						ret.put("subsenka", subsenka);
						resultlist.add(ret);
					}
				}
			}
			Collections.sort(resultlist, new Comparator<JSONObject>() {
				public int compare(JSONObject a,JSONObject b){
					try {
						return b.getInt("senka")-a.getInt("senka");
					} catch (JSONException e) {
						e.printStackTrace();
						return 1;
					}
				}
			});
			JSONObject j = new JSONObject();
			j.put("ts", new Date(updatets).getTime());
			j.put("exfrom", exfrom);
			j.put("exto", exto);
			j.put("expfrom", expfrom);
			j.put("expto", expto);
			j.put("rankts", latestsenkats);
			j.put("d", resultlist);
			return j;
		}catch (Exception e) {
			e.printStackTrace();
			return new JSONObject();
		} finally{
			if(dbc!=null){
				dbc.close();
			}
			if(dbc2!=null){
				dbc.close();
			}
		}
	}
	
	public static DBObject getFirstExpData(BasicDBList explist){
		Date now = new Date();
		for(int i=0;i<explist.size();i++){
			DBObject expdata = (DBObject)explist.get(i);
			Date ts = (Date)expdata.get("ts");
			if(ts.getMonth()==now.getMonth()){
				return expdata;
			}
		}
		return null;
		
	}
	
	public static JSONObject getResultByPairlist(int latestexp,Date latestts,ArrayList<JSONObject> pairlist,String name)throws Exception{

		if(pairlist.size()>0){
			JSONObject latestpair = pairlist.get(pairlist.size()-1);
			JSONObject firstpair = pairlist.get(0);
			int senkasub = latestpair.getInt("senka")-firstpair.getInt("senka");
			int expsub = latestpair.getInt("exp")-firstpair.getInt("exp");
			int ex = senkasub-expsub*7/10000;
			Date latestpairts = (Date)latestpair.get("ts");
			int latestpairexp = latestpair.getInt("exp");
			int latestpairsenka = latestpair.getInt("senka");
			Date firstpairts = (Date)firstpair.get("ts");
			JSONObject j = new JSONObject();
			if(latestts.getTime()>latestpairts.getTime()){
				int senkanow = (latestexp-latestpairexp)*7/10000+latestpairsenka;
				j.put("senka", senkanow);
				j.put("ts", latestts);
			}else{
				j.put("senka", latestpairsenka);
				j.put("ts", latestpairts);
			}
			j.put("type", 1);
			j.put("name",name);
			j.put("ex", ex);
			j.put("exfrom", firstpairts.getTime());
			j.put("exto", latestpairts.getTime());
			return j;
		}else{
			return null;
		}
	}
	
	
	public static ArrayList<JSONObject> handleDuplicateNames(DBObject senkaData,int server)throws Exception{
		int c = Integer.valueOf(senkaData.get("c").toString());
		String idstr = senkaData.get("id").toString();
		String[] ida = idstr.split(",");
		BasicDBList dbl = new BasicDBList();
		for(int i=0;i<ida.length;i++){
			dbl.add(Integer.valueOf(ida[i]));
		}
		DBCursor dbc = null;
		Date now = new Date();
		BasicDBList senkaList = (BasicDBList)senkaData.get("d"+now.getMonth());
		ArrayList<JSONObject> jsenkaList = new ArrayList<>();
		for(int i=0;i<senkaList.size();i++){
			DBObject senka = (DBObject)senkaList.get(i);
			JSONObject jd = new JSONObject(senka.toString());
			jsenkaList.add(jd);
		}
		ArrayList<BasicDBList> explist = new ArrayList<>();
		ArrayList<Integer> idlist = new ArrayList<>();
		DBCollection cl_senka = Util.db.getCollection("cl_senka_"+server);
		Map<String, Integer> cmt2id = new HashMap<>();
		String name = senkaData.get("_id").toString();
		ArrayList<JSONObject> result = new ArrayList<>();
		try {
			dbc = cl_senka.find(new BasicDBObject("_id",new BasicDBObject("$in",dbl)));
			ArrayList<Integer> emptyCmtList = new ArrayList<>();
			while (dbc.hasNext()) {
				DBObject dbObject = (DBObject) dbc.next();
				BasicDBList exp = (BasicDBList)dbObject.get("exp");
				JSONObject info = new JSONObject(dbObject.get("info").toString());
				String cmt = info.getString("api_cmt");
				int id = Integer.valueOf(dbObject.get("_id").toString());
				if(!cmt.equals("")){
					cmt2id.put(cmt, id);
				}else{
					emptyCmtList.add(id);
				}
				int lastts = -1;
				idlist.add(id);
				explist.add(exp);
			}
			if(emptyCmtList.size()==1){
				cmt2id.put("", emptyCmtList.get(0));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(dbc!=null){
				dbc.close();
			}
		}
		
		for(int i=0;i<jsenkaList.size();i++){
			JSONObject jsenka = jsenkaList.get(i);
			String cmt = jsenka.getString("cmt");
			Object ido = cmt2id.get(cmt);
			if(ido!=null){
				int id = (int)ido;
				jsenka.put("id", id);
			}
		}
		int a=0;
		for(int i=0;i<explist.size();i++){
			int id = idlist.get(i);
			BasicDBList expL = explist.get(i);
			int pointer1=0;
			int pointer2=0;		
			int lastexp=0;
			int lastsenka =0;
			ArrayList<JSONObject> pairlist = new ArrayList<>();
			while(pointer1<expL.size()&&pointer2<jsenkaList.size()){
				DBObject exp = (DBObject)expL.get(pointer1);
				JSONObject senka = jsenkaList.get(pointer2);
				int senkaid = 0;
				if(senka.has("id")){
					senkaid = senka.getInt("id");
				}
				Date expts = (Date)exp.get("ts");
				if(!isExpKeyTs(expts)){
					pointer1++;
				}else if(senkaid!=id){
					pointer2++;
				}else{
					int expno = Util.getRankDateNo(new Date(expts.getTime()+3600000*2));
					int senkano = Integer.valueOf(senka.get("ts").toString());
					if(expts.getMonth()<now.getMonth()){
						pointer1++;
					}else if(expno>senkano){
						pointer2++;
					}else if(expno<senkano){
						pointer1++;
					}else{
						JSONObject j = new JSONObject();
						int expthen = Integer.valueOf(exp.get("d").toString());
						int senkathen = Integer.valueOf(senka.get("senka").toString());
						j.put("exp", expthen);
						j.put("ts", expts);
						j.put("senka", senkathen);
						j.put("name", name);
						j.put("senkats", senkano);
						if(lastexp>0){
							int addexpsenka = (expthen-lastexp)*7/10000;
							int addsenka = senkathen-lastsenka;

							j.put("subexp", addsenka);
							j.put("subsenka",addexpsenka);
							
						}
						lastexp= expthen;
						lastsenka = senkathen;
						pairlist.add(j);
						pointer2++;
					}
				}
			}
			if(pairlist.size()>0){
				DBObject latestexpdata = (DBObject)expL.get(expL.size()-1);
				int latestexp = Integer.valueOf(latestexpdata.get("d").toString());
				Date latestts = (Date)latestexpdata.get("ts");
				JSONObject retj = getResultByPairlist(latestexp, latestts,pairlist, name);
				result.add(retj);
				a++;
			}
		}
		if(c>a){
			ArrayList<JSONObject> mayexplist = new ArrayList<>();
			int maxexpsenka = 0;
			for(int i=0;i<explist.size();i++){
				int id = idlist.get(i);
				BasicDBList expL = explist.get(i);
				DBObject latestexpdata = (DBObject)expL.get(expL.size()-1);
				DBObject firstexpdata = (DBObject)expL.get(0);
				Date firstts = (Date)firstexpdata.get("ts");
				Date lastts = (Date)latestexpdata.get("ts");
				int subexp = Integer.valueOf(latestexpdata.get("d").toString())-Integer.valueOf(firstexpdata.get("d").toString());
				int subsenka = subexp*7/10000;
				if(subsenka>maxexpsenka){
					maxexpsenka=subsenka;
				}
				JSONObject ret = new JSONObject();
				ret.put("expfrom", firstts);
				ret.put("expto", lastts);
				ret.put("senka", subsenka);
				mayexplist.add(ret);
			}
			int lastts=0;
			for(int i=jsenkaList.size();i>0;i--){
				JSONObject jd = jsenkaList.get(i-1);
				int ts = jd.getInt("ts");
				if(ts>=lastts){
					int senka = jd.getInt("senka");
					JSONObject ret = new JSONObject();
					ret.put("lsenka", senka);
					ret.put("senka", senka);
					ret.put("may", mayexplist);
					ret.put("type", 2);
					ret.put("name", name);
					result.add(ret);
					lastts=ts;
				}else{
					break;
				}
			}
		}
		return result;
	}
	

	
	
	
	public static boolean isExpKeyTs(Date dat){
		Date  n1 = new Date(dat.getTime()+(dat.getTimezoneOffset()+480)*60000);
		int left = (int)(43200000-(n1.getTime()-18000000)%43200000)/1000;
		if(left<1200||left>43200-600){
			return true;
		}else{
			return false;
		}
	}
}