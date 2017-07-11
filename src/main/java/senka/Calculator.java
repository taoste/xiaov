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

import com.alibaba.druid.sql.visitor.functions.If;
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
			calculateRank(8);
		} catch (Exception e) {
			e.printStackTrace();
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
			dbc = cl_n_senka.find(); //1000 min
			Map<Integer, DBObject> id2senka = new HashMap<>();
			Map<Integer, Integer> minmap = new HashMap<>();
			Map<Integer, Integer> frontmap = new HashMap<>();
			while (dbc.hasNext()) {
				DBObject senkaData = dbc.next();
				Object ido = senkaData.get("id");
				Object senkaListObj = senkaData.get("d"+now.getMonth());
				if(senkaListObj==null){
					continue;
				}
				BasicDBList senkaList = (BasicDBList)senkaListObj;
				DBObject frontSenka = (DBObject)senkaList.get(0);
				int no = Integer.valueOf(frontSenka.get("no").toString());
				int ts = Integer.valueOf(frontSenka.get("ts").toString());
				int senka = Integer.valueOf(frontSenka.get("senka").toString());
				if(minmap.get(ts)==null){
					minmap.put(ts, senka);
				}else{
					if(senka<minmap.get(ts)){
						minmap.put(ts, senka);
					}
				}
				if(ts==0){
					if(no==5||no==20||no==100||no==500){
						frontmap.put(no, senka);
						System.out.println(no);
						System.out.println(senka);
					}
				}
				Date lastupdatets = (Date)senkaData.get("ts");
				if(now.getTime()-lastupdatets.getTime()>60000000L){
					continue;
				}
				if(ido==null){
					continue;
				}
				String idstr = senkaData.get("id").toString();
				if(idstr.equals("")){
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
				Object zo = userData.get("z");
				int z=-1;
				if(zo!=null){
					z=Integer.valueOf(zo.toString());
				}
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
					DBObject baseExpData = getBaseExpData(explist);
					DBObject frontExpData = getFrontExpData(explist);

					int firstexp = Integer.valueOf(firstExpData.get("d").toString());
					Date firstts = (Date)firstExpData.get("ts");
					int baseexp = Integer.valueOf(baseExpData.get("d").toString());
					Date basets = (Date)baseExpData.get("ts");
					int subbase = (firstexp-baseexp)*7/10000;
					int subsenka = (latestexp-firstexp)*7/10000;
					int subfront =-1;
					if(frontExpData!=null){
						int frontexp = Integer.valueOf(frontExpData.get("d").toString());
						subfront = (latestexp-frontexp)*7/10000;
					}
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
						if(exto<pairexto.getTime()){
							exto = pairexto.getTime();
						}
						retj.put("fsenka", fsenka);
						retj.put("fsenkats", fsenkats);
						retj.put("lsenka", senka);
						retj.put("lsenkats", senkats);
						retj.put("lno", lastno);
						retj.put("expfrom", firstts.getTime());
						retj.put("expto", latestts.getTime());
						
						retj.put("basets", basets.getTime());
						retj.put("subbase", subbase);
						
						retj.put("subfront", subfront);
						
						retj.put("subsenka", subsenka);
						retj.put("pair", 1);
						retj.put("z", z);
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
						ret.put("basets", basets.getTime());
						ret.put("subbase", subbase);
						
						ret.put("subfront", subfront);
						
						ret.put("subsenka", subsenka);
						ret.put("z", z);
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
			j.put("min", minmap);
			j.put("front", frontmap);
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
	
	public static DBObject getFrontExpData(BasicDBList explist){
		Date now =	new Date();
		int month = now.getMonth();
		Date then = new Date();
		then.setMonth(month);
		then.setDate(1);
		then.setHours(0);
		then.setMinutes(0);
		then.setSeconds(0);
		Date frontDate = new Date(then.getTime()-3600000*3);
		for(int i=0;i<explist.size();i++){
			DBObject expdata = (DBObject)explist.get(explist.size()-i-1);
			Date ts = (Date)expdata.get("ts");
			if(Math.abs(ts.getTime()-frontDate.getTime())<1200000){
				return expdata;
			}
		}
		return null;
	}
	
	
	public static DBObject getBaseExpData(BasicDBList explist){
		Date now = new Date();
		int month = now.getMonth();
		for(int i=0;i<explist.size();i++){
			DBObject expdata = (DBObject)explist.get(explist.size()-i-1);
			Date ts = (Date)expdata.get("ts");
			if(ts.getMonth()<month){
				return expdata;
			}
		}
		return null;
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
			
			
			DBObject latestexpdata = (DBObject)expL.get(expL.size()-1);
			int latestexp = Integer.valueOf(latestexpdata.get("d").toString());
			Date latestts = (Date)latestexpdata.get("ts");

			DBObject senkaD = (DBObject)senkaList.get(senkaList.size()-1);
			DBObject senkaF = (DBObject)senkaList.get(0);
			int fsenka = Integer.valueOf(senkaF.get("senka").toString());
			int fsenkats = Integer.valueOf(senkaF.get("ts").toString()); 
			int lsenka = Integer.valueOf(senkaD.get("senka").toString());
			int lsenkats = Integer.valueOf(senkaD.get("ts").toString()); 
			int lastno = Integer.valueOf(senkaD.get("no").toString()); 
			DBObject firstExpData  = getFirstExpData(expL);
			int firstexp = Integer.valueOf(firstExpData.get("d").toString());
			Date firstts = (Date)firstExpData.get("ts");
			int subsenka = (latestexp-firstexp)*7/10000;
			

			int senka = Integer.valueOf(senkaD.get("senka").toString());
			int senkats = Integer.valueOf(senkaD.get("ts").toString()); 
			
			
			DBObject baseExpData = getBaseExpData(expL);
			int baseexp = Integer.valueOf(baseExpData.get("d").toString());
			Date basets = (Date)baseExpData.get("ts");
			int subbase = (firstexp-baseexp)*7/10000;
			
			
			
			
			if(pairlist.size()>0){

				JSONObject retj = getResultByPairlist(latestexp, latestts,pairlist, name);
				
				retj.put("type", 1);
				retj.put("fsenka", fsenka);
				retj.put("fsenkats", fsenkats);
				retj.put("senkats", senkats);
				retj.put("lsenka", lsenka);
				retj.put("lsenkats", lsenkats);
				retj.put("lno", lastno);
				retj.put("expfrom", firstts.getTime());
				retj.put("expto", latestts.getTime());
				retj.put("subsenka", subsenka);
				retj.put("pair", 1);
				retj.put("basets", basets.getTime());
				retj.put("subbase", subbase);
				
				retj.put("z", -1);
				result.add(retj);
				a++;
			}else{
				
			}
		}
		if(c>a){
			ArrayList<JSONObject> mayexplist = new ArrayList<>();
			int maxexpsenka = 0;
			for(int i=0;i<explist.size();i++){
				int id = idlist.get(i);
				BasicDBList expL = explist.get(i);
				DBObject latestexpdata = (DBObject)expL.get(expL.size()-1);
				DBObject firstexpdata = getFirstExpData(expL);
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
				ret.put("subsenka", subsenka);
				mayexplist.add(ret);
			}
			int lastts=0;
			for(int i=jsenkaList.size();i>0;i--){
				JSONObject jd = jsenkaList.get(i-1);
				JSONObject fd = jsenkaList.get(0);
				int ts = jd.getInt("ts");
				if(ts>=lastts){
					int senka = jd.getInt("senka");
					int no = jd.getInt("no");
					int fsenka = fd.getInt("senka");
					int fts = fd.getInt("ts");
					JSONObject ret = new JSONObject();
					ret.put("lsenka", senka);
					ret.put("senka", senka);
					ret.put("fsenka", fsenka);
					ret.put("fts", fts);
					ret.put("lno", no);
					ret.put("may", mayexplist);
					ret.put("type", 2);
					ret.put("name", name);
					ret.put("senkalist", jsenkaList);
					result.add(ret);
					lastts=ts;
					System.out.println(ret);
				}else{
					break;
				}
			}
		}
		return result;
	}
	
	public static void calculateZ(int server,int month){
		DBCollection cl_n_senka = Util.db.getCollection("cl_n_senka_"+server);
		DBCollection cl_senka = Util.db.getCollection("cl_senka_"+server);
		DBCursor dbc = null;
		Date now = new Date();
		try {
			dbc = cl_n_senka.find();
			while (dbc.hasNext()) {
				DBObject dbObject = (DBObject) dbc.next();
				String key = "d"+month;
				String name = dbObject.get("_id").toString();
				Object senkaListObject = dbObject.get(key);
				BasicDBList senkaList=null;
				if(senkaListObject!=null){
					senkaList = (BasicDBList)senkaListObject;
				}
				Object ido = dbObject.get("id");
				String ids = null;
				if(ido!=null){
					ids = ido.toString();
				}
				if(senkaList!=null&&ids!=null&&ids.length()>1){
					String[] ida = ids.split(",");
					if(ida.length==1){
						DBObject user = cl_senka.findOne(new BasicDBObject("_id",Integer.valueOf(ids)));
						BasicDBList expList = (BasicDBList)user.get("exp");
						int pointer1=0;
						int pointer2=0;
						ArrayList<JSONObject> pairlist = new ArrayList<>();
						while(pointer1<expList.size()&&pointer2<senkaList.size()){
							DBObject expData = (DBObject)expList.get(pointer1);
							DBObject senkaData = (DBObject)senkaList.get(pointer2);
							Date expts = (Date)expData.get("ts");
							
							if(expts.getMonth()<month){
								pointer1++;
							}else if(expts.getMonth()>month){
								break;
							}else{
								int rankts = Integer.valueOf(senkaData.get("ts").toString());
								int expno = Util.getRankDateNo(new Date(expts.getTime()+3600000*2));
								if(expno<rankts){
									pointer1++;
								}else if(expno>rankts){
									pointer2++;
								}else{
									if(!isExpKeyTs(expts)){
										pointer1++;
									}else{
										int exp = Integer.valueOf(expData.get("d").toString());
										int senka = Integer.valueOf(senkaData.get("senka").toString());
										JSONObject j = new JSONObject();
										j.put("exp", exp);
										j.put("senka", senka);
										j.put("ts", expts);
										pairlist.add(j);
										pointer1++;
										pointer2++;
									}
								}
							}
						}
						if(pairlist.size()>1){
							JSONObject firstPair = pairlist.get(0);
							JSONObject lastPair = pairlist.get(pairlist.size()-1);
							int firstexp = firstPair.getInt("exp");
							int lastexp = lastPair.getInt("exp");
							int subexp = lastexp-firstexp;
							int firstsenka = firstPair.getInt("senka");
							int lastsenka = lastPair.getInt("senka");
							int subsenka = lastsenka-firstsenka;
							int ex = subsenka-subexp*7/10000;
							if(ex>1050){
								System.out.println();
								System.out.println(name+":"+ex);
								System.out.println(pairlist);
								cl_senka.update(user, new BasicDBObject("$set",new BasicDBObject("z",month)));
							}
						}
						
					}else{
						
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(dbc != null){
				dbc.close();
			}
		}
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
