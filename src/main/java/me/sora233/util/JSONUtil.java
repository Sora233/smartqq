package me.sora233.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Map;

/**
 * Created by Tsubasa on 17/10/21.
 */
public class JSONUtil {

    public static JSONObject MapToJson(Map<String, String> mp) {
        return new JSONObject(mp);
    }

    public static Map<String, String> JsonToMap(JSONObject object) {
        return object;
    }

    public static String JsonToString(JSONObject object) {
        return object.toJSONString();
    }

    public static JSONObject StringToJson(String json) throws ParseException {
        return (JSONObject) new JSONParser().parse(json);
    }
}
