package com.example.vision;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class JsonRequest extends JsonArrayRequest {
    //web 주소
    private static final String REQUEST_URL = IpPath.WEBIP + "/mapdata/sangsu";
    private static JSONObject jsonBody = new JSONObject();

    // string,string 해쉬맵
    private byte[] body;


    //생성자
    public JsonRequest(byte[] content, Response.Listener<JSONArray> listener) {
        //post형식으로 전송
        super(Method.GET,REQUEST_URL,null,listener,null);
        body = content;
    }


    @Override
    public String getBodyContentType() {
        return super.getBodyContentType();
    }

    @Override
    public byte[] getBody() {
        JSONObject j = new JSONObject();
        try {
            j.put("android","client");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return j.toString().getBytes();
    }

}
