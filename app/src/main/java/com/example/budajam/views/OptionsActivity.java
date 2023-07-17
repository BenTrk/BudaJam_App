package com.example.budajam.views;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.budajam.R;
import com.example.budajam.controllers.MainController;
import com.example.budajam.controllers.OptionsController;
import com.example.budajam.models.initModel;
import com.google.firebase.auth.FirebaseUser;

public class OptionsActivity extends AppCompatActivity {
    //this is an options screen, rename it if you have time

    private Button changeEmail;
    private Button changePassword;
    private Button sendEmail;
    private Button remove;

    private EditText oldEmail, newEmail, password, newPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set view
        setContentView(R.layout.activity_options);

        //Authenticate the user
        if (OptionsController.authenticate() == null){
            startActivity(new Intent(OptionsActivity.this, LoginActivity.class));
            finish();
        }

        //Initialize parts of the screen
        Button btnChangeEmail = findViewById(R.id.change_email_button);
        Button btnChangePassword = findViewById(R.id.change_password_button);
        Button btnSendResetEmail = findViewById(R.id.sending_pass_reset_button);
        Button btnRemoveUser = findViewById(R.id.remove_user_button);
        changeEmail = findViewById(R.id.changeEmail);
        changePassword = findViewById(R.id.changePass);
        sendEmail = findViewById(R.id.send);
        remove = findViewById(R.id.remove);
        Button signOut = findViewById(R.id.sign_out);
        Button backButton = findViewById(R.id.backButton);

        oldEmail = findViewById(R.id.old_email);
        newEmail = findViewById(R.id.new_email);
        password = findViewById(R.id.password);
        newPassword = findViewById(R.id.newPassword);

        oldEmail.setVisibility(View.GONE);
        newEmail.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        newPassword.setVisibility(View.GONE);
        changeEmail.setVisibility(View.GONE);
        changePassword.setVisibility(View.GONE);
        sendEmail.setVisibility(View.GONE);
        remove.setVisibility(View.GONE);

        progressBar = findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        //added button to get back to MainActivity
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(OptionsActivity.this, MainActivity.class));
            finish();
        });

        btnChangeEmail.setOnClickListener(v -> {
            oldEmail.setVisibility(View.GONE);
            newEmail.setVisibility(View.VISIBLE);
            password.setVisibility(View.GONE);
            newPassword.setVisibility(View.GONE);
            changeEmail.setVisibility(View.VISIBLE);
            changePassword.setVisibility(View.GONE);
            sendEmail.setVisibility(View.GONE);
            remove.setVisibility(View.GONE);
        });

        changeEmail.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            if (!newEmail.getText().toString().trim().equals("")) {
                initModel.getUser().updateEmail(newEmail.getText().toString().trim())
                        .addOnCompleteListener(task -> {
                            if (OptionsController.changeEmail(OptionsActivity.this, task)) {
                                signOut();
                            }
                            progressBar.setVisibility(View.GONE);
                        });
            } else if (newEmail.getText().toString().trim().equals("")) {
                newEmail.setError("Enter email");
                progressBar.setVisibility(View.GONE);
            }
        });

        btnChangePassword.setOnClickListener(v -> {
            oldEmail.setVisibility(View.GONE);
            newEmail.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            newPassword.setVisibility(View.VISIBLE);
            changeEmail.setVisibility(View.GONE);
            changePassword.setVisibility(View.VISIBLE);
            sendEmail.setVisibility(View.GONE);
            remove.setVisibility(View.GONE);
        });

        changePassword.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            if (!newPassword.getText().toString().trim().equals("")) {
                if (newPassword.getText().toString().trim().length() < 6) {
                    newPassword.setError("Password too short, enter minimum 6 characters");
                    progressBar.setVisibility(View.GONE);
                } else {
                    initModel.getUser().updatePassword(newPassword.getText().toString().trim())
                            .addOnCompleteListener(task -> {
                                if (OptionsController.changePassword(OptionsActivity.this, task)) {
                                    signOut();
                                }
                                progressBar.setVisibility(View.GONE);
                            });
                }
            } else if (newPassword.getText().toString().trim().equals("")) {
                newPassword.setError("Enter password");
                progressBar.setVisibility(View.GONE);
            }
        });

        btnSendResetEmail.setOnClickListener(v -> {
            oldEmail.setVisibility(View.VISIBLE);
            newEmail.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            newPassword.setVisibility(View.GONE);
            changeEmail.setVisibility(View.GONE);
            changePassword.setVisibility(View.GONE);
            sendEmail.setVisibility(View.VISIBLE);
            remove.setVisibility(View.GONE);
        });

        sendEmail.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            if (!oldEmail.getText().toString().trim().equals("")) {
                initModel.getAuth().sendPasswordResetEmail(oldEmail.getText().toString().trim())
                        .addOnCompleteListener(task -> OptionsController.resetPassword(OptionsActivity.this, task));
            } else {
                oldEmail.setError("Enter email");
                progressBar.setVisibility(View.GONE);
            }
        });

        btnRemoveUser.setOnClickListener(v -> showAlertDialogRemoveUser(initModel.getUser()));

        signOut.setOnClickListener(v -> {
            signOut();
            startActivity(new Intent(OptionsActivity.this, LoginActivity.class));
            finish();
        });

    }

    public void showAlertDialogRemoveUser(FirebaseUser user) {

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning!");
        builder.setMessage("Would you like to permanently remove your user account?");

        // add the buttons
        builder.setPositiveButton("Yes, remove", (dialog, which) -> {

            progressBar.setVisibility(View.VISIBLE);
            if (user != null) {
                user.delete().addOnCompleteListener(task -> {
                    if (OptionsController.showAlertDialogRemoveUser(OptionsActivity.this, task)){
                        startActivity(new Intent(OptionsActivity.this, SignupActivity.class));
                        finish();
                    }
                    progressBar.setVisibility(View.GONE);
                });
            }

        });
        builder.setNegativeButton("No, abort remove", (dialog, which) -> dialog.dismiss());
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //sign out method
    public void signOut() {
        OptionsController.signOut();
    }

    private void authenticate(){
        if (MainController.authentication() == null){
            startActivity(new Intent(OptionsActivity.this, LoginActivity.class));
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