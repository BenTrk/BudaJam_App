package com.example.budajam.controllers;

import android.app.Dialog;
import android.content.Context;

import com.example.budajam.classes.Routes;
import com.example.budajam.models.CheckOutModel;
import com.example.budajam.models.MainModel;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class CheckOutController {

    public static boolean setUserRoutesSynced() {
        return CheckOutModel.setUserRoutesSynced();
    }

    public static void init(CheckOutModel.OnGetDataListener listener) {
        CheckOutModel.init(listener);
    }
    public static void getTeamPoints(CheckOutModel.OnGetDataListener listener){
        CheckOutModel.getTeamPoints(listener);
    }
    public static boolean areTherePlaces(String selectedName){
        return CheckOutModel.areTherePlaces(selectedName);
    }
    public static String[] getClimbPlaces(String selectedName){
        return CheckOutModel.getClimbPlaces(selectedName);
    }

    public static void getClimbedRoutes(String selectedName, String place, CheckOutModel.OnGetDataListener listener) {
        CheckOutModel.getClimbedRoutes(selectedName, place, listener);
    }

    public static void removeClimb(Routes route, String climberName, String place){
        CheckOutModel.removeClimb(route, climberName, place);
    }
}
