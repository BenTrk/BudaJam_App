package com.example.budajam.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.budajam.R;
import com.example.budajam.classes.ClimberNames;
import com.example.budajam.classes.DataStorage;
import com.example.budajam.classes.Routes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MainModel {
    static FirebaseDatabase database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
    static Query routesQuery = database.getReference("Routes/");
    static String climberName1, climberName2;
    static double teamPoints;
    static HashMap<String, List<Routes>> placesWithRoutes = new HashMap<>();

    public static void init(){
        routesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot childSnapshot : postSnapshot.getChildren()) {
                        if (placesWithRoutes.containsKey(postSnapshot.getKey())) {
                            List<Routes> container = placesWithRoutes.get(postSnapshot.getKey());
                            container.add(childSnapshot.getValue(Routes.class));
                            placesWithRoutes.put(postSnapshot.getKey(), container);
                        } else {
                            List<Routes> container = new ArrayList<>();
                            container.add(childSnapshot.getValue(Routes.class));
                            placesWithRoutes.put(postSnapshot.getKey(), container);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //ToDo
            }
        });
    }

    public static boolean setUserAndRouteSynced(FirebaseAuth auth, FirebaseUser user) {
        if (user != null) {
            FirebaseDatabase.getInstance().getReference(user.getUid() + "/").keepSynced(true);
            FirebaseDatabase.getInstance().getReference("Routes/").keepSynced(true);
            return true;
        } else {
            return false;
        }
    }
    public static void getNamesFromDatabase(String userID) {
        Query routesQuery = database.getReference(userID + "/");
        routesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ClimberNames climberNamesInside = dataSnapshot.getValue(ClimberNames.class);
                assert climberNamesInside != null;
                climberName1 = climberNamesInside.ClimberOne;
                climberName2 = climberNamesInside.ClimberTwo;
                teamPoints = climberNamesInside.teamPoints;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //
            }
        });
    }
    public static Set<String> getPlacesFromDatabase(Context context, LinearLayout routeLayout, Button buttonShowDropDown) {
        return placesWithRoutes.keySet();
    }
    public static List<Routes> getRoutes(String name) {
        return placesWithRoutes.get(name);
    }
    public static String[] getNames(){
        return new String[]{climberName1, climberName2};
    }

}
