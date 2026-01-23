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
    public static final String API_GET_ALL_EMPLOYEES_BY_POSITION = API_VIET_BUS + EMPLOYEE + "/get-all-by-position";
    public static final String API_CREATE_EMPLOYEE = API_VIET_BUS + EMPLOYEE + "/create";
    public static final String API_UPDATE_EMPLOYEE = API_VIET_BUS + EMPLOYEE + "/update";
    public static final String API_DELETE_EMPLOYEE = API_VIET_BUS + EMPLOYEE + "/delete";

    //Vehicle
    public static final String VEHICLE = "/vehicle";
    public static final String API_GET_ALL_VEHICLES = API_VIET_BUS + VEHICLE + "/get-all";
    public static final String API_GET_ALL_VEHICLES_ACTIVE = API_VIET_BUS + VEHICLE + "/get-all-active";
    public static final String API_GET_VEHICLE_BY_VEHICLE_ID = API_VIET_BUS + VEHICLE + "/get-by-id";
    public static final String API_CREATE_VEHICLE = API_VIET_BUS + VEHICLE + "/create";
    public static final String API_UPDATE_VEHICLE = API_VIET_BUS + VEHICLE + "/update";
    public static final String API_DELETE_VEHICLE = API_VIET_BUS + VEHICLE + "/delete";

    //Route
    public static final String ROUTE = "/route";
    public static final String API_GET_ALL_ROUTE = API_VIET_BUS + ROUTE + "/get-all";
    public static final String API_GET_ALL_ROUTE_ACTIVE = API_VIET_BUS + ROUTE + "/get-all-active";
    public static final String API_CREATE_ROUTE = API_VIET_BUS + ROUTE + "/create";
    public static final String API_UPDATE_ROUTE = API_VIET_BUS + ROUTE + "/update";
    public static final String API_DELETE_ROUTE = API_VIET_BUS + ROUTE + "/delete";

    //Trip
    public static final String TRIP = "/trip";
    public static final String API_GET_ALL_TRIP = API_VIET_BUS + TRIP + "/get-all";
    public static final String API_GET_TRIP_BY_TRIP_ID = API_VIET_BUS + TRIP + "/get-by-id";
    public static final String API_CREATE_TRIP = API_VIET_BUS + TRIP + "/create";
    public static final String API_UPDATE_TRIP = API_VIET_BUS + TRIP + "/update";
    public static final String API_DELETE_TRIP = API_VIET_BUS + TRIP + "/delete";

    //Account
    public static final String ACCOUNT = "/account";
    public static final String API_GET_ALL_ACCOUNT = API_VIET_BUS + ACCOUNT + "/get-all";
    public static final String API_CREATE_ACCOUNT = API_VIET_BUS + ACCOUNT + "/create";
    public static final String API_GET_ALL_ACCOUNT_BY_ROLE = API_VIET_BUS + ACCOUNT + "/get-all-by-role";

    //Seat
    public static final String SEAT = "/seat";
    public static final String API_GET_SEAT_BY_VEHICLE_ID = API_VIET_BUS + SEAT + "/get-by-vehicle-id";

    //Trip-Seat
    public static final String TRIP_SEAT = "/trip-seat";
    public static final String API_COUNT_TRIP_SEAT_SOLD_BY_TRIP_ID = API_VIET_BUS + TRIP_SEAT + "/count-trip-seat-sold-by-trip-id";
    public static final String API_GET_ALL_TRIP_SEAT_BY_TRIP_ID = API_VIET_BUS + TRIP_SEAT + "/get-all-trip-seat-by-trip-id";

    //Station
    public static final String STATION = "/station";
    public static final String API_GET_ALL_STATION = API_VIET_BUS + STATION + "/get-all";

}
