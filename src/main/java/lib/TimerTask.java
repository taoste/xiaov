package lib;

import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import senka.Collector;
import senka.Rank;

public class TimerTask {
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
				Collector.collectByLastSenka("8c3f8fa5533a18f92ac54c65022491eb2900125e", 8);

			}
		}, left1, 43200, TimeUnit.SECONDS);
		
		int left2 = (int)(43200000-(now.getTime()-21700000)%43200000)/1000;
		System.out.println("will get senka after "+left2/60+"minutes");
		ScheduledThreadPoolExecutor stpe2 = new ScheduledThreadPoolExecutor(5);
		stpe2.scheduleAtFixedRate(new Runnable() {
			public void run() {
				System.out.println(new Date());
				System.out.println("-----------------will get senka now------------");
				try {
					Rank.runRankTask("8c3f8fa5533a18f92ac54c65022491eb2900125e", 8, 8156938);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, left2, 43200, TimeUnit.SECONDS);
		System.out.println("--------------------------------");
	}
	
	public static void init(){

	}
	
	public static void main(String[] args){
		System.out.println(123);
	}
}
