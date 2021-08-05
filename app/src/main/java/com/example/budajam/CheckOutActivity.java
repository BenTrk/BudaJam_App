package com.example.budajam;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
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

import static android.view.View.GONE;

public class CheckOutActivity extends AppCompatActivity {

    Button backButton, allTheClimbs;

    int buttonID = 0;

    boolean removeClimbBool;

    RadioGroup toggle;
    RadioButton climber1OnSwitch, climber2OnSwitch;

    List<Routes> routes = new ArrayList<>();
    String[] places;

    double teamPointsCheckOut;

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

        backButton = (Button) findViewById(R.id.backButton);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        roka = (ImageButton) findViewById(R.id.rokaCheck);
        francia = (ImageButton) findViewById(R.id.franciaCheck);
        svab = (ImageButton) findViewById(R.id.svabCheck);
        kecskeCheck = (ImageButton) findViewById(R.id.kecskeCheck);
        allTheClimbs = (Button) findViewById(R.id.allClimbs);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

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

        pointsView.setText("Your Team's Points: " + teamPointsCheckOut);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CheckOutActivity.this, MainActivity.class));
                finish();
            }
        });

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
                    addCustomRemove(routes, placeName, name);
                    routes.clear();
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });
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

    @SuppressLint("SetTextI18n")
    private void addCustomRemove(List<Routes> mRouteItemsToAdd, String placeName, String climberName) {

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
        placeNoClimb.setText(climberName + " did not climb in " + placeName + " yet.");

        if (mRouteItemsToAdd.size() < 1){
            linearLayout.addView(placeNoClimb);
            linearLayout.addView(separatorView);
        }

        else {
            for (int i = 0; i < mRouteItemsToAdd.size(); i++) {
                LinearLayout buttonsDetails = new LinearLayout(CheckOutActivity.this);
                View customRemoveView = new View(CheckOutActivity.this);

                LinearLayout.LayoutParams customViewParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                customRemoveView.setLayoutParams(customViewParams);
                buttonsDetails.setLayoutParams(customViewParams);

                //if my custom button is true

                customRemoveView = LayoutInflater.from(this).inflate(
                        R.layout.custom_remove_layout, linearLayout, false
                );

                ImageView placeNameImage = customRemoveView.findViewById(R.id.placeNameImage);
                TextView climbedRouteName = customRemoveView.findViewById(R.id.climbedRouteName);
                TextView climbedRoutePointsAndClass = customRemoveView.findViewById(R.id.climbedRoutePointsAndClass);
                TextView climbedRouteStyle = customRemoveView.findViewById(R.id.climbedRouteStyle);
                ImageButton removeButtonImageButton = customRemoveView.findViewById(R.id.removeButtonImageButton);

                if (placeName.equals("francia")){ placeNameImage.setImageResource(R.mipmap.french); }
                else if (placeName.equals("svab")){ placeNameImage.setImageResource(R.mipmap.german); }
                else if (placeName.equals("kecske")){ placeNameImage.setImageResource(R.mipmap.goat); }
                else if (placeName.equals("roka")){ placeNameImage.setImageResource(R.mipmap.fox); }
                String routeName = mRouteItemsToAdd.get(i).name;
                Double routePoints = mRouteItemsToAdd.get(i).points;

                climbedRouteName.setText(routeName);
                climbedRoutePointsAndClass.setText("Points: " + routePoints + " Class: " + mRouteItemsToAdd.get(i).difficulty);
                climbedRouteStyle.setText("Last climbed in: " + mRouteItemsToAdd.get(i).climbStyle + " style");

                removeButtonImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showAlertDialogButtonClicked(removeButtonImageButton, routeName, climberName, placeName, routePoints, buttonsDetails);
                    }
                });
                buttonsDetails.addView(customRemoveView);
                linearLayout.addView(buttonsDetails);
            }
            linearLayout.addView(separatorView);
        }
    }

}
