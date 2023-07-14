package com.example.budajam.views;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.example.budajam.ExtraPointsActivity;
import com.example.budajam.OptionsActivity;
import com.example.budajam.R;
import com.example.budajam.classes.Routes;
import com.example.budajam.controllers.MainController;

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
    Button buttonShowDropDown;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the view
        setContentView(R.layout.activity_main);

        //Authenticate
        MainController.authentication();
        //Initialize routes in MainModel. WARNING: timeing issue, if it takes longer to grab the data,
        // it could happen that the screen shows empty.
        //ToDo: Make it with a listener interface
        MainController.init();

        //Set User details and Route details synced.
        if (MainController.setUserAndRouteSynced()) {
            MainController.getNamesFromDatabase();
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        //Setting up the view items
        emptyScreenLinear = findViewById(R.id.emptyScreenLinear);
        routeLayout = findViewById(R.id.routeLayout);
        LinearLayout linearLayout = findViewById(R.id.linearLayout);
        menuButton = findViewById(R.id.menuButton);
        buttonShowDropDown = findViewById(R.id.buttonShowDropDown);

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
                    MainController.signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
                return true;
            });
            popup.show();
        });

        //Set the onclicklistener on dropdown button to
        // show the list of places when the dropdown button is selected.
        buttonShowDropDown.setOnClickListener(view -> {
            routeLayout.removeAllViews();
            if (MainController.getPlacesFromDatabase().isEmpty()){
                Toast.makeText(MainActivity.this, "Could not grab the routes yet. Try again the button in 5 seconds!", Toast.LENGTH_SHORT).show();
            }
            for (String place : MainController.getPlacesFromDatabase()) {
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
        });
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
        customButton.setOnClickListener(v -> {
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
        });

        climbedItButton.setOnClickListener(v -> {

            RadioButton checkedNameRadioButton = findViewById(climberNameRadioGroup.getCheckedRadioButtonId());
            RadioButton checkedStyleRadioButton = findViewById(climbingStyleRadioGroup.getCheckedRadioButtonId());

            String checkedName = (String) checkedNameRadioButton.getText();
            String checkedStyle = (String) checkedStyleRadioButton.getText();
            MainController.addClimbToDatabase(checkedName, checkedStyle, placeName, mRouteItemToAdd, MainActivity.this);

            routeLayout.removeAllViews();
            emptyScreenLinear.setVisibility(View.VISIBLE);
            routeLayout.addView(emptyScreenLinear);
        });

        routeLayout.addView(customRoutesView);
    }

    private void authenticate(){
        if (MainController.authentication() == null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
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


