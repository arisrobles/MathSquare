package com.happym.mathsquare;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    private FrameLayout numberContainer, backgroundFrame;
    private final Random random = new Random();
    private NumBGAnimation numBGAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.layout_quizzes_section); // your layout file

        FirebaseApp.initializeApp(this);

        // Get UI elements
        TextView quizLevelText = findViewById(R.id.quiz_level_text);
        TextView gradeLevelText = findViewById(R.id.grade_level_text);
        AppCompatButton startQuizButton = findViewById(R.id.btn_start_quiz);
        LinearLayout quizLevelDisplay = findViewById(R.id.quiz_level_display);

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
        
        // First, check for Firebase quizzes for this grade and section
        loadFirebaseQuizzes(studentGrade, studentSection, quizLevelText, gradeLevelText, 
            startQuizButton, quizLevelDisplay, quizId, difficulty);

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
        
        // Fetch Firestore status first to check if quiz is available
        fetchQuizStatus(quizId, startButton);

        // Check sequential quiz locking (Quiz 2 locked until Quiz 1 completed, etc.)
        QuizProgressTracker quizTracker = new QuizProgressTracker(this);
        quizTracker.canAccessQuiz(quizId, (canAccess, message) -> {
            Log.d("QuizAccess", "  - Sequential check result: " + (canAccess ? "✅ Accessible" : "❌ Locked"));
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
                            
                            // Also check sequential quiz locking (double-check)
                            QuizProgressTracker quizTracker = new QuizProgressTracker(view.getContext());
                            quizTracker.canAccessQuiz(quizId, (canAccess, accessMessage) -> {
                                Log.d("QuizAccess", "  - Final sequential check: " + (canAccess ? "✅ Accessible" : "❌ Locked"));
                                if (!canAccess) {
                                    Log.w("QuizAccess", "⚠️ Quiz blocked by sequential check: " + accessMessage);
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
     * Load Firebase quizzes for student's grade and section
     * If no Firebase quizzes found, fall back to CSV quiz
     */
    private void loadFirebaseQuizzes(String studentGrade, String studentSection,
                                    TextView quizLevelText, TextView gradeLevelText,
                                    AppCompatButton startQuizButton, LinearLayout quizLevelDisplay,
                                    String defaultQuizId, String defaultDifficulty) {
        
        if (studentGrade == null) {
            // Fall back to default CSV quiz
            setupDefaultQuiz(quizLevelText, gradeLevelText, startQuizButton, quizLevelDisplay, 
                defaultQuizId, defaultDifficulty);
            return;
        }
        
        // Query Firebase for active quizzes matching grade
        // Note: Firestore doesn't support OR queries easily, so we'll query and filter client-side
        com.google.firebase.firestore.Query query = db.collection("TeacherQuizzes")
            .whereEqualTo("grade", studentGrade);
        
        query.get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    java.util.List<com.google.firebase.firestore.QueryDocumentSnapshot> activeQuizzes = new java.util.ArrayList<>();
                    java.util.Date now = new java.util.Date();
                    
                    // Filter by schedule and section (active quizzes only)
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : task.getResult()) {
                        // Check section match
                        String quizSection = doc.getString("section");
                        boolean sectionMatches = false;
                        
                        if (studentSection != null && !studentSection.trim().isEmpty()) {
                            // Student has section - match if quiz is for their section or "All Sections"
                            sectionMatches = studentSection.equals(quizSection) || 
                                           "All Sections".equals(quizSection);
                        } else {
                            // Student has no section - only show "All Sections" quizzes
                            sectionMatches = "All Sections".equals(quizSection);
                        }
                        
                        if (!sectionMatches) {
                            continue; // Skip this quiz
                        }
                        
                        // Check schedule
                        com.google.firebase.Timestamp startTimestamp = doc.getTimestamp("startDateTime");
                        com.google.firebase.Timestamp endTimestamp = doc.getTimestamp("endDateTime");
                        
                        if (startTimestamp != null && endTimestamp != null) {
                            java.util.Date startDate = startTimestamp.toDate();
                            java.util.Date endDate = endTimestamp.toDate();
                            
                            // Check if quiz is currently active
                            if (now.after(startDate) && now.before(endDate)) {
                                activeQuizzes.add(doc);
                            }
                        }
                    }
                    
                    if (!activeQuizzes.isEmpty()) {
                        // Display Firebase quizzes
                        displayFirebaseQuizzes(activeQuizzes, quizLevelText, gradeLevelText, 
                            startQuizButton, quizLevelDisplay);
                    } else {
                        // No active Firebase quizzes, fall back to CSV quiz
                        setupDefaultQuiz(quizLevelText, gradeLevelText, startQuizButton, 
                            quizLevelDisplay, defaultQuizId, defaultDifficulty);
                    }
                } else {
                    // Error loading Firebase quizzes, fall back to CSV
                    Log.e("QuizzesSection", "Error loading Firebase quizzes", task.getException());
                    setupDefaultQuiz(quizLevelText, gradeLevelText, startQuizButton, 
                        quizLevelDisplay, defaultQuizId, defaultDifficulty);
                }
            });
    }
    
    /**
     * Display Firebase quizzes to student
     */
    private void displayFirebaseQuizzes(java.util.List<com.google.firebase.firestore.QueryDocumentSnapshot> quizzes,
                                       TextView quizLevelText, TextView gradeLevelText,
                                       AppCompatButton startQuizButton, LinearLayout quizLevelDisplay) {
        
        // For now, show the first active quiz (can be enhanced to show multiple)
        com.google.firebase.firestore.QueryDocumentSnapshot quiz = quizzes.get(0);
        
        String quizTitle = quiz.getString("quizTitle");
        String quizNumber = quiz.getString("quizNumber");
        String quizId = quiz.getId();
        
        // Update UI
        quizLevelText.setText(quizNumber != null ? quizNumber : "1");
        gradeLevelText.setText(quizTitle != null ? quizTitle : "Grade Quiz");
        
        // Add animation
        animateButtonFocus(quizLevelDisplay);
        
        // Set up button to start Firebase quiz
        setupFirebaseQuizButton(startQuizButton, quiz);
    }
    
    /**
     * Set up button for Firebase quiz
     */
    private void setupFirebaseQuizButton(AppCompatButton startButton, 
                                        com.google.firebase.firestore.QueryDocumentSnapshot quizDoc) {
        
        startButton.setBackgroundResource(R.drawable.btn_playgame);
        startButton.setText("START QUIZ");
        startButton.setClickable(true);
        startButton.setEnabled(true);
        
        startButton.setOnClickListener(v -> {
            playSound("click.mp3");
            
            // Get quiz data
            String quizId = quizDoc.getId();
            java.util.List<Map<String, Object>> questions = 
                (java.util.List<Map<String, Object>>) quizDoc.get("questions");
            
            if (questions == null || questions.isEmpty()) {
                Toast.makeText(this, "Quiz has no questions", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Convert Firebase questions to MathProblem list
            java.util.List<MathProblem> problemSet = convertFirebaseQuestionsToMathProblems(questions);
            
            // Start quiz with Firebase questions
            Intent intent = new Intent(this, MultipleChoicePage.class);
            intent.putExtra("quizId", quizId);
            intent.putExtra("game_type", "Quiz");
            intent.putExtra("isFirebaseQuiz", true);
            intent.putParcelableArrayListExtra("firebaseQuestions", 
                new java.util.ArrayList<>(problemSet));
            startActivity(intent);
        });
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
}
