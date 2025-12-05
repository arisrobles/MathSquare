package com.happym.mathsquare;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.constraintlayout.widget.ConstraintSet;
import com.google.firebase.FirebaseApp;
import com.happym.mathsquare.dialog.PauseDialog;
import com.happym.mathsquare.dialog.PauseDialog.PauseDialogListener; // Adjust to match your project
// structure

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java.util.Set;
import java.util.HashSet;

import com.happym.mathsquare.MusicManager;

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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.view.animation.BounceInterpolator;
import java.io.IOException;
import java.util.Random;

import androidx.constraintlayout.widget.ConstraintLayout;
import com.happym.mathsquare.Animation.*;
import com.happym.mathsquare.QuestionTracker;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.happym.mathsquare.sharedPreferences;

public class MultipleChoicePage extends AppCompatActivity
        implements PauseDialog.PauseDialogListener {

    private int questionCount = 0;
    private CountDownTimer countDownTimer;
    private boolean isGameOver = false;
    private boolean repeatTaskUser = false;
    private long timeLeftInMillis; // Stores remaining time
    private boolean isTimerRunning = false;
    private int score = 0;
    private double num1, num2;
    private int heartLimit, timerLimit, questionLimits, selHeart, selTimer;

    private int currentQuestionIndex = 0;
    private List<MathProblem> problemSet = new ArrayList<>();
    private List<MathProblem> answeredQuestions = new ArrayList<>();
    private FirebaseFirestore db;

    private ImageButton imgBtn_pause;

    private String operationText, gametypeGame,difficulty, gametype, quidId, levelid, levelNext, worldType;
    private TextView givenOneTextView,
            givenTwoTextView,
            operationTextView,
            feedbackTextView,
            questionProgressTextView,
            text_operator,
            heartTxt,
            timerTxt,
            operationDisplay;
    private Button btnChoice1, btnChoice2, btnChoice3, btnChoice4;
    private MediaPlayer bgMediaPlayer;
    private MediaPlayer soundEffectPlayer;

    // Define constants for SharedPreferences
    private static final String PREFS_NAME = "MathAppPrefs";
    private static final String KEY_OPERATION_SET = "selectedOperationSet";
    private static final int REQUEST_CODE_RESULTS = 1;
    private String currentOperation, newOperation;
    private FrameLayout numberContainer;
    private FrameLayout backgroundFrame;
    private ArrayList<String> operationList;
    private ConstraintLayout gameView;
    private String numberRunlimit;
    private final Random random = new Random();
    private NumBGAnimation numBGAnimation;
    private ValueAnimator vignetteAnimator;
    private boolean isRedTransitionApplied = false; // Prevents unnecessary re-animation
    private List<String> usedOperations = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_multiplechoice);
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        imgBtn_pause = findViewById(R.id.imgBtn_pause);

        operationTextView = findViewById(R.id.text_operation);


        givenOneTextView = findViewById(R.id.text_givenone);
        givenTwoTextView = findViewById(R.id.text_giventwo);
        feedbackTextView = findViewById(R.id.text_feedback);

        text_operator = findViewById(R.id.text_operator);
        heartTxt = findViewById(R.id.heart_txt);
        timerTxt = findViewById(R.id.timer_txt);
        questionProgressTextView = findViewById(R.id.text_question_progress);

        gameView = findViewById(R.id.gameView);
       operationDisplay = findViewById(R.id.operationDisplay);

        gameView.setVisibility(View.VISIBLE);


        ImageButton imageButton = findViewById(R.id.imgBtn_home);
        imageButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    playSound("click.mp3");
                        Intent intent = new Intent(MultipleChoicePage.this, MainActivity.class);
                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });

        imgBtn_pause.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    playSound("click.mp3");
                        int currentScore = currentQuestionIndex;
                        currentScore++;
                        boolean isPaused = true; // Set based on your conditions
                        PauseDialog pauseDialog =
                                PauseDialog.newInstance(isPaused, 10 - currentScore);
                        pauseDialog.setListener(MultipleChoicePage.this);
                        pauseDialog.show(
                                getSupportFragmentManager(), "PauseDialog"); // Show the dialog
                        if (countDownTimer != null) {
                            countDownTimer.cancel(); // Stop the timer
                        }
                    }
                });

        btnChoice1 = findViewById(R.id.btn_choice1);
        btnChoice2 = findViewById(R.id.btn_choice2);
        btnChoice3 = findViewById(R.id.btn_choice3);
        btnChoice4 = findViewById(R.id.btn_choice4);

        animateButtonFocus(btnChoice1);
        animateButtonFocus(btnChoice2);
        animateButtonFocus(btnChoice3);
        animateButtonFocus(btnChoice4);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String gameType = getIntent().getStringExtra("game_type");
        gametypeGame = getIntent().getStringExtra("game_type");
        FrameLayout heart_choice = findViewById(R.id.heart_choice);
       FrameLayout timer_choice = findViewById(R.id.timer_choice);
        TextView lives_label = findViewById(R.id.lives_label);
        TextView timer_label = findViewById(R.id.timer_label);
        quidId = getIntent().getStringExtra("quizId");
        worldType = getIntent().getStringExtra("passing_world");
        levelid = getIntent().getStringExtra("passing");
        levelNext = getIntent().getStringExtra("passing_next_level");
        heartLimit = getIntent().getIntExtra("heartLimit", 3);
        timerLimit = getIntent().getIntExtra("timerLimit", 10);
        selHeart = getIntent().getIntExtra("heartLimit", 3);
        selTimer = getIntent().getIntExtra("timerLimit", 10);
        questionLimits = getIntent().getIntExtra("questionLimit", 20);

        // Debug logging for game type and question limits
        android.util.Log.d("GAME_TYPE_DEBUG", "Game Type: " + gameType + ", Question Limit: " + questionLimits);

        // Check if this is a Firebase quiz
        boolean isFirebaseQuiz = getIntent().getBooleanExtra("isFirebaseQuiz", false);
        
        if("Quiz".equals(gameType)){
           heart_choice.setVisibility(View.GONE);
           timer_choice.setVisibility(View.GONE);
           lives_label.setVisibility(View.GONE);
           timer_label.setVisibility(View.GONE);
           
           if (isFirebaseQuiz) {
               // Load Firebase quiz questions
               java.util.ArrayList<MathProblem> firebaseQuestions = 
                   getIntent().getParcelableArrayListExtra("firebaseQuestions");
               if (firebaseQuestions != null && !firebaseQuestions.isEmpty()) {
                   problemSet = firebaseQuestions;
                   currentQuestionIndex = 0;
                   
                   // Set up first question
                   generateNewQuestion(0, problemSet);
                   
                   // Start timer for quiz
                   startTimer(timerLimit * 60 * 1000L); // Convert minutes to milliseconds
                   
                   // Update progress
                   questionProgressTextView.setText("1/" + problemSet.size());
                   
                   // Skip CSV loading and operation switching
               } else {
                   Toast.makeText(this, "No questions found in quiz", Toast.LENGTH_SHORT).show();
                   finish();
               }
           }
        }else{


            updateHeartDisplay();
        startTimer(timerLimit * 60 * 1000);
        }



        operationText =
                getIntent().getStringExtra("operation");

       operationList = getIntent().getStringArrayListExtra("operationList");

