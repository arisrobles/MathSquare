package com.happym.mathsquare;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

public class FloatingTextView extends RelativeLayout {
    private Random random = new Random();
    private Handler handler = new Handler();
    private boolean useLetters = false; // Toggle between numbers and letters

    public FloatingTextView(Context context) {
        super(context);
        init();
    }

    public FloatingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatingTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        startAnimationLoop();
    }

    public void setUseLetters(boolean useLetters) {
        this.useLetters = useLetters;
    }

    private void startAnimationLoop() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                addFloatingText();
                handler.postDelayed(this, 500); // Adds a new floating text every 500ms
            }
        }, 500);
    }

    private void addFloatingText() {
        final TextView textView = new TextView(getContext());
        textView.setTextSize(random.nextInt(30) + 30); // Random size between 30sp - 60sp
        textView.setTextColor(Color.WHITE);
        textView.setAlpha(0.5f); // Set transparency

        // Choose between numbers (0-9) or letters (A-Z)
        String text = useLetters ? String.valueOf((char) (random.nextInt(26) + 'A')) 
                                 : String.valueOf(random.nextInt(10));
        textView.setText(text);

        // Random starting position
        int startX = random.nextInt(getWidth());
        int startY = random.nextInt(getHeight());
        textView.setX(startX);
        textView.setY(startY);

        addView(textView);

        // Animate movement
        animateText(textView);
    }

    private void animateText(final TextView textView) {
        int screenWidth = getWidth();
        int screenHeight = getHeight();

        int endX = random.nextInt(screenWidth);
        int endY = random.nextInt(screenHeight);

        // Move animation
        ObjectAnimator moveX = ObjectAnimator.ofFloat(textView, "x", textView.getX(), endX);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(textView, "y", textView.getY(), endY);

        // Rotation animation
        ObjectAnimator rotate = ObjectAnimator.ofFloat(textView, "rotation", 0, 360);

        // Fade in and out
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(textView, "alpha", 0.0f, 0.7f);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(textView, "alpha", 0.7f, 0.0f);

        moveX.setDuration(3000);
        moveY.setDuration(3000);
        rotate.setDuration(3000);
        fadeIn.setDuration(1500);
        fadeOut.setDuration(1500);

        moveX.setInterpolator(new AccelerateDecelerateInterpolator());
        moveY.setInterpolator(new AccelerateDecelerateInterpolator());
        rotate.setInterpolator(new AccelerateDecelerateInterpolator());

        moveX.start();
        moveY.start();
        rotate.start();
        fadeIn.start();

        fadeIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fadeOut.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}

            @Override
            public void onAnimationStart(Animator animation) {}
        });

        fadeOut.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                removeView(textView);
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}

            @Override
            public void onAnimationStart(Animator animation) {}
        });
    }
}
