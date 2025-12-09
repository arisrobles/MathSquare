package com.happym.mathsquare;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.happym.mathsquare.sharedPreferences;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Quiz Manager - Full CRUD operations for managing quizzes
 * Allows teachers to view, edit, and delete quizzes filtered by grade and section
 */
public class QuizManagerActivity extends AppCompatActivity {
    
    private FirebaseFirestore db;
    private Spinner gradeFilterSpinner, sectionFilterSpinner;
    private LinearLayout quizzesListLayout;
    private TextView txtEmptyState, txtQuizzesTitle;
    private Button btnClearFilters, btnCustomQuizzes, btnCSVQuizzes;
    private List<com.happym.mathsquare.Model.Sections> sectionsList;
    private List<Map<String, Object>> allQuizzes;
    private List<MathProblem> allCSVQuizzes;
    private String selectedGradeFilter = "All";
    private String selectedSectionFilter = "All";
    private boolean isCSVMode = false;
    private CSVProcessor csvProcessor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_quiz_manager);
        
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        
        sectionsList = new ArrayList<>();
        allQuizzes = new ArrayList<>();
        
        // Initialize views
        gradeFilterSpinner = findViewById(R.id.spinnerGradeFilter);
        sectionFilterSpinner = findViewById(R.id.spinnerSectionFilter);
        quizzesListLayout = findViewById(R.id.quizzesListLayout);
        txtEmptyState = findViewById(R.id.txtEmptyState);
        txtQuizzesTitle = findViewById(R.id.txtQuizzesTitle);
        btnClearFilters = findViewById(R.id.btnClearFilters);
        btnCustomQuizzes = findViewById(R.id.btnCustomQuizzes);
        btnCSVQuizzes = findViewById(R.id.btnCSVQuizzes);
        
        csvProcessor = new CSVProcessor();
        allCSVQuizzes = new ArrayList<>();
        
        // Setup spinners
        setupGradeFilterSpinner();
        loadSections();
        
        // Setup filter listeners
        gradeFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGradeFilter = parent.getItemAtPosition(position).toString();
                filterAndDisplayQuizzes();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        sectionFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSectionFilter = parent.getItemAtPosition(position).toString();
                filterAndDisplayQuizzes();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        btnClearFilters.setOnClickListener(v -> {
            gradeFilterSpinner.setSelection(0);
            sectionFilterSpinner.setSelection(0);
            selectedGradeFilter = "All";
            selectedSectionFilter = "All";
            filterAndDisplayQuizzes();
        });
        
        // Toggle between Teacher Quizzes and CSV quizzes
        btnCustomQuizzes.setOnClickListener(v -> {
            isCSVMode = false;
            updateToggleButtons();
            txtQuizzesTitle.setText("Teacher Quizzes");
            loadAllQuizzes();
        });
        
        btnCSVQuizzes.setOnClickListener(v -> {
            isCSVMode = true;
            updateToggleButtons();
            txtQuizzesTitle.setText("CSV Quizzes (Read-Only)");
            loadAllCSVQuizzes();
        });
        
        // Load all quizzes (default: Custom)
        loadAllQuizzes();
    }
    
    private void setupGradeFilterSpinner() {
        String[] grades = {"All", "1", "2", "3", "4", "5", "6"};
        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, grades);
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gradeFilterSpinner.setAdapter(gradeAdapter);
    }
    
    private void loadSections() {
        db.collection("Sections")
            .orderBy("Grade_Number", Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    sectionsList.clear();
                    List<String> sectionDisplayList = new ArrayList<>();
                    sectionDisplayList.add("All Sections");
                    
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Long gradeNumLong = doc.getLong("Grade_Number");
                        String grade = gradeNumLong != null ? String.valueOf(gradeNumLong.intValue()) : null;
                        String section = doc.getString("Section");
                        String docId = doc.getId();
                        
                        if (grade != null && section != null) {
                            sectionsList.add(new com.happym.mathsquare.Model.Sections(section, grade, docId));
                            sectionDisplayList.add("Grade " + grade + " - " + section);
                        }
                    }
                    
                    ArrayAdapter<String> sectionAdapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, sectionDisplayList);
                    sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sectionFilterSpinner.setAdapter(sectionAdapter);
                }
            });
    }
    
    private void loadAllQuizzes() {
        quizzesListLayout.removeAllViews();
        txtEmptyState.setVisibility(View.GONE);
        
        // Load TeacherQuizzes (complete quizzes with scheduling)
        db.collection("TeacherQuizzes")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    allQuizzes.clear();
                    
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Map<String, Object> quizData = new HashMap<>(doc.getData());
                        quizData.put("documentId", doc.getId()); // Store document ID for editing/deleting
                        allQuizzes.add(quizData);
                    }
                    
                    Log.d("QuizManager", "Loaded " + allQuizzes.size() + " teacher quizzes");
                    filterAndDisplayQuizzes();
                } else {
                    Toast.makeText(this, "Error loading quizzes: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                        Toast.LENGTH_LONG).show();
                }
            });
    }
    
    private void filterAndDisplayQuizzes() {
        quizzesListLayout.removeAllViews();
        
        if (isCSVMode) {
            filterAndDisplayCSVQuizzes();
        } else {
            filterAndDisplayCustomQuizzes();
        }
    }
    
    private void filterAndDisplayCustomQuizzes() {
        List<Map<String, Object>> filteredQuizzes = new ArrayList<>();
        
        for (Map<String, Object> quiz : allQuizzes) {
            String quizGrade = quiz.get("grade") != null ? quiz.get("grade").toString() : null;
            String quizSection = quiz.get("section") != null ? quiz.get("section").toString() : null;
            
            // Apply grade filter
            boolean gradeMatches = selectedGradeFilter.equals("All") || 
                (quizGrade != null && quizGrade.equals(selectedGradeFilter));
            
            // Apply section filter
            boolean sectionMatches = selectedSectionFilter.equals("All Sections");
            if (!sectionMatches && quizSection != null) {
                // Extract section name from filter (e.g., "Grade 4 - Hays" -> "Hays")
                String filterSectionName = extractSectionName(selectedSectionFilter);
                sectionMatches = quizSection.equals(filterSectionName);
            }
            
            if (gradeMatches && sectionMatches) {
                filteredQuizzes.add(quiz);
            }
        }
        
        if (filteredQuizzes.isEmpty()) {
            txtEmptyState.setVisibility(View.VISIBLE);
            txtEmptyState.setText("No quizzes found for the selected filters.");
        } else {
            txtEmptyState.setVisibility(View.GONE);
            for (Map<String, Object> quiz : filteredQuizzes) {
                addQuizToUI(quiz);
            }
        }
    }
    
    private void filterAndDisplayCSVQuizzes() {
        List<MathProblem> filteredQuizzes = new ArrayList<>();
        
        for (MathProblem problem : allCSVQuizzes) {
            String operation = problem.getOperation();
            String difficulty = problem.getDifficulty();
            // Apply grade filter by mapping grade -> difficulty (CSV has no section)
            if (!gradeMatchesCSV(difficulty)) {
                continue;
            }
            // Section filter has no meaning for CSV; always pass
            filteredQuizzes.add(problem);
        }
        
        if (filteredQuizzes.isEmpty()) {
            txtEmptyState.setVisibility(View.VISIBLE);
            txtEmptyState.setText("No CSV quizzes found for the selected filters.");
        } else {
            txtEmptyState.setVisibility(View.GONE);
            
            // Show summary
            TextView summaryText = new TextView(this);
            summaryText.setText("Total CSV Questions: " + filteredQuizzes.size() + 
                " (Filtered from " + allCSVQuizzes.size() + " total)");
            summaryText.setTextSize(14);
            summaryText.setTypeface(summaryText.getTypeface(), android.graphics.Typeface.BOLD);
            summaryText.setTextColor(0xFF5DB575);
            summaryText.setPadding(16, 16, 16, 8);
            summaryText.setBackgroundColor(0xFFE8F5E9);
            quizzesListLayout.addView(summaryText);
            
            // Group by operation and difficulty for better organization
            Map<String, List<MathProblem>> grouped = new HashMap<>();
            for (MathProblem problem : filteredQuizzes) {
                String key = problem.getOperation() + " - " + problem.getDifficulty();
                if (!grouped.containsKey(key)) {
                    grouped.put(key, new ArrayList<>());
                }
                grouped.get(key).add(problem);
            }
            
            // Display ALL grouped quizzes (no preview limit)
            for (Map.Entry<String, List<MathProblem>> entry : grouped.entrySet()) {
                addCSVQuizGroupToUI(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Map grade filter to CSV difficulty: 1-2 -> Easy, 3-4 -> Medium, 5-6 -> Hard.
     * If grade filter is "All", allow all.
     */
    private boolean gradeMatchesCSV(String difficulty) {
        if (selectedGradeFilter == null || "All".equalsIgnoreCase(selectedGradeFilter)) {
            return true;
        }
        String targetDifficulty;
        switch (selectedGradeFilter) {
            case "1":
            case "2":
                targetDifficulty = "Easy";
                break;
            case "3":
            case "4":
                targetDifficulty = "Medium";
                break;
            case "5":
            case "6":
                targetDifficulty = "Hard";
                break;
            default:
                targetDifficulty = null;
        }
        if (targetDifficulty == null) return true;
        return targetDifficulty.equalsIgnoreCase(difficulty);
    }
    
    private String extractSectionName(String filterDisplay) {
        if (filterDisplay.contains(" - ")) {
            return filterDisplay.split(" - ")[1];
        }
        return filterDisplay;
    }
    
    private void addQuizToUI(Map<String, Object> quiz) {
        View quizView = getLayoutInflater().inflate(R.layout.item_quiz_manager, 
            quizzesListLayout, false);
        
        TextView questionText = quizView.findViewById(R.id.textQuestion);
        TextView detailsText = quizView.findViewById(R.id.textDetails);
        Button viewButton = quizView.findViewById(R.id.btnViewQuiz);
        Button editButton = quizView.findViewById(R.id.btnEditQuiz);
        Button deleteButton = quizView.findViewById(R.id.btnDeleteQuiz);
        
        // Extract quiz data (TeacherQuizzes format)
        String quizTitle = quiz.get("quizTitle") != null ? quiz.get("quizTitle").toString() : "Untitled Quiz";
        String quizNumber = quiz.get("quizNumber") != null ? quiz.get("quizNumber").toString() : "N/A";
        String grade = quiz.get("grade") != null ? quiz.get("grade").toString() : "N/A";
        String section = quiz.get("section") != null ? quiz.get("section").toString() : "All Sections";
        
        // Get question count
        java.util.List<Map<String, Object>> questions = 
            (java.util.List<Map<String, Object>>) quiz.get("questions");
        int questionCount = questions != null ? questions.size() : 0;
        
        // Get schedule info
        com.google.firebase.Timestamp startTimestamp = (com.google.firebase.Timestamp) quiz.get("startDateTime");
        com.google.firebase.Timestamp endTimestamp = (com.google.firebase.Timestamp) quiz.get("endDateTime");
        String scheduleInfo = "";
        if (startTimestamp != null && endTimestamp != null) {
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", java.util.Locale.getDefault());
            scheduleInfo = dateFormat.format(startTimestamp.toDate()) + " - " + 
                          dateFormat.format(endTimestamp.toDate());
        }
        
        // Display quiz info
        questionText.setText(quizTitle + " (Quiz #" + quizNumber + ")");
        detailsText.setText("Grade: " + grade + " | Section: " + section + 
            " | Questions: " + questionCount + 
            (scheduleInfo.isEmpty() ? "" : " | Schedule: " + scheduleInfo));
        
        // View button
        viewButton.setOnClickListener(v -> viewQuiz(quiz));
        
        // Edit button
        editButton.setOnClickListener(v -> editQuiz(quiz));
        
        // Delete button
        deleteButton.setOnClickListener(v -> deleteQuiz(quiz));
        
        quizzesListLayout.addView(quizView);
    }
    
    private void addCSVQuizGroupToUI(String groupKey, List<MathProblem> problems) {
        // Add group header
        TextView groupHeader = new TextView(this);
        groupHeader.setText(groupKey + " (" + problems.size() + " questions)");
        groupHeader.setTextSize(18);
        groupHeader.setTypeface(groupHeader.getTypeface(), android.graphics.Typeface.BOLD);
        groupHeader.setTextColor(0xFF5DB575);
        groupHeader.setPadding(16, 20, 16, 12);
        groupHeader.setBackgroundColor(0xFFF5F5F5);
        quizzesListLayout.addView(groupHeader);
        
        // Show ALL questions (not just preview)
        for (MathProblem problem : problems) {
            addCSVQuizToUI(problem);
        }
    }
    
    private void addCSVQuizToUI(MathProblem problem) {
        View quizView = getLayoutInflater().inflate(R.layout.item_quiz_manager, 
            quizzesListLayout, false);
        
        TextView questionText = quizView.findViewById(R.id.textQuestion);
        TextView detailsText = quizView.findViewById(R.id.textDetails);
        Button viewButton = quizView.findViewById(R.id.btnViewQuiz);
        Button editButton = quizView.findViewById(R.id.btnEditQuiz);
        Button deleteButton = quizView.findViewById(R.id.btnDeleteQuiz);
        
        // Hide all action buttons for CSV quizzes (read-only)
        viewButton.setVisibility(View.GONE);
        editButton.setVisibility(View.GONE);
        deleteButton.setVisibility(View.GONE);
        
        questionText.setText(problem.getQuestion());
        detailsText.setText("Operation: " + capitalizeFirst(problem.getOperation()) + 
            " | Difficulty: " + problem.getDifficulty() + 
            " | Answer: " + problem.getAnswer() + 
            " | Source: CSV File");
        
        quizzesListLayout.addView(quizView);
    }
    
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    private String getOperationSymbol(String operation) {
        switch (operation.toLowerCase()) {
            case "addition": return "+";
            case "subtraction": return "-";
            case "multiplication": return "×";
            case "division": return "÷";
            default: return "+";
        }
    }
    
    private void viewQuiz(Map<String, Object> quiz) {
        String documentId = quiz.get("documentId") != null ? quiz.get("documentId").toString() : null;
        if (documentId == null) {
            Toast.makeText(this, "Cannot view: Quiz ID not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, QuizViewActivity.class);
        intent.putExtra("quizDocumentId", documentId);
        startActivity(intent);
    }
    
    private void editQuiz(Map<String, Object> quiz) {
        String documentId = quiz.get("documentId") != null ? quiz.get("documentId").toString() : null;
        if (documentId == null) {
            Toast.makeText(this, "Cannot edit: Quiz ID not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Navigate to quiz creator in edit mode
        Intent intent = new Intent(this, TeacherQuizCreatorActivity.class);
        intent.putExtra("quizDocumentId", documentId);
        startActivity(intent);
    }
    
    private void deleteQuiz(Map<String, Object> quiz) {
        String documentId = quiz.get("documentId") != null ? quiz.get("documentId").toString() : null;
        if (documentId == null) {
            Toast.makeText(this, "Cannot delete: Quiz ID not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Extract quiz info for display
        String quizTitle = quiz.get("quizTitle") != null ? quiz.get("quizTitle").toString() : "Untitled Quiz";
        String quizNumber = quiz.get("quizNumber") != null ? quiz.get("quizNumber").toString() : "N/A";
        java.util.List<Map<String, Object>> questions = 
            (java.util.List<Map<String, Object>>) quiz.get("questions");
        int questionCount = questions != null ? questions.size() : 0;
        
        new AlertDialog.Builder(this)
            .setTitle("Delete Quiz")
            .setMessage("Are you sure you want to delete this quiz?\n\n" + 
                "Quiz: " + quizTitle + " (Quiz #" + quizNumber + ")\n" +
                "Questions: " + questionCount)
            .setPositiveButton("Delete", (dialog, which) -> {
                db.collection("TeacherQuizzes")
                    .document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Quiz deleted successfully", Toast.LENGTH_SHORT).show();
                        loadAllQuizzes(); // Reload the list
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error deleting quiz: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void updateToggleButtons() {
        if (isCSVMode) {
            btnCSVQuizzes.setBackgroundResource(R.drawable.btn_save_rounded);
            btnCustomQuizzes.setBackgroundResource(R.drawable.btn_inactive_rounded);
        } else {
            btnCustomQuizzes.setBackgroundResource(R.drawable.btn_save_rounded);
            btnCSVQuizzes.setBackgroundResource(R.drawable.btn_inactive_rounded);
        }
    }
    
    private void loadAllCSVQuizzes() {
        quizzesListLayout.removeAllViews();
        txtEmptyState.setVisibility(View.GONE);
        allCSVQuizzes.clear();
        
        // List of all CSV files to load
        String[] csvFiles = {
            "additionProblemSet.csv",
            "subtractionProblemSet.csv",
            "multiplicationProblemSet.csv",
            "divisionProblemSet.csv",
            "percentageProblemSet.csv",
            "decimalProblemSet.csv",
            "decimalAdditionProblemSet.csv",
            "decimalSubtractionProblemSet.csv",
            "decimalMultiplicationProblemSet.csv",
            "decimalDivisionProblemSet.csv",
            "problemSet.csv"
        };
        
        try {
            AssetManager assetManager = getAssets();
            int totalLoaded = 0;
            
            for (String fileName : csvFiles) {
                try {
                    BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(assetManager.open(fileName)));
                    List<MathProblem> problems = csvProcessor.readCSVFile(bufferedReader);
                    allCSVQuizzes.addAll(problems);
                    totalLoaded += problems.size();
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.w("QuizManager", "Could not load CSV file: " + fileName, e);
                }
            }
            
            Log.d("QuizManager", "Loaded " + totalLoaded + " CSV quiz questions from " + csvFiles.length + " files");
            filterAndDisplayQuizzes();
            
        } catch (Exception e) {
            Log.e("QuizManager", "Error loading CSV quizzes", e);
            Toast.makeText(this, "Error loading CSV quizzes: " + e.getMessage(), 
                Toast.LENGTH_LONG).show();
        }
    }
}

