/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.Reply;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.urlfetch.HTTPRequest;
import org.b3log.latke.urlfetch.HTTPResponse;
import org.b3log.latke.urlfetch.URLFetchService;
import org.b3log.latke.urlfetch.URLFetchServiceFactory;
import org.b3log.xiaov.util.XiaoVs;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Turing query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, May 29, 2016
 * @since 1.0.0
 */
@Service
public class TuringBot {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TuringBot.class.getName());

    /**
     * Turing Robot API.
     */
    private static final String TURING_API = "http://www.tuling123.com/openapi/api";

    /**
     * Turing Robot Key.
     */
    private static final String TURING_KEY = "9cca8707060f4432800730b2ddfb029b";

    /**
     * URL fetch service.
     */
//    private static final URLFetchService URL_FETCH_SVC = URLFetchServiceFactory.getURLFetchService();

    /**
     * Chat with Turing Robot.
     *
     * @param userName the specified user name
     * @param msg the specified message
     * @return robot returned message, return {@code null} if not found
     */
    public static String chat(final String userName, String msg) {
        if (StringUtils.isBlank(msg)) {
            return null;
        }

        if (StringUtils.isBlank(userName) || StringUtils.isBlank(msg)) {
            return null;
        }

        final HTTPRequest request = new HTTPRequest();
        request.setRequestMethod(HTTPRequestMethod.POST);

        try {
            JSONObject body = new JSONObject();
            body.put("key", TURING_KEY);
            body.put("info", msg);
            body.put("userid", userName);
            final String response = Post(TURING_API, body.toString());
            final JSONObject data = new JSONObject(response);
            System.out.println(data);
            final int code = data.optInt("code");

            switch (code) {
                case 40001:
                case 40002:
                case 40007:
                    LOGGER.log(Level.ERROR, data.optString("text"));

                    return null;
                case 40004:
                    return "聊累了，明天请早吧~";
                case 100000:
                    return data.optString("text");
                case 200000:
                    return data.optString("text") + " " + data.optString("url");
                case 302000:
                    String ret302000 = data.optString("text") + " ";
                    final JSONArray list302000 = data.optJSONArray("list");
                    final StringBuilder builder302000 = new StringBuilder();
                    for (int i = 0; i < list302000.length(); i++) {
                        final JSONObject news = list302000.optJSONObject(i);
                        builder302000.append(news.optString("article")).append(news.optString("detailurl"))
                                .append("\n\n");
                    }

                    return ret302000 + " " + builder302000.toString();
                case 308000:
                    String ret308000 = data.optString("text") + " ";
                    final JSONArray list308000 = data.optJSONArray("list");
                    final StringBuilder builder308000 = new StringBuilder();
                    for (int i = 0; i < list308000.length(); i++) {
                        final JSONObject news = list308000.optJSONObject(i);
                        builder308000.append(news.optString("name")).append(news.optString("detailurl"))
                                .append("\n\n");
                    }

                    return ret308000 + " " + builder308000.toString();
                default:
                    LOGGER.log(Level.WARN, "Turing Robot default return [" + data.toString(4) + "]");
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Chat with Turing Robot failed", e);
        }

        return null;
    }
    
	 public static String  Post(String urlStr,String param) throws Exception{
	         URL url = new URL(urlStr);
	         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	         conn.setRequestMethod("POST");
	         conn.setConnectTimeout(10000);
	         conn.setReadTimeout(12000);
	         conn.setDoInput(true);
	         conn.setDoOutput(true);
	         conn.setRequestProperty("Content-Type","application/json");
	         OutputStream os = conn.getOutputStream();     
	         os.write(param.toString().getBytes("utf-8"));     
	         os.close();         
	         BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
	         String line ;
	         String result ="";
	         while( (line =br.readLine()) != null ){
	             result += line;
	         }
	         br.close();
	         return result;
     }
    
    
    
    public static void main(String[] args){
	    System.out.println(123);
	    System.out.println(chat("121231233", "圆周率第100位是多少"));
    }
}
