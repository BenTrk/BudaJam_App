package com.example.budajam;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
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
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

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

    double teamPointsCheckOut;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase database;

    private TextView pointsView;
    private static LinearLayout linearLayout;

    RadioGroup radioGroup;
    private RadioButton radioButton;

    List<String> places = new ArrayList<>();
    String[] popUpContents;
    PopupWindow popupWindowPlaces;
    Button buttonShowDropDown;

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
        radioGroup = (RadioGroup) findViewById(R.id.toggle);

        removeClimbBool = false;

        teamPointsCheckOut = MainActivity.teamPoints;

        pointsView = (TextView) findViewById(R.id.teamPointsView);

        backButton = (Button) findViewById(R.id.backButton);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

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

        getPlacesFromDB(places);
        allTheClimbs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                radioButton = (RadioButton) findViewById(selectedId);
                selectedName = (String) radioButton.getText();

                CheckOutActivity.linearLayout.removeAllViews();
                for (String place : places) {
                    populateClimbedRoutesList(selectedName, place);
                }

            }
        });
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
                setCustomSpinner();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "DB Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setCustomSpinner(){
        popUpContents = new String[places.size()];
        places.toArray(popUpContents);
        popupWindowPlaces = popupWindowPlaces();
        buttonShowDropDown = (Button) findViewById(R.id.buttonShowDropDown);
        LinearLayout popupLinear = findViewById(R.id.popupLinear);
        View.OnClickListener handler = v -> {
            if (v.getId() == R.id.buttonShowDropDown) {
                // show the list view as dropdown
                pointsView.setVisibility(GONE);
                Rect locationToShow = locateView(popupLinear);
                popupWindowPlaces.showAtLocation(popupLinear, Gravity.TOP, locationToShow.left,locationToShow.bottom);
                //popupWindowPlaces.showAsDropDown(v, -5, 0);
            }
        };
        buttonShowDropDown.setOnClickListener(handler);
    }
    public static Rect locateView(View v){
        int[] loc_int = new int[2];
        if (v == null) return null;
        try
        {
            v.getLocationOnScreen(loc_int);
        } catch (NullPointerException npe)
        {
            //Happens when the view doesn't exist on screen anymore.
            return null;
        }
        Rect location = new Rect();
        location.left = loc_int[0];
        location.top = loc_int[1];
        location.right = location.left + v.getWidth();
        location.bottom = location.top + v.getHeight();
        return location;
    }

    public PopupWindow popupWindowPlaces() {
        // initialize a pop up window type
        PopupWindow popupWindow = new PopupWindow(this);
        popupWindow.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.popup_background, getTheme()));
        // the drop down list is a list view
        ListView listViewPlaces;
        // set our adapter and pass our pop up window contents
        listViewPlaces = placesAdapter(popUpContents);
        // set the item click listener
        listViewPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String name = adapterView.getAdapter().getItem(i).toString();
                //routeLayout.removeAllViews();
                int selectedId = radioGroup.getCheckedRadioButtonId();
                radioButton = (RadioButton) findViewById(selectedId);
                selectedName = (String) radioButton.getText();
                CheckOutActivity.linearLayout.removeAllViews();
                populateClimbedRoutesList(selectedName, name);
                popupWindow.dismiss();
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                pointsView.setVisibility(View.VISIBLE);
            }
        });
        // some other visual settings
        popupWindow.setFocusable(true);// set the list view as pop up window content
        popupWindow.setContentView(listViewPlaces);
        popupWindow.setWidth(300);
        return popupWindow;
    }

    private ListView placesAdapter(String[] placesArray) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.popup_layout, R.id.list_content,
                placesArray);
        ListView listViewSort = new ListView(getApplicationContext());
        listViewSort.setAdapter(adapter);
        return listViewSort;
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

        LinearLayout.LayoutParams customViewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        View customNoClimbView = new LinearLayout(CheckOutActivity.this);
        customNoClimbView.setLayoutParams(customViewParams);
        customNoClimbView = LayoutInflater.from(this).inflate(
                R.layout.custom_remove_layout, linearLayout, false
        );
        TextView placeNoClimb = customNoClimbView.findViewById(R.id.climbedRoutePointsAndClass);
        TextView noClimbPlaceNameTextView = customNoClimbView.findViewById(R.id.placeNameView);
        noClimbPlaceNameTextView.setText(placeName);
        ImageButton noClimbRemoveButtonImageButton = customNoClimbView.findViewById(R.id.removeButtonImageButton);
        placeNoClimb.setText(climberName + " did not climb here yet.");
        noClimbRemoveButtonImageButton.setImageResource(R.mipmap.no_image);
        noClimbRemoveButtonImageButton.setEnabled(false);

        if (mRouteItemsToAdd.size() < 1){
            linearLayout.addView(customNoClimbView);
            linearLayout.addView(separatorView);
        }

        else {
            for (int i = 0; i < mRouteItemsToAdd.size(); i++) {
                LinearLayout buttonsDetails = new LinearLayout(CheckOutActivity.this);
                View customRemoveView = new View(CheckOutActivity.this);

                customRemoveView.setLayoutParams(customViewParams);
                buttonsDetails.setLayoutParams(customViewParams);

                customRemoveView = LayoutInflater.from(this).inflate(
                        R.layout.custom_remove_layout, linearLayout, false
                );

                TextView placeNameTextView = customRemoveView.findViewById(R.id.placeNameView);
                placeNameTextView.setText(placeName);
                TextView climbedRouteName = customRemoveView.findViewById(R.id.climbedRouteName);
                TextView climbedRoutePointsAndClass = customRemoveView.findViewById(R.id.climbedRoutePointsAndClass);
                TextView climbedRouteStyle = customRemoveView.findViewById(R.id.climbedRouteStyle);
                ImageButton removeButtonImageButton = customRemoveView.findViewById(R.id.removeButtonImageButton);

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
