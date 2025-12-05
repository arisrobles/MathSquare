package com.happym.mathsquare;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.happym.mathsquare.sharedPreferences;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Teacher Quiz Creator - Create complete quizzes with title, number, grade, section, schedule, and questions
 * Real-time Firebase integration for student-side display
 */
public class TeacherQuizCreatorActivity extends AppCompatActivity {
    
    private FirebaseFirestore db;
    private EditText quizTitleEditText, quizNumberEditText;
    private Spinner gradeSpinner, sectionSpinner;
    private Button startDateButton, startTimeButton, endDateButton, endTimeButton;
    private Button addQuestionButton, saveQuizButton;
    private LinearLayout questionsListLayout;
    private List<Map<String, Object>> questionsList;
    private List<com.happym.mathsquare.Model.Sections> sectionsList;
    
    private Calendar startDateTime = Calendar.getInstance();
    private Calendar endDateTime = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    
    // Edit mode variables
    private boolean isEditMode = false;
    private String quizDocumentId = null;
    
    // Question edit mode variables
    private Integer editingQuestionIndex = null;
    
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("QuizCreator", "onStart called");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("QuizCreator", "onResume called");
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("QuizCreator", "onPause called");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("QuizCreator", "onDestroy called - isEditMode: " + isEditMode + ", quizDocumentId: " + quizDocumentId);
    }
    
    @Override
    public void onBackPressed() {
        Log.d("QuizCreator", "onBackPressed called");
        // Check if there are unsaved changes
        if (hasUnsavedChanges()) {
            com.happym.mathsquare.utils.BackButtonHandler.showExitConfirmation(
                this,
                "Exit Quiz Creator",
                "You have unsaved changes. Are you sure you want to exit?",
                () -> finish()
            );
        } else {
            super.onBackPressed();
        }
    }
    
    /**
     * Check if there are unsaved changes in the quiz form
     */
    private boolean hasUnsavedChanges() {
        // If in edit mode, always show confirmation (we're modifying existing data)
        if (isEditMode) {
            return true;
        }
        
        // Check if quiz title or number has been entered
        String title = quizTitleEditText != null ? quizTitleEditText.getText().toString().trim() : "";
        String number = quizNumberEditText != null ? quizNumberEditText.getText().toString().trim() : "";
        
        // Check if questions have been added
        boolean hasQuestions = questionsList != null && !questionsList.isEmpty();
        
        // Check if any form field has been filled
        boolean hasFormData = !title.isEmpty() || !number.isEmpty() || hasQuestions;
        
        return hasFormData;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("QuizCreator", "onCreate called");
        super.onCreate(savedInstanceState);
        
        try {
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            setContentView(R.layout.activity_teacher_quiz_creator);
            
            // Check if we're in edit mode BEFORE initializing views
            quizDocumentId = getIntent().getStringExtra("quizDocumentId");
            if (quizDocumentId != null && !quizDocumentId.isEmpty()) {
                isEditMode = true;
                Log.d("QuizCreator", "Edit mode detected, quizDocumentId: " + quizDocumentId);
                Toast.makeText(this, "Loading quiz for editing...", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("QuizCreator", "Create mode (new quiz)");
            }
            
            FirebaseApp.initializeApp(this);
            db = FirebaseFirestore.getInstance();
            
            questionsList = new ArrayList<>();
            sectionsList = new ArrayList<>();
            
            // Initialize views
            quizTitleEditText = findViewById(R.id.editQuizTitle);
            quizNumberEditText = findViewById(R.id.editQuizNumber);
            gradeSpinner = findViewById(R.id.spinnerGrade);
            sectionSpinner = findViewById(R.id.spinnerSection);
            startDateButton = findViewById(R.id.btnStartDate);
            startTimeButton = findViewById(R.id.btnStartTime);
            endDateButton = findViewById(R.id.btnEndDate);
            endTimeButton = findViewById(R.id.btnEndTime);
            addQuestionButton = findViewById(R.id.btnAddQuestion);
            saveQuizButton = findViewById(R.id.btnSaveQuiz);
            questionsListLayout = findViewById(R.id.questionsListLayout);
            
            // Validate all views are found
            if (quizTitleEditText == null || quizNumberEditText == null || 
                gradeSpinner == null || sectionSpinner == null ||
                startDateButton == null || startTimeButton == null ||
                endDateButton == null || endTimeButton == null ||
                addQuestionButton == null || saveQuizButton == null ||
                questionsListLayout == null) {
                Toast.makeText(this, "Error: Some UI elements not found. Please restart the app.", Toast.LENGTH_LONG).show();
                Log.e("QuizCreator", "One or more views are null");
                // Don't finish immediately - let user see the error message
                return;
            }
            
            // Setup spinners
            setupSpinners();
            loadSections();
            
            // Setup date/time buttons
            setupDateTimeButtons();
            
            // Setup question form (reuse from QuizCMSActivity logic)
            setupQuestionForm();
            
            addQuestionButton.setOnClickListener(v -> addQuestionToList());
            saveQuizButton.setOnClickListener(v -> saveQuizToFirestore());
            
            // If in edit mode, load the quiz data
            if (isEditMode && quizDocumentId != null && !quizDocumentId.isEmpty()) {
                saveQuizButton.setText("Update Quiz");
                // Delay loading quiz to ensure UI is ready and sections are loaded
                // Increase delay to ensure sections are fully loaded
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    Log.d("QuizCreator", "Starting to load quiz for editing, documentId: " + quizDocumentId);
                    if (quizDocumentId != null && !quizDocumentId.isEmpty()) {
                        loadQuizForEditing(quizDocumentId);
                    } else {
                        Log.e("QuizCreator", "quizDocumentId became null before loading");
                        Toast.makeText(this, "Error: Quiz ID is missing. Please try again.", Toast.LENGTH_LONG).show();
                    }
                }, 1000); // Increased delay to 1 second
            }
            
        } catch (Exception e) {
            Log.e("QuizCreator", "Error in onCreate", e);
            Toast.makeText(this, "Error initializing activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            // Don't finish on exception - let user see the error and try to continue
        }
    }
    
    private void setupSpinners() {
        // Grade spinner
        String[] grades = {"1", "2", "3", "4", "5", "6"};
        android.widget.ArrayAdapter<String> gradeAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, grades);
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gradeSpinner.setAdapter(gradeAdapter);
        
        // Section spinner will be populated dynamically
    }
    
    private void loadSections() {
        db.collection("Sections")
            .orderBy("Grade_Number", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    sectionsList.clear();
                    List<String> sectionDisplayList = new ArrayList<>();
                    sectionDisplayList.add("All Sections");
                    
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : task.getResult()) {
                        Long gradeNumLong = doc.getLong("Grade_Number");
                        String grade = gradeNumLong != null ? String.valueOf(gradeNumLong.intValue()) : null;
                        String section = doc.getString("Section");
                        String docId = doc.getId();
                        
                        if (grade != null && section != null) {
                            sectionsList.add(new com.happym.mathsquare.Model.Sections(section, grade, docId));
                            sectionDisplayList.add("Grade " + grade + " - " + section);
                        }
                    }
                    
                    android.widget.ArrayAdapter<String> sectionAdapter = new android.widget.ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, sectionDisplayList);
                    sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sectionSpinner.setAdapter(sectionAdapter);
                }
            });
    }
    
    private void setupDateTimeButtons() {
        // Set default times (start: now, end: 7 days from now)
        endDateTime.add(Calendar.DAY_OF_MONTH, 7);
        
        updateDateTimeButtons();
        
        startDateButton.setOnClickListener(v -> showDatePicker(startDateTime, startDateButton, true));
        startTimeButton.setOnClickListener(v -> showTimePicker(startDateTime, startTimeButton, true));
        endDateButton.setOnClickListener(v -> showDatePicker(endDateTime, endDateButton, false));
        endTimeButton.setOnClickListener(v -> showTimePicker(endDateTime, endTimeButton, false));
    }
    
    private void showDatePicker(Calendar calendar, Button button, boolean isStart) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                
                // Validate: end date must be after start date
                if (!isStart && calendar.before(startDateTime)) {
                    Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
                    calendar.setTimeInMillis(startDateTime.getTimeInMillis());
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
                
                updateDateTimeButtons();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        if (!isStart) {
            datePickerDialog.getDatePicker().setMinDate(startDateTime.getTimeInMillis());
        }
        
        datePickerDialog.show();
    }
    
    private void showTimePicker(Calendar calendar, Button button, boolean isStart) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                
                // Validate: end time must be after start time if same date
                if (!isStart && 
                    calendar.get(Calendar.YEAR) == startDateTime.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == startDateTime.get(Calendar.DAY_OF_YEAR) &&
                    calendar.before(startDateTime)) {
                    Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
                    calendar.setTimeInMillis(startDateTime.getTimeInMillis());
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                }
                
                updateDateTimeButtons();
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false // 12-hour format
        );
        
        timePickerDialog.show();
    }
    
    private void updateDateTimeButtons() {
        startDateButton.setText("Start Date: " + dateFormat.format(startDateTime.getTime()));
        startTimeButton.setText("Start Time: " + timeFormat.format(startDateTime.getTime()));
        endDateButton.setText("End Date: " + dateFormat.format(endDateTime.getTime()));
        endTimeButton.setText("End Time: " + timeFormat.format(endDateTime.getTime()));
    }
    
    private void setupQuestionForm() {
        // Setup operation and difficulty spinners
        Spinner operationSpinner = findViewById(R.id.spinnerOperation);
        Spinner difficultySpinner = findViewById(R.id.spinnerDifficulty);
        
        // Operation spinner
        String[] operations = {"Addition", "Subtraction", "Multiplication", "Division", "Decimal", "Percentage"};
        android.widget.ArrayAdapter<String> operationAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, operations);
        operationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        operationSpinner.setAdapter(operationAdapter);
        
        // Difficulty spinner
        String[] difficulties = {"Easy", "Medium", "Hard"};
        android.widget.ArrayAdapter<String> difficultyAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, difficulties);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(difficultyAdapter);
    }
    
    private void addQuestionToList() {
        // Get question form fields
        EditText num1EditText = findViewById(R.id.editNum1);
        EditText num2EditText = findViewById(R.id.editNum2);
        EditText answerEditText = findViewById(R.id.editAnswer);
        EditText choice1EditText = findViewById(R.id.editChoice1);
        EditText choice2EditText = findViewById(R.id.editChoice2);
        EditText choice3EditText = findViewById(R.id.editChoice3);
        EditText choice4EditText = findViewById(R.id.editChoice4);
        Spinner operationSpinner = findViewById(R.id.spinnerOperation);
        Spinner difficultySpinner = findViewById(R.id.spinnerDifficulty);
        
        // Validate inputs
        if (num1EditText.getText().toString().trim().isEmpty() ||
            num2EditText.getText().toString().trim().isEmpty() ||
            answerEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            double num1 = Double.parseDouble(num1EditText.getText().toString());
            double num2 = Double.parseDouble(num2EditText.getText().toString());
            double answer = Double.parseDouble(answerEditText.getText().toString());
            
            String operation = operationSpinner.getSelectedItem().toString();
            String difficulty = difficultySpinner.getSelectedItem().toString();
            
            String choice1 = choice1EditText.getText().toString().trim();
            String choice2 = choice2EditText.getText().toString().trim();
            String choice3 = choice3EditText.getText().toString().trim();
            String choice4 = choice4EditText.getText().toString().trim();
            
            if (choice1.isEmpty() || choice2.isEmpty() || choice3.isEmpty() || choice4.isEmpty()) {
                Toast.makeText(this, "Please fill in all 4 choices", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String choices = choice1 + "," + choice2 + "," + choice3 + "," + choice4;
            
            // Create question map
            Map<String, Object> question = new HashMap<>();
            question.put("question", num1 + "|||" + num2);
            question.put("operation", operation);
            question.put("difficulty", difficulty);
            question.put("answer", answer);
            question.put("correctAnswer", String.valueOf(answer)); // Also store as string for compatibility
            question.put("choices", choices);
            
            if (editingQuestionIndex != null && editingQuestionIndex >= 0 && editingQuestionIndex < questionsList.size()) {
                // Update existing question
                questionsList.set(editingQuestionIndex, question);
                Toast.makeText(this, "Question updated", Toast.LENGTH_SHORT).show();
                editingQuestionIndex = null;
                addQuestionButton.setText("Add Question");
            } else {
                // Add new question
                questionsList.add(question);
                Toast.makeText(this, "Question added to list", Toast.LENGTH_SHORT).show();
            }
            
            // Refresh the list
            refreshQuestionList();
            
            // Clear form
            clearQuestionForm();
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Loads question data into the form for editing
     */
    private void loadQuestionToForm(Map<String, Object> question) {
        try {
            Log.d("QuizCreator", "Loading question to form for editing");
            
            EditText num1EditText = findViewById(R.id.editNum1);
            EditText num2EditText = findViewById(R.id.editNum2);
            EditText answerEditText = findViewById(R.id.editAnswer);
            EditText choice1EditText = findViewById(R.id.editChoice1);
            EditText choice2EditText = findViewById(R.id.editChoice2);
            EditText choice3EditText = findViewById(R.id.editChoice3);
            EditText choice4EditText = findViewById(R.id.editChoice4);
            Spinner operationSpinner = findViewById(R.id.spinnerOperation);
            Spinner difficultySpinner = findViewById(R.id.spinnerDifficulty);
            
            if (num1EditText == null || num2EditText == null || answerEditText == null ||
                choice1EditText == null || choice2EditText == null || 
                choice3EditText == null || choice4EditText == null ||
                operationSpinner == null || difficultySpinner == null) {
                Log.e("QuizCreator", "Question form views are null");
                Toast.makeText(this, "Error: Question form not ready. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Parse question string
            String questionStr = (String) question.get("question");
            if (questionStr != null) {
                String[] parts = questionStr.split("\\|\\|\\|");
                if (parts.length >= 2) {
                    num1EditText.setText(parts[0]);
                    num2EditText.setText(parts[1]);
                } else {
                    Log.w("QuizCreator", "Question string format unexpected: " + questionStr);
                }
            }
            
            // Set answer
            Object answerObj = question.get("answer");
            if (answerObj != null) {
                answerEditText.setText(answerObj.toString());
            } else {
                // Try correctAnswer as fallback
                Object correctAnswerObj = question.get("correctAnswer");
                if (correctAnswerObj != null) {
                    answerEditText.setText(correctAnswerObj.toString());
                }
            }
            
            // Set choices
            String choicesStr = question.get("choices") != null ? question.get("choices").toString() : "";
            if (!choicesStr.isEmpty()) {
                String[] choices = choicesStr.split(",");
                if (choices.length >= 4) {
                    choice1EditText.setText(choices[0].trim());
                    choice2EditText.setText(choices[1].trim());
                    choice3EditText.setText(choices[2].trim());
                    choice4EditText.setText(choices[3].trim());
                } else {
                    Log.w("QuizCreator", "Choices format unexpected, length: " + choices.length);
                }
            }
            
            // Set operation spinner
            String operation = question.get("operation") != null ? question.get("operation").toString() : "Addition";
            boolean operationSet = false;
            for (int i = 0; i < operationSpinner.getCount(); i++) {
                if (operationSpinner.getItemAtPosition(i).toString().equals(operation)) {
                    operationSpinner.setSelection(i);
                    operationSet = true;
                    break;
                }
            }
            if (!operationSet) {
                Log.w("QuizCreator", "Operation not found in spinner: " + operation);
            }
            
            // Set difficulty spinner
            String difficulty = question.get("difficulty") != null ? question.get("difficulty").toString() : "Easy";
            boolean difficultySet = false;
            for (int i = 0; i < difficultySpinner.getCount(); i++) {
                if (difficultySpinner.getItemAtPosition(i).toString().equals(difficulty)) {
                    difficultySpinner.setSelection(i);
                    difficultySet = true;
                    break;
                }
            }
            if (!difficultySet) {
                Log.w("QuizCreator", "Difficulty not found in spinner: " + difficulty);
            }
            
            // Scroll to question form section - use a safer approach
            final View questionFormSection = findViewById(R.id.question_form_section) != null ? 
                findViewById(R.id.question_form_section) : num1EditText;
            
            if (questionFormSection != null) {
                questionFormSection.post(() -> {
                    try {
                        // Find ScrollView in the view hierarchy
                        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
                        android.widget.ScrollView scrollView = null;
                        if (rootView != null) {
                            scrollView = findScrollView(rootView);
                        }
                        if (scrollView != null) {
                            int[] location = new int[2];
                            questionFormSection.getLocationOnScreen(location);
                            int[] scrollLocation = new int[2];
                            scrollView.getLocationOnScreen(scrollLocation);
                            int scrollY = location[1] - scrollLocation[1] - 100; // Offset to show form better
                            scrollView.smoothScrollTo(0, Math.max(0, scrollY));
                            Log.d("QuizCreator", "Scrolled to question form");
                        } else {
                            Log.w("QuizCreator", "ScrollView not found for scrolling");
                        }
                    } catch (Exception e) {
                        Log.e("QuizCreator", "Error scrolling to question form", e);
                        // Don't show error to user - scrolling is not critical
                    }
                });
            }
            
            Log.d("QuizCreator", "Question loaded to form successfully");
            Toast.makeText(this, "Question loaded for editing", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e("QuizCreator", "Error loading question to form", e);
            Toast.makeText(this, "Error loading question: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private android.widget.ScrollView findScrollView(View view) {
        if (view instanceof android.widget.ScrollView) {
            return (android.widget.ScrollView) view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                android.widget.ScrollView found = findScrollView(group.getChildAt(i));
                if (found != null) return found;
            }
        }
        return null;
    }
    
    /**
     * Shows a modal dialog to edit a question
     */
    private void showEditQuestionDialog(Map<String, Object> question, int questionIndex) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_question);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(R.color.yellowbg);
        
        // Get dialog views
        EditText num1EditText = dialog.findViewById(R.id.dialog_editNum1);
        EditText num2EditText = dialog.findViewById(R.id.dialog_editNum2);
        EditText answerEditText = dialog.findViewById(R.id.dialog_editAnswer);
        EditText choice1EditText = dialog.findViewById(R.id.dialog_editChoice1);
        EditText choice2EditText = dialog.findViewById(R.id.dialog_editChoice2);
        EditText choice3EditText = dialog.findViewById(R.id.dialog_editChoice3);
        EditText choice4EditText = dialog.findViewById(R.id.dialog_editChoice4);
        Spinner operationSpinner = dialog.findViewById(R.id.dialog_spinnerOperation);
        Spinner difficultySpinner = dialog.findViewById(R.id.dialog_spinnerDifficulty);
        Button cancelButton = dialog.findViewById(R.id.dialog_btnCancel);
        Button updateButton = dialog.findViewById(R.id.dialog_btnUpdate);
        
        // Setup spinners
        String[] operations = {"Addition", "Subtraction", "Multiplication", "Division", "Decimal", "Percentage"};
        android.widget.ArrayAdapter<String> operationAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, operations);
        operationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        operationSpinner.setAdapter(operationAdapter);
        
        String[] difficulties = {"Easy", "Medium", "Hard"};
        android.widget.ArrayAdapter<String> difficultyAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, difficulties);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(difficultyAdapter);
        
        // Load question data into dialog
        String questionStr = (String) question.get("question");
        if (questionStr != null) {
            String[] parts = questionStr.split("\\|\\|\\|");
            if (parts.length >= 2) {
                num1EditText.setText(parts[0]);
                num2EditText.setText(parts[1]);
            }
        }
        
        // Set answer
        Object answerObj = question.get("answer");
        if (answerObj != null) {
            answerEditText.setText(answerObj.toString());
        } else {
            Object correctAnswerObj = question.get("correctAnswer");
            if (correctAnswerObj != null) {
                answerEditText.setText(correctAnswerObj.toString());
            }
        }
        
        // Set choices
        String choicesStr = question.get("choices") != null ? question.get("choices").toString() : "";
        if (!choicesStr.isEmpty()) {
            String[] choices = choicesStr.split(",");
            if (choices.length >= 4) {
                choice1EditText.setText(choices[0].trim());
                choice2EditText.setText(choices[1].trim());
                choice3EditText.setText(choices[2].trim());
                choice4EditText.setText(choices[3].trim());
            }
        }
        
        // Set operation spinner
        String operation = question.get("operation") != null ? question.get("operation").toString() : "Addition";
        for (int i = 0; i < operationSpinner.getCount(); i++) {
            if (operationSpinner.getItemAtPosition(i).toString().equals(operation)) {
                operationSpinner.setSelection(i);
                break;
            }
        }
        
        // Set difficulty spinner
        String difficulty = question.get("difficulty") != null ? question.get("difficulty").toString() : "Easy";
        for (int i = 0; i < difficultySpinner.getCount(); i++) {
            if (difficultySpinner.getItemAtPosition(i).toString().equals(difficulty)) {
                difficultySpinner.setSelection(i);
                break;
            }
        }
        
        // Cancel button
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        // Update button
        updateButton.setOnClickListener(v -> {
            // Validate inputs
            if (num1EditText.getText().toString().trim().isEmpty() ||
                num2EditText.getText().toString().trim().isEmpty() ||
                answerEditText.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            try {
                double num1 = Double.parseDouble(num1EditText.getText().toString());
                double num2 = Double.parseDouble(num2EditText.getText().toString());
                double answer = Double.parseDouble(answerEditText.getText().toString());
                
                String selectedOperation = operationSpinner.getSelectedItem().toString();
                String selectedDifficulty = difficultySpinner.getSelectedItem().toString();
                
                String choice1 = choice1EditText.getText().toString().trim();
                String choice2 = choice2EditText.getText().toString().trim();
                String choice3 = choice3EditText.getText().toString().trim();
                String choice4 = choice4EditText.getText().toString().trim();
                
                if (choice1.isEmpty() || choice2.isEmpty() || choice3.isEmpty() || choice4.isEmpty()) {
                    Toast.makeText(this, "Please fill in all 4 choices", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                String choices = choice1 + "," + choice2 + "," + choice3 + "," + choice4;
                
                // Create updated question map
                Map<String, Object> updatedQuestion = new HashMap<>();
                updatedQuestion.put("question", num1 + "|||" + num2);
                updatedQuestion.put("operation", selectedOperation);
                updatedQuestion.put("difficulty", selectedDifficulty);
                updatedQuestion.put("answer", answer);
                updatedQuestion.put("correctAnswer", String.valueOf(answer));
                updatedQuestion.put("choices", choices);
                
                // Update question in list
                questionsList.set(questionIndex, updatedQuestion);
                refreshQuestionList();
                
                Toast.makeText(this, "Question updated successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.show();
    }
    
    /**
     * Clears the question form
     */
    private void clearQuestionForm() {
        EditText num1EditText = findViewById(R.id.editNum1);
        EditText num2EditText = findViewById(R.id.editNum2);
        EditText answerEditText = findViewById(R.id.editAnswer);
        EditText choice1EditText = findViewById(R.id.editChoice1);
        EditText choice2EditText = findViewById(R.id.editChoice2);
        EditText choice3EditText = findViewById(R.id.editChoice3);
        EditText choice4EditText = findViewById(R.id.editChoice4);
        
        num1EditText.setText("");
        num2EditText.setText("");
        answerEditText.setText("");
        choice1EditText.setText("");
        choice2EditText.setText("");
        choice3EditText.setText("");
        choice4EditText.setText("");
    }
    
    private void addQuestionToUI(Map<String, Object> question, int index) {
        View questionView = getLayoutInflater().inflate(R.layout.item_question_preview, 
            questionsListLayout, false);
        
        TextView questionText = questionView.findViewById(R.id.textQuestionPreview);
        TextView operationText = questionView.findViewById(R.id.textOperationPreview);
        Button editButton = questionView.findViewById(R.id.btnEditQuestion);
        Button removeButton = questionView.findViewById(R.id.btnRemoveQuestion);
        
        String questionStr = (String) question.get("question");
        String[] parts = questionStr.split("\\|\\|\\|");
        String num1 = parts.length > 0 ? parts[0] : "?";
        String num2 = parts.length > 1 ? parts[1] : "?";
        String operation = (String) question.get("operation");
        String opSymbol = getOperationSymbol(operation);
        
        questionText.setText(num1 + " " + opSymbol + " " + num2 + " = ?");
        operationText.setText("Operation: " + operation + " | Difficulty: " + question.get("difficulty"));
        
        // Edit button - show modal dialog
        editButton.setOnClickListener(v -> {
            showEditQuestionDialog(question, index);
        });
        
        // Remove button
        removeButton.setOnClickListener(v -> {
            questionsList.remove(index);
            questionsListLayout.removeAllViews();
            refreshQuestionList();
            // Reset edit mode if this was the question being edited
            if (editingQuestionIndex != null && editingQuestionIndex == index) {
                editingQuestionIndex = null;
                addQuestionButton.setText("Add Question");
                clearQuestionForm();
            } else if (editingQuestionIndex != null && editingQuestionIndex > index) {
                // Adjust index if a question before the editing one was removed
                editingQuestionIndex--;
            }
        });
        
        questionsListLayout.addView(questionView);
    }
    
    private void refreshQuestionList() {
        questionsListLayout.removeAllViews();
        for (int i = 0; i < questionsList.size(); i++) {
            addQuestionToUI(questionsList.get(i), i);
        }
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
    
    private void saveQuizToFirestore() {
        // Validate quiz info
        String quizTitle = quizTitleEditText.getText().toString().trim();
        String quizNumber = quizNumberEditText.getText().toString().trim();
        
        if (quizTitle.isEmpty()) {
            Toast.makeText(this, "Please enter quiz title", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (quizNumber.isEmpty()) {
            Toast.makeText(this, "Please enter quiz number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (questionsList.isEmpty()) {
            Toast.makeText(this, "Please add at least one question", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String grade = gradeSpinner.getSelectedItem().toString();
        String selectedSectionDisplay = sectionSpinner.getSelectedItem().toString();
        
        // Get section info
        String section = null;
        String sectionId = null;
        if (!selectedSectionDisplay.equals("All Sections") && sectionSpinner.getSelectedItemPosition() > 0) {
            int sectionIndex = sectionSpinner.getSelectedItemPosition() - 1;
            if (sectionIndex >= 0 && sectionIndex < sectionsList.size()) {
                com.happym.mathsquare.Model.Sections selectedSection = sectionsList.get(sectionIndex);
                section = selectedSection.getSection();
                sectionId = selectedSection.getDocId();
            }
        }
        
        String teacherEmail = sharedPreferences.getEmail(this);
        if (teacherEmail == null) {
            Toast.makeText(this, "Teacher not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create quiz document
        Map<String, Object> quizData = new HashMap<>();
        quizData.put("quizTitle", quizTitle);
        quizData.put("quizNumber", quizNumber);
        quizData.put("grade", grade);
        quizData.put("section", section != null ? section : "All Sections");
        quizData.put("sectionId", sectionId);
        quizData.put("questions", questionsList);
        quizData.put("startDateTime", new com.google.firebase.Timestamp(startDateTime.getTime()));
        quizData.put("endDateTime", new com.google.firebase.Timestamp(endDateTime.getTime()));
        quizData.put("createdBy", teacherEmail);
        quizData.put("createdAt", FieldValue.serverTimestamp());
        quizData.put("isActive", false); // Will be set to true when current time is between start and end
        quizData.put("status", "scheduled"); // scheduled, active, expired
        
        // Save or update to Firestore
        if (isEditMode && quizDocumentId != null) {
            // Update existing quiz - preserve createdAt and createdBy
            Map<String, Object> updateData = new HashMap<>(quizData);
            updateData.remove("createdAt");
            updateData.remove("createdBy");
            updateData.put("updatedAt", FieldValue.serverTimestamp());
            
            db.collection("TeacherQuizzes")
                .document(quizDocumentId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("QuizCreator", "Quiz updated with ID: " + quizDocumentId);
                    Toast.makeText(this, "Quiz updated successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to quiz manager
                })
                .addOnFailureListener(e -> {
                    Log.e("QuizCreator", "Error updating quiz", e);
                    Toast.makeText(this, "Error updating quiz: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
        } else {
            // Create new quiz
            db.collection("TeacherQuizzes")
                .add(quizData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("QuizCreator", "Quiz saved with ID: " + documentReference.getId());
                    Toast.makeText(this, "Quiz created successfully!", Toast.LENGTH_SHORT).show();
                    
                    // Clear form
                    quizTitleEditText.setText("");
                    quizNumberEditText.setText("");
                    questionsList.clear();
                    questionsListLayout.removeAllViews();
                    
                    // Reset date/time to defaults
                    startDateTime = Calendar.getInstance();
                    endDateTime = Calendar.getInstance();
                    endDateTime.add(Calendar.DAY_OF_MONTH, 7);
                    updateDateTimeButtons();
                })
                .addOnFailureListener(e -> {
                    Log.e("QuizCreator", "Error saving quiz", e);
                    Toast.makeText(this, "Error creating quiz: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
        }
    }
    
    /**
     * Loads quiz data for editing
     */
    private void loadQuizForEditing(String documentId) {
        if (documentId == null || documentId.isEmpty()) {
            Toast.makeText(this, "Invalid quiz ID", Toast.LENGTH_SHORT).show();
            Log.e("QuizCreator", "loadQuizForEditing: documentId is null or empty");
            return;
        }
        
        Log.d("QuizCreator", "Loading quiz for editing: " + documentId);
        
        db.collection("TeacherQuizzes")
            .document(documentId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult() != null && task.getResult().exists()) {
                        Map<String, Object> quizData = task.getResult().getData();
                        
                        if (quizData == null) {
                            Toast.makeText(this, "Quiz data is empty", Toast.LENGTH_SHORT).show();
                            Log.e("QuizCreator", "Quiz data is null for document: " + documentId);
                            return;
                        }
                        
                        try {
                            Log.d("QuizCreator", "Quiz data retrieved, parsing...");
                            
                            // Load quiz title and number
                            String quizTitle = quizData.get("quizTitle") != null ? quizData.get("quizTitle").toString() : "";
                            String quizNumber = quizData.get("quizNumber") != null ? quizData.get("quizNumber").toString() : "";
                            
                            if (quizTitleEditText != null) {
                                quizTitleEditText.setText(quizTitle);
                            }
                            if (quizNumberEditText != null) {
                                quizNumberEditText.setText(quizNumber);
                            }
                            
                            Log.d("QuizCreator", "Quiz title: " + quizTitle + ", number: " + quizNumber);
                            
                            // Load grade
                            String grade = quizData.get("grade") != null ? quizData.get("grade").toString() : "1";
                            if (gradeSpinner != null) {
                                for (int i = 0; i < gradeSpinner.getCount(); i++) {
                                    if (gradeSpinner.getItemAtPosition(i).toString().equals(grade)) {
                                        gradeSpinner.setSelection(i);
                                        break;
                                    }
                                }
                            }
                            
                            // Load section (need to wait for sections to load)
                            // This will be handled after sections are loaded
                            final String section = quizData.get("section") != null ? quizData.get("section").toString() : "All Sections";
                            final String sectionId = quizData.get("sectionId") != null ? quizData.get("sectionId").toString() : null;
                            
                            // Load date/time
                            com.google.firebase.Timestamp startTimestamp = (com.google.firebase.Timestamp) quizData.get("startDateTime");
                            com.google.firebase.Timestamp endTimestamp = (com.google.firebase.Timestamp) quizData.get("endDateTime");
                            
                            if (startTimestamp != null) {
                                startDateTime.setTime(startTimestamp.toDate());
                            }
                            if (endTimestamp != null) {
                                endDateTime.setTime(endTimestamp.toDate());
                            }
                            updateDateTimeButtons();
                            
                            // Load questions
                            java.util.List<Map<String, Object>> questions = 
                                (java.util.List<Map<String, Object>>) quizData.get("questions");
                            if (questions != null) {
                                questionsList.clear();
                                questionsList.addAll(questions);
                                Log.d("QuizCreator", "Loaded " + questions.size() + " questions");
                                refreshQuestionList();
                            } else {
                                Log.w("QuizCreator", "No questions found in quiz data");
                            }
                            
                            // Set section after sections are loaded (use a delay to ensure sections are loaded)
                            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                setSectionSelection(section, sectionId);
                                Log.d("QuizCreator", "Quiz loaded successfully for editing");
                                Toast.makeText(this, "Quiz loaded successfully", Toast.LENGTH_SHORT).show();
                            }, 1000); // Increased delay to ensure sections are loaded
                            
                        } catch (Exception e) {
                            Toast.makeText(this, "Error parsing quiz data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("QuizCreator", "Error parsing quiz data", e);
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(this, "Quiz not found", Toast.LENGTH_SHORT).show();
                        Log.e("QuizCreator", "Quiz document does not exist: " + documentId);
                    }
                } else {
                    Exception exception = task.getException();
                    String errorMsg = exception != null ? exception.getMessage() : "Unknown error";
                    Toast.makeText(this, "Error loading quiz: " + errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("QuizCreator", "Error loading quiz: " + errorMsg, exception);
                    if (exception != null) {
                        exception.printStackTrace();
                    }
                }
            });
    }
    
    /**
     * Sets the section spinner selection based on section name and ID
     */
    private void setSectionSelection(String sectionName, String sectionId) {
        if (sectionName == null || sectionName.equals("All Sections")) {
            sectionSpinner.setSelection(0);
            return;
        }
        
        for (int i = 0; i < sectionsList.size(); i++) {
            com.happym.mathsquare.Model.Sections section = sectionsList.get(i);
            if (section.getSection().equals(sectionName) || 
                (sectionId != null && section.getDocId().equals(sectionId))) {
                sectionSpinner.setSelection(i + 1); // +1 because "All Sections" is at index 0
                break;
            }
        }
    }
}

