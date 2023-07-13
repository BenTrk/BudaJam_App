package com.example.budajam;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.example.budajam.classes.ClimberNames;
import com.example.budajam.classes.DataStorage;
import com.example.budajam.classes.Routes;
import com.example.budajam.controllers.MainController;
import com.example.budajam.sqlhelper.RoutesSQLiteDBHelper;
import com.example.budajam.views.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static android.view.View.GONE;

//Now it is synched - SQLite is not really needed
//Use it for a save as xml activity - let the user add the days when the app should save the climbed routes
//to an xml as a backup.

//Important: Before launch, make sure climbers cannot add climbs to the database outside of the given dates!

public class MainActivity extends AppCompatActivity {
    private ImageView menuButton;
    private AnimatedVectorDrawable animMenu, animArrowAnim;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout routeLayout, emptyScreenLinear;

    public static String climberName1;
    public static String climberName2;
    public static double teamPoints;
    public DataStorage routes = new DataStorage();

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseUser user;
    String[] popUpContents;
    Button buttonShowDropDown;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the view
        setContentView(R.layout.activity_main);

        //Initialize routes in MainModel. WARNING: timeing issue, if it takes longer to grab the data,
        // it could happen that the screen shows empty.
        MainController.init();

        //Get the user.
        auth = FirebaseAuth.getInstance();
        //Check authentication, if none -> LoginActivity
        authListener = firebaseAuth -> {
            user = firebaseAuth.getCurrentUser();
            if (user == null) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        };

