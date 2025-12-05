package com.happym.mathsquare;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.FirebaseApp;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.happym.mathsquare.dashboard_StudentsPanel;
import com.happym.mathsquare.dashboard_SectionPanel;
import java.io.IOException;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.view.animation.BounceInterpolator;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

import com.happym.mathsquare.Model.Sections;
import com.happym.mathsquare.Model.Student;
import com.happym.mathsquare.utils.SessionManager;

public class Dashboard extends AppCompatActivity {
    private FirebaseFirestore db;
    private String teacherFirstName; // This should be initialized with the teacher's first name
    private RelativeLayout quizhistory_panel;
    private MediaPlayer bgMediaPlayer;
    private MediaPlayer soundEffectPlayer;
    private TextView firstSection, firstGrade;
    private ListenerRegistration sectionsListener;
    private String teacherEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.layout_teacher_dashboard);
        FirebaseApp.initializeApp(this);

          LinearLayout btnLogOut = findViewById(R.id.btn_logout);
          animateButtonFocus(btnLogOut);

          // Firestore instance
          db = FirebaseFirestore.getInstance();

          teacherEmail = sharedPreferences.getEmail(this);



          firstSection = findViewById(R.id.first_section);
          firstGrade = findViewById(R.id.first_grade);
          initializeSwitchListeners();

          quizhistory_panel = findViewById(R.id.quizhistory_panel);

        quizhistory_panel.setOnClickListener(v -> {
            playSound("click.mp3");
            Intent intent = new Intent(this, dashboard_StudentsPanel.class);
            startActivity(intent);
        });

        // Setup Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        ScrollView scrollView = findViewById(R.id.scroll);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            playSound("click.mp3");
            int itemId = item.getItemId();
            
            if (itemId == R.id.dashboard) {
                // Scroll to top of dashboard
                scrollView.smoothScrollTo(0, 0);
                return true;
            } else if (itemId == R.id.student_numbers) {
                showSectionSelectionDialog();
                return true;
            } else if (itemId == R.id.quiz_manager) {
                Intent intent = new Intent(this, QuizManagerActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.create_quiz) {
                Intent intent = new Intent(this, TeacherQuizCreatorActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.profile) {
                Intent intent = new Intent(this, TeacherProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        btnLogOut.setOnClickListener(view -> {
               animateButtonClick(btnLogOut);
               playSound("click.mp3");
               showLogoutConfirmationDialog();
               stopButtonFocusAnimation(btnLogOut);
               animateButtonFocus(btnLogOut);
        });

    }

   @Override
protected void onStart() {
    super.onStart();
    listenToTeacherSections(firstGrade, firstSection);
}

   @Override
protected void onStop() {
    super.onStop();
    // Remove listener to prevent memory leaks
    if (sectionsListener != null) {
        sectionsListener.remove();
        sectionsListener = null;
    }
}

    /**
     * Fetches and displays the teacher's assigned grade and section from their profile
     */
    private void listenToTeacherSections(TextView gradeTextView, TextView sectionTextView) {
        // Fetch teacher's assigned grade and section from TeacherProfiles
        sectionsListener = db.collection("TeacherProfiles")
            .whereEqualTo("email", teacherEmail)
            .limit(1)
            .addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (e != null) {
                    Log.e("Dashboard", "Error fetching teacher profile: " + e.getMessage());
                    // Set default values if error occurs
                    gradeTextView.setText("N/A");
                    sectionTextView.setText("N/A");
                    return;
                }

                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot teacherDoc = queryDocumentSnapshots.getDocuments().get(0);
                    String assignedGrade = teacherDoc.getString("assignedGrade");
                    String assignedSection = teacherDoc.getString("assignedSection");

                    if (assignedGrade != null && assignedSection != null && 
                        !assignedGrade.isEmpty() && !assignedSection.isEmpty()) {
                        // Display the assigned grade and section
                        gradeTextView.setText(assignedGrade);
                        sectionTextView.setText(assignedSection);
                    } else {
                        // No assigned grade/section, display default values
                        gradeTextView.setText("N/A");
                        sectionTextView.setText("N/A");
                    }
                } else {
                    // Teacher profile not found, display default values
                    gradeTextView.setText("N/A");
                    sectionTextView.setText("N/A");
                }
            });
}



    // Method to set up a real-time listener and toggle Firestore status
