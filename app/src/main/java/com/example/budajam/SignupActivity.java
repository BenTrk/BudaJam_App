package com.example.budajam;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, teamName, climber1Name, climber2Name;
    private Button btnSignIn, btnSignUp, btnResetPassword, verify_email_button;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignIn = (Button) findViewById(R.id.sign_in_button);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        verify_email_button = (Button) findViewById(R.id.verify_email_button);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        teamName = (EditText) findViewById(R.id.teamname);
        climber1Name = (EditText) findViewById(R.id.climber1);
        climber2Name = (EditText) findViewById(R.id.climber2);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnResetPassword = (Button) findViewById(R.id.btn_reset_password);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, ResetPasswordActivity.class));
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String teamNameString = teamName.getText().toString();
                String climber1NameString = climber1Name.getText().toString();
                String climber2NameString = climber2Name.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(teamNameString)) {
                    Toast.makeText(getApplicationContext(), "Enter team name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(climber1NameString)) {
                    Toast.makeText(getApplicationContext(), "Enter climber #1 name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(climber2NameString)) {
                    Toast.makeText(getApplicationContext(), "Enter climber #2 name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //create user
                createUser(email, password, teamNameString, climber1NameString, climber2NameString);

            }
        });
    }
    private void validateUser(){
        btnSignUp.setVisibility(View.GONE);
        verify_email_button.setVisibility(View.VISIBLE);

        user.sendEmailVerification()
                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();

                            findViewById(R.id.verify_email_button).setEnabled(true);

                        } else {
                            Log.e("Verification", "sendEmailVerification", task.getException());
                            Toast.makeText(SignupActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        verify_email_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                auth.getCurrentUser().reload();
                                user = auth.getCurrentUser();
                                if (user.isEmailVerified()) {
                                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(SignupActivity.this, "Please verify your email!", Toast.LENGTH_LONG).show();
                                    auth.getCurrentUser().reload();
                                }
                            }
                        });
                    }
                });
    }

    private void createUser(String email, String password, String teamNameString, String climber1NameString, String climber2NameString){
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Toast.makeText(SignupActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            user = auth.getCurrentUser();

                            validateUser();

                            if (!task.isSuccessful()) {
                                Toast.makeText(SignupActivity.this, "Authentication failed." + task.getException(),
                                        Toast.LENGTH_SHORT).show();
                            } else {

                                FirebaseDatabase database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
                                DatabaseReference myRef = database.getReference();
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                String userID = user.getUid();

                                myRef.child(userID).setValue(user.getUid());
                                myRef.child(userID).child("TeamName").setValue(teamNameString);
                                myRef.child(userID).child("ClimberOne").setValue(climber1NameString);
                                myRef.child(userID).child("ClimberTwo").setValue(climber2NameString);
                                myRef.child(userID).child("teamPoints").setValue(0.0);

                                //startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                //finish();
                            }
                        }
                    });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

}