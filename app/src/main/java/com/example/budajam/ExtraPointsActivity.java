package com.example.budajam;

import static android.view.View.GONE;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

//ToDo: Handle extra activities for teams or for climbers.
public class ExtraPointsActivity extends AppCompatActivity {
    FirebaseUser user;
    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authListener;
    FirebaseDatabase database;
    String climberName1, climberName2;
    HashMap<String, List<HashMap<String, Pair<String, Integer>>>> qualityMap = new HashMap<>();
    private AnimatedVectorDrawable animArrowAnim;
    Double teamPoints;

    Integer pointsInDB;
    int points;
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
    //ToDo: add points to teams activities. No Spinner, but textfield.
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

    private void addCustomDropDown(HashMap<String, List<HashMap<String, Pair<String, Integer>>>> qualityMap) {
        LinearLayout popupLinear = findViewById(R.id.popupLinear);
        popupLinear.removeAllViews();
        database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
        DatabaseReference myRef = database.getReference(user.getUid() + "/Activities/");

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
            TextView activityText = customRoutesView.findViewById(R.id.mostPointsTextView);
            EditText editText = customRoutesView.findViewById(R.id.pointsEarned);

            //teams - climbers
            for (HashMap<String, Pair<String, Integer>> map : qualityMap.get(activityName)){
                if (map.containsKey("teams")) {
                    editText.setVisibility(View.VISIBLE);
                    qualitySpinner.setVisibility(GONE);
                    climberNameRadioGroup.clearCheck();
                    for (View v : climberNameRadioGroup.getTouchables()){
                        v.setEnabled(false);
                    }
                    break;
                } else {
                    editText.setVisibility(View.GONE);
                    climberNameRadioGroup.clearCheck();
                    for (View v : climberNameRadioGroup.getTouchables()){
                            v.setEnabled(true);
                    }
                }
            };

            View.OnClickListener radioButtonClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int checkedNameButton = climberNameRadioGroup.getCheckedRadioButtonId();
                    RadioButton checkedNameRadioButton = (RadioButton) findViewById(checkedNameButton);
                    String checkedName = (String) checkedNameRadioButton.getText();
                    ImageButton removeButton = customRoutesView.findViewById(R.id.removeButtonImageButton);
                    //Do smthng
                    //int points = getClimberPointsActivity(activityName, checkedName, activityText);

                    database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
                    DatabaseReference myRef = database.getReference(user.getUid() + "/Activities/" + activityName);
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(checkedName)) {
                                points = snapshot.child(checkedName).getValue(Integer.class);
                                activityText.setText("Currently, " + checkedName + " earned: " + points + " points.");
                                removeButton.setVisibility(View.VISIBLE);
                                removeButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        myRef.child(checkedName).removeValue();
                                        DatabaseReference myRefPoints = database.getReference(user.getUid());
                                        myRefPoints.child("teamPoints").setValue(teamPoints - points);
                                        activityText.setText("Currently, " + checkedName + " did not do this activity.");
                                        removeButton.setVisibility(GONE);
                                    }
                                });
                            } else {
                                points = 0;
                                activityText.setText("Currently, " + checkedName + " did not do this activity.");
                                removeButton.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            //ToDo:
                        }
                    });
                }
            };

            climberNameOne.setText(climberName1);
            climberNameTwo.setText(climberName2);
            climberNameOne.setOnClickListener(radioButtonClickListener);
            climberNameTwo.setOnClickListener(radioButtonClickListener);

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

                        for (HashMap<String, Pair<String, Integer>> map : qualityMap.get(activityName)) {
                            for (Pair<String, Integer> pair : map.values()) {
                                spinnerHelper.add((String) pair.first);
                            }
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
            for (HashMap<String, Pair<String, Integer>> map : qualityMap.get(activityName)) {
                if (map.containsKey("teams")) {
                    setCurrentActivityDetails(activityName, customRoutesView, "teams");
                    break;
                } else setCurrentActivityDetails(activityName, customRoutesView, "climbers");
            }
            ActivityNameView.setText(activityName);
            climbedItButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String selected = (String) qualitySpinner.getSelectedItem();
                    int points = 0;
                    for (HashMap<String, Pair<String, Integer>> map : qualityMap.get(activityName)) {
                        if (map.containsKey("climbers")) {
                            for (Pair<String, Integer> pair : map.values()) {
                                String first = (String) pair.first;
                                if (first.equals(selected)) {
                                    points = (int) pair.second;
                                }
                            }

                            int checkedNameButton = climberNameRadioGroup.getCheckedRadioButtonId();
                            if (checkedNameButton != -1) {
                                RadioButton checkedNameRadioButton = (RadioButton) findViewById(checkedNameButton);
                                String checkedName = (String) checkedNameRadioButton.getText();
                                addPointsToDatabaseClimbers(points, activityName, checkedName, activityText);
                            }
                            else Toast.makeText(ExtraPointsActivity.this, "Select a climber!", Toast.LENGTH_LONG).show();
                        } else {
                            String pointsText = editText.getText().toString();
                            if (pointsText.matches("\\d+")) {
                                points = Integer.parseInt(pointsText);
                                addPointsToDatabase(points, activityName, activityText);
                                editText.setEnabled(false);
                                climbedItButton.setEnabled(false);
                            } else Toast.makeText(ExtraPointsActivity.this, "This is not a number, Bro!", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
            popupLinear.addView(customRoutesView);
        }
    }

    private void addPointsToDatabaseClimbers(int points, String activityName, String checkedName, TextView textView) {
        database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
        DatabaseReference myRefPoints = database.getReference(user.getUid() +"/teamPoints");
        DatabaseReference myRef = database.getReference(user.getUid() + "/Activities/" + activityName);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(checkedName)) {
                    Query activityQuery = myRef.child(checkedName);
                    activityQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            double pointsInDB = dataSnapshot.getValue(Integer.class);

                            int isMoreOrLess = Double.compare(pointsInDB, points);

                            if (isMoreOrLess < 0) {
                                Dialog dialog = dialogBuilderFunc(true, true);
                                dialog.show();
                                myRef.child(checkedName).setValue(points);
                                myRefPoints.setValue((teamPoints - pointsInDB) + points);
                                textView.setText("Currently, " + checkedName + " earned: " + points + " points.");
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

                    myRef.child(checkedName).setValue(points);
                    myRefPoints.setValue(teamPoints + points);
                    textView.setText("Currently, " + checkedName + " earned: " + points + " points.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private int getClimberPointsActivity(String activity, String climberName, TextView activityText){
        database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
        DatabaseReference myRef = database.getReference(user.getUid() + "/Activities/" + activity);
        final int[] points = new int[1];
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(climberName)) {
                    points[0] = snapshot.getValue(Integer.class);
                } else {
                    points[0] = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //ToDo:
            }
        });
        return points[0];
    }

    private void setCurrentActivityDetails(String activityName, View customRoutesView, String group) {
        //ToDO: refresh the point-display after an activity is documented.
        database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
        DatabaseReference myRef = database.getReference(user.getUid() + "/Activities/");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(activityName)) {
                    LinearLayout activities = customRoutesView.findViewById(R.id.activities);
                    activities.setVisibility(View.VISIBLE);
                    ImageButton removeButton = customRoutesView.findViewById(R.id.removeButtonImageButton);
                    TextView editText = customRoutesView.findViewById(R.id.pointsEarned);
                    Button climbedItButton = customRoutesView.findViewById(R.id.climbed_it_button);

                    Query activityQuery = myRef.child(activityName);
                    if (group.equals("climbers")) {
                        RadioGroup climberNameRadioGroup = customRoutesView.findViewById(R.id.climberNameRadioGroup);
                        climberNameRadioGroup.clearCheck();
                        TextView activityText = customRoutesView.findViewById(R.id.mostPointsTextView);
                        activityText.setText("Select a climber!");
                        removeButton.setVisibility(GONE);
                    }
                    else {
                        removeButton.setVisibility(View.VISIBLE);
                        activityQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                pointsInDB = dataSnapshot.getValue(Integer.class);
                                TextView activityText = customRoutesView.findViewById(R.id.mostPointsTextView);
                                activityText.setText("Currently, your team earned: " + pointsInDB + " points.");
                                editText.setEnabled(false);
                                climbedItButton.setEnabled(false);
                                removeButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        myRef.child(activityName).removeValue();
                                        DatabaseReference myRefPoints = database.getReference(user.getUid());
                                        myRefPoints.child("teamPoints").setValue(teamPoints - pointsInDB);
                                        activityText.setText("Team did not do this activity yet!");
                                        removeButton.setVisibility(GONE);
                                        editText.setEnabled(true);
                                        climbedItButton.setEnabled(true);
                                        Toast.makeText(ExtraPointsActivity.this, "Points removed!", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    //Somehow this is the devil and makes the DB cry. :) If sh*t hits the fan, use DBexport and import it.
                    //Update - its alive!
                } else {
                    LinearLayout activities = customRoutesView.findViewById(R.id.activities);
                    activities.setVisibility(View.VISIBLE);
                    ImageButton removeButton = customRoutesView.findViewById(R.id.removeButtonImageButton);
                    TextView editText = customRoutesView.findViewById(R.id.pointsEarned);
                    Spinner qualitySpinner = customRoutesView.findViewById(R.id.category);
                    if (group.equals("climbers")) {
                        RadioGroup climberNameRadioGroup = customRoutesView.findViewById(R.id.climberNameRadioGroup);
                        climberNameRadioGroup.clearCheck();
                        editText.setVisibility(View.GONE);
                        for (View v : climberNameRadioGroup.getTouchables()){
                            v.setEnabled(true);
                        }
                        TextView activityText = customRoutesView.findViewById(R.id.mostPointsTextView);
                        activityText.setText("Select a climber!");
                        removeButton.setVisibility(GONE);
                    } else {
                        RadioGroup climberNameRadioGroup = customRoutesView.findViewById(R.id.climberNameRadioGroup);
                        climberNameRadioGroup.clearCheck();
                        editText.setVisibility(View.VISIBLE);
                        qualitySpinner.setVisibility(GONE);
                        for (View v : climberNameRadioGroup.getTouchables()){
                            v.setEnabled(false);
                        }
                        TextView activityText = customRoutesView.findViewById(R.id.mostPointsTextView);
                        //ToDo: Somethings wrong with the trashcan imagebutton.
                        activityText.setText("Team did not do this activity yet!");
                        removeButton.setVisibility(GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //
            }
        });
    }

    private void addPointsToDatabase(int points, String activityName, TextView textView) {
        database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
        DatabaseReference myRefPoints = database.getReference(user.getUid() +"/teamPoints");
        DatabaseReference myRef = database.getReference(user.getUid() + "/Activities/");

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
                                textView.setText("Currently, your team earned: " + points + " points.");
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
                    textView.setText("Currently, your team earned: " + points + " points.");
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
                builder.setMessage("It was fun, wasn't it? But you had already more points for this.")
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
