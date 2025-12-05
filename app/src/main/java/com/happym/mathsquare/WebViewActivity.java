package com.happym.mathsquare;

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
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.view.animation.BounceInterpolator;

// import androidx.activity.EdgeToEdge;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;


import android.view.View;
import com.google.firebase.FirebaseApp;

import java.io.IOException;
import java.util.Random;
import org.w3c.dom.Text;

import com.happym.mathsquare.sharedPreferences;
import com.happym.mathsquare.R;
import com.happym.mathsquare.utils.TutorialProgressTracker;
import com.happym.mathsquare.utils.GradeRestrictionUtil;

import com.happym.mathsquare.Animation.*;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView;
    private Toolbar toolbar;

    private FrameLayout numberContainer,backgroundFrame;
    private final Random random = new Random();
    private NumBGAnimation numBGAnimation;
    
    // Tutorial buttons - declared as class fields for access in filterTutorialsByGrade()
    private AppCompatButton tutaddition;
    private AppCompatButton tutmultiplication;
    private AppCompatButton tutdivision;
    private AppCompatButton tutsubtraction;
    private AppCompatButton tutdecimals;
    private AppCompatButton tutpercentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.layout_web);

        FirebaseApp.initializeApp(this);

        tutaddition = findViewById(R.id.btn_tut_addition);
        tutmultiplication = findViewById(R.id.btn_tut_multiplication);
        tutdivision = findViewById(R.id.btn_tut_division);
        tutsubtraction = findViewById(R.id.btn_tut_subtraction);
        tutdecimals = findViewById(R.id.btn_tut_decimals);
        tutpercentage = findViewById(R.id.btn_tut_percentage);

        animateButtonFocus(tutaddition);
        animateButtonFocus(tutmultiplication);
        animateButtonFocus(tutdivision);
        animateButtonFocus(tutsubtraction);
        animateButtonFocus(tutdecimals);
        animateButtonFocus(tutpercentage);

        /*

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back button
        getSupportActionBar().setTitle("WebView");

        webView = findViewById(R.id.webView);
        setupWebView();

        String webURL = getIntent().getStringExtra("URL");
        if (webURL != null) {
            webView.loadUrl(webURL);
        }

        */
        backgroundFrame = findViewById(R.id.main);
        numberContainer = findViewById(R.id.number_container); // Get FrameLayout from XML

        numBGAnimation = new NumBGAnimation(this, numberContainer);
        numBGAnimation.startNumberAnimationLoop();

        backgroundFrame.post(() -> {
        VignetteEffect.apply(this, backgroundFrame);
        });


    webView = findViewById(R.id.webView);
setupWebView();

        // Button click listeners to open PDF tutorials with progression check
        tutaddition.setOnClickListener(v -> checkAndOpenTutorial("addition.pdf", "addition"));
        tutmultiplication.setOnClickListener(v -> checkAndOpenTutorial("multiplication.pdf", "multiplication"));
        tutdivision.setOnClickListener(v -> checkAndOpenTutorial("division.pdf", "division"));
        tutsubtraction.setOnClickListener(v -> checkAndOpenTutorial("subtraction.pdf", "subtraction"));
        tutdecimals.setOnClickListener(v -> checkAndOpenTutorial("decimals.pdf", "decimals"));
        tutpercentage.setOnClickListener(v -> checkAndOpenTutorial("percentage.pdf", "percentage"));
        
        // Filter tutorials based on grade level
        filterTutorialsByGrade();
    }

    // Method to open the tutorial in WebView
    private void openTutorial(String url) {
        webView.setVisibility(View.VISIBLE);  // Show the WebView
        webView.loadUrl(url);                 // Load the URL
        MusicManager.pause();
    }

    // Method to open PDF tutorial with progression check
    private void checkAndOpenTutorial(String pdfFileName, String tutorialName) {
        TutorialProgressTracker tracker = new TutorialProgressTracker(this);
        
        // Check if tutorial is accessible (previous one completed)
        if (!tracker.canAccessTutorial(tutorialName)) {
            String previousTut = getPreviousTutorial(tutorialName);
            Toast.makeText(this, 
                "Please complete the " + previousTut + " tutorial first!", 
                Toast.LENGTH_LONG).show();
            return;
        }
        
        // Check grade level access
        String grade = sharedPreferences.getGrade(this);
        if (!GradeRestrictionUtil.isTutorialAllowedForGrade(grade, tutorialName)) {
            Toast.makeText(this, 
                "This tutorial is not available for your grade level", 
                Toast.LENGTH_LONG).show();
            return;
        }
        
        Intent intent = new Intent(this, PDFViewerActivity.class);
        intent.putExtra("PDF_FILE", pdfFileName);
        intent.putExtra("TUTORIAL_NAME", tutorialName);
        startActivity(intent);
        
        // Mark as completed when opened (you can also mark when finished)
        tracker.markTutorialCompleted(tutorialName);
    }
    
    private String getPreviousTutorial(String current) {
        String[] order = {"addition", "subtraction", "multiplication", "division", "decimals", "percentage"};
        for (int i = 1; i < order.length; i++) {
            if (order[i].equals(current.toLowerCase())) {
                return order[i - 1];
            }
        }
        return "";
    }
    
    // Filter tutorials based on grade level
    private void filterTutorialsByGrade() {
        String grade = sharedPreferences.getGrade(this);
        
        if (!GradeRestrictionUtil.isTutorialAllowedForGrade(grade, "multiplication")) {
            tutmultiplication.setVisibility(View.GONE);
            tutdivision.setVisibility(View.GONE);
        }
        if (!GradeRestrictionUtil.isTutorialAllowedForGrade(grade, "decimals")) {
            tutdecimals.setVisibility(View.GONE);
        }
        if (!GradeRestrictionUtil.isTutorialAllowedForGrade(grade, "percentage")) {
            tutpercentage.setVisibility(View.GONE);
        }
    }
    
    // Method to open PDF tutorial (kept for backwards compatibility)
    private void openPDFTutorial(String pdfFileName) {
        Intent intent = new Intent(this, PDFViewerActivity.class);
        intent.putExtra("PDF_FILE", pdfFileName);
        startActivity(intent);
    }


    //Game Button Animation Press

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

    //Text Title Animation

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
    animatorSet.addListener(new AnimatorListenerAdapter() {
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


    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(true);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
    }

    @Override
public void onBackPressed() {
    if (webView.getVisibility() == View.VISIBLE) {
        // Check if WebView can go back
        if (webView.canGoBack()) {
            webView.goBack();  // Navigate back in WebView history if possible
        } else {
            webView.setVisibility(View.GONE);  // Hide WebView if no more history
                MusicManager.resume();
        }
    } else {
        super.onBackPressed();  // Exit activity if WebView is not visible
    }
}


    /*

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    */
}
