package com.happym.mathsquare.utils;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class to migrate existing teacher accounts from the old structure
 * (Accounts/Teachers/{email}/MyProfile) to the new queryable TeacherProfiles collection
 */
public class TeacherMigrationUtil {

    private static final String TAG = "TeacherMigration";
    private FirebaseFirestore db;

    public TeacherMigrationUtil() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Migrate existing teacher accounts to TeacherProfiles collection
     * This method should be called once to migrate existing data
     */
    public void migrateExistingTeachers() {
        Log.d(TAG, "Starting teacher migration...");

        // Note: This is a simplified approach since we can't directly query all subcollections
        // In a real scenario, you would need to know the teacher emails or use a different approach

        // For the emails visible in your screenshot, let's migrate them
        String[] knownTeacherEmails = {
            "naxs@gmail.com",
            "teacher@gmail.com"
        };

        for (String email : knownTeacherEmails) {
            migrateTeacherByEmail(email);
        }
    }

    /**
     * Migrate a specific teacher by email
     */
    private void migrateTeacherByEmail(String email) {
        Log.d(TAG, "Attempting to migrate teacher with email: " + email);

        db.collection("Accounts")
            .document("Teachers")
            .collection(email)
            .document("MyProfile")
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                Log.d(TAG, "Fetch successful for " + email + ". Document exists: " + documentSnapshot.exists());

                if (documentSnapshot.exists()) {
                    try {
                        String firstName = documentSnapshot.getString("firstName");
                        String teacherEmail = documentSnapshot.getString("email");
                        String password = documentSnapshot.getString("password");

                        Log.d(TAG, "Teacher data - Name: " + firstName + ", Email: " + teacherEmail);

                        if (firstName != null && teacherEmail != null) {
                            // Create teacher profile data
                            Map<String, Object> profileData = new HashMap<>();
                            profileData.put("firstName", firstName);
                            profileData.put("email", teacherEmail);
                            profileData.put("password", password);
                            profileData.put("originalEmail", email);
                            profileData.put("accountType", "Teacher");
                            profileData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
                            profileData.put("migrated", true);

                            // Generate unique ID for the profile
                            String teacherId = UUID.randomUUID().toString();

                            Log.d(TAG, "Saving teacher profile with ID: " + teacherId);

                            // Save to TeacherProfiles collection
                            db.collection("TeacherProfiles")
                                .document(teacherId)
                                .set(profileData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Successfully migrated teacher: " + email + " (" + firstName + ")");

                                    // Also add to TeacherEmailList for future discovery
                                    addToTeacherEmailList(email);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to migrate teacher " + email + ": " + e.getMessage());
                                });
                        } else {
                            Log.w(TAG, "Missing required data for teacher " + email + " - firstName: " + firstName + ", email: " + teacherEmail);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing teacher data for " + email + ": " + e.getMessage());
                    }
                } else {
                    Log.w(TAG, "No profile document found for teacher: " + email);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to fetch teacher profile for " + email + ": " + e.getMessage());
            });
    }

    /**
     * Check if migration is needed by checking if TeacherProfiles collection has any documents
     */
    public void checkAndMigrateIfNeeded() {
        Log.d(TAG, "Checking if teacher migration is needed...");

        db.collection("TeacherProfiles")
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "TeacherProfiles check successful. Documents found: " + queryDocumentSnapshots.size());

                if (queryDocumentSnapshots.isEmpty()) {
                    Log.d(TAG, "TeacherProfiles collection is empty, starting migration...");
                    migrateExistingTeachers();
                } else {
                    Log.d(TAG, "TeacherProfiles collection already has data (" + queryDocumentSnapshots.size() + " docs), skipping migration");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to check TeacherProfiles collection: " + e.getMessage());
                // Try migration anyway in case collection doesn't exist yet
                Log.d(TAG, "Attempting migration despite check failure...");
                migrateExistingTeachers();
            });
    }

    /**
     * Add teacher email to discoverable list
     */
    private void addToTeacherEmailList(String email) {
        // Add email to a list in a special document for discovery
        Map<String, Object> emailData = new HashMap<>();
        emailData.put("email", email);
        emailData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
        emailData.put("migrated", true);

        db.collection("TeacherEmailList")
                .document(email.replace(".", "_").replace("@", "_at_")) // Safe document ID
                .set(emailData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Teacher email added to discovery list: " + email);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add teacher email to discovery list: " + e.getMessage());
                });
    }
}
