package com.example.vision;

import android.app.MediaRouteActionProvider;

import java.util.ArrayList;

public class Service {

    private double longitude;
    private double latitude;

    //private jsonObject Array
    private ArrayList<Mapdata> mapdataArrayList;
    //instances data structure class;


    public void startService(){
        mapdataArrayList = new ArrayList<Mapdata>();
    }


    public void setLongitude(double longitude){
        this.longitude = longitude;
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    public double getLongitude(){
        return this.longitude;
    }

    public double getLatitude(){
        return this.latitude;
    }

    public void setMapdataArrayList(ArrayList<Mapdata> mapList){
        this.mapdataArrayList = mapList;
    }

    public void push_backMapdata(Mapdata md){
        mapdataArrayList.add(md);
    }

    // 얕 복사본을 넘겨준다.은 .. -> Mapdata 클래스의 깊은 복사자를 만들어야되는데 귀찮다..ㅜ
    public ArrayList<Mapdata> getMapdataArrayList() {
        return this.mapdataArrayList;
    }

    public Mapdata getMapdataFromIdx(int index){
        return this.mapdataArrayList.get(index);
    }

}




