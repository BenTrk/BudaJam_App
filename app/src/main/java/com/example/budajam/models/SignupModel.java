package com.example.budajam.models;

import static java.lang.Boolean.FALSE;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.budajam.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignupModel {

    public static boolean checkRegisterData(Context context, EditText inputEmail, EditText inputPassword, EditText teamName, Spinner category, EditText climber1Name, EditText climber2Name) {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String teamNameString = teamName.getText().toString();
        String categoryString = category.getSelectedItem().toString();
        String climber1NameString = climber1Name.getText().toString();
        String climber2NameString = climber2Name.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(context, "Enter email address!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, "Enter password!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(context, "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(teamNameString)) {
            Toast.makeText(context, "Enter team name!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(categoryString)) {
            Toast.makeText(context, "Select a category!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(climber1NameString)) {
            Toast.makeText(context, "Enter climber #1 name!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(climber2NameString)) {
            Toast.makeText(context, "Enter climber #2 name!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static boolean validateUser(Context context, Task<Void> task, FirebaseUser user,
                                    Button verify_email_button, Button btnSignUp) {
        if (task.isSuccessful()) {
            Toast.makeText(context,
                    "Verification email sent to " + user.getEmail(),
                    Toast.LENGTH_SHORT).show();

            verify_email_button.setEnabled(true);
            btnSignUp.setVisibility(View.GONE);
            verify_email_button.setVisibility(View.VISIBLE);
            return true;
        } else {
            Log.e("Verification", "sendEmailVerification", task.getException());
            Toast.makeText(context,
                    "Failed to send verification email.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static boolean checkIfEmailVerified(Context context, FirebaseAuth auth) {
        Objects.requireNonNull(auth.getCurrentUser()).reload();
        FirebaseUser user = auth.getCurrentUser();
        if (user.isEmailVerified()) {
            return true;
        } else {
            Toast.makeText(context, "Please verify your email!", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public static void createUser(Task<AuthResult> task, EditText teamName,
                                  Spinner category, EditText climber1Name, EditText climber2Name) {
        if (!task.isSuccessful()) {
            //ToDo: team already exists message
            Log.d("AuthFailed:", "Authentication failed." + task.getException());
        }
        else {
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://budajam-ea659-default-rtdb.firebaseio.com/");
            DatabaseReference myRef = database.getReference();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            assert user != null;
            String userID = user.getUid();

            myRef.child(userID).setValue(user.getUid());
            myRef.child(userID).child("TeamName").setValue(teamName.getText().toString());
            myRef.child(userID).child("Category").setValue(category.getSelectedItem().toString());
            myRef.child(userID).child("ClimberOne").setValue(climber1Name.getText().toString());
            myRef.child(userID).child("ClimberTwo").setValue(climber2Name.getText().toString());
            myRef.child(userID).child("teamPoints").setValue(0.0);
            myRef.child(userID).child("Paid").setValue(FALSE);
        }
    }

    public void setAdapter(Spinner category, Context context) {
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(context, R.array.category, R.layout.spinner_item_primarycolor);
        adapter.setDropDownViewResource(R.layout.activity_spinner_dropdown_item);
        category.setAdapter(adapter);
    }
}
