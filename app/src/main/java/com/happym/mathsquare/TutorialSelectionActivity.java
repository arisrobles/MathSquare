package com.happym.mathsquare;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.WindowCompat;

import com.happym.mathsquare.sharedPreferences;
import com.happym.mathsquare.utils.GradeRestrictionUtil;
import com.happym.mathsquare.utils.TutorialProgressTracker;
import com.happym.mathsquare.Animation.*;

import java.io.IOException;
import java.util.Random;

public class TutorialSelectionActivity extends AppCompatActivity {
    private MediaPlayer soundEffectPlayer;
    private FrameLayout numberContainer, backgroundFrame;
    private final Random random = new Random();
    private NumBGAnimation numBGAnimation;
    
    private String tutorialName;
    private String grade;
    private int gradeNum;
    
    // YouTube video URLs by grade and operation
    private static final String GRADE_1_ADDITION_VIDEO = "https://youtu.be/s79OUi4Nog0?feature=shared";
    private static final String GRADE_1_SUBTRACTION_VIDEO = "https://youtube.com/shorts/c7lBcItBqFE?feature=shared";
    private static final String GRADE_2_SUBTRACTION_VIDEO = "https://youtube.com/shorts/xnQ11Bpv-68?feature=shared";
    private static final String GRADE_2_ADDITION_VIDEO = "https://youtube.com/shorts/eL6VzyCPivY?feature=shared";
    
    private static final String GRADE_3_4_ADDITION_VIDEO = "https://youtu.be/1Al2Fc3wOIQ?feature=shared";
    private static final String GRADE_3_4_SUBTRACTION_VIDEO = "https://youtube.com/shorts/whGG2OwvJ_g?feature=shared";
    private static final String GRADE_3_4_MULTIPLICATION_VIDEO = "https://youtu.be/-aNSUlo5sSA?feature=shared";
    private static final String GRADE_3_4_DIVISION_VIDEO = "https://youtu.be/irfMCIgFJZY?feature=shared";
    
    private static final String GRADE_5_6_ADDITION_VIDEO = "https://youtu.be/1Al2Fc3wOIQ?feature=shared";
    private static final String GRADE_5_6_SUBTRACTION_VIDEO = "https://youtube.com/shorts/whGG2OwvJ_g?feature=shared";
    private static final String GRADE_5_6_MULTIPLICATION_VIDEO = "https://youtu.be/-aNSUlo5sSA?feature=shared";
    private static final String GRADE_5_6_DIVISION_VIDEO = "https://youtu.be/irfMCIgFJZY?feature=shared";
    private static final String GRADE_5_6_DECIMAL_VIDEO = "https://youtu.be/PF6r1N6rglY?si=XhTKbAnED0xeQNqK";
    private static final String GRADE_5_6_PERCENTAGE_VIDEO = "https://youtu.be/kDFLcCOS7aw?feature=shared";
    
