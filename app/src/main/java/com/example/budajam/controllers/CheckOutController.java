package com.example.budajam.controllers;

import com.example.budajam.classes.Routes;
import com.example.budajam.interfaces.OnGetClimbDataListener;
import com.example.budajam.interfaces.OnGetPointsListener;
import com.example.budajam.models.CheckOutModel;

import java.util.List;

public class CheckOutController {

    public static boolean setUserRoutesSynced() {
        return CheckOutModel.setUserRoutesSynced();
    }

    public static void init(OnGetClimbDataListener listener) {
        CheckOutModel.init(listener);
    }
    public static void getTeamPoints(OnGetPointsListener listener){
        CheckOutModel.getTeamPoints(listener);
    }
    public static boolean areTherePlaces(String selectedName){
        return CheckOutModel.areTherePlaces(selectedName);
    }
    public static String[] getClimbPlaces(String selectedName){
        return CheckOutModel.getClimbPlaces(selectedName);
    }

    public static void removeClimb(Routes route, String climberName, String place){
        CheckOutModel.removeClimb(route, climberName, place);
    }
    public static List<Routes> getClimbedRoutesPerClimber(String selectedName, String place){
        return CheckOutModel.getClimbedRoutesPerClimber(selectedName, place);
    }
}
