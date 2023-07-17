package com.example.budajam.controllers;

import android.content.Context;
import android.widget.EditText;

import com.example.budajam.models.LoginModel;
import com.google.firebase.auth.FirebaseUser;

public class LoginController {
    LoginModel loginModel = new LoginModel();

    public boolean checkFields(Context context, EditText inputEmail, EditText inputPassword) {
        return loginModel.checkFieldsEditText(context, inputEmail, inputPassword);
    }

    public void signInError(Context context, EditText inputPassword) {
        loginModel.signInError(context, inputPassword);
    }

    public boolean checkIfPaid(FirebaseUser user, Context context) {
        return loginModel.checkIfPaid(user, context);
    }
}
