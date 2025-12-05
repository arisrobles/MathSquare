package com.happym.mathsquare;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.happym.mathsquare.sharedPreferences;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for teachers to create and manage student numbers for their sections
 * Implements defense comment #1: Student number restriction system
 */
public class StudentNumberManagerActivity extends AppCompatActivity {
    
    private FirebaseFirestore db;
    private EditText studentNumberInput;
    private Button addButton, saveButton;
    private LinearLayout studentNumbersList;
    private TextView txtSectionInfo, txtStudentCount, txtEmptyState;
    private List<String> studentNumbers;
    private String currentSectionId;
    private String currentSectionName;
    private String currentGrade;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_student_number_manager);
        
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        
        studentNumberInput = findViewById(R.id.editStudentNumber);
        addButton = findViewById(R.id.btnAddStudentNumber);
        saveButton = findViewById(R.id.btnSaveStudentNumbers);
        studentNumbersList = findViewById(R.id.studentNumbersList);
        txtSectionInfo = findViewById(R.id.txtSectionInfo);
        txtStudentCount = findViewById(R.id.txtStudentCount);
        txtEmptyState = findViewById(R.id.txtEmptyState);
        
        studentNumbers = new ArrayList<>();
        
        // Get section info from intent or SharedPreferences
        String sectionNameFromIntent = getIntent().getStringExtra("sectionName");
        String gradeFromIntent = getIntent().getStringExtra("grade");
        String sectionIdFromIntent = getIntent().getStringExtra("sectionId"); // Try to get section ID directly
        
        if (sectionNameFromIntent == null || gradeFromIntent == null) {
            Toast.makeText(this, "Section information not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Trim section name immediately to ensure consistency
        currentSectionName = sectionNameFromIntent.trim();
        
        // Extract numeric grade value (trim and extract number)
        currentGrade = extractGradeNumber(gradeFromIntent);
        
        if (currentGrade == null) {
            Toast.makeText(this, "Invalid grade format: " + gradeFromIntent + ". Please select a valid section.", Toast.LENGTH_LONG).show();
            Log.e("StudentNumberManager", "Invalid grade format received: '" + gradeFromIntent + "'");
            finish();
            return;
        }
        
        // Set title and section info
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Student Numbers");
        }
        if (txtSectionInfo != null) {
            txtSectionInfo.setText("Section: " + currentSectionName + " | Grade: " + currentGrade);
        }
        
        // If section ID was passed directly, use it; otherwise find it
        if (sectionIdFromIntent != null && !sectionIdFromIntent.trim().isEmpty()) {
            currentSectionId = sectionIdFromIntent.trim();
            Log.d("StudentNumberManager", "✅ Using section ID from intent: " + currentSectionId);
            Log.d("StudentNumberManager", "  - Section Name: '" + currentSectionName + "'");
            Log.d("StudentNumberManager", "  - Grade: " + currentGrade);
            // Verify the section exists and matches, then load student numbers
            verifySectionAndLoad();
        } else {
            Log.d("StudentNumberManager", "⚠️ No section ID in intent, searching for section...");
            // Find section document ID (this will call loadExistingStudentNumbers when done)
            findSectionDocument();
        }
        
        addButton.setOnClickListener(v -> addStudentNumber());
        saveButton.setOnClickListener(v -> saveStudentNumbers());
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
    
    private void findSectionDocument() {
        // Validate that grade is a number
        int gradeNumber;
        try {
            gradeNumber = Integer.parseInt(currentGrade);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid grade format: " + currentGrade, Toast.LENGTH_LONG).show();
            Log.e("StudentNumberManager", "Invalid grade format: " + currentGrade, e);
            finish();
            return;
        }
        
        // Trim section name to handle whitespace issues
        String trimmedSectionName = currentSectionName != null ? currentSectionName.trim() : "";
        
        Log.d("StudentNumberManager", "🔍 Searching for section...");
        Log.d("StudentNumberManager", "  - Section Name (original): '" + currentSectionName + "'");
        Log.d("StudentNumberManager", "  - Section Name (trimmed): '" + trimmedSectionName + "'");
        Log.d("StudentNumberManager", "  - Grade Number: " + gradeNumber);
        Log.d("StudentNumberManager", "  - Query: Sections where Section='" + trimmedSectionName + "' AND Grade_Number=" + gradeNumber);
        
        db.collection("Sections")
            .whereEqualTo("Section", trimmedSectionName)
            .whereEqualTo("Grade_Number", gradeNumber)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    int resultCount = task.getResult().size();
                    Log.d("StudentNumberManager", "Query returned " + resultCount + " matching section(s)");
                    
                    if (!task.getResult().isEmpty()) {
                        // If multiple sections match, log a warning
                        if (task.getResult().size() > 1) {
                            Log.w("StudentNumberManager", "⚠️ WARNING: Multiple sections match the criteria!");
                            Log.w("StudentNumberManager", "  - Found " + task.getResult().size() + " matching sections");
                            Log.w("StudentNumberManager", "  - Using the first one, but this might cause issues!");
                            int index = 1;
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Log.w("StudentNumberManager", "    " + index + ". ID: " + doc.getId() + 
                                      ", Section: '" + doc.getString("Section") + 
                                      "', Grade: " + doc.getLong("Grade_Number"));
                                index++;
                            }
                        }
                        
                        QueryDocumentSnapshot firstDoc = task.getResult().iterator().next();
                        currentSectionId = firstDoc.getId();
                        String foundSectionName = firstDoc.getString("Section");
                        Long foundGradeNum = firstDoc.getLong("Grade_Number");
                        
                        Log.d("StudentNumberManager", "✅ Section found!");
                        Log.d("StudentNumberManager", "  - Section ID: " + currentSectionId);
                        Log.d("StudentNumberManager", "  - ⚠️⚠️⚠️ LOAD THIS SECTION ID: " + currentSectionId + " ⚠️⚠️⚠️");
                        Log.d("StudentNumberManager", "  - Section Name (from DB): '" + foundSectionName + "'");
                        Log.d("StudentNumberManager", "  - Section Name (expected): '" + trimmedSectionName + "'");
                        Log.d("StudentNumberManager", "  - Grade Number: " + foundGradeNum);
                        Log.d("StudentNumberManager", "  - 🔍 Compare this ID with the one used during save!");
                        
                        // Verify the found section matches what we're looking for
                        String foundSectionTrimmed = foundSectionName != null ? foundSectionName.trim() : "";
                        if (!trimmedSectionName.equals(foundSectionTrimmed) || gradeNumber != foundGradeNum.intValue()) {
                            Log.w("StudentNumberManager", "⚠️ WARNING: Found section doesn't match search criteria!");
                            Log.w("StudentNumberManager", "  - Expected: Section='" + trimmedSectionName + "', Grade=" + gradeNumber);
                            Log.w("StudentNumberManager", "  - Found: Section='" + foundSectionTrimmed + "', Grade=" + foundGradeNum);
                        } else {
                            Log.d("StudentNumberManager", "✅ Section match verified!");
                        }
                        
                        loadExistingStudentNumbers();
                    } else {
                        String errorMsg = "Section not found: " + currentSectionName + " (Grade " + gradeNumber + ")";
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        Log.e("StudentNumberManager", "❌ " + errorMsg);
                        Log.d("StudentNumberManager", "Available sections in database:");
                        // Log available sections for debugging
                        db.collection("Sections")
                            .limit(20)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                Log.d("StudentNumberManager", "Found " + querySnapshot.size() + " total sections:");
                                for (QueryDocumentSnapshot doc : querySnapshot) {
                                    String sectionName = doc.getString("Section");
                                    Long gradeNum = doc.getLong("Grade_Number");
                                    String docId = doc.getId();
                                    Log.d("StudentNumberManager", "  - ID: " + docId + ", Section: '" + sectionName + 
                                          "', Grade: " + gradeNum);
                                }
                            });
                    }
                } else {
                    String errorMsg = "Error finding section: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error");
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("StudentNumberManager", "❌ " + errorMsg, task.getException());
                    if (task.getException() != null) {
                        task.getException().printStackTrace();
                    }
                }
            });
    }
    
    private void addStudentNumber() {
        String studentNumber = studentNumberInput.getText().toString().trim();
        
        if (TextUtils.isEmpty(studentNumber)) {
            Toast.makeText(this, "Please enter a student number", Toast.LENGTH_SHORT).show();
            studentNumberInput.requestFocus();
            return;
        }
        
        if (studentNumbers.contains(studentNumber)) {
            Toast.makeText(this, "Student number already added", Toast.LENGTH_SHORT).show();
            studentNumberInput.requestFocus();
            return;
        }
        
        studentNumbers.add(studentNumber);
        studentNumberInput.setText("");
        updateStudentNumbersList();
        Toast.makeText(this, "Student number added: " + studentNumber, Toast.LENGTH_SHORT).show();
    }
    
    private void updateStudentNumbersList() {
        studentNumbersList.removeAllViews();
        
        // Update student count
        if (txtStudentCount != null) {
            txtStudentCount.setText("(" + studentNumbers.size() + ")");
        }
        
        // Show/hide empty state
        if (txtEmptyState != null) {
            txtEmptyState.setVisibility(studentNumbers.isEmpty() ? View.VISIBLE : View.GONE);
        }
        
        // Add student number items
        for (int i = 0; i < studentNumbers.size(); i++) {
            String number = studentNumbers.get(i);
            View itemView = getLayoutInflater().inflate(R.layout.item_student_number, 
                studentNumbersList, false);
            
            TextView numberText = itemView.findViewById(R.id.textStudentNumber);
            Button removeButton = itemView.findViewById(R.id.btnRemoveStudentNumber);
            
            numberText.setText("Student #" + number);
            final int index = i;
            removeButton.setOnClickListener(v -> {
                studentNumbers.remove(index);
                updateStudentNumbersList();
            });
            
            studentNumbersList.addView(itemView);
        }
    }
    
    /**
     * Verifies that the section ID from intent is valid and matches the expected section/grade,
     * then loads student numbers.
     */
    private void verifySectionAndLoad() {
        if (currentSectionId == null) {
            Log.e("StudentNumberManager", "Cannot verify: currentSectionId is null");
            findSectionDocument(); // Fall back to searching
            return;
        }
        
        Log.d("StudentNumberManager", "🔍 Verifying section ID: " + currentSectionId);
        
        db.collection("Sections")
            .document(currentSectionId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String foundSectionName = documentSnapshot.getString("Section");
                    Long foundGradeNum = documentSnapshot.getLong("Grade_Number");
                    
                    String foundSectionTrimmed = foundSectionName != null ? foundSectionName.trim() : "";
                    int foundGrade = foundGradeNum != null ? foundGradeNum.intValue() : -1;
                    int expectedGrade = Integer.parseInt(currentGrade);
                    
                    Log.d("StudentNumberManager", "✅ Section document found!");
                    Log.d("StudentNumberManager", "  - Section ID: " + currentSectionId);
                    Log.d("StudentNumberManager", "  - Section Name (from DB): '" + foundSectionTrimmed + "'");
                    Log.d("StudentNumberManager", "  - Section Name (expected): '" + currentSectionName + "'");
                    Log.d("StudentNumberManager", "  - Grade (from DB): " + foundGrade);
                    Log.d("StudentNumberManager", "  - Grade (expected): " + expectedGrade);
                    
                    if (foundSectionTrimmed.equals(currentSectionName) && foundGrade == expectedGrade) {
                        Log.d("StudentNumberManager", "✅ Section verification successful! Loading student numbers...");
                        loadExistingStudentNumbers();
                    } else {
                        Log.w("StudentNumberManager", "⚠️ Section ID doesn't match expected section/grade!");
                        Log.w("StudentNumberManager", "  - Expected: Section='" + currentSectionName + "', Grade=" + expectedGrade);
                        Log.w("StudentNumberManager", "  - Found: Section='" + foundSectionTrimmed + "', Grade=" + foundGrade);
                        Log.w("StudentNumberManager", "  - Falling back to section search...");
                        findSectionDocument(); // Fall back to searching
                    }
                } else {
                    Log.w("StudentNumberManager", "⚠️ Section document not found with ID: " + currentSectionId);
                    Log.w("StudentNumberManager", "  - Falling back to section search...");
                    findSectionDocument(); // Fall back to searching
                }
            })
            .addOnFailureListener(e -> {
                Log.e("StudentNumberManager", "❌ Error verifying section: " + e.getMessage());
                Log.e("StudentNumberManager", "  - Falling back to section search...");
                findSectionDocument(); // Fall back to searching
            });
    }
    
    private void loadExistingStudentNumbers() {
        if (currentSectionId == null) {
            Log.w("StudentNumberManager", "❌ Cannot load student numbers: currentSectionId is null");
            Log.w("StudentNumberManager", "Section Name: " + currentSectionName + ", Grade: " + currentGrade);
            Toast.makeText(this, "Section ID not found. Cannot load student numbers.", Toast.LENGTH_LONG).show();
            return;
        }
        
        Log.d("StudentNumberManager", "📥 Loading student numbers...");
        Log.d("StudentNumberManager", "  - Section ID: " + currentSectionId);
        Log.d("StudentNumberManager", "  - Section Name: '" + currentSectionName + "'");
        Log.d("StudentNumberManager", "  - Grade: " + currentGrade);
        Log.d("StudentNumberManager", "  - Query path: Sections/" + currentSectionId + "/StudentNumbers");
        Log.d("StudentNumberManager", "  - ⚠️ IMPORTANT: Compare this Section ID with the one used during save!");
        Log.d("StudentNumberManager", "  - 🔍 If this ID differs from save ID, data won't be found!");
        
        db.collection("Sections")
            .document(currentSectionId)
            .collection("StudentNumbers")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    studentNumbers.clear();
                    int count = 0;
                    int totalDocs = task.getResult().size();
                    Log.d("StudentNumberManager", "📊 Query returned " + totalDocs + " documents");
                    
                    if (totalDocs == 0) {
                        Log.w("StudentNumberManager", "⚠️ No student numbers found in collection!");
                        Log.w("StudentNumberManager", "  - Collection path: Sections/" + currentSectionId + "/StudentNumbers");
                        Log.w("StudentNumberManager", "  - This might mean:");
                        Log.w("StudentNumberManager", "    1. No student numbers have been saved yet");
                        Log.w("StudentNumberManager", "    2. Data was saved to a different section ID");
                        Log.w("StudentNumberManager", "    3. Collection doesn't exist yet");
                    }
                    
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        String number = doc.getString("studentNumber");
                        String docId = doc.getId();
                        Log.d("StudentNumberManager", "  📄 Document ID: " + docId + ", studentNumber: " + number);
                        
                        if (number != null && !number.trim().isEmpty()) {
                            studentNumbers.add(number.trim());
                            count++;
                            Log.d("StudentNumberManager", "    ✅ Added: " + number.trim());
                        } else {
                            Log.w("StudentNumberManager", "    ⚠️ Document " + docId + " has null or empty studentNumber field");
                            // Log all fields in the document for debugging
                            Log.d("StudentNumberManager", "    Document data: " + doc.getData());
                        }
                    }
                    Log.d("StudentNumberManager", "✅ Successfully loaded " + count + " student numbers (out of " + totalDocs + " documents)");
                    updateStudentNumbersList();
                } else {
                    Log.e("StudentNumberManager", "❌ Error loading student numbers", task.getException());
                    Log.e("StudentNumberManager", "  - Section ID: " + currentSectionId);
                    Log.e("StudentNumberManager", "  - Error: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    if (task.getException() != null) {
                        task.getException().printStackTrace();
                    }
                    Toast.makeText(this, "Error loading student numbers: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void saveStudentNumbers() {
        if (currentSectionId == null) {
            Toast.makeText(this, "Section not found. Please try again.", Toast.LENGTH_LONG).show();
            Log.e("StudentNumberManager", "Cannot save: currentSectionId is null");
            return;
        }
        
        if (studentNumbers.isEmpty()) {
            Toast.makeText(this, "Please add at least one student number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Disable save button to prevent double-clicks
        saveButton.setEnabled(false);
        saveButton.setText("💾 Saving...");
        
        Log.d("StudentNumberManager", "Saving " + studentNumbers.size() + " student numbers to section: " + currentSectionId);
        
        // Get all existing documents to delete
        db.collection("Sections")
            .document(currentSectionId)
            .collection("StudentNumbers")
            .get()
            .addOnCompleteListener(deleteTask -> {
                if (deleteTask.isSuccessful()) {
                    List<com.google.firebase.firestore.DocumentReference> docsToDelete = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : deleteTask.getResult()) {
                        docsToDelete.add(doc.getReference());
                    }
                    
                    Log.d("StudentNumberManager", "Found " + docsToDelete.size() + " existing student numbers to delete");
                    
                    // Delete all existing numbers in a batch
                    if (!docsToDelete.isEmpty()) {
                        com.google.firebase.firestore.WriteBatch batch = db.batch();
                        for (com.google.firebase.firestore.DocumentReference docRef : docsToDelete) {
                            batch.delete(docRef);
                        }
                        batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d("StudentNumberManager", "Successfully deleted " + docsToDelete.size() + " existing student numbers");
                                addNewStudentNumbers();
                            })
                            .addOnFailureListener(e -> {
                                saveButton.setEnabled(true);
                                saveButton.setText("💾 Save Student Numbers");
                                Log.e("StudentNumberManager", "Error deleting existing student numbers", e);
                                Toast.makeText(this, "Error deleting existing numbers: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                    } else {
                        // No existing numbers to delete, just add new ones
                        addNewStudentNumbers();
                    }
                } else {
                    saveButton.setEnabled(true);
                    saveButton.setText("💾 Save Student Numbers");
                    Log.e("StudentNumberManager", "Error fetching existing student numbers", deleteTask.getException());
                    Toast.makeText(this, "Error saving: " + (deleteTask.getException() != null ? 
                        deleteTask.getException().getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                }
            });
    }
    
    private void addNewStudentNumbers() {
        int totalNumbers = studentNumbers.size();
        
        if (totalNumbers == 0) {
            Log.w("StudentNumberManager", "No student numbers to save!");
            Toast.makeText(this, "No student numbers to save", Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(true);
            saveButton.setText("💾 Save Student Numbers");
            return;
        }
        
        Log.d("StudentNumberManager", "Adding " + totalNumbers + " new student numbers to section: " + currentSectionId);
        Log.d("StudentNumberManager", "Student numbers to save: " + studentNumbers.toString());
        
        // Use batch write for better reliability
        com.google.firebase.firestore.WriteBatch batch = db.batch();
        
        for (String number : studentNumbers) {
            Map<String, Object> studentNumberData = new HashMap<>();
            studentNumberData.put("studentNumber", number);
            studentNumberData.put("section", currentSectionName);
            studentNumberData.put("grade", currentGrade);
            studentNumberData.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
            
            com.google.firebase.firestore.DocumentReference newDocRef = db.collection("Sections")
                .document(currentSectionId)
                .collection("StudentNumbers")
                .document(); // Use auto-generated ID
            
            Log.d("StudentNumberManager", "Adding to batch: studentNumber=" + number + ", docRef=" + newDocRef.getId());
            batch.set(newDocRef, studentNumberData);
        }
        
        Log.d("StudentNumberManager", "Committing batch write with " + totalNumbers + " documents");
        
        // Commit all writes in one batch
        batch.commit()
            .addOnSuccessListener(aVoid -> {
                Log.d("StudentNumberManager", "✅ Batch commit SUCCESS! Saved all " + totalNumbers + " student numbers");
                Log.d("StudentNumberManager", "📝 Save Details:");
                Log.d("StudentNumberManager", "  - Section ID: " + currentSectionId);
                Log.d("StudentNumberManager", "  - ⚠️⚠️⚠️ SAVE THIS SECTION ID: " + currentSectionId + " ⚠️⚠️⚠️");
                Log.d("StudentNumberManager", "  - Section Name: '" + currentSectionName + "'");
                Log.d("StudentNumberManager", "  - Grade: " + currentGrade);
                Log.d("StudentNumberManager", "  - Save Path: Sections/" + currentSectionId + "/StudentNumbers");
                Log.d("StudentNumberManager", "  - Student Numbers Saved: " + studentNumbers.toString());
                Log.d("StudentNumberManager", "  - 🔍 When loading, compare the Section ID used!");
                
                // Verify the save with retry mechanism (Firestore might need a moment to index)
                verifySaveWithRetry(currentSectionId, totalNumbers, 0);
            })
            .addOnFailureListener(e -> {
                saveButton.setEnabled(true);
                saveButton.setText("💾 Save Student Numbers");
                Log.e("StudentNumberManager", "❌ Batch commit FAILED! Error saving student numbers", e);
                Log.e("StudentNumberManager", "Error details: " + e.getClass().getName() + ": " + e.getMessage());
                if (e.getCause() != null) {
                    Log.e("StudentNumberManager", "Cause: " + e.getCause().getMessage());
                }
                Toast.makeText(this, "Error saving student numbers: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }
    
    /**
     * Verifies the save operation with retry mechanism.
     * Firestore might need a moment to index new documents, so we retry a few times.
     */
    private void verifySaveWithRetry(String sectionId, int expectedCount, int retryCount) {
        final int MAX_RETRIES = 3;
        final long RETRY_DELAY_MS = 1000; // 1 second
        
        Log.d("StudentNumberManager", "🔍 Verifying save (attempt " + (retryCount + 1) + "/" + (MAX_RETRIES + 1) + ")...");
        Log.d("StudentNumberManager", "  - Section ID: " + sectionId);
        Log.d("StudentNumberManager", "  - Expected count: " + expectedCount);
        Log.d("StudentNumberManager", "  - Query path: Sections/" + sectionId + "/StudentNumbers");
        
        db.collection("Sections")
            .document(sectionId)
            .collection("StudentNumbers")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int verifiedCount = querySnapshot.size();
                Log.d("StudentNumberManager", "📊 Verification query returned " + verifiedCount + " documents");
                
                if (verifiedCount >= expectedCount) {
                    // Success! Found all or more documents
                    Log.d("StudentNumberManager", "✅ Verification SUCCESS! Found " + verifiedCount + " student numbers (expected " + expectedCount + ")");
                    Log.d("StudentNumberManager", "Verified student numbers:");
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String savedNumber = doc.getString("studentNumber");
                        String docId = doc.getId();
                        Log.d("StudentNumberManager", "  - Doc ID: " + docId + ", studentNumber: " + savedNumber);
                    }
                    Toast.makeText(this, "✅ " + verifiedCount + " student number(s) saved and verified!", Toast.LENGTH_SHORT).show();
                    
                    // Reload the list to show the saved numbers
                    loadExistingStudentNumbers();
                    
                    // Re-enable save button
                    saveButton.setEnabled(true);
                    saveButton.setText("💾 Save Student Numbers");
                } else if (retryCount < MAX_RETRIES) {
                    // Not enough documents found, retry after delay
                    Log.w("StudentNumberManager", "⚠️ Verification found " + verifiedCount + " documents (expected " + expectedCount + "), retrying...");
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        verifySaveWithRetry(sectionId, expectedCount, retryCount + 1);
                    }, RETRY_DELAY_MS);
                } else {
                    // Max retries reached, still not found
                    Log.e("StudentNumberManager", "❌ Verification FAILED after " + (MAX_RETRIES + 1) + " attempts!");
                    Log.e("StudentNumberManager", "  - Found: " + verifiedCount + " documents");
                    Log.e("StudentNumberManager", "  - Expected: " + expectedCount + " documents");
                    Log.e("StudentNumberManager", "  - Section ID used: " + sectionId);
                    Log.e("StudentNumberManager", "  - This might indicate:");
                    Log.e("StudentNumberManager", "    1. Data was saved to a different section ID");
                    Log.e("StudentNumberManager", "    2. Firestore indexing delay (unlikely after " + (MAX_RETRIES + 1) + " attempts)");
                    Log.e("StudentNumberManager", "    3. Permissions issue preventing read");
                    
                    // Still reload to show what we found
                    loadExistingStudentNumbers();
                    
                    Toast.makeText(this, "⚠️ Saved but verification incomplete. Found " + verifiedCount + " of " + expectedCount + " numbers.", Toast.LENGTH_LONG).show();
                    saveButton.setEnabled(true);
                    saveButton.setText("💾 Save Student Numbers");
                }
            })
            .addOnFailureListener(e -> {
                Log.e("StudentNumberManager", "❌ Verification query failed", e);
                if (retryCount < MAX_RETRIES) {
                    Log.d("StudentNumberManager", "Retrying verification after error...");
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        verifySaveWithRetry(sectionId, expectedCount, retryCount + 1);
                    }, RETRY_DELAY_MS);
                } else {
                    Toast.makeText(this, "Saved but verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    saveButton.setEnabled(true);
                    saveButton.setText("💾 Save Student Numbers");
                }
            });
    }
}

