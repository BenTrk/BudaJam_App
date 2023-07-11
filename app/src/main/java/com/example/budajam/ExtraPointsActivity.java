package com.example.budajam;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExtraPointsActivity extends AppCompatActivity {
    FirebaseUser user;
    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authListener;
    FirebaseDatabase database;
    String climberName1, climberName2;
    HashMap<String, List<Pair<String, Integer>>> qualityMap = new HashMap<>();
    private AnimatedVectorDrawable animArrowAnim;
    Double teamPoints;

    Integer pointsInDB;
    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extrapoints);

        auth = FirebaseAuth.getInstance();
        authListener = firebaseAuth -> {
            user = firebaseAuth.getCurrentUser();
            if (user != null) {
                getNamesFromDatabase(user.getUid());
            }
            else {
                startActivity(new Intent(ExtraPointsActivity.this, LoginActivity.class));
                finish();
            }
        };

        setCustomSpinner();

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ExtraPointsActivity.this, MainActivity.class));
                finish();
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
    private void setCustomSpinner(){
        database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
        Query activitiesQuery = database.getReference("Activities/");
        Button showActivities = (Button) findViewById(R.id.buttonShowDropDown);

        //Go through the Activities, collect stuff in qualityMap, do listing and stuff.
        activitiesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot qualitiesSnapshot : postSnapshot.getChildren()) {
                        String activityString = qualitiesSnapshot.getValue(String.class);
                        Activity activity = new Activity(activityString);
                        assert activity != null;
                        qualityMap.put(postSnapshot.getKey(), activity.getQualityPairs());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "DB Error", Toast.LENGTH_LONG).show();
            }
        });

        View.OnClickListener handler = v -> {
            if (v.getId() == R.id.buttonShowDropDown) {
                addCustomDropDown(qualityMap);
            }
        };
        showActivities.setOnClickListener(handler);
    }

    private void addCustomDropDown(HashMap<String, List<Pair<String, Integer>>> qualityMap) {
        //ToDo: Show current points for an activity and provide functionality to remove it.
        LinearLayout popupLinear = findViewById(R.id.popupLinear);
        popupLinear.removeAllViews();

        for (String activityName : qualityMap.keySet()) {
            final View customRoutesView = LayoutInflater.from(this).inflate(
                    R.layout.custom_extrapoints_layout, popupLinear, false
            );
            LinearLayout.LayoutParams customViewParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            customRoutesView.setLayoutParams(customViewParams);

            TextView ActivityNameView = customRoutesView.findViewById(R.id.routeNameTextView);
            ImageButton customButton = customRoutesView.findViewById(R.id.activityDropDownButton);
            Spinner qualitySpinner = customRoutesView.findViewById(R.id.category);
            RadioGroup climberNameRadioGroup = customRoutesView.findViewById(R.id.climberNameRadioGroup);
            RadioButton climberNameOne = customRoutesView.findViewById(R.id.climberNameOne);
            RadioButton climberNameTwo = customRoutesView.findViewById(R.id.climberNameTwo);
            Button climbedItButton = customRoutesView.findViewById(R.id.climbed_it_button);
            RelativeLayout routeWhoClimbed = customRoutesView.findViewById(R.id.routeWhoActivityRelativeLayout);

            climberNameOne.setText(climberName1);
            climberNameTwo.setText(climberName2);

            routeWhoClimbed.setVisibility(GONE);

            customButton.setImageResource(R.drawable.arrow_anim_start);
            customButton.setOnClickListener(new View.OnClickListener() {
                boolean isCustomButtonClicked = true;

                @Override
                public void onClick(View v) {
                    List<String> spinnerHelper = new ArrayList<>();
                    if (isCustomButtonClicked) {
                        customButton.setImageResource(R.drawable.avd_anim_arrow_blue_back);
                        Drawable d = customButton.getDrawable();
                        if (d instanceof AnimatedVectorDrawable) {
                            animArrowAnim = (AnimatedVectorDrawable) d;
                            animArrowAnim.start();
                        }
                        for (Pair qualityPair : qualityMap.get(activityName)){
                            spinnerHelper.add((String) qualityPair.first);
                        }
                        String[] spinnerArray = new String[spinnerHelper.size()];
                        spinnerHelper.toArray(spinnerArray);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.activity_spinner_item, spinnerArray);
                        adapter.setDropDownViewResource(R.layout.activity_spinner_dropdown_item);
                        qualitySpinner.setAdapter(adapter);
                        routeWhoClimbed.setVisibility(View.VISIBLE);
                        isCustomButtonClicked = false;
                    } else {
                        customButton.setImageResource(R.drawable.avd_anim_arrow_blue);
                        Drawable d = customButton.getDrawable();
                        if (d instanceof AnimatedVectorDrawable) {
                            animArrowAnim = (AnimatedVectorDrawable) d;
                            animArrowAnim.start();
                        }
                        routeWhoClimbed.setVisibility(GONE);
                        isCustomButtonClicked = true;
                    }
                }
            });

            ActivityNameView.setText(activityName);
            setCurrentActivityDetails(activityName, customRoutesView);
            climbedItButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String selected = (String) qualitySpinner.getSelectedItem();
                    int points = 0;
                    for (Pair pair : qualityMap.get(activityName)){
                        String first = (String) pair.first;
                        if (first.equals(selected)){
                            points = (int) pair.second;
                        }
                    }
                    addPointsToDatabase(points, activityName);
                }
            });
            popupLinear.addView(customRoutesView);
        }
    }

    private void setCurrentActivityDetails(String activityName, View customRoutesView) {
        database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
        DatabaseReference myRef = database.getReference(user.getUid() + "/Activities/");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(activityName)) {
                    LinearLayout activities = customRoutesView.findViewById(R.id.activities);
                    activities.setVisibility(View.VISIBLE);

                    Query activityQuery = myRef.child(activityName);
                    activityQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.d("Points", "Points in DB: " + dataSnapshot.getValue(Integer.class));
                            pointsInDB = dataSnapshot.getValue(Integer.class);
                            TextView activityText = customRoutesView.findViewById(R.id.mostPointsTextView);
                            activityText.setText("Currently, you earned: " + pointsInDB + " points.");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    ImageButton removeButton = customRoutesView.findViewById(R.id.removeButtonImageButton);

                    //Somehow this is the devil and makes the DB cry. :) If sh*t hits the fan, use DBexport and import it.
                    //Update - its alive!
                    removeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            myRef.child(activityName).removeValue();
                            DatabaseReference myRefPoints = database.getReference(user.getUid());
                            myRefPoints.child("teamPoints").setValue(teamPoints - pointsInDB);
                            activities.setVisibility(View.GONE);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //
            }
        });
    }

    private void addPointsToDatabase(int points, String activityName) {
        database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
        DatabaseReference myRefPoints = database.getReference(user.getUid() +"/teamPoints");
        DatabaseReference myRef = database.getReference(user.getUid() + "/Activities/");
        //myRefPoints.setValue((teamPoints - routePoints) + pointsToAdd);
        Toast.makeText(ExtraPointsActivity.this, "Points for this: " + points, Toast.LENGTH_LONG).show();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(activityName)) {
                    Query activityQuery = myRef.child(activityName);
                    activityQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            double pointsInDB = dataSnapshot.getValue(Integer.class);

                            int isMoreOrLess = Double.compare(pointsInDB, points);

                            if (isMoreOrLess < 0) {
                                Dialog dialog = dialogBuilderFunc(true, true);
                                dialog.show();
                                myRef.child(activityName).setValue(points);
                                myRefPoints.setValue((teamPoints - pointsInDB) + points);
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
                    Dialog dialog = dialogBuilderFunc(false, false);
                    dialog.show();

                    myRef.child(activityName).setValue(points);
                    myRefPoints.setValue(teamPoints + points);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public Dialog dialogBuilderFunc(boolean exists, boolean isMore){
        AlertDialog dialog;
        if (!exists) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ExtraPointsActivity.this);
            builder.setMessage("Well done, doing extra work!")
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
                AlertDialog.Builder builder = new AlertDialog.Builder(ExtraPointsActivity.this);
                builder.setMessage("Wow, even extra above the extra! More points earned!")
                        .setTitle(R.string.dialog_climbed_title);
                builder.setNeutralButton(R.string.dialog_climbed_neutral, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                dialog = builder.create();
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(ExtraPointsActivity.this);
                builder.setMessage("It was fun, wasn't it? But team had already more points for this.")
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
