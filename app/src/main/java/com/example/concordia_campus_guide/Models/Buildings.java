package com.example.concordia_campus_guide.Models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Buildings {

    private List<Building> BuildingsList;

    public Buildings(){
        BuildingsList = new ArrayList<Building>();
    }

    public List<Building> getBuildingsList(){
        return BuildingsList;
    }

    public Building getBuilding(String buildingCode){
        for(Building building: BuildingsList){
            if(building.getBuildingCode().equals(buildingCode)){
                return building;
            }
        }
        return null;
    }

    public Buildings(List<Building> buildingsList){
        this.BuildingsList = buildingsList;
    }

    public void setBuildingsList(List<Building> buildingsList){
        this.BuildingsList = buildingsList;
    }

    public JSONObject getGeoJson(){
        JSONObject toReturn = new JSONObject();
        try{
            toReturn.put("type", "FeatureCollection");
            toReturn.put("features", getInnerGeoJson());
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return toReturn;
    }

    public JSONArray getInnerGeoJson(){
        JSONArray features = new JSONArray();

        try{
            for(Building building: BuildingsList){
                JSONObject buildingGeoJson = building.getGeoJson();
                if(buildingGeoJson!=null) features.put(building.getGeoJson());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return features;
    }

}
