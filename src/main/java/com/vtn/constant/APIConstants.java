package com.vtn.constant;

public class APIConstants {
    private APIConstants() {
    }

    // Domain api use for all lc import
    public static final String API_VIET_BUS = "/api/v1/app";

    //Auth
    public static final String AUTH = "/auth";
    public static final String API_LOGIN = API_VIET_BUS + AUTH + "/login";
    public static final String API_REGISTER = API_VIET_BUS + AUTH + "/register";

    //Employee
    public static final String EMPLOYEE = "/employee";
    public static final String API_GET_ALL_EMPLOYEES = API_VIET_BUS + EMPLOYEE + "/get-all";
    public static final String API_CREATE_EMPLOYEE = API_VIET_BUS + EMPLOYEE + "/create";
    public static final String API_UPDATE_EMPLOYEE = API_VIET_BUS + EMPLOYEE + "/update";
    public static final String API_DELETE_EMPLOYEE = API_VIET_BUS + EMPLOYEE + "/delete";

    //Bus
    public static final String BUS = "/bus";
    public static final String API_GET_ALL_BUSES = API_VIET_BUS + BUS + "/get-all";
    public static final String API_CREATE_BUS = API_VIET_BUS + BUS + "/create";
    public static final String API_UPDATE_BUS = API_VIET_BUS + BUS + "/update";
    public static final String API_DELETE_BUS = API_VIET_BUS + BUS + "/delete";
}
