package com.example.budajam.views;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.budajam.R;
import com.example.budajam.controllers.ResetPasswordController;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText inputEmail;
    private Button btnReset, btnBack;
    private FirebaseAuth auth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the layout
        setContentView(R.layout.activity_reset_password);

        //Instantiate screen details
        inputEmail = (EditText) findViewById(R.id.email);
        btnReset = (Button) findViewById(R.id.btn_reset_password);
        btnBack = (Button) findViewById(R.id.btn_back);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Set onclicklisteners
        btnBack.setOnClickListener(v -> finish());
        btnReset.setOnClickListener(v -> {
            auth = FirebaseAuth.getInstance();
            //Check if email is provided or not
            //ToDo: check what happens for an email not registered or corrupt
            if (ResetPasswordController.checkIfEmailIsProvided(ResetPasswordActivity.this, inputEmail)) {
                progressBar.setVisibility(View.VISIBLE);
                //Send password reset emails
                auth.sendPasswordResetEmail(inputEmail.getText().toString()).addOnCompleteListener(task -> {
                            if (ResetPasswordController.sendPasswordResetEmail(ResetPasswordActivity.this ,task)){
                                progressBar.setVisibility(View.GONE);
                            }
                });
            }
        });
    }

}
