package Api;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import lib.TimerTask;
import senka.Calculator;
import senka.Login;
import senka.Search;

public class ApiHandler {
	static{
		TimerTask.init();
	}
	
	public static void handleReq(final HttpServletRequest req, final HttpServletResponse resp){
		String pathinfo = req.getPathInfo();
		String path = pathinfo.substring(1);
		String queryString = req.getQueryString();
		Map<String, String[]> data = new HashMap<>();
		if(queryString!=null){
			data = ApiUtil.getParamsMap(queryString, "utf-8");
		}
		
		
		try {
			//resp.addHeader("Cache-Control", "no-store");
			String ret = handleData(path,data,req,resp);
			OutputStream output = null;
			output = resp.getOutputStream();
			IOUtils.write(ret.getBytes("utf-8"), output);
			output.flush();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				OutputStream output = null;
				output = resp.getOutputStream();
				IOUtils.write("{\"r\":121}", output);
				output.flush();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	public static String handleData(String path,Map<String, String[]> data,final HttpServletRequest req, final HttpServletResponse resp)throws Exception{
		String ret = "{\"r\":100}";
		System.out.println(path);
		System.out.println(data);
		if(path.equals("test")){
			ret = "{\"r\":110}";
		}
		if(path.equals("collect")){
			senka.Collector.runCollector(data);
			ret = "will run collector";
		}
		if(path.equals("rank")){
			senka.Rank.runRank(data);
			ret = "will run rank";
		}
		if(path.equals("seek")){
			resp.setCharacterEncoding("utf-8");
			resp.setContentType("text/plain");
			ret = Search.seekByName(data);
		}
		if(path.equals("ranktask")){
			TimerTask.rankTask();
			ret = "will run rank task";
		}
		if(path.equals("forcecollect")){
			TimerTask.collectorTask();
			ret = "will run rank task";
		}
		if(path.equals("calrank")){
			resp.setCharacterEncoding("utf-8");
			resp.setHeader("Access-Control-Allow-Origin", "*");
			ret = Calculator.calculator(data);
		}
		
		if(path.equals("easyrank")){
			resp.setCharacterEncoding("utf-8");
			resp.setContentType("text/plain");
			ret = Calculator.easyRank(data);
		}
		
		return ret;
	}
}
