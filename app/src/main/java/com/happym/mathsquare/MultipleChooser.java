package com.happym.mathsquare;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.happym.mathsquare.GameType.Practice.PracticeLevels;
import com.happym.mathsquare.GameType.Passing.passingStageSelection;
import com.happym.mathsquare.GameType.OnTimer.OnTimerLevelSelection;
import com.happym.mathsquare.GameType.OnTimer.OnTimerSettings;
import java.io.IOException;

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

public class MultipleChooser extends AppCompatActivity {
    private MediaPlayer soundEffectPlayer;
    private String difficultySection;
    private FrameLayout numberContainer,backgroundFrame;
    private final Random random = new Random();
    private NumBGAnimation numBGAnimation;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_multiple_chooser);
        
        String operation = getIntent().getStringExtra("operation");
String gradeLevel = getIntent().getStringExtra("difficulty");

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
        
        if ("grade_one".equals(gradeLevel)) {
   difficultySection = "Easy";
} else if ("grade_two".equals(gradeLevel)) {
    difficultySection = "Easy";
} else if ("grade_three".equals(gradeLevel)) {
    difficultySection = "Medium";
} else if ("grade_four".equals(gradeLevel)) {
    difficultySection = "Medium";
} else if ("grade_five".equals(gradeLevel)) {
    difficultySection = "Medium";
} else if ("grade_six".equals(gradeLevel)) {
    difficultySection = "Hard";
} else {
    difficultySection = "Easy";
}

        TextView operationDisplay = findViewById(R.id.selectedDifficulty);
        LinearLayout practicebtn = findViewById(R.id.btn_practice);
        LinearLayout passingBtn = findViewById(R.id.passing_btn);
        LinearLayout ontimerBtn = findViewById(R.id.ontimer_btn);
        
        animateButtonFocus(practicebtn);
        animateButtonFocus(passingBtn);
        animateButtonFocus(ontimerBtn);
        
        practicebtn.setOnClickListener(v -> {
            Intent intent = new Intent(MultipleChooser.this, PracticeLevels.class);
                intent.putExtra("operation", operation);
    intent.putExtra("difficulty", gradeLevel);
                playSound("click.mp3");
                animateButtonClick(practicebtn);
        stopButtonFocusAnimation(practicebtn);
            startActivity(intent);
            });
        
        passingBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MultipleChooser.this, passingStageSelection.class);
                intent.putExtra("operation", operation);
    intent.putExtra("difficulty", gradeLevel);
                playSound("click.mp3");
               animateButtonClick(passingBtn);
        stopButtonFocusAnimation(passingBtn);
            startActivity(intent);
            });
        
        ontimerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MultipleChooser.this, OnTimerSettings.class);
    intent.putExtra("operation", operation);
            // For percentage and decimal operations, pass the actual grade level
            if ("Percentage".equals(operation) || "Decimal".equals(operation) || 
                "DecimalAddition".equals(operation) || "DecimalSubtraction".equals(operation) || 
                "DecimalMultiplication".equals(operation) || "DecimalDivision".equals(operation)) {
                intent.putExtra("difficulty", gradeLevel);
            } else {
                intent.putExtra("difficulty", difficultySection);
            }
                playSound("click.mp3");
                animateButtonClick(ontimerBtn);
        stopButtonFocusAnimation(ontimerBtn);
    startActivity(intent);
            });
        
        operationDisplay.setText(operation);
        
        backgroundFrame = findViewById(R.id.main);
        numberContainer = findViewById(R.id.number_container);

        numBGAnimation = new NumBGAnimation(this, numberContainer);
        numBGAnimation.startNumberAnimationLoop();
        
        backgroundFrame.post(() -> {
        VignetteEffect.apply(this, backgroundFrame);
        });
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
     @Override
    protected void onStart() {
        super.onStart();

            MusicManager.resume();
        
    }
}
