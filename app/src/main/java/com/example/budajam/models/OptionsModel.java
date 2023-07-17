package com.example.budajam.models;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

public class OptionsModel {
    public static boolean changeEmail(Context context, Task<Void> task){
        if (task.isSuccessful()) {
            Toast.makeText(context, "Email address is updated. Please sign in with new email id!", Toast.LENGTH_LONG).show();
            return true;
        } else {
            Toast.makeText(context, "Failed to update email!", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public static boolean changePassword(Context context, Task<Void> task){
        if (task.isSuccessful()) {
            Toast.makeText(context, "Password is updated, sign in with new password!", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(context, "Failed to update password!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static void resetPassword(Context context, Task<Void> task){
        if (task.isSuccessful()) {
            Toast.makeText(context, "Reset password email is sent!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean showAlertDialogRemoveUser(Context context, Task<Void> task) {
        if (task.isSuccessful()) {
            Toast.makeText(context, "Your profile is deleted:( Create a account now!", Toast.LENGTH_SHORT).show();
            DatabaseReference myRef = initModel.database.getReference();
            myRef.child(initModel.getUser().getUid()).removeValue();
            return true;
        } else {
            Toast.makeText(context, "Failed to delete your account!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
