package com.example.budajam.controllers;

import android.content.Context;
import android.widget.EditText;

import com.example.budajam.models.ResetPasswordModel;
import com.example.budajam.models.initModel;

public class ResetPasswordController {
    public static boolean checkIfEmailIsProvided(Context context, EditText inputEmail) {
        return ResetPasswordModel.checkIfEmailIsProvided(context, inputEmail);
    }

    public static void sendPasswordResetEmail(Context context, EditText inputEmail) {
        initModel.initAuthentication();
        ResetPasswordModel.sendPasswordResetEmail(context, inputEmail, initModel.getAuth());
    }
}
