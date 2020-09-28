package com.example.vision;

import org.json.JSONException;
import org.json.JSONObject;

public class Sector {
    private String id;
    private String name;
    private String type;

    public Sector(){

    }

    public Sector(JSONObject job){
        try {
            this.id = job.getString("_id");
            this.name = job.getString("name");
            this.type = job.getString("type");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setId(String id){
        this.id = id;
    }

    public void setName(String name) { this.name = name; }

    public void setType(String type){
        this.type = type;
    }

    public String getId(){
        return this.id;
    }

    public String getName() { return this.name; }

    public String getType(){
        return this.type;
    }
}