    // Video credits
    private static final String VIDEO_CREDITS = "Video credits to respective owners";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.layout_tutorial_selection);
        
        tutorialName = getIntent().getStringExtra("TUTORIAL_NAME");
        grade = sharedPreferences.getGrade(this);
        gradeNum = getGradeNumber(grade);
        
        if (tutorialName == null) {
            Toast.makeText(this, "Tutorial not specified", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        setupUI();
        
        backgroundFrame = findViewById(R.id.main);
        numberContainer = findViewById(R.id.number_container);
        
        numBGAnimation = new NumBGAnimation(this, numberContainer);
        numBGAnimation.startNumberAnimationLoop();
        
        backgroundFrame.post(() -> {
            VignetteEffect.apply(this, backgroundFrame);
        });
    }
    
    private void setupUI() {
        TextView titleText = findViewById(R.id.tutorial_title);
        String displayName = capitalizeFirst(tutorialName);
        titleText.setText(displayName + " Tutorial");
        
        LinearLayout buttonContainer = findViewById(R.id.button_container);
        buttonContainer.removeAllViews();
        
        // Always show two buttons: Videos and Slides
        // Videos button - opens VideoPlayerActivity with all videos
        AppCompatButton videosBtn = createButton("Videos", () -> openVideos());
        buttonContainer.addView(videosBtn);
            
        // Slides button - opens PDF tutorial (for grade 3-6 and guests)
        // Guests (grade == null) get gradeNum = 5, so they can access slides
        if (gradeNum > 2 || grade == null) {
            AppCompatButton slidesBtn = createButton("Slides", () -> openPDFTutorial());
            buttonContainer.addView(slidesBtn);
        }
        
        // Add credits text
        TextView creditsText = findViewById(R.id.video_credits);
        if (creditsText != null) {
            creditsText.setText(VIDEO_CREDITS);
            creditsText.setVisibility(View.VISIBLE);
        }
    }
    
    private void openVideos() {
        // Open VideoPlayerActivity which displays all videos in-app using iframe
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra("TUTORIAL_NAME", tutorialName);
        startActivity(intent);
    }
    
    private AppCompatButton createButton(String text, Runnable onClick) {
        AppCompatButton button = new AppCompatButton(this);
        button.setText(text);
        button.setTextSize(20);
        button.setTextColor(getResources().getColor(android.R.color.white));
        button.setBackgroundResource(R.drawable.btn_long_condition_green);
        button.setPadding(0, 20, 0, 20);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(25, 0, 25, 20);
        button.setLayoutParams(params);
        button.setOnClickListener(v -> {
            playSound("click.mp3");
            onClick.run();
        });
        animateButtonFocus(button);
        return button;
    }
    
    private String getYouTubeVideoUrl() {
        String tutLower = tutorialName.toLowerCase();
        
        // Grade 1-2 videos
        if (gradeNum == 1) {
            if (tutLower.contains("addition")) return GRADE_1_ADDITION_VIDEO;
            if (tutLower.contains("subtraction")) return GRADE_1_SUBTRACTION_VIDEO;
        } else if (gradeNum == 2) {
            if (tutLower.contains("addition")) return GRADE_2_ADDITION_VIDEO;
            if (tutLower.contains("subtraction")) return GRADE_2_SUBTRACTION_VIDEO;
        }
        // Grade 3-4 videos
        else if (gradeNum == 3 || gradeNum == 4) {
            if (tutLower.contains("addition")) return GRADE_3_4_ADDITION_VIDEO;
            if (tutLower.contains("subtraction")) return GRADE_3_4_SUBTRACTION_VIDEO;
            if (tutLower.contains("multiplication")) return GRADE_3_4_MULTIPLICATION_VIDEO;
            if (tutLower.contains("division")) return GRADE_3_4_DIVISION_VIDEO;
        }
        // Grade 5-6 videos
        else if (gradeNum == 5 || gradeNum == 6) {
            if (tutLower.contains("addition")) return GRADE_5_6_ADDITION_VIDEO;
            if (tutLower.contains("subtraction")) return GRADE_5_6_SUBTRACTION_VIDEO;
            if (tutLower.contains("multiplication")) return GRADE_5_6_MULTIPLICATION_VIDEO;
            if (tutLower.contains("division")) return GRADE_5_6_DIVISION_VIDEO;
            if (tutLower.contains("decimal")) return GRADE_5_6_DECIMAL_VIDEO;
            if (tutLower.contains("percentage")) return GRADE_5_6_PERCENTAGE_VIDEO;
        }
        
        return null;
    }
    
    private void openPDFTutorial() {
        TutorialProgressTracker tracker = new TutorialProgressTracker(this);
        
        // Check grade level access (guests are allowed all tutorials)
        if (!GradeRestrictionUtil.isTutorialAllowedForGrade(grade, tutorialName)) {
            Toast.makeText(this, 
                "This tutorial is not available for your grade level", 
                Toast.LENGTH_LONG).show();
            return;
        }
        
        // For guests, skip progression check and allow direct access
        // For logged-in students, check tutorial progression
        if (grade != null && !tracker.canAccessTutorial(tutorialName)) {
            String previousTut = tracker.getPreviousTutorial(tutorialName);
            if (!previousTut.isEmpty()) {
                Toast.makeText(this, 
                    "Please complete the " + previousTut + " tutorial first!", 
                    Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, 
                    "Please complete the previous tutorial in your grade's sequence first!", 
                    Toast.LENGTH_LONG).show();
            }
            return;
        }
        
        String pdfFileName = tutorialName.toLowerCase() + ".pdf";
        Intent intent = new Intent(this, PDFViewerActivity.class);
        intent.putExtra("PDF_FILE", pdfFileName);
        intent.putExtra("TUTORIAL_NAME", tutorialName);
        intent.putExtra("SHOW_VIDEO_AFTER", false); // Don't show video after slides - it's available in Videos section
        startActivity(intent);
        
        // Note: Tutorial completion is now handled by PDFViewerActivity when student finishes viewing
    }
    
    private int getGradeNumber(String grade) {
        try {
            // Handle null grade (guests) - default to grade 5 to allow all tutorials
            if (grade == null) {
                return 5; // Grade 5 allows all tutorials
            }
            
            if (grade.startsWith("grade_")) {
                String numStr = grade.replace("grade_", "")
                    .replace("one", "1").replace("two", "2")
                    .replace("three", "3").replace("four", "4")
                    .replace("five", "5").replace("six", "6");
                return Integer.parseInt(numStr);
            } else {
                return Integer.parseInt(grade);
            }
        } catch (NumberFormatException e) {
            return 5; // Default to grade 5 for guests to allow all tutorials
        }
    }
    
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
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
            soundEffectPlayer.setOnCompletionListener(mp -> {
                mp.release();
                soundEffectPlayer = null;
            });
            soundEffectPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void animateButtonFocus(View button) {
        android.animation.ObjectAnimator scaleX = android.animation.ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.06f, 1f);
        android.animation.ObjectAnimator scaleY = android.animation.ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.06f, 1f);
        
        scaleX.setDuration(2000);
        scaleY.setDuration(2000);
        
        android.view.animation.AccelerateDecelerateInterpolator interpolator = 
            new android.view.animation.AccelerateDecelerateInterpolator();
        scaleX.setInterpolator(interpolator);
        scaleY.setInterpolator(interpolator);
        
        scaleX.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        scaleX.setRepeatMode(android.animation.ObjectAnimator.REVERSE);
        scaleY.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        scaleY.setRepeatMode(android.animation.ObjectAnimator.REVERSE);
        
        android.animation.AnimatorSet animatorSet = new android.animation.AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundEffectPlayer != null) {
            soundEffectPlayer.release();
        }
    }
}

