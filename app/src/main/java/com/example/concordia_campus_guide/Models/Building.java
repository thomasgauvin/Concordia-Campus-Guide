package com.example.concordia_campus_guide.Models;

import android.text.TextUtils;

import java.util.List;

public class Building {

    private String Campus;
    private String Building_Code;
    private String Building_Long_Name;
    private String Address;
    private List<String> Departments;
    private List<String> Services;

    /**
     * Constructor
     */
    public Building(){
    }

    /**
     * Constructor
     */
    public Building(String campus, String buildingCode, String building_Long_Name, String address, List<String> departments, List<String> services){
        this.Address = address;
        this.Campus = campus;
        this.Building_Code = buildingCode;
        this.Building_Long_Name = building_Long_Name;
        this.Departments = departments;
        this.Services = services;
    }

    /**
     * Sets departments param to Departments attribute
     *
     * @param departments represents list of strings of department names
     */
    public void setDepartments(List<String> departments) {
        Departments = departments;
    }

    /**
     * Sets services param to Services attribute
     *
     * @param services represents list of strings of service names
     */
    public void setServices(List<String> services) {
        Services = services;
    }

    /**
     * Sets campus param to Departments attribute
     *
     * @param campus represents string campus name
     */
    public void setCampus(String campus) {
        Campus = campus;
    }

    /**
     * Sets building_Code param to Building_Code attribute
     *
     * @param building_Code represents string of building code
     */
    public void setBuilding_Code(String building_Code) {
        Building_Code = building_Code;
    }

    /**
     * Sets building_Long_Name param to building_Long_Name attribute
     *
     * @param building_Long_Name represents string of building long name
     */
    public void setBuilding_Long_Name(String building_Long_Name) {
        Building_Long_Name = building_Long_Name;
    }

    /**
     * Sets address param to Address attribute
     *
     * @param address represents string of address
     */
    public void setAddress(String address) {
        Address = address;
    }

    /**
     * @return campus as string
     */
    public String getCampus() {
        return Campus;
    }

    /**
     * @return building code as string
     */
    public String getBuilding_Code() {
        return Building_Code;
    }

    /**
     * @return building long name as string
     */
    public String getBuilding_Long_Name() {
        return Building_Long_Name;
    }

    /**
     * @return building address as string
     */
    public String getAddress() {
        return Address;
    }

    /**
     * @return services as list of strings
     */
    public List<String> getServices() {
        return Services;
    }

    /**
     * Returns services joined in a string for easy display
     *
     * @return string of all services joined by comma ", "
     */
    public String getServicesString(){
        if(Services.isEmpty()){
            return "";
        }
        return TextUtils.join(", ", Services);
    }

    /**
     * @return departments as list of strings
     */
    public List<String> getDepartments() {
        return Departments;
    }

    /**
     * Returns departments joined in a string for easy display
     *
     * @return string of all departments joined by comma ", "
     */
    public String getDepartmentsString(){
        if(Departments.isEmpty()){
            return "";
        }
        return TextUtils.join(", ", Departments);
    }
}