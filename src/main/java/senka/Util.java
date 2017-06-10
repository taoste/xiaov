package senka;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class Util {
	public static DB db=null;
	static{
		init();
	}
	public static void init(){
		String mongouri = new String();
			mongouri="mongodb://127.0.0.1:27050/?replicaSet=rs0";
		try {
			MongoClient mongoClient = new MongoClient(new MongoClientURI(mongouri));
			db = mongoClient.getDB("db_senka");
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
}
