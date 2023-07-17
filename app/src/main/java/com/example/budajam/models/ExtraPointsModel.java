package com.example.budajam.models;

import android.app.Dialog;
import android.content.Context;
import android.util.Pair;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.budajam.classes.ExtraActivity;
import com.example.budajam.controllers.ExtraPointsController;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        for (ExtraActivity extraActivity : Objects.requireNonNull(initModel.getTeamData().getActivitiesMap().get(activityName))) {
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
        double points;
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
    public static void saveToDatabaseClimbers(Context context, ExtraActivity activity, String climberName, int points){
        String reference = initModel.getUser().getUid() + "/Activities/" + activity.getName() + "/" + climberName;

        List<ExtraActivity> activitiesForThisClimber = initModel.getTeamData().getActivitiesMap().get(activity.getName());
        double pointsFromDatabase;
        ExtraActivity activityNowDone = new ExtraActivity();

        if (initModel.getTeamData().getActivitiesMap().get(activity.getName()) != null) {
            assert activitiesForThisClimber != null;
            for (ExtraActivity activityInList : activitiesForThisClimber) {
                if (activityInList.getName().equals(climberName)) {
                    activityNowDone = activityInList;
                    break;
                }
            }
            pointsFromDatabase = activityNowDone.getPoints();
        } else pointsFromDatabase = 0.0;

        DatabaseReference activitiesOfUser = initModel.database.getReference(reference);

        int moreOrLess = Double.compare(pointsFromDatabase, points);
        Dialog dialog;
        if (pointsFromDatabase != 0) {
            if (moreOrLess <= 0) {
                activitiesOfUser.setValue((double) points);
                DatabaseReference myRefPoints = initModel.database.getReference(initModel.getUser().getUid() + "/teamPoints");
                myRefPoints.setValue((initModel.getTeamData().teamPoints - pointsFromDatabase) + (double) points);
                dialog = dialogBuilderFunc(context, true, true);
                dialog.show();
            } else {
                dialog = dialogBuilderFunc(context, true, false);
                dialog.show();
            }
        }
        else {
            dialog = dialogBuilderFunc(context, false, false);
            dialog.show();
            activitiesOfUser.setValue((double) points);
            DatabaseReference myRefPoints = initModel.database.getReference(initModel.getUser().getUid() + "/teamPoints");
            myRefPoints.setValue(initModel.getTeamData().teamPoints + (double) points);
        }
    }
    public static void saveToDatabaseTeams(Context context, ExtraActivity activity, int points){
        String reference = initModel.getUser().getUid() + "/Activities/" + activity.getName();

        List<ExtraActivity> activitiesForThisClimber = initModel.getTeamData().getActivitiesMap().get(activity.getName());
        double pointsFromDatabase;
        ExtraActivity activityNowDone = new ExtraActivity();

        if (initModel.getTeamData().getActivitiesMap().get(activity.getName()) != null) {
            assert activitiesForThisClimber != null;
            for (ExtraActivity activityInList : activitiesForThisClimber) {
                if (activityInList.getName().equals("Team")) {
                    activityNowDone = activityInList;
                    break;
                }
            }
            pointsFromDatabase = activityNowDone.getPoints();
        } else pointsFromDatabase = 0.0;

        DatabaseReference activitiesOfTeam = initModel.database.getReference(reference);

        int moreOrLess = Double.compare(pointsFromDatabase, points);
        Dialog dialog;
        if (pointsFromDatabase != 0) {
            if (moreOrLess <= 0) {
                activitiesOfTeam.setValue((double) points);
                DatabaseReference myRefPoints = initModel.database.getReference(initModel.getUser().getUid() + "/teamPoints");
                myRefPoints.setValue((initModel.getTeamData().teamPoints - pointsFromDatabase) + (double) points);
                dialog = dialogBuilderFunc(context, true, true);
                dialog.show();
            } else {
                dialog = dialogBuilderFunc(context, true, false);
                dialog.show();
            }
        }
        else {
            dialog = dialogBuilderFunc(context, false, false);
            dialog.show();
            activitiesOfTeam.setValue((double) points);
            DatabaseReference myRefPoints = initModel.database.getReference(initModel.getUser().getUid() + "/teamPoints");
            myRefPoints.setValue(initModel.getTeamData().teamPoints + (double) points);
        }
    }
    private static Dialog dialogBuilderFunc(Context context, boolean exists, boolean isMore) {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (!exists) {
            builder.setMessage("Nice activity, added to the database!")
                    .setTitle("Way to go!");
            builder.setNeutralButton("Gotcha, thanks!", (dialog1, id) -> dialog1.dismiss());
        }
        else {
            if (isMore){
                builder.setMessage("You had this activity, but you earned more points!")
                        .setTitle("Way to go!");
                builder.setNeutralButton("Gotcha, thanks!", (dialog12, id) -> dialog12.dismiss());
            }
            else {
                builder.setMessage("You had this activity with more points!")
                        .setTitle("It had to be fun, though!");
                builder.setNeutralButton("Gotcha, thanks!", (dialog13, id) -> dialog13.dismiss());
            }
        }
        dialog = builder.create();
        return dialog;
    }
}
