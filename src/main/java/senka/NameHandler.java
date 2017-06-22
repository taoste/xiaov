package senka;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class NameHandler {
	private static int totalcount = 0;
	public static void getIdByName(String name,int server){
		DBCursor dbc = null;
		DBCollection cl_senka = Util.db.getCollection("cl_senka_"+server);
		ArrayList<Integer> idlist = new ArrayList<>();
 		try {
			dbc = cl_senka.find(new BasicDBObject("name",name));
			ArrayList<DBObject> userlist = new ArrayList<>();
			while (dbc.hasNext()) {
				DBObject userdata = dbc.next();
				userlist.add(userdata);
			}
			System.out.println(name+":"+userlist.size());
			
			if(userlist.size()==0){
				System.out.println("------------------------------------------");
				System.out.println("need fetch users");
			}else if(userlist.size()==1){
				idlist.add(Integer.valueOf(userlist.get(0).get("_id").toString()));
			}else{
				totalcount = totalcount + userlist.size();
			}
		}catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(dbc!=null){
				dbc.close();
			}
		}
		System.out.println("total users:"+totalcount);
	}
	
	public static void handleSenkaList2(int server,ArrayList<JSONObject> senkaList)throws Exception{
		DBCollection cl_senka = Util.db.getCollection("cl_senka_"+server);
		BasicDBList idlist = new BasicDBList();
		for(int i=0;i<senkaList.size();i++){
			JSONObject jd = senkaList.get(i);
			int senka = jd.getInt("senka");
			String name = jd.getString("name");
			String cmt = jd.getString("cmt");
			int no = jd.getInt("no");
			DBCursor dbc = null;
			try {
				dbc = cl_senka.find(new BasicDBObject("name",name));
				ArrayList<DBObject> userlist = new ArrayList<>();
				while (dbc.hasNext()) {
					DBObject dbObject = (DBObject) dbc.next();
					userlist.add(dbObject);
				}
				if(userlist.size()==0){
					System.out.println("------------------------------------------");
					System.out.println("need fetch users");
				}else if(userlist.size()==1){
					idlist.add(userlist.get(0).get("_id").toString());
				}else{
					for(int k=0;k<userlist.size();k++){
						DBObject ud = userlist.get(k);
						String info = ud.get("info").toString();
						JSONObject infoj = new JSONObject(info);
						int rank = infoj.getInt("api_rank");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				if(dbc!=null){
					dbc.close();
				}
			}
		}
	}
	
	public static void handleName(String name,DBObject senkaUser,int tid,int count,int server,String token){
		DBCollection cl_senka = Util.db.getCollection("cl_senka_"+server);
		DBCollection cl_n_senka = Util.db.getCollection("cl_n_senka_"+server);
		String cmt = senkaUser.get("cmt").toString();
		int no = Integer.valueOf(senkaUser.get("no").toString());
		Date now = new Date();
		int month = now.getMonth();
		int date = now.getDate();
		DBCursor dbc = null;
		try {
			dbc = cl_senka.find(new BasicDBObject("name",name));
			ArrayList<DBObject> userlist = new ArrayList<>();
			while (dbc.hasNext()) {
				DBObject dbObject = (DBObject) dbc.next();
				userlist.add(dbObject);
			}
			if(userlist.size()==0){
				System.out.println("------------------------------------------");
				System.out.println("need fetch users");
			}else if(userlist.size()==1){
				totalcount++;
				int id = Integer.valueOf(userlist.get(0).get("_id").toString());
				cl_n_senka.update(new BasicDBObject("_id",name),new BasicDBObject("$set",new BasicDBObject("id",""+id).append("c", 1)));
				Collector.collectById(id, token, server);
			}else{
				ArrayList<Integer> fetchidlist = new ArrayList<>();
				ArrayList<Integer> mayfetchidlist = new ArrayList<>();
				for(int i=0;i<userlist.size();i++){
					DBObject user = userlist.get(i);
					int id = Integer.valueOf(user.get("_id").toString());
					BasicDBList exp = (BasicDBList)user.get("exp");
					ArrayList<DBObject> explist = new ArrayList<>();
					boolean willupdate = false;
					Date last = new Date(0);
					for(int k=0;k<exp.size();k++){
						DBObject expdata = (DBObject)exp.get(k);
						int expn = Integer.valueOf(expdata.get("d").toString());
						Date then = (Date) expdata.get("ts");
						if(then.getMonth()==month){
							explist.add(expdata);
							if(then.after(last)){
								last = then;
							}
						}else if(now.getTime()-then.getTime()>86400000L*30){
							exp.remove(k);
							willupdate = true;
						}else{
							
						}
					}
					String info = user.get("info").toString();
					JSONObject infoj = new JSONObject(info);
					int rank = infoj.getInt("api_rank");
					String tcmt = infoj.getString("api_cmt");
					if(now.getTime()-last.getTime()>86400000L*3*(rank-1)){
						fetchidlist.add(id);
					}else{
						mayfetchidlist.add(id);
					}
				}
				if(fetchidlist.size()>=count){
					totalcount+=fetchidlist.size();
					if(fetchidlist.size()==1){
						cl_n_senka.update(new BasicDBObject("_id",name),new BasicDBObject("$set",new BasicDBObject("id",""+fetchidlist.get(0)).append("c", count)));
						Collector.collectById(fetchidlist.get(0), token, server);
					}else{
						String saveidlist = "";
						for(int k=0;k<fetchidlist.size();k++){
							int id = fetchidlist.get(k);
							JSONObject userj = Collector.collectById(id, token, server);
							int rank = userj.getInt("api_rank");
							if(rank==1){
								if(saveidlist.equals("")){
									saveidlist = id+"";
								}else{
									saveidlist = saveidlist + "," + id;
								}
							}
						}
						cl_n_senka.update(new BasicDBObject("_id",name),new BasicDBObject("$set",new BasicDBObject("id",saveidlist).append("c", count)));
					}
				}else{
					System.out.println("(((((((((((((((((((((((((((((");
					System.out.println("no user matched,will fetch from mayidlist");
				}
			}
//			System.out.println(totalcount);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(dbc!=null){
				dbc.close();
			}
		}
	}
	
	
	public static void main(String[] args){
		DBCollection cl_n_senka = Util.db.getCollection("cl_n_senka_"+8);
		DBObject senkaData = cl_n_senka.findOne(new BasicDBObject("_id","雅"));
		DBObject senkaUser = (DBObject)((BasicDBList)senkaData.get("d5")).get(0);
		handleName("雅", senkaUser, 0, 1, 8, "8c3f8fa5533a18f92ac54c65022491eb2900125e");
	}
}
