package lib;

import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import senka.Collector;
import senka.Rank;

public class TimerTask {
	private static String token8="82686f39ebbfd68bc8ee6d3fc29b0c28a952aa59";
	private static int id8 = 8156938;
	private static String token19="70d709bb2a3cfa7027939452aae19d084509c846";
	private static int id19 = 19154349;
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
						Collector.collectByLastSenka(token8, 8);
					}
				}).start();
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						Collector.collectByLastSenka(token19, 19);
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
							Rank.runRankTask(token8, 8, id8);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Rank.runRankTask(token19, 19, 8156938);
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
	
	public static void main(String[] args){
		System.out.println(123);
	}
}