private void setupSwitchListener(SwitchCompat switchCompat, String quizId) {
    // Real-time listener to sync status from Firestore
    db.collection("Quizzes").document("Status").collection(quizId)
        .document("status")
        .addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.e("Quiz", "Error listening to status changes for " + quizId, e);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                String status = documentSnapshot.getString("status");
                if ("open".equalsIgnoreCase(status)) {
                    switchCompat.setChecked(true);
                } else if ("closed".equalsIgnoreCase(status)) {
                    switchCompat.setChecked(false);
                }
            }
        });

    // Switch toggle listener to update Firestore
    switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
        String newStatus = isChecked ? "open" : "closed";
               playSound("click.mp3");
        db.collection("Quizzes").document("Status").collection(quizId)
            .document("status")
            .update("status", newStatus)
            .addOnSuccessListener(aVoid -> Log.d("Quiz", quizId + " status updated to " + newStatus))
            .addOnFailureListener(error -> Log.e("Quiz", "Failed to update status for " + quizId, error));
    });
}
private void playSound(String fileName) {
        // Stop any previous sound effect before playing a new one
        if (soundEffectPlayer != null) {
            soundEffectPlayer.release();
            soundEffectPlayer = null;
        }

        try {
            AssetFileDescriptor afd = getAssets().openFd(fileName);
            soundEffectPlayer = new MediaPlayer();
            soundEffectPlayer.setDataSource(
                    afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            soundEffectPlayer.prepare();
            soundEffectPlayer.setOnCompletionListener(
                    mp -> {
                        mp.release();
                        soundEffectPlayer = null;
                    });
            soundEffectPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
// Call this in onCreate() or appropriate lifecycle method
private void initializeSwitchListeners() {
    // Quiz switches removed from teacher dashboard
}
     //Game Button Animation Press

private void animateButtonClick(View button) {
    ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.6f, 1.1f, 1f);
    ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.6f, 1.1f, 1f);

    // Set duration for the animations
    scaleX.setDuration(1000);
    scaleY.setDuration(1000);

    // OvershootInterpolator for game-like snappy effect
    OvershootInterpolator overshootInterpolator = new OvershootInterpolator(2f);
    scaleX.setInterpolator(overshootInterpolator);
    scaleY.setInterpolator(overshootInterpolator);

    // Combine animations into a set
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(scaleX, scaleY);
    animatorSet.start();
}


// Function to animate button focus with a smooth pulsing bounce effect
private void animateButtonFocus(View button) {
    ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.06f, 1f);
    ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.06f, 1f);

    // Set duration for a slower, smoother pulsing bounce effect
    scaleX.setDuration(2000);
    scaleY.setDuration(2000);

    // AccelerateDecelerateInterpolator for smooth pulsing
    AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
    scaleX.setInterpolator(interpolator);
    scaleY.setInterpolator(interpolator);

    // Set repeat count and mode on each ObjectAnimator
    scaleX.setRepeatCount(ObjectAnimator.INFINITE);  // Infinite repeat
    scaleX.setRepeatMode(ObjectAnimator.REVERSE);    // Reverse animation on repeat
    scaleY.setRepeatCount(ObjectAnimator.INFINITE);  // Infinite repeat
    scaleY.setRepeatMode(ObjectAnimator.REVERSE);    // Reverse animation on repeat

    // Combine the animations into an AnimatorSet
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(scaleX, scaleY);
    animatorSet.start();
}

// Stop Focus Animation
private void stopButtonFocusAnimation(View button) {
    AnimatorSet animatorSet = (AnimatorSet) button.getTag();
    if (animatorSet != null) {
        animatorSet.cancel();  // Stop the animation when focus is lost
    }
}

