package com.happym.mathsquare;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.WindowCompat;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.view.animation.BounceInterpolator;
import java.util.Random;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;

// import androidx.activity.EdgeToEdge;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.view.animation.BounceInterpolator;
import java.io.IOException;
import java.util.Random;

import com.happym.mathsquare.Animation.*;
import com.happym.mathsquare.utils.PracticeProgressTracker;
import com.happym.mathsquare.utils.QuizProgressTracker;

public class QuizzesSection extends AppCompatActivity {

    private FirebaseFirestore db;
    private MediaPlayer soundEffectPlayer;
    private String quizType = "quiz";
    private String firstCreatedQuizId = null; // Store the ID of the first created Firebase quiz
    private FrameLayout numberContainer, backgroundFrame;
    private final Random random = new Random();
    private NumBGAnimation numBGAnimation;
    private QuizTileAdapter quizTileAdapter;
    private TextView emptyStateText;
    private RecyclerView quizzesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.layout_quizzes_section); // your layout file

        FirebaseApp.initializeApp(this);

        // Get UI elements - RecyclerView for grid layout
        quizzesRecyclerView = findViewById(R.id.quizzes_recycler_view);
        emptyStateText = findViewById(R.id.emptyStateText);
        
        // Set up GridLayoutManager with 3 columns
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        quizzesRecyclerView.setLayoutManager(layoutManager);
        
        // Create adapter for quiz tiles
        quizTileAdapter = new QuizTileAdapter();
        quizzesRecyclerView.setAdapter(quizTileAdapter);
        
        // Initially show empty state (will be hidden when quizzes are loaded)
        updateEmptyState();

        // Firestore instance
        db = FirebaseFirestore.getInstance();

        // Get student's grade from SharedPreferences
        String studentGrade = sharedPreferences.getGrade(this);

        // Determine quiz level based on grade
        String quizLevel = "1"; // default
        String quizId = "quiz_1"; // default
        String difficulty = "Easy"; // default

        if (studentGrade != null) {
            switch (studentGrade) {
                case "1":
                    quizLevel = "1";
                    quizId = "quiz_1";
                    difficulty = "Easy";
                    break;
                case "2":
                    quizLevel = "2";
                    quizId = "quiz_2";
                    difficulty = "Easy";
                    break;
                case "3":
                    quizLevel = "3";
                    quizId = "quiz_3";
                    difficulty = "Medium";
                    break;
                case "4":
                    quizLevel = "4";
                    quizId = "quiz_4";
                    difficulty = "Medium";
                    break;
                case "5":
                    quizLevel = "5";
                    quizId = "quiz_5";
                    difficulty = "Medium";
                    break;
                case "6":
                    quizLevel = "6";
                    quizId = "quiz_6";
                    difficulty = "Hard";
                    break;
                default:
                    quizLevel = "1";
                    quizId = "quiz_1";
                    difficulty = "Easy";
                    break;
            }
        }

        // Get student's section
        String studentSection = sharedPreferences.getSection(this);
        
        // ALWAYS add default CSV quiz as a tile first (independent, works alone)
        addCSVQuizTile(quizId, difficulty);
        
        // SEPARATELY check for Firebase custom quizzes (independent fetching logic)
        // Each Firebase quiz will be added as a separate tile using the same layout/design/flow
        loadFirebaseQuizzes(studentGrade, studentSection);

        backgroundFrame = findViewById(R.id.main);
        numberContainer = findViewById(R.id.number_container); // Get FrameLayout from XML

        numBGAnimation = new NumBGAnimation(this, numberContainer);
        numBGAnimation.startNumberAnimationLoop();

        backgroundFrame.post(
                () -> {
                    VignetteEffect.apply(this, backgroundFrame);
                });
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

    private void setupStartQuizButton(AppCompatButton startButton, String quizId, String difficulty) {
        Log.d("QuizAccess", "🔍 Setting up quiz button for: " + quizId);
        
        // Get student info for logging
        String firstName = sharedPreferences.getFirstN(this);
        String lastName = sharedPreferences.getLastN(this);
        String grade = sharedPreferences.getGrade(this);
        Log.d("QuizAccess", "  - Student: " + firstName + " " + lastName + ", Grade: " + grade);
        
        // For default CSV quizzes (quiz_1, quiz_2, etc.), skip Firebase status check
        // They should only be controlled by sequential locking
        // Only check Firebase status for custom teacher quizzes (which have Firebase document IDs)
        boolean isDefaultCSVQuiz = quizId != null && quizId.startsWith("quiz_") && quizId.matches("quiz_\\d+");
        
        if (!isDefaultCSVQuiz) {
            // This is a Firebase quiz, check its status
            fetchQuizStatus(quizId, startButton);
        } else {
            // Default CSV quiz - set button to enabled state initially
            startButton.setBackgroundResource(R.drawable.btn_playgame);
            startButton.setText("START QUIZ");
            startButton.setClickable(true);
            startButton.setEnabled(true);
            startButton.setAlpha(1.0f);
        }

        // Check grade-based quiz access (students can only access quizzes matching their grade)
        QuizProgressTracker quizTracker = new QuizProgressTracker(this);
        quizTracker.canAccessQuiz(quizId, (canAccess, message) -> {
            Log.d("QuizAccess", "  - Grade-based check result: " + (canAccess ? "✅ Accessible" : "❌ Locked"));
            Log.d("QuizAccess", "  - Message: " + message);
            
            if (!canAccess) {
                // Lock the quiz button
                Log.w("QuizAccess", "⚠️ Quiz LOCKED due to: " + message);
                startButton.setBackgroundResource(R.drawable.btn_exitgame);
                startButton.setText("QUIZ LOCKED");
                startButton.setClickable(false);
                startButton.setEnabled(false);
                startButton.setAlpha(0.6f);
                // Show tooltip or message about why it's locked
                startButton.setOnClickListener(v -> {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                });
            } else {
                // Quiz is accessible, set up normal click listener
                Log.d("QuizAccess", "✅ Quiz is accessible, setting up click listener");
                // For default CSV quizzes, ensure button is enabled
                if (isDefaultCSVQuiz) {
                    startButton.setBackgroundResource(R.drawable.btn_playgame);
                    startButton.setText("START QUIZ");
                    startButton.setClickable(true);
                    startButton.setEnabled(true);
                    startButton.setAlpha(1.0f);
                }
                setupQuizClickListener(startButton, quizId, difficulty);
            }
        });
    }
    
    private void setupQuizClickListener(AppCompatButton startButton, String quizId, String difficulty) {
        // Set click listener with practice prerequisite check
        startButton.setOnClickListener(
                view -> {
                    if (startButton.isEnabled()) {
                        playSound("click.mp3");

                        // Check if student has passed practice modules (defense comment #5)
                        // NOTE: Quiz 1 should be accessible even without practice, but practice is recommended
                        int quizNumber = extractQuizNumberFromId(quizId);
                        boolean isQuiz1 = quizNumber <= 1;
                        
                        Log.d("QuizAccess", "🎯 Starting quiz: " + quizId + " (Quiz #" + quizNumber + ")");
                        
                        PracticeProgressTracker progressTracker = new PracticeProgressTracker(view.getContext());
                        progressTracker.canTakeQuiz((canTake, message) -> {
                            Log.d("QuizAccess", "  - Practice check result: " + (canTake ? "✅ Passed" : "❌ Not passed"));
                            Log.d("QuizAccess", "  - Practice message: " + message);
                            
                            // For Quiz 1, allow even if practice not passed (but show warning)
                            if (!canTake) {
                                if (isQuiz1) {
                                    Log.d("QuizAccess", "  - Quiz 1: Allowing despite practice requirement (recommended but not required)");
                                    // Continue anyway for Quiz 1, but show info message
                                    Toast.makeText(view.getContext(), 
                                        "Note: Practice is recommended. " + message, 
                                        Toast.LENGTH_LONG).show();
                                } else {
                                    // For Quiz 2+, practice is required
                                    Log.w("QuizAccess", "  - Quiz " + quizNumber + ": Practice requirement NOT met, blocking quiz");
                                    Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }
                            
                            // Also check grade-based quiz access (double-check)
                            QuizProgressTracker quizTracker = new QuizProgressTracker(view.getContext());
                            quizTracker.canAccessQuiz(quizId, (canAccess, accessMessage) -> {
                                Log.d("QuizAccess", "  - Final grade-based check: " + (canAccess ? "✅ Accessible" : "❌ Locked"));
                                if (!canAccess) {
                                    Log.w("QuizAccess", "⚠️ Quiz blocked by grade restriction: " + accessMessage);
                                    Toast.makeText(view.getContext(), accessMessage, Toast.LENGTH_LONG).show();
                                    return;
                                }
                                
                                Log.d("QuizAccess", "✅ All checks passed! Starting quiz...");
                            
                            // Proceed with quiz
                        Intent intent = new Intent(view.getContext(), MultipleChoicePage.class);

                        // Set operations based on difficulty
                        if (difficulty.equals("Easy")) {
                            // For grade 1 and 2 (Easy difficulty), only use addition and subtraction
                            String[] operations = {"Addition", "Subtraction"};
                            List<String> operationList = new ArrayList<>(Arrays.asList(operations));
                            Collections.shuffle(operationList);
                            intent.putStringArrayListExtra("operationList", new ArrayList<>(operationList));
                        } else if (difficulty.equals("Medium")) {
                            // For medium grades, use all basic operations
                            String[] operations = {"Addition", "Multiplication", "Division", "Subtraction"};
                            List<String> operationList = new ArrayList<>(Arrays.asList(operations));
                            Collections.shuffle(operationList);
                            intent.putStringArrayListExtra("operationList", new ArrayList<>(operationList));
                        } else if (difficulty.equals("Hard")) {
                            // For higher grades (Quiz 5 and 6), use all operations plus decimals and percentage
                            String[] operations = {"Addition", "Multiplication", "Division", "Subtraction", "Decimals", "Percentage"};
                            List<String> operationList = new ArrayList<>(Arrays.asList(operations));
                            Collections.shuffle(operationList);
                            intent.putStringArrayListExtra("operationList", new ArrayList<>(operationList));
                        } else {
                            // Default to addition and subtraction only
                            String[] operations = {"Addition", "Subtraction"};
                            List<String> operationList = new ArrayList<>(Arrays.asList(operations));
                            Collections.shuffle(operationList);
                            intent.putStringArrayListExtra("operationList", new ArrayList<>(operationList));
                        }

                        intent.putExtra("quizId", quizId);
                        intent.putExtra("difficulty", difficulty);
                        intent.putExtra("game_type", "Quiz");

                        view.getContext().startActivity(intent);
                            });
                        });
                    }
                });
    }

    private com.google.firebase.firestore.ListenerRegistration quizStatusListener;
    
    /** Fetches quiz status from Firestore with REAL-TIME listener and enables/disables the button accordingly. */
    private void fetchQuizStatus(String quizId, AppCompatButton quizButton) {
        // Remove any existing listener first
        if (quizStatusListener != null) {
            quizStatusListener.remove();
        }
        
        // Set up REAL-TIME listener for quiz status
        quizStatusListener = db.collection("Quizzes")
                .document("Status")
                .collection(quizId)
                .document("status")
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.e("Quiz", "Error listening to quiz status for " + quizId, e);
                        // Default to closed on error
                        quizButton.setBackgroundResource(R.drawable.btn_exitgame);
                        quizButton.setText("QUIZ UNAVAILABLE");
                        quizButton.setClickable(false);
                        quizButton.setEnabled(false);
                        return;
                    }
                    
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                                String status = documentSnapshot.getString("status");
                                if ("closed".equalsIgnoreCase(status)) {
                                    quizButton.setBackgroundResource(R.drawable.btn_exitgame);
                                    quizButton.setText("QUIZ CLOSED");
                                    quizButton.setClickable(false);
                                    quizButton.setEnabled(false);
                            Log.d("Quiz", quizId + " is closed (real-time update)");
                                } else {
                                    quizButton.setBackgroundResource(R.drawable.btn_playgame);
                                    quizButton.setText("START QUIZ");
                                    quizButton.setClickable(true);
                                    quizButton.setEnabled(true);
                            Log.d("Quiz", quizId + " is open (real-time update)");
                                }
                            } else {
                                Log.d("Quiz", quizId + " status document does not exist");
                                // Default to open if no status document exists
                                quizButton.setBackgroundResource(R.drawable.btn_playgame);
                                quizButton.setText("START QUIZ");
                                quizButton.setClickable(true);
                                quizButton.setEnabled(true);
                            }
                });
    }
    
    /**
     * Extract quiz number from quizId string (e.g., "quiz_1" -> 1)
     */
    private int extractQuizNumberFromId(String quizId) {
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
     * Load Firebase custom quizzes for student's grade and section
     * SEPARATE fetching logic from CSV quizzes
     * Each quiz will be added as a tile using the same layout/design/flow
     */
    private void loadFirebaseQuizzes(String studentGrade, String studentSection) {
        
        if (studentGrade == null) {
            // No student grade, skip Firebase quiz loading (CSV quiz already added)
            Log.d("QuizzesSection", "No student grade, skipping Firebase quiz loading");
            return;
        }
        
        // Query Firebase for active quizzes matching grade
        // Note: Firestore doesn't support OR queries easily, so we'll query and filter client-side
        Log.d("QuizzesSection", "Loading Firebase quizzes for Grade: " + studentGrade + ", Section: " + studentSection);
        
        // Query all quizzes (we'll filter by grade and section client-side for better control)
        // This ensures we don't miss quizzes due to format mismatches
        com.google.firebase.firestore.Query query = db.collection("TeacherQuizzes");
        
        query.get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    java.util.List<com.google.firebase.firestore.QueryDocumentSnapshot> activeQuizzes = new java.util.ArrayList<>();
                    java.util.Date now = new java.util.Date();
                    
                    int totalQuizzes = task.getResult().size();
                    Log.d("QuizzesSection", "Found " + totalQuizzes + " total quiz(es) in Firebase");
                    
                    // Filter by grade, section, and schedule
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : task.getResult()) {
                        String quizTitle = doc.getString("quizTitle");
                        String quizGrade = doc.getString("grade");
                        String quizSection = doc.getString("section");
                        
                        Log.d("QuizzesSection", "Checking quiz: " + quizTitle + " (Grade: " + quizGrade + ", Section: " + quizSection + ")");
                        
                        // Check grade match (handle potential format differences)
                        boolean gradeMatches = false;
                        if (quizGrade != null && studentGrade != null) {
                            // Normalize both to string and compare
                            String normalizedQuizGrade = quizGrade.trim();
                            String normalizedStudentGrade = studentGrade.trim();
                            gradeMatches = normalizedQuizGrade.equals(normalizedStudentGrade);
                            
                            // Also try numeric comparison if both are numeric
                            if (!gradeMatches) {
                                try {
                                    int quizGradeNum = Integer.parseInt(normalizedQuizGrade);
                                    int studentGradeNum = Integer.parseInt(normalizedStudentGrade);
                                    gradeMatches = (quizGradeNum == studentGradeNum);
                                } catch (NumberFormatException e) {
                                    // Not numeric, skip
                                }
                            }
                        }
                        
                        if (!gradeMatches) {
                            Log.d("QuizzesSection", "  ❌ Quiz skipped: Grade mismatch (Quiz: " + quizGrade + ", Student: " + studentGrade + ")");
                            continue; // Skip this quiz
                        }
                        
                        Log.d("QuizzesSection", "  ✅ Grade matches!");
                        
                        // Check section match
                        boolean sectionMatches = false;
                        
                        if (studentSection != null && !studentSection.trim().isEmpty()) {
                            // Student has section - match if quiz is for their section or "All Sections"
                            sectionMatches = studentSection.equals(quizSection) || 
                                           "All Sections".equals(quizSection);
                            Log.d("QuizzesSection", "  Section match: " + sectionMatches + " (Student: " + studentSection + ", Quiz: " + quizSection + ")");
                        } else {
                            // Student has no section - only show "All Sections" quizzes
                            sectionMatches = "All Sections".equals(quizSection);
                            Log.d("QuizzesSection", "  Section match (no student section): " + sectionMatches + " (Quiz: " + quizSection + ")");
                        }
                        
                        if (!sectionMatches) {
                            Log.d("QuizzesSection", "  ❌ Quiz skipped: Section mismatch");
                            continue; // Skip this quiz
                        }
                        
                        // Check schedule - show all quizzes regardless of schedule for now
                        // (Teachers can control access via schedule, but we'll show them all)
                        com.google.firebase.Timestamp startTimestamp = doc.getTimestamp("startDateTime");
                        com.google.firebase.Timestamp endTimestamp = doc.getTimestamp("endDateTime");
                        
                        if (startTimestamp != null && endTimestamp != null) {
                            java.util.Date startDate = startTimestamp.toDate();
                            java.util.Date endDate = endTimestamp.toDate();
                            
                            Log.d("QuizzesSection", "  Schedule: " + startDate + " to " + endDate);
                            Log.d("QuizzesSection", "  Current time: " + now);
                            
                            // Check if quiz is currently active (between start and end dates) or scheduled for future
                            boolean isActive = now.after(startDate) && now.before(endDate);
                            boolean isFuture = now.before(startDate);
                            boolean isPast = now.after(endDate);
                            
                            Log.d("QuizzesSection", "  Active: " + isActive + ", Future: " + isFuture + ", Past: " + isPast);
                            
                            // Show ALL quizzes (active, future, and past) - let the button handle enabling/disabling
                            // This ensures students can see all quizzes created for them
                            Log.d("QuizzesSection", "  ✅ Quiz found, adding to list (schedule will be checked when starting)");
                            activeQuizzes.add(doc);
                        } else {
                            // If no schedule, show the quiz anyway (for backwards compatibility or unscheduled quizzes)
                            Log.d("QuizzesSection", "  ⚠️ Quiz has no schedule, showing anyway");
                            activeQuizzes.add(doc);
                        }
                    }
                    
                    Log.d("QuizzesSection", "Total active quizzes after filtering: " + activeQuizzes.size());
                    
                    if (!activeQuizzes.isEmpty()) {
                        // Sort quizzes by creation time (oldest first) to identify the first created quiz
                        activeQuizzes.sort((doc1, doc2) -> {
                            com.google.firebase.Timestamp createdAt1 = doc1.getTimestamp("createdAt");
                            com.google.firebase.Timestamp createdAt2 = doc2.getTimestamp("createdAt");
                            
                            // Handle null timestamps (shouldn't happen, but defensive)
                            if (createdAt1 == null && createdAt2 == null) return 0;
                            if (createdAt1 == null) return 1; // null goes to end
                            if (createdAt2 == null) return -1; // null goes to end
                            
                            // Compare timestamps (older = smaller = first)
                            return createdAt1.compareTo(createdAt2);
                        });
                        
                        // Store the first created quiz's ID (oldest = first in sorted list)
                        firstCreatedQuizId = activeQuizzes.get(0).getId();
                        Log.d("QuizzesSection", "📌 First created quiz ID: " + firstCreatedQuizId + " (Title: " + activeQuizzes.get(0).getString("quizTitle") + ")");
                        
                        // Add each Firebase quiz as a separate tile (CSV quiz already added)
                        Log.d("QuizzesSection", "Adding " + activeQuizzes.size() + " Firebase quiz(es) as tiles");
                        for (com.google.firebase.firestore.QueryDocumentSnapshot quizDoc : activeQuizzes) {
                            addFirebaseQuizTile(quizDoc);
                        }
                    } else {
                        // No active Firebase quizzes - CSV quiz tile already displayed
                        Log.d("QuizzesSection", "No active Firebase quizzes found - only CSV quiz tile displayed");
                        firstCreatedQuizId = null; // Reset if no quizzes
                    }
                    
                    // Update empty state after loading all quizzes
                    updateEmptyState();
                } else {
                    // Error loading Firebase quizzes - CSV quiz tile already displayed
                    Log.e("QuizzesSection", "Error loading Firebase quizzes", task.getException());
                    // CSV quiz already added, so no need to do anything
                    updateEmptyState();
                }
            });
    }
    
    /**
     * Add CSV quiz as a tile
     * CSV quizzes are always accessible (default quiz for each grade from CSV data)
     * Labeled as "Quiz No. 1" since it's the default quiz
     */
    private void addCSVQuizTile(String quizId, String difficulty) {
        QuizTileItem item = new QuizTileItem();
        item.isCSVQuiz = true;
        item.quizId = quizId;
        item.difficulty = difficulty;
        item.quizNumber = "1"; // Always "1" for CSV/default quiz
        item.quizTitle = "Quiz No. 1"; // Label as Quiz No. 1
        
        if (quizTileAdapter != null) {
            quizTileAdapter.addQuiz(item);
        }
    }
    
    /**
     * Add Firebase quiz as a tile
     */
    private void addFirebaseQuizTile(com.google.firebase.firestore.QueryDocumentSnapshot quizDoc) {
        QuizTileItem item = new QuizTileItem();
        item.isCSVQuiz = false;
        item.quizDoc = quizDoc;
        item.quizId = quizDoc.getId();
        item.quizNumber = quizDoc.getString("quizNumber");
        item.quizTitle = quizDoc.getString("quizTitle");
        
        if (quizTileAdapter != null) {
            quizTileAdapter.addQuiz(item);
        }
        updateEmptyState();
    }
    
    /**
     * Update empty state visibility based on quiz count
     */
    private void updateEmptyState() {
        if (emptyStateText != null && quizzesRecyclerView != null && quizTileAdapter != null) {
            int quizCount = quizTileAdapter.getQuizCount();
            if (quizCount == 0) {
                emptyStateText.setVisibility(View.VISIBLE);
                quizzesRecyclerView.setVisibility(View.GONE);
            } else {
                emptyStateText.setVisibility(View.GONE);
                quizzesRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
    
    /**
     * Start CSV quiz - always accessible (no restrictions, no tutorial check)
     * CSV quizzes are opened by default and should remain accessible
     */
    private void startCSVQuiz(String quizId, String difficulty) {
        // CSV quizzes are always accessible - preserve default access
        startCSVQuizInternal(quizId, difficulty);
    }

    private void startCSVQuizInternal(String quizId, String difficulty) {
        Log.d("QuizAccess", "🎯 Starting CSV quiz: " + quizId + " with difficulty: " + difficulty + " (Always accessible)");
        
        // CSV quizzes are always accessible - no practice or grade checks needed
        Intent intent = new Intent(this, MultipleChoicePage.class);

        // Set operations based on difficulty
        if (difficulty.equals("Easy")) {
            intent.putStringArrayListExtra("operationList", new ArrayList<>(Arrays.asList("Addition", "Subtraction")));
        } else if (difficulty.equals("Medium")) {
            intent.putStringArrayListExtra("operationList", new ArrayList<>(Arrays.asList("Addition", "Subtraction", "Multiplication", "Division")));
        } else if (difficulty.equals("Hard")) {
            intent.putStringArrayListExtra("operationList", new ArrayList<>(Arrays.asList("Addition", "Subtraction", "Multiplication", "Division", "Decimal", "Percentage")));
        }

        intent.putExtra("quizId", quizId);
        intent.putExtra("game_type", "Quiz");
        intent.putExtra("difficulty", difficulty); // CRITICAL: Pass difficulty to MultipleChoicePage
        intent.putExtra("heartLimit", 3);
        intent.putExtra("timerLimit", 10);
        startActivity(intent);
    }
    
    /**
     * Start Firebase quiz - check schedule first
     * Note: The first created quiz (oldest by createdAt) is always available regardless of schedule
     */
    private void startFirebaseQuiz(com.google.firebase.firestore.QueryDocumentSnapshot quizDoc) {
        String quizId = quizDoc.getId();
        boolean isFirstCreatedQuiz = (firstCreatedQuizId != null && firstCreatedQuizId.equals(quizId));
        
        if (isFirstCreatedQuiz) {
            Log.d("QuizzesSection", "🎯 First created quiz detected - bypassing schedule checks");
            // First created quiz is always available - skip schedule checks
        } else {
            // Check if quiz is active, scheduled for future, or expired
            com.google.firebase.Timestamp startTimestamp = quizDoc.getTimestamp("startDateTime");
            com.google.firebase.Timestamp endTimestamp = quizDoc.getTimestamp("endDateTime");
            java.util.Date now = new java.util.Date();
            boolean isActive = true;
            boolean isFuture = false;
            boolean isExpired = false;
            
            if (startTimestamp != null && endTimestamp != null) {
                java.util.Date startDate = startTimestamp.toDate();
                java.util.Date endDate = endTimestamp.toDate();
                isFuture = now.before(startDate);
                isExpired = now.after(endDate);
                isActive = !isFuture && !isExpired;
            } else if (startTimestamp != null) {
                java.util.Date startDate = startTimestamp.toDate();
                isFuture = now.before(startDate);
                isActive = !isFuture;
            }
            
            if (isFuture) {
                // Quiz is scheduled for future
                if (startTimestamp != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", java.util.Locale.getDefault());
                    String startDateStr = sdf.format(startTimestamp.toDate());
                    Toast.makeText(this, "Quiz starts on: " + startDateStr, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Quiz is scheduled for a future date", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            
            if (isExpired) {
                // Quiz has expired
                if (endTimestamp != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", java.util.Locale.getDefault());
                    String endDateStr = sdf.format(endTimestamp.toDate());
                    Toast.makeText(this, "Quiz ended on: " + endDateStr, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "This quiz has expired", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
        
        // Quiz is active - start it
        // Only show tutorial warning for scheduled quizzes, not the first created quiz (opened by default)
        if (!isFirstCreatedQuiz && !hasCompletedAnyTutorial()) {
            showTutorialWarning(() -> startFirebaseQuizInternal(quizDoc));
            return;
        }
        startFirebaseQuizInternal(quizDoc);
    }
    
    /**
     * Set up button for Firebase quiz
     * Note: The first created quiz (oldest by createdAt) is always available regardless of schedule
     */
    private void setupFirebaseQuizButton(AppCompatButton startButton, 
                                        com.google.firebase.firestore.QueryDocumentSnapshot quizDoc) {
        
        String quizId = quizDoc.getId();
        boolean isFirstCreatedQuiz = (firstCreatedQuizId != null && firstCreatedQuizId.equals(quizId));
        
        // Check if quiz is active, scheduled for future, or expired
        com.google.firebase.Timestamp startTimestamp = quizDoc.getTimestamp("startDateTime");
        com.google.firebase.Timestamp endTimestamp = quizDoc.getTimestamp("endDateTime");
        java.util.Date now = new java.util.Date();
        boolean isActive = true;
        boolean isFuture = false;
        boolean isExpired = false;
        
        // First created quiz bypasses schedule checks
        if (!isFirstCreatedQuiz) {
            if (startTimestamp != null && endTimestamp != null) {
                java.util.Date startDate = startTimestamp.toDate();
                java.util.Date endDate = endTimestamp.toDate();
                isFuture = now.before(startDate);
                isExpired = now.after(endDate);
                isActive = !isFuture && !isExpired; // Active if not future and not expired
            } else if (startTimestamp != null) {
                // Only start time set
                java.util.Date startDate = startTimestamp.toDate();
                isFuture = now.before(startDate);
                isActive = !isFuture;
            }
        } else {
            Log.d("QuizzesSection", "🎯 First created quiz - button always enabled");
        }
        
        if (isFuture && !isFirstCreatedQuiz) {
            // Quiz is scheduled for future - disable button
            startButton.setBackgroundResource(R.drawable.btn_exitgame);
            startButton.setText("QUIZ SCHEDULED");
            startButton.setClickable(false);
            startButton.setEnabled(false);
            startButton.setAlpha(0.6f);
            
            // Show when quiz starts on click
            startButton.setOnClickListener(v -> {
                if (startTimestamp != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", java.util.Locale.getDefault());
                    String startDateStr = sdf.format(startTimestamp.toDate());
                    Toast.makeText(this, "Quiz starts on: " + startDateStr, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Quiz is scheduled for a future date", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (isExpired && !isFirstCreatedQuiz) {
            // Quiz has expired - disable button
            startButton.setBackgroundResource(R.drawable.btn_exitgame);
            startButton.setText("QUIZ EXPIRED");
            startButton.setClickable(false);
            startButton.setEnabled(false);
            startButton.setAlpha(0.6f);
            
            // Show when quiz ended on click
            startButton.setOnClickListener(v -> {
                if (endTimestamp != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", java.util.Locale.getDefault());
                    String endDateStr = sdf.format(endTimestamp.toDate());
                    Toast.makeText(this, "Quiz ended on: " + endDateStr, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "This quiz has expired", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Quiz is active (or first created quiz) - enable button
            startButton.setBackgroundResource(R.drawable.btn_playgame);
            startButton.setText("START QUIZ");
            startButton.setClickable(true);
            startButton.setEnabled(true);
            startButton.setAlpha(1.0f);
            
            startButton.setOnClickListener(v -> {
                // startFirebaseQuiz already handles tutorial check and preserves first created quiz
                startFirebaseQuiz(quizDoc);
            });
        }
    }

    private void startFirebaseQuizInternal(com.google.firebase.firestore.QueryDocumentSnapshot quizDoc) {
        playSound("click.mp3");

        // Get quiz data (quizId already declared above)
        String quizId = quizDoc.getId();
        String quizTitle = quizDoc.getString("quizTitle");
        String quizNumber = quizDoc.getString("quizNumber");
        java.util.List<Map<String, Object>> questions = 
            (java.util.List<Map<String, Object>>) quizDoc.get("questions");

        if (questions == null || questions.isEmpty()) {
            Toast.makeText(this, "Quiz has no questions", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert Firebase questions to MathProblem list
        java.util.List<MathProblem> problemSet = convertFirebaseQuestionsToMathProblems(questions);

        // Start quiz with Firebase questions - same structure as CSV quiz
        Intent intent = new Intent(this, MultipleChoicePage.class);
        intent.putExtra("quizId", quizId);
        intent.putExtra("game_type", "Quiz");
        intent.putExtra("isFirebaseQuiz", true);
        intent.putExtra("firebaseQuizTitle", quizTitle != null ? quizTitle : "Custom Quiz");
        intent.putExtra("firebaseQuizNumber", quizNumber != null ? quizNumber : "1");
        intent.putExtra("timerLimit", 10); // Default timer limit for quizzes (10 minutes)
        intent.putParcelableArrayListExtra("firebaseQuestions", 
            new java.util.ArrayList<>(problemSet));
        startActivity(intent);
    }

    /**
     * Show warning dialog when tutorials were not taken yet.
     */
    /**
     * Show warning dialog when tutorials were not taken yet.
     * Only shown for scheduled Firebase quizzes, NOT for default quizzes (CSV or first created quiz).
     */
    private void showTutorialWarning(Runnable onContinue) {
        new AlertDialog.Builder(this)
            .setTitle("Warning")
            .setMessage("The system detected that you didn't take your tutorials yet. Are you sure you wish to continue?")
            .setNegativeButton("Cancel", (d, which) -> d.dismiss())
            .setPositiveButton("Continue", (d, which) -> {
                d.dismiss();
                onContinue.run();
            })
            .setCancelable(true)
            .show();
    }

    private boolean hasCompletedAnyTutorial() {
        com.happym.mathsquare.utils.TutorialProgressTracker tracker =
            new com.happym.mathsquare.utils.TutorialProgressTracker(this);
        java.util.Set<String> completed = tracker.getCompletedTutorials();
        return completed != null && !completed.isEmpty();
    }
    
    /**
     * Convert Firebase question format to MathProblem objects
     */
    private java.util.List<MathProblem> convertFirebaseQuestionsToMathProblems(
            java.util.List<Map<String, Object>> questions) {
        
        java.util.List<MathProblem> problemSet = new java.util.ArrayList<>();
        
        for (Map<String, Object> question : questions) {
            String questionStr = (String) question.get("question");
            String operation = (String) question.get("operation");
            String difficulty = (String) question.get("difficulty");
            Object answerObj = question.get("answer");
            String choices = (String) question.get("choices");
            
            double answer = 0.0;
            if (answerObj instanceof Number) {
                answer = ((Number) answerObj).doubleValue();
            } else if (answerObj instanceof String) {
                try {
                    answer = Double.parseDouble((String) answerObj);
                } catch (NumberFormatException e) {
                    Log.e("QuizzesSection", "Invalid answer format", e);
                    continue;
                }
            }
            
            MathProblem problem = new MathProblem(questionStr, operation, difficulty, answer, choices);
            problemSet.add(problem);
        }
        
        return problemSet;
    }
    
    /**
     * Set up default CSV quiz (fallback)
     */
    private void setupDefaultQuiz(TextView quizLevelText, TextView gradeLevelText,
                                  AppCompatButton startQuizButton, LinearLayout quizLevelDisplay,
                                  String quizId, String difficulty) {
        
        // Update UI with student's quiz level
        quizLevelText.setText(quizId.replace("quiz_", ""));
        gradeLevelText.setText("Grade " + quizLevelText.getText() + " Quiz");
        
        // Add animation to the quiz level display
        animateButtonFocus(quizLevelDisplay);
        
        // Set up the start quiz button (CSV quiz)
        setupStartQuizButton(startQuizButton, quizId, difficulty);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listener when activity is destroyed
        if (quizStatusListener != null) {
            quizStatusListener.remove();
            quizStatusListener = null;
        }
    }



    private void animateButtonClick(View button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.6f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.6f, 1.1f, 1f);

        // Set duration for the animations
        scaleX.setDuration(1000);
        scaleY.setDuration(1000);

        // OvershootInterpolator for game-like snappy effect
        OvershootInterpolator overshootInterpolator = new OvershootInterpolator(2f);
        scaleX.setInterpolator(overshootInterpolator);
        scaleY.setInterpolator(overshootInterpolator);

        // Combine animations into a set
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();
    }

    // Function to animate button focus with a smooth pulsing bounce effect
    private void animateButtonFocus(View button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.06f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.06f, 1f);

        // Set duration for a slower, smoother pulsing bounce effect
        scaleX.setDuration(2000);
        scaleY.setDuration(2000);

        // AccelerateDecelerateInterpolator for smooth pulsing
        AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
        scaleX.setInterpolator(interpolator);
        scaleY.setInterpolator(interpolator);

        // Set repeat count and mode on each ObjectAnimator
        scaleX.setRepeatCount(ObjectAnimator.INFINITE); // Infinite repeat
        scaleX.setRepeatMode(ObjectAnimator.REVERSE); // Reverse animation on repeat
        scaleY.setRepeatCount(ObjectAnimator.INFINITE); // Infinite repeat
        scaleY.setRepeatMode(ObjectAnimator.REVERSE); // Reverse animation on repeat

        // Combine the animations into an AnimatorSet
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();
    }

    // Stop Focus Animation
    private void stopButtonFocusAnimation(View button) {
        AnimatorSet animatorSet = (AnimatorSet) button.getTag();
        if (animatorSet != null) {
            animatorSet.cancel(); // Stop the animation when focus is lost
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        MusicManager.resume();
    }
    
    /**
     * Quiz tile item data class
     */
    private static class QuizTileItem {
        boolean isCSVQuiz;
        String quizId;
        String quizNumber;
        String quizTitle;
        String difficulty;
        com.google.firebase.firestore.QueryDocumentSnapshot quizDoc;
    }
    
    /**
     * Adapter for quiz tiles in grid layout
     */
    private class QuizTileAdapter extends RecyclerView.Adapter<QuizTileAdapter.QuizTileViewHolder> {
        private java.util.List<QuizTileItem> quizItems;
        
        public QuizTileAdapter() {
            this.quizItems = new java.util.ArrayList<>();
        }
        
        public void addQuiz(QuizTileItem item) {
            quizItems.add(item);
            notifyItemInserted(quizItems.size() - 1);
            // Notify parent activity to update empty state
            if (QuizzesSection.this != null) {
                QuizzesSection.this.updateEmptyState();
            }
        }
        
        public int getQuizCount() {
            return quizItems.size();
        }
        
        @Override
        public QuizTileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            android.view.View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_tile, parent, false);
            return new QuizTileViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(QuizTileViewHolder holder, int position) {
            QuizTileItem item = quizItems.get(position);
            
            // Set quiz number (pure tile - just the number)
            holder.quizLevelText.setText(item.quizNumber != null ? item.quizNumber : "1");
            
            // Add animation to the tile
            animateButtonFocus(holder.quizTileContainer);
            
            // Make entire tile clickable
            holder.quizTileContainer.setOnClickListener(v -> {
                playSound("click.mp3");
                
                if (item.isCSVQuiz) {
                    // CSV quiz - always accessible, no restrictions
                    startCSVQuiz(item.quizId, item.difficulty);
                } else {
                    // Firebase quiz - check schedule and start
                    startFirebaseQuiz(item.quizDoc);
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return quizItems.size();
        }
        
        class QuizTileViewHolder extends RecyclerView.ViewHolder {
            LinearLayout quizTileContainer;
            TextView quizLevelText;
            
            QuizTileViewHolder(android.view.View itemView) {
                super(itemView);
                quizTileContainer = itemView.findViewById(R.id.quiz_tile_container);
                quizLevelText = itemView.findViewById(R.id.quiz_level_text);
            }
        }
    }
}
