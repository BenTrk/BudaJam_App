package com.example.budajam.controllers;

import android.content.Context;

import com.example.budajam.models.OptionsModel;
import com.example.budajam.models.initModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

public class OptionsController {
    public static FirebaseUser authenticate() {
        initModel.initAuthentication();
        return initModel.getUser();
    }
    public static boolean changeEmail(Context context, Task<Void> task){
        return OptionsModel.changeEmail(context, task);
    }

    public static boolean changePassword(Context context, Task<Void> task){
        return OptionsModel.changePassword(context, task);
    }

    public static void resetPassword(Context context, Task<Void> task){
        OptionsModel.resetPassword(context, task);
    }

    public static boolean showAlertDialogRemoveUser(Context context, Task<Void> task){
        return OptionsModel.showAlertDialogRemoveUser(context, task);
    }

    public static void signOut() {
        initModel.getAuth().signOut();
    }
}
