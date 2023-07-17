package com.example.budajam.controllers;

import android.content.Context;

import com.example.budajam.classes.PlaceWithRoutes;
import com.example.budajam.classes.Route;
import com.example.budajam.models.MainModel;
import com.example.budajam.models.initModel;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MainController {
    public static boolean init(){
        return initModel.onCreate();
    }
    public static void signOut(){
        MainModel.signOut();
    }

    public static List<PlaceWithRoutes> getPlacesFromDatabase(){
        return initModel.getPlacesWithRoutes();
    }

    public static List<Route> getRoutes(String name) {
        return MainModel.getRoutes(name);
    }
    public static String[] getNames() {
        return MainModel.getNames();
    }

    public static void addClimbToDatabase(String checkedName, String checkedStyle,
                                          String placeName, Route mRouteItemToAdd, Context context) {
        double teamPoints = initModel.getTeamData().teamPoints;
        MainModel.addClimbToTheDatabase(checkedName, checkedStyle, placeName, mRouteItemToAdd, context, teamPoints);
    }

    public static FirebaseUser authentication() {
        initModel.initAuthentication();
        return initModel.getUser();
    }
}