if (operationList == null || operationList.isEmpty()) {
    operationList = new ArrayList<>();
    operationList.add("Subtraction");
}

        difficulty =
                getIntent().getStringExtra("difficulty") != null
                        ? getIntent().getStringExtra("difficulty")
                        : "";

        if("Quiz".equals(gameType) && !isFirebaseQuiz) {
            // Only do CSV quiz logic if not Firebase quiz
            // Defensive check: ensure difficulty is set for CSV quizzes
            if (difficulty == null || difficulty.isEmpty()) {
                Log.e("CSVQuiz", "⚠️ Difficulty is empty for CSV quiz! Defaulting to 'Easy'");
                difficulty = "Easy"; // Default to Easy if not provided
            }
            Log.d("CSVQuiz", "Starting CSV quiz with difficulty: " + difficulty + ", operations: " + operationList);
            Toast.makeText(this, operationList.toString() , Toast.LENGTH_SHORT)
                    .show();

           switchOperation(difficulty);

        } else if (!"Quiz".equals(gameType)){

            operationDisplay.setVisibility(View.GONE);

           if (operationText == null ) {
            Toast.makeText(this, "No Math Operation detected at the moment. :(", Toast.LENGTH_SHORT)
                    .show();
        } else {
            operationTextView.setText(operationText);
            feedbackTextView.setText("Operation detected");

            setupProblemSet(operationText, difficulty);

            if (repeatTaskUser) {

            } else {
                generateNewQuestion(currentQuestionIndex, problemSet);
            }
        }


        }



        btnChoice1.setOnClickListener(
                view -> checkAnswer(Double.parseDouble(btnChoice1.getText().toString()), btnChoice1, gameType));
        btnChoice2.setOnClickListener(
                view -> checkAnswer(Double.parseDouble(btnChoice2.getText().toString()), btnChoice2,gameType));
        btnChoice3.setOnClickListener(
                view -> checkAnswer(Double.parseDouble(btnChoice3.getText().toString()), btnChoice3, gameType));
        btnChoice4.setOnClickListener(
                view -> checkAnswer(Double.parseDouble(btnChoice4.getText().toString()), btnChoice4,gameType));


        playBGGame("newgamemusic.mp3");
        backgroundFrame = findViewById(R.id.main);
        numberContainer = findViewById(R.id.number_container); // Get FrameLayout from XML

        numBGAnimation = new NumBGAnimation(this, numberContainer);
        numBGAnimation.startNumberAnimationLoop();

 backgroundFrame.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            backgroundFrame.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            applyDefaultVignetteEffect(); // Set the default effect first

        }
    });
}



private void switchOperation(String difficulty) {
    if (operationList.isEmpty()) {
        return;
    }

    // Check if all operations have been used
    if (usedOperations.size() >= operationList.size()) {
        // All operations completed, end the quiz
        launchResultsActivity("Quiz");
        return;
    }

    // Get next unused operation
    String newOperation = null;
    for (String op : operationList) {
        if (!usedOperations.contains(op)) {
            newOperation = op;
            break;
        }
    }

    // Add to used operations and update UI
    if (newOperation != null) {
        usedOperations.add(newOperation);
        setupProblemSetList(difficulty, newOperation);
    } else {
        // This shouldn't happen, but end quiz as fallback
        launchResultsActivity("Quiz");
    }
}


private void setupProblemSetList(String difficulty, String operation) {
    // Defensive check: ensure difficulty is not empty
    if (difficulty == null || difficulty.isEmpty()) {
        Log.e("CSVQuiz", "⚠️ setupProblemSetList called with empty difficulty! Operation: " + operation);
        difficulty = "Easy"; // Default to Easy
    }
    
    BufferedReader bufferedReader;
    CSVProcessor csvProcessor = new CSVProcessor();
    String fileName = "";
    text_operator.setText("");

    switch (operation) {
        case "Addition":
            fileName = "additionProblemSet.csv";
            text_operator.setText("+");
            break;
        case "Subtraction":
            fileName = "subtractionProblemSet.csv";
            text_operator.setText("-");
            break;
        case "Multiplication":
            fileName = "multiplicationProblemSet.csv";
            text_operator.setText("×");
            break;
        case "Division":
            fileName = "divisionProblemSet.csv";
            text_operator.setText("÷");
            break;
        case "Percentage":
            fileName = "problemSet.csv";
            // Don't set operator here, will be set in generateNewQuestionList
            break;
        case "Decimal":
            fileName = "decimalProblemSet.csv";
            text_operator.setText(".");
            break;
        case "DecimalAddition":
            fileName = "decimalAdditionProblemSet.csv";
            text_operator.setText("+");
            break;
        case "DecimalSubtraction":
            fileName = "decimalSubtractionProblemSet.csv";
            text_operator.setText("-");
            break;
        case "DecimalMultiplication":
            fileName = "decimalMultiplicationProblemSet.csv";
            text_operator.setText("×");
            break;
        case "DecimalDivision":
            fileName = "decimalDivisionProblemSet.csv";
            text_operator.setText("÷");
            break;
        case "Decimals":
            fileName = "problemSet.csv";
            // Don't set operator here, will be set in generateNewQuestionList
            break;
    }

    try {
        bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open(fileName)));
    } catch (IOException e) {
        throw new RuntimeException(e);
    }

    List<MathProblem> mathProblemList = csvProcessor.readCSVFile(bufferedReader);

    // For Decimals and Percentage operations in Quiz 5 & 6, use Hard difficulty directly
    List<MathProblem> filteredProblems;
    String actualDifficulty = difficulty;
    if (("Percentage".equals(operation) || "Decimals".equals(operation)) && "Hard".equals(difficulty)) {
        // Use Hard difficulty directly for Quiz 5 & 6 decimal and percentage questions
        actualDifficulty = "Hard";
        filteredProblems = csvProcessor.getProblemsByOperationExcludingUsed(mathProblemList, "Hard", operation, this);
    } else {
        filteredProblems = csvProcessor.getProblemsByOperationExcludingUsed(mathProblemList, difficulty, operation, this);
    }

    android.util.Log.d("QUIZ_QUESTIONS", "Quiz Operation: " + operation + ", Difficulty: " + actualDifficulty);
    android.util.Log.d("QUIZ_QUESTIONS", "Quiz problems found: " + filteredProblems.size() + ", need: 5");

    // Auto-recycle: Clear used questions if not enough available for quiz (need 5 per operation)
    if (filteredProblems.size() < 5) {
        QuestionTracker recycleTracker = new QuestionTracker(this);
        int usedCount = recycleTracker.getUsedQuestionsCount(operation, actualDifficulty);
        android.util.Log.d("QUIZ_QUESTIONS", "AUTO-RECYCLE: Need 5 questions for " + operation + ", only " + filteredProblems.size() + " available, " + usedCount + " previously used");

        recycleTracker.clearUsedQuestions(operation, actualDifficulty);
        android.util.Log.d("QUIZ_QUESTIONS", "AUTO-RECYCLE: Cleared " + usedCount + " used questions for " + operation + "/" + actualDifficulty);

        // Re-fetch problems after clearing used questions
        if ("Hard".equals(actualDifficulty) && ("Percentage".equals(operation) || "Decimals".equals(operation))) {
            filteredProblems = csvProcessor.getProblemsByOperationExcludingUsed(mathProblemList, "Hard", operation, this);
        } else {
            filteredProblems = csvProcessor.getProblemsByOperationExcludingUsed(mathProblemList, difficulty, operation, this);
        }
        android.util.Log.d("QUIZ_QUESTIONS", "After recycling: " + filteredProblems.size() + " problems available");

        if (filteredProblems.size() < 5) {
            android.util.Log.w("QUIZ_QUESTIONS", "WARNING: Still not enough questions after recycling! Only " + filteredProblems.size() + " available for " + operation + ", need 5");
        }
    }

    // Clear old problem set
    problemSet.clear();

    // Create a list of available problems and shuffle it
    List<MathProblem> availableProblems = new ArrayList<>(filteredProblems);
    Collections.shuffle(availableProblems);

    // Add exactly 5 random problems for this operation (limit per operation)
    int numProblemsToAdd = Math.min(5, availableProblems.size());

    if (numProblemsToAdd == 0) {
        android.util.Log.e("CSVQuiz", "❌ No problems found for operation: " + operation + ", difficulty: " + actualDifficulty);
        Toast.makeText(this, "No " + operation + " problems found for difficulty: " + actualDifficulty + ". Please check CSV files.", Toast.LENGTH_LONG).show();
        finish(); // Exit quiz if no problems available
        return;
    }

    List<MathProblem> selectedProblems = availableProblems.subList(0, numProblemsToAdd);
    problemSet.addAll(selectedProblems);

    // Shuffle the final problem set
    Collections.shuffle(problemSet);

    // Reset UI and generate the first question
    updateUI(operation);
}


