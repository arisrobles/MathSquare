package com.happym.mathsquare;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;

// import androidx.activity.EdgeToEdge;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;
import com.google.firebase.FirebaseApp;

import java.io.IOException;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;

// import androidx.activity.EdgeToEdge;
import android.widget.FrameLayout;

import java.util.Random;

import com.happym.mathsquare.Animation.*;
import com.happym.mathsquare.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer bgMediaPlayer;
    private MediaPlayer soundEffectPlayer;
    private FloatingTextView floatingTextView;
    private FrameLayout numberContainer, backgroundFrame;
    private final Random random = new Random();
    private NumBGAnimation numBGAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        //  EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        MusicManager.playBGGame(this, "newbgmusic.mp3"); // Updated to new background music

        TextView txtTitle = findViewById(R.id.text_title);
        Button btnPlay = findViewById(R.id.btn_playgame);
        Button btnTutorial = findViewById(R.id.btn_tutorials);
        Button btnEExit = findViewById(R.id.btn_exitgame);
        LinearLayout btnPlayQuiz = findViewById(R.id.btn_play_quiz);
        LinearLayout btnLogOut = findViewById(R.id.btn_logout);
        LinearLayout btnScoreHistory = findViewById(R.id.btn_score_history);
        LinearLayout btnProfile = findViewById(R.id.btn_profile);

        animateButtonFocus(btnLogOut);
        animateButtonFocus(btnPlay);
        animateButtonFocus(btnTutorial);
        animateButtonFocus(btnEExit);
        animateButtonFocus(btnPlayQuiz);
        animateButtonFocus(btnLogOut);
        if (btnScoreHistory != null) {
            animateButtonFocus(btnScoreHistory);
        }
        if (btnProfile != null) {
            animateButtonFocus(btnProfile);
        }
        startRotationAnimation(txtTitle);

        if (sharedPreferences.StudentIsLoggedIn(this)) {

            String section = sharedPreferences.getSection(this);
            String grade = sharedPreferences.getGrade(this);
            String firstName = sharedPreferences.getFirstN(this);
            String lastName = sharedPreferences.getLastN(this);

            Toast.makeText(this, "Hi " + firstName + " " + lastName + "! Ready to play?", Toast.LENGTH_SHORT).show();

            // Show score history and profile buttons for logged-in students
            if (btnScoreHistory != null) {
                btnScoreHistory.setVisibility(View.VISIBLE);
            }
            if (btnProfile != null) {
                btnProfile.setVisibility(View.VISIBLE);
            }

        } else {

            btnPlayQuiz.setVisibility(View.GONE);
            if (btnScoreHistory != null) {
                btnScoreHistory.setVisibility(View.GONE);
            }
            if (btnProfile != null) {
                btnProfile.setVisibility(View.GONE);
            }
        }
        
        // Score History button click listener
        if (btnScoreHistory != null) {
            btnScoreHistory.setOnClickListener(view -> {
                animateButtonClick(btnScoreHistory);
                Intent intent = new Intent(MainActivity.this, ScoreHistoryActivity.class);
                playSound("click.mp3");
                startActivity(intent);
                stopButtonFocusAnimation(btnScoreHistory);
                animateButtonFocus(btnScoreHistory);
            });
        }
        
        // Profile button click listener
        if (btnProfile != null) {
            btnProfile.setOnClickListener(view -> {
                animateButtonClick(btnProfile);
                Intent intent = new Intent(MainActivity.this, StudentProfileActivity.class);
                playSound("click.mp3");
                startActivity(intent);
                stopButtonFocusAnimation(btnProfile);
                animateButtonFocus(btnProfile);
            });
        }

        btnPlay.setOnClickListener(
                view -> {
                    animateButtonClick(btnPlay);
                    Intent intent = new Intent(MainActivity.this, Difficulty.class);
                    playSound("click.mp3");
                    startActivity(intent);
                    stopButtonFocusAnimation(btnPlay);
                    animateButtonFocus(btnPlay);
                });

        btnLogOut.setOnClickListener(
                view -> {
                    animateButtonClick(btnLogOut);
                    playSound("click.mp3");
                    showLogoutConfirmationDialog();
                    stopButtonFocusAnimation(btnLogOut);
                    animateButtonFocus(btnLogOut);
                });

        btnPlayQuiz.setOnClickListener(
                view -> {
                    animateButtonClick(btnPlayQuiz);
                    Intent intent = new Intent(MainActivity.this, QuizzesSection.class);
                    playSound("click.mp3");
                    startActivity(intent);
                    stopButtonFocusAnimation(btnPlayQuiz);
                    animateButtonFocus(btnPlayQuiz);
                });

        btnTutorial.setOnClickListener(
                view -> {
                    animateButtonClick(btnTutorial);
                    playSound("click.mp3");
                    stopButtonFocusAnimation(btnTutorial);
                    animateButtonFocus(btnTutorial);

                    String youtubeUrl = "https://www.youtube.com/";

                    Intent intent = new Intent(this, WebViewActivity.class);
                    intent.putExtra("URL", youtubeUrl);
                    startActivity(intent);
                });

        btnEExit.setOnClickListener(
                view -> {
                    animateButtonClick(btnEExit);
                    stopButtonFocusAnimation(btnEExit);
                    playSound("click.mp3");
                    finishAffinity();
                    System.exit(0);
                });

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(
                            systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });

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

    // Game Button Animation Press

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

    // Text Title Animation

    private void startRotationAnimation(TextView txtTitle) {
        // Create an ObjectAnimator for rotation to 30 degrees
        ObjectAnimator rotateTo30 = ObjectAnimator.ofFloat(txtTitle, "rotation", 0f, 30f);
        rotateTo30.setDuration(500); // 500 milliseconds for smooth rotation
        rotateTo30.setInterpolator(new LinearInterpolator());

        // Create an ObjectAnimator for rotation to -30 degrees
        ObjectAnimator rotateToMinus30 = ObjectAnimator.ofFloat(txtTitle, "rotation", 30f, -30f);
        rotateToMinus30.setDuration(1000); // 1000 milliseconds to add some time to rotate back
        rotateToMinus30.setInterpolator(new LinearInterpolator());

        // Create an ObjectAnimator for rotation back to 0 degrees
        ObjectAnimator rotateTo0 = ObjectAnimator.ofFloat(txtTitle, "rotation", -30f, 0f);
        rotateTo0.setDuration(500); // 500 milliseconds for smooth rotation back to 0
        rotateTo0.setInterpolator(new LinearInterpolator());

        // Create an AnimatorSet to play animations in sequence
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(rotateTo30, rotateToMinus30, rotateTo0);
        animatorSet.setStartDelay(500); // Add delay between animations if needed
        animatorSet.setInterpolator(new LinearInterpolator());

        // Add a listener to restart the animation once it ends
        animatorSet.addListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animatorSet.start(); // Restart the animation when it ends
                    }
                });

        // Start the animation
        animatorSet.start();
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
    }

    @Override
    protected void onPause() {
        super.onPause();

        MusicManager.pause();
    }
    
    /**
     * Show logout confirmation dialog
     */
    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.RoundedAlertDialog);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            SessionManager.logoutStudent(this);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }
}
