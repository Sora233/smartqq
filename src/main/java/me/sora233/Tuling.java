package me.sora233;

import com.google.common.collect.Lists;
import com.scienjus.smartqq.callback.MessageCallback;
import com.scienjus.smartqq.client.SmartQQClient;
import com.scienjus.smartqq.model.DiscussMessage;
import com.scienjus.smartqq.model.GroupMessage;
import com.scienjus.smartqq.model.Message;
import me.sora233.util.JSONUtil;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Tsubasa on 17/10/20.
 */
public class Tuling {
    private static final String APIkey = "ee4f1777f49941dd9f9d95beeaf129bf";
    private static final String APIAddr = "http://www.tuling123.com/openapi/api";
    private static final Logger logger = Logger.getLogger(Tuling.class);
    List<String> keyword = Lists.newArrayList("@kg", "@1561991863");

    private SmartQQClient client;

    public Tuling() {

        this.client = new SmartQQClient(new MessageCallback() {
            public void onMessage(Message message) {
                logger.info("userId: " + message.getUserId() + ", say: " + message.getContent());
            }

            public void onDiscussMessage(DiscussMessage message) {
            }

            public void onGroupMessage(GroupMessage message) {
                String content = message.getContent();
                logger.info("userId: " + message.getUserId() + ", say: " + message.getContent());
                for (String kw : keyword) {
                    if (content.contains(kw)) {
                        logger.info("be at in " + message.getGroupId() + "content: " + message.getContent());
                        GroupQuery(content, message.getUserId(), message.getGroupId());
                        break;
                    }
                }
            }
        });

    }

    public void GroupQuery(final String q, final long userId, final long groupId) {
        (new Thread(new Runnable() {
            public void run() {
                try {
                    URL url = new URL(APIAddr);// 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.setInstanceFollowRedirects(true);
                    conn.setRequestMethod("POST"); // 设置请求方式
                    conn.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
                    conn.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
                    conn.connect();
                    OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8"); // utf-8编码
                    out.append(encode(q, userId));
                    out.flush();
                    out.close();

                    int code = conn.getResponseCode();
                    BufferedReader in;
                    if (code != 200) {
                        in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    } else {
                        in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    }

                    String line;
                    StringBuilder resultBuilder = new StringBuilder();
                    while ((line = in.readLine()) != null) {
                        resultBuilder.append(line);
                    }
                    Map<String, Object> mp = JSONUtil.StringToJson(resultBuilder.toString());
                    if (code != 200) {
                        logger.error("Tuling status code: " + code);
                        logger.error("Tuling error info: " + mp.get("text"));
                        return;
                    }
                    logger.info("code 200");
                    Long respCode = (Long) mp.get("code");
                    if (respCode == 100000L) { // 文本类
                        DealText(groupId, (String) mp.get("text"));
                    } else if (respCode == 200000L) {
                        // TODO 链接类
                    } else if (respCode == 302000L) {
                        // TODO 新闻类
                    } else if (respCode == 308000L) {
                        // TODO 菜谱类
                    } else {
                        logger.error("can't recognize the api code");
                    }

                } catch (ParseException e) {
                    logger.error("json parse faild!");
                } catch (IOException e) {
                    logger.error("GroupQuery faild!");
                }
            }
        })).start();
    }

    public SmartQQClient getClient() {
        return client;
    }

    public void setClient(SmartQQClient client) {
        this.client = client;
    }

    private void DealText(long groupId, String msg) {
        client.sendMessageToGroup(groupId, msg);
        logger.info("send to " + groupId + ", content: " + msg);
    }

    private String encode(String q, long userId) {
        Map<String, String> mp = new HashMap<String, String>();
        mp.put("key", APIkey);
        mp.put("info", q);
        mp.put("userid", String.valueOf(userId));
        return JSONObject.toJSONString(mp);
    }

}
