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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
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
import java.util.Calendar;
import java.util.List;

import static android.view.View.GONE;

//Now it is synched - SQLite is not really needed
//Use it for a save as xml activity - let the user add the days when the app should save the climbed routes
//to an xml as a backup.

//Important: Before launch, make sure climbers cannot add climbs to the database outside of the given dates!

public class MainActivity extends AppCompatActivity {

    private ImageButton roka, kecske, francia, svab;
    private ImageView menuButton;
    private AnimatedVectorDrawable animMenu, animArrowAnim;
    private static LinearLayout routeLayout, emptyScreenLinear;

    public static String climberName1;
    public static String climberName2;
    public static double teamPoints;
    public boolean isPaid;

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

        //Check if team has paid the entrance fee - ugly but works.
        //Refactor getting team information. You make queries for the same thing over and over again.

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

        //Show the menu, upper left corner
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

                Context wrapper = new ContextThemeWrapper(MainActivity.this, R.style.MyMenu);
                PopupMenu popup = new PopupMenu(wrapper, menuButton);

                popup.getMenuInflater()
                        .inflate(R.menu.popup_menu, popup.getMenu());

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

                popup.show();
            }
        });

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

        //dateChecker(roka, kecske, francia, svab);

        roka.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllClimbPlaceButtonsDefault();
                climbPlaceButtonHandler(rokaRoutes, roka, "roka");
            }
        });

        francia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllClimbPlaceButtonsDefault();
                climbPlaceButtonHandler(franciaRoutes, francia, "francia");
            }
        });

        svab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllClimbPlaceButtonsDefault();
                climbPlaceButtonHandler(svabRoutes, svab, "svab");
            }
        });

        kecske.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllClimbPlaceButtonsDefault();
                climbPlaceButtonHandler(kecskeRoutes, kecske, "kecske");
            }
        });
    }

    public void climbPlaceButtonHandler(List<Routes> routes, ImageButton placeButton, String placeName){
        MainActivity.routeLayout.removeAllViews();
        emptyScreenLinear.setVisibility(GONE);

        if (routes == null) {
            Toast.makeText(getApplicationContext(), "Sorry, still fetching data from database. Check your internet connection!", Toast.LENGTH_LONG).show();
        } else if (routes.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Sorry, there are no routes in the database for this place!", Toast.LENGTH_LONG).show();
            placeButton.setEnabled(false);
        } else {
            for(int i = 0; i < routes.size(); i++) {
                addCustomSpinner(routes.get(i), placeName);
            }
            placeButton.setEnabled(false);
        }
    }

    public void setAllClimbPlaceButtonsDefault(){
        roka.setEnabled(true);
        svab.setEnabled(true);
        kecske.setEnabled(true);
        francia.setEnabled(true);
    }

    public void getNamesFromDatabase(String userID) {
        database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
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
            public void onCancelled(DatabaseError error) {
                //
            }
        });
    }

    public void addClimbToDatabase(String userID, String climber, Routes route, String name, String climbStyle) {
        database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
        DatabaseReference myRef3 = database.getReference(userID + "/" + climber);

        pointsCalculator(userID, climber, name, route.name, myRef3, route, climbStyle);
    }

    private void pointsCalculator(String userID, String climberName, String placeName, String routeName, DatabaseReference myRef3, Routes route, String climbStyle) {
        String reference = user.getUid() + "/" + climberName + "/" + placeName + "/" + routeName;
        Query boolReference = FirebaseDatabase.getInstance().getReference();
        boolReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("/" + user.getUid() + "/" + climberName + "/" + placeName + "/" + routeName)) {
                    myRef3.child(placeName).child(routeName).child("name").setValue(route.name);
                    myRef3.child(placeName).child(routeName).child("difficulty").setValue(route.difficulty);
                    //Now, if someone climbs a route clean, then also front, it does not
                    //add more points, but it changes the climbStyle to "Front".
                    myRef3.child(placeName).child(routeName).child("climbStyle").setValue(climbStyle);

                    database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
                    Query routesQuery2 = database.getReference(reference);
                    routesQuery2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Routes climberPointsInside = dataSnapshot.getValue(Routes.class);
                            assert climberPointsInside != null;
                            double routePoints = climberPointsInside.points;
                            double pointsToAdd = pointsAccordingToStyles(climbStyle, route);

                            int isMoreOrLess = Double.compare(routePoints, pointsToAdd);

                            if (isMoreOrLess < 0) {
                                Toast.makeText(getApplicationContext(), "Exist, but " + climberName + " earned more points!", Toast.LENGTH_LONG).show();

                                myRef3.child(placeName).child(routeName).child("points").setValue(pointsToAdd);
                                DatabaseReference myRefPoints = database.getReference(userID + "/teamPoints");
                                myRefPoints.setValue((teamPoints - routePoints) + pointsToAdd);

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
                    double pointsToAddNotExist = pointsAccordingToStyles(climbStyle, route);

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

    private double pointsAccordingToStyles(String climbStyle, Routes route){
        double points;
        if (climbStyle.equals("Top-rope")) {
            points = route.points * 0.5;
        } else if (climbStyle.equals("Clean")) {
            points = route.points * 2;
        } else {
            points = route.points;
        }
        return points;
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

    @SuppressLint("SetTextI18n")
    private void addCustomSpinner(Routes mRouteItemToAdd, String placeName) {

        View customRoutesView = new View(this);
        LinearLayout.LayoutParams customViewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        customRoutesView.setLayoutParams(customViewParams);

        //if my custom button is true
        customRoutesView = LayoutInflater.from(this).inflate(
                R.layout.custom_view_layout, routeLayout, false
        );

        ImageView imageViewDiffImage = customRoutesView.findViewById(R.id.routeDiffImageView);
        TextView textViewRouteName = customRoutesView.findViewById(R.id.routeNameTextView);
        TextView textViewRouteDiff = customRoutesView.findViewById(R.id.routeDiffTextView);
        ImageButton customButton = customRoutesView.findViewById(R.id.customButton);
        RadioButton climberNameOne = customRoutesView.findViewById(R.id.climberNameOne);
        RadioButton climberNameTwo = customRoutesView.findViewById(R.id.climberNameTwo);
        Button climbedItButton = customRoutesView.findViewById(R.id.climbed_it_button);
        RadioGroup climberNameRadioGroup = customRoutesView.findViewById(R.id.climberNameRadioGroup);
        RadioGroup climbingStyleRadioGroup = customRoutesView.findViewById(R.id.styleNameRadioGroup);
        RelativeLayout routeWhoClimbed = customRoutesView.findViewById(R.id.routeWhoClimbedRelativeLayout);

        imageViewDiffImage.setImageResource(R.mipmap.muscle);
        textViewRouteName.setText(mRouteItemToAdd.name);
        textViewRouteDiff.setText("Difficulty: " + (int) mRouteItemToAdd.difficulty);

        climberNameOne.setText(climberName1);
        climberNameTwo.setText(climberName2);

        routeWhoClimbed.setVisibility(GONE);

        customButton.setImageResource(R.drawable.arrow_anim_start);
        customButton.setOnClickListener(new View.OnClickListener() {
            boolean isCustomButtonClicked = true;

            @Override
            public void onClick(View v) {
                if (isCustomButtonClicked) {
                    customButton.setImageResource(R.drawable.avd_anim_arrow_blue_back);
                    Drawable d = customButton.getDrawable();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (d instanceof AnimatedVectorDrawable) {
                            animArrowAnim = (AnimatedVectorDrawable) d;
                            animArrowAnim.start();
                        }
                    }
                    routeWhoClimbed.setVisibility(View.VISIBLE);
                    isCustomButtonClicked = false;
                } else if (!isCustomButtonClicked) {
                    customButton.setImageResource(R.drawable.avd_anim_arrow_blue);
                    Drawable d = customButton.getDrawable();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (d instanceof AnimatedVectorDrawable) {
                            animArrowAnim = (AnimatedVectorDrawable) d;
                            animArrowAnim.start();
                        }
                    }
                    routeWhoClimbed.setVisibility(GONE);
                    isCustomButtonClicked = true;
                }
            }
        });

        climbedItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int checkedNameButton = climberNameRadioGroup.getCheckedRadioButtonId();
                int checkedStyleButton = climbingStyleRadioGroup.getCheckedRadioButtonId();
                RadioButton checkedNameRadioButton = (RadioButton) findViewById(checkedNameButton);
                RadioButton checkedStyleRadioButton = (RadioButton) findViewById(checkedStyleButton);
                String checkedName = (String) checkedNameRadioButton.getText();
                String checkedStyle = (String) checkedStyleRadioButton.getText();

                addClimbToDatabase(user.getUid(), checkedName, mRouteItemToAdd, placeName, checkedStyle);
            }
        });

        routeLayout.addView(customRoutesView);
    }

    private void dateChecker(ImageButton roka, ImageButton kecske, ImageButton francia, ImageButton svab){
        Calendar currentDateCalendar = Calendar.getInstance();
        Calendar startFirstDateCalendar = Calendar.getInstance();
        startFirstDateCalendar.set(2021, 8, 24, 0, 0);
        Calendar endFirstDateCalendar = Calendar.getInstance();
        endFirstDateCalendar.set(2021, 9, 3, 23, 59);
        Calendar startSecondDateCalendar = Calendar.getInstance();
        startSecondDateCalendar.set(2021, 9, 2, 0,0);
        Calendar endSecondDateCalendar = Calendar.getInstance();
        endSecondDateCalendar.set(2021, 9, 10, 20, 00);

        if (currentDateCalendar.before(startFirstDateCalendar) || currentDateCalendar.after(endFirstDateCalendar)){
            francia.setEnabled(false);
            svab.setEnabled(false);
        } else {
            francia.setEnabled(true);
            svab.setEnabled(true);
        }

        if (currentDateCalendar.before(startSecondDateCalendar) || currentDateCalendar.after(endSecondDateCalendar)) {
            kecske.setEnabled(false);
            roka.setEnabled(false);
        } else {
            kecske.setEnabled(true);
            roka.setEnabled(true);
        }
    }
}


