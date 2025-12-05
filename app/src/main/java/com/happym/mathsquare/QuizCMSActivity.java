package com.happym.mathsquare;

import android.os.Bundle;
import android.view.View;
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
import com.happym.mathsquare.sharedPreferences;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Quiz Content Management System - Allows teachers to add questions through UI
 * Implements defense comment #4: Quiz - CMS - can add questions through UI
 */
public class QuizCMSActivity extends AppCompatActivity {
    
    private FirebaseFirestore db;
    private EditText num1EditText, num2EditText, answerEditText;
    private EditText choice1EditText, choice2EditText, choice3EditText, choice4EditText;
    private Spinner operationSpinner, difficultySpinner, gradeSpinner, sectionSpinner;
    private Button addQuestionButton, saveButton;
    private LinearLayout questionsListLayout;
    private List<Map<String, Object>> questionsList;
    private List<com.happym.mathsquare.Model.Sections> sectionsList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_quiz_cms);
        
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        
        questionsList = new ArrayList<>();
        
        // Initialize views
        num1EditText = findViewById(R.id.editNum1);
        num2EditText = findViewById(R.id.editNum2);
        answerEditText = findViewById(R.id.editAnswer);
        choice1EditText = findViewById(R.id.editChoice1);
        choice2EditText = findViewById(R.id.editChoice2);
        choice3EditText = findViewById(R.id.editChoice3);
        choice4EditText = findViewById(R.id.editChoice4);
        operationSpinner = findViewById(R.id.spinnerOperation);
        difficultySpinner = findViewById(R.id.spinnerDifficulty);
        gradeSpinner = findViewById(R.id.spinnerGrade);
        sectionSpinner = findViewById(R.id.spinnerSection);
        addQuestionButton = findViewById(R.id.btnAddQuestion);
        saveButton = findViewById(R.id.btnSaveQuestions);
        questionsListLayout = findViewById(R.id.questionsListLayout);
        
        sectionsList = new ArrayList<>();
        
        // Populate spinners
        setupSpinners();
        loadSections();
        
        addQuestionButton.setOnClickListener(v -> addQuestionToList());
        saveButton.setOnClickListener(v -> saveQuestionsToFirestore());
    }
    
    private void setupSpinners() {
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
        
        // Grade spinner
        String[] grades = {"1", "2", "3", "4", "5", "6"};
        android.widget.ArrayAdapter<String> gradeAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, grades);
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gradeSpinner.setAdapter(gradeAdapter);
        
        // Section spinner will be populated dynamically after loading sections
    }
    
    private void loadSections() {
        db.collection("Sections")
            .orderBy("Grade_Number", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    sectionsList.clear();
                    List<String> sectionDisplayList = new ArrayList<>();
                    sectionDisplayList.add("All Sections"); // Option for all sections
                    
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
                } else {
                    Toast.makeText(this, "Error loading sections", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void addQuestionToList() {
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
            String grade = gradeSpinner.getSelectedItem().toString();
            String selectedSectionDisplay = sectionSpinner.getSelectedItem().toString();
            
            // Get section info (if not "All Sections")
            String section = null;
            String sectionId = null;
            if (!selectedSectionDisplay.equals("All Sections") && sectionSpinner.getSelectedItemPosition() > 0) {
                int sectionIndex = sectionSpinner.getSelectedItemPosition() - 1; // -1 because "All Sections" is at index 0
                if (sectionIndex >= 0 && sectionIndex < sectionsList.size()) {
                    com.happym.mathsquare.Model.Sections selectedSection = sectionsList.get(sectionIndex);
                    section = selectedSection.getSection();
                    sectionId = selectedSection.getDocId();
                }
            }
            
            // Build choices string
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
            question.put("choices", choices);
            question.put("grade", grade);
            if (section != null) {
                question.put("section", section);
                question.put("sectionId", sectionId);
            }
            
            questionsList.add(question);
            
            // Add to UI list
            addQuestionToUI(question, questionsList.size() - 1);
            
            // Clear form
            clearForm();
            
            Toast.makeText(this, "Question added to list", Toast.LENGTH_SHORT).show();
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void addQuestionToUI(Map<String, Object> question, int index) {
        View questionView = getLayoutInflater().inflate(R.layout.item_question_preview, 
            questionsListLayout, false);
        
        TextView questionText = questionView.findViewById(R.id.textQuestionPreview);
        TextView operationText = questionView.findViewById(R.id.textOperationPreview);
        Button removeButton = questionView.findViewById(R.id.btnRemoveQuestion);
        
        String num1 = ((String) question.get("question")).split("\\|\\|\\|")[0];
        String num2 = ((String) question.get("question")).split("\\|\\|\\|")[1];
        String operation = (String) question.get("operation");
        String opSymbol = getOperationSymbol(operation);
        
        questionText.setText(num1 + " " + opSymbol + " " + num2 + " = ?");
        operationText.setText("Operation: " + operation + " | Difficulty: " + question.get("difficulty"));
        
        removeButton.setOnClickListener(v -> {
            questionsList.remove(index);
            questionsListLayout.removeView(questionView);
            refreshQuestionList();
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
    
    private void clearForm() {
        num1EditText.setText("");
        num2EditText.setText("");
        answerEditText.setText("");
        choice1EditText.setText("");
        choice2EditText.setText("");
        choice3EditText.setText("");
        choice4EditText.setText("");
    }
    
    private void saveQuestionsToFirestore() {
        if (questionsList.isEmpty()) {
            Toast.makeText(this, "No questions to save", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String teacherEmail = sharedPreferences.getEmail(this);
        if (teacherEmail == null) {
            Toast.makeText(this, "Teacher not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Save to Firestore under CustomQuestions collection
        for (Map<String, Object> question : questionsList) {
            question.put("createdBy", teacherEmail);
            question.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
            
            db.collection("CustomQuestions")
                .add(question)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Questions saved successfully!", Toast.LENGTH_SHORT).show();
                    questionsList.clear();
                    questionsListLayout.removeAllViews();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving question: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
        }
    }
}

