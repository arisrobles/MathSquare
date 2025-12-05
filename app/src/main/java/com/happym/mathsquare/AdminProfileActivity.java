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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.happym.mathsquare.sharedPreferences;
import androidx.core.view.WindowCompat;

public class AdminProfileActivity extends AppCompatActivity {
    
    private FirebaseFirestore db;
    private TextView tvAdminName, tvAdminEmail;
    private LinearLayout btnChangePassword;
    private String adminEmail;
    private String adminDocId;
    private ProgressDialog loadingDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.layout_admin_profile);
        
        db = FirebaseFirestore.getInstance();
        adminEmail = sharedPreferences.getEmail(this);
        
        // Initialize views
        tvAdminName = findViewById(R.id.tv_admin_name);
        tvAdminEmail = findViewById(R.id.tv_admin_email);
        btnChangePassword = findViewById(R.id.btn_change_password);
        
        // Show loading dialog
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Loading profile...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        
        // Load admin data
        loadAdminData();
        
        // Setup change password button
        if (btnChangePassword != null) {
            animateButtonFocus(btnChangePassword);
            btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }
    }
    
    private void loadAdminData() {
        if (adminEmail == null || adminEmail.isEmpty()) {
            Toast.makeText(this, "Error: Admin email not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Fetch from Admin collection
        db.collection("Admin")
            .whereEqualTo("email", adminEmail)
            .limit(1)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    QueryDocumentSnapshot doc = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                    String firstName = doc.getString("firstName");
                    adminDocId = doc.getId();
                    
                    if (firstName != null) {
                        tvAdminName.setText(firstName);
                    } else {
                        tvAdminName.setText("N/A");
                    }
                    
                    tvAdminEmail.setText(adminEmail);
                } else {
                    Toast.makeText(this, "Error: Admin profile not found", Toast.LENGTH_SHORT).show();
                }
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
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
        if (adminEmail == null || adminEmail.isEmpty() || adminDocId == null) {
            Toast.makeText(this, "Error: Admin information not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Verify current password first
        db.collection("Admin")
            .document(adminDocId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String storedPassword = documentSnapshot.getString("password");
                    
                    if (storedPassword != null && storedPassword.equals(currentPassword)) {
                        // Current password matches, update to new password
                        db.collection("Admin")
                            .document(adminDocId)
                            .update("password", newPassword)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                Log.d("ADMIN_PROFILE", "Password updated successfully");
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error updating password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("ADMIN_PROFILE", "Error updating password", e);
                            });
                    } else {
                        Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Error: Admin profile not found", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ADMIN_PROFILE", "Error verifying password", e);
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

