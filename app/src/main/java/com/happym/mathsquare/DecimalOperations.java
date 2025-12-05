package com.happym.mathsquare;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.view.WindowCompat;

import com.happym.mathsquare.Animation.NumBGAnimation;
import com.happym.mathsquare.Animation.VignetteEffect;
import com.happym.mathsquare.R;

import java.io.IOException;

public class DecimalOperations extends AppCompatActivity {
    private MediaPlayer soundEffectPlayer;
    private FrameLayout numberContainer, backgroundFrame;
    private NumBGAnimation numBGAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_decimal_operations);

        String difficulty = getIntent().getStringExtra("difficulty");

        // operations buttons
        int btnOperationAdd = R.id.btn_operation_add;
        int btnOperationSubtract = R.id.btn_operation_subtract;
        int btnOperationMultiply = R.id.btn_operation_multiply;
        int btnOperationDivide = R.id.btn_operation_divide;

        AppCompatImageView btnAdd = findViewById(btnOperationAdd);
        AppCompatImageView btnSubtract = findViewById(btnOperationSubtract);
        AppCompatImageView btnMultiply = findViewById(btnOperationMultiply);
        AppCompatImageView btnDivide = findViewById(btnOperationDivide);

        // Create a common OnClickListener
        View.OnClickListener operationClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DecimalOperations.this, MultipleChooser.class);
                int viewId = view.getId();
                String operationName = null;
                playSound("click.mp3");

                if (viewId == btnOperationAdd) {
                    operationName = "DecimalAddition";
                    animateButtonClick(btnAdd);
                    stopButtonFocusAnimation(btnAdd);
                    animateButtonFocus(btnAdd);
                } else if (viewId == btnOperationSubtract) {
                    operationName = "DecimalSubtraction";
                    animateButtonClick(btnSubtract);
                    stopButtonFocusAnimation(btnSubtract);
                    animateButtonFocus(btnSubtract);
                } else if (viewId == btnOperationMultiply) {
                    operationName = "DecimalMultiplication";
                    animateButtonClick(btnMultiply);
                    stopButtonFocusAnimation(btnMultiply);
                    animateButtonFocus(btnMultiply);
                } else if (viewId == btnOperationDivide) {
                    operationName = "DecimalDivision";
                    animateButtonClick(btnDivide);
                    stopButtonFocusAnimation(btnDivide);
                    animateButtonFocus(btnDivide);
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

        // Start button focus animations
        animateButtonFocus(btnAdd);
        animateButtonFocus(btnSubtract);
        animateButtonFocus(btnMultiply);
        animateButtonFocus(btnDivide);

        backgroundFrame = findViewById(R.id.main);
        numberContainer = findViewById(R.id.number_container);

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
        android.animation.ObjectAnimator scaleX = android.animation.ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.6f, 1.1f, 1f);
        android.animation.ObjectAnimator scaleY = android.animation.ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.6f, 1.1f, 1f);

        // Set duration for the animations
        scaleX.setDuration(1000);
        scaleY.setDuration(1000);

        // OvershootInterpolator for game-like snappy effect
        android.view.animation.OvershootInterpolator overshootInterpolator = new android.view.animation.OvershootInterpolator(2f);
        scaleX.setInterpolator(overshootInterpolator);
        scaleY.setInterpolator(overshootInterpolator);

        // Combine animations into a set
        android.animation.AnimatorSet animatorSet = new android.animation.AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();
    }

    // Function to animate button focus with a smooth pulsing bounce effect
    private void animateButtonFocus(View button) {
        android.animation.ObjectAnimator scaleX = android.animation.ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.06f, 1f);
        android.animation.ObjectAnimator scaleY = android.animation.ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.06f, 1f);

        // Set duration for a slower, smoother pulsing bounce effect
        scaleX.setDuration(1000);
        scaleY.setDuration(1000);

        // AccelerateDecelerateInterpolator for smooth pulsing
        android.view.animation.AccelerateDecelerateInterpolator interpolator = new android.view.animation.AccelerateDecelerateInterpolator();
        scaleX.setInterpolator(interpolator);
        scaleY.setInterpolator(interpolator);

        // Set repeat count and mode on each ObjectAnimator
        scaleX.setRepeatCount(android.animation.ObjectAnimator.INFINITE);  // Infinite repeat
        scaleX.setRepeatMode(android.animation.ObjectAnimator.REVERSE);    // Reverse animation on repeat
        scaleY.setRepeatCount(android.animation.ObjectAnimator.INFINITE);  // Infinite repeat
        scaleY.setRepeatMode(android.animation.ObjectAnimator.REVERSE);    // Reverse animation on repeat

        // Combine the animations into an AnimatorSet
        android.animation.AnimatorSet animatorSet = new android.animation.AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();

        // Store the animator set as a tag on the button for later reference
        button.setTag(animatorSet);
    }

    // Stop Focus Animation
    private void stopButtonFocusAnimation(View button) {
        android.animation.AnimatorSet animatorSet = (android.animation.AnimatorSet) button.getTag();
        if (animatorSet != null) {
            animatorSet.cancel();  // Stop the animation when focus is lost
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundEffectPlayer != null) {
            soundEffectPlayer.release();
            soundEffectPlayer = null;
        }
    }
}