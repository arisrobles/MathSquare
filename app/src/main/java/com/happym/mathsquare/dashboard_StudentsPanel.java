package com.happym.mathsquare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.happym.mathsquare.Model.Student;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class dashboard_StudentsPanel extends AppCompatActivity {
    private RecyclerView studentsRecyclerView;
    private StudentAdapter studentAdapter;
    private FirebaseFirestore db;
    private ImageView deleteBtn,checkBtn;
    private LinearLayout confirmDeleteLayout;

    private List<Student> selectedStudents = new ArrayList<>();
    private List<CheckBox> rowCheckBoxes = new ArrayList<>();
    private CheckBox allCheckBox;
    private boolean isDeleteButtonSelected = false;
    private boolean isCanceled = false;
    private MediaPlayer bgMediaPlayer;
    private MediaPlayer soundEffectPlayer;

    // Search functionality variables
    private EditText searchEditText;
    private ImageView searchIcon, clearSearchIcon;
    private List<Student> allStudents = new ArrayList<>(); // Store all students for filtering
    private TextView emptyStateText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.layout_dashboard_students_panel);
        
        // Initialize RecyclerView
        studentsRecyclerView = findViewById(R.id.studentsRecyclerView);
        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentAdapter = new StudentAdapter(new ArrayList<>());
        studentsRecyclerView.setAdapter(studentAdapter);
        
        emptyStateText = findViewById(R.id.emptyStateText);

         // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        deleteBtn = findViewById(R.id.deletesection);
        checkBtn = findViewById(R.id.checkdelete);
        confirmDeleteLayout = findViewById(R.id.confirm_delete_layout);
        allCheckBox =  findViewById(R.id.rowCheckboxSelectAll);




deleteBtn.setOnClickListener(v -> {
                playSound("click.mp3");
    // Toggle delete button selection state
    isDeleteButtonSelected = !isDeleteButtonSelected;

    if (isDeleteButtonSelected) {
        // First press: show check button and checkboxes
        isCanceled = false;
        allCheckBox.setVisibility(View.VISIBLE);
        confirmDeleteLayout.setVisibility(View.VISIBLE);
        deleteBtn.setImageResource(R.drawable.ic_cancel);
        studentAdapter.setDeleteMode(true);
    } else {
        // Second press: cancel and reset
        isCanceled = true;
        allCheckBox.setVisibility(View.GONE);
        confirmDeleteLayout.setVisibility(View.GONE);
        deleteBtn.setImageResource(R.drawable.ic_delete);
        studentAdapter.setDeleteMode(false);
        selectedStudents.clear();
    }
});

checkBtn.setOnClickListener(v -> {
    isCanceled = false;
    playSound("click.mp3");
    if (selectedStudents.isEmpty()) {
        checkBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.btn_green_off));
        checkBtn.setImageResource(R.drawable.ic_check);
        Toast.makeText(this, "No data deleted.", Toast.LENGTH_SHORT).show();
    } else {
        checkBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.btn_condition_create));
        checkBtn.setImageResource(R.drawable.ic_check);

        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete the selected students?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    playSound("click.mp3");
                    deleteSelectedStudents();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    playSound("click.mp3");
                    dialog.dismiss();
                })
                .show();
    }
});




        allCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            playSound("click.mp3");
            toggleAllRows(isChecked);
        });


        if (selectedStudents.isEmpty()) {
            checkBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.btn_green_off));
            checkBtn.setImageResource(R.drawable.ic_check);
        } else {
            checkBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.btn_condition_create));
            checkBtn.setImageResource(R.drawable.ic_check);
        }





      /*  // Sample student data
        List<Student> students = new ArrayList<>();
        students.add(new Student("Cristopher", "Butter", "1", "1", "Nara"));
        students.add(new Student("James", "Smith", "2", "2", "Leo"));
        students.add(new Student("Alice", "Cooper", "3", "3", "Milo"));

        // Add rows dynamically
        addRowsToTable(students);

        */

        fetchStudents("Quiz");
        initializeSwitchListeners();
        initializeSearchFunction();


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
               playSound("click.mp3");
        String newStatus = isChecked ? "open" : "closed";
        db.collection("Quizzes").document("Status").collection(quizId)
            .document("status")
            .update("status", newStatus)
            .addOnSuccessListener(aVoid -> Log.d("Quiz", quizId + " status updated to " + newStatus))
            .addOnFailureListener(error -> Log.e("Quiz", "Failed to update status for " + quizId, error));
    });
}

