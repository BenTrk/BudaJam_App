package com.example.budajam.models;

import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;

public class ResetPasswordModel {
    public static boolean checkIfEmailIsProvided(Context context, EditText inputEmail) {
        String email = inputEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(context, "Enter your registered email id", Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }

    public static boolean sendPasswordResetEmail(Context context, Task<Void> task) {
        if (task.isSuccessful()) {
            Toast.makeText(context, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(context, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
