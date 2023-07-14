package com.example.budajam.models;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.example.budajam.classes.Routes;
import com.example.budajam.controllers.CheckOutController;
import com.example.budajam.controllers.MainController;
import com.example.budajam.views.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CheckOutModel {
    double teamPoints;
    static List<Routes> routes = new ArrayList<>();
    static FirebaseDatabase database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
    static FirebaseUser user;
    static String climberName1, climberName2;
    static HashMap<String, List<String>> placesPerClimbers = new HashMap<>();
    static double points;
    static List<String> placesPerClimber = new ArrayList<>();
    public static void init(OnGetDataListener listener) {
        climberName1 = MainModel.climberName1;
        climberName2 = MainModel.climberName2;
        String[] climbers = MainController.getNames();
        user = FirebaseAuth.getInstance().getCurrentUser();

        for (String climber : climbers) {
            Query routesQuery = database.getReference(user.getUid() + "/" + climber + "/");
            routesQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (placesPerClimbers.get(dataSnapshot.getKey()) != null) {
                        placesPerClimbers.get(dataSnapshot.getKey()).clear();
                    }
                    List<String> places = new ArrayList<>();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        if (postSnapshot.hasChildren()) {
                            places.add(postSnapshot.getKey());
                            //It is not removed from the map
                            placesPerClimbers.put(dataSnapshot.getKey(), places);
                        }
                    }
                    listener.onSuccess();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //ToDo
                }
            });
        }
    }
    public static boolean setUserRoutesSynced() {
        if (user != null) {
            DatabaseReference myKeepSyncClimberOne = FirebaseDatabase.getInstance().getReference(user.getUid() + "/" + MainActivity.climberName1);
            myKeepSyncClimberOne.keepSynced(true);

            DatabaseReference myKeepSyncClimberTwo = FirebaseDatabase.getInstance().getReference(user.getUid() + "/" + MainActivity.climberName2);
            myKeepSyncClimberTwo.keepSynced(true);

            return true;
        }
        return false;
    }
    public static boolean areTherePlaces(String selectedName){
        return placesPerClimbers.containsKey(selectedName) && placesPerClimbers.get(selectedName).size() > 0;
    }

    public static void getTeamPoints(OnGetDataListener listener){
        Query teamPointsQuery = database.getReference(user.getUid() + "/teamPoints");
        teamPointsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                points = dataSnapshot.getValue(double.class);
                listener.onSuccess(points);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //ToDo
            }
        });
    }
    public static String[] getClimbPlaces(String selectedName){
        placesPerClimber = placesPerClimbers.get(selectedName);
        if (placesPerClimber != null) {
            //Since there is no climb on this name, it will be null!
            return placesPerClimber.toArray(new String[0]);
        } else return null;
    }
    public static void getClimbedRoutes(String selectedName, String place,
                                        OnGetDataListener listener) {
        DatabaseReference climbedRouteReference = database.getReference(user.getUid()
                + "/" + selectedName + "/" + place);
        Query routesQuery = climbedRouteReference;

        routesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //List routes declaration was here
                routes.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Routes routesDetails = postSnapshot.getValue(Routes.class);
                    routes.add(routesDetails);
                }
                listener.onSuccess(routes);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //ToDo
            }
        });
    }

    public static void removeClimb(Routes route, String climberName, String place){
        DatabaseReference climbersRoutes = database.getReference(user.getUid() + "/" + climberName + "/" + place);
        DatabaseReference teamPointsReference = database.getReference(user.getUid() + "/teamPoints");

        climbersRoutes.child(route.name).removeValue();
        teamPointsReference.setValue(points - route.points);
    }
    public interface OnGetDataListener {
        void onSuccess(List<Routes> routes);
        void onSuccess(double points);
        void onSuccess();
    }
}
