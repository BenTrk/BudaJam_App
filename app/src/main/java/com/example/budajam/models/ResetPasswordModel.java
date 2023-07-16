package com.example.budajam.models;

import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordModel {
    public static boolean checkIfEmailIsProvided(Context context, EditText inputEmail) {
        String email = inputEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(context, "Enter your registered email id", Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }

    public static void sendPasswordResetEmail(Context context, EditText inputEmail, FirebaseAuth auth) {
        //Check if email is provided or not
        //ToDo: check what happens for an email not registered or corrupt
            //Send password reset emails
        auth.sendPasswordResetEmail(inputEmail.getText().toString()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
