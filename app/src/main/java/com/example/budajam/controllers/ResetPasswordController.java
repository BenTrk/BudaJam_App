package com.example.budajam.controllers;

import android.content.Context;
import android.widget.EditText;

import com.example.budajam.models.ResetPasswordModel;
import com.google.android.gms.tasks.Task;

public class ResetPasswordController {
    public static boolean checkIfEmailIsProvided(Context context, EditText inputEmail) {
        return ResetPasswordModel.checkIfEmailIsProvided(context, inputEmail);
    }

    public static boolean sendPasswordResetEmail(Context context, Task<Void> task) {
        return ResetPasswordModel.sendPasswordResetEmail(context, task);
    }
}
