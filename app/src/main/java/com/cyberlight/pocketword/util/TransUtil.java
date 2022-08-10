package com.cyberlight.pocketword.util;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TransUtil {

    private static final String TAG = "TransUtil";
    public static String APP_ID = "20220703001262714";
    public static String SECURITY_KEY = "8V0S_CxAsK15L5oWmKof";

    public synchronized static String translate(String query, String from, String to) {
        TransApi api = new TransApi(APP_ID, SECURITY_KEY);
        String json = api.getTransResult(query, from, to);
        // json为空说明翻译结果获取失败
        if (!TextUtils.isEmpty(json)) {
            try {
                JSONObject obj = new JSONObject(json);
                JSONArray jarr = obj.optJSONArray("trans_result");
                if (jarr != null && jarr.length() > 0) {
                    JSONObject jobj = jarr.getJSONObject(0);
                    return jobj.optString("dst");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