private void updateUI(String nextOperation) {
    // Reset currentQuestionIndex to 0 for the new operation (but keep track of overall progress)
    int questionInCurrentOperation = (currentQuestionIndex % 5) + 1;
    String questionProgressText = questionInCurrentOperation + "/5";
    questionProgressTextView.setText(questionProgressText);
operationTextView.setText(nextOperation);
        operationDisplay.setText(nextOperation);
        gameView.setVisibility(View.GONE);
        operationDisplay.setVisibility(View.VISIBLE);
        operationDisplay.setAlpha(0f);
        operationDisplay.animate().alpha(1f).setDuration(1000).withEndAction(() -> {
            new Handler().postDelayed(() -> {
                operationDisplay.animate().alpha(0f).setDuration(1000).withEndAction(() -> {
                    operationDisplay.setVisibility(View.GONE);
                    gameView.setVisibility(View.VISIBLE);
                    // Start with the first question of the new operation
                    generateNewQuestionList(0, problemSet);
                });
            }, 2000);
        });

}

private void generateNewQuestionList(int currentQIndex, List<MathProblem> sourceQuestions) {
    if (sourceQuestions == null || sourceQuestions.isEmpty()) {
        android.util.Log.e("QuestionGeneration", "Problem set is empty or null!");
        Toast.makeText(this, "No questions available. Please try again.", Toast.LENGTH_LONG).show();
        finish();
        return;
    }
    
    if (currentQIndex < sourceQuestions.size()) {
        MathProblem currentProblem = sourceQuestions.get(currentQIndex);

        // Handle special formatting for percentage and decimal questions
        Log.d("QuestionDisplay", "Operation: " + currentProblem.getOperation());
        if ("Percentage".equalsIgnoreCase(currentProblem.getOperation())) {
            // For percentage: show "50% of 10" format
            String[] formattedGivens = currentProblem.getFormattedGivenNumbers();
            givenOneTextView.setText(formattedGivens[0] + "%");
            givenTwoTextView.setText(formattedGivens.length > 1 ? formattedGivens[1] : "");
            text_operator.setText("of");
            Log.d("QuestionDisplay", "Set percentage format: " + formattedGivens[0] + "% of " + formattedGivens[1]);
        } else if ("Decimals".equalsIgnoreCase(currentProblem.getOperation())) {
            // For decimals: determine and show the correct operation
            String[] formattedGivens = currentProblem.getFormattedGivenNumbers();
            givenOneTextView.setText(formattedGivens[0]);
            givenTwoTextView.setText(formattedGivens.length > 1 ? formattedGivens[1] : "");

            // Determine the correct operation symbol
            double n1 = currentProblem.getGivenNumbers()[0];
            double n2 = currentProblem.getGivenNumbers()[1];
            double answer = currentProblem.getAnswer();

            if (Math.abs((n1 + n2) - answer) < 0.001) {
                text_operator.setText("+");
                Log.d("QuestionDisplay", "Set decimal addition: " + n1 + " + " + n2 + " = " + answer);
            } else if (Math.abs((n1 - n2) - answer) < 0.001) {
                text_operator.setText("-");
                Log.d("QuestionDisplay", "Set decimal subtraction: " + n1 + " - " + n2 + " = " + answer);
            } else if (Math.abs((n1 * n2) - answer) < 0.001) {
                text_operator.setText("×");
                Log.d("QuestionDisplay", "Set decimal multiplication: " + n1 + " × " + n2 + " = " + answer);
            } else if (n2 != 0 && Math.abs((n1 / n2) - answer) < 0.001) {
                text_operator.setText("÷");
                Log.d("QuestionDisplay", "Set decimal division: " + n1 + " ÷ " + n2 + " = " + answer);
            } else {
                text_operator.setText("+"); // Default fallback
                Log.d("QuestionDisplay", "Used fallback addition for: " + n1 + " ? " + n2 + " = " + answer);
            }
        } else {
            // For regular operations: use normal formatting
            String[] formattedGivens = currentProblem.getFormattedGivenNumbers();
            givenOneTextView.setText(formattedGivens[0]);
            givenTwoTextView.setText(formattedGivens.length > 1 ? formattedGivens[1] : "");
            
            // Set operator based on the actual problem's operation (not the expected operation)
            // This ensures the operator matches the question
            String problemOperation = currentProblem.getOperation();
            android.util.Log.d("QuestionDisplay", "Problem operation from CSV: '" + problemOperation + "'");
            
            if (problemOperation != null) {
                String opLower = problemOperation.toLowerCase().trim();
                android.util.Log.d("QuestionDisplay", "Normalized operation: '" + opLower + "'");
                
                switch (opLower) {
                    case "addition":
                        text_operator.setText("+");
                        // Update operation header to match
                        operationTextView.setText("Addition");
                        android.util.Log.d("QuestionDisplay", "Set operator to + and header to Addition");
                        break;
                    case "subtraction":
                        text_operator.setText("-");
                        // Update operation header to match
                        operationTextView.setText("Subtraction");
                        android.util.Log.d("QuestionDisplay", "Set operator to - and header to Subtraction");
                        break;
                    case "multiplication":
                        text_operator.setText("×");
                        // Update operation header to match
                        operationTextView.setText("Multiplication");
                        android.util.Log.d("QuestionDisplay", "Set operator to × and header to Multiplication");
                        break;
                    case "division":
                        text_operator.setText("÷");
                        // Update operation header to match
                        operationTextView.setText("Division");
                        android.util.Log.d("QuestionDisplay", "Set operator to ÷ and header to Division");
                        break;
                    default:
                        // Fallback: try to infer from question text or use what was set
                        android.util.Log.w("QuestionDisplay", "Unknown operation: '" + problemOperation + "', using fallback");
                        // Try to infer operator from question text if possible
                        String questionText = currentProblem.getQuestion();
                        if (questionText != null) {
                            if (questionText.contains("+")) {
                                text_operator.setText("+");
                                operationTextView.setText("Addition");
                            } else if (questionText.contains("-")) {
                                text_operator.setText("-");
                                operationTextView.setText("Subtraction");
                            } else if (questionText.contains("×") || questionText.contains("*")) {
                                text_operator.setText("×");
                                operationTextView.setText("Multiplication");
                            } else if (questionText.contains("÷") || questionText.contains("/")) {
                                text_operator.setText("÷");
                                operationTextView.setText("Division");
                            }
                        }
                        break;
                }
            } else {
                android.util.Log.e("QuestionDisplay", "Problem operation is null!");
            }
        }

        List<String> choicesList = currentProblem.getFormattedChoices();
        Collections.shuffle(choicesList);

        btnChoice1.setText(choicesList.get(0));
        btnChoice2.setText(choicesList.get(1));
        btnChoice3.setText(choicesList.get(2));
        btnChoice4.setText(choicesList.get(3));

        // Show progress within current operation (1-5 for each operation)
        int questionInCurrentOperation = (currentQIndex % 5) + 1;
        questionProgressTextView.setText(questionInCurrentOperation + "/5");
    } else {
        // This shouldn't happen in the new logic, but keep as fallback
        launchResultsActivity("Quiz");
    }
}

