package com.example.budajam;

import static android.view.View.GONE;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.DeadObjectException;
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

import com.example.budajam.classes.Activity;
import com.example.budajam.classes.ClimberNames;
import com.example.budajam.classes.ExtraActivity;
import com.example.budajam.controllers.ExtraPointsController;
import com.example.budajam.controllers.CheckOutController;
import com.example.budajam.controllers.MainController;
import com.example.budajam.models.ExtraPointsModel;
import com.example.budajam.models.initModel;
import com.example.budajam.views.CheckOutActivity;
import com.example.budajam.views.LoginActivity;
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

public class ExtraPointsActivity extends AppCompatActivity {
    FirebaseUser user;
    FirebaseDatabase database;
    String climberName1, climberName2;
    HashMap<String, List<HashMap<String, Pair<String, Integer>>>> qualityMap = new HashMap<>();
    private AnimatedVectorDrawable animArrowAnim;
    Double teamPoints;

    Integer pointsInDB;
    LinearLayout popupLinear;
    double points;
    Button backButton, showDropDown;
    String checkedName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set layout view
        setContentView(R.layout.activity_extrapoints);

        //Check authentication
        if (ExtraPointsController.authenticate() == null) {
            startActivity(new Intent(ExtraPointsActivity.this, LoginActivity.class));
            finish();
        }

