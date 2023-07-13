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
    public static boolean setUserAndRouteSynced(FirebaseUser user) {
        return MainModel.setUserAndRouteSynced(user);
    }

    public static void getNamesFromDatabase(String uid) {
        MainModel.getNamesFromDatabase(uid);
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

    public static void addClimbToDatabase(String uid, String checkedName, String checkedStyle,
                                          String placeName, Routes mRouteItemToAdd, Context context) {
        MainModel.addClimbToTheDatabase(uid, checkedName, checkedStyle, placeName, mRouteItemToAdd, context);
    }
}