private void generateNewQuestion(int currentQIndex, List<MathProblem> sourceQuestions) {
        if (currentQIndex < sourceQuestions.size()) {
            MathProblem currentProblem = sourceQuestions.get(currentQIndex);

            // Handle special formatting for percentage and decimal questions
            if ("Percentage".equalsIgnoreCase(currentProblem.getOperation())) {
                // For percentage: show "50% of 10" format
                String[] formattedGivens = currentProblem.getFormattedGivenNumbers();
                givenOneTextView.setText(formattedGivens[0] + "%");
                givenTwoTextView.setText(formattedGivens.length > 1 ? formattedGivens[1] : "");
                text_operator.setText("of");
            } else if ("Decimals".equalsIgnoreCase(currentProblem.getOperation())) {
                // For decimals: determine and show the correct operation
                String[] formattedGivens = currentProblem.getFormattedGivenNumbers();
                givenOneTextView.setText(formattedGivens[0]);
                givenTwoTextView.setText(formattedGivens.length > 1 ? formattedGivens[1] : "");

                // Determine the correct operation symbol
                double n1 = currentProblem.getGivenNumbers()[0];
                double n2 = currentProblem.getGivenNumbers()[1];
                double answer = currentProblem.getAnswer();

                if (Math.abs((n1 + n2) - answer) < 0.001) {
                    text_operator.setText("+");
                } else if (Math.abs((n1 - n2) - answer) < 0.001) {
                    text_operator.setText("-");
                } else if (Math.abs((n1 * n2) - answer) < 0.001) {
                    text_operator.setText("×");
                } else if (n2 != 0 && Math.abs((n1 / n2) - answer) < 0.001) {
                    text_operator.setText("÷");
                } else {
                    text_operator.setText("+"); // Default fallback
                }
            } else {
                // For regular operations: use normal formatting
                String[] formattedGivens = currentProblem.getFormattedGivenNumbers();
                givenOneTextView.setText(formattedGivens[0]);
                givenTwoTextView.setText(formattedGivens.length > 1 ? formattedGivens[1] : "");
                // text_operator is already set in setupProblemSet
            }

            List<String> choicesList = currentProblem.getFormattedChoices();
            Collections.shuffle(choicesList);

            btnChoice1.setText(choicesList.get(0));
            btnChoice2.setText(choicesList.get(1));
            btnChoice3.setText(choicesList.get(2));
            btnChoice4.setText(choicesList.get(3));

            questionProgressTextView.setText((currentQIndex + 1) + "/" + sourceQuestions.size());
        } else {
            // Navigate to Results with answered questions
            Intent intent = new Intent(MultipleChoicePage.this, Results.class);

            // Add answered questions to intent
            intent.putParcelableArrayListExtra("EXTRA_ANSWERED_QUESTIONS", new ArrayList<>(answeredQuestions));

            int totalQuestions = problemSet.size();

            if (score == totalQuestions) {
                intent.putExtra("EXTRA_RESULT", "Congratulations");
            } else if (score > (totalQuestions * 0.75)) {
                intent.putExtra("EXTRA_RESULT", "Good Job!");
            } else if (score > (totalQuestions * 0.5)) {
                intent.putExtra("EXTRA_RESULT", "Nice Try!");
            } else {
                intent.putExtra("EXTRA_RESULT", "Failed");
            }

            intent.putExtra("quizid", quidId);
            intent.putExtra("passinglevelnext", levelNext);
            intent.putExtra("leveltype", levelid);
            intent.putExtra("passingworldtype", worldType);
            intent.putExtra("gametype", gametypeGame);
            intent.putExtra("heartLimit", selHeart);
            intent.putExtra("timerLimit", selTimer);
            intent.putExtra("EXTRA_SCORE", score);
            intent.putExtra("EXTRA_TOTAL", totalQuestions);
            intent.putExtra("EXTRA_OPERATIONTEXT", operationText);
            intent.putExtra("EXTRA_DIFFICULTY", difficulty);

            startActivity(intent);
            finish();
        }
    }



    private void setupProblemSet(String operationText, String difficulty) {
        BufferedReader bufferedReader = null;
        CSVProcessor csvProcessor = new CSVProcessor();
        String fileName = "additionProblemSet.csv";
        text_operator.setText("");

        if (operationText.equalsIgnoreCase("addition")) {
            fileName = "additionProblemSet.csv";
            text_operator.setText("+");
        } else if (operationText.equalsIgnoreCase("subtraction")) {
            fileName = "subtractionProblemSet.csv";
            text_operator.setText("-");
        } else if (operationText.equalsIgnoreCase("multiplication")) {
            fileName = "multiplicationProblemSet.csv";
            text_operator.setText("×");
        } else if (operationText.equalsIgnoreCase("division")) {
            fileName = "divisionProblemSet.csv";
            text_operator.setText("÷");
        } else if (operationText.equalsIgnoreCase("percentage")) {
            fileName = "percentageProblemSet.csv";
            text_operator.setText("%");
        } else if (operationText.equalsIgnoreCase("decimal")) {
            fileName = "decimalProblemSet.csv";
            text_operator.setText(".");
        } else if (operationText.equalsIgnoreCase("decimaladdition")) {
            fileName = "decimalAdditionProblemSet.csv";
            text_operator.setText("+");
        } else if (operationText.equalsIgnoreCase("decimalsubtraction")) {
            fileName = "decimalSubtractionProblemSet.csv";
            text_operator.setText("-");
        } else if (operationText.equalsIgnoreCase("decimalmultiplication")) {
            fileName = "decimalMultiplicationProblemSet.csv";
            text_operator.setText("×");
        } else if (operationText.equalsIgnoreCase("decimaldivision")) {
            fileName = "decimalDivisionProblemSet.csv";
            text_operator.setText("÷");
        }

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open(fileName)));
            List<MathProblem> allProblems = csvProcessor.readCSVFile(bufferedReader);
            problemSet = csvProcessor.getProblemsByOperationExcludingUsed(allProblems, difficulty, operationText, this);

            android.util.Log.d("QUESTION_LIMIT", "Operation: " + operationText + ", Difficulty: " + difficulty);
            android.util.Log.d("QUESTION_LIMIT", "Total problems found: " + problemSet.size());
            android.util.Log.d("QUESTION_LIMIT", "Question limit: " + questionLimits);

            // Auto-recycle: Clear used questions if not enough available for the required limit
            // This ensures players can always get the full 20 questions by recycling previously used ones
            if (problemSet.size() < questionLimits) {
                QuestionTracker recycleTracker = new QuestionTracker(this);
                int usedCount = recycleTracker.getUsedQuestionsCount(operationText, difficulty);
                android.util.Log.d("QUESTION_LIMIT", "AUTO-RECYCLE: Need " + questionLimits + " questions, only " + problemSet.size() + " available, " + usedCount + " previously used");

                recycleTracker.clearUsedQuestions(operationText, difficulty);
                android.util.Log.d("QUESTION_LIMIT", "AUTO-RECYCLE: Cleared " + usedCount + " used questions for " + operationText + "/" + difficulty);

                // Re-fetch problems after clearing used questions
                problemSet = csvProcessor.getProblemsByOperationExcludingUsed(allProblems, difficulty, operationText, this);
                android.util.Log.d("QUESTION_LIMIT", "After recycling: " + problemSet.size() + " problems available");

                if (problemSet.size() < questionLimits) {
                    android.util.Log.w("QUESTION_LIMIT", "WARNING: Still not enough questions after recycling! Only " + problemSet.size() + " available, need " + questionLimits);
                }
            }

            // Limit to questionLimits (20 for practice/timer games)
            if (problemSet.size() > questionLimits) {
                problemSet = problemSet.subList(0, questionLimits);
                android.util.Log.d("QUESTION_LIMIT", "Limited to: " + problemSet.size());
            } else {
                android.util.Log.d("QUESTION_LIMIT", "Using all available: " + problemSet.size());
            }

            if (problemSet.isEmpty()) {
                Toast.makeText(this, "No problems found for the selected difficulty", Toast.LENGTH_SHORT).show();
                return;
            }

            generateNewQuestion(currentQuestionIndex, problemSet);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading problem set", Toast.LENGTH_SHORT).show();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

