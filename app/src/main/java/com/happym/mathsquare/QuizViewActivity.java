package com.happym.mathsquare;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class QuizViewActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private MediaPlayer soundEffectPlayer;
    private LinearLayout quizInfoLayout;
    private LinearLayout questionsLayout;
    private TextView emptyStateText;
    private String quizDocumentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_quiz_view);

        db = FirebaseFirestore.getInstance();
        quizInfoLayout = findViewById(R.id.quizInfoLayout);
        questionsLayout = findViewById(R.id.questionsLayout);
        emptyStateText = findViewById(R.id.emptyStateText);

        quizDocumentId = getIntent().getStringExtra("quizDocumentId");
        if (quizDocumentId == null || quizDocumentId.isEmpty()) {
            Toast.makeText(this, "Quiz ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadQuizDetails();
    }

    private void loadQuizDetails() {
        db.collection("TeacherQuizzes")
            .document(quizDocumentId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        displayQuiz(doc.getData());
                    } else {
                        showEmptyState("Quiz not found");
                    }
                } else {
                    showEmptyState("Error loading quiz");
                    Toast.makeText(this, "Error loading quiz: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                        Toast.LENGTH_LONG).show();
                }
            });
    }

    private void displayQuiz(Map<String, Object> quizData) {
        // Display quiz information
        String quizTitle = quizData.get("quizTitle") != null ? quizData.get("quizTitle").toString() : "Untitled Quiz";
        String quizNumber = quizData.get("quizNumber") != null ? quizData.get("quizNumber").toString() : "N/A";
        String grade = quizData.get("grade") != null ? quizData.get("grade").toString() : "N/A";
        String section = quizData.get("section") != null ? quizData.get("section").toString() : "All Sections";
        
        Timestamp startTimestamp = (Timestamp) quizData.get("startDateTime");
        Timestamp endTimestamp = (Timestamp) quizData.get("endDateTime");
        
        // Set title
        TextView titleText = findViewById(R.id.quizTitleText);
        titleText.setText(quizTitle);
        
        // Set quiz info
        TextView infoText = findViewById(R.id.quizInfoText);
        StringBuilder info = new StringBuilder();
        info.append("Quiz Number: ").append(quizNumber).append("\n");
        info.append("Grade: ").append(grade).append("\n");
        info.append("Section: ").append(section).append("\n");
        
        if (startTimestamp != null && endTimestamp != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            info.append("Start: ").append(dateFormat.format(startTimestamp.toDate())).append("\n");
            info.append("End: ").append(dateFormat.format(endTimestamp.toDate()));
        }
        
        infoText.setText(info.toString());
        
        // Display questions
        List<Map<String, Object>> questions = (List<Map<String, Object>>) quizData.get("questions");
        if (questions != null && !questions.isEmpty()) {
            questionsLayout.removeAllViews();
            emptyStateText.setVisibility(View.GONE);
            questionsLayout.setVisibility(View.VISIBLE);
            
            for (int i = 0; i < questions.size(); i++) {
                addQuestionToUI(questions.get(i), i + 1);
            }
        } else {
            showEmptyState("No questions in this quiz");
        }
    }

    private void addQuestionToUI(Map<String, Object> question, int questionNumber) {
        View questionView = getLayoutInflater().inflate(R.layout.item_quiz_question_view, 
            questionsLayout, false);
        
        TextView questionNumberText = questionView.findViewById(R.id.questionNumberText);
        TextView questionText = questionView.findViewById(R.id.questionText);
        TextView operationText = questionView.findViewById(R.id.operationText);
        TextView difficultyText = questionView.findViewById(R.id.difficultyText);
        TextView correctAnswerText = questionView.findViewById(R.id.correctAnswerText);
        LinearLayout choicesLayout = questionView.findViewById(R.id.choicesLayout);
        
        // Set question number
        questionNumberText.setText("Question " + questionNumber);
        
        // Parse question
        String questionStr = question.get("question") != null ? question.get("question").toString() : "";
        String[] parts = questionStr.split("\\|\\|\\|");
        String num1 = parts.length > 0 ? parts[0] : "?";
        String num2 = parts.length > 1 ? parts[1] : "?";
        String operation = question.get("operation") != null ? question.get("operation").toString() : "Unknown";
        String opSymbol = getOperationSymbol(operation);
        
        questionText.setText(num1 + " " + opSymbol + " " + num2 + " = ?");
        operationText.setText("Operation: " + operation);
        
        String difficulty = question.get("difficulty") != null ? question.get("difficulty").toString() : "Unknown";
        difficultyText.setText("Difficulty: " + difficulty);
        
        // Get correct answer
        String correctAnswer = question.get("correctAnswer") != null ? question.get("correctAnswer").toString() : "N/A";
        correctAnswerText.setText("Correct Answer: " + correctAnswer);
        
        // Display choices
        String choicesStr = question.get("choices") != null ? question.get("choices").toString() : "";
        if (!TextUtils.isEmpty(choicesStr)) {
            String[] choices = choicesStr.split(",");
            choicesLayout.removeAllViews();
            
            for (int i = 0; i < choices.length; i++) {
                TextView choiceView = new TextView(this);
                choiceView.setText((i + 1) + ". " + choices[i].trim());
                choiceView.setTextSize(14);
                // Use app's font family
                try {
                    android.graphics.Typeface typeface = android.graphics.Typeface.createFromAsset(getAssets(), "fonts/paytone_one.ttf");
                    choiceView.setTypeface(typeface);
                } catch (Exception e) {
                    // Fallback to default if font not found
                    choiceView.setTypeface(null, android.graphics.Typeface.NORMAL);
                }
                choiceView.setTextColor(0xFF5DB575);
                choiceView.setPadding(8, 4, 8, 4);
                
                // Highlight correct answer
                if (choices[i].trim().equals(correctAnswer)) {
                    choiceView.setTextColor(0xFF204520);
                    choiceView.setTypeface(choiceView.getTypeface(), android.graphics.Typeface.BOLD);
                }
                
                choicesLayout.addView(choiceView);
            }
        }
        
        questionsLayout.addView(questionView);
    }

    private String getOperationSymbol(String operation) {
        if (operation == null) return "+";
        switch (operation.toLowerCase()) {
            case "addition": return "+";
            case "subtraction": return "-";
            case "multiplication": return "×";
            case "division": return "÷";
            default: return "+";
        }
    }

    private void showEmptyState(String message) {
        emptyStateText.setText(message);
        emptyStateText.setVisibility(View.VISIBLE);
        questionsLayout.setVisibility(View.GONE);
    }

    private void playSound(String fileName) {
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
}

