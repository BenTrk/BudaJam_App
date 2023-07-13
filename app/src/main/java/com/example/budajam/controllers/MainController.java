package com.example.budajam.controllers;

import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.budajam.classes.Routes;
import com.example.budajam.models.MainModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Set;

public class MainController {
    public static void init(){
        MainModel.init();
    }
    public static boolean setUserAndRouteSynced(FirebaseAuth auth, FirebaseUser user) {
        return MainModel.setUserAndRouteSynced(auth, user);
    }

    public static void getNamesFromDatabase(String uid) {
        MainModel.getNamesFromDatabase(uid);
    }

    public static Set<String> getPlacesFromDatabase(Context context, LinearLayout routeLayout, Button buttonShowDropDown){
        return MainModel.getPlacesFromDatabase(context, routeLayout, buttonShowDropDown);
    }

    public static List<Routes> getRoutes(String name) {
        return MainModel.getRoutes(name);
    }
    public static String[] getNames() {
        return MainModel.getNames();
    }
}