// ✅ Apply default vignette effect (not animated)
private void applyDefaultVignetteEffect() {
    int width = backgroundFrame.getWidth();
    int height = backgroundFrame.getHeight();
    if (width == 0 || height == 0) return;

    // Create a bitmap
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);

    // Define the default vignette effect
    RadialGradient gradient = new RadialGradient(
            width / 2f, height / 2f,
            Math.max(width, height) * 0.8f,
            new int[]{Color.parseColor("#FFEF47"), Color.parseColor("#898021"), Color.parseColor("#504A31")},
            new float[]{0.2f, 0.6f, 1f},
            Shader.TileMode.CLAMP);

    Paint paint = new Paint();
    paint.setShader(gradient);
    paint.setAlpha(180);

    // Draw gradient on the canvas
    canvas.drawRect(0, 0, width, height, paint);
    backgroundFrame.setBackground(new BitmapDrawable(getResources(), bitmap));
}

// ✅ Smooth transition to red when heartLimit == 1
private void startVignetteEffect() {
    if (backgroundFrame.getWidth() == 0 || backgroundFrame.getHeight() == 0) return;

    if (heartLimit == 1 && !isRedTransitionApplied) {
        isRedTransitionApplied = true; // Prevents unnecessary re-animation

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(6000); // 1-second transition
        animator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();

            // Blend colors from default to red effect
            int blendedColor1 = blendColors(Color.parseColor("#FFEF47"), Color.parseColor("#DD4E47"), progress);
            int blendedColor2 = blendColors(Color.parseColor("#898021"), Color.parseColor("#A8403B"), progress);
            int blendedColor3 = blendColors(Color.parseColor("#504A31"), Color.parseColor("#6C211D"), progress);

            // Create animated vignette effect
            Bitmap bitmap = Bitmap.createBitmap(backgroundFrame.getWidth(), backgroundFrame.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            RadialGradient gradient = new RadialGradient(
                    backgroundFrame.getWidth() / 2f, backgroundFrame.getHeight() / 2f,
                    Math.max(backgroundFrame.getWidth(), backgroundFrame.getHeight()) * 0.8f,
                    new int[]{blendedColor1, blendedColor2, blendedColor3},
                    new float[]{0.2f, 0.6f, 1f},
                    Shader.TileMode.CLAMP
            );

            Paint paint = new Paint();
            paint.setShader(gradient);
            paint.setAlpha(200);

            // Draw gradient on canvas
            canvas.drawRect(0, 0, backgroundFrame.getWidth(), backgroundFrame.getHeight(), paint);
            backgroundFrame.setBackground(new BitmapDrawable(getResources(), bitmap));
        });

        animator.start();
    }
}

