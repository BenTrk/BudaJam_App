package com.example.budajam.models;

import com.example.budajam.classes.PlaceWithRoutes;
import com.example.budajam.classes.Route;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class CheckOutModel {
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
}
