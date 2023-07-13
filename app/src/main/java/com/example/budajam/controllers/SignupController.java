package com.example.budajam.controllers;

import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.budajam.models.SignupModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupController {
    SignupModel signupModel = new SignupModel();

    public static boolean checkRegisterData(Context context, EditText inputEmail, EditText inputPassword, EditText teamName,
                                         Spinner category, EditText climber1Name, EditText climber2Name) {
        return SignupModel.checkRegisterData(context, inputEmail, inputPassword, teamName, category,
                climber1Name, climber2Name);
    }

    public static void createUser(Task<AuthResult> task, EditText teamName, Spinner category,
                                  EditText climber1Name, EditText climber2Name) {
        SignupModel.createUser(task, teamName, category, climber1Name, climber2Name);
    }

    public static boolean validateUser(Context context, Task<Void> task, FirebaseUser user,
                                       Button verify_email_button, Button btnSignUp) {
        return SignupModel.validateUser(context, task, user, verify_email_button, btnSignUp);
    }

    public static boolean checkIfEmailVerified(Context context, FirebaseAuth auth) {
        return SignupModel.checkIfEmailVerified(context, auth);
    }

    public void setAdapter(Spinner category, Context context) {
        signupModel.setAdapter(category, context);
    }
}
