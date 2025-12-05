package com.happym.mathsquare;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

/**
 * Helper class to track used questions across levels to avoid repetition
 */
public class QuestionTracker {
    private static final String PREF_NAME = "question_tracker";
    private static final String KEY_PREFIX = "used_questions_";
    
    private SharedPreferences sharedPreferences;
    private Context context;
    
    public QuestionTracker(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Get the key for storing used questions for a specific operation and difficulty
     */
    private String getKey(String operation, String difficulty) {
        return KEY_PREFIX + operation.toLowerCase() + "_" + difficulty.toLowerCase();
    }
    
    /**
     * Get set of used question IDs for a specific operation and difficulty
     */
    public Set<String> getUsedQuestions(String operation, String difficulty) {
        String key = getKey(operation, difficulty);
        return sharedPreferences.getStringSet(key, new HashSet<>());
    }
    
    /**
     * Add a question ID to the used questions set
     */
    public void addUsedQuestion(String operation, String difficulty, String questionId) {
        String key = getKey(operation, difficulty);
        Set<String> usedQuestions = new HashSet<>(getUsedQuestions(operation, difficulty));
        usedQuestions.add(questionId);
        
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(key, usedQuestions);
        editor.apply();
    }
    
    /**
     * Add multiple question IDs to the used questions set
     */
    public void addUsedQuestions(String operation, String difficulty, Set<String> questionIds) {
        String key = getKey(operation, difficulty);
        Set<String> usedQuestions = new HashSet<>(getUsedQuestions(operation, difficulty));
        usedQuestions.addAll(questionIds);
        
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(key, usedQuestions);
        editor.apply();
    }
    
    /**
     * Check if a question has been used
     */
    public boolean isQuestionUsed(String operation, String difficulty, String questionId) {
        Set<String> usedQuestions = getUsedQuestions(operation, difficulty);
        return usedQuestions.contains(questionId);
    }
    
    /**
     * Clear used questions for a specific operation and difficulty
     * (useful for testing or resetting progress)
     */
    public void clearUsedQuestions(String operation, String difficulty) {
        String key = getKey(operation, difficulty);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }
    
    /**
     * Clear all used questions (reset all progress)
     */
    public void clearAllUsedQuestions() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    
    /**
     * Get count of used questions for a specific operation and difficulty
     */
    public int getUsedQuestionsCount(String operation, String difficulty) {
        return getUsedQuestions(operation, difficulty).size();
    }
    
    /**
     * Generate a unique question ID from a MathProblem
     */
    public static String generateQuestionId(MathProblem problem) {
        return problem.getQuestion() + "_" + problem.getAnswer();
    }
}
