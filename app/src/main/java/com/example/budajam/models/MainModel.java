package com.example.budajam.models;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.budajam.classes.ClimberNames;
import com.example.budajam.classes.Routes;
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
import java.util.Set;

public class MainModel {
    static FirebaseDatabase database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
    static String climberName1, climberName2;
    static double teamPoints;
    static HashMap<String, List<Routes>> placesWithRoutes = new HashMap<>();
    static FirebaseAuth auth = FirebaseAuth.getInstance();
    static FirebaseAuth.AuthStateListener authListener;
    public static FirebaseUser user = auth.getCurrentUser();

    public static void init(){
        Query routesQuery = database.getReference("Routes/");
        routesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot childSnapshot : postSnapshot.getChildren()) {
                        if (placesWithRoutes.containsKey(postSnapshot.getKey())) {
                            List<Routes> container = placesWithRoutes.get(postSnapshot.getKey());
                            assert container != null;
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
    public static FirebaseUser authentication(){
        //If something is wrong with authentication, search here. :)
        authListener = firebaseAuth -> {
            user = firebaseAuth.getCurrentUser();
        };
        auth.addAuthStateListener(authListener);
        return user;
    }
    public static void signOut(){
        auth.signOut();
    }
    public static boolean setUserAndRouteSynced() {
        if (user != null) {
            FirebaseDatabase.getInstance().getReference(user.getUid() + "/").keepSynced(true);
            FirebaseDatabase.getInstance().getReference("Routes/").keepSynced(true);
            return true;
        } else {
            return false;
        }
    }
    public static void getNamesFromDatabase() {
        Query routesQuery = database.getReference(user.getUid() + "/");
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
    public static Set<String> getPlacesFromDatabase() {
        return placesWithRoutes.keySet();
    }
    public static List<Routes> getRoutes(String name) {
        return placesWithRoutes.get(name);
    }
    public static String[] getNames(){
        return new String[]{climberName1, climberName2};
    }
    public static void addClimbToTheDatabase(String climberName, String styleName,
                                             String placeName, Routes route, Context context){
        String reference = user.getUid() + "/" + climberName + "/" + placeName + "/" + route.name;
        DatabaseReference climbsOfUser = database.getReference(reference);
        Query climbsOfUserQuery = database.getReference(reference);
        double pointsToAdd = pointsCounter(styleName, route);

        climbsOfUser.child("name").setValue(route.name);
        climbsOfUser.child("difficulty").setValue(route.difficulty);
        climbsOfUser.child("climbStyle").setValue(styleName);

        climbsOfUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Routes route = snapshot.getValue(Routes.class);
                assert route != null;
                double pointsFromDatabase = route.points;
                int moreOrLess = Double.compare(pointsFromDatabase, pointsToAdd);
                if(moreOrLess <= 0) {
                    Dialog dialog;
                    if (pointsFromDatabase == 0.0) {
                        dialog = dialogBuilderFunc(context, false, false);
                    } else {
                        dialog = dialogBuilderFunc(context, true, true);
                    }
                    dialog.show();
                    climbsOfUser.child("points").setValue(pointsToAdd);
                    climbsOfUser.child("best").setValue(styleName);
                    DatabaseReference myRefPoints = database.getReference(user.getUid() + "/teamPoints");
                    myRefPoints.setValue((teamPoints - pointsFromDatabase) + pointsToAdd);
                } else {
                    Dialog dialog = dialogBuilderFunc(context, true, false);
                    dialog.show();
                }
            }

            private Dialog dialogBuilderFunc(Context context, boolean exists, boolean isMore) {
                AlertDialog dialog;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                if (!exists) {
                    builder.setMessage("Nice climb, added to the database!")
                            .setTitle("Way to go!");
                    builder.setNeutralButton("Gotcha, thanks!", (dialog1, id) -> dialog1.dismiss());
                }
                else {
                    if (isMore){
                        builder.setMessage("Team had this route, but you earned more points!")
                                .setTitle("Way to go!");
                        builder.setNeutralButton("Gotcha, thanks!", (dialog12, id) -> dialog12.dismiss());
                    }
                    else {
                        builder.setMessage("Team had this climb with more points!")
                                .setTitle("It had to be fun, though!");
                        builder.setNeutralButton("Gotcha, thanks!", (dialog13, id) -> dialog13.dismiss());
                    }
                }
                dialog = builder.create();
                return dialog;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //ToDo
            }
        });

    }
    private static double pointsCounter(String styleName, Routes route) {
        double points;
        if (styleName.equals("Top-rope")) {
            points = route.points * 0.5;
        } else if (styleName.equals("Clean")) {
            points = route.points * 2;
        } else {
            points = route.points;
        }
        return points;
    }

