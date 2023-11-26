package com.musalasoft.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class ResponseCodes {

    public static final String DISPATCH_OK = "S00";
    public static final String STATUS_OK = "S01";
    public static final String MEDICATION_SENT = "S02";
    public static final String MEDICATION_DISPATCHED = "S03";
    public static final String ITEMS_RECEIVED = "00";
    public static final String SUCCESS = "000";
    public static final String REGISTERED_OK = "200";
    public static final String ALREADY_REGISTERED = "202";
    public static final String CANNOT_BE_INITIATED = "E02";
    public static final String INTERFACE_ERROR = "E04";
    public static final String INVALID_TIME = "E05";
    public static final String WEIGHT_LIMIT_EXCEEDED= "E06";
    public static final String LOW_BATTERY_CAPACITY= "E07";
    public static final String CHARGING_BATTERY= "E08";
    public static final String LOADING= "E09";
    public static final String BUSY= "E10";
    public static final String EMPTY_DRONE= "E11";
    public static final String DRONE_NOT_FOUND= "E12";
    public static final String DRONES_FETCHED= "300";
    public static final String AVAIL_DRONES= "100";
    public static final String DELIVERING = "E13";
    public static final String FULLY_CHARGED = "E14";
    public static final String UNAVAILABLE = "E15";
    public static final String DRONE_IDLE = "E16";
    public static final String INVALID_DATA = "E17";
    public static final String BAD_INPUT_DATA = "E18";
    public static final String INVALID_SERIAL_NUMBER= "E19";
    public static final String INVALID_DELIVERY_TIME= "E20";
    public static final String DRONES_UNAVAILABLE= "E21";
    public static final String NO_DRONES_FOUND= "E22";

    private static Map<String, ResourceBundle> resourceMap = new HashMap<String, ResourceBundle>();

    public static String getErrorMessage(String errorCode) {
        return getErrorMessage(errorCode, null);
    }

    public static String getErrorMessage(String errorCode, String langCode) {
        if (langCode == null) {
            langCode = Locale.ENGLISH.getLanguage();
        }

        ResourceBundle resource = resourceMap.get(langCode);
        if (resource == null) {
            resource = ResourceBundle.getBundle("ResponseMessages", new Locale(langCode));
            resourceMap.put(langCode, resource);
        }

        String[] errors = errorCode.split("-");
        return resource.getString(errors[0].trim())
                + ((errors.length > 1 && !errors[1].trim().isEmpty()) ? (" - " + errors[1].trim()) : "");
    }
}
