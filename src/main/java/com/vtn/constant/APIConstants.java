package com.vtn.constant;

public class APIConstants {
    private APIConstants() {
    }

    // Domain api use for all lc import
    public static final String API_VIET_BUS = "/api/v1/app";

    //Auth
    public static final String AUTH = "/auth";
    public static final String API_LOGIN = API_VIET_BUS + AUTH + "/login";

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

    //Route
    public static final String ROUTE = "/route";
    public static final String API_GET_ALL_ROUTE = API_VIET_BUS + ROUTE + "/get-all";
    public static final String API_CREATE_ROUTE = API_VIET_BUS + ROUTE + "/create";
    public static final String API_DELETE_ROUTE = API_VIET_BUS + ROUTE + "/delete";

    //Trip
    public static final String TRIP = "/trip";
    public static final String API_GET_ALL_TRIP = API_VIET_BUS + TRIP + "/get-all";
    public static final String API_CREATE_TRIP = API_VIET_BUS + TRIP + "/create";
    public static final String API_UPDATE_TRIP = API_VIET_BUS + TRIP + "/update";
    public static final String API_DELETE_TRIP = API_VIET_BUS + TRIP + "/delete";

    //Account
    public static final String ACCOUNT = "/account";
    public static final String API_GET_ALL_ACCOUNT = API_VIET_BUS + ACCOUNT + "/get-all";
    public static final String API_CREATE_ACCOUNT = API_VIET_BUS + ACCOUNT + "/create";
    public static final String API_GET_ALL_ACCOUNT_BY_ROLE = API_VIET_BUS + ACCOUNT + "/get-all-by-role";

}
