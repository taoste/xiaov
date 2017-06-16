package senka;

import java.util.Date;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class Util {
	public static DB db=null;
	static{
		init();
	}
	public static void init(){
		String mongouri="mongodb://192.168.17.52:27050/?replicaSet=rs0";
		try {
			MongoClient mongoClient = new MongoClient(new MongoClientURI(mongouri));
			db = mongoClient.getDB("db_senka");
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public static int getRankDateNo(Date dat){
		  Date now = new Date(dat.getTime()+(dat.getTimezoneOffset()+480)*60000);
		  int date = now.getDate();
		  int hour = now.getHours();
		  if(hour<2){
		    date = date -1;
		    hour = hour + 24;
		  }
		  int no = (date-1)*2+((hour>=14)?1:0);
		  return no;
	}
}
