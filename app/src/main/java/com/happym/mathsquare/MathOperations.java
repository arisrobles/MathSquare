package com.happym.mathsquare;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;

import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
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
import android.util.Log;
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
import com.happym.mathsquare.utils.GradeRestrictionUtil;

public class MathOperations extends AppCompatActivity {
    private MediaPlayer soundEffectPlayer;

    private FrameLayout numberContainer,backgroundFrame;
    private final Random random = new Random();
    private NumBGAnimation numBGAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mathoperations);

        // Get difficulty from intent
        String difficulty = getIntent().getStringExtra("difficulty");

        // operations buttons
        int btnOperationAdd = R.id.btn_operation_add;
        int btnOperationSubtract = R.id.btn_operation_subtract;
        int btnOperationMultiply = R.id.btn_operation_multiply;
        int btnOperationDivide = R.id.btn_operation_divide;
        int btnOperationDecimal = R.id.btn_operation_decimal;
        int btnOperationPercentage = R.id.btn_operation_percentage;

        AppCompatImageView btnAdd = findViewById(btnOperationAdd);
        AppCompatImageView btnSubtract = findViewById(btnOperationSubtract);
        AppCompatImageView btnMultiply = findViewById(btnOperationMultiply);
        AppCompatImageView btnDivide = findViewById(btnOperationDivide);
        AppCompatImageView btnDecimal = findViewById(btnOperationDecimal);
        AppCompatImageView btnPercentage = findViewById(btnOperationPercentage);

        // Get label references
        TextView labelAdd = findViewById(R.id.label_operation_add);
        TextView labelSubtract = findViewById(R.id.label_operation_subtract);
        TextView labelMultiply = findViewById(R.id.label_operation_multiply);
        TextView labelDivide = findViewById(R.id.label_operation_divide);
        TextView labelDecimal = findViewById(R.id.label_operation_decimal);
        TextView labelPercentage = findViewById(R.id.label_operation_percentage);

        // Show/hide operations based on grade level
        Log.d("MATH_OPERATIONS", "Setting visibility for difficulty: " + difficulty);

        if ("grade_one".equals(difficulty) || "grade_two".equals(difficulty)) {
            // For grades 1-2: only show addition and subtraction
            Log.d("MATH_OPERATIONS", "Grade 1-2: Hiding multiplication, division, decimal, percentage");
            btnMultiply.setVisibility(View.GONE);
            btnDivide.setVisibility(View.GONE);
            btnDecimal.setVisibility(View.GONE);
            btnPercentage.setVisibility(View.GONE);
            // Hide corresponding labels
            labelMultiply.setVisibility(View.GONE);
            labelDivide.setVisibility(View.GONE);
            labelDecimal.setVisibility(View.GONE);
            labelPercentage.setVisibility(View.GONE);
        } else if ("grade_three".equals(difficulty) || "grade_four".equals(difficulty)) {
            // For grades 3-4: show all basic operations but hide decimal and percentage
            Log.d("MATH_OPERATIONS", "Grade 3-4: Showing basic operations, hiding decimal and percentage");
            btnMultiply.setVisibility(View.VISIBLE);
            btnDivide.setVisibility(View.VISIBLE);
            btnDecimal.setVisibility(View.GONE);
            btnPercentage.setVisibility(View.GONE);
            // Show/hide corresponding labels
            labelMultiply.setVisibility(View.VISIBLE);
            labelDivide.setVisibility(View.VISIBLE);
            labelDecimal.setVisibility(View.GONE);
            labelPercentage.setVisibility(View.GONE);
            animateButtonFocus(btnMultiply);
            animateButtonFocus(btnDivide);
        } else {
            // For grades 5-6: show all operations
            Log.d("MATH_OPERATIONS", "Grade 5-6: Showing all operations");
            btnMultiply.setVisibility(View.VISIBLE);
            btnDivide.setVisibility(View.VISIBLE);
            btnDecimal.setVisibility(View.VISIBLE);
            btnPercentage.setVisibility(View.VISIBLE);
            // Show all labels
            labelMultiply.setVisibility(View.VISIBLE);
            labelDivide.setVisibility(View.VISIBLE);
            labelDecimal.setVisibility(View.VISIBLE);
            labelPercentage.setVisibility(View.VISIBLE);
            animateButtonFocus(btnMultiply);
            animateButtonFocus(btnDivide);
            animateButtonFocus(btnDecimal);
            animateButtonFocus(btnPercentage);
        }

        animateButtonFocus(btnAdd);
        animateButtonFocus(btnSubtract);

        // Create a common OnClickListener with grade restrictions
        View.OnClickListener operationClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MathOperations.this, MultipleChooser.class);
                int viewId = view.getId();
                String operationName = null;
                playSound("click.mp3");

                if (viewId == btnOperationAdd) {
                    operationName = "Addition";
                    animateButtonClick(btnAdd);
                    stopButtonFocusAnimation(btnAdd);
                    animateButtonFocus(btnAdd);
                } else if (viewId == btnOperationSubtract) {
                    operationName = "Subtraction";
                    animateButtonClick(btnSubtract);
                    stopButtonFocusAnimation(btnSubtract);
                    animateButtonFocus(btnSubtract);
                } else if (viewId == btnOperationMultiply) {
                    operationName = "Multiplication";
                    // Check grade restriction (defense comment #1)
                    if (!GradeRestrictionUtil.allowAccess(MathOperations.this, operationName)) {
                        Toast.makeText(MathOperations.this, 
                            "This operation is not available for your grade level", 
                            Toast.LENGTH_LONG).show();
                        return;
                    }
                    animateButtonClick(btnMultiply);
                    stopButtonFocusAnimation(btnMultiply);
                    animateButtonFocus(btnMultiply);
                } else if (viewId == btnOperationDivide) {
                    operationName = "Division";
                    // Check grade restriction
                    if (!GradeRestrictionUtil.allowAccess(MathOperations.this, operationName)) {
                        Toast.makeText(MathOperations.this, 
                            "This operation is not available for your grade level", 
                            Toast.LENGTH_LONG).show();
                        return;
                    }
                    animateButtonClick(btnDivide);
                    stopButtonFocusAnimation(btnDivide);
                    animateButtonFocus(btnDivide);
                } else if (viewId == btnOperationDecimal) {
                    operationName = "Decimal";
                    // Check grade restriction
                    if (!GradeRestrictionUtil.allowAccess(MathOperations.this, operationName)) {
                        Toast.makeText(MathOperations.this, 
                            "This operation is not available for your grade level", 
                            Toast.LENGTH_LONG).show();
                        return;
                    }
                    animateButtonClick(btnDecimal);
                    stopButtonFocusAnimation(btnDecimal);
                    animateButtonFocus(btnDecimal);
                    // Redirect to DecimalOperations activity instead of MultipleChooser
                    Intent decimalIntent = new Intent(MathOperations.this, DecimalOperations.class);
                    decimalIntent.putExtra("difficulty", difficulty);
                    startActivity(decimalIntent);
                    return; // Exit early to avoid going to MultipleChooser
                } else if (viewId == btnOperationPercentage) {
                    operationName = "Percentage";
                    // Check grade restriction
                    if (!GradeRestrictionUtil.allowAccess(MathOperations.this, operationName)) {
                        Toast.makeText(MathOperations.this, 
                            "This operation is not available for your grade level", 
                            Toast.LENGTH_LONG).show();
                        return;
                    }
                    animateButtonClick(btnPercentage);
                    stopButtonFocusAnimation(btnPercentage);
                    animateButtonFocus(btnPercentage);
                }

                intent.putExtra("operation", operationName);
                intent.putExtra("difficulty", difficulty);
                startActivity(intent);
            }
        };

        // Set click listeners
        btnAdd.setOnClickListener(operationClickListener);
        btnSubtract.setOnClickListener(operationClickListener);
        btnMultiply.setOnClickListener(operationClickListener);
        btnDivide.setOnClickListener(operationClickListener);
        btnDecimal.setOnClickListener(operationClickListener);
        btnPercentage.setOnClickListener(operationClickListener);

        backgroundFrame = findViewById(R.id.main);
        numberContainer = findViewById(R.id.number_container); // Get FrameLayout from XML

        numBGAnimation = new NumBGAnimation(this, numberContainer);
        numBGAnimation.startNumberAnimationLoop();

        backgroundFrame.post(() -> {
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

    }

@Override
    protected void onPause() {
        super.onPause();
        MusicManager.pause();
    }

}
