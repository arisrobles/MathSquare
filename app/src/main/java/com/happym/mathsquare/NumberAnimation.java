package com.happym.mathsquare;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.Gravity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class NumberAnimation extends AppCompatActivity {

    private final Random random = new Random();
    private final int[] numbers = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    private FrameLayout layout;
    private final int numberCount = 5; // Number of numbers per side

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create and set up the layout
        layout = new FrameLayout(this);
        setContentView(layout);

        // Start number animation loop
        startNumberAnimationLoop();
    }

    private void startNumberAnimationLoop() {
        changeNumbers();
    }

    private void changeNumbers() {
        layout.removeAllViews(); // Clear previous numbers

        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        for (int i = 0; i < numberCount; i++) {
            // Create numbers from the left
            TextView numberTextViewLeft = createNumberTextView();
            numberTextViewLeft.setText(String.valueOf(numbers[random.nextInt(numbers.length)]));
            layout.addView(numberTextViewLeft);
            animateNumber(numberTextViewLeft, -300f, screenWidth / 2f - 300);

            // Create numbers from the right
            TextView numberTextViewRight = createNumberTextView();
            numberTextViewRight.setText(String.valueOf(numbers[random.nextInt(numbers.length)]));
            layout.addView(numberTextViewRight);
            animateNumber(numberTextViewRight, screenWidth + 300f, screenWidth / 2f + 300);
        }

        // Schedule the next wave of numbers
        layout.postDelayed(this::changeNumbers, 3000);
    }

    private TextView createNumberTextView() {
        TextView textView = new TextView(this);
        textView.setTextSize(48);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        ));
        return textView;
    }

    private void animateNumber(TextView textView, float startX, float endX) {
        textView.setX(startX);
        textView.setY(random.nextInt(600)); // Random Y position for a dynamic effect

        // Move animation
        ObjectAnimator moveAnimation = ObjectAnimator.ofFloat(textView, "translationX", startX, endX);
        moveAnimation.setDuration(2000);
        moveAnimation.setInterpolator(new LinearInterpolator());

        // Fade-in effect
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(1000);
        fadeIn.setFillAfter(true);
        textView.startAnimation(fadeIn);

        // Start moving animation
        moveAnimation.start();

        // Fade-out effect
        textView.postDelayed(() -> {
            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setDuration(1000);
            fadeOut.setFillAfter(true);
            textView.startAnimation(fadeOut);
        }, 2000);
    }
}