// Call this in onCreate() or appropriate lifecycle method
private void initializeSwitchListeners() {
    // Quiz switches removed from student dashboard
}

   private void fetchStudents(String filter) {
    try {
        Log.d("FETCH_STUDENTS", "Starting fetch from Firestore...");
        
        // First, get the teacher's assigned grade and section
        String teacherEmail = sharedPreferences.getEmail(this);
        
        if (teacherEmail == null || teacherEmail.isEmpty()) {
            Toast.makeText(this, "Teacher email not found. Please log in again.", Toast.LENGTH_LONG).show();
            Log.e("FETCH_STUDENTS", "Teacher email is null or empty");
            return;
        }
        
        Log.d("FETCH_STUDENTS", "Fetching teacher profile for: " + teacherEmail);
        
        // Fetch teacher's assigned grade and section
        db.collection("TeacherProfiles")
            .whereEqualTo("email", teacherEmail)
            .limit(1)
            .get()
            .addOnCompleteListener(teacherTask -> {
                if (teacherTask.isSuccessful() && teacherTask.getResult() != null && !teacherTask.getResult().isEmpty()) {
                    QueryDocumentSnapshot teacherDoc = (QueryDocumentSnapshot) teacherTask.getResult().getDocuments().get(0);
                    String assignedGrade = teacherDoc.getString("assignedGrade");
                    String assignedSection = teacherDoc.getString("assignedSection");
                    
                    Log.d("FETCH_STUDENTS", "Teacher assigned grade: " + assignedGrade + ", section: " + assignedSection);
                    
                    // If teacher has assigned grade and section, filter students accordingly
                    if (assignedGrade != null && assignedSection != null && 
                        !assignedGrade.isEmpty() && !assignedSection.isEmpty()) {
                        
                        // Normalize section name (trim for consistency)
                        String normalizedSection = assignedSection.trim();
                        
                        // Fetch students filtered by teacher's assigned grade and section
                        fetchStudentsWithFilter(filter, assignedGrade, normalizedSection);
                    } else {
                        // Teacher doesn't have assigned grade/section, show all students (fallback)
                        Log.w("FETCH_STUDENTS", "Teacher has no assigned grade/section, showing all students");
                        fetchAllStudents(filter);
                    }
                } else {
                    // Teacher profile not found, show all students (fallback)
                    Log.w("FETCH_STUDENTS", "Teacher profile not found, showing all students");
                    fetchAllStudents(filter);
                }
            })
            .addOnFailureListener(e -> {
                Log.e("FETCH_STUDENTS", "Error fetching teacher profile: " + e.getMessage());
                // Fallback to showing all students
                fetchAllStudents(filter);
            });
    } catch (Exception e) {
        Log.e("FETCH_STUDENTS", "Initial fetch error: " + e.getMessage());
        Toast.makeText(this, "Error initializing fetch: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}

/**
 * Fetches students filtered by teacher's assigned grade and section
 */
private void fetchStudentsWithFilter(String filter, String assignedGrade, String assignedSection) {
    try {
        Log.d("FETCH_STUDENTS", "Fetching students for Grade: " + assignedGrade + ", Section: " + assignedSection);

        // Use only gameType and grade in Firestore query to avoid composite index requirement
        // We'll filter by section client-side
        db.collection("Accounts")
            .document("Students")
            .collection("MathSquare")
            .whereEqualTo("gameType", filter) // Filter by gameType
            .whereEqualTo("grade", assignedGrade) // Filter by teacher's assigned grade
            .get()
            .addOnCompleteListener(task -> {
                processStudentResults(task, assignedGrade, assignedSection);
            })
            .addOnFailureListener(e -> {
                Log.e("FETCH_STUDENTS", "Firestore query failed: " + e.getMessage());
                Toast.makeText(this, "Error fetching students: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    } catch (Exception e) {
        Log.e("FETCH_STUDENTS", "Error in fetchStudentsWithFilter: " + e.getMessage());
        Toast.makeText(this, "Error fetching students: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}

/**
 * Fetches all students (fallback when teacher has no assigned grade/section)
 */
private void fetchAllStudents(String filter) {
    try {
        Log.d("FETCH_STUDENTS", "Fetching all students (no grade/section filter)");

        db.collection("Accounts")
            .document("Students")
            .collection("MathSquare")
            .whereEqualTo("gameType", filter) // Filter by gameType
            .get()
            .addOnCompleteListener(task -> {
                processStudentResults(task, null, null);
            })
            .addOnFailureListener(e -> {
                Log.e("FETCH_STUDENTS", "Firestore query failed: " + e.getMessage());
                Toast.makeText(this, "Error fetching students: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    } catch (Exception e) {
        Log.e("FETCH_STUDENTS", "Error in fetchAllStudents: " + e.getMessage());
        Toast.makeText(this, "Error fetching students: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}

/**
 * Processes the student query results
 */
private void processStudentResults(com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> task, String assignedGrade, String assignedSection) {
    try {
        if (task.isSuccessful() && task.getResult() != null) {
            // Use a Map to deduplicate students by unique identifier
            // Key: firstName_lastName_section_grade
            // Value: Student object (keeping the latest attempt based on timestamp)
            Map<String, Student> uniqueStudentsMap = new HashMap<>();
            Map<String, Date> studentTimestamps = new HashMap<>();

            Log.d("FETCH_STUDENTS", "Documents fetched: " + task.getResult().size());
            if (assignedGrade != null && assignedSection != null) {
                Log.d("FETCH_STUDENTS", "Filtered by Grade: " + assignedGrade + ", Section: " + assignedSection);
            }

            for (QueryDocumentSnapshot doc : task.getResult()) {
                try {
                    String firstName = doc.getString("firstName");
                    String lastName = doc.getString("lastName");
                    String section = doc.getString("section");
                    String grade = doc.getString("grade");
                    String quizNo = doc.getString("quizno");
                    String score = doc.getString("quizscore");
                    String gameType = doc.getString("gameType");
                    Date timestamp = doc.getDate("timestamp");

                    String documentId = doc.getId();

                    // Skip if required fields are missing
                    if (firstName == null || lastName == null || section == null || grade == null) {
                        Log.w("FETCH_STUDENTS", "Skipping document with missing required fields: " + documentId);
                        continue;
                    }

                    // Additional filter: if teacher has assigned grade/section, ensure exact match
                    if (assignedGrade != null && assignedSection != null) {
                        // Normalize section names for comparison (trim whitespace)
                        String normalizedDocSection = section.trim();
                        String normalizedAssignedSection = assignedSection.trim();
                        
                        if (!grade.equals(assignedGrade) || !normalizedDocSection.equals(normalizedAssignedSection)) {
                            Log.d("FETCH_STUDENTS", "Skipping student - doesn't match assigned grade/section: " + 
                                firstName + " " + lastName + " (Grade: " + grade + ", Section: " + section + ")");
                            continue;
                        }
                    }

                    // Create unique key for student (firstName + lastName + section + grade)
                    String studentKey = (firstName + "_" + lastName + "_" + section + "_" + grade).toLowerCase().trim();

                    Log.d("FETCH_STUDENTS", "Processing doc ID: " + documentId + " | Student: " + firstName + " " + lastName + " | Key: " + studentKey);

                    // Check if we've seen this student before
                    if (!uniqueStudentsMap.containsKey(studentKey)) {
                        // First time seeing this student - add them
                        Student student = new Student(
                                firstName + " " + lastName,
                                section,
                                grade,
                                quizNo != null ? quizNo : "N/A",
                                score != null ? score : "0",
                                documentId
                        );
                        uniqueStudentsMap.put(studentKey, student);
                        studentTimestamps.put(studentKey, timestamp != null ? timestamp : new Date());
                        Log.d("FETCH_STUDENTS", "Added new student: " + studentKey);
                    } else {
                        // Student already exists - keep the one with the latest timestamp
                        Date existingTimestamp = studentTimestamps.get(studentKey);
                        if (timestamp != null && (existingTimestamp == null || timestamp.after(existingTimestamp))) {
                            // This attempt is newer - replace the student entry
                            Student student = new Student(
                                    firstName + " " + lastName,
                                    section,
                                    grade,
                                    quizNo != null ? quizNo : "N/A",
                                    score != null ? score : "0",
                                    documentId
                            );
                            uniqueStudentsMap.put(studentKey, student);
                            studentTimestamps.put(studentKey, timestamp);
                            Log.d("FETCH_STUDENTS", "Updated student with newer attempt: " + studentKey);
                        } else {
                            Log.d("FETCH_STUDENTS", "Skipping older attempt for student: " + studentKey);
                        }
                    }
                } catch (Exception e) {
                    Log.e("FETCH_STUDENTS", "Error parsing document: " + e.getMessage());
                    Toast.makeText(this, "Error processing document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            // Convert map values to list
            List<Student> students = new ArrayList<>(uniqueStudentsMap.values());
            
            // Sort students by quiz number (client-side sorting to avoid Firestore index requirement)
            students.sort((s1, s2) -> {
                try {
                    int quiz1 = Integer.parseInt(s1.getQuizNo().replaceAll("[^0-9]", ""));
                    int quiz2 = Integer.parseInt(s2.getQuizNo().replaceAll("[^0-9]", ""));
                    return Integer.compare(quiz1, quiz2);
                } catch (NumberFormatException e) {
                    // If parsing fails, maintain original order
                    return s1.getQuizNo().compareTo(s2.getQuizNo());
                }
            });

            // Store all students for search functionality
            allStudents.clear();
            allStudents.addAll(students);

            updateStudentList(students);
            Log.d("FETCH_STUDENTS", "Unique students after deduplication: " + students.size() + " (from " + task.getResult().size() + " total documents)");

        } else {
            Log.w("FETCH_STUDENTS", "Task unsuccessful or no result");
            Toast.makeText(this, "No student data found.", Toast.LENGTH_SHORT).show();
        }
    } catch (Exception e) {
        Log.e("FETCH_STUDENTS", "Error in processStudentResults: " + e.getMessage());
        Toast.makeText(this, "Error processing student data: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}



private void updateStudentList(List<Student> students) {
    try {
        studentAdapter.updateStudents(students);
        
        // Show/hide empty state
        if (students.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            studentsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            studentsRecyclerView.setVisibility(View.VISIBLE);
        }
    } catch (Exception e) {
        Toast.makeText(this, "An error occurred while updating list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        e.printStackTrace();
    }
}

    private void toggleAllRows(boolean isChecked) {
        selectedStudents.clear();
        studentAdapter.selectAll(isChecked);
        
        if (isChecked) {
            selectedStudents.addAll(allStudents);
            checkBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.btn_condition_create));
            checkBtn.setImageResource(R.drawable.ic_delete);
        } else {
            checkBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.btn_green_off));
            checkBtn.setImageResource(R.drawable.ic_check);
        }
    }


   private void deleteSelectedStudents() {
        int deleteCount = selectedStudents.size();
        int[] deletedCount = {0};
        
        for (Student student : selectedStudents) {
            db.collection("Accounts")
                .document("Students")
                .collection("MathSquare")
                .document(student.getDocId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    deletedCount[0]++;
                    // Remove from allStudents list
                    allStudents.remove(student);
                    
                    if (deletedCount[0] == deleteCount) {
                        // All deletions complete
                        updateStudentList(allStudents);
                        selectedStudents.clear();
                        isDeleteButtonSelected = false;
                        allCheckBox.setVisibility(View.GONE);
                        confirmDeleteLayout.setVisibility(View.GONE);
                        deleteBtn.setImageResource(R.drawable.ic_delete);
                        studentAdapter.setDeleteMode(false);
                        Toast.makeText(this, "Selected students deleted successfully.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting student: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        }
    }

    // Initialize search functionality
    private void initializeSearchFunction() {
        searchEditText = findViewById(R.id.search_edittext);
        searchIcon = findViewById(R.id.search_icon);
        clearSearchIcon = findViewById(R.id.clear_search_icon);

        // Set up search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchQuery = s.toString().trim();

                // Show/hide clear button based on text presence
                if (searchQuery.isEmpty()) {
                    clearSearchIcon.setVisibility(View.GONE);
                } else {
                    clearSearchIcon.setVisibility(View.VISIBLE);
                }

                // Filter the table based on search query
                filterTable(searchQuery);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        // Clear search functionality
        clearSearchIcon.setOnClickListener(v -> {
            playSound("click.mp3");
            searchEditText.setText("");
            clearSearchIcon.setVisibility(View.GONE);
            filterTable(""); // Show all rows
        });

        // Search icon click to focus on EditText
        searchIcon.setOnClickListener(v -> {
            playSound("click.mp3");
            searchEditText.requestFocus();
        });
    }

    // Filter table based on search query
    private void filterTable(String query) {
        try {
            // If query is empty, show all students
            if (query.isEmpty()) {
                updateStudentList(allStudents);
                return;
            }

            // Filter students based on search query
            List<Student> filteredStudents = new ArrayList<>();
            String lowerCaseQuery = query.toLowerCase();

            for (Student student : allStudents) {
                // Search in name, section, grade, quiz number, and score
                if (student.getName().toLowerCase().contains(lowerCaseQuery) ||
                    student.getSection().toLowerCase().contains(lowerCaseQuery) ||
                    student.getGrade().toLowerCase().contains(lowerCaseQuery) ||
                    student.getQuizNo().toLowerCase().contains(lowerCaseQuery) ||
                    student.getScore().toLowerCase().contains(lowerCaseQuery)) {

                    filteredStudents.add(student);
                }
            }

            // Update RecyclerView with filtered results
            updateStudentList(filteredStudents);

            // Log search results
            Log.d("SEARCH", "Query: '" + query + "' | Results: " + filteredStudents.size() + "/" + allStudents.size());

        } catch (Exception e) {
            Log.e("SEARCH", "Error filtering table: " + e.getMessage());
            Toast.makeText(this, "Error during search: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Shows student quiz history in a popup dialog
     */
    private void showStudentHistoryDialog(String firstName, String lastName, String studentName) {
        // Create dialog view
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_student_history, null);
        
        TextView studentNameText = dialogView.findViewById(R.id.dialogStudentName);
        androidx.recyclerview.widget.RecyclerView recyclerView = dialogView.findViewById(R.id.dialogRecyclerView);
        TextView emptyStateText = dialogView.findViewById(R.id.dialogEmptyState);
        
        studentNameText.setText(studentName + "'s Quiz History");
        
        // Setup RecyclerView
        List<StudentHistoryItem> historyItems = new ArrayList<>();
        StudentHistoryAdapter adapter = new StudentHistoryAdapter(historyItems);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        // Create and show dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .create();
        
        dialog.show();
        
        // Load student history
        db.collection("Accounts")
            .document("Students")
            .collection("MathSquare")
            .whereEqualTo("firstName", firstName)
            .whereEqualTo("lastName", lastName)
            .whereEqualTo("gameType", "Quiz")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    historyItems.clear();
                    
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        String gameType = doc.getString("gameType");
                        String score = doc.getString("quizscore");
                        String quizNo = doc.getString("quizno");
                        Date timestamp = doc.getDate("timestamp");
                        
                        // Get attempt number
                        Long attemptNumLong = doc.getLong("attemptNumber");
                        int attemptNumber = attemptNumLong != null ? attemptNumLong.intValue() : 1;
                        
                        if (timestamp != null && score != null) {
                            String dateStr = dateFormat.format(timestamp);
                            String timeStr = timeFormat.format(timestamp);
                            String attemptStr = formatAttemptNumber(attemptNumber);
                            
                            historyItems.add(new StudentHistoryItem(
                                gameType != null ? gameType : "Unknown",
                                quizNo != null ? quizNo : "N/A",
                                score,
                                dateStr,
                                timeStr,
                                attemptStr
                            ));
                        }
                    }
                    
                    adapter.notifyDataSetChanged();
                    
                    if (historyItems.isEmpty()) {
                        emptyStateText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyStateText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(this, "Error loading history: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private String formatAttemptNumber(int attemptNumber) {
        if (attemptNumber % 100 >= 11 && attemptNumber % 100 <= 13) {
            return attemptNumber + "th Attempt";
        }
        switch (attemptNumber % 10) {
            case 1: return attemptNumber + "st Attempt";
            case 2: return attemptNumber + "nd Attempt";
            case 3: return attemptNumber + "rd Attempt";
            default: return attemptNumber + "th Attempt";
        }
    }
    
    // Inner class for history items
    private static class StudentHistoryItem {
        private String gameType;
        private String quizNo;
        private String score;
        private String date;
        private String time;
        private String attemptNumber;
        
        public StudentHistoryItem(String gameType, String quizNo, String score, 
                                String date, String time, String attemptNumber) {
            this.gameType = gameType;
            this.quizNo = quizNo;
            this.score = score;
            this.date = date;
            this.time = time;
            this.attemptNumber = attemptNumber;
        }
        
        public String getGameType() { return gameType; }
        public String getQuizNo() { return quizNo; }
        public String getScore() { return score; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getAttemptNumber() { return attemptNumber; }
    }
    
    // Adapter for dialog RecyclerView
    private static class StudentHistoryAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<StudentHistoryAdapter.ViewHolder> {
        private List<StudentHistoryItem> items;
        
        public StudentHistoryAdapter(List<StudentHistoryItem> items) {
            this.items = items;
        }
        
        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_score_history, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            StudentHistoryItem item = items.get(position);
            holder.gameTypeText.setText(item.getGameType());
            holder.quizNoText.setText(item.getQuizNo() + " - " + item.getAttemptNumber());
            holder.scoreText.setText("Score: " + item.getScore());
            holder.dateText.setText("Date: " + item.getDate());
            holder.timeText.setText("Time: " + item.getTime());
        }
        
        @Override
        public int getItemCount() {
            return items.size();
        }
        
        static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            TextView gameTypeText, quizNoText, scoreText, dateText, timeText;
            
            ViewHolder(View itemView) {
                super(itemView);
                gameTypeText = itemView.findViewById(R.id.textGameType);
                quizNoText = itemView.findViewById(R.id.textQuizNo);
                scoreText = itemView.findViewById(R.id.textScore);
                dateText = itemView.findViewById(R.id.textDate);
                timeText = itemView.findViewById(R.id.textTime);
            }
        }
    }
    
    // Adapter for Students RecyclerView
    private class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
        private List<Student> students;
        private boolean deleteMode = false;
        
        public StudentAdapter(List<Student> students) {
            this.students = new ArrayList<>(students);
        }
        
        public void updateStudents(List<Student> newStudents) {
            this.students = new ArrayList<>(newStudents);
            notifyDataSetChanged();
        }
        
        public void setDeleteMode(boolean enabled) {
            deleteMode = enabled;
            notifyDataSetChanged();
        }
        
        public void selectAll(boolean select) {
            for (int i = 0; i < students.size(); i++) {
                notifyItemChanged(i);
            }
        }
        
        @Override
        public StudentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_card, parent, false);
            return new StudentViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(StudentViewHolder holder, int position) {
            Student student = students.get(position);
            
            holder.nameText.setText(student.getName());
            holder.sectionText.setText(student.getSection());
            holder.gradeText.setText(student.getGrade());
            holder.quizNoText.setText(student.getQuizNo());
            holder.scoreText.setText(student.getScore());
            
            // Show/hide checkbox based on delete mode
            holder.checkbox.setVisibility(deleteMode ? View.VISIBLE : View.GONE);
            holder.checkbox.setChecked(selectedStudents.contains(student));
            
            // Checkbox listener
            holder.checkbox.setOnCheckedChangeListener(null); // Remove previous listener
            holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                playSound("click.mp3");
                if (isChecked) {
                    if (!selectedStudents.contains(student)) {
                        selectedStudents.add(student);
                    }
                } else {
                    selectedStudents.remove(student);
                    allCheckBox.setOnCheckedChangeListener(null);
                    allCheckBox.setChecked(false);
                    allCheckBox.setOnCheckedChangeListener((buttonView1, isChecked1) -> {
                        toggleAllRows(isChecked1);
                    });
                }
                
                // Update check button state
                if (selectedStudents.isEmpty()) {
                    checkBtn.setImageDrawable(ContextCompat.getDrawable(dashboard_StudentsPanel.this, R.drawable.btn_green_off));
                    checkBtn.setImageResource(R.drawable.ic_check);
                } else {
                    checkBtn.setImageDrawable(ContextCompat.getDrawable(dashboard_StudentsPanel.this, R.drawable.btn_condition_create));
                    checkBtn.setImageResource(R.drawable.ic_delete);
                }
            });
            
            // History button listener
            holder.historyButton.setOnClickListener(v -> {
                playSound("click.mp3");
                String[] nameParts = student.getName().split(" ", 2);
                String firstName = nameParts.length > 0 ? nameParts[0] : "";
                String lastName = nameParts.length > 1 ? nameParts[1] : "";
                showStudentHistoryDialog(firstName, lastName, student.getName());
            });
        }
        
        @Override
        public int getItemCount() {
            return students.size();
        }
        
        class StudentViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkbox;
            TextView nameText, sectionText, gradeText, quizNoText, scoreText;
            ImageView historyButton;
            
            StudentViewHolder(View itemView) {
                super(itemView);
                checkbox = itemView.findViewById(R.id.rowCheckbox);
                nameText = itemView.findViewById(R.id.nameText);
                sectionText = itemView.findViewById(R.id.sectionText);
                gradeText = itemView.findViewById(R.id.gradeText);
                quizNoText = itemView.findViewById(R.id.quizNoText);
                scoreText = itemView.findViewById(R.id.scoreText);
                historyButton = itemView.findViewById(R.id.btnHistory);
            }
        }
    }

}