/**
 * Extracts the numeric grade value from a string.
 * Handles cases like "1", "Grade 1", "1st", etc.
 * @param gradeString The grade string to parse
 * @return The numeric grade as a string, or null if not found
 */
private String extractGradeNumber(String gradeString) {
    if (gradeString == null) return null;
    
    // Trim whitespace
    String trimmed = gradeString.trim();
    if (trimmed.isEmpty()) return null;
    
    // Try to parse directly as integer
    try {
        int num = Integer.parseInt(trimmed);
        return String.valueOf(num);
    } catch (NumberFormatException e) {
        // If direct parse fails, try to extract number from string
        // Remove all non-digit characters except minus sign
        String digitsOnly = trimmed.replaceAll("[^0-9]", "");
        if (!digitsOnly.isEmpty()) {
            try {
                int num = Integer.parseInt(digitsOnly);
                return String.valueOf(num);
            } catch (NumberFormatException ex) {
                // Still failed
            }
        }
    }
    
    return null;
}

    /**
     * Shows a dialog to select a section before opening Student Number Manager
     * If teacher has assigned grade/section, skip selection and go directly
     */
    private void showSectionSelectionDialog() {
        // First check if teacher has assigned grade and section
        db.collection("TeacherProfiles")
            .whereEqualTo("email", teacherEmail)
            .limit(1)
            .get()
            .addOnCompleteListener(teacherTask -> {
                if (teacherTask.isSuccessful() && teacherTask.getResult() != null && !teacherTask.getResult().isEmpty()) {
                    QueryDocumentSnapshot teacherDoc = (QueryDocumentSnapshot) teacherTask.getResult().getDocuments().get(0);
                    String assignedGrade = teacherDoc.getString("assignedGrade");
                    String assignedSection = teacherDoc.getString("assignedSection");
                    
                    // If teacher has assigned grade and section, use them directly
                    if (assignedGrade != null && assignedSection != null && 
                        !assignedGrade.isEmpty() && !assignedSection.isEmpty()) {
                        // Find the section document ID
                        int gradeNumber;
                        try {
                            gradeNumber = Integer.parseInt(assignedGrade);
                        } catch (NumberFormatException e) {
                            // Invalid grade, fall back to selection dialog
                            showSectionSelectionDialogFallback();
                            return;
                        }
                        
                        db.collection("Sections")
                            .whereEqualTo("Grade_Number", gradeNumber)
                            .whereEqualTo("Section", assignedSection)
                            .limit(1)
                            .get()
                            .addOnCompleteListener(sectionTask -> {
                                if (sectionTask.isSuccessful() && sectionTask.getResult() != null && !sectionTask.getResult().isEmpty()) {
                                    QueryDocumentSnapshot sectionDoc = (QueryDocumentSnapshot) sectionTask.getResult().getDocuments().get(0);
                                    String sectionId = sectionDoc.getId();
                                    
                                    // Go directly to StudentNumberManagerActivity
                                    Intent intent = new Intent(this, StudentNumberManagerActivity.class);
                                    intent.putExtra("sectionName", assignedSection);
                                    intent.putExtra("grade", assignedGrade);
                                    intent.putExtra("sectionId", sectionId);
                                    startActivity(intent);
                                } else {
                                    // Section not found, fall back to selection dialog
                                    showSectionSelectionDialogFallback();
                                }
                            });
                    } else {
                        // No assigned grade/section, show selection dialog
                        showSectionSelectionDialogFallback();
                    }
                } else {
                    // Teacher profile not found, show selection dialog
                    showSectionSelectionDialogFallback();
                }
            });
    }
    
    /**
     * Fallback: Shows dialog to select a section (original behavior)
     */
    private void showSectionSelectionDialogFallback() {
        // Fetch all sections for the teacher
        db.collection("Sections")
            .orderBy("Grade_Number", Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                    List<Sections> sectionsList = new ArrayList<>();
                    String[] sectionNames = new String[task.getResult().size()];
                    
                    int index = 0;
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Long gradeNumLong = doc.getLong("Grade_Number");
                        String grade = gradeNumLong != null ? String.valueOf(gradeNumLong.intValue()) : null;
                        String section = doc.getString("Section");
                        String docId = doc.getId();
                        
                        if (grade != null && section != null) {
                            sectionsList.add(new Sections(section, grade, docId));
                            sectionNames[index] = "Grade " + grade + " - " + section;
                            index++;
                        }
                    }
                    
                    if (sectionsList.isEmpty()) {
                        Toast.makeText(this, "No sections found. Please create a section first.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    // Create and show custom dialog
                    showCustomSectionSelectionDialog(sectionsList);
                } else {
                    Toast.makeText(this, "No sections found. Please create a section first.", Toast.LENGTH_LONG).show();
                    if (task.getException() != null) {
                        Log.e("Dashboard", "Error fetching sections", task.getException());
                    }
                }
            });
    }
    
    /**
     * Shows a custom styled dialog to select a section
     */
    private void showCustomSectionSelectionDialog(List<Sections> sectionsList) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_section_selection, null);
        
        RecyclerView sectionsRecyclerView = dialogView.findViewById(R.id.sectionsRecyclerView);
        TextView emptyStateText = dialogView.findViewById(R.id.emptyStateText);
        Button cancelButton = dialogView.findViewById(R.id.btnCancel);
        
        // Setup RecyclerView
        Dashboard dashboardActivity = this;
        SectionSelectionAdapter adapter = new SectionSelectionAdapter(sectionsList, section -> {
            String gradeNumber = extractGradeNumber(section.getGrade());
            
            if (gradeNumber != null) {
                Intent intent = new Intent(dashboardActivity, StudentNumberManagerActivity.class);
                intent.putExtra("sectionName", section.getSection());
                intent.putExtra("grade", gradeNumber);
                intent.putExtra("sectionId", section.getDocId());
                dashboardActivity.startActivity(intent);
                // Dismiss dialog after selection
                if (sectionSelectionDialog != null) {
                    sectionSelectionDialog.dismiss();
                }
            } else {
                Toast.makeText(dashboardActivity, "Invalid grade format: " + section.getGrade(), Toast.LENGTH_LONG).show();
            }
        });
        
        sectionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sectionsRecyclerView.setAdapter(adapter);
        
        // Show/hide empty state
        if (sectionsList.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            sectionsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            sectionsRecyclerView.setVisibility(View.VISIBLE);
        }
        
        // Create and show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.RoundedAlertDialog);
        builder.setView(dialogView);
        sectionSelectionDialog = builder.create();
        sectionSelectionDialog.show();
        
        // Cancel button
        cancelButton.setOnClickListener(v -> {
            playSound("click.mp3");
            sectionSelectionDialog.dismiss();
        });
    }
    
    private AlertDialog sectionSelectionDialog;
    
    // Interface for section click listener
    private interface OnSectionClickListener {
        void onSectionClick(Sections section);
    }
    
    // Adapter for section selection RecyclerView
    private class SectionSelectionAdapter extends RecyclerView.Adapter<SectionSelectionAdapter.ViewHolder> {
        private List<Sections> sections;
        private OnSectionClickListener listener;
        
        public SectionSelectionAdapter(List<Sections> sections, OnSectionClickListener listener) {
            this.sections = sections;
            this.listener = listener;
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_section_selection, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Sections section = sections.get(position);
            holder.sectionNameText.setText("Grade " + section.getGrade() + " - " + section.getSection());
            holder.sectionDetailsText.setText("Tap to manage student numbers");
            
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSectionClick(section);
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return sections.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView sectionNameText, sectionDetailsText;
            
            ViewHolder(View itemView) {
                super(itemView);
                sectionNameText = itemView.findViewById(R.id.sectionNameText);
                sectionDetailsText = itemView.findViewById(R.id.sectionDetailsText);
            }
        }
    }
    
    /**
     * Show logout confirmation dialog
     */
    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.RoundedAlertDialog);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            SessionManager.logoutTeacher(this);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

}
