package com.example.budajam.controllers;

import com.example.budajam.classes.Route;
import com.example.budajam.interfaces.OnGetClimbDataListener;
import com.example.budajam.interfaces.OnGetPointsListener;
import com.example.budajam.models.CheckOutModel;
import com.example.budajam.models.initModel;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class CheckOutController {

    public static double getTeamPoints(){
        return CheckOutModel.getTeamPoints();
    }
    public static boolean areTherePlaces(String selectedName){
        return CheckOutModel.areTherePlaces(selectedName);
    }
    public static String[] getClimbPlaces(String selectedName){
        return CheckOutModel.getClimbPlaces(selectedName);
    }

    public static double removeClimb(Route route, String climberName, String place){
        return CheckOutModel.removeClimb(route, climberName, place);
    }
    public static List<Route> getClimbedRoutesPerClimber(String selectedName, String place){
        return CheckOutModel.getClimbedRoutesPerClimber(selectedName, place);
    }

    public static FirebaseUser authenticate() {
        initModel.initAuthentication();
        return initModel.getUser();
    }
}
