package com.example.budajam.models;

import androidx.annotation.NonNull;

import com.example.budajam.classes.PlaceWithRoutes;
import com.example.budajam.classes.Route;
import com.example.budajam.controllers.MainController;
import com.example.budajam.interfaces.OnGetClimbDataListener;
import com.example.budajam.interfaces.OnGetPointsListener;
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
    static FirebaseUser user;
    static double points;

    public static boolean areTherePlaces(String selectedName){
        return initModel.getTeamData().getClimbersClimbsMap().get(selectedName) != null;
    }
    public static double getTeamPoints(){
        return initModel.getTeamData().getTeamPoints();
    }
    public static String[] getClimbPlaces(String selectedName){
        List<PlaceWithRoutes> listOfClimbs = initModel.getTeamData().getClimbersClimbsMap().get(selectedName);
        List<String> placesList = new ArrayList<>();
        if (listOfClimbs != null) {
            for (PlaceWithRoutes place : listOfClimbs) {
                placesList.add(place.getPlaceName());
            }
            String[] placesArray = new String[placesList.size()];
            return placesList.toArray(placesArray);
        } else return null;
    }
    public static List<Route> getClimbedRoutesPerClimber(String selectedName, String place){
        List<Route> routesPerClimber = new ArrayList<>();
        List<PlaceWithRoutes> listOfClimbs = initModel.getTeamData().getClimbersClimbsMap().get(selectedName);

        for (PlaceWithRoutes placeInList : listOfClimbs){
            if (placeInList.getPlaceName().equals(place)) {
                routesPerClimber = placeInList.getRouteList();
                break;
            }
        }
        return routesPerClimber;
    }
    public static double removeClimb(Route route, String climberName, String place){
        DatabaseReference climbersRoutes = initModel.database.getReference(initModel.getUser().getUid() + "/Climbs/" + climberName + "/" + place);
        DatabaseReference teamPointsReference = initModel.database.getReference(initModel.getUser().getUid() + "/teamPoints");

        points = initModel.getTeamData().teamPoints;
        climbersRoutes.child(route.name).removeValue();
        teamPointsReference.setValue(points - route.points);
        return points - route.points;
    }

    public static double pointSetter(double teamPointsPast) {
        while (teamPointsPast == initModel.getTeamData().teamPoints){
            return -1;
        } return initModel.getTeamData().teamPoints;
    }
}
