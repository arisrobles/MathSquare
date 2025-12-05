package com.happym.mathsquare;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
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

import android.view.View;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.happym.mathsquare.studentSignUp;
import com.happym.mathsquare.teacherSignUp;
import com.happym.mathsquare.sharedPreferences;
import com.happym.mathsquare.NumberAnimation;
import com.happym.mathsquare.Animation.*;

public class signInUp extends AppCompatActivity {
    
    private MediaPlayer soundEffectPlayer;
    private MediaPlayer bgMediaPlayer;
    private FirebaseFirestore db;
    private NumBGAnimation numBGAnimation;
    private FrameLayout numberContainer,backgroundFrame;
    private final Random random = new Random();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
      //  EdgeToEdge.enable(this);
        setContentView(R.layout.activity_opening);

        playBGGame("bgmusic.mp3");
        
        db = FirebaseFirestore.getInstance();
        
         // Show Loading Dialog
    ProgressDialog loadingDialog = new ProgressDialog(this);
    loadingDialog.setMessage("Checking Your Account...");
    loadingDialog.setCancelable(true);
    loadingDialog.show();
        
        
             
        if(sharedPreferences.isLoggedIn(this)){
            
            loadingDialog.dismiss();
            Intent intent = new Intent(signInUp.this, Dashboard.class);
            startActivity(intent);
            Toast.makeText(this, "Welcome back Teacher!", Toast.LENGTH_SHORT).show();
            finish();
            
        }else if(sharedPreferences.StudentIsLoggedIn(this)){
                
            String section = sharedPreferences.getSection(this);
            String grade = sharedPreferences.getGrade(this);
            String firstName = sharedPreferences.getFirstN(this);
            String lastName = sharedPreferences.getLastN(this);

    CollectionReference collectionRef = db.collection("Accounts")
            .document("Students")
            .collection("MathSquare");

            // Query to check if a document with the same firstName, lastName, grade, and section exists
            collectionRef
            .whereEqualTo("firstName", firstName)
            .whereEqualTo("lastName", lastName)
            .whereEqualTo("grade", grade)
            .whereEqualTo("section", section)
            .get()
            .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                       loadingDialog.dismiss(); 
                        
                        // Check if student account exists in Firebase
                        if (!task.getResult().isEmpty()) {
                            // Account exists - auto-login successful
                            Log.d("STUDENT_LOGIN", "✅ Student account found in Firebase - Auto-login successful");
                            Intent intent = new Intent(signInUp.this, MainActivity.class);
                       startActivity(intent);
                        sharedPreferences.setLoggedIn(this, false);
                        finish();
                        } else {
                            // Account NOT found in Firebase - account was deleted
                            Log.w("STUDENT_LOGIN", "❌ Student account NOT found in Firebase - Account was deleted");
                            Toast.makeText(this, "Account Deleted, Sign Up a new Student Account", Toast.LENGTH_LONG).show();
                            
                            // Clear login state
                            sharedPreferences.StudentIsSetLoggedIn(this, false);
                            sharedPreferences.setLoggedIn(this, false);
                            
                            // Navigate to sign up
                            Intent intenttwo = new Intent(signInUp.this, studentSignUp.class);
                            startActivity(intenttwo);
                            finish();
                            }
                      } else {
                          // Query failed
                          loadingDialog.dismiss();
                          Log.e("STUDENT_LOGIN", "❌ Error checking student account", task.getException());
                          Toast.makeText(this, "Error checking account. Please try again.", Toast.LENGTH_LONG).show();
                      }  
                   })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error fetching student data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    
                   loadingDialog.dismiss();
            });
            
        }else{
            loadingDialog.dismiss();
            Toast.makeText(this, "Select to Sign Up", Toast.LENGTH_SHORT).show();
            LinearLayout btnEnterStudent = findViewById(R.id.btn_playgame_as_student);
        LinearLayout btnEnterTeacher = findViewById(R.id.btn_signinteacher);
            LinearLayout btnEnterAsGuest = findViewById(R.id.btn_playgame_as_guest);
        Button btnExit = findViewById(R.id.btn_exitgame);

        animateButtonFocus(btnEnterStudent);
        animateButtonFocus(btnEnterTeacher);
            animateButtonFocus(btnEnterAsGuest);
        animateButtonFocus(btnExit);
        
            
            btnEnterStudent.setOnClickListener(view -> {
                    playSound("click.mp3");
    animateButtonPushDowm(btnEnterStudent);  
    Intent intent = new Intent(signInUp.this, studentSignUp.class);
    startActivity(intent);
                    
                stopButtonFocusAnimation(btnEnterStudent);
                animateButtonFocus(btnEnterStudent);
});

btnEnterTeacher.setOnClickListener(view -> {
                    playSound("click.mp3");
    animateButtonPushDowm(btnEnterTeacher);  
    Intent intent = new Intent(signInUp.this, teacherLogIn.class);
    startActivity(intent);
                stopButtonFocusAnimation(btnEnterTeacher);
                animateButtonFocus(btnEnterTeacher);
});
            
            btnEnterAsGuest.setOnClickListener(view -> {
                    playSound("click.mp3");
    animateButtonPushDowm(btnEnterAsGuest);  
    Intent intent = new Intent(signInUp.this, MainActivity.class);
    startActivity(intent);
                stopButtonFocusAnimation(btnEnterAsGuest);
                animateButtonFocus(btnEnterAsGuest);
});

btnExit.setOnClickListener(view -> {
                    playSound("click.mp3");
    animateButtonPushDowm(btnExit);  
    finishAffinity();
    System.exit(0);
});

// Focus Listeners
btnEnterStudent.setOnTouchListener((v, event) -> {
    switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            animateButtonClick(btnEnterStudent);  // Start touch animation
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            stopButtonFocusAnimation(btnEnterStudent);  // Stop animation when touch is released or canceled
            break;
    }
    return false;  // Return false to allow long click events to be handled
});

btnEnterStudent.setOnLongClickListener(v -> {
    animateButtonPushDowm(btnEnterStudent);  // Start long press animation
    return true;  // Return true to indicate the long press was handled
});


        btnEnterTeacher.setOnTouchListener((v, event) -> {
    switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            animateButtonClick(btnEnterTeacher);  // Start touch animation
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            stopButtonFocusAnimation(btnEnterTeacher);  // Stop animation when touch is released or canceled
            break;
    }
    return false;  // Return false to allow long click events to be handled
});

btnEnterTeacher.setOnLongClickListener(v -> {
    animateButtonPushDowm(btnEnterTeacher);  // Start long press animation
    return true;  // Return true to indicate the long press was handled
});
        

        
        btnExit.setOnTouchListener((v, event) -> {
    switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            animateButtonClick(btnExit);  // Start touch animation
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            stopButtonFocusAnimation(btnExit);  // Stop animation when touch is released or canceled
            break;
    }
    return false;  // Return false to allow long click events to be handled
});

btnExit.setOnLongClickListener(v -> {
    animateButtonPushDowm(btnExit);  // Start long press animation
    return true;  // Return true to indicate the long press was handled
});
        


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
            
        }
        backgroundFrame = findViewById(R.id.main);
        numberContainer = findViewById(R.id.number_container); // Get FrameLayout from XML

        numBGAnimation = new NumBGAnimation(this, numberContainer);
        numBGAnimation.startNumberAnimationLoop();
        
        backgroundFrame.post(() -> {
        VignetteEffect.apply(this, backgroundFrame);
        });
        
    }
    
   private void playBGGame(String fileName) {
    if (bgMediaPlayer == null) { // Prevent re-initializing
            try {
        AssetFileDescriptor afd = getAssets().openFd(fileName);
        bgMediaPlayer = new MediaPlayer();
        bgMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        bgMediaPlayer.prepare();
        bgMediaPlayer.setOnCompletionListener(mp -> {
            mp.release();
            bgMediaPlayer = null;
        });
                
         // Enable looping
        bgMediaPlayer.setLooping(true);
                       
        bgMediaPlayer.start();
    } catch (IOException e) {
        e.printStackTrace();
    }
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

    //Game Button Animation Press 

private void animateButtonClick(View button) {
    ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.6f, 1.1f, 1f);
    ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.6f, 1.1f, 1f);

    // Set duration for the animations
    scaleX.setDuration(3000);
    scaleY.setDuration(3000);

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

    private void animateButtonPushDowm(View button) {
    ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f);  // Scale down slightly
    ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f);  // Scale down slightly

    // Set shorter duration for a quick push effect
    scaleX.setDuration(200);
    scaleY.setDuration(200);

    // Use a smooth interpolator
    AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
    scaleX.setInterpolator(interpolator);
    scaleY.setInterpolator(interpolator);

    // Combine the animations into an AnimatorSet
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(scaleX, scaleY);
    
    // Start the animation
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
protected void onDestroy() {
    super.onDestroy();
   if (bgMediaPlayer != null) {
        bgMediaPlayer.release();
        bgMediaPlayer = null;
    }
    
}

    @Override
    protected void onResume() {
        super.onResume();
        
       if (bgMediaPlayer != null) {
            bgMediaPlayer.start();
            }
    }
    
@Override
    protected void onPause() {
        super.onPause();

        if (bgMediaPlayer != null) {
            bgMediaPlayer.pause();
        }
        
    }
    
}


    
