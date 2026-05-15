package com.vtn.constant;

public class APIConstants {
    private APIConstants() {
    }

    // Domain api
    public static final String API_VIET_BUS = "/api/v1/app";

    //Auth
    public static final String AUTH = "/auth";
    public static final String API_LOGIN = API_VIET_BUS + AUTH + "/login";
    public static final String API_REFRESH_TOKEN = API_VIET_BUS + AUTH + "/refresh";
    public static final String API_LOGOUT = API_VIET_BUS + AUTH + "/logout";
    public static final String API_LOGOUT_ALL = API_VIET_BUS + AUTH + "/logout-all";

    //Account
    public static final String ACCOUNT = "/account";
    public static final String API_GET_ALL_ACCOUNT = API_VIET_BUS + ACCOUNT + "/get-all";
    public static final String API_CREATE_ACCOUNT = API_VIET_BUS + ACCOUNT + "/create";
    public static final String API_UPDATE_ACCOUNT = API_VIET_BUS + ACCOUNT + "/update";
    public static final String API_GET_ALL_ACCOUNT_BY_ROLE = API_VIET_BUS + ACCOUNT + "/get-all-by-role";

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
    public static final String API_GET_ALL_TRIP_OPEN_FOR_BOOKING = API_VIET_BUS + TRIP + "/get-all-open-booking";
    public static final String API_GET_TRIP_BY_TRIP_ID = API_VIET_BUS + TRIP + "/get-by-id";
    public static final String API_CREATE_TRIP = API_VIET_BUS + TRIP + "/create";
    public static final String API_UPDATE_TRIP = API_VIET_BUS + TRIP + "/update";

    //Trip History
    public static final String TRIP_HISTORY = "/trip-history";
    public static final String API_GET_TRIP_HISTORY_BY_TRIP_ID = API_VIET_BUS + TRIP_HISTORY + "/get-by-trip-id";

    //Seat
    public static final String SEAT = "/seat";
    public static final String API_GET_SEAT_BY_VEHICLE_ID = API_VIET_BUS + SEAT + "/get-by-vehicle-id";

    //Trip-Seat
    public static final String TRIP_SEAT = "/trip-seat";
    public static final String API_COUNT_TRIP_SEAT_SOLD_BY_TRIP_ID = API_VIET_BUS + TRIP_SEAT + "/count-trip-seat-sold-by-trip-id";
    public static final String API_GET_ALL_TRIP_SEAT_BY_TRIP_ID = API_VIET_BUS + TRIP_SEAT + "/get-all-trip-seat-by-trip-id";
    public static final String API_GET_ALL_TRIP_SEAT_AVAILABLE_BY_TRIP_ID = API_VIET_BUS + TRIP_SEAT + "/get-all-trip-seat-available-by-trip-id";

    //Station
    public static final String STATION = "/station";
    public static final String API_GET_ALL_STATION = API_VIET_BUS + STATION + "/get-all";

    //Ticket
    public static final String TICKET = "/ticket";
    public static final String API_CREATE_TICKET = API_VIET_BUS + TICKET + "/create";
    public static final String API_UPDATE_TICKET = API_VIET_BUS + TICKET + "/update";
    public static final String API_GET_ALL_TICKET = API_VIET_BUS + TICKET + "/get-all";
    public static final String API_GET_ALL_TICKET_UNPAID = API_VIET_BUS + TICKET + "/get-all-ticket-unpaid";
    public static final String API_GET_ALL_TICKET_BY_TRIP_ID = API_VIET_BUS + TICKET + "/get-all-ticket-by-trip-id";

    //Payment
    public static final String PAYMENT = "/payment";
    public static final String API_GET_ALL_PAYMENT = API_VIET_BUS + PAYMENT + "/get-all";

    //Passenger
    public static final String PASSENGER = "/passenger";
    public static final String API_GET_ALL_PASSENGER = API_VIET_BUS + PASSENGER + "/get-all";
    public static final String API_CREATE_PASSENGER = API_VIET_BUS + PASSENGER + "/create";
    public static final String API_GET_PASSENGER = API_VIET_BUS + PASSENGER + "/get";
    public static final String API_UPDATE_PASSENGER = API_VIET_BUS + PASSENGER + "/update";
    public static final String API_DELETE_PASSENGER = API_VIET_BUS + PASSENGER + "/delete";
    public static final String API_SEARCH_PASSENGER = API_VIET_BUS + PASSENGER + "/search";

    //Statistics
    public static final String STATISTICS = "/statistics";
    public static final String API_GET_REVENUE_BY_MONTH = API_VIET_BUS + STATISTICS + "/get-revenue-by-month";
    public static final String API_GET_TOTAL_TICKET_BY_MONTH = API_VIET_BUS + STATISTICS + "/total-ticket-by-month";
    public static final String API_GET_TOTAL_TRIP_BY_MONTH = API_VIET_BUS + STATISTICS + "/total-trip-by-month";
    public static final String API_GET_TOTAL_PASSENGER_BY_MONTH = API_VIET_BUS + STATISTICS + "/total-passenger-by-month";
    public static final String API_GET_TOTAL_VEHICLE = API_VIET_BUS + STATISTICS + "/total-vehicle";
    public static final String API_GET_All_TRIP_DEPARTED = API_VIET_BUS + STATISTICS + "/get-all-trip-departed";
    public static final String API_GET_TOTAL_TICKET_PER_ROUTE = API_VIET_BUS + STATISTICS + "/total-ticket-per-route";
}
