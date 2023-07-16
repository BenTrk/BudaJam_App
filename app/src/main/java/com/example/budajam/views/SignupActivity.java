package com.example.budajam.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.budajam.R;
import com.example.budajam.controllers.SignupController;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, teamName, climber1Name, climber2Name;
    private Button btnSignUp;
    private Button verify_email_button;
    private Spinner category;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseUser user;
    SignupController signupController = new SignupController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the view
        setContentView(R.layout.activity_signup);

        //Instantiate the visuals
        Button btnSignIn = (Button) findViewById(R.id.sign_in_button);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        verify_email_button = (Button) findViewById(R.id.verify_email_button);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        teamName = (EditText) findViewById(R.id.teamname);
        category = (Spinner) findViewById(R.id.category);
        climber1Name = (EditText) findViewById(R.id.climber1);
        climber2Name = (EditText) findViewById(R.id.climber2);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        Button btnResetPassword = (Button) findViewById(R.id.btn_reset_password);

        //Set adapter for the spinner
        signupController.setAdapter(category, SignupActivity.this);

        //Set onclicklisteners
        btnResetPassword.setOnClickListener(v -> startActivity(new Intent(SignupActivity.this, ResetPasswordActivity.class)));
        btnSignIn.setOnClickListener(v -> finish());
        btnSignUp.setOnClickListener(v -> {

            if (!SignupController.checkRegisterData(SignupActivity.this, inputEmail, inputPassword, teamName, category,
                    climber1Name, climber2Name)) { return; }

            progressBar.setVisibility(View.VISIBLE);

            //create user
            createUser(inputEmail.getText().toString(), inputPassword.getText().toString());

        });
    }
    //Validate the user
    private void validateUser(){

        user.sendEmailVerification()
                .addOnCompleteListener(SignupActivity.this, (OnCompleteListener<Void>) task -> {

                    //Check if validation could be started & done
                    if (!SignupController.validateUser(SignupActivity.this, task, user,
                            verify_email_button, btnSignUp)){ finish(); }

                    //Set onclicklistener to verify_email_button
                    verify_email_button.setOnClickListener(v -> {
                        if (SignupController.checkIfEmailVerified(SignupActivity.this, auth)){
                            Toast.makeText(SignupActivity.this, "You are enrolled!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                            finish();
                        }
                    });
                });
    }

    //Create the user if validated (validation check validateUser() is called inside)
    private void createUser(String email, String password){
        auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignupActivity.this, task -> {
                    Toast.makeText(SignupActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    SignupController.createUser(SignupActivity.this, task, teamName, category, climber1Name, climber2Name);
                    user = auth.getCurrentUser();
                    validateUser();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

}