package com.example.budajam.controllers;

import android.content.Context;

import com.example.budajam.classes.ExtraActivity;
import com.example.budajam.models.ExtraPointsModel;
import com.example.budajam.models.initModel;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ExtraPointsController {
    public static FirebaseUser authenticate(){
        initModel.initAuthentication();
        return initModel.getUser();
    }

    public static boolean getTeamsActivities(String activityName, String climberName) {
        return ExtraPointsModel.getTeamsActivities(activityName, climberName);
    }

    public static double getActivityPointsFromSaved(String activityName, String climberName){
        return ExtraPointsModel.getActivityPointsFromSaved(activityName, climberName);
    }

    public static void removeActivity(String checkedName, ExtraActivity activity) {
        ExtraPointsModel.removeActivity(checkedName, activity);
    }

    public static String[] adapterHelper(ExtraActivity activity){
        return ExtraPointsModel.adapterHelper(activity);
    }

    public static double pointsForActivity(Context context, ExtraActivity activity, String selectedSpinner,
                                           String pointsText){
        return ExtraPointsModel.pointsForActivity(context, activity, selectedSpinner, pointsText);
    }
}
