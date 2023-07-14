package com.example.budajam.controllers;

import android.content.Context;

import com.example.budajam.classes.Routes;
import com.example.budajam.models.MainModel;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Set;

public class MainController {
    public static void init(){
        MainModel.init();
    }
    public static FirebaseUser authentication(){
        return MainModel.authentication();
    }

    public static void signOut(){
        MainModel.signOut();
    }
    public static boolean setUserAndRouteSynced() {
        return MainModel.setUserAndRouteSynced();
    }

    public static void getNamesFromDatabase() {
        MainModel.getNamesFromDatabase();
    }

    public static Set<String> getPlacesFromDatabase(){
        return MainModel.getPlacesFromDatabase();
    }

    public static List<Routes> getRoutes(String name) {
        return MainModel.getRoutes(name);
    }
    public static String[] getNames() {
        return MainModel.getNames();
    }

    public static void addClimbToDatabase(String checkedName, String checkedStyle,
                                          String placeName, Routes mRouteItemToAdd, Context context) {
        MainModel.addClimbToTheDatabase(checkedName, checkedStyle, placeName, mRouteItemToAdd, context);
    }
}
