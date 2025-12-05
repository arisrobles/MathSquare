package com.happym.mathsquare.utils;

import android.content.Context;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.happym.mathsquare.sharedPreferences;
import java.util.ArrayList;
import java.util.List;

/**
 * Tracks quiz completion for sequential quiz unlocking
 * Implements defense comment #4 & #5: Quiz - Path - quizzes locked until previous ones completed
 */
public class QuizProgressTracker {
    
    private FirebaseFirestore db;
    private Context context;
    
    // Minimum score to pass a quiz (out of 10)
    private static final double MIN_PASSING_SCORE = 7.0; // 70%
    
    public QuizProgressTracker(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Check if student can access a specific quiz
     * Quiz 1 is always accessible
     * Quiz N requires Quiz N-1 to be completed with passing score
     */
    public void canAccessQuiz(String quizId, QuizAccessCallback callback) {
        String firstName = sharedPreferences.getFirstN(context);
        String lastName = sharedPreferences.getLastN(context);
        String grade = sharedPreferences.getGrade(context);
        
        if (firstName == null || lastName == null || grade == null) {
            callback.onResult(false, "Student information not found");
            return;
        }
        
        // Extract quiz number from quizId (e.g., "quiz_1" -> 1)
        int quizNumber = extractQuizNumber(quizId);
        
        // Quiz 1 is always accessible
        if (quizNumber <= 1) {
            callback.onResult(true, "You can take this quiz!");
            return;
        }
        
        // Check if previous quiz is completed with passing score
        String previousQuizId = "quiz_" + (quizNumber - 1);
        checkPreviousQuizCompletion(firstName, lastName, grade, previousQuizId, quizNumber - 1, callback);
    }
    
    /**
     * Extract quiz number from quizId string
     */
    private int extractQuizNumber(String quizId) {
        try {
            if (quizId != null && quizId.startsWith("quiz_")) {
                String numberStr = quizId.replace("quiz_", "");
                return Integer.parseInt(numberStr);
            }
        } catch (NumberFormatException e) {
            // Return 1 as default
        }
        return 1;
    }
    
    /**
     * Check if previous quiz was completed with passing score
     */
    private void checkPreviousQuizCompletion(String firstName, String lastName, String grade,
                                            String previousQuizId, int previousQuizNumber,
                                            QuizAccessCallback callback) {
        // Query all attempts for the previous quiz
        db.collection("Accounts")
            .document("Students")
            .collection("MathSquare")
            .whereEqualTo("firstName", firstName)
            .whereEqualTo("lastName", lastName)
            .whereEqualTo("gameType", "Quiz")
            .whereEqualTo("quizno_int", previousQuizNumber)
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
                        callback.onResult(false, 
                            "Please complete Quiz " + previousQuizNumber + " first with a passing score (7/10 or higher)");
                        return;
                    }
                    
                    // Check if any attempt has passing score
                    boolean hasPassingScore = false;
                    for (Double score : scores) {
                        if (score >= MIN_PASSING_SCORE) {
                            hasPassingScore = true;
                            break;
                        }
                    }
                    
                    if (hasPassingScore) {
                        callback.onResult(true, "You can take this quiz!");
                    } else {
                        double bestScore = scores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                        callback.onResult(false, 
                            String.format("You need to pass Quiz %d first (Best score: %.1f/10, Required: %.1f/10)", 
                                previousQuizNumber, bestScore, MIN_PASSING_SCORE));
                    }
                } else {
                    callback.onResult(false, 
                        "Please complete Quiz " + previousQuizNumber + " first");
                }
            });
    }
    
    /**
     * Get the highest score achieved for a specific quiz
     */
    public void getBestQuizScore(String quizId, ScoreCallback callback) {
        String firstName = sharedPreferences.getFirstN(context);
        String lastName = sharedPreferences.getLastN(context);
        String grade = sharedPreferences.getGrade(context);
        
        if (firstName == null || lastName == null || grade == null) {
            callback.onScore(0.0);
            return;
        }
        
        int quizNumber = extractQuizNumber(quizId);
        
        db.collection("Accounts")
            .document("Students")
            .collection("MathSquare")
            .whereEqualTo("firstName", firstName)
            .whereEqualTo("lastName", lastName)
            .whereEqualTo("gameType", "Quiz")
            .whereEqualTo("quizno_int", quizNumber)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    double bestScore = 0.0;
                    
                    for (var doc : task.getResult()) {
                        String scoreStr = doc.getString("quizscore");
                        if (scoreStr != null) {
                            try {
                                double score = Double.parseDouble(scoreStr);
                                if (score > bestScore) {
                                    bestScore = score;
                                }
                            } catch (NumberFormatException e) {
                                // Skip invalid scores
                            }
                        }
                    }
                    
                    callback.onScore(bestScore);
                } else {
                    callback.onScore(0.0);
                }
            });
    }
    
    public interface QuizAccessCallback {
        void onResult(boolean canAccess, String message);
    }
    
    public interface ScoreCallback {
        void onScore(double score);
    }
}

