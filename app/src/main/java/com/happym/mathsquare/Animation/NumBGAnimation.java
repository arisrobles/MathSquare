package com.happym.mathsquare.Animation;

import android.animation.*;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.*;

import java.util.Random;

public class NumBGAnimation {
    private final Context context;
    private final FrameLayout numberContainer;
    private final Random random = new Random();
    private final int[] numbers = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    private final int numberCount = 3;

    public NumBGAnimation(Context context, FrameLayout numberContainer) {
        this.context = context;
        this.numberContainer = numberContainer;
    }

    public void startNumberAnimationLoop() {
        changeNumbers();
    }

    private void changeNumbers() {
        numberContainer.removeAllViews();

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        for (int i = 0; i < numberCount; i++) {
            float randomY1 = random.nextInt(screenHeight - 300) + 100;
            float randomY2 = random.nextInt(screenHeight - 300) + 100;

            float startXLeft1 = -300f;
            float startXLeft2 = -600f;
            float endXLeft1 = screenWidth - (random.nextInt(screenWidth / 2 - 100) + 100);
            float endXLeft2 = screenWidth - (random.nextInt(screenWidth / 2 - 100) + 150);

            TextView numberLeft1 = createNumberTextView();
            numberLeft1.setText(String.valueOf(numbers[random.nextInt(numbers.length)]));
            numberLeft1.setX(startXLeft1);
            numberLeft1.setY(randomY1);
            numberContainer.addView(numberLeft1);
            numberLeft1.postDelayed(() -> animateNumber(numberLeft1, startXLeft1, endXLeft1), random.nextInt(3000));

            TextView numberLeft2 = createNumberTextView();
            numberLeft2.setText(String.valueOf(numbers[random.nextInt(numbers.length)]));
            numberLeft2.setX(startXLeft2);
            numberLeft2.setY(randomY2);
            numberContainer.addView(numberLeft2);
            numberLeft2.postDelayed(() -> animateNumber(numberLeft2, startXLeft2, endXLeft2), random.nextInt(6000));

            float startXRight1 = screenWidth + 300f;
            float startXRight2 = screenWidth + 600f;
            float endXRight1 = random.nextInt(screenWidth / 2 - 300) + 100;
            float endXRight2 = random.nextInt(screenWidth / 2 - 300) + 150;

            TextView numberRight1 = createNumberTextView();
            numberRight1.setText(String.valueOf(numbers[random.nextInt(numbers.length)]));
            numberRight1.setX(startXRight1);
            numberRight1.setY(randomY1);
            numberContainer.addView(numberRight1);
            numberRight1.postDelayed(() -> animateNumber(numberRight1, startXRight1, endXRight1), random.nextInt(3000));

            TextView numberRight2 = createNumberTextView();
            numberRight2.setText(String.valueOf(numbers[random.nextInt(numbers.length)]));
            numberRight2.setX(startXRight2);
            numberRight2.setY(randomY2);
            numberContainer.addView(numberRight2);
            numberRight2.postDelayed(() -> animateNumber(numberRight2, startXRight2, endXRight2), random.nextInt(6000));
        }

        numberContainer.postDelayed(this::changeNumbers, 20000);
    }

    private TextView createNumberTextView() {
        TextView textView = new TextView(context);
        textView.setTextSize(200);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.GRAY);
        textView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        ));
        return textView;
    }

    private void animateNumber(TextView textView, float startX, float endX) {
        textView.setX(startX);
        textView.setAlpha(0.65f);

        ObjectAnimator moveAnimation = ObjectAnimator.ofFloat(textView, "translationX", startX, endX);
        moveAnimation.setDuration(9000);
        moveAnimation.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator rotateAnimation = ObjectAnimator.ofFloat(textView, "rotation", -20f, 20f);
        rotateAnimation.setDuration(5000);
        rotateAnimation.setRepeatMode(ValueAnimator.REVERSE);
        rotateAnimation.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimation.setInterpolator(new LinearInterpolator());

        AnimatorSet moveAndRotate = new AnimatorSet();
        moveAndRotate.playTogether(moveAnimation, rotateAnimation);
        moveAndRotate.start();

        moveAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(textView, "alpha", 0.65f, 0);
                fadeOut.setDuration(4000);
                fadeOut.setInterpolator(new LinearInterpolator());
                fadeOut.start();
            }
        });
    }
}
