package com.example.budajam.models;

import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.budajam.R;
import com.example.budajam.classes.TeamData;
import com.google.firebase.auth.FirebaseUser;


public class LoginModel extends AppCompatActivity {
    //ToDo: get the strings out of here. :)

    //Check if there's something in the fields
    public boolean checkFieldsEditText(Context context, EditText inputEmail, EditText inputPassword) {
        final String email = inputEmail.getText().toString();
        final String password = inputPassword.getText().toString();
        boolean isCorrect = true;

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(context, "Enter email address!", Toast.LENGTH_SHORT).show();
            isCorrect = false;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, "Enter password!", Toast.LENGTH_SHORT).show();
            isCorrect = false;
        }
        return isCorrect;
    }

    //Inform user why sign in was not successful
    public void signInError(Context context, EditText inputPassword) {
        final String password = inputPassword.getText().toString();
        if (password.length() < 6) {
            inputPassword.setError(getString(R.string.minimum_password));
        } else {
            Toast.makeText(context, "Authentication failed!", Toast.LENGTH_LONG).show();
        }
    }

    //Check if the user paid the entry fee so they can sign in
    public boolean checkIfPaid(FirebaseUser user, Context context) {
        initModel.initTeamData();
        TeamData team = initModel.getTeamData();
        if (team != null) {
            boolean isPaid = team.isPaid();
            if (user.isEmailVerified() && isPaid) {
                return true;
            } else if (!user.isEmailVerified()) {
                Toast.makeText(context, "Please verify your email!", Toast.LENGTH_LONG).show();
            } else if (!isPaid) {
                Toast.makeText(context, "Looks like you did not pay the entry fee.", Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }
}