        //Set User details and Route details synced.
        FirebaseUser user = auth.getCurrentUser();
        if (MainController.setUserAndRouteSynced(auth, user)) {
            MainController.getNamesFromDatabase(user.getUid());
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        //Setting up the view items
        emptyScreenLinear = (LinearLayout) findViewById(R.id.emptyScreenLinear);
        routeLayout = (LinearLayout) findViewById(R.id.routeLayout);
        LinearLayout linearLayout = findViewById(R.id.linearLayout);
        menuButton = (ImageView) findViewById(R.id.menuButton);
        buttonShowDropDown = (Button) findViewById(R.id.buttonShowDropDown);

        //Refreshing screen to default when clicked outside of routeLayout
        linearLayout.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //some code....
                    break;
                case MotionEvent.ACTION_UP:
                    routeLayout.removeAllViews();
                    emptyScreenLinear.setVisibility(View.VISIBLE);
                    routeLayout.addView(emptyScreenLinear);
                    v.performClick();
                    break;
                default:
                    break;
            }
            return true;
        });

        //Show the menu, upper left corner
        menuButton.setOnClickListener(v -> {
            Drawable d = menuButton.getDrawable();
            if (d instanceof AnimatedVectorDrawable) {
                animMenu = (AnimatedVectorDrawable) d;
                animMenu.start();
            }

            //Create the dropdown
            Context wrapper = new ContextThemeWrapper(MainActivity.this, R.style.MyMenu);
            PopupMenu popup = new PopupMenu(wrapper, menuButton);
            popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

            //Set what happens when items are selected in menu
            popup.setOnMenuItemClickListener(item -> {
                if (R.id.one == item.getItemId()) {
                    startActivity(new Intent(MainActivity.this, CheckOutActivity.class));
                    finish();
                }
                else if (R.id.two == item.getItemId()) {
                    startActivity(new Intent(MainActivity.this, OptionsActivity.class));
                    finish();
                }
                else if (R.id.four == item.getItemId()) {
                    startActivity(new Intent(MainActivity.this, ExtraPointsActivity.class));
                    finish();
                }
                else if (R.id.three == item.getItemId()) {
                    auth.signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
                return true;
            });
            popup.show();
        });

        //Set the onclicklistener on dropdown button to
        // show the list of places when the dropdown button is selected.
        buttonShowDropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                routeLayout.removeAllViews();
                if (MainController.getPlacesFromDatabase(MainActivity.this, routeLayout, buttonShowDropDown).isEmpty()){
                    Toast.makeText(MainActivity.this, "Could not grab the routes yet. Try again the button in 5 seconds!", Toast.LENGTH_SHORT).show();
                }
                for (String place : MainController.getPlacesFromDatabase(MainActivity.this, routeLayout, buttonShowDropDown)) {
                    //For every place create the custom dropdown
                    final View customRoutesView = LayoutInflater.from(MainActivity.this).inflate(
                            R.layout.custom_dropdown_layout, routeLayout, false
                    );
                    LinearLayout.LayoutParams customViewParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    customRoutesView.setLayoutParams(customViewParams);

                    RelativeLayout relContainer = customRoutesView.findViewById(R.id.routeDropDownRelativeLayout);
                    TextView placeNameView = customRoutesView.findViewById(R.id.routeNameTextView);
                    placeNameView.setText(place);

                    relContainer.setOnClickListener(v -> {
                        routeLayout.removeAllViews();
                        //On click, populate the route list - create custom view for them and set the behavior
                        populateRouteList(place);
                    });
                    routeLayout.addView(customRoutesView);
                }
            }
        });
        //dateChecker(roka, kecske, francia, svab); - if necessary
    }
    private void setCustomSpinner(){
        //popUpContents = new String[places.size()];
        //places.toArray(popUpContents);
        buttonShowDropDown = (Button) findViewById(R.id.buttonShowDropDown);
        View.OnClickListener handler = v -> {
            if (v.getId() == R.id.buttonShowDropDown) {
                //addCustomDropDown(popUpContents);
            }
        };
        buttonShowDropDown.setOnClickListener(handler);
    }

    private void getPlacesFromDB(List<String> places) {
        database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
        Query routesQuery = database.getReference("Routes/");
        routesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String routeName = postSnapshot.getKey();
                    places.add(routeName);
                }
                //setCustomSpinner();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "DB Error", Toast.LENGTH_LONG).show();
            }
        });
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
            public void onCancelled(@NonNull DatabaseError error) {
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
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Routes climberPointsInside = dataSnapshot.getValue(Routes.class);
                            assert climberPointsInside != null;
                            double routePoints = climberPointsInside.points;
                            double pointsToAdd = pointsAccordingToStyles(climbStyle, route);

                            int isMoreOrLess = Double.compare(routePoints, pointsToAdd);

                            if (isMoreOrLess < 0) {
                                Dialog dialog = dialogBuilderFunc(true, true);
                                dialog.show();
                                myRef3.child(placeName).child(routeName).child("points").setValue(pointsToAdd);
                                myRef3.child(placeName).child(routeName).child("best").setValue(climbStyle);
                                DatabaseReference myRefPoints = database.getReference(userID + "/teamPoints");
                                myRefPoints.setValue((teamPoints - routePoints) + pointsToAdd);
                            } else {
                                Dialog dialog = dialogBuilderFunc(true, false);
                                dialog.show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            //
                        }
                    });
                } else {
                    double pointsToAddNotExist = pointsAccordingToStyles(climbStyle, route);

                    Dialog dialog = dialogBuilderFunc(false, false);
                    dialog.show();

                    myRef3.child(placeName).child(routeName).child("name").setValue(route.name);
                    myRef3.child(placeName).child(routeName).child("difficulty").setValue(route.difficulty);
                    myRef3.child(placeName).child(routeName).child("climbStyle").setValue(climbStyle);
                    myRef3.child(placeName).child(routeName).child("points").setValue(pointsToAddNotExist);
                    myRef3.child(placeName).child(routeName).child("best").setValue(climbStyle);

                    DatabaseReference myRefPoints = database.getReference(userID + "/teamPoints");
                    myRefPoints.setValue(teamPoints + pointsToAddNotExist);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //it also counts duplicated points! So here's what you need.
        //Get the points for each climbed routes from the database
        //And count them. That's all, easypeasy.
    }

    public Dialog dialogBuilderFunc(boolean exists, boolean isMore){
        AlertDialog dialog;
        if (!exists) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(R.string.dialog_climbed_message)
                    .setTitle(R.string.dialog_climbed_title);
            builder.setNeutralButton(R.string.dialog_climbed_neutral, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            dialog = builder.create();
        }
        else {
            if (isMore){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.dialog_climbedmorepoints_message)
                        .setTitle(R.string.dialog_climbed_title);
                builder.setNeutralButton(R.string.dialog_climbed_neutral, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                dialog = builder.create();
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.dialog_climbedalready_message)
                        .setTitle(R.string.dialog_climbed_title);
                builder.setNeutralButton(R.string.dialog_climbed_neutral, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                dialog = builder.create();
            }
        }
        return dialog;
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

    private void populateRouteList(String name) {
        //Set the visuals
        ProgressBar progressBar = findViewById(R.id.progressBarPlaces);
        LinearLayout progressLayout = findViewById(R.id.progressLinear);
        progressLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        emptyScreenLinear.setVisibility(GONE);

        //For each route, create a custom spinner
        for (Routes routesIn : MainController.getRoutes(name)) {
            addCustomSpinner(routesIn, name);
        }
        //When done, remove progressbar
        progressBar.setVisibility(View.GONE);
    }

    @SuppressLint("SetTextI18n")
    private void addCustomSpinner(Routes mRouteItemToAdd, String placeName) {

        //Inflate the custom routeview
        final View customRoutesView = LayoutInflater.from(this).inflate(
                R.layout.custom_view_layout, routeLayout, false
        );
        LinearLayout.LayoutParams customViewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        customRoutesView.setLayoutParams(customViewParams);

        //Set up the views
        TextView textViewRouteName = customRoutesView.findViewById(R.id.routeNameTextView);
        TextView textViewRouteDiff = customRoutesView.findViewById(R.id.routeDiffTextView);
        ImageButton customButton = customRoutesView.findViewById(R.id.customButton);
        RadioButton climberNameOne = customRoutesView.findViewById(R.id.climberNameOne);
        RadioButton climberNameTwo = customRoutesView.findViewById(R.id.climberNameTwo);
        Button climbedItButton = customRoutesView.findViewById(R.id.climbed_it_button);
        RadioGroup climberNameRadioGroup = customRoutesView.findViewById(R.id.climberNameRadioGroup);
        RadioGroup climbingStyleRadioGroup = customRoutesView.findViewById(R.id.styleNameRadioGroup);
        RelativeLayout routeWhoClimbed = customRoutesView.findViewById(R.id.routeWhoClimbedRelativeLayout);

        //Set up route details
        textViewRouteName.setText(mRouteItemToAdd.name);
        textViewRouteDiff.setText("Difficulty: " + (int) mRouteItemToAdd.difficulty);
        //Set up climber names in radiogroup
        climberNameOne.setText(MainController.getNames()[0]);
        climberNameTwo.setText(MainController.getNames()[1]);

        //At first, don't show the part where you can select who climbed
        routeWhoClimbed.setVisibility(GONE);

        //Set onclicklistener to show the part where you can select who climbed.
        customButton.setImageResource(R.drawable.arrow_anim_start);
        customButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (routeWhoClimbed.getVisibility() == GONE) {
                    customButton.setImageResource(R.drawable.avd_anim_arrow_blue_back);
                    if (customButton.getDrawable() instanceof AnimatedVectorDrawable) {
                        animArrowAnim = (AnimatedVectorDrawable) customButton.getDrawable();
                        animArrowAnim.start();
                    }
                    routeWhoClimbed.setVisibility(View.VISIBLE);
                } else {
                    customButton.setImageResource(R.drawable.avd_anim_arrow_blue);
                    if (customButton.getDrawable() instanceof AnimatedVectorDrawable) {
                        animArrowAnim = (AnimatedVectorDrawable) customButton.getDrawable();
                        animArrowAnim.start();
                    }
                    routeWhoClimbed.setVisibility(GONE);
                }
            }
        });

        climbedItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RadioButton checkedNameRadioButton = (RadioButton) findViewById(climberNameRadioGroup.getCheckedRadioButtonId());
                RadioButton checkedStyleRadioButton = (RadioButton) findViewById(climbingStyleRadioGroup.getCheckedRadioButtonId());

                //ToDo: Continue from here
                String checkedName = (String) checkedNameRadioButton.getText();
                String checkedStyle = (String) checkedStyleRadioButton.getText();
                addClimbToDatabase(user.getUid(), checkedName, mRouteItemToAdd, placeName, checkedStyle);

                routeLayout.removeAllViews();
                emptyScreenLinear.setVisibility(View.VISIBLE);
                routeLayout.addView(emptyScreenLinear);
            }
        });

        routeLayout.addView(customRoutesView);
    }

    private void addCustomDropDown(String[] places) {

        routeLayout.removeAllViews();
        for (String place : places) {
            final View customRoutesView = LayoutInflater.from(this).inflate(
                    R.layout.custom_dropdown_layout, routeLayout, false
            );
            LinearLayout.LayoutParams customViewParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            customRoutesView.setLayoutParams(customViewParams);

            RelativeLayout relContainer = customRoutesView.findViewById(R.id.routeDropDownRelativeLayout);
            TextView placeNameView = customRoutesView.findViewById(R.id.routeNameTextView);

            placeNameView.setText(place);
            relContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    routeLayout.removeAllViews();
                    populateRouteList(place);
                }
            });
            routeLayout.addView(customRoutesView);
        }
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
        endSecondDateCalendar.set(2021, 9, 10, 20, 0);

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
}


