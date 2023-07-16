package com.example.budajam.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.budajam.classes.ExtraActivity;
import com.example.budajam.classes.PlaceWithRoutes;
import com.example.budajam.classes.TeamData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class initModel {
    //Database instance
    public static FirebaseDatabase database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
    //All places and routes in the event
    static FirebaseAuth.AuthStateListener authListener;
    static FirebaseAuth auth = FirebaseAuth.getInstance();
    static FirebaseUser user = auth.getCurrentUser();
    static List<PlaceWithRoutes> placesWithRoutes = new ArrayList<>();
    static TeamData teamData;
    static List<ExtraActivity> listOfActivities = new ArrayList<>();

    //Authenticate the user, and if OK, initialize data.
    public static boolean onCreate() {
        initAuthentication();
        if (user != null) {
            initPlacesWithRoutes();
            initTeamData();
            initActivities();
            return true;
        } return false;
    }

    //Authenticate the user.
    public static void initAuthentication(){
        //If something is wrong with authentication, search here. :)
        authListener = firebaseAuth -> {
            user = firebaseAuth.getCurrentUser();
        };
        auth.addAuthStateListener(authListener);
    }

    //Initialize places where climbs could happen.
    //Set up with one time database call
    public static void initPlacesWithRoutes() {
        Query routesQuery = database.getReference("Routes/");
        routesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Object object = postSnapshot.getValue(Object.class);
                    String placeWithRoutesJSON = new Gson().toJson(object);
                    try {
                        PlaceWithRoutes placeWithRoutes = new PlaceWithRoutes(postSnapshot.getKey(), placeWithRoutesJSON);
                        placesWithRoutes.add(placeWithRoutes);
                    } catch (JsonProcessingException e) {
                        Log.v("JSON Error", "Error: " + e);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //ToDo
            }
        });
    }

    //Initialize team data.
    //Set up with database call for every change
    public static void initTeamData(){
        assert user != null;
        Query routesQuery = database.getReference(user.getUid());
        routesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    HashMap<String, List<PlaceWithRoutes>> climbersClimbsMap = new HashMap<>();
                    DataSnapshot mainFolderSnapshot = dataSnapshot.child("Climbs");
                    for (DataSnapshot climberNameSnapshot : mainFolderSnapshot.getChildren()) {
                        List<PlaceWithRoutes> placesForThisClimber = new ArrayList<>();
                        for (DataSnapshot placeNameSnapshot : climberNameSnapshot.getChildren()) {
                            Object object = placeNameSnapshot.getValue(Object.class);
                            String placeWithRoutesJSON = new Gson().toJson(object);
                            try {
                                PlaceWithRoutes placeWithRoutes = new PlaceWithRoutes(placeNameSnapshot.getKey(), placeWithRoutesJSON);
                                placesForThisClimber.add(placeWithRoutes);
                            } catch (JsonProcessingException e) {
                                Log.v("JSON Team Error", "Error: " + e);
                            }
                        }
                        climbersClimbsMap.put(climberNameSnapshot.getKey(), placesForThisClimber);
                    }
                    teamData = new TeamData(dataSnapshot.child("TeamName").getValue(String.class),
                            dataSnapshot.child("ClimberOne").getValue(String.class), dataSnapshot.child("ClimberTwo").getValue(String.class),
                            dataSnapshot.child("teamPoints").getValue(double.class), dataSnapshot.child("Paid").getValue(boolean.class),
                            dataSnapshot.child("Category").getValue(String.class), climbersClimbsMap);
                }
                else {
                    Object object = dataSnapshot.getValue(Object.class);
                    String teamDataString = new Gson().toJson(object);
                    try {
                        teamData = new ObjectMapper().readValue(teamDataString, TeamData.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //ToDo
            }
        });
    }

    //Initialize the activities.
    //Set up with one time database call
    public static void initActivities(){
        Query routesQuery = database.getReference("Activities/");
        routesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot activitySnapshot : dataSnapshot.getChildren()){
                    String activityString = activitySnapshot.getValue().toString();
                    ExtraActivity activity = new ExtraActivity(activitySnapshot.getKey(), activityString);
                    listOfActivities.add(activity);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Return list of all places and all routes.
    public static List<PlaceWithRoutes> getPlacesWithRoutes() {
        return placesWithRoutes;
    }

    public static void setPlacesWithRoutes(List<PlaceWithRoutes> placesWithRoutes) {
        initModel.placesWithRoutes = placesWithRoutes;
    }

    public static TeamData getTeamData() {
        return teamData;
    }

    public static void setTeamData(TeamData teamData) {
        initModel.teamData = teamData;
    }

    public static List<ExtraActivity> getListOfActivities() {
        return listOfActivities;
    }

    public static void setListOfActivities(List<ExtraActivity> listOfActivities) {
        initModel.listOfActivities = listOfActivities;
    }

    public static FirebaseAuth getAuth() {
        return auth;
    }

    public static void setAuth(FirebaseAuth auth) {
        initModel.auth = auth;
    }

    public static FirebaseUser getUser() {
        return user;
    }
}
