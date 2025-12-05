package com.happym.mathsquare;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.happym.mathsquare.sharedPreferences;
import androidx.core.view.WindowCompat;

public class TeacherProfileActivity extends AppCompatActivity {
    
    private FirebaseFirestore db;
    private TextView tvTeacherName, tvTeacherEmail, tvTeacherGrade, tvTeacherSection;
    private LinearLayout btnChangePassword;
    private String teacherEmail;
    private ProgressDialog loadingDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.layout_teacher_profile);
        
        db = FirebaseFirestore.getInstance();
        teacherEmail = sharedPreferences.getEmail(this);
        
        // Initialize views
        tvTeacherName = findViewById(R.id.tv_teacher_name);
        tvTeacherEmail = findViewById(R.id.tv_teacher_email);
        tvTeacherGrade = findViewById(R.id.tv_teacher_grade);
        tvTeacherSection = findViewById(R.id.tv_teacher_section);
        btnChangePassword = findViewById(R.id.btn_change_password);
        
        // Show loading dialog
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Loading profile...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        
        // Load teacher data
        loadTeacherData();
        
        // Setup change password button
        if (btnChangePassword != null) {
            animateButtonFocus(btnChangePassword);
            btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }
    }
    
    private void loadTeacherData() {
        if (teacherEmail == null || teacherEmail.isEmpty()) {
            Toast.makeText(this, "Error: Teacher email not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Fetch from TeacherProfiles collection (where assignedGrade and assignedSection are stored)
        db.collection("TeacherProfiles")
            .whereEqualTo("email", teacherEmail)
            .limit(1)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                    DocumentSnapshot teacherDoc = task.getResult().getDocuments().get(0);
                    String firstName = teacherDoc.getString("firstName");
                    String assignedGrade = teacherDoc.getString("assignedGrade");
                    String assignedSection = teacherDoc.getString("assignedSection");
                    
                    if (firstName != null) {
                        tvTeacherName.setText(firstName);
                    } else {
                        // Fallback: try to get from MyProfile if not in TeacherProfiles
                        fetchNameFromMyProfile();
                    }
                    
                    tvTeacherEmail.setText(teacherEmail);
                    
                    if (assignedGrade != null && !assignedGrade.isEmpty()) {
                        tvTeacherGrade.setText(assignedGrade);
                    } else {
                        tvTeacherGrade.setText("N/A");
                    }
                    
                    if (assignedSection != null && !assignedSection.isEmpty()) {
                        tvTeacherSection.setText(assignedSection);
                    } else {
                        tvTeacherSection.setText("N/A");
                    }
                    
                    if (loadingDialog != null && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                } else {
                    // If not found in TeacherProfiles, try MyProfile as fallback
                    Log.w("TEACHER_PROFILE", "Teacher not found in TeacherProfiles, trying MyProfile");
                    fetchFromMyProfile();
                }
            });
    }
    
    /**
     * Fallback method to fetch teacher name from MyProfile if not found in TeacherProfiles
     */
    private void fetchNameFromMyProfile() {
        db.collection("Accounts")
            .document("Teachers")
            .collection(teacherEmail)
            .document("MyProfile")
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
                if (documentSnapshot.exists()) {
                    String firstName = documentSnapshot.getString("firstName");
                    if (firstName != null) {
                        tvTeacherName.setText(firstName);
                    } else {
                        tvTeacherName.setText("N/A");
                    }
                } else {
                    tvTeacherName.setText("N/A");
                }
            })
            .addOnFailureListener(e -> {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
            });
    }
    
    /**
     * Fallback method to fetch all data from MyProfile if TeacherProfiles doesn't exist
     */
    private void fetchFromMyProfile() {
        db.collection("Accounts")
            .document("Teachers")
            .collection(teacherEmail)
            .document("MyProfile")
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String firstName = documentSnapshot.getString("firstName");
                    String assignedGrade = documentSnapshot.getString("assignedGrade");
                    String assignedSection = documentSnapshot.getString("assignedSection");
                    
                    if (firstName != null) {
                        tvTeacherName.setText(firstName);
                    } else {
                        tvTeacherName.setText("N/A");
                    }
                    
                    tvTeacherEmail.setText(teacherEmail);
                    
                    if (assignedGrade != null && !assignedGrade.isEmpty()) {
                        tvTeacherGrade.setText(assignedGrade);
                    } else {
                        tvTeacherGrade.setText("N/A");
                    }
                    
                    if (assignedSection != null && !assignedSection.isEmpty()) {
                        tvTeacherSection.setText(assignedSection);
                    } else {
                        tvTeacherSection.setText("N/A");
                    }
                } else {
                    Toast.makeText(this, "Error: Teacher profile not found", Toast.LENGTH_SHORT).show();
                    tvTeacherName.setText("N/A");
                    tvTeacherGrade.setText("N/A");
                    tvTeacherSection.setText("N/A");
                }
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
            })
            .addOnFailureListener(e -> {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
                Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("TEACHER_PROFILE", "Error loading teacher data", e);
            });
    }
    
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.RoundedAlertDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);
        
        TextInputLayout currentPasswordLayout = dialogView.findViewById(R.id.current_password_layout);
        TextInputLayout newPasswordLayout = dialogView.findViewById(R.id.new_password_layout);
        TextInputLayout confirmPasswordLayout = dialogView.findViewById(R.id.confirm_password_layout);
        
        // Prevent spaces in password fields
        InputFilter noSpacesFilter = (source, start, end, dest, dstart, dend) -> {
            if (source.toString().contains(" ")) {
                return "";
            }
            return source;
        };
        
        if (currentPasswordLayout.getEditText() != null) {
            currentPasswordLayout.getEditText().setFilters(new InputFilter[]{noSpacesFilter});
        }
        if (newPasswordLayout.getEditText() != null) {
            newPasswordLayout.getEditText().setFilters(new InputFilter[]{noSpacesFilter});
        }
        if (confirmPasswordLayout.getEditText() != null) {
            confirmPasswordLayout.getEditText().setFilters(new InputFilter[]{noSpacesFilter});
        }
        
        builder.setPositiveButton("Change", null);
        
        builder.setNegativeButton("Cancel", null);
        
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        // Set positive button listener after dialog creation to prevent auto-dismiss
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String currentPassword = currentPasswordLayout.getEditText() != null ?
                    currentPasswordLayout.getEditText().getText().toString() : "";
                String newPassword = newPasswordLayout.getEditText() != null ?
                    newPasswordLayout.getEditText().getText().toString() : "";
                String confirmPassword = confirmPasswordLayout.getEditText() != null ?
                    confirmPasswordLayout.getEditText().getText().toString() : "";
                
                if (validatePasswordChange(currentPassword, newPassword, confirmPassword)) {
                    dialog.dismiss();
                    updatePassword(currentPassword, newPassword);
                }
            });
        });
        
        dialog.show();
    }
    
    private boolean validatePasswordChange(String currentPassword, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(this, "Current password is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "New password is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (newPassword.length() < 6) {
            Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void updatePassword(String currentPassword, String newPassword) {
        if (teacherEmail == null || teacherEmail.isEmpty()) {
            Toast.makeText(this, "Error: Teacher email not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Verify current password first
        db.collection("Accounts")
            .document("Teachers")
            .collection(teacherEmail)
            .document("MyProfile")
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String storedPassword = documentSnapshot.getString("password");
                    
                    if (storedPassword != null && storedPassword.equals(currentPassword)) {
                        // Current password matches, update to new password
                        db.collection("Accounts")
                            .document("Teachers")
                            .collection(teacherEmail)
                            .document("MyProfile")
                            .update("password", newPassword)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                Log.d("TEACHER_PROFILE", "Password updated successfully");
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error updating password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("TEACHER_PROFILE", "Error updating password", e);
                            });
                    } else {
                        Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Error: Teacher profile not found", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("TEACHER_PROFILE", "Error verifying password", e);
            });
    }
    
    private void animateButtonFocus(View button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f, 1.05f, 1f);
        
        scaleX.setDuration(2000);
        scaleY.setDuration(2000);
        
        OvershootInterpolator overshootInterpolator = new OvershootInterpolator(2f);
        scaleX.setInterpolator(overshootInterpolator);
        scaleY.setInterpolator(overshootInterpolator);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();
    }
}

