package com.tencent.qcloudcspsample;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void useAppContext() {


        Map<String, String> headers = new HashMap<>();
        headers.put("user-agent", "android-cos");
        headers.put("server", "tencent");

        Map<String, String> params = new HashMap<>();
        params.put("part", "1");
        params.put("number", "10");

        try {
            System.out.println(signField2Json("PUT", "http", "www.qcloud.com", "/path/test.txt",
                    headers, params));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public String signField2Json(String method, String schema, String host, String path,
                                 Map<String, String> headers, Map<String, String> params) throws JSONException {

        JSONObject signJson = new JSONObject();
        signJson.put("method", method);
        signJson.put("schema", schema);
        signJson.put("host", host);
        signJson.put("path", path);

        JSONObject headersJSON = new JSONObject(headers);
        signJson.put("headers", headersJSON);

        JSONObject paramsJSON = new JSONObject(params);
        signJson.put("params", paramsJSON);

        return signJson.toString();
    }
}
