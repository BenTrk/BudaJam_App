package com.example.budajam.models;

import android.content.Context;
import android.util.Pair;
import android.widget.Toast;

import com.example.budajam.ExtraPointsActivity;
import com.example.budajam.classes.ExtraActivity;
import com.example.budajam.controllers.ExtraPointsController;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class ExtraPointsModel {
    public static boolean getTeamsActivities(String activityName, String climberName) {
        List<ExtraActivity> extraActivityList = initModel.getTeamData().getActivitiesMap().get(activityName);
        if (extraActivityList != null) {
            for (ExtraActivity activity : extraActivityList) {
                if (activity.getName().equals(climberName)) {
                    return true;
                }
            }
        } return false;
    }

    public static double getActivityPointsFromSaved(String activityName, String climberName) {
        double points;
        initModel.initTeamData();
        for (ExtraActivity extraActivity : initModel.getTeamData().getActivitiesMap().get(activityName)) {
            if (extraActivity.getName().equals(climberName)) {
                points = extraActivity.getPoints();
                return points;
            }
        } return 0;
    }

    public static void removeActivity(String checkedName, ExtraActivity activity) {
        double points;
        DatabaseReference climbersActivities = initModel.database.getReference(initModel.getUser().getUid() + "/Activities/" + activity.getName() + "/" + checkedName);
        if (activity.getGroup().equals("teams")) {
            checkedName = "Team";
            climbersActivities = initModel.database.getReference(initModel.getUser().getUid() + "/Activities/" + activity.getName());
        }
        DatabaseReference teamPointsReference = initModel.database.getReference(initModel.getUser().getUid() + "/teamPoints");

        points = ExtraPointsController.getActivityPointsFromSaved(activity.getName(), checkedName);
        climbersActivities.removeValue();
        teamPointsReference.setValue(initModel.getTeamData().teamPoints - points);
    }

    public static String[] adapterHelper(ExtraActivity activity){
        List<String> spinnerHelper = new ArrayList<>();
        for (Pair<String, Integer> pair : activity.getPointsList()) {
            spinnerHelper.add((String) pair.first);
        }

        String[] spinnerArray = new String[spinnerHelper.size()];
        return spinnerHelper.toArray(spinnerArray);
    }

    //ToDo
    public static double pointsForActivity(Context context, ExtraActivity activity, String selectedSpinner,
                                       String pointsText){
        double points = 0;
        if (activity.getGroup().equals("climbers")) {
            for (Pair<String, Integer> pair : activity.getPointsList()) {
                String first = (String) pair.first;
                if (first.equals(selectedSpinner)) {
                    points = pair.second;
                    return points;
                }
            }
        } else {
            if (pointsText.matches("\\d+")) {
                points = Integer.parseInt(pointsText);
                return points;
                //addPointsToDatabase(points, activity.getName(), activityText);
            } else {
                Toast.makeText(context, "This is not a number, Bro!", Toast.LENGTH_LONG).show();
                return -1;
            }
        }
        return -1;
    }
}
