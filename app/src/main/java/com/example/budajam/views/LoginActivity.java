package com.example.budajam.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.budajam.MainActivity;
import com.example.budajam.R;
import com.example.budajam.ResetPasswordActivity;
import com.example.budajam.controllers.LoginController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    public EditText inputEmail;
    public EditText inputPassword;
    private FirebaseAuth auth;
    private ProgressBar progressBar;

    LoginController loginController = new LoginController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Authenticate if already signed in
        auth = loginController.authenticate(LoginActivity.this);

        //Set the view now
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        Button btnSignup = findViewById(R.id.btn_signup);
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnReset = findViewById(R.id.btn_reset_password);

        //Setting up Buttons with functions
        //  Start signupactivity when signup is selected
        btnSignup.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
        //  Start resetactivity when reset is selected
        btnReset.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class)));
        //  Start login process when login is selected -> authenticate, check if entry fee was paid, login, start mainactivity
        btnLogin.setOnClickListener(v -> {
            //Check if email and password fields are empty
            if (loginController.checkFields(LoginActivity.this, inputEmail, inputPassword)) {
                progressBar.setVisibility(View.VISIBLE);
                //authenticate user
                auth.signInWithEmailAndPassword(inputEmail.getText().toString(), inputPassword.getText().toString())
                        .addOnCompleteListener(LoginActivity.this, task -> {
                            progressBar.setVisibility(View.GONE);
                            if (!task.isSuccessful()) {
                                //There was an error
                                loginController.signInError(LoginActivity.this, inputPassword);
                            } else {
                                //No error, signing in, if team paid the entry fee
                                checkIfPaid(Objects.requireNonNull(auth.getCurrentUser()));
                            }
                        });
                }
            });

    }
    //Check if team paid the entry fee
    private void checkIfPaid(FirebaseUser user){
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
        DatabaseReference ref = database.getReference(user.getUid()+"/");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (loginController.checkIfPaid(dataSnapshot, user, LoginActivity.this)){
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Something went wrong. Try again later!", Toast.LENGTH_LONG).show();
            }
        });
    }
}
