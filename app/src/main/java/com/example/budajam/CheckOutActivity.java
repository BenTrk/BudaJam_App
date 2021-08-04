package com.example.budajam;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class CheckOutActivity extends AppCompatActivity {

    //Spinner nameSpinner;
    Button backButton, allTheClimbs;

    int buttonID = 0;

    boolean removeClimbBool;

    RadioGroup toggle;
    RadioButton climber1OnSwitch, climber2OnSwitch;

    List<Routes> routes = new ArrayList<>();
    String[] places;

    double teamPointsCheckOut;
    private Drawable removeClimb, removeTextClimbs, removeClimbRight;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase database;

    private TextView pointsView;
    private static LinearLayout linearLayout;

    private ImageButton roka, kecskeCheck, francia, svab;

    String selectedName;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        toggle = (RadioGroup) findViewById(R.id.toggle);
        climber1OnSwitch = (RadioButton) findViewById(R.id.climber1onswitch);
        climber2OnSwitch = (RadioButton) findViewById(R.id.climber2onswitch);
        climber1OnSwitch.setText(MainActivity.climberName1);
        climber2OnSwitch.setText(MainActivity.climberName2);

        removeClimbBool = false;

        teamPointsCheckOut = MainActivity.teamPoints;

        pointsView = (TextView) findViewById(R.id.teamPointsView);

        //nameSpinner = (Spinner) findViewById(R.id.nameSpinner);
        backButton = (Button) findViewById(R.id.backButton);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        roka = (ImageButton) findViewById(R.id.rokaCheck);
        francia = (ImageButton) findViewById(R.id.franciaCheck);
        svab = (ImageButton) findViewById(R.id.svabCheck);
        kecskeCheck = (ImageButton) findViewById(R.id.kecskeCheck);
        allTheClimbs = (Button) findViewById(R.id.allClimbs);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            removeClimb = getDrawable(R.drawable.buttonselectorclimb);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            removeTextClimbs = getDrawable(R.drawable.button_shape_textclimb);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            removeClimbRight = getDrawable(R.drawable.buttonselectorclimbright);
        }

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(CheckOutActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");

        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                String uid = profile.getUid();
                String email = profile.getEmail();
            }

            DatabaseReference myKeepSyncClimberOne = FirebaseDatabase.getInstance().getReference(user.getUid() + "/" + MainActivity.climberName1);
            myKeepSyncClimberOne.keepSynced(true);

            DatabaseReference myKeepSyncClimberTwo = FirebaseDatabase.getInstance().getReference(user.getUid() + "/" + MainActivity.climberName2);
            myKeepSyncClimberTwo.keepSynced(true);

        } else {
            startActivity(new Intent(CheckOutActivity.this, LoginActivity.class));
            finish();
        }

        //Just call the working method 4 times, without adding views, counting the points. That will work.

        pointsView.setText("Your Team's Points: " + teamPointsCheckOut);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CheckOutActivity.this, MainActivity.class));
                finish();
            }
        });

        //String[] names = new String[]{MainActivity.climberName1, MainActivity.climberName2};
        //ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_checkout, names);
        //adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        //nameSpinner.setAdapter(adapter);

        allTheClimbs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedID = toggle.getCheckedRadioButtonId();
                RadioButton radioButton = (RadioButton) findViewById(selectedID);
                selectedName = (String) radioButton.getText();

                CheckOutActivity.linearLayout.removeAllViews();
                places = new String[]{"roka", "kecske", "francia", "svab"};
                populateClimbedRoutesList(selectedName, "roka");
                populateClimbedRoutesList(selectedName, "francia");
                populateClimbedRoutesList(selectedName, "kecske");
                populateClimbedRoutesList(selectedName, "svab");
            }
        });

        roka.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedID = toggle.getCheckedRadioButtonId();
                RadioButton radioButton = (RadioButton) findViewById(selectedID);
                selectedName = (String) radioButton.getText();

                CheckOutActivity.linearLayout.removeAllViews();
                populateClimbedRoutesList(selectedName, "roka");
            }
        });

        francia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedID = toggle.getCheckedRadioButtonId();
                RadioButton radioButton = (RadioButton) findViewById(selectedID);
                selectedName = (String) radioButton.getText();

                CheckOutActivity.linearLayout.removeAllViews();
                populateClimbedRoutesList(selectedName, "francia");
            }
        });

        svab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedID = toggle.getCheckedRadioButtonId();
                RadioButton radioButton = (RadioButton) findViewById(selectedID);
                selectedName = (String) radioButton.getText();

                CheckOutActivity.linearLayout.removeAllViews();
                populateClimbedRoutesList(selectedName, "svab");
            }
        });

        kecskeCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedID = toggle.getCheckedRadioButtonId();
                RadioButton radioButton = (RadioButton) findViewById(selectedID);
                selectedName = (String) radioButton.getText();

                CheckOutActivity.linearLayout.removeAllViews();
                populateClimbedRoutesList(selectedName, "kecske");
            }
        });
    }

    private void populateClimbedRoutesList(String name, String place){
        buttonID = 0;

        //ScrollView routesClimbed = new ScrollView(CheckOutActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        params.height = 600;
        //routesClimbed.setLayoutParams(params);

        LinearLayout scrollLinear = new LinearLayout(CheckOutActivity.this);
        scrollLinear.setLayoutParams(params);
        scrollLinear.setOrientation(LinearLayout.VERTICAL);

        //routesClimbed.addView(scrollLinear);
        //CheckOutActivity.linearLayout.addView(routesClimbed);


            DatabaseReference myRef = database.getReference(user.getUid() + "/" + name + "/" + place);
            routes.clear();
            String placeName = place;

            Query routesQuery = myRef;
            routesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //List routes declaration was here
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Routes routesDetails = postSnapshot.getValue(Routes.class);
                        routes.add(routesDetails);
                    }
                    addView(routes, placeName, name);
                    routes.clear();
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });
    }

    public void addView(List<Routes> route, String place, String name) {
        View separatorView = new View(CheckOutActivity.this);
        LinearLayout.LayoutParams separator = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5
        );
        separator.setMargins(10,10,10,10);
        separatorView.setLayoutParams(separator);
        separatorView.setBackgroundResource(R.drawable.separator_checkoutlist);

        TextView placeNoClimb = new TextView(CheckOutActivity.this);
        placeNoClimb.setGravity(Gravity.CENTER);
        placeNoClimb.setTextSize(20);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            placeNoClimb.setTextColor(getColor(R.color.accent));
        }
        placeNoClimb.setText(name + " did not climb in " + place + " yet.");

        if (route.size() < 1){
            linearLayout.addView(placeNoClimb);
            linearLayout.addView(separatorView);
        } else {

            for (int i = 0; i < route.size(); i++) {
                ImageButton removeMeButton = new ImageButton(CheckOutActivity.this, null, R.style.addViewButton);
                ImageView placeImage = new ImageView(CheckOutActivity.this);

                LinearLayout justForText = new LinearLayout(CheckOutActivity.this);
                LinearLayout buttonsDetails = new LinearLayout(CheckOutActivity.this);
                LinearLayout textLinearLayout = new LinearLayout(CheckOutActivity.this);
                TextView routeDetails = new TextView(CheckOutActivity.this);
                TextView routePointsTextView = new TextView(CheckOutActivity.this);
                TextView styleView = new TextView(CheckOutActivity.this);

                int counter = i;
                String routeName = route.get(counter).name;
                double routePoints = route.get(counter).points;

                LinearLayout.LayoutParams buttons = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                buttons.setMargins(10,5,10,5);
                buttonsDetails.setLayoutParams(buttons);
                buttonsDetails.setGravity(Gravity.LEFT);
                buttonsDetails.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout.LayoutParams textLinear = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                textLinearLayout.setLayoutParams(textLinear);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    textLinearLayout.setBackgroundColor(getColor(R.color.primary));
                }
                textLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                textLinearLayout.setPadding(10,10,10,10);

                LinearLayout.LayoutParams removeButton = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );

                LinearLayout.LayoutParams textLinearParams = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        4f
                );

                justForText.setLayoutParams(textLinearParams);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    textLinearLayout.setBackgroundColor(getColor(R.color.primary));
                }
                justForText.setOrientation(LinearLayout.VERTICAL);

                RelativeLayout imageRelative = new RelativeLayout(CheckOutActivity.this);
                LinearLayout.LayoutParams imageRelativeParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                imageRelative.setLayoutParams(imageRelativeParams);
                imageRelative.setGravity(Gravity.CENTER);

                placeImage.setLayoutParams(imageRelativeParams);
                placeImage.setPadding(1,1,1,1);
                if (place == "roka"){ placeImage.setImageResource(R.mipmap.fox); }
                else if (place == "kecske"){ placeImage.setImageResource(R.mipmap.goat); }
                else if (place == "francia"){ placeImage.setImageResource(R.mipmap.french); }
                else if (place == "svab"){ placeImage.setImageResource(R.mipmap.german); }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    placeImage.setBackgroundColor(getColor(R.color.primary));
                }

                removeMeButton.setLayoutParams(removeButton);
                removeMeButton.setPadding(1, 1, 1, 1);
                removeMeButton.setImageResource(R.mipmap.trash);
                removeMeButton.setBackground(removeClimb);
                removeMeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View removeMeButton) {
                        //write a safe remove method: should ask if you really want to remove,
                        //then from a spinner, choose yes, then remove.
                        showAlertDialogButtonClicked(removeMeButton, routeName, name, place, routePoints, buttonsDetails);

                    }
                });

                routeDetails.setLayoutParams(textLinear);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    routeDetails.setTextColor(getColor(R.color.icons));
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    routeDetails.setBackgroundColor(getColor(R.color.primary));
                }
                routeDetails.setText("Name: " + route.get(counter).name);
                routePointsTextView.setLayoutParams(textLinear);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    routePointsTextView.setTextColor(getColor(R.color.icons));
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    routePointsTextView.setBackgroundColor(getColor(R.color.primary));
                }
                routePointsTextView.setText("Points: " + route.get(counter).points  + " Class: " + route.get(counter).difficulty);
                styleView.setLayoutParams(textLinear);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    styleView.setTextColor(getColor(R.color.icons));
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    styleView.setBackgroundColor(getColor(R.color.primary));
                }
                styleView.setText("Last climbed in: " + route.get(counter).climbStyle + " style");

                justForText.addView(routeDetails);
                justForText.addView(routePointsTextView);
                justForText.addView(styleView);

                textLinearLayout.addView(justForText);
                imageRelative.addView(placeImage);
                textLinearLayout.addView(imageRelative);

                buttonsDetails.removeAllViews();

                buttonsDetails.addView(removeMeButton);
                buttonsDetails.addView(textLinearLayout);

                linearLayout.addView(buttonsDetails);
            }
            linearLayout.addView(separatorView);
        }
    }

    private void removeClimb(String routes, String name, String place, double points) {
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
            DatabaseReference myRef3 = database.getReference(user.getUid() + "/" + name + "/" + place);
            DatabaseReference myRefPoints = database.getReference(user.getUid());

            myRef3.child(routes).removeValue();
            myRefPoints.child("teamPoints").setValue(MainActivity.teamPoints - points);

            teamPointsCheckOut = teamPointsCheckOut - points;
            pointsView.setText("Your Team's Points: " + teamPointsCheckOut);
        }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRestart(){
        super.onRestart();
    }

    public void showAlertDialogButtonClicked(View view, String routeName, String name, String place, double routePoints, LinearLayout buttonsDetails) {

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning!");
        builder.setMessage("Would you like to remove this climb with all the points?");

        // add the buttons
        builder.setPositiveButton("Yes, remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeClimbBool = true;
                removeClimb(routeName, name, place, routePoints);
                linearLayout.removeView(buttonsDetails);
            }
        });
        builder.setNegativeButton("No, abort remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeClimbBool = false;
            }
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
