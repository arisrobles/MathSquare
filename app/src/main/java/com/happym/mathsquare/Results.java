package com.happym.mathsquare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import com.happym.mathsquare.Animation.*;
import com.happym.mathsquare.GameType.Passing.*;
import com.happym.mathsquare.GameType.Practice.*;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class Results extends AppCompatActivity {
    private FirebaseFirestore db;
    private String quizId, quizScore;
    private ProgressDialog loadingDialog;
    private TextView showScore, showResult, showMotive;
    private List<MathProblem> problemSet = new ArrayList<>();
    private MediaPlayer soundEffectPlayer;
    private boolean saveSuccesfully = false;
    private int selHeart, selTimer;
    private ArrayList<String> operationList;
    private String quizIds;
    private String starRating;
    private int number;
    private FrameLayout numberContainer, backgroundFrame;
    private final Random random = new Random();
    private NumBGAnimation numBGAnimation;
    String completedLevelsField = "";
    String worldCompletedField = "";
    private ArrayList<MathProblem> answeredQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Firestore instance
        db = FirebaseFirestore.getInstance();

        selHeart = getIntent().getIntExtra("heartLimit", 3);
        selTimer = getIntent().getIntExtra("timerLimit", 10);
        String gameType = getIntent().getStringExtra("gametype");
        String getQuiz = getIntent().getStringExtra("quizid");
        final String getOperationText = getIntent().getStringExtra("EXTRA_OPERATIONTEXT");
        final String getDifficulty = getIntent().getStringExtra("EXTRA_DIFFICULTY");
        operationList = getIntent().getStringArrayListExtra("operationList");

        // Get answered questions from intent
        answeredQuestions = getIntent().getParcelableArrayListExtra("EXTRA_ANSWERED_QUESTIONS");

        ImageButton imageButton = findViewById(R.id.imgBtn_home);
        imageButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playSound("click.mp3");
                        // Use .equals() to compare string values.
                        if ("Passing".equals(gameType)) {
                            Intent intent = new Intent(Results.this, passingStageSelection.class);
                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("difficulty", getDifficulty);
                            intent.putExtra("operation", getOperationText);
                            intent.putExtra("reload_progress", true);
                            startActivity(intent);
                            finish();
                        } else if ("Quiz".equals(gameType)) {
                            Intent intent = new Intent(Results.this, QuizzesSection.class);
                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(Results.this, MainActivity.class);
                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
                });

        ImageButton imageButton_retry = findViewById(R.id.imgBtn_retry);
        imageButton_retry.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playSound("click.mp3");
                        // Get current data
                        ArrayList<MathProblem> answeredQuestions =
                                getIntent().getParcelableArrayListExtra("EXTRA_ANSWERED_QUESTIONS");

                        // Create an intent to return to MultipleChoicePage
                        Intent resultIntent = new Intent(Results.this, MultipleChoicePage.class);
                        resultIntent.putExtra("game_type", gameType);

                        // For quiz game types, pass additional extras
                        if ("Quiz".equals(gameType)) {
                            resultIntent.putStringArrayListExtra(
                                    "operationList", new ArrayList<>(operationList));
                            resultIntent.putExtra("quizId", getQuiz);
                        } else {
                            resultIntent.putExtra("operation", getOperationText);
                        }
                        resultIntent.putExtra("difficulty", getDifficulty);
                        resultIntent.putExtra("heartLimit", selHeart);
                        resultIntent.putExtra("timerLimit", selTimer);
                        startActivity(resultIntent);
                        finish();
                    }
                });

        ImageButton imageButton_mistakes = findViewById(R.id.imgBtn_mistakes);
        imageButton_mistakes.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playSound("click.mp3");
                        showMistakesDialog();
                    }
                });

        TextView textView = findViewById(R.id.textViewResults);
        textView.setText("");
        textView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playSound("click.mp3");
                        Intent intent = new Intent(Results.this, Difficulty.class);
                        startActivity(intent);
                    }
                });

        String getResult = getIntent().getStringExtra("EXTRA_RESULT");
        String worldType = getIntent().getStringExtra("passingworldtype");
        String levelType = getIntent().getStringExtra("leveltype");
        String difficulty = getIntent().getStringExtra("EXTRA_DIFFICULTY");
        String levelNext = getIntent().getStringExtra("passinglevelnext");

        int getScore = getIntent().getIntExtra("EXTRA_SCORE", 0);
        int getTotal = getIntent().getIntExtra("EXTRA_TOTAL", 0);

        // Debug logging to see what score is received
        android.util.Log.d("ScoreDebug", "Results received score: " + getScore + " out of " + getTotal + " for gameType: " + gameType);

        // Mark questions as used if this is a passing level and player got at least 1 star
        if ("Passing".equals(gameType) && getScore >= 1 && answeredQuestions != null && !answeredQuestions.isEmpty()) {
            CSVProcessor csvProcessor = new CSVProcessor();
            csvProcessor.markQuestionsAsUsed(answeredQuestions, getOperationText, difficulty, this);
        }

        showResult = findViewById(R.id.textViewResult);
        showScore = findViewById(R.id.textViewScore);
        showMotive = findViewById(R.id.textViewMotive);

        // Result and score text
        showResult.setText(getResult);
        String scoreDisplay = getScore + "/" + getTotal;
        showScore.setText(scoreDisplay);

        // Configure the loading dialog, but now show it before performing any network operations.
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Saving Progress...");
        loadingDialog.setCancelable(false);

        // Display Motivational message based on the result
        switch (getResult) {
            case "Congratulations":
                playSound("victory.mp3");
                if (sharedPreferences.StudentIsLoggedIn(Results.this)) {
                    // Show the loading dialog before sending the score
                    loadingDialog.show();
                    sendScoreResult(
                            getScore,
                            getQuiz,
                            gameType,
                            levelType,
                            levelNext,
                            worldType,
                            getOperationText,
                            difficulty);
                }
                showMotive.setText("Excellent!");
                break;
            case "Good Job!":
                playSound("victory.mp3");
                if (sharedPreferences.StudentIsLoggedIn(Results.this)) {
                    loadingDialog.show();
                    sendScoreResult(
                            getScore,
                            getQuiz,
                            gameType,
                            levelType,
                            levelNext,
                            worldType,
                            getOperationText,
                            difficulty);
                }
                showMotive.setText("Keep it Up!");
                break;
            case "Nice Try!":
                playSound("victory.mp3");
                if (sharedPreferences.StudentIsLoggedIn(Results.this)) {
                    loadingDialog.show();
                    sendScoreResult(
                            getScore,
                            getQuiz,
                            gameType,
                            levelType,
                            levelNext,
                            worldType,
                            getOperationText,
                            difficulty);
                }
                showMotive.setText("You can do even better!");
                break;
            case "Failed":
                if (sharedPreferences.StudentIsLoggedIn(Results.this)) {
                    loadingDialog.show();
                    sendScoreResult(
                            getScore,
                            getQuiz,
                            gameType,
                            levelType,
                            levelNext,
                            worldType,
                            getOperationText,
                            difficulty);
                }
                showMotive.setText("Try Again!");
                break;
        }

        // Initialize background animation
        backgroundFrame = findViewById(R.id.main);
        numberContainer = findViewById(R.id.number_container); // Get FrameLayout from XML

        numBGAnimation = new NumBGAnimation(this, numberContainer);
        numBGAnimation.startNumberAnimationLoop();

        backgroundFrame.post(
                new Runnable() {
                    @Override
                    public void run() {
                        VignetteEffect.apply(Results.this, backgroundFrame);
                    }
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

    /**
     * Sends the score result to the Firebase backend for different game types.
     *
     * @param Score The score achieved by the student.
     * @param quizid The quiz identifier.
     * @param gametype The type of game ("passing_level", "quiz", "OnTimer", or "Practicd").
     * @param levelNum The level number (e.g., "level_1", "level_10").
     * @param nextlevel The identifier for the next level to complete.
     * @param worldType The world in which the level is played (e.g., "world_one", "world_two").
     * @param OnTimerDifficulty The difficulty setting for OnTimer mode.
     */
    private void sendScoreResult(
            int Score,
            String quizid,
            String gametype,
            String levelNum,
            String nextlevel,
            String worldType,
            String operation,
            String OnTimerDifficulty) {

        // Retrieve student-related information from shared preferences.
        String section = sharedPreferences.getSection(this);
        String grade = sharedPreferences.getGrade(this);
        String firstName = sharedPreferences.getFirstN(this);
        String lastName = sharedPreferences.getLastN(this);

        // Determine quiz name & number
        int number;

        if (quizid == null) {
            quizIds = "Quiz 1";
            number = 1;
        } else {
            switch (quizid) {
                case "quiz_1":
                    quizIds = "Quiz 1";
                    number = 1;
                    break;
                case "quiz_2":
                    quizIds = "Quiz 2";
                    number = 2;
                    break;
                case "quiz_3":
                    quizIds = "Quiz 3";
                    number = 3;
                    break;
                case "quiz_4":
                    quizIds = "Quiz 4";
                    number = 4;
                    break;
                case "quiz_5":
                    quizIds = "Quiz 5";
                    number = 5;
                    break;
                case "quiz_6":
                    quizIds = "Quiz 6";
                    number = 6;
                    break;
                default:
                    quizIds = "Quiz 1";
                    number = 1;
                    break;
            }
        }

        String quizname = quizIds;
        String uuid = UUID.randomUUID().toString();

        // Common data map
        Map<String, Object> studentDataQuiz = new HashMap<>();
        studentDataQuiz.put("firstName", firstName);
        studentDataQuiz.put("lastName", lastName);
        studentDataQuiz.put("section", section);
        studentDataQuiz.put("gameType", "Quiz");
        studentDataQuiz.put("grade", grade);
        studentDataQuiz.put("timestamp", FieldValue.serverTimestamp());
        studentDataQuiz.put("quizno", quizname);
        studentDataQuiz.put("quizno_int", number);
        studentDataQuiz.put("quizscore", String.valueOf(Score));

        // Create a HashMap to store OnTimer mode data.
        HashMap<String, Object> OnTimerData = new HashMap<>();
        OnTimerData.put("firstName", firstName);
        OnTimerData.put("lastName", lastName);
        OnTimerData.put("section", section);
        OnTimerData.put("grade", grade);
        OnTimerData.put("gameType", "OnTimer");
        OnTimerData.put("quizno", "OnTimer");
        OnTimerData.put("quizno_int", number);
        OnTimerData.put("timestamp", FieldValue.serverTimestamp());
        OnTimerData.put("ontimer_difficulty", OnTimerDifficulty);
        OnTimerData.put("quizscore", String.valueOf(Score));

        // Create a HashMap to store Practice mode data.
        HashMap<String, Object> PracticeData = new HashMap<>();
        PracticeData.put("firstName", firstName);
        PracticeData.put("lastName", lastName);
        PracticeData.put("section", section);
        PracticeData.put("gameType", "Practice");
        PracticeData.put("grade", grade);
        PracticeData.put("quizno_int", number);
        PracticeData.put("quizno", "Practice" + "_" + operation);
        PracticeData.put("timestamp", FieldValue.serverTimestamp());
        PracticeData.put("practice_difficulty", OnTimerDifficulty);
        PracticeData.put("quizscore", String.valueOf(Score));

        // Create a HashMap for passing level data.
        HashMap<String, Object> passingData = new HashMap<>();
        passingData.put("firstName", firstName);
        passingData.put("gameType", "Passing");
        passingData.put("lastName", lastName);
        passingData.put("section", section);
        passingData.put("grade", grade);
        passingData.put("quizno_int", number);
        passingData.put("operation_type", operation);
        passingData.put("quizno", "Passing_" + levelNum + "_" + operation);
        passingData.put("timestamp", FieldValue.serverTimestamp());
        passingData.put("passing_level_must_complete", nextlevel);
        passingData.put("quizscore", String.valueOf(Score));

        // Determine star rating based on the score (for 10-question system)
        // 0 scores = 0 star, 1-4 scores = 1 star, 5-9 scores = 2 stars, 10 scores = 3 stars
        if (Score == 0) {
            starRating = "0 Stars";
        } else if (Score >= 1 && Score <= 4) {
            starRating = "1 Stars";
        } else if (Score >= 5 && Score <= 9) {
            starRating = "2 Stars";
        } else if (Score == 10) {
            starRating = "3 Stars";
        } else {
            // Fallback for any unexpected scores
            starRating = "0 Stars";
        }

        // Debug logging to help identify issues
        android.util.Log.d("StarRating", "Score: " + Score + ", Star Rating: " + starRating);

        // Prepare the new stars entry for the current level.
        String newStarsEntry = levelNum + "_" + starRating;
        // Create an initial starsHistory list with the new entry.
        List<String> starsHistory = new ArrayList<>();
        starsHistory.add(newStarsEntry);

        // Put the final starRating back into passingData
        passingData.put("stars_passing_" + levelNum, starRating);
        passingData.put("stars_list", starsHistory);

        if ("Passing".equals(gametype)) {
            CollectionReference collectionRef =
                    db.collection("Accounts").document("Students").collection("MathSquare");

            if ("0 Stars".equals(starRating)) {
                loadingDialog.dismiss();
                Toast.makeText(
                                this,
                                "Oops! You got 0 stars. Try again to save your score!",
                                Toast.LENGTH_LONG)
                        .show();
                return;
            }

            collectionRef
                    .whereEqualTo("firstName", firstName)
                    .whereEqualTo("lastName", lastName)
                    .whereEqualTo("gameType", "Passing")
                    .whereEqualTo("operation_type", operation)
                    .get()
                    .addOnCompleteListener(
                            task -> {
                                if (!task.isSuccessful()) {
                                    loadingDialog.dismiss();
                                    Toast.makeText(
                                                    this,
                                                    "Error fetching data: "
                                                            + task.getException().getMessage(),
                                                    Toast.LENGTH_LONG)
                                            .show();
                                    return;
                                }

                                if (!task.getResult().isEmpty()) {
                                    // Existing student
                                    for (DocumentSnapshot document : task.getResult()) {
                                        DocumentReference docRef =
                                                collectionRef.document(document.getId());

                                        docRef.get()
                                                .addOnSuccessListener(
                                                        snapshot -> {
                                                            if (!snapshot.exists()) return;

                                                            // Retrieve the current completed levels
                                                            List<String> completedLevels =
                                                                    (List<String>)
                                                                            snapshot.get(
                                                                                    "passing_completed_levels");
                                                            if (completedLevels == null)
                                                                completedLevels = new ArrayList<>();

                                                            // Merge the stars_list: get the
                                                            // existing list then update with the
                                                            // new entry.
                                                            List<String> existingStarsList =
                                                                    (List<String>)
                                                                            snapshot.get(
                                                                                    "stars_list");
                                                            if (existingStarsList == null) {
                                                                existingStarsList =
                                                                        new ArrayList<>();
                                                            }
                                                            // Remove any existing entry for the current level
                                                            Iterator<String> iterator = existingStarsList.iterator();
                                                            String oldEntry = null;
                                                            while (iterator.hasNext()) {
                                                                String entry = iterator.next();
                                                                if (entry.startsWith(levelNum + "_")) {
                                                                    oldEntry = entry;
                                                                    iterator.remove();
                                                                    android.util.Log.d("StarUpdate", "Removed old entry: " + oldEntry);
                                                                }
                                                            }
                                                            // Add the new entry
                                                            existingStarsList.add(newStarsEntry);
                                                            android.util.Log.d("StarUpdate", "Added new entry: " + newStarsEntry +
                                                                " (Level: " + levelNum + ", Score: " + Score + ", Stars: " + starRating + ")");
                                                            // Sort the list numerically based on
                                                            // the numeric part of the level string
                                                            Collections.sort(
                                                                    existingStarsList,
                                                                    (a, b) -> {
                                                                        // Add null checks to prevent crashes
                                                                        if (a == null && b == null) return 0;
                                                                        if (a == null) return -1;
                                                                        if (b == null) return 1;

                                                                        try {
                                                                            int numA = Integer.parseInt(a.replaceAll("\\D+", ""));
                                                                            int numB = Integer.parseInt(b.replaceAll("\\D+", ""));
                                                                            return Integer.compare(numA, numB);
                                                                        } catch (NumberFormatException e) {
                                                                            // If parsing fails, fall back to string comparison
                                                                            return a.compareTo(b);
                                                                        }
                                                                    });

                                                            // Add level to completed levels if not already there
                                                            if (!completedLevels.contains(levelNum)) {
                                                                completedLevels.add(levelNum);
                                                                Collections.sort(
                                                                        completedLevels,
                                                                        (a, b) -> {
                                                                            // Add null checks to prevent crashes
                                                                            if (a == null && b == null) return 0;
                                                                            if (a == null) return -1;
                                                                            if (b == null) return 1;

                                                                            try {
                                                                                int na = Integer.parseInt(a.replaceAll("\\D+", ""));
                                                                                int nb = Integer.parseInt(b.replaceAll("\\D+", ""));
                                                                                return Integer.compare(na, nb);
                                                                            } catch (NumberFormatException e) {
                                                                                // If parsing fails, fall back to string comparison
                                                                                return a.compareTo(b);
                                                                            }
                                                                        });
                                                            }

                                                            // Always update the database with the new star rating (even for retakes)
                                                            docRef.update(
                                                                            "passing_completed_levels",
                                                                                    completedLevels,
                                                                            "stars_list",
                                                                                    existingStarsList,
                                                                            "passing_level_must_complete",
                                                                                    nextlevel)
                                                                    .addOnSuccessListener(
                                                                            aVoid ->
                                                                                    Toast
                                                                                            .makeText(
                                                                                                    this,
                                                                                                    "Star rating updated successfully!",
                                                                                                    Toast
                                                                                                            .LENGTH_SHORT)
                                                                                            .show())
                                                                    .addOnFailureListener(
                                                                            e ->
                                                                                    Toast
                                                                                            .makeText(
                                                                                                    this,
                                                                                                    "Update failed: "
                                                                                                            + e
                                                                                                                    .getMessage(),
                                                                                                    Toast
                                                                                                            .LENGTH_SHORT)
                                                                                            .show());

                                                            // Create a new PassingHistory entry
                                                            // that contains the new stars entry.
                                                            Map<String, Object> historyData =
                                                                    new HashMap<>();
                                                            historyData.put("level", levelNum);
                                                            historyData.put("score", Score);
                                                            historyData.put("stars", starRating);
                                                            historyData.put(
                                                                    "timestamp",
                                                                    FieldValue.serverTimestamp());

                                                            docRef.collection("PassingHistory")
                                                                    .add(historyData)
                                                                    .addOnSuccessListener(
                                                                            aVoid -> {
                                                                                loadingDialog
                                                                                        .dismiss();
                                                                                Toast.makeText(
                                                                                                this,
                                                                                                "Passing history saved",
                                                                                                Toast
                                                                                                        .LENGTH_SHORT)
                                                                                        .show();
                                                                            })
                                                                    .addOnFailureListener(
                                                                            e -> {
                                                                                loadingDialog
                                                                                        .dismiss();
                                                                                Toast.makeText(
                                                                                                this,
                                                                                                "History save failed: "
                                                                                                        + e
                                                                                                                .getMessage(),
                                                                                                Toast
                                                                                                        .LENGTH_LONG)
                                                                                        .show();
                                                                            });
                                                        });
                                    }
                                } else {
                                    // New student: create main record + first history entry
                                    passingData.put(
                                            "passing_completed_levels", Arrays.asList(levelNum));

                                    collectionRef
                                            .add(passingData)
                                            .addOnSuccessListener(
                                                    docRef -> {
                                                        // Build first history entry
                                                        Map<String, Object> historyData =
                                                                new HashMap<>();
                                                        historyData.put("level", levelNum);
                                                        historyData.put("score", Score);
                                                        historyData.put("stars", starRating);
                                                        historyData.put(
                                                                "timestamp",
                                                                FieldValue.serverTimestamp());

                                                        docRef.collection("PassingHistory")
                                                                .add(historyData)
                                                                .addOnSuccessListener(
                                                                        aVoid -> {
                                                                            loadingDialog.dismiss();
                                                                            Toast.makeText(
                                                                                            this,
                                                                                            "Student and history created",
                                                                                            Toast
                                                                                                    .LENGTH_SHORT)
                                                                                    .show();
                                                                        })
                                                                .addOnFailureListener(
                                                                        e -> {
                                                                            loadingDialog.dismiss();
                                                                            Toast.makeText(
                                                                                            this,
                                                                                            "Failed saving history: "
                                                                                                    + e
                                                                                                            .getMessage(),
                                                                                            Toast
                                                                                                    .LENGTH_LONG)
                                                                                    .show();
                                                                        });
                                                    })
                                            .addOnFailureListener(
                                                    e -> {
                                                        loadingDialog.dismiss();
                                                        Toast.makeText(
                                                                        this,
                                                                        "Error creating student: "
                                                                                + e.getMessage(),
                                                                        Toast.LENGTH_LONG)
                                                                .show();
                                                    });
                                }
                            });
        } else if ("Quiz".equals(gametype)) {

            CollectionReference collectionRef =
                    db.collection("Accounts").document("Students").collection("MathSquare");

            // Calculate attempt number by counting previous attempts for this quiz
            collectionRef
                    .whereEqualTo("firstName", firstName)
                    .whereEqualTo("lastName", lastName)
                    .whereEqualTo("gameType", "Quiz")
                    .whereEqualTo("quizno_int", number)
                    .get()
                    .addOnCompleteListener(
                            task -> {
                                loadingDialog.dismiss();
                                if (!task.isSuccessful()) {
                                    Toast.makeText(
                                                    this,
                                                    "Error checking student data: "
                                                            + task.getException().getMessage(),
                                                    Toast.LENGTH_LONG)
                                            .show();
                                    return;
                                }

                                // Calculate attempt number (1st attempt, 2nd attempt, etc.)
                                int attemptNumber = task.getResult().size() + 1;
                                studentDataQuiz.put("attemptNumber", attemptNumber);
                                
                                // Always add new record to track all attempts
                                    collectionRef
                                            .add(studentDataQuiz)
                                            .addOnSuccessListener(
                                                    aVoid ->
                                                            Toast.makeText(
                                                                            this,
                                                                        "Quiz attempt #" + attemptNumber + " saved",
                                                                            Toast.LENGTH_SHORT)
                                                                    .show())
                                            .addOnFailureListener(
                                                    e ->
                                                            Toast.makeText(
                                                                            this,
                                                                        "Error saving attempt: "
                                                                                    + e
                                                                                            .getMessage(),
                                                                            Toast.LENGTH_LONG)
                                                                    .show());
                            })
                    .addOnFailureListener(
                            e -> {
                                loadingDialog.dismiss();
                                Toast.makeText(
                                                this,
                                                "Error fetching student data: " + e.getMessage(),
                                                Toast.LENGTH_LONG)
                                        .show();
                            });

        } else if ("OnTimer".equals(gametype)) {
            // Process for OnTimer game type.
            CollectionReference collectionRef =
                    db.collection("Accounts").document("Students").collection("MathSquare");

            // Query for an existing OnTimer record matching the student's difficulty level.
            collectionRef
                    .whereEqualTo("firstName", firstName)
                    .whereEqualTo("lastName", lastName)
                    .whereEqualTo("gameType", "OnTimer")
                    .get()
                    .addOnCompleteListener(
                            task -> {
                                if (task.isSuccessful()) {
                                    if (!task.getResult().isEmpty()) {
                                        // Update the existing OnTimer document.
                                        for (DocumentSnapshot document : task.getResult()) {
                                            collectionRef
                                                    .document(document.getId())
                                                    .set(OnTimerData)
                                                    .addOnSuccessListener(
                                                            aVoid -> {
                                                                loadingDialog.dismiss();
                                                                Toast.makeText(
                                                                                this,
                                                                                "On Timer Score updated successfully",
                                                                                Toast.LENGTH_SHORT)
                                                                        .show();
                                                            })
                                                    .addOnFailureListener(
                                                            e -> {
                                                                loadingDialog.dismiss();
                                                                Toast.makeText(
                                                                                this,
                                                                                "Error updating: "
                                                                                        + e
                                                                                                .getMessage(),
                                                                                Toast.LENGTH_LONG)
                                                                        .show();
                                                            });
                                        }
                                    } else {
                                        // No existing document found; simply dismiss the loading
                                        // dialog.
                                        loadingDialog.dismiss();
                                    }
                                } else {
                                    // In case of failure, show an error and attempt to add a new
                                    // OnTimer document.
                                    Toast.makeText(
                                                    this,
                                                    "Error checking student data: "
                                                            + task.getException().getMessage(),
                                                    Toast.LENGTH_LONG)
                                            .show();
                                    collectionRef
                                            .add(OnTimerData)
                                            .addOnSuccessListener(
                                                    aVoid -> {
                                                        loadingDialog.dismiss();
                                                        Toast.makeText(
                                                                        this,
                                                                        "New On Timer Score added",
                                                                        Toast.LENGTH_SHORT)
                                                                .show();
                                                    })
                                            .addOnFailureListener(
                                                    e -> {
                                                        loadingDialog.dismiss();
                                                        Toast.makeText(
                                                                        this,
                                                                        "Error updating: "
                                                                                + e.getMessage(),
                                                                        Toast.LENGTH_LONG)
                                                                .show();
                                                    });
                                }
                            })
                    .addOnFailureListener(
                            e -> {
                                loadingDialog.dismiss();
                                Toast.makeText(
                                                this,
                                                "Error fetching student data: " + e.getMessage(),
                                                Toast.LENGTH_LONG)
                                        .show();
                            });

        } else if ("Practice".equals(gametype)) {
            // Process for Practice mode (note: "Practicd" may be a typo for "Practice").
            CollectionReference collectionRef =
                    db.collection("Accounts").document("Students").collection("MathSquare");

            // Query for an existing Practice record where practice_score is set to "None".
            collectionRef
                    .whereEqualTo("firstName", firstName)
                    .whereEqualTo("lastName", lastName)
                    .whereEqualTo("gameType", "Practice")
                    .get()
                    .addOnCompleteListener(
                            task -> {
                                if (task.isSuccessful()) {
                                    if (!task.getResult().isEmpty()) {
                                        // If found, update the existing Practice document.
                                        for (DocumentSnapshot document : task.getResult()) {
                                            collectionRef
                                                    .document(document.getId())
                                                    .set(PracticeData)
                                                    .addOnSuccessListener(
                                                            aVoid -> {
                                                                loadingDialog.dismiss();
                                                                Toast.makeText(
                                                                                this,
                                                                                "Practice Score updated successfully",
                                                                                Toast.LENGTH_SHORT)
                                                                        .show();
                                                            })
                                                    .addOnFailureListener(
                                                            e -> {
                                                                loadingDialog.dismiss();
                                                                Toast.makeText(
                                                                                this,
                                                                                "Error updating: "
                                                                                        + e
                                                                                                .getMessage(),
                                                                                Toast.LENGTH_LONG)
                                                                        .show();
                                                            });
                                        }
                                    } else {
                                        // If no matching record is found, simply dismiss the
                                        // loading dialog.
                                        loadingDialog.dismiss();
                                    }
                                } else {
                                    // On query failure, show an error and attempt to add a new
                                    // Practice document.
                                    Toast.makeText(
                                                    this,
                                                    "Error checking student data: "
                                                            + task.getException().getMessage(),
                                                    Toast.LENGTH_LONG)
                                            .show();
                                    collectionRef
                                            .add(PracticeData)
                                            .addOnSuccessListener(
                                                    aVoid -> {
                                                        loadingDialog.dismiss();
                                                        Toast.makeText(
                                                                        this,
                                                                        "New Practice Score added",
                                                                        Toast.LENGTH_SHORT)
                                                                .show();
                                                    })
                                            .addOnFailureListener(
                                                    e -> {
                                                        loadingDialog.dismiss();
                                                        Toast.makeText(
                                                                        this,
                                                                        "Error updating: "
                                                                                + e.getMessage(),
                                                                        Toast.LENGTH_LONG)
                                                                .show();
                                                    });
                                }
                            })
                    .addOnFailureListener(
                            e -> {
                                loadingDialog.dismiss();
                                Toast.makeText(
                                                this,
                                                "Error fetching student data: " + e.getMessage(),
                                                Toast.LENGTH_LONG)
                                        .show();
                            });
        } else {
            // If none of the gametypes match, simply dismiss the loading dialog.
            loadingDialog.dismiss();
        }
    }

    private void showMistakesDialog() {
        if (answeredQuestions == null || answeredQuestions.isEmpty()) {
            Toast.makeText(this, "No questions data available", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_mistakes);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout mistakesContainer = dialog.findViewById(R.id.mistakesContainer);
        Button closeButton = dialog.findViewById(R.id.btnClose);

        // Count mistakes
        boolean hasMistakes = false;
        for (MathProblem problem : answeredQuestions) {
            if (!problem.isCorrect()) {
                hasMistakes = true;
                View mistakeView = LayoutInflater.from(this).inflate(R.layout.item_mistake, mistakesContainer, false);

                TextView questionText = mistakeView.findViewById(R.id.tvQuestion);
                TextView yourAnswerText = mistakeView.findViewById(R.id.tvYourAnswer);
                TextView correctAnswerText = mistakeView.findViewById(R.id.tvCorrectAnswer);

                // Set the texts
                questionText.setText(problem.getFormattedQuestion());
                yourAnswerText.setText(problem.getUserAnswer());
                correctAnswerText.setText(problem.getCorrectAnswer());

                mistakesContainer.addView(mistakeView);
            }
        }

        // If no mistakes, show congratulatory message
        if (!hasMistakes) {
            View perfectScoreView = LayoutInflater.from(this).inflate(R.layout.item_perfect_score, mistakesContainer, false);
            mistakesContainer.addView(perfectScoreView);
        }

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound("click.mp3");
                dialog.dismiss();
            }
        });

        dialog.show();
    }


}
