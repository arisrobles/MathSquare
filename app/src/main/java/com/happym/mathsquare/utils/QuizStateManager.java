package com.happym.mathsquare.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.happym.mathsquare.MathProblem;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages quiz state persistence for resume functionality
 * Saves and restores quiz progress for both CSV and Firebase quizzes
 */
public class QuizStateManager {
    
    private static final String PREFS_NAME = "QuizStatePrefs";
    private static final String KEY_QUIZ_ID = "quiz_id";
    private static final String KEY_CURRENT_QUESTION_INDEX = "current_question_index";
    private static final String KEY_SCORE = "score";
    private static final String KEY_TIME_LEFT = "time_left";
    private static final String KEY_IS_FIREBASE_QUIZ = "is_firebase_quiz";
    private static final String KEY_CURRENT_OPERATION = "current_operation";
    private static final String KEY_USED_OPERATIONS = "used_operations";
    private static final String KEY_ANSWERED_COUNT = "answered_count";
    private static final String KEY_HEART_LIMIT = "heart_limit";
    
    private SharedPreferences prefs;
    private Context context;
    
    public QuizStateManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Save quiz state
     */
    public void saveQuizState(String quizId, int currentQuestionIndex, int score, 
                             long timeLeftInMillis, boolean isFirebaseQuiz,
                             String currentOperation, Set<String> usedOperations,
                             int heartLimit) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_QUIZ_ID, quizId);
        editor.putInt(KEY_CURRENT_QUESTION_INDEX, currentQuestionIndex);
        editor.putInt(KEY_SCORE, score);
        editor.putLong(KEY_TIME_LEFT, timeLeftInMillis);
        editor.putBoolean(KEY_IS_FIREBASE_QUIZ, isFirebaseQuiz);
        editor.putString(KEY_CURRENT_OPERATION, currentOperation);
        editor.putStringSet(KEY_USED_OPERATIONS, usedOperations != null ? usedOperations : new HashSet<>());
        editor.putInt(KEY_ANSWERED_COUNT, currentQuestionIndex); // Track how many questions answered
        editor.putInt(KEY_HEART_LIMIT, heartLimit);
        editor.apply();
    }
    
    /**
     * Load quiz state
     */
    public QuizState loadQuizState(String quizId) {
        String savedQuizId = prefs.getString(KEY_QUIZ_ID, null);
        
        // Only restore if it's the same quiz
        if (savedQuizId == null || !savedQuizId.equals(quizId)) {
            return null; // No saved state for this quiz
        }
        
        int currentQuestionIndex = prefs.getInt(KEY_CURRENT_QUESTION_INDEX, 0);
        int score = prefs.getInt(KEY_SCORE, 0);
        long timeLeftInMillis = prefs.getLong(KEY_TIME_LEFT, 0);
        boolean isFirebaseQuiz = prefs.getBoolean(KEY_IS_FIREBASE_QUIZ, false);
        String currentOperation = prefs.getString(KEY_CURRENT_OPERATION, null);
        Set<String> usedOperations = prefs.getStringSet(KEY_USED_OPERATIONS, new HashSet<>());
        int heartLimit = prefs.getInt(KEY_HEART_LIMIT, 3);
        
        return new QuizState(currentQuestionIndex, score, timeLeftInMillis, isFirebaseQuiz,
                           currentOperation, usedOperations, heartLimit);
    }
    
    /**
     * Clear saved quiz state
     */
    public void clearQuizState(String quizId) {
        String savedQuizId = prefs.getString(KEY_QUIZ_ID, null);
        if (savedQuizId != null && savedQuizId.equals(quizId)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(KEY_QUIZ_ID);
            editor.remove(KEY_CURRENT_QUESTION_INDEX);
            editor.remove(KEY_SCORE);
            editor.remove(KEY_TIME_LEFT);
            editor.remove(KEY_IS_FIREBASE_QUIZ);
            editor.remove(KEY_CURRENT_OPERATION);
            editor.remove(KEY_USED_OPERATIONS);
            editor.remove(KEY_ANSWERED_COUNT);
            editor.remove(KEY_HEART_LIMIT);
            editor.apply();
        }
    }
    
    /**
     * Check if there's a saved state for a quiz
     */
    public boolean hasSavedState(String quizId) {
        String savedQuizId = prefs.getString(KEY_QUIZ_ID, null);
        return savedQuizId != null && savedQuizId.equals(quizId);
    }
    
    /**
     * Quiz state data class
     */
    public static class QuizState {
        public int currentQuestionIndex;
        public int score;
        public long timeLeftInMillis;
        public boolean isFirebaseQuiz;
        public String currentOperation;
        public Set<String> usedOperations;
        public int heartLimit;
        
        public QuizState(int currentQuestionIndex, int score, long timeLeftInMillis,
                        boolean isFirebaseQuiz, String currentOperation,
                        Set<String> usedOperations, int heartLimit) {
            this.currentQuestionIndex = currentQuestionIndex;
            this.score = score;
            this.timeLeftInMillis = timeLeftInMillis;
            this.isFirebaseQuiz = isFirebaseQuiz;
            this.currentOperation = currentOperation;
            this.usedOperations = usedOperations != null ? new HashSet<>(usedOperations) : new HashSet<>();
            this.heartLimit = heartLimit;
        }
    }
}

