package com.example.budajam.views;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.budajam.R;
import com.example.budajam.classes.Route;
import com.example.budajam.controllers.CheckOutController;
import com.example.budajam.controllers.MainController;

import java.util.List;

public class CheckOutActivity extends AppCompatActivity {

    Button backButton, allTheClimbs;
    boolean removeClimbBool;
    RadioButton climber1OnSwitch, climber2OnSwitch;
    private TextView pointsView;
    LinearLayout linearLayout;
    RadioGroup radioGroup;
    Button buttonShowDropDown;
    TextView selectedClimberView;

    String selectedName;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        MainController.init();
        //Set the view
        setContentView(R.layout.activity_checkout);

        //Instantiate the items on the screen
        climber1OnSwitch = findViewById(R.id.climber1onswitch);
        climber2OnSwitch = findViewById(R.id.climber2onswitch);
        radioGroup = findViewById(R.id.toggle);
        pointsView = findViewById(R.id.teamPointsView);
        selectedClimberView = findViewById(R.id.selectedClimber);
        backButton = findViewById(R.id.backButton);
        linearLayout = findViewById(R.id.linearLayoutDropDown);
        allTheClimbs = findViewById(R.id.allClimbs);
        buttonShowDropDown = findViewById(R.id.buttonShowDropDown);

        removeClimbBool = false;

        //If not authenticated, kick out :)
        if (CheckOutController.authenticate() == null) {
            startActivity(new Intent(CheckOutActivity.this, LoginActivity.class));
            finish();
        }

        //Set the radiobutton so it shows which climber is selected
        climber1OnSwitch.setText(MainController.getNames()[0]);
        climber2OnSwitch.setText(MainController.getNames()[1]);
        pointsView.setText("Your team's points: " + CheckOutController.getTeamPoints());
        selectedClimberView.setText("\nClimber: " + getSelectedName());

        //Set up backbutton to go back to the main screen
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(CheckOutActivity.this, MainActivity.class));
            finish();
        });

        allTheClimbs.setOnClickListener(v -> {
            selectedClimberView.setText("\nClimber: " + getSelectedName());
            if (CheckOutController.areTherePlaces(getSelectedName())) {
                linearLayout.removeAllViews();
                for (String place : CheckOutController.getClimbPlaces(getSelectedName())) {
                    populateClimbedRoutesList(selectedName, place);
                }
            }
            else {
                linearLayout.removeAllViews();
                Toast.makeText(CheckOutActivity.this, getSelectedName() + " has no climbs in the database yet...", Toast.LENGTH_LONG).show();
            }
        });

        buttonShowDropDown.setOnClickListener(view -> {
            selectedClimberView.setText("\nClimber: " + getSelectedName());
            if (CheckOutController.areTherePlaces(getSelectedName())) {
                addCustomDropDown(CheckOutController.getClimbPlaces(getSelectedName()));
            } else {
                linearLayout.removeAllViews();
                Toast.makeText(CheckOutActivity.this, getSelectedName() + " has no climbs in the database yet...", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void addCustomDropDown(String[] places) {
        linearLayout.removeAllViews();
        //Go through the places and create the layout
        for (String place : places) {
            final View customRoutesView = LayoutInflater.from(this).inflate(
                    R.layout.custom_dropdown_layout, linearLayout, false
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
                //Populate the list of climbed routes.
                linearLayout.removeAllViews();
                populateClimbedRoutesList(selectedName, place);
            });
            linearLayout.addView(customRoutesView);
        }
    }

    private void populateClimbedRoutesList(String name, String place){
        //Create the view for the routes climbed
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        params.height = 600;

        LinearLayout scrollLinear = new LinearLayout(CheckOutActivity.this);
        scrollLinear.setLayoutParams(params);
        scrollLinear.setOrientation(LinearLayout.VERTICAL);

        //Get the routes climbed from the database
        addCustomRemove((CheckOutController.getClimbedRoutesPerClimber(selectedName, place)), place, name);
    }

    public void showAlertDialogButtonClicked(Route route, String climberName, String place) {

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning!");
        builder.setMessage("Would you like to remove this climb with all the points?");

        // add the buttons
        builder.setPositiveButton("Yes, remove", (dialog, which) -> {
            double points = CheckOutController.removeClimb(route, climberName, place);
            linearLayout.removeAllViews();
            pointsView.setText("Your team's points: " + points);
        });
        builder.setNegativeButton("No, abort remove", (dialog, which) -> dialog.dismiss());
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void addCustomRemove(List<Route> mRouteItemsToAdd, String placeName, String climberName) {
        //Create the view to see route details and if necessary, remove.
        //Create params for the views
        LinearLayout.LayoutParams customViewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        //Create a view for each route
        for (Route route : mRouteItemsToAdd) {
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
            TextView climbedBestStyle = customRemoveView.findViewById(R.id.mostPointsStyle);
            ImageButton removeButtonImageButton = customRemoveView.findViewById(R.id.removeButtonImageButton);

            climbedRouteName.setText(route.name);
            climbedRoutePointsAndClass.setText("Points: " + route.points + " Class: " + route.difficulty);
            climbedRouteStyle.setText("Last climbed in: " + route.climbStyle + " style");
            climbedBestStyle.setText("Most points earned: " + route.best);
            //would be great to see the style the points are now given.

            //Set up the remove button to show the dialog and handle remove
            removeButtonImageButton.setOnClickListener(v -> {
                showAlertDialogButtonClicked(route, climberName, placeName);
            });
            buttonsDetails.addView(customRemoveView);
            linearLayout.addView(buttonsDetails);
        }
    }
    private void authenticate(){
        if (MainController.authentication() == null){
            startActivity(new Intent(CheckOutActivity.this, LoginActivity.class));
            finish();
        }
    }

    private String getSelectedName(){
        int selectedId = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectedId);
        return selectedName = (String) radioButton.getText();
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