    //ToDo: check dates!
    /* Add date checker too!
      These could be useful:
      private void saveToDB(Routes route, String place, int key) {
              SQLiteDatabase database = new RoutesSQLiteDBHelper(this).getWritableDatabase();
              ContentValues values = new ContentValues();
              values.put(RoutesSQLiteDBHelper.ROUTES_COLUMN_KEY, key);
              values.put(RoutesSQLiteDBHelper.ROUTES_COLUMN_PLACE, place);
              values.put(RoutesSQLiteDBHelper.ROUTES_COLUMN_NAME, route.name);
              values.put(RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFICULTY, route.difficulty);
              values.put(RoutesSQLiteDBHelper.ROUTES_COLUMN_LENGTH, route.length);
              values.put(RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFCHANGER, route.diffchanger);

              database.insert(RoutesSQLiteDBHelper.ROUTES_TABLE_NAME, null, values);
          }

          private void readFromDB(String placeName) {

              SQLiteDatabase database = new RoutesSQLiteDBHelper(this).getReadableDatabase();

              String[] projection = {
                      RoutesSQLiteDBHelper.ROUTES_COLUMN_PLACE,
                      RoutesSQLiteDBHelper.ROUTES_COLUMN_NAME,
                      RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFICULTY.toString(),
                      RoutesSQLiteDBHelper.ROUTES_COLUMN_LENGTH.toString(),
                      RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFCHANGER
              };

              String selection = RoutesSQLiteDBHelper.ROUTES_COLUMN_PLACE + " like ?";

              String[] selectionArgs = {"%" + placeName};

              Cursor cursor = database.query(
                      RoutesSQLiteDBHelper.ROUTES_TABLE_NAME,   // The table to query
                      projection,                               // The columns to return
                      selection,                  // The columns for the WHERE clause
                      selectionArgs,                          // The values for the WHERE clause
                      null,                                     // don't group the rows
                      null,                                     // don't filter by row groups
                      null                                      // don't sort
              );

              while (cursor.moveToNext()) {
                  if (Objects.equals(placeName, "roka")) {
                      Routes rokaRoute = new Routes();

                      rokaRoute.name = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_NAME));

                      String itemDifficulty = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFICULTY));
                      rokaRoute.difficulty = Long.parseLong(itemDifficulty);

                      String itemLength = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_LENGTH));
                      rokaRoute.length = Long.parseLong(itemLength);

                      rokaRoute.diffchanger = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFCHANGER));

                      //rokaRoutes.add(rokaRoute);
                  } else if (Objects.equals(placeName, "kecske")) {
                      Routes kecskeRoute = new Routes();

                      kecskeRoute.name = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_NAME));

                      String itemDifficulty = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFICULTY));
                      kecskeRoute.difficulty = Long.parseLong(itemDifficulty);

                      String itemLength = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_LENGTH));
                      kecskeRoute.length = Long.parseLong(itemLength);

                      kecskeRoute.diffchanger = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFCHANGER));

                      //kecskeRoutes.add(kecskeRoute);
                  } else if (Objects.equals(placeName, "francia")) {
                      Routes franciaRoute = new Routes();

                      franciaRoute.name = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_NAME));

                      String itemDifficulty = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFICULTY));
                      franciaRoute.difficulty = Long.parseLong(itemDifficulty);

                      String itemLength = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_LENGTH));
                      franciaRoute.length = Long.parseLong(itemLength);

                      franciaRoute.diffchanger = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFCHANGER));

                      //franciaRoutes.add(franciaRoute);
                  } else if (Objects.equals(placeName, "svab")) {
                      Routes svabRoute = new Routes();

                      svabRoute.name = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_NAME));

                      String itemDifficulty = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFICULTY));
                      svabRoute.difficulty = Long.parseLong(itemDifficulty);

                      String itemLength = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_LENGTH));
                      svabRoute.length = Long.parseLong(itemLength);

                      svabRoute.diffchanger = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFCHANGER));

                      //svabRoutes.add(svabRoute);
                  }
              }**/
}
