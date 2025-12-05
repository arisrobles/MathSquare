package com.happym.mathsquare;

import android.text.InputFilter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.happym.mathsquare.sharedPreferences;
import androidx.core.view.WindowCompat;

public class studentLogIn extends AppCompatActivity {
    
    private FirebaseFirestore db;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        FirebaseApp.initializeApp(this);

        setContentView(R.layout.layoutstudent_log_in);

        // Firestore instance
        db = FirebaseFirestore.getInstance();

        TextInputLayout studentIdLayout = findViewById(R.id.student_id_layout);
        TextInputLayout passwordLayout = findViewById(R.id.password_layout);
        AppCompatButton submitButton = findViewById(R.id.btn_sign_in);
        TextView signUpLink = findViewById(R.id.sign_up_link);

        // Defensive check - if views are null, the layout might have issues
        if (studentIdLayout == null || passwordLayout == null || submitButton == null) {
            Log.e("STUDENT_LOGIN", "❌ Critical views not found in layout!");
            Toast.makeText(this, "Error loading login screen. Please restart the app.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        TextInputEditText studentIdEditText = (TextInputEditText) studentIdLayout.getEditText();

        InputFilter noSpacesFilter = (source, start, end, dest, dstart, dend) -> {
            if (source.toString().contains(" ")) {
                return "";
            }
            return source;
        };

        if (studentIdEditText != null) {
            studentIdEditText.setFilters(new InputFilter[]{noSpacesFilter});
        }

        TextInputEditText passwordEditText = (TextInputEditText) passwordLayout.getEditText();

        if (passwordEditText != null) {
            passwordEditText.setFilters(new InputFilter[]{noSpacesFilter});
        }

        // Sign up link - navigate to registration
        if (signUpLink != null) {
            signUpLink.setOnClickListener(v -> {
                Intent intent = new Intent(studentLogIn.this, studentSignUp.class);
                startActivity(intent);
            });
        }

        submitButton.setOnClickListener(v -> {
            boolean hasError = false;
            studentIdLayout.setError(null);
            passwordLayout.setError(null);

            String studentId = ((TextInputEditText) studentIdLayout.getEditText()).getText().toString().trim();
            if (TextUtils.isEmpty(studentId)) {
                studentIdLayout.setError("Student ID is required");
                animateShakeRotateEditTextErrorAnimation(studentIdLayout);
                hasError = true;
            }

            String password = ((TextInputEditText) passwordLayout.getEditText()).getText().toString();
            if (TextUtils.isEmpty(password)) {
                passwordLayout.setError("Password is required");
                animateShakeRotateEditTextErrorAnimation(passwordLayout);
                hasError = true;
            }

            if (!hasError) {
                animateButtonClick(submitButton);
                
                // Query Firebase for student with matching student ID and password
                db.collection("Accounts")
                    .document("Students")
                    .collection("MathSquare")
                    .whereEqualTo("studentNumber", studentId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                // Student ID found, check password
                                QueryDocumentSnapshot doc = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                                String storedPassword = doc.getString("password");
                                
                                if (storedPassword != null && storedPassword.equals(password)) {
                                    // Password matches - login successful
                                    String firstName = doc.getString("firstName");
                                    String lastName = doc.getString("lastName");
                                    String section = doc.getString("section");
                                    String grade = doc.getString("grade");
                                    
                                    // Save to SharedPreferences
                                    sharedPreferences.StudentIsSetLoggedIn(studentLogIn.this, true);
                                    sharedPreferences.setLoggedIn(studentLogIn.this, false);
                                    sharedPreferences.saveSection(studentLogIn.this, section);
                                    sharedPreferences.saveGrade(studentLogIn.this, grade);
                                    sharedPreferences.saveFirstN(studentLogIn.this, firstName);
                                    sharedPreferences.saveLastN(studentLogIn.this, lastName);
                                    sharedPreferences.saveStudentNumber(studentLogIn.this, studentId);
                                    
                                    Log.d("STUDENT_LOGIN", "✅ Student login successful: " + studentId);
                                    
                                    Intent intent = new Intent(studentLogIn.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                    Toast.makeText(this, "Welcome back, " + firstName + "!", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Incorrect password
                                    passwordLayout.setError("Incorrect password");
                                    animateShakeRotateEditTextErrorAnimation(passwordLayout);
                                    Log.w("STUDENT_LOGIN", "❌ Incorrect password for student ID: " + studentId);
                                }
                            } else {
                                // Student ID not found
                                studentIdLayout.setError("Student ID not found");
                                animateShakeRotateEditTextErrorAnimation(studentIdLayout);
                                Log.w("STUDENT_LOGIN", "❌ Student ID not found: " + studentId);
                            }
                        } else {
                            // Query failed
                            Log.e("STUDENT_LOGIN", "❌ Error querying student account", task.getException());
                            Toast.makeText(this, "Error accessing account: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });
    }

    private void animateShakeRotateEditTextErrorAnimation(View view) {
        AnimatorSet animatorSet = new AnimatorSet();

        // Shake animation
        ObjectAnimator shakeAnimator = ObjectAnimator.ofFloat(view, "translationX", 0, 20f, -20f, 20f, -20f, 0);
        shakeAnimator.setDuration(350);

        // Rotate animation
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(view, "rotation", 0, 5f, -5f, 5f, -5f, 0);
        rotateAnimator.setDuration(350);

        // Play both animations together
        animatorSet.playTogether(shakeAnimator, rotateAnimator);
        animatorSet.start();
    }

    private void animateButtonClick(View button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.6f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.6f, 1.1f, 1f);

        // Set duration for the animations
        scaleX.setDuration(3000);
        scaleY.setDuration(3000);

        // OvershootInterpolator for game-like snappy effect
        OvershootInterpolator overshootInterpolator = new OvershootInterpolator(2f);
        scaleX.setInterpolator(overshootInterpolator);
        scaleY.setInterpolator(overshootInterpolator);

        // Combine animations into a set
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();
    }
}

