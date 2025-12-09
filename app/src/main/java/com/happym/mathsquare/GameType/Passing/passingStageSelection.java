package com.happym.mathsquare.GameType.Passing;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.WindowCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.happym.mathsquare.MusicManager;
import com.happym.mathsquare.dashboard_StudentsPanel;
import com.happym.mathsquare.dashboard_SectionPanel;
import com.happym.mathsquare.dialog.CreateSection;
import com.happym.mathsquare.MultipleChoicePage;

import com.happym.mathsquare.R;
import java.io.IOException;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import com.happym.mathsquare.sharedPreferences;
import com.happym.mathsquare.Animation.*;

public class passingStageSelection extends AppCompatActivity {

    private FrameLayout levelone, leveltwo, levelthree;
    private ImageView flashone, flashtwo, flashthree;
    private ImageView levelOneStar, levelTwoStar, levelThreeStar;

    private MediaPlayer soundEffectPlayer;
    private String difficultySection, passingNextLevel;
    private FrameLayout numberContainer,backgroundFrame;
    private NumBGAnimation numBGAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.passing_levels);

        // Initialize the levels
        String operation = getIntent().getStringExtra("operation");
        String difficulty = getIntent().getStringExtra("difficulty");
        String passingWorldType = getIntent().getStringExtra("passing");
        boolean reloadProgress = getIntent().getBooleanExtra("reload_progress", false);

        AppCompatButton btnNext = findViewById(R.id.btn_next);
        AppCompatButton btnBack = findViewById(R.id.btn_back);

        animateButtonFocus(btnNext);
        animateButtonFocus(btnBack);

        difficultySection = (difficulty != null && !difficulty.isEmpty()) ? difficulty : "Easy";

        btnNext.setVisibility(View.GONE);
        btnBack.setVisibility(View.GONE);

        btnNext.setOnClickListener(v -> {
            animateButtonClick(btnNext);
            stopButtonFocusAnimation(btnNext);
            animateButtonFocus(btnBack);
            playSound("click.mp3");
            btnNext.setVisibility(View.GONE);
            btnBack.setVisibility(View.VISIBLE);
        });

        btnBack.setOnClickListener(v -> {
            animateButtonClick(btnBack);
            stopButtonFocusAnimation(btnBack);
            animateButtonFocus(btnNext);
            playSound("click.mp3");
            btnNext.setVisibility(View.VISIBLE);
            btnBack.setVisibility(View.GONE);
        });

        levelone = findViewById(R.id.level_one);
        leveltwo = findViewById(R.id.level_two);
        levelthree = findViewById(R.id.level_three);

        flashone = findViewById(R.id.level_one_flash_box);
        flashtwo = findViewById(R.id.level_two_flash_box);
        flashthree = findViewById(R.id.level_three_flash_box);

        levelOneStar = findViewById(R.id.level_one_stars);
        levelTwoStar = findViewById(R.id.level_two_stars);
        levelThreeStar = findViewById(R.id.level_three_stars);

        ImageView operationDisplayIcon = findViewById(R.id.difficultyImage);

        if ("Addition".equals(operation)) {
            operationDisplayIcon.setImageResource(R.drawable.ic_operation_add);
        } else if ("Subtraction".equals(operation)) {
            operationDisplayIcon.setImageResource(R.drawable.ic_operation_subtract);
        } else if ("Multiplication".equals(operation)) {
            operationDisplayIcon.setImageResource(R.drawable.ic_operation_multiply);
        } else if ("Division".equals(operation)) {
            operationDisplayIcon.setImageResource(R.drawable.ic_operation_divide);
        } else if ("Percentage".equals(operation)) {
            operationDisplayIcon.setImageResource(R.drawable.ic_operation_percentage);
        } else if ("Decimal".equals(operation)) {
            operationDisplayIcon.setImageResource(R.drawable.ic_operation_decimal);
        } else if ("DecimalAddition".equals(operation)) {
            operationDisplayIcon.setImageResource(R.drawable.ic_operation_add);
        } else if ("DecimalSubtraction".equals(operation)) {
            operationDisplayIcon.setImageResource(R.drawable.ic_operation_subtract);
        } else if ("DecimalMultiplication".equals(operation)) {
            operationDisplayIcon.setImageResource(R.drawable.ic_operation_multiply);
        } else if ("DecimalDivision".equals(operation)) {
            operationDisplayIcon.setImageResource(R.drawable.ic_operation_divide);
        } else {
            // Default icon if no match is found
            operationDisplayIcon.setImageResource(R.drawable.btn_operation_add);
        }

        animateButtonFocus(levelone);
        animateButtonFocus(flashone);

        // Array of game types
        String[] gameTypes = {
            "passing_level_1", "passing_level_2", "passing_level_3"
        };

        // Add levels and flashboxes to arrays for iteration
        FrameLayout[] levels = {
            levelone, leveltwo, levelthree
        };

        ImageView[] flashboxes = {
            flashone, flashtwo, flashthree
        };

        ImageView[] starViews = {
            levelOneStar, levelTwoStar, levelThreeStar
        };

        for (int i = 0; i < levels.length; i++) {
            String previousLevel = "level_" + (i);
            String currentLevel = "level_" + (i + 1);
            String nextLevelToUnlock = "level_" + (i + 2);
            FrameLayout level = levels[i];
            ImageView flashbox = flashboxes[i];

            if (i == 0) {
                // For level 1 (index 0), set as available and flash
                level.setContentDescription("Available");
                level.setBackgroundResource(R.drawable.btn_short_condition); // Resource for available state
                startFlashingAnimation(flashbox);
            } else {
                // For all other levels, set as not available
                level.setContentDescription("Not_Available");
                level.setBackgroundResource(R.drawable.btn_short_condition_off);
            }

            // Handle level click actions based on the content description
            String levelState = (String) level.getContentDescription();
            if ("Available".equals(levelState)) {
                level.setOnClickListener(v -> {
                    playSound("click.mp3");

                    Intent intent = new Intent(passingStageSelection.this, MultipleChoicePage.class);
                    intent.putExtra("operation", operation);
                    intent.putExtra("passing", currentLevel);
                    intent.putExtra("game_type", "Passing");
                    intent.putExtra("passing_world", "world_one");
                    intent.putExtra("passing_next_level", nextLevelToUnlock);
                    intent.putExtra("questionLimit", 10);
                    intent.putExtra("difficulty", difficultySection);

                    animateButtonClick(level);
                    stopButtonFocusAnimation(level);
                    startActivity(intent);
                });
            } else {
                level.setOnClickListener(v ->
                    Toast.makeText(this, "Complete previous " + previousLevel + " to unlock.", Toast.LENGTH_SHORT).show()
                );
            }
        }
        // Get a reference to the root view for Snackbar, e.g., the activity's content view.
        View rootView = findViewById(android.R.id.content);

        // Create and show a loading Snackbar.
        final Snackbar loadingSnackbar = Snackbar.make(rootView, "Loading progress, please wait...", Snackbar.LENGTH_INDEFINITE);
        loadingSnackbar.show();

        // Record the start time to measure how long the data fetch takes.
        final long startTime = System.currentTimeMillis();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String section = sharedPreferences.getSection(this);
        String grade = sharedPreferences.getGrade(this);
        String firstName = sharedPreferences.getFirstN(this);
        String lastName = sharedPreferences.getLastN(this);

        CollectionReference collectionRef = db.collection("Accounts")
            .document("Students")
            .collection("MathSquare");

        Query query = collectionRef
            .whereEqualTo("firstName", firstName)
            .whereEqualTo("lastName", lastName)
            .whereEqualTo("section", section)
            .whereEqualTo("gameType", "Passing")
            .whereEqualTo("grade", grade)
            .whereEqualTo("operation_type", operation);

        if (reloadProgress) {
            // Force re-fetch
            query.get()
                   .addOnSuccessListener(queryDocumentSnapshots -> {

                // Same inside here
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed > 3000) {
                    Snackbar.make(rootView, "Slow connection detected. Data loaded in " + elapsed / 1000 + " seconds.",
                            Snackbar.LENGTH_LONG).show();
                }

                if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                    for (int i = 0; i < levels.length; i++) {
                        String previousLevel = "level_" + (i);
                        String currentLevel = "level_" + (i + 1);
                        String nextLevelToUnlock = "level_" + (i + 2);
                        FrameLayout level = levels[i];
                        ImageView flashbox = flashboxes[i];

                        if (i == 0) {
                            level.setContentDescription("Available");
                            level.setBackgroundResource(R.drawable.btn_short_condition);
                            startFlashingAnimation(flashbox);
                        } else {
                            level.setContentDescription("Not_Available");
                            level.setBackgroundResource(R.drawable.btn_short_condition_off);
                        }

                        String levelState = (String) level.getContentDescription();
                        if ("Available".equals(levelState)) {
                            level.setOnClickListener(v -> {
                                playSound("click.mp3");
                                Intent intent = new Intent(passingStageSelection.this, MultipleChoicePage.class);
                                intent.putExtra("operation", operation);
                                intent.putExtra("passing", currentLevel);
                                intent.putExtra("game_type", "Passing");
                                intent.putExtra("passing_world", "world_one");
                                intent.putExtra("passing_next_level", nextLevelToUnlock);
                                intent.putExtra("questionLimit", 10);
                                intent.putExtra("difficulty", difficultySection);
                                animateButtonClick(level);
                                stopButtonFocusAnimation(level);
                                startActivity(intent);
                            });
                        } else {
                            level.setOnClickListener(v ->
                                    Toast.makeText(this, "Complete previous " + previousLevel + " to unlock.", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                    loadingSnackbar.dismiss();
                    return;
                }

                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    String docId = document.getId();
                    DocumentReference studentDocRef = collectionRef.document(docId);

                    passingNextLevel = document.getString("passing_level_must_complete");

                    List<String> completedLevels = new ArrayList<>();
                    Map<String, String> starsPerLevel = new HashMap<>();

                    // Get completed levels and stars from the main document
                    List<String> levelList = (List<String>) document.get("passing_completed_levels");
                    if (levelList != null) {
                        completedLevels.addAll(levelList);
                    }

                    List<String> starsList = (List<String>) document.get("stars_list");
                    if (starsList != null) {
                        for (String entry : starsList) {
                            int lastUnderscore = entry.lastIndexOf('_');
                            if (lastUnderscore > 0) {
                                String levelKey = entry.substring(0, lastUnderscore);
                                String starRating = entry.substring(lastUnderscore + 1);
                                starsPerLevel.put(levelKey, starRating);
                                android.util.Log.d("StarDisplay", "Loaded star (reload): " + levelKey + " = " + starRating);
                            }
                        }
                    }

                    // Now fetch PassingHistory once (for future use if needed)
                    studentDocRef.collection("PassingHistory")
                        .get()
                        .addOnSuccessListener(historySnapshots -> {
                            // History processing can be added here if needed in the future

                            int maxCompleted = completedLevels.stream()
                                    .map(lvl -> {
                                        String num = lvl.replaceAll("\\D+", "");
                                        return num.isEmpty() ? 0 : Integer.parseInt(num);
                                    })
                                    .max(Integer::compare).orElse(0);

                            if (passingNextLevel == null || passingNextLevel.isEmpty()) {
                                passingNextLevel = "level_" + (maxCompleted + 1);
                            }
                            int availableIndex = Integer.parseInt(passingNextLevel.replaceAll("\\D+", "")) - 1;

                            for (int i = 0; i < levels.length; i++) {
                                final int currentIndex = i;  // Create final copy outside the lambda
                                String previousLevel = "level_" + (currentIndex);
                                String currentLevel = "level_" + (currentIndex + 1);
                                String nextLevelToUnlock = "level_" + (currentIndex + 2);
                                FrameLayout level = levels[currentIndex];
                                ImageView flashbox = flashboxes[currentIndex];

                                boolean isCompleted = completedLevels.contains(currentLevel);
                                boolean isAvailable = (i == availableIndex);

                                startFlashingAnimation(flashbox);

                                if (isCompleted) {
                                    level.setContentDescription("Completed");
                                    level.setBackgroundResource(R.drawable.btn_short_condition);
                                    flashbox.setImageResource(R.drawable.transparent_box);

                                    String starsEarned = starsPerLevel.get(currentLevel);
                                    if ("1 Stars".equals(starsEarned)) {
                                        starViews[currentIndex].setImageResource(R.drawable.ic_star_one);
                                    } else if ("2 Stars".equals(starsEarned)) {
                                        starViews[currentIndex].setImageResource(R.drawable.ic_star_two);
                                    } else if ("3 Stars".equals(starsEarned)) {
                                        starViews[currentIndex].setImageResource(R.drawable.ic_star_three);
                                    } else {
                                        // No stars or unknown rating - use the none/empty star image
                                        starViews[currentIndex].setImageResource(R.drawable.ic_star_none);
                                    }

                                    // Check if this level has 3 stars before making next level available
                                    if (currentIndex < levels.length - 1) {  // If not the last level
                                        if (!"3 Stars".equals(starsEarned)) {
                                            // If current level doesn't have 3 stars, make next level unavailable
                                            levels[currentIndex + 1].setContentDescription("Not_Available");
                                            levels[currentIndex + 1].setBackgroundResource(R.drawable.btn_short_condition_off);
                                            flashboxes[currentIndex + 1].setImageResource(R.drawable.transparent_box);
                                        }
                                    }
                                } else if (isAvailable) {
                                    // Check if previous level has 3 stars
                                    if (currentIndex > 0) {  // If not the first level
                                        String previousLevelStars = starsPerLevel.get("level_" + currentIndex);
                                        if (!"3 Stars".equals(previousLevelStars)) {
                                            level.setContentDescription("Not_Available");
                                            level.setBackgroundResource(R.drawable.btn_short_condition_off);
                                            flashbox.setImageResource(R.drawable.transparent_box);
                                            continue;  // Skip the rest of this iteration
                                        }
                                    }

                                    level.setContentDescription("Available");
                                    level.setBackgroundResource(R.drawable.btn_short_condition);
                                    flashbox.setImageResource(R.drawable.white_box);
                                    startFlashingAnimation(flashbox);

                                    boolean streak3 = isOnStarStreak(starsPerLevel, 1, 3);

                                    if (streak3) {
                                        showStarStreakDialog("Awesome! You got 3 Stars in a row! Keep it up, star champ!");
                                    }
                                } else {
                                    level.setContentDescription("Not_Available");
                                    level.setBackgroundResource(R.drawable.btn_short_condition_off);
                                }

                                level.setOnClickListener(v -> {
                                    String state = (String) level.getContentDescription();
                                    if ("Available".equals(state) || "Completed".equals(state)) {
                                        playSound("click.mp3");
                                        Intent intent = new Intent(passingStageSelection.this, MultipleChoicePage.class);
                                        intent.putExtra("operation", operation);
                                        intent.putExtra("passing", currentLevel);
                                        intent.putExtra("game_type", "Passing");
                                        intent.putExtra("passing_world", "world_one");
                                        intent.putExtra("passing_next_level", nextLevelToUnlock);
                                        intent.putExtra("questionLimit", 10);
                                        intent.putExtra("difficulty", difficultySection);
                                        animateButtonClick(level);
                                        stopButtonFocusAnimation(level);
                                        startActivity(intent);
                                    } else {
                                        if (currentIndex > 0) {  // If not the first level
                                            String previousLevelStars = starsPerLevel.get("level_" + currentIndex);
                                            if (!"3 Stars".equals(previousLevelStars)) {
                                                Toast.makeText(passingStageSelection.this,
                                                        "You need 3 stars in level " + currentIndex + " to unlock this level!",
                                                        Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        }
                                        Toast.makeText(passingStageSelection.this,
                                                "Complete previous " + previousLevel + " to unlock.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                                loadingSnackbar.dismiss();
                            }
                        })
                        .addOnFailureListener(historyError -> {
                            Toast.makeText(this, "Error loading history: " + historyError.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                }

            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            // Just normal load
            query.get()
                   .addOnSuccessListener(queryDocumentSnapshots -> {

                // Same inside here
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed > 3000) {
                    Snackbar.make(rootView, "Slow connection detected. Data loaded in " + elapsed / 1000 + " seconds.",
                            Snackbar.LENGTH_LONG).show();
                }

                if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                    for (int i = 0; i < levels.length; i++) {
                        String previousLevel = "level_" + (i);
                        String currentLevel = "level_" + (i + 1);
                        String nextLevelToUnlock = "level_" + (i + 2);
                        FrameLayout level = levels[i];
                        ImageView flashbox = flashboxes[i];

                        if (i == 0) {
                            level.setContentDescription("Available");
                            level.setBackgroundResource(R.drawable.btn_short_condition);
                            startFlashingAnimation(flashbox);
                        } else {
                            level.setContentDescription("Not_Available");
                            level.setBackgroundResource(R.drawable.btn_short_condition_off);
                        }

                        String levelState = (String) level.getContentDescription();
                        if ("Available".equals(levelState)) {
                            level.setOnClickListener(v -> {
                                playSound("click.mp3");
                                Intent intent = new Intent(passingStageSelection.this, MultipleChoicePage.class);
                                intent.putExtra("operation", operation);
                                intent.putExtra("passing", currentLevel);
                                intent.putExtra("game_type", "Passing");
                                intent.putExtra("passing_world", "world_one");
                                intent.putExtra("passing_next_level", nextLevelToUnlock);
                                intent.putExtra("questionLimit", 10);
                                    intent.putExtra("difficulty", difficultySection);
                                animateButtonClick(level);
                                stopButtonFocusAnimation(level);
                                startActivity(intent);
                            });
                        } else {
                            level.setOnClickListener(v ->
                                    Toast.makeText(this, "Complete previous " + previousLevel + " to unlock.", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                    loadingSnackbar.dismiss();
                    return;
                }

                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    String docId = document.getId();
                    DocumentReference studentDocRef = collectionRef.document(docId);

                    passingNextLevel = document.getString("passing_level_must_complete");

                    List<String> completedLevels = new ArrayList<>();
                    Map<String, String> starsPerLevel = new HashMap<>();

                    // Get completed levels and stars from the main document
                    List<String> levelList = (List<String>) document.get("passing_completed_levels");
                    if (levelList != null) {
                        completedLevels.addAll(levelList);
                    }

                    List<String> starsList = (List<String>) document.get("stars_list");
                    if (starsList != null) {
                        for (String entry : starsList) {
                            int lastUnderscore = entry.lastIndexOf('_');
                            if (lastUnderscore > 0) {
                                String levelKey = entry.substring(0, lastUnderscore);
                                String starRating = entry.substring(lastUnderscore + 1);
                                starsPerLevel.put(levelKey, starRating);
                                android.util.Log.d("StarDisplay", "Loaded star: " + levelKey + " = " + starRating);
                            }
                        }
                    }

                    // Now fetch PassingHistory once (for future use if needed)
                    studentDocRef.collection("PassingHistory")
                        .get()
                        .addOnSuccessListener(historySnapshots -> {
                            // History processing can be added here if needed in the future

                            int maxCompleted = completedLevels.stream()
                                    .map(lvl -> {
                                        String num = lvl.replaceAll("\\D+", "");
                                        return num.isEmpty() ? 0 : Integer.parseInt(num);
                                    })
                                    .max(Integer::compare).orElse(0);

                            if (passingNextLevel == null || passingNextLevel.isEmpty()) {
                                passingNextLevel = "level_" + (maxCompleted + 1);
                            }
                            int availableIndex = Integer.parseInt(passingNextLevel.replaceAll("\\D+", "")) - 1;

                            for (int i = 0; i < levels.length; i++) {
                                final int currentIndex = i;  // Create final copy outside the lambda
                                String previousLevel = "level_" + (currentIndex);
                                String currentLevel = "level_" + (currentIndex + 1);
                                String nextLevelToUnlock = "level_" + (currentIndex + 2);
                                FrameLayout level = levels[currentIndex];
                                ImageView flashbox = flashboxes[currentIndex];

                                boolean isCompleted = completedLevels.contains(currentLevel);
                                boolean isAvailable = (i == availableIndex);

                                startFlashingAnimation(flashbox);

                                if (isCompleted) {
                                    level.setContentDescription("Completed");
                                    level.setBackgroundResource(R.drawable.btn_short_condition);
                                    flashbox.setImageResource(R.drawable.transparent_box);

                                    String starsEarned = starsPerLevel.get(currentLevel);
                                    if ("1 Stars".equals(starsEarned)) {
                                        starViews[currentIndex].setImageResource(R.drawable.ic_star_one);
                                    } else if ("2 Stars".equals(starsEarned)) {
                                        starViews[currentIndex].setImageResource(R.drawable.ic_star_two);
                                    } else if ("3 Stars".equals(starsEarned)) {
                                        starViews[currentIndex].setImageResource(R.drawable.ic_star_three);
                                    } else {
                                        // No stars or unknown rating - use the none/empty star image
                                        starViews[currentIndex].setImageResource(R.drawable.ic_star_none);
                                    }

                                    // Check if this level has 3 stars before making next level available
                                    if (currentIndex < levels.length - 1) {  // If not the last level
                                        if (!"3 Stars".equals(starsEarned)) {
                                            // If current level doesn't have 3 stars, make next level unavailable
                                            levels[currentIndex + 1].setContentDescription("Not_Available");
                                            levels[currentIndex + 1].setBackgroundResource(R.drawable.btn_short_condition_off);
                                            flashboxes[currentIndex + 1].setImageResource(R.drawable.transparent_box);
                                        }
                                    }
                                } else if (isAvailable) {
                                    // Check if previous level has 3 stars
                                    if (currentIndex > 0) {  // If not the first level
                                        String previousLevelStars = starsPerLevel.get("level_" + currentIndex);
                                        if (!"3 Stars".equals(previousLevelStars)) {
                                            level.setContentDescription("Not_Available");
                                            level.setBackgroundResource(R.drawable.btn_short_condition_off);
                                            flashbox.setImageResource(R.drawable.transparent_box);
                                            continue;  // Skip the rest of this iteration
                                        }
                                    }

                                    level.setContentDescription("Available");
                                    level.setBackgroundResource(R.drawable.btn_short_condition);
                                    flashbox.setImageResource(R.drawable.white_box);
                                    startFlashingAnimation(flashbox);

                                    boolean streak3 = isOnStarStreak(starsPerLevel, 1, 3);

                                    if (streak3) {
                                        showStarStreakDialog("Awesome! You got 3 Stars in a row! Keep it up, star champ!");
                                    }
                                } else {
                                    level.setContentDescription("Not_Available");
                                    level.setBackgroundResource(R.drawable.btn_short_condition_off);
                                }

                                level.setOnClickListener(v -> {
                                    String state = (String) level.getContentDescription();
                                    if ("Available".equals(state) || "Completed".equals(state)) {
                                        playSound("click.mp3");
                                        Intent intent = new Intent(passingStageSelection.this, MultipleChoicePage.class);
                                        intent.putExtra("operation", operation);
                                        intent.putExtra("passing", currentLevel);
                                        intent.putExtra("game_type", "Passing");
                                        intent.putExtra("passing_world", "world_one");
                                        intent.putExtra("passing_next_level", nextLevelToUnlock);
                                        intent.putExtra("questionLimit", 10);
                                        intent.putExtra("difficulty", difficultySection);
                                        animateButtonClick(level);
                                        stopButtonFocusAnimation(level);
                                        startActivity(intent);
                                    } else {
                                        if (currentIndex > 0) {  // If not the first level
                                            String previousLevelStars = starsPerLevel.get("level_" + currentIndex);
                                            if (!"3 Stars".equals(previousLevelStars)) {
                                                Toast.makeText(passingStageSelection.this,
                                                        "You need 3 stars in level " + currentIndex + " to unlock this level!",
                                                        Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        }
                                        Toast.makeText(passingStageSelection.this,
                                                "Complete previous " + previousLevel + " to unlock.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                                loadingSnackbar.dismiss();
                            }
                        })
                        .addOnFailureListener(historyError -> {
                            Toast.makeText(this, "Error loading history: " + historyError.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                }

            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }

        // Apply animations to all levels and flash boxes.
        for (FrameLayout level : levels) {
            animateButtonFocus(level);
        }

        for (ImageView flash : flashboxes) {
            animateButtonFocus(flash);
        }

        backgroundFrame = findViewById(R.id.main);
        numberContainer = findViewById(R.id.number_container); // Get FrameLayout from XML

        numBGAnimation = new NumBGAnimation(this, numberContainer);
        numBGAnimation.startNumberAnimationLoop();

        backgroundFrame.post(() -> {
            VignetteEffect.apply(this, backgroundFrame);
        });
    }

    private boolean isOnStarStreak(Map<String, String> starsPerLevel, int fromLevel, int toLevel) {
        for (int i = fromLevel; i <= toLevel; i++) {
            String levelKey = "level_" + i;
            String stars = starsPerLevel.get(levelKey);
            if (!"3 Stars".equals(stars)) {
                return false;
            }
        }
        return true;
    }

    private void showStarStreakDialog(String message) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_star_streak);
        dialog.setCancelable(true);

        TextView messageText = dialog.findViewById(R.id.starStreakMessage);
        AppCompatButton okBtn = dialog.findViewById(R.id.okBtn);

        messageText.setText(message);
        okBtn.setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.show();
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
        scaleX.setDuration(1000);
        scaleY.setDuration(1000);

        // AccelerateDecelerateInterpolator for smooth pulsing
        AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
        scaleX.setInterpolator(interpolator);
        scaleY.setInterpolator(interpolator);

        // Set repeat count and mode on each ObjectAnimator
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);  // Infinite repeat
        scaleX.setRepeatMode(ObjectAnimator.REVERSE);    // Reverse animation on repeat
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);  // Infinite repeat
        scaleY.setRepeatMode(ObjectAnimator.REVERSE);    // Reverse animation on repeat

        // Combine the animations into an AnimatorSet
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();
    }

    // Stop Focus Animation
    private void stopButtonFocusAnimation(View button) {
        AnimatorSet animatorSet = (AnimatorSet) button.getTag();
        if (animatorSet != null) {
            animatorSet.cancel();  // Stop the animation when focus is lost
        }
    }

    private void startFlashingAnimation(View view) {
        // Create an ObjectAnimator to animate the alpha property
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        animator.setDuration(500); // Duration for the fade in and out
        animator.setRepeatMode(ObjectAnimator.REVERSE); // Reverse the animation
        animator.setRepeatCount(ValueAnimator.INFINITE); // Repeat indefinitely
        animator.start();
    }

    @Override
    protected void onStart() {
        super.onStart();

        MusicManager.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MusicManager.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicManager.resume();

        // Reload progress when returning to this activity (e.g., after completing a level)
        // This ensures star ratings are updated immediately
        loadProgressData();
    }

    private void loadProgressData() {
        // Reload the star ratings and level progress
        String operation = getIntent().getStringExtra("operation");
        String difficulty = getIntent().getStringExtra("difficulty");

        if (operation == null || difficulty == null) {
            return; // Can't reload without these parameters
        }

        // Get user data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        String firstName = sharedPreferences.getString("firstName", "");
        String lastName = sharedPreferences.getString("lastName", "");
        String section = sharedPreferences.getString("section", "");
        String grade = sharedPreferences.getString("grade", "");

        if (firstName.isEmpty() || lastName.isEmpty()) {
            return; // Can't reload without user data
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("Accounts")
            .document("Students")
            .collection("MathSquare");

        Query query = collectionRef
            .whereEqualTo("firstName", firstName)
            .whereEqualTo("lastName", lastName)
            .whereEqualTo("section", section)
            .whereEqualTo("gameType", "Passing")
            .whereEqualTo("grade", grade)
            .whereEqualTo("operation_type", operation);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    List<String> starsList = (List<String>) document.get("stars_list");
                    if (starsList != null) {
                        // Update star displays
                        updateStarDisplays(starsList);
                    }
                }
            }
        }).addOnFailureListener(e -> {
            android.util.Log.e("StarReload", "Failed to reload star data: " + e.getMessage());
        });
    }

    private void updateStarDisplays(List<String> starsList) {
        Map<String, String> starsPerLevel = new HashMap<>();

        for (String entry : starsList) {
            int lastUnderscore = entry.lastIndexOf('_');
            if (lastUnderscore > 0) {
                String levelKey = entry.substring(0, lastUnderscore);
                String starRating = entry.substring(lastUnderscore + 1);
                starsPerLevel.put(levelKey, starRating);
                android.util.Log.d("StarReload", "Updated star display: " + levelKey + " = " + starRating);
            }
        }

        // Update the star images for each level
        ImageView[] starViews = {levelOneStar, levelTwoStar, levelThreeStar};

        for (int i = 0; i < starViews.length; i++) {
            String levelKey = "level_" + (i + 1);
            String starsEarned = starsPerLevel.get(levelKey);

            if ("1 Stars".equals(starsEarned)) {
                starViews[i].setImageResource(R.drawable.ic_star_one);
            } else if ("2 Stars".equals(starsEarned)) {
                starViews[i].setImageResource(R.drawable.ic_star_two);
            } else if ("3 Stars".equals(starsEarned)) {
                starViews[i].setImageResource(R.drawable.ic_star_three);
            } else {
                // No stars or unknown rating - use the none/empty star image
                starViews[i].setImageResource(R.drawable.ic_star_none);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicManager.pause();
    }
}
