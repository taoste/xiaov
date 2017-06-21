package lib;

import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import senka.Collector;
import senka.Login;
import senka.Rank;
import senka.Util;

public class TimerTask {
	public static int id8 = 8156938;
	public static int id19 = 19154349;
	private static String user8 = "1127805853@qq.com";
	private static String user19 = "bot1@rewards.msharebox.com";
	private static String user20 = "bot2@rewards.msharebox.com";
	private static String user18 = "bot3@rewards.msharebox.com";
	private static String pass8 = "9876543210";
	private static String pass19 = "9876543210";
	private static String pass20 = "9876543210";
	private static String pass18 = "9876543210";

	static{
		Date  now = new Date(new Date().getTime()+(new Date().getTimezoneOffset()+480)*60000);
		int left1 = (int)(43200000-(now.getTime()-18002000)%43200000)/1000;
		System.out.println("--------------------------------");
		System.out.println("will get exp after "+left1/60+"minutes");
		ScheduledThreadPoolExecutor stpe1 = new ScheduledThreadPoolExecutor(5);
		stpe1.scheduleAtFixedRate(new Runnable() {
			public void run() {
				System.out.println(new Date());
				System.out.println("-----------------will get exp now------------");
				new Thread(new Runnable() {
					@Override
					public void run() {
						String token8 = getToken(8);
						if(token8.length()>2){
							Collector.collectByLastSenka(token8, 8);
						}
					}
				}).start();
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						String token19 = getToken(19);
						if(token19.length()>2){
							Collector.collectByLastSenka(token19, 19);
						}
					}
				}).start();
				
			}
		}, left1, 43200, TimeUnit.SECONDS);
		
		int left2 = (int)(43200000-(now.getTime()-21700000)%43200000)/1000;
		System.out.println("will get senka after "+left2/60+"minutes");
		ScheduledThreadPoolExecutor stpe2 = new ScheduledThreadPoolExecutor(5);
		stpe2.scheduleAtFixedRate(new Runnable() {
			public void run() {
				System.out.println(new Date());
				System.out.println("-----------------will get senka now------------");
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							String token8 = getToken(8);
							if(token8.length()>2){
								Rank.runRankTask(token8, 8, id8);
							}
							
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							String token19 = getToken(19);
							if(token19.length()>2){
								Rank.runRankTask(token19, 19, id19);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
				

			}
		}, left2, 43200, TimeUnit.SECONDS);
		System.out.println("--------------------------------");
	}
	
	public static void init(){

	}

	
	public static String getToken(int server){
		DBCollection cl_token = Util.db.getCollection("cl_token");
		BasicDBObject query = new BasicDBObject("_id",server);
		DBObject tokenData = cl_token.findOne(query);
		String token;
		Date now = new Date();
		if(tokenData==null){
			token = login(server);
			cl_token.save(new BasicDBObject("_id",server).append("token", token).append("ts", now));
		}else{
			Date then = (Date)tokenData.get("ts");
			if(now.getTime()-then.getTime()>43200000){
				token = login(server);
				cl_token.save(new BasicDBObject("_id",server).append("token", token).append("ts", now));
			}else{
				token = tokenData.get("token").toString();
			}
		}
		return token;
	}
	
	public static String login(int server){
		String token="";
		if(server==8){
			token=Login.login(user8,pass8);
		}else if(server==19){
			token=Login.login(user19,pass19);
		}else if(server==18){
			token=Login.login(user18,pass18);
		}else if(server==20){
			token=Login.login(user20,pass20);
		}
		return token;
	}
	
	
	public static void main(String[] args){
		System.out.println(123);
		System.out.println(getToken(8));
	}
}
