package com.happym.mathsquare.utils;

import android.content.Context;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.happym.mathsquare.sharedPreferences;
import java.util.ArrayList;
import java.util.List;

/**
 * Tracks practice module completion for quiz prerequisites
 * Implements defense comment #5: Quiz - Path - should pass (avg) 1 module before taking the test
 */
public class PracticeProgressTracker {
    
    private FirebaseFirestore db;
    private Context context;
    
    // Minimum average score required to take quiz (out of 10)
    private static final double MIN_AVERAGE_SCORE = 7.0; // 70% average
    
    public PracticeProgressTracker(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Check if student has passed practice modules (average score >= 70%)
     * Returns true if student can take quiz
     */
    public void canTakeQuiz(QuizAccessCallback callback) {
        String firstName = sharedPreferences.getFirstN(context);
        String lastName = sharedPreferences.getLastN(context);
        String grade = sharedPreferences.getGrade(context);
        
        if (firstName == null || lastName == null || grade == null) {
            callback.onResult(false, "Student information not found");
            return;
        }
        
        // Query all practice attempts for this student
        db.collection("Accounts")
            .document("Students")
            .collection("MathSquare")
            .whereEqualTo("firstName", firstName)
            .whereEqualTo("lastName", lastName)
            .whereEqualTo("gameType", "Practice")
            .whereEqualTo("grade", grade)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<Double> scores = new ArrayList<>();
                    
                    for (var doc : task.getResult()) {
                        String scoreStr = doc.getString("quizscore");
                        if (scoreStr != null) {
                            try {
                                double score = Double.parseDouble(scoreStr);
                                scores.add(score);
                            } catch (NumberFormatException e) {
                                // Skip invalid scores
                            }
                        }
                    }
                    
                    if (scores.isEmpty()) {
                        callback.onResult(false, "No practice attempts found. Please complete practice modules first.");
                        return;
                    }
                    
                    // Calculate average
                    double sum = 0;
                    for (Double score : scores) {
                        sum += score;
                    }
                    double average = sum / scores.size();
                    
                    boolean canTake = average >= MIN_AVERAGE_SCORE;
                    String message = canTake 
                        ? "You can take the quiz!" 
                        : String.format("Average practice score: %.1f/10. You need at least %.1f/10 to take the quiz.", 
                            average, MIN_AVERAGE_SCORE);
                    
                    callback.onResult(canTake, message);
                } else {
                    callback.onResult(false, "Error checking practice progress: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                }
            });
    }
    
    /**
     * Get average practice score
     */
    public void getAverageScore(ScoreCallback callback) {
        String firstName = sharedPreferences.getFirstN(context);
        String lastName = sharedPreferences.getLastN(context);
        String grade = sharedPreferences.getGrade(context);
        
        if (firstName == null || lastName == null || grade == null) {
            callback.onScore(0.0, 0);
            return;
        }
        
        db.collection("Accounts")
            .document("Students")
            .collection("MathSquare")
            .whereEqualTo("firstName", firstName)
            .whereEqualTo("lastName", lastName)
            .whereEqualTo("gameType", "Practice")
            .whereEqualTo("grade", grade)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<Double> scores = new ArrayList<>();
                    
                    for (var doc : task.getResult()) {
                        String scoreStr = doc.getString("quizscore");
                        if (scoreStr != null) {
                            try {
                                scores.add(Double.parseDouble(scoreStr));
                            } catch (NumberFormatException e) {
                                // Skip invalid scores
                            }
                        }
                    }
                    
                    if (scores.isEmpty()) {
                        callback.onScore(0.0, 0);
                        return;
                    }
                    
                    double sum = 0;
                    for (Double score : scores) {
                        sum += score;
                    }
                    double average = sum / scores.size();
                    
                    callback.onScore(average, scores.size());
                } else {
                    callback.onScore(0.0, 0);
                }
            });
    }
    
    public interface QuizAccessCallback {
        void onResult(boolean canTake, String message);
    }
    
    public interface ScoreCallback {
        void onScore(double average, int attemptCount);
    }
}

