package com.example.budajam;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

//Now it is synched - SQLite is not really needed
//Use it for a save as xml activity - let the user add the days when the app should save the climbed routes
//to an xml as a backup.

public class MainActivity extends AppCompatActivity {

    private ImageButton roka, kecske, francia, svab;
    private ImageView menuButton;
    private AnimatedVectorDrawable animMenu;
    private static LinearLayout routeLayout, emptyScreenLinear;

    private Drawable shapeClimb, shapeTextClimbs, shapeClimbRight;

    public static String climberName1;
    public static String climberName2;
    private String[] place;
    public static double teamPoints;

    private List<Routes> rokaRoutes, franciaRoutes, svabRoutes, kecskeRoutes;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseUser user;

    boolean exist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        exist = false;
        auth = FirebaseAuth.getInstance();

        emptyScreenLinear = (LinearLayout) findViewById(R.id.emptyScreenLinear);

        rokaRoutes = new ArrayList<>();
        franciaRoutes = new ArrayList<>();
        svabRoutes = new ArrayList<>();
        kecskeRoutes = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shapeClimb = getDrawable(R.drawable.buttonselectorclimb);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shapeTextClimbs = getDrawable(R.drawable.button_shape_textclimb);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shapeClimbRight = getDrawable(R.drawable.buttonselectorclimbright);
        }

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        if (user != null) {
            DatabaseReference myKeepSync = FirebaseDatabase.getInstance().getReference(user.getUid() + "/");
            myKeepSync.keepSynced(true);

            DatabaseReference myKeepSyncRoutes = FirebaseDatabase.getInstance().getReference("Routes/");
            myKeepSyncRoutes.keepSynced(true);
        }

        routeLayout = (LinearLayout) findViewById(R.id.routeLayout);
        roka = (ImageButton) findViewById(R.id.roka);
        francia = (ImageButton) findViewById(R.id.francia);
        svab = (ImageButton) findViewById(R.id.svab);
        kecske = (ImageButton) findViewById(R.id.kecske);
        menuButton = (ImageView) findViewById(R.id.menuButton);

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable d = menuButton.getDrawable();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (d instanceof AnimatedVectorDrawable) {
                        animMenu = (AnimatedVectorDrawable) d;
                        animMenu.start();
                    }
                }

                //Creating the instance of PopupMenu
                Context wrapper = new ContextThemeWrapper(MainActivity.this, R.style.MyMenu);
                PopupMenu popup = new PopupMenu(wrapper, menuButton);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (R.id.one == item.getItemId()) {
                            startActivity(new Intent(MainActivity.this, CheckOutActivity.class));
                            finish();
                        } else if (R.id.two == item.getItemId()) {
                            startActivity(new Intent(MainActivity.this, OptionsActivity.class));
                            finish();
                        } else if (R.id.three == item.getItemId()) {
                            auth.signOut();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        }
                        return true;
                    }
                });

                popup.show(); //showing popup menu
            }
        }); //closing the setOnClickListener method

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                String uid = profile.getUid();
                String email = profile.getEmail();
            }
            getNamesFromDatabase(user.getUid());

            populateRoutesListatStart("roka", rokaRoutes);
            populateRoutesListatStart("kecske", kecskeRoutes);
            populateRoutesListatStart("francia", franciaRoutes);
            populateRoutesListatStart("svab", svabRoutes);

        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        roka.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.routeLayout.removeAllViews();
                emptyScreenLinear.setVisibility(GONE);

                if (rokaRoutes == null) {
                    Toast.makeText(getApplicationContext(), "Sorry, still fetching data from database. Check your internet connection!", Toast.LENGTH_LONG).show();
                } else if (rokaRoutes.isEmpty()) {
                    addView(rokaRoutes, user, "roka");
                    roka.setEnabled(false);
                    francia.setEnabled(true);
                    svab.setEnabled(true);
                    kecske.setEnabled(true);
                } else {
                    addView(rokaRoutes, user, "roka");
                    roka.setEnabled(false);
                    francia.setEnabled(true);
                    svab.setEnabled(true);
                    kecske.setEnabled(true);
                }
            }
        });

        francia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.routeLayout.removeAllViews();
                emptyScreenLinear.setVisibility(GONE);

                if (franciaRoutes == null) {
                    Toast.makeText(getApplicationContext(), "Sorry, still fetching data from database. Check your internet connection!", Toast.LENGTH_LONG).show();
                } else if (franciaRoutes.isEmpty()) {
                    addView(franciaRoutes, user, "francia");
                    roka.setEnabled(true);
                    francia.setEnabled(false);
                    svab.setEnabled(true);
                    kecske.setEnabled(true);
                } else {
                    addView(franciaRoutes, user, "francia");
                    roka.setEnabled(true);
                    francia.setEnabled(false);
                    svab.setEnabled(true);
                    kecske.setEnabled(true);
                }
            }
        });

        svab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.routeLayout.removeAllViews();
                emptyScreenLinear.setVisibility(GONE);

                if (svabRoutes == null) {
                    Toast.makeText(getApplicationContext(), "Sorry, still fetching data from database. Check your internet connection!", Toast.LENGTH_LONG).show();
                } else if (svabRoutes.isEmpty()) {
                    addView(svabRoutes, user, "svab");
                    roka.setEnabled(true);
                    francia.setEnabled(true);
                    svab.setEnabled(false);
                    kecske.setEnabled(true);
                } else {
                    addView(svabRoutes, user, "svab");
                    roka.setEnabled(true);
                    francia.setEnabled(true);
                    svab.setEnabled(false);
                    kecske.setEnabled(true);
                }
            }
        });

        kecske.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.routeLayout.removeAllViews();
                emptyScreenLinear.setVisibility(GONE);

                if (kecskeRoutes == null) {
                    Toast.makeText(getApplicationContext(), "Sorry, still fetching data from database. Check your internet connection!", Toast.LENGTH_LONG).show();
                } else if (kecskeRoutes.isEmpty()) {
                    addView(kecskeRoutes, user, "kecske");
                    roka.setEnabled(true);
                    francia.setEnabled(true);
                    svab.setEnabled(true);
                    kecske.setEnabled(false);
                } else {
                    addView(kecskeRoutes, user, "kecske");
                    roka.setEnabled(true);
                    francia.setEnabled(true);
                    svab.setEnabled(true);
                    kecske.setEnabled(false);
                }
            }
        });

        place = new String[]{"roka", "kecske", "francia", "svab"};


    }

    @SuppressLint("NewApi")
    private void addView(List<Routes> route, FirebaseUser user, String name) {

        for (int i = 0; i < route.size(); i++) {
            LinearLayout buttonsDetails = new LinearLayout(MainActivity.this);
            ImageButton addMeButton = new ImageButton(MainActivity.this, null, R.style.addViewButton);

            TextView routeDetailsName = new TextView(MainActivity.this);
            TextView routeDetailsDiff = new TextView(MainActivity.this);

            int counter = i;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.topMargin = 5;
            buttonsDetails.setLayoutParams(params);
            buttonsDetails.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams paramsText = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );

            LinearLayout textLayout = new LinearLayout(MainActivity.this);
            textLayout.setLayoutParams(paramsText);
            textLayout.setOrientation(LinearLayout.VERTICAL);
            textLayout.setGravity(Gravity.CENTER_VERTICAL);
            textLayout.setBackground(shapeTextClimbs);

            routeDetailsName.setLayoutParams(params);
            routeDetailsName.setPadding(20, 0, 20, 0);
            routeDetailsName.setText("Name: " + route.get(i).name);
            routeDetailsName.setBackgroundColor(getColor(android.R.color.transparent));
            routeDetailsName.setTextColor(getColor(R.color.icons));
            routeDetailsDiff.setLayoutParams(params);
            routeDetailsDiff.setPadding(20, 0, 20, 0);
            routeDetailsDiff.setText("Difficulty: " + route.get(i).difficulty);
            routeDetailsDiff.setBackgroundColor(getColor(android.R.color.transparent));
            routeDetailsDiff.setTextColor(getColor(R.color.icons));

            addMeButton.setBackground(shapeClimb);
            addMeButton.setPadding(10, 10, 10, 10);
            addMeButton.setImageResource(R.mipmap.muscle);

            ImageButton okButton = new ImageButton(MainActivity.this, null, R.style.addViewButton);
            okButton.setImageResource(R.mipmap.ok_sign);
            okButton.setBackground(shapeClimbRight);
            okButton.setPadding(10, 10, 10, 10);

            TextView whatClimb = new TextView(MainActivity.this);
            TextView climbedText = new TextView(MainActivity.this);

            LinearLayout climbingData = new LinearLayout(MainActivity.this);

            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params2.gravity = Gravity.CENTER;
            params2.leftMargin = 5;
            params2.rightMargin = 5;

            whatClimb.setLayoutParams(params2);
            whatClimb.setTextSize(14);
            whatClimb.setText("Route: " + route.get(counter).name);
            whatClimb.setBackgroundColor(getColor(android.R.color.transparent));
            whatClimb.setTextColor(getColor(R.color.icons));

            climbedText.setLayoutParams(params2);
            climbedText.setTextSize(12);
            climbedText.setBackgroundColor(getColor(android.R.color.transparent));
            climbedText.setTextColor(getColor(R.color.icons));

            climbingData.setLayoutParams(params2);
            climbingData.setBackgroundColor(getColor(android.R.color.transparent));
            climbingData.setOrientation(LinearLayout.HORIZONTAL);

            Spinner climberName = new Spinner(MainActivity.this);
            climberName.setLayoutParams(params2);
            String[] names = new String[]{climberName1, climberName2};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner, names);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown);
            climberName.setAdapter(adapter);
            climbingData.addView(climberName);

            Spinner climbingStyle = new Spinner(MainActivity.this);
            climbingStyle.setLayoutParams(params2);
            String[] styles = new String[]{"Top-Rope", "Front", "Clean"};
            ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, R.layout.spinner, styles);
            adapter2.setDropDownViewResource(R.layout.spinner_dropdown);
            climbingStyle.setAdapter(adapter2);
            climbingData.addView(climbingStyle);

            addMeButton.setOnClickListener(new View.OnClickListener() {
                boolean counterButtonHere = false;

                @Override
                public void onClick(View v) {
                    textLayout.removeView(climbedText);

                    if (!counterButtonHere) {
                        textLayout.removeView(routeDetailsName);
                        textLayout.removeView(routeDetailsDiff);
                        textLayout.addView(whatClimb);
                        textLayout.addView(climbingData);
                        buttonsDetails.addView(okButton);

                        okButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //count the score
                                addClimbToDatabase(user.getUid(), climberName.getSelectedItem().toString(), route.get(counter), name, climbingStyle.getSelectedItem().toString());
                                climbedText.setText(climberName.getSelectedItem().toString() + " climbed it, " + climbingStyle.getSelectedItem().toString() + " style!");
                                textLayout.removeView(climbingData);
                                buttonsDetails.removeView(okButton);
                                textLayout.addView(climbedText);
                            }
                        });

                        counterButtonHere = true;

                    } else {
                        textLayout.addView(routeDetailsName);
                        textLayout.addView(routeDetailsDiff);
                        textLayout.removeView(whatClimb);
                        textLayout.removeView(climbingData);
                        buttonsDetails.removeView(okButton);
                        counterButtonHere = false;
                    }
                }
            });

            textLayout.addView(routeDetailsName);
            textLayout.addView(routeDetailsDiff);

            buttonsDetails.addView(addMeButton);
            buttonsDetails.addView(textLayout);
            MainActivity.routeLayout.addView(buttonsDetails);
        }
    }

    public void getNamesFromDatabase(String userID) {
        database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
        DatabaseReference myRef2 = database.getReference(userID + "/");

        Query routesQuery2 = myRef2;
        routesQuery2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ClimberNames climberNamesInside = dataSnapshot.getValue(ClimberNames.class);
                climberName1 = climberNamesInside.ClimberOne;
                climberName2 = climberNamesInside.ClimberTwo;
                teamPoints = climberNamesInside.teamPoints;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //
            }
        });
    }

    public void addClimbToDatabase(String userID, String climber, Routes route, String name, String climbStyle) {
        database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
        DatabaseReference myRef3 = database.getReference(userID + "/" + climber);
        //double pointsToAdd;

        //get a list of the climbed routes here, important is routes.style and routes.points
        //count the correct points depending on style
        //write back to database

        //Count on the other activity! There it works.

        pointsCalculator(userID, climber, name, route.name, myRef3, route, climbStyle);
    }

    private void pointsCalculator(String userID, String climberName, String placeName, String routeName, DatabaseReference myRef3, Routes route, String climbStyle) {
        String reference = user.getUid() + "/" + climberName + "/" + placeName + "/" + routeName;

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        Query boolReference = rootRef;
        boolReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild("/" + user.getUid() + "/" + climberName + "/" + placeName + "/" + routeName)) {
                    myRef3.child(placeName).child(routeName).child("name").setValue(route.name);
                    myRef3.child(placeName).child(routeName).child("difficulty").setValue(route.difficulty);
                    //Now, if someone climbs a route clean, then also front, it does not
                    //add more points, but it changes the climbStyle to "Front".
                    myRef3.child(placeName).child(routeName).child("climbStyle").setValue(climbStyle);

                    database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
                    DatabaseReference myRef2 = database.getReference(reference);

                    Query routesQuery2 = myRef2;
                    routesQuery2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Routes climberPointsInside = dataSnapshot.getValue(Routes.class);
                            double routePoints = climberPointsInside.points;
                            double pointsToRemove = routePoints;
                            double pointsToAdd;

                            if (climbStyle == "Top-Rope") {
                                //myRef3.child(name).child(route.name).child("points").setValue(route.points * 0.5);
                                pointsToAdd = route.points * 0.5;
                            } else if (climbStyle == "Clean") {
                                //myRef3.child(name).child(route.name).child("points").setValue(route.points * 2);
                                pointsToAdd = route.points * 2;
                            } else {
                                //myRef3.child(name).child(route.name).child("points").setValue(route.points);
                                pointsToAdd = route.points;
                            }

                            int isMoreOrLess = Double.compare(routePoints, pointsToAdd);


                            if (isMoreOrLess < 0) {
                                Toast.makeText(getApplicationContext(), "Exist, but " + climberName + " earned more points!", Toast.LENGTH_LONG).show();

                                myRef3.child(placeName).child(routeName).child("points").setValue(pointsToAdd);
                                DatabaseReference myRefPoints = database.getReference(userID + "/teamPoints");
                                myRefPoints.setValue((teamPoints - pointsToRemove) + pointsToAdd);

                            } else {
                                Toast.makeText(getApplicationContext(), "Exist, and " + climberName + " earned less points!", Toast.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            //
                        }
                    });
                } else {
                    double pointsToAddNotExist;

                    if (climbStyle == "Top-Rope") {
                        pointsToAddNotExist = route.points * 0.5;
                    } else if (climbStyle == "Clean") {
                        pointsToAddNotExist = route.points * 2;
                    } else {
                        pointsToAddNotExist = route.points;
                    }

                    Toast.makeText(getApplicationContext(), "Added climb to database.", Toast.LENGTH_LONG).show();
                    myRef3.child(placeName).child(routeName).child("name").setValue(route.name);
                    myRef3.child(placeName).child(routeName).child("difficulty").setValue(route.difficulty);
                    myRef3.child(placeName).child(routeName).child("climbStyle").setValue(climbStyle);
                    myRef3.child(placeName).child(routeName).child("points").setValue(pointsToAddNotExist);

                    DatabaseReference myRefPoints = database.getReference(userID + "/teamPoints");
                    myRefPoints.setValue(teamPoints + pointsToAddNotExist);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

        //it also counts duplicated points! So here's what you need.
        //Get the points for each climbed routes from the database
        //And count them. That's all, easypeasy.
    }

    private void populateRoutesListatStart(String name, List<Routes> routes) {
        database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
        DatabaseReference myRef = database.getReference("Routes/" + name);

        if (routes != null) {
            routes.clear();
        }

        Query routesQuery = myRef;
        routesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Routes routesDetails = postSnapshot.getValue(Routes.class);
                    routes.add(routesDetails);
                    //int key = routesDetails.key;

                    //can be removed
                    // try {
                    //    saveToDB(routesDetails, name, key);
                    //   } catch(SQLException ex){
                    //       Log.i("SQLException", "Duplicate route");
                    //   }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

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
            if (placeName == "roka") {
                Routes rokaRoute = new Routes();

                String itemName = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_NAME));
                rokaRoute.name = itemName;

                String itemDifficulty = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFICULTY));
                rokaRoute.difficulty = Long.parseLong(itemDifficulty);

                String itemLength = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_LENGTH));
                rokaRoute.length = Long.parseLong(itemLength);

                String itemDiffChanger = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFCHANGER));
                rokaRoute.diffchanger = itemDiffChanger;

                rokaRoutes.add(rokaRoute);
            } else if (placeName == "kecske") {
                Routes kecskeRoute = new Routes();

                String itemName = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_NAME));
                kecskeRoute.name = itemName;

                String itemDifficulty = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFICULTY));
                kecskeRoute.difficulty = Long.parseLong(itemDifficulty);

                String itemLength = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_LENGTH));
                kecskeRoute.length = Long.parseLong(itemLength);

                String itemDiffChanger = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFCHANGER));
                kecskeRoute.diffchanger = itemDiffChanger;

                kecskeRoutes.add(kecskeRoute);
            } else if (placeName == "francia") {
                Routes franciaRoute = new Routes();

                String itemName = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_NAME));
                franciaRoute.name = itemName;

                String itemDifficulty = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFICULTY));
                franciaRoute.difficulty = Long.parseLong(itemDifficulty);

                String itemLength = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_LENGTH));
                franciaRoute.length = Long.parseLong(itemLength);

                String itemDiffChanger = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFCHANGER));
                franciaRoute.diffchanger = itemDiffChanger;

                franciaRoutes.add(franciaRoute);
            } else if (placeName == "svab") {
                Routes svabRoute = new Routes();

                String itemName = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_NAME));
                svabRoute.name = itemName;

                String itemDifficulty = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFICULTY));
                svabRoute.difficulty = Long.parseLong(itemDifficulty);

                String itemLength = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_LENGTH));
                svabRoute.length = Long.parseLong(itemLength);

                String itemDiffChanger = cursor.getString(cursor.getColumnIndexOrThrow(RoutesSQLiteDBHelper.ROUTES_COLUMN_DIFFCHANGER));
                svabRoute.diffchanger = itemDiffChanger;

                svabRoutes.add(svabRoute);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void emptyFiller() {

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        LinearLayout.LayoutParams paramsText = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        paramsText.setMargins(0, 100, 0, 60);

        TextView choosePlace = new TextView(MainActivity.this);
        choosePlace.setBackgroundResource(R.drawable.textview_shape_climb);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            choosePlace.setTextColor(getColor(R.color.icons));
        }
        choosePlace.setTextSize(24);
        choosePlace.setPadding(10, 5, 10, 5);
        choosePlace.setLayoutParams(paramsText);
        choosePlace.setGravity(Gravity.CENTER_HORIZONTAL);
        choosePlace.setText("Pick a place!");

        MainActivity.routeLayout.setGravity(Gravity.CENTER);
        MainActivity.routeLayout.addView(choosePlace);

        for (int i = 0; i < place.length; i++) {
            LinearLayout emptyFillerLayout = new LinearLayout(MainActivity.this);
            ImageView placeImageView = new ImageView(MainActivity.this);
            TextView placeNameTextView = new TextView(MainActivity.this);

            params.setMargins(0, 20, 0, 0);
            emptyFillerLayout.setPadding(10, 0, 10, 0);
            emptyFillerLayout.setLayoutParams(params);
            emptyFillerLayout.setOrientation(LinearLayout.HORIZONTAL);
            emptyFillerLayout.setBackgroundResource(R.drawable.button_shape_main);
            emptyFillerLayout.setGravity(Gravity.CENTER_VERTICAL);

            placeNameTextView.setPadding(10, 20, 10, 20);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                placeNameTextView.setBackgroundColor(getColor(android.R.color.transparent));
            }


            placeImageView.setMinimumHeight(50);
            placeImageView.setPadding(10, 25, 10, 25);

            if (place[i] == "roka") {
                placeImageView.setBackgroundResource(R.mipmap.fox);
                placeNameTextView.setText("Róka Hegy");
                emptyFillerLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.routeLayout.removeAllViews();
                        MainActivity.routeLayout.setGravity(Gravity.NO_GRAVITY);
                        addView(rokaRoutes, user, "roka");
                        roka.setEnabled(false);
                    }
                });
            } else if (place[i] == "svab") {
                placeImageView.setBackgroundResource(R.mipmap.german);
                placeNameTextView.setText("Sváb Hegy");
                emptyFillerLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.routeLayout.removeAllViews();
                        MainActivity.routeLayout.setGravity(Gravity.NO_GRAVITY);
                        addView(svabRoutes, user, "svab");
                        svab.setEnabled(false);
                    }
                });
            } else if (place[i] == "francia") {
                placeImageView.setBackgroundResource(R.mipmap.french);
                placeNameTextView.setText("Francia Bánya");
                emptyFillerLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.routeLayout.removeAllViews();
                        MainActivity.routeLayout.setGravity(Gravity.NO_GRAVITY);
                        addView(franciaRoutes, user, "francia");
                        francia.setEnabled(false);
                    }
                });
            } else if (place[i] == "kecske") {
                placeImageView.setBackgroundResource(R.mipmap.goat);
                placeNameTextView.setText("Kecske Hegy");
                emptyFillerLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.routeLayout.removeAllViews();
                        MainActivity.routeLayout.setGravity(Gravity.NO_GRAVITY);
                        addView(kecskeRoutes, user, "kecske");
                        kecske.setEnabled(false);
                    }
                });
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                placeNameTextView.setTextColor(getColor(R.color.icons));
            }
            placeNameTextView.setTextSize(20);

            emptyFillerLayout.addView(placeImageView);
            emptyFillerLayout.addView(placeNameTextView);

            MainActivity.routeLayout.addView(emptyFillerLayout);
        }
    }
}


