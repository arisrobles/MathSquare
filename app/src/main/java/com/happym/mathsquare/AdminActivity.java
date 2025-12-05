package com.happym.mathsquare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.happym.mathsquare.utils.SessionManager;

public class AdminActivity extends AppCompatActivity {

    private Fragment fragment;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_admin);


        executeFragment();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.user) {
                executeFragment();
                return true;
            } else if (itemId == R.id.sections) {
                // Navigate to section management
                Intent intent = new Intent(AdminActivity.this, dashboard_SectionPanel.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.profile) {
                Intent intent = new Intent(AdminActivity.this, AdminProfileActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.logout) {
                showLogoutConfirmationDialog();
                return true;
            }
            return false;
        });
    }

    void executeFragment() {
        fragment = new UserFragment();
        ReplaceFragment(fragment);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    void ReplaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }
    
    /**
     * Show logout confirmation dialog
     */
    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this, R.style.RoundedAlertDialog);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            SessionManager.logoutAdmin(this);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

}