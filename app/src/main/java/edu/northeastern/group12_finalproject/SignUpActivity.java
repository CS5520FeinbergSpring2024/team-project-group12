package edu.northeastern.group12_finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/**
 * Inspired by Android Knowledge https://www.youtube.com/watch?v=TStttJRAPhE.
 */
public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText signUpEmail;
    private EditText signUpPassword;
    private Button signupBtn;
    private TextView redirectLogin;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        signUpEmail = findViewById(R.id.signup_email);
        signUpPassword = findViewById(R.id.signup_password);
        signupBtn = findViewById(R.id.signup_button);
        redirectLogin = findViewById(R.id.loginRedirectText);

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = signUpEmail.getText().toString().trim();
                String password = signUpPassword.getText().toString().trim();

                if (user.isEmpty()) {
                    signUpEmail.setError("Email cannot be empty!");
                }
                if (password.isEmpty()) {
                    signUpPassword.setError("Password cannot be empty!");
                } else {
                    auth.createUserWithEmailAndPassword(user, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, "You have successfully sign up an acount", Toast.LENGTH_SHORT).show();
                                FirebaseUser user = auth.getCurrentUser();

                                // Get the user email and uid from auth.
                                String email = user.getEmail();
                                String uid = user.getUid();

                                // When user is registered store user info in firebase realtime.
                                // Using HashMap.
                                HashMap<Object, String> hashmap = new HashMap<>();

                                // Put info in hashmap.
                                hashmap.put("email", email);
                                hashmap.put("uid", uid);
                                hashmap.put("username", "Unknown User"); // To be added in Edit profile
                                hashmap.put("bio", "Bio to be added..."); // To be added in Edit profile

                                // Firebase data base instance.
                                FirebaseDatabase db = FirebaseDatabase.getInstance();

                                // Path to store user data named "Users"
                                DatabaseReference reference = db.getReference("Users");
                                // put data from hashmap to database.
                                reference.child(uid).setValue(hashmap);

                                // Redirect to homepage.
                                startActivity(new Intent(SignUpActivity.this, HomePageActivity.class));
                                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            } else {
                                Toast.makeText(SignUpActivity.this, "Fail to signUp", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });
    redirectLogin.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    });

    }
}