package com.happym.mathsquare.GameType.Practice;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.AssetFileDescriptor;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.util.TypedValue;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.WindowCompat;
import android.graphics.drawable.Drawable;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.happym.mathsquare.MultipleChoicePage;
import com.happym.mathsquare.MusicManager;
import com.happym.mathsquare.dashboard_StudentsPanel;
import com.happym.mathsquare.dashboard_SectionPanel;
import com.happym.mathsquare.dialog.CreateSection;

import com.happym.mathsquare.R;
import java.io.IOException;
import java.util.HashSet;
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

public class PracticeLevels extends AppCompatActivity {

    private FrameLayout levelone, leveltwo, levelthree, levelfour, heartChoice, timerChoice, questionChoice;
    private ImageView buttonOne, buttonTwo, buttonThree, buttonFour, iconHeart, iconClock, iconQuestion, imgone,imgtwo
            ,imgthree, imgfour;
    private MediaPlayer soundEffectPlayer;
    private TextView heartTxt, timerTxt, questionTxt;
    private String difficulty;
    HashSet<Integer> selectedButtons = new HashSet<>();
    int selectedNumber = 0;
    
    private boolean isButtonOneOn = false;
    private boolean isButtonTwoOn = false;
    private boolean isButtonThreeOn = false;
    private boolean isButtonFourOn = false;

    int heartCount = 3;
    int timerCount = 5;
    int questionCount = 10;
    
    private FrameLayout numberContainer,backgroundFrame;
    private NumBGAnimation numBGAnimation;
    
    private String difficultySection;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.practice_levels);

         // Initialize the levels
        AppCompatButton btnEnter = findViewById(R.id.btn_enter);
        
        heartChoice = findViewById(R.id.heart_choice);
        timerChoice = findViewById(R.id.timer_choice);
        
        
        String operation = getIntent().getStringExtra("operation");
        difficulty = getIntent().getStringExtra("difficulty");
        
        ImageView operationDisplayIcon = findViewById(R.id.difficultyImage);
        
    // Set images based on the operation
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
        operationDisplayIcon.setImageResource(R.drawable.btn_operation_add);
    }

         if ("grade_one".equals(difficulty)) {
   difficultySection = "Easy";
} else if ("grade_two".equals(difficulty)) {
    difficultySection = "Easy";
} else if ("grade_three".equals(difficulty)) {
    difficultySection = "Medium";
} else if ("grade_four".equals(difficulty)) {
    difficultySection = "Medium";
} else if ("grade_five".equals(difficulty)) {
    difficultySection = "Medium";
} else if ("grade_six".equals(difficulty)) {
    difficultySection = "Hard";
} else {
    difficultySection = "Easy";
}

        animateButtonFocus(btnEnter);

        
        heartTxt = findViewById(R.id.heart_txt);
        timerTxt = findViewById(R.id.timer_txt);
        
        
        iconHeart = findViewById(R.id.icon_heart);
        iconClock = findViewById(R.id.icon_timer);
        
        btnEnter.setOnClickListener(v -> {
                playSound("click.mp3");
// Create intent to start MultipleChoicePage
Intent intent = new Intent(PracticeLevels.this, MultipleChoicePage.class);
intent.putExtra("operation", operation);
// For percentage and decimal operations, pass the actual grade level
if ("Percentage".equals(operation) || "Decimal".equals(operation) || 
    "DecimalAddition".equals(operation) || "DecimalSubtraction".equals(operation) || 
    "DecimalMultiplication".equals(operation) || "DecimalDivision".equals(operation)) {
    intent.putExtra("difficulty", difficulty);
} else {
intent.putExtra("difficulty", difficultySection);
}
intent.putExtra("game_type", "practice");
intent.putExtra("heartLimit", heartCount);
intent.putExtra("timerLimit", timerCount);
intent.putExtra("questionLimit", 20);
                
// Animate button click and stop animation
animateButtonClick(btnEnter);
stopButtonFocusAnimation(btnEnter);

// Start the activity
startActivity(intent);
  
        });
        
 
        heartTxt.setText(String.valueOf(heartCount));
    timerTxt.setText(String.valueOf(timerCount));
    
        
        // Set onClickListeners for each FrameLayout
        heartChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    playSound("click.mp3");
                    animateButtonClick(heartChoice);
                heartCount++;
                heartTxt.setText(String.valueOf(heartCount));
            }
        });

        timerChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    playSound("click.mp3");
                    animateButtonClick(timerChoice);
                timerCount++;
                timerTxt.setText(String.valueOf(timerCount));
            }
        });

        
        animateButtonFocus(iconHeart);
        animateButtonFocus(iconClock);
        
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
    private void updateSelectedNumber() {
    selectedNumber = 0;
    for (int number : selectedButtons) {
        selectedNumber += number;
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