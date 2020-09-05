package com.example.volley_example;

import org.json.JSONException;
import org.json.JSONObject;

public class Mapdata {
    private String id;
    private String type;

    public Mapdata(){
        this.id = "";
        this.type = "";
    }

    public Mapdata(JSONObject job){
        try {
            this.id = job.getJSONObject("_id").getString("$oid");
            this.type = job.getString("type");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setId(String id){
        this.id = id;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getId(){
        return this.id;
    }

    public String getType(){
        return this.type;
    }
}