// ✅ Color blending function
private int blendColors(int colorStart, int colorEnd, float ratio) {
    int startA = (colorStart >> 24) & 0xff;
    int startR = (colorStart >> 16) & 0xff;
    int startG = (colorStart >> 8) & 0xff;
    int startB = colorStart & 0xff;

    int endA = (colorEnd >> 24) & 0xff;
    int endR = (colorEnd >> 16) & 0xff;
    int endG = (colorEnd >> 8) & 0xff;
    int endB = colorEnd & 0xff;

    return ((int) (startA + (endA - startA) * ratio) << 24) |
           ((int) (startR + (endR - startR) * ratio) << 16) |
           ((int) (startG + (endG - startG) * ratio) << 8) |
           ((int) (startB + (endB - startB) * ratio));
}


    /* //Not Working

        @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_RESULTS && resultCode == RESULT_OK && data != null) {
            String operationText = data.getStringExtra("operation");
            String difficulty = data.getStringExtra("difficulty");
            ArrayList<MathProblem> returnedAnsweredQuestions = data.getParcelableArrayListExtra("EXTRA_ANSWERED_QUESTIONS");
            boolean repeatTask = data.getBooleanExtra("repeatTask", false);

            if (repeatTask) {
                    recreate();
            }
        }
    }
        */

    @Override
    public void onResumeGame(boolean resumeGame) {
        if (resumeGame) {
            startTimer(timeLeftInMillis);
        }
    }


    private void updateHeartDisplay() {
        heartTxt.setText(String.valueOf(heartLimit));
    }

    private void startTimer(long millisInFuture) {
        timeLeftInMillis = millisInFuture; // Set the time left

        countDownTimer =
                new CountDownTimer(timeLeftInMillis, 1000) {
                    public void onTick(long millisUntilFinished) {
                        timeLeftInMillis = millisUntilFinished; // Update remaining time
                        int seconds = (int) (millisUntilFinished / 1000);
                        int minutes = seconds / 60;
                        seconds = seconds % 60;

                        numberRunlimit = String.format("%d:%02d", minutes, seconds);
                        timerTxt.setText(numberRunlimit);
                    }

                    public void onFinish() {
                        if (!isFinishing() && !isDestroyed()) {
                            numberRunlimit = "0:00";
                            timerTxt.setText("0:00");

                            if ("Quiz".equals(gametypeGame)) {
                                showPauseDialog();
                            } else {
                                int totalQuestions = problemSet.size();

                                // Pass Data to Results Activity
                                Intent intent = new Intent(MultipleChoicePage.this, Results.class);

                                intent.putExtra("EXTRA_RESULT", "Times Up!");
                                intent.putExtra("quizid", quidId);
                                intent.putExtra("passinglevelnext", levelNext);
                                intent.putExtra("leveltype", levelid);
                                intent.putExtra("passingworldtype", worldType);
                                intent.putExtra("gametype", gametypeGame);
                                intent.putExtra("heartLimit", selHeart);
                                intent.putExtra("timerLimit", selTimer);
                                intent.putExtra("EXTRA_SCORE", score);
                                intent.putExtra("EXTRA_TOTAL", totalQuestions);
                                intent.putExtra("EXTRA_OPERATIONTEXT", operationText);
                                intent.putExtra("EXTRA_DIFFICULTY", difficulty);

                                startActivity(intent);
                            }
                            isTimerRunning = false;
                        }
                    }
                }.start();

        isTimerRunning = true;
    }

    private void resumeTimer() {
        if (!isTimerRunning) {
            startTimer(timeLeftInMillis); // Resume from remaining time
        }
    }

    private void checkAnswer(double btnText, Button btnChoice, String gameType) {
        // Check if this is a Firebase quiz (declare once at method start)
        boolean isFirebaseQuiz = getIntent().getBooleanExtra("isFirebaseQuiz", false);
        
        // For quiz mode, calculate the question index within the current operation (0-4)
        int questionIndexInOperation;
        if ("Quiz".equals(gameType) && isFirebaseQuiz) {
            // Firebase quiz: use currentQuestionIndex directly
            questionIndexInOperation = currentQuestionIndex;
        } else if ("Quiz".equals(gameType)) {
            // CSV quiz: calculate index within operation
            questionIndexInOperation = currentQuestionIndex % 5;
        } else {
            questionIndexInOperation = currentQuestionIndex;
        }
        
        // Safety check: ensure problemSet is not empty and index is valid
        if (problemSet == null || problemSet.isEmpty()) {
            android.util.Log.e("QuizError", "Problem set is empty when checking answer!");
            Toast.makeText(this, "Error: No questions available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        if (questionIndexInOperation >= problemSet.size()) {
            android.util.Log.e("QuizError", "Question index " + questionIndexInOperation + " is out of bounds! ProblemSet size: " + problemSet.size());
            Toast.makeText(this, "Error: Question index out of bounds", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        MathProblem currentProblem = problemSet.get(questionIndexInOperation);
        android.util.Log.d("QuizAnswer", "Checking answer for question " + questionIndexInOperation + " of " + problemSet.size() + 
            ", Operation: " + currentProblem.getOperation() + ", CurrentQuestionIndex: " + currentQuestionIndex);
        currentProblem.setUserAnswer(String.valueOf(btnText));
        answeredQuestions.add(currentProblem);

        // Parse button text as double to handle decimal answers
        double userAnswer = Double.parseDouble(btnChoice.getText().toString());
        double correctAnswer = currentProblem.getAnswer();

        if (Math.abs(userAnswer - correctAnswer) < 0.01) { // Use small epsilon for double comparison
            // Increase the score on a correct answer.
            score++;
            feedbackTextView.setText("Correct!");
            animateCorrectAnswer(btnChoice);
            playEffectSound("correct.mp3");

            // For quiz type games, check if we've completed all questions
            if ("Quiz".equals(gameType)) {
                if (isFirebaseQuiz) {
                    // Firebase quiz: check if we've completed all questions
                    if (currentQuestionIndex + 1 >= problemSet.size()) {
                        launchResultsActivity(gameType);
                        return;
                    }
                } else {
                    // CSV quiz: check if we've completed all questions for all operations
                    int totalExpectedQuestions = operationList.size() * 5; // 5 questions per operation
                    if (currentQuestionIndex + 1 >= totalExpectedQuestions) {
                        // Launch the results activity when all questions are completed
                        launchResultsActivity(gameType);
                        return;
                    }
                }
            } else {
                // For non-quiz game types, check if the question index has reached the limit (now dynamic based on questionLimits)
                android.util.Log.d("QUESTION_PROGRESS", "Current question index: " + currentQuestionIndex + ", Question limit: " + questionLimits);
                if (currentQuestionIndex >= questionLimits) {
                    android.util.Log.d("QUESTION_PROGRESS", "Reached question limit, launching results");
                    launchResultsActivity(gameType);
                    return;
                }
            }
        } else {  // For an incorrect answer.
            feedbackTextView.setText("Wrong! The correct answer is " + currentProblem.getAnswer());
            animateIncorrectAnswer(btnChoice);
            playEffectSound("wrong.mp3");
            heartLimit--; // Reduce a life.
            updateHeartDisplay();

            // For quiz type games, check if we've completed all questions
            if ("Quiz".equals(gameType)) {
                if (isFirebaseQuiz) {
                    // Firebase quiz: check if we've completed all questions
                    if (currentQuestionIndex + 1 >= problemSet.size()) {
                        launchResultsActivity(gameType);
                        return;
                    }
                } else {
                    // CSV quiz: check if we've completed all questions for all operations
                    int totalExpectedQuestions = operationList.size() * 5; // 5 questions per operation
                    if (currentQuestionIndex + 1 >= totalExpectedQuestions) {
                        launchResultsActivity(gameType);
                        return;
                    }
                }
            } else {
                // In non-quiz game types, check if no lives remain.
                if (heartLimit == 0) {
                    playSound("failed.mp3");
                    showGameOver(gameType);
                    return;
                }
            }

            if ("Quiz".equals(gameType)) {

                }else{
                     // If only one heart is left, you may want to add a visual effect.
            if (heartLimit == 1) {
                startVignetteEffect();
            }
                }


            // Highlight the correct answer for user feedback.
            highlightCorrectAnswer(currentProblem.getAnswer());
        }

        // Move on to the next question.
        currentQuestionIndex++;
        android.util.Log.d("QUESTION_PROGRESS", "Moving to next question. New index: " + currentQuestionIndex + " (1-based: " + (currentQuestionIndex + 1) + ")");

        // For quiz games: check if we need to switch operations or end the quiz
        if ("Quiz".equals(gameType)) {
            if (isFirebaseQuiz) {
                // Firebase quiz: check if we've completed all questions
                if (currentQuestionIndex >= problemSet.size()) {
                    launchResultsActivity(gameType);
                    return;
                }
            } else {
                // CSV quiz: check if we need to switch operations
                int totalExpectedQuestions = operationList.size() * 5; // 5 questions per operation

                // Check if we've completed all questions for all operations
                if (currentQuestionIndex >= totalExpectedQuestions) {
                    launchResultsActivity(gameType);
                    return;
                }

                // Check if we need to switch operations (every 5 questions)
                // Don't switch at index 0 (first question), only after completing 5 questions (at index 5, 10, etc.)
                if (currentQuestionIndex > 0 && currentQuestionIndex % 5 == 0 && !operationList.isEmpty()) {
                    switchOperation(difficulty);
                    return;
                }
            }
        } else {
            // For non-quiz games, check if we have reached the end of the problemSet
            if (currentQuestionIndex >= problemSet.size()) {
                launchResultsActivity(gameType);
                return;
            }
        }

        // Post a delay before generating the new question, allowing animations or feedback to complete.
        btnChoice.postDelayed(() -> {
            // Clear any feedback message.
            feedbackTextView.setText("");

            if ("Quiz".equals(gameType)) {
                if (isFirebaseQuiz) {
                    // Firebase quiz: generate next question from problemSet
                    if (currentQuestionIndex < problemSet.size()) {
                        generateNewQuestion(currentQuestionIndex, problemSet);
                        questionProgressTextView.setText((currentQuestionIndex + 1) + "/" + problemSet.size());
                    }
                } else {
                    // CSV quiz: check if there are more questions to generate.
                    int totalExpectedQuestions = operationList.size() * 5; // 5 questions per operation
                    android.util.Log.d("CSVQuiz", "CurrentQuestionIndex: " + currentQuestionIndex + ", TotalExpected: " + totalExpectedQuestions + ", ProblemSet size: " + problemSet.size());
                    
                    if (currentQuestionIndex < totalExpectedQuestions) {
                        // Calculate the question index within the current operation (0-4)
                        int questionIndexInCurrentOperation = currentQuestionIndex % 5;
                        android.util.Log.d("CSVQuiz", "Question index in current operation: " + questionIndexInCurrentOperation);
                        
                        // Check if we need to switch operations (at the start of a new operation set)
                        if (questionIndexInCurrentOperation == 0 && currentQuestionIndex > 0) {
                            // We've completed 5 questions, switch to next operation
                            android.util.Log.d("CSVQuiz", "Switching to next operation after completing 5 questions");
                            switchOperation(difficulty);
                            return;
                        }
                        
                        // Check if problemSet is empty or doesn't have enough questions
                        if (problemSet.isEmpty() || questionIndexInCurrentOperation >= problemSet.size()) {
                            android.util.Log.w("CSVQuiz", "ProblemSet is empty or index out of bounds. Loading new operation.");
                            // Get current operation based on which questions we've done
                            int operationIndex = currentQuestionIndex / 5;
                            if (operationIndex < operationList.size()) {
                                String currentOp = operationList.get(operationIndex);
                                android.util.Log.d("CSVQuiz", "Loading operation: " + currentOp + " (index: " + operationIndex + ")");
                                setupProblemSetList(difficulty, currentOp);
                                // After setupProblemSetList, updateUI will be called which generates the first question
                                return; // Exit early, updateUI will handle question generation
                            }
                        } else {
                            // Generate the next question from current problemSet
                            android.util.Log.d("CSVQuiz", "Generating question " + questionIndexInCurrentOperation + " from problemSet");
                            generateNewQuestionList(questionIndexInCurrentOperation, problemSet);
                        }
                    } else {
                        android.util.Log.d("CSVQuiz", "All questions completed!");
                        Toast.makeText(this, "All Questions Completed!", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                // For non-quiz mode, generate the next question if available.
                if (currentQuestionIndex < problemSet.size()) {
                    generateNewQuestion(currentQuestionIndex, problemSet);
                }
            }
        }, 1000);
    }

    /**
     * Helper method to prepare and launch the Results Activity.
     * This method creates an intent, adds all necessary extras,
     * and then starts the new activity. Adjust the threshold and extras as needed.
     */
    private void launchResultsActivity(String gameType) {
        Intent intent = new Intent(MultipleChoicePage.this, Results.class);

        // Add answered questions to intent
        intent.putParcelableArrayListExtra("EXTRA_ANSWERED_QUESTIONS", new ArrayList<>(answeredQuestions));

        // Calculate correct total questions based on game type
        int totalQuestions;
        if ("Quiz".equals(gameType)) {
            // Check if this is a Firebase quiz
            boolean isFirebaseQuiz = getIntent().getBooleanExtra("isFirebaseQuiz", false);
            if (isFirebaseQuiz) {
                // Firebase quiz: use actual problemSet size
                totalQuestions = problemSet.size();
            } else {
                // CSV quiz: 5 questions per operation
                totalQuestions = operationList.size() * 5;
            }
        } else {
            totalQuestions = problemSet.size(); // For non-quiz games
        }

        // Provide feedback messages based on the score.
        if ("Quiz".equals(gameType)) {
            if (score == totalQuestions) {
                intent.putExtra("EXTRA_RESULT", "Congratulations");
            } else if (score >= (totalQuestions * 0.75)) {
                intent.putExtra("EXTRA_RESULT", "Good Job!");
            } else if (score >= (totalQuestions * 0.25)) {
                intent.putExtra("EXTRA_RESULT", "Nice Try!");
            } else {
                intent.putExtra("EXTRA_RESULT", "Failed");
            }
        } else {
            // For non-quiz games, use dynamic scoring based on totalQuestions
            if (score == totalQuestions) {
                intent.putExtra("EXTRA_RESULT", "Congratulations");
            } else if (score >= (totalQuestions * 0.75)) {
                intent.putExtra("EXTRA_RESULT", "Good Job!");
            } else if (score >= (totalQuestions * 0.25)) {
                intent.putExtra("EXTRA_RESULT", "Nice Try!");
            } else {
                intent.putExtra("EXTRA_RESULT", "Failed");
            }
        }

        // Pass necessary extras to the Results activity.
        if ("Quiz".equals(gameType)) {
            intent.putStringArrayListExtra("operationList", new ArrayList<>(operationList));
        }
        intent.putExtra("quizid", quidId);
        intent.putExtra("passinglevelnext", levelNext);
        intent.putExtra("leveltype", levelid);
        intent.putExtra("passingworldtype", worldType);
        intent.putExtra("gametype", gameType);
        intent.putExtra("heartLimit", selHeart);
        intent.putExtra("timerLimit", selTimer);
        intent.putExtra("EXTRA_SCORE", score);
        intent.putExtra("EXTRA_TOTAL", totalQuestions);
        intent.putExtra("EXTRA_OPERATIONTEXT", operationText);
        intent.putExtra("EXTRA_DIFFICULTY", difficulty);
        
        // Pass Firebase quiz metadata and questions if this is a Firebase quiz
        boolean isFirebaseQuiz = getIntent().getBooleanExtra("isFirebaseQuiz", false);
        if (isFirebaseQuiz) {
            intent.putExtra("isFirebaseQuiz", true);
            intent.putExtra("firebaseQuizTitle", getIntent().getStringExtra("firebaseQuizTitle"));
            intent.putExtra("firebaseQuizNumber", getIntent().getStringExtra("firebaseQuizNumber"));
            // CRITICAL: Pass the Firebase questions so they can be reused on retry
            intent.putParcelableArrayListExtra("firebaseQuestions", new ArrayList<>(problemSet));
            Log.d("FirebaseQuiz", "Passing " + problemSet.size() + " Firebase questions to Results");
        }

        // Debug logging to track score being passed
        android.util.Log.d("ScoreDebug", "Passing score to Results: " + score + " out of " + totalQuestions);

        // Start the Results activity.
        startActivity(intent);
        finish();
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

private void playEffectSound(String fileName) {


    // Stop any previous sound effect before playing a new one
    if (soundEffectPlayer != null) {
        soundEffectPlayer.release();
        soundEffectPlayer = null;
    }

    try {
        AssetFileDescriptor afd = getAssets().openFd(fileName);
        soundEffectPlayer = new MediaPlayer();
        soundEffectPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        soundEffectPlayer.prepare();

        // Set volume to max
        soundEffectPlayer.setVolume(1.0f, 1.0f);

        soundEffectPlayer.setOnCompletionListener(mp -> {
            mp.release();
            soundEffectPlayer = null;
        });

        soundEffectPlayer.start();
    } catch (IOException e) {
        e.printStackTrace();
    }
}


    private void playBGGame(String fileName) {
    if (bgMediaPlayer == null) { // Prevent re-initializing
        try {
            AssetFileDescriptor afd = getAssets().openFd(fileName);
            bgMediaPlayer = new MediaPlayer();
            bgMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            bgMediaPlayer.prepare();

            // Set looping to true
            bgMediaPlayer.setLooping(true);

            bgMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


    private void highlightCorrectAnswer(double actualAnswer) {
        double epsilon = 0.01; // Small value for double comparison

        if (Math.abs(Double.parseDouble(btnChoice1.getText().toString()) - actualAnswer) < epsilon) {
            animateCorrectAnswer(btnChoice1);
        } else if (Math.abs(Double.parseDouble(btnChoice2.getText().toString()) - actualAnswer) < epsilon) {
            animateCorrectAnswer(btnChoice2);
        } else if (Math.abs(Double.parseDouble(btnChoice3.getText().toString()) - actualAnswer) < epsilon) {
            animateCorrectAnswer(btnChoice3);
        } else if (Math.abs(Double.parseDouble(btnChoice4.getText().toString()) - actualAnswer) < epsilon) {
            animateCorrectAnswer(btnChoice4);
        }
    }

    private void showPauseDialog() {
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Stop the timer
        }
        // Check if the activity is still valid and not finishing
        if (!isFinishing() && !isDestroyed()) {
            PauseDialog pauseDialog = PauseDialog.newInstance(true, heartLimit);
            pauseDialog.setListener(this);
            pauseDialog.show(getSupportFragmentManager(), "PauseDialog");
        }
    }

    private void showGameOver(String gameType) {
        isGameOver = true;
        Intent intent = new Intent(MultipleChoicePage.this, Results.class);

        // Add answered questions to intent
        intent.putParcelableArrayListExtra("EXTRA_ANSWERED_QUESTIONS", new ArrayList<>(answeredQuestions));

        int totalQuestions = problemSet.size();

        if (score == totalQuestions) {
            intent.putExtra("EXTRA_RESULT", "Congratulations");
        } else if (score > (totalQuestions * 0.75)) {
            intent.putExtra("EXTRA_RESULT", "Good Job!");
        } else if (score > (totalQuestions * 0.5)) {
            intent.putExtra("EXTRA_RESULT", "Nice Try!");
        } else {
            intent.putExtra("EXTRA_RESULT", "Failed");
        }

        intent.putExtra("quizid", quidId);
        intent.putExtra("passinglevelnext", levelNext);
        intent.putExtra("leveltype", levelid);
        intent.putExtra("passingworldtype", worldType);
        intent.putExtra("gametype", gametypeGame);
        intent.putExtra("heartLimit", selHeart);
        intent.putExtra("timerLimit", selTimer);
        intent.putExtra("EXTRA_SCORE", score);
        intent.putExtra("EXTRA_TOTAL", totalQuestions);
        intent.putExtra("EXTRA_OPERATIONTEXT", operationText);
        intent.putExtra("EXTRA_DIFFICULTY", difficulty);

        // Debug logging for showGameOver method
        android.util.Log.d("ScoreDebug", "showGameOver passing score: " + score + " out of " + totalQuestions);

        startActivity(intent);
        finish();
    }

    private void endGame() {
        countDownTimer.cancel();
        Intent intent = new Intent(MultipleChoicePage.this, Results.class);
        intent.putExtra("EXTRA_SCORE", score);
        intent.putExtra("EXTRA_TOTAL", problemSet.size());
        startActivity(intent);
        finish();
    }

    @Override
    public void onRepeatGame(boolean shouldRepeat) {
        if (shouldRepeat) {
            recreate();
        }
    }

    @Override
    public void onBackPressed() {
        // Show pause dialog instead of directly exiting
        showPauseDialog();
    }

    private void animateCorrectAnswer(Button button) {

        button.setBackgroundResource(R.drawable.btn_condition_create);

        ObjectAnimator bounceAnimator = ObjectAnimator.ofFloat(button, "translationY", 0, -30f, 0);
        bounceAnimator.setDuration(400);
        bounceAnimator.setRepeatCount(2);

        bounceAnimator.addListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // Reset the button background to the specified drawable
                        button.setBackgroundResource(R.drawable.btn_short_condition);
                    }
                });

        bounceAnimator.start();
    }

    private void animateIncorrectAnswer(Button button) {


        // Change the background color to red
        button.setBackgroundResource(R.drawable.btn_condition_red);

        // Create the shake animation
        ObjectAnimator shakeAnimator =
                ObjectAnimator.ofFloat(button, "translationX", 0, 20f, -20f, 20f, -20f, 0);
        shakeAnimator.setDuration(350);

        // Add a listener to change the background back after the animation ends
        shakeAnimator.addListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // Reset the button background to the specified drawable
                        button.setBackgroundResource(R.drawable.btn_short_condition);

                    }
                });

        // Start the animation
        shakeAnimator.start();
    }

    private void animateButtonClick(View button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.6f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.6f, 1.1f, 1f);

        // Set duration for the animations
        scaleX.setDuration(3000);
        scaleY.setDuration(3000);

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

    private void animateButtonPushDowm(View button) {
        ObjectAnimator scaleX =
                ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f); // Scale down slightly
        ObjectAnimator scaleY =
                ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f); // Scale down slightly

        // Set shorter duration for a quick push effect
        scaleX.setDuration(200);
        scaleY.setDuration(200);

        // Use a smooth interpolator
        AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
        scaleX.setInterpolator(interpolator);
        scaleY.setInterpolator(interpolator);

        // Combine the animations into an AnimatorSet
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);

        // Start the animation
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
    protected void onDestroy() {
        super.onDestroy();

        if (bgMediaPlayer != null) {
            bgMediaPlayer.release();
            bgMediaPlayer = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        MusicManager.stop();
        playBGGame("newgamemusic.mp3");
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (bgMediaPlayer != null) {
            bgMediaPlayer.pause();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        bgMediaPlayer.start();
    }
}