        popupLinear = findViewById(R.id.popupLinear);
        showDropDown = findViewById(R.id.buttonShowDropDown);
        showDropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCustomDropDown(initModel.getListOfActivities());
            }
        });

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupLinear.removeAllViews();
                startActivity(new Intent(ExtraPointsActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void addCustomDropDown(List<ExtraActivity> activityList) {
        popupLinear.removeAllViews();

        for (ExtraActivity activity : activityList) {
            View customRoutesView = createCustomView(popupLinear);

            TextView activityNameView = customRoutesView.findViewById(R.id.routeNameTextView);
            ImageButton customButton = customRoutesView.findViewById(R.id.activityDropDownButton);
            Spinner qualitySpinner = customRoutesView.findViewById(R.id.category);
            RadioGroup climberNameRadioGroup = customRoutesView.findViewById(R.id.climberNameRadioGroup);
            RadioButton climberNameOne = customRoutesView.findViewById(R.id.climberNameOne);
            RadioButton climberNameTwo = customRoutesView.findViewById(R.id.climberNameTwo);
            ImageButton removeButton = customRoutesView.findViewById(R.id.removeButtonImageButton);
            Button climbedItButton = customRoutesView.findViewById(R.id.climbed_it_button);
            RelativeLayout routeWhoClimbed = customRoutesView.findViewById(R.id.routeWhoActivityRelativeLayout);
            TextView activityText = customRoutesView.findViewById(R.id.mostPointsTextView);
            EditText editText = customRoutesView.findViewById(R.id.pointsEarned);

            climberName1 = MainController.getNames()[0];
            climberName2 = MainController.getNames()[1];
            climberNameOne.setText(climberName1);
            climberNameTwo.setText(climberName2);

            //teams - climbers difference in visuals
            if (activity.getGroup().equals("teams")) {
                editText.setVisibility(View.VISIBLE);
                qualitySpinner.setVisibility(GONE);
                climberNameRadioGroup.clearCheck();
                for (View v : climberNameRadioGroup.getTouchables()){
                    v.setEnabled(false);
                }
                if (ExtraPointsController.getTeamsActivities(activity.getName(), "Team")) {
                    activityText.setText("Currently, your team earned: " + ExtraPointsController.getActivityPointsFromSaved(activity.getName(), "Team") + " points.");
                    if (ExtraPointsController.getActivityPointsFromSaved(activity.getName(), "Team") > 0) {
                        removeButton.setVisibility(View.VISIBLE);
                        checkedName = "Team";
                    } else removeButton.setVisibility(GONE);
                } else {
                    activityText.setText("Currently, your team earned: 0 points.");
                    removeButton.setVisibility(GONE);
                }
            } else {
                editText.setVisibility(View.GONE);
                climberNameRadioGroup.clearCheck();
                for (View v : climberNameRadioGroup.getTouchables()){
                    v.setEnabled(true);
                }
                activityText.setText("Select a climber!");
                removeButton.setVisibility(GONE);
            }
            activityNameView.setText(activity.getName());

            //Set radiobutton to show the correct data and removebutton as necessary based on the selection

            View.OnClickListener radioButtonClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int checkedNameButton = climberNameRadioGroup.getCheckedRadioButtonId();
                        RadioButton checkedNameRadioButton = findViewById(checkedNameButton);
                        checkedName = (String) checkedNameRadioButton.getText();

                        if (ExtraPointsController.getTeamsActivities(activity.getName(), checkedName)) {
                            activityText.setText("Currently, " + checkedName + " earned: " +
                                    ExtraPointsController.getActivityPointsFromSaved(activity.getName(), checkedName) + " points.");
                            removeButton.setVisibility(View.VISIBLE);
                        } else {
                            points = 0;
                            activityText.setText("Currently, " + checkedName + " did not do this activity.");
                            removeButton.setVisibility(View.GONE);
                        }
                    }
                };

            climberNameOne.setOnClickListener(radioButtonClickListener);
            climberNameTwo.setOnClickListener(radioButtonClickListener);

            //Set remove button functionality
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //ToDo
                    ExtraPointsController.removeActivity(checkedName, activity);
                    activityText.setText("Currently, " + checkedName + " did not do this activity.");
                    removeButton.setVisibility(GONE);
                    if (checkedName == null){
                        activityText.setText("Currently, your team earned: 0 points.");
                        editText.setEnabled(true);
                        climbedItButton.setEnabled(true);
                    }
                }
            });

            routeWhoClimbed.setVisibility(GONE);

            //Animation onclick
            customButton.setImageResource(R.drawable.arrow_anim_start);
            customButton.setOnClickListener(new View.OnClickListener() {
                boolean isCustomButtonClicked = true;
                @Override
                public void onClick(View v) {
                    LinearLayout activities = customRoutesView.findViewById(R.id.activities);
                    if (isCustomButtonClicked) {
                        activities.setVisibility(View.VISIBLE);
                        customButton.setImageResource(R.drawable.avd_anim_arrow_blue_back);
                        Drawable d = customButton.getDrawable();
                        if (d instanceof AnimatedVectorDrawable) {
                            animArrowAnim = (AnimatedVectorDrawable) d;
                            animArrowAnim.start();
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.activity_spinner_item,
                                ExtraPointsController.adapterHelper(activity));
                        adapter.setDropDownViewResource(R.layout.activity_spinner_dropdown_item);
                        qualitySpinner.setAdapter(adapter);
                        routeWhoClimbed.setVisibility(View.VISIBLE);
                        isCustomButtonClicked = false;
                    } else {
                        activities.setVisibility(GONE);
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
            climbedItButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String selected = (String) qualitySpinner.getSelectedItem();
                    String pointsText = editText.getText().toString();
                    double pointsHere = ExtraPointsController.pointsForActivity(ExtraPointsActivity.this,
                            activity, selected, pointsText);

                    if (activity.getGroup().equals("climbers")){
                        if (climberNameRadioGroup.getCheckedRadioButtonId() != -1){
                            addPointsToDatabaseClimbers((int) pointsHere, activity.getName(), checkedName, activityText);
                            removeButton.setVisibility(View.VISIBLE);
                        } else Toast.makeText(ExtraPointsActivity.this, "Select a climber!", Toast.LENGTH_LONG).show();
                    } else {
                        addPointsToDatabase((int) pointsHere, activity.getName(), activityText);
                        editText.setEnabled(false);
                        climbedItButton.setEnabled(false);
                        removeButton.setVisibility(View.VISIBLE);
                    }
                }
            });
            popupLinear.addView(customRoutesView);
        }
    }

    private View createCustomView(LinearLayout popupLinear){
        View customRoutesView = LayoutInflater.from(this).inflate(
                R.layout.custom_extrapoints_layout, popupLinear, false
        );
        LinearLayout.LayoutParams customViewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        customRoutesView.setLayoutParams(customViewParams);
        return customRoutesView;
    }

    //ToDo
    private void addPointsToDatabaseClimbers(int points, String activityName, String checkedName, TextView textView) {
        database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
        DatabaseReference myRefPoints = database.getReference(initModel.getUser().getUid() +"/teamPoints");
        DatabaseReference myRef = database.getReference(initModel.getUser().getUid() + "/Activities/" + activityName);
        teamPoints = initModel.getTeamData().teamPoints;
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

    //ToDo
    private void addPointsToDatabase(int points, String activityName, TextView textView) {
        database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
        DatabaseReference myRefPoints = database.getReference(initModel.getUser().getUid() +"/teamPoints");
        DatabaseReference myRef = database.getReference(initModel.getUser().getUid() + "/Activities/");
        teamPoints = initModel.getTeamData().teamPoints;

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(activityName)) {
                    Query activityQuery = myRef.child(activityName + "/Team");
                    activityQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            double pointsInDB = dataSnapshot.getValue(Integer.class);

                            int isMoreOrLess = Double.compare(pointsInDB, points);

                            if (isMoreOrLess < 0) {
                                Dialog dialog = dialogBuilderFunc(true, true);
                                dialog.show();
                                myRef.child(activityName + "/Team").setValue(points);
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
    private void authenticate(){
        if (MainController.authentication() == null){
            startActivity(new Intent(ExtraPointsActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        authenticate();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        authenticate();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (MainController.authentication() != null) {
            authenticate();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        authenticate();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (MainController.authentication() != null) {
            authenticate();
        }
    }
}
