package com.happym.mathsquare;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.WindowCompat;

import com.happym.mathsquare.sharedPreferences;
import com.happym.mathsquare.utils.GradeRestrictionUtil;
import com.happym.mathsquare.utils.TutorialProgressTracker;
import com.happym.mathsquare.Animation.*;
import com.happym.mathsquare.MusicManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VideoPlayerActivity extends AppCompatActivity {
    private MediaPlayer soundEffectPlayer;
    private FrameLayout numberContainer, backgroundFrame;
    private final Random random = new Random();
    private NumBGAnimation numBGAnimation;
    
    private String tutorialName;
    private String grade;
    private int gradeNum;
    private boolean hasMarkedCompleted = false; // Track if we've already marked as completed
    
    private LinearLayout stepByStepContainer;
    private LinearLayout additionalVideosContainer;
    private ScrollView scrollView;
    
    // YouTube video URLs by grade and operation
    private static final String GRADE_1_ADDITION_VIDEO = "https://youtu.be/s79OUi4Nog0?feature=shared";
    private static final String GRADE_1_SUBTRACTION_VIDEO = "https://youtube.com/shorts/c7lBcItBqFE?feature=shared";
    private static final String GRADE_2_SUBTRACTION_VIDEO = "https://youtube.com/shorts/xnQ11Bpv-68?feature=shared";
    private static final String GRADE_2_ADDITION_VIDEO = "https://youtube.com/shorts/eL6VzyCPivY?feature=shared";
    
    private static final String GRADE_3_4_ADDITION_VIDEO = "https://youtu.be/1Al2Fc3wOIQ?feature=shared";
    private static final String GRADE_3_4_SUBTRACTION_VIDEO = "https://youtube.com/shorts/whGG2OwvJ_g?feature=shared";
    private static final String GRADE_3_4_MULTIPLICATION_VIDEO = "https://youtu.be/-aNSUlo5sSA?feature=shared";
    private static final String GRADE_3_4_DIVISION_VIDEO = "https://youtu.be/irfMCIgFJZY?feature=shared";
    
    private static final String GRADE_5_6_ADDITION_VIDEO = "https://youtu.be/1Al2Fc3wOIQ?feature=shared";
    private static final String GRADE_5_6_SUBTRACTION_VIDEO = "https://youtube.com/shorts/whGG2OwvJ_g?feature=shared";
    private static final String GRADE_5_6_MULTIPLICATION_VIDEO = "https://youtu.be/-aNSUlo5sSA?feature=shared";
    private static final String GRADE_5_6_DIVISION_VIDEO = "https://youtu.be/irfMCIgFJZY?feature=shared";
    private static final String GRADE_5_6_DECIMAL_VIDEO = "https://youtu.be/PF6r1N6rglY?si=XhTKbAnED0xeQNqK";
    private static final String GRADE_5_6_PERCENTAGE_VIDEO = "https://youtu.be/kDFLcCOS7aw?feature=shared";
    
    // Video credits
    private static final String VIDEO_CREDITS = "Video credits to respective owners";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.layout_video_player);
        
        // Pause app music when video player opens
        MusicManager.pause();
        
        tutorialName = getIntent().getStringExtra("TUTORIAL_NAME");
        grade = sharedPreferences.getGrade(this);
        gradeNum = getGradeNumber(grade);
        
        if (tutorialName == null) {
            Toast.makeText(this, "Tutorial not specified", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        setupUI();
        
        backgroundFrame = findViewById(R.id.main);
        numberContainer = findViewById(R.id.number_container);
        
        numBGAnimation = new NumBGAnimation(this, numberContainer);
        numBGAnimation.startNumberAnimationLoop();
        
        backgroundFrame.post(() -> {
            VignetteEffect.apply(this, backgroundFrame);
        });
    }
    
    private void setupUI() {
        TextView titleText = findViewById(R.id.tutorial_title);
        String displayName = capitalizeFirst(tutorialName);
        titleText.setText(displayName + " Tutorial - Videos");
        
        stepByStepContainer = findViewById(R.id.step_by_step_container);
        additionalVideosContainer = findViewById(R.id.additional_videos_container);
        scrollView = findViewById(R.id.scroll_view);
        
        stepByStepContainer.removeAllViews();
        additionalVideosContainer.removeAllViews();
        
        // Add Additional Videos section FIRST (YouTube videos)
        addSectionHeader(additionalVideosContainer, "Additional Videos");
        List<String> additionalUrls = getAdditionalVideoUrls();
        if (additionalUrls != null && !additionalUrls.isEmpty()) {
            for (String url : additionalUrls) {
                addVideoPlayer(additionalVideosContainer, url, "Additional Tutorial");
            }
        } else {
            addEmptyMessage(additionalVideosContainer, "Additional videos not available");
        }
        
        // Add Step-by-Step Video section LAST (local video from raw folder)
        addSectionHeader(stepByStepContainer, "Step-by-Step Video");
        String tutLower = tutorialName.toLowerCase();
        
        if (tutLower.contains("decimals")) {
            // For decimals, show all three videos
            String[] decimalVideos = {"decimal_addition", "decimal_multiplication", "decimal_subtraction"};
            String[] decimalTitles = {"Decimal Addition - Step-by-Step", "Decimal Multiplication - Step-by-Step", "Decimal Subtraction - Step-by-Step"};
            for (int i = 0; i < decimalVideos.length; i++) {
                addLocalVideoPlayer(stepByStepContainer, decimalVideos[i], decimalTitles[i]);
            }
        } else {
            String localVideoFileName = getLocalVideoFileName();
            if (localVideoFileName != null) {
                addLocalVideoPlayer(stepByStepContainer, localVideoFileName, "Step-by-Step Tutorial");
            } else {
                addEmptyMessage(stepByStepContainer, "Step-by-step video not available");
            }
        }
        
        // Add credits text
        TextView creditsText = findViewById(R.id.video_credits);
        if (creditsText != null) {
            creditsText.setText(VIDEO_CREDITS);
            creditsText.setVisibility(View.VISIBLE);
        }
    }
    
    private void addSectionHeader(LinearLayout container, String title) {
        TextView header = new TextView(this);
        header.setText(title);
        header.setTextSize(22);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setTextColor(getResources().getColor(android.R.color.white));
        header.setPadding(25, 30, 25, 15);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        header.setLayoutParams(params);
        container.addView(header);
    }
    
    private void addVideoPlayer(LinearLayout container, String videoUrl, String videoTitle) {
        // Create container for video
        LinearLayout videoContainer = new LinearLayout(this);
        videoContainer.setOrientation(LinearLayout.VERTICAL);
        videoContainer.setPadding(25, 10, 25, 30);
        videoContainer.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        
        // Add title
        TextView titleView = new TextView(this);
        titleView.setText(videoTitle);
        titleView.setTextSize(18);
        titleView.setTextColor(getResources().getColor(android.R.color.white));
        titleView.setPadding(0, 0, 0, 10);
        videoContainer.addView(titleView);
        
        // Convert YouTube URL to embed format
        String embedUrl = convertToEmbedUrl(videoUrl);
        
        if (embedUrl == null) {
            // If conversion failed, show error message with fallback button
            TextView errorView = new TextView(this);
            errorView.setText("Unable to embed video. Click button below to watch on YouTube.");
            errorView.setTextSize(14);
            errorView.setTextColor(getResources().getColor(android.R.color.white));
            errorView.setPadding(25, 20, 25, 10);
            errorView.setGravity(android.view.Gravity.CENTER);
            videoContainer.addView(errorView);
            
            // Add button to open in YouTube
            AppCompatButton openButton = new AppCompatButton(this);
            openButton.setText("Watch on YouTube");
            openButton.setTextSize(16);
            openButton.setTextColor(getResources().getColor(android.R.color.white));
            openButton.setBackgroundResource(R.drawable.btn_long_condition_green);
            openButton.setPadding(0, 15, 0, 15);
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            buttonParams.setMargins(25, 10, 25, 0);
            openButton.setLayoutParams(buttonParams);
            openButton.setOnClickListener(v -> {
                playSound("click.mp3");
                openYouTubeVideo(videoUrl);
            });
            videoContainer.addView(openButton);
            container.addView(videoContainer);
            return;
        }
        
        // Set WebView dimensions - center it
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int videoWidth = (int) (screenWidth * 0.9); // 90% of screen width
        int videoHeight = (int) (videoWidth * 0.5625); // 16:9 aspect ratio
        
        // Extract video ID and try improved embedding method
        String videoId = extractVideoId(videoUrl);
        if (videoId != null) {
            addYouTubeWebViewPlayer(videoContainer, videoId, videoUrl, videoWidth, videoHeight);
        } else {
            // Fallback to button if video ID extraction fails
            TextView errorView = new TextView(this);
            errorView.setText("Unable to load video. Click button below to watch on YouTube.");
            errorView.setTextSize(14);
            errorView.setTextColor(getResources().getColor(android.R.color.white));
            errorView.setPadding(25, 20, 25, 10);
            errorView.setGravity(android.view.Gravity.CENTER);
            videoContainer.addView(errorView);
            
            AppCompatButton openButton = new AppCompatButton(this);
            openButton.setText("Watch on YouTube");
            openButton.setTextSize(16);
            openButton.setTextColor(getResources().getColor(android.R.color.white));
            openButton.setBackgroundResource(R.drawable.btn_long_condition_green);
            openButton.setPadding(0, 15, 0, 15);
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            buttonParams.setMargins(25, 10, 25, 0);
            openButton.setLayoutParams(buttonParams);
            openButton.setOnClickListener(v -> {
                playSound("click.mp3");
                openYouTubeVideo(videoUrl);
            });
            videoContainer.addView(openButton);
        }
        
        container.addView(videoContainer);
    }
    
    private void openYouTubeVideo(String url) {
        if (url == null) {
            Toast.makeText(this, "Video URL not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            android.net.Uri uri = android.net.Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.youtube");
            if (intent.resolveActivity(getPackageManager()) == null) {
                // Fallback to browser if YouTube app not installed
                intent.setPackage(null);
            }
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open video", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void addEmptyMessage(LinearLayout container, String message) {
        TextView emptyText = new TextView(this);
        emptyText.setText(message);
        emptyText.setTextSize(16);
        emptyText.setTextColor(getResources().getColor(android.R.color.white));
        emptyText.setPadding(25, 20, 25, 20);
        emptyText.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        emptyText.setLayoutParams(params);
        container.addView(emptyText);
    }
    
    private String extractVideoId(String youtubeUrl) {
        if (youtubeUrl == null) return null;
        
        String videoId = null;
        
        // Handle different YouTube URL formats
        if (youtubeUrl.contains("youtu.be/")) {
            int start = youtubeUrl.indexOf("youtu.be/") + 9;
            int end = youtubeUrl.indexOf("?", start);
            if (end == -1) end = youtubeUrl.length();
            videoId = youtubeUrl.substring(start, end);
        } else if (youtubeUrl.contains("youtube.com/shorts/")) {
            int start = youtubeUrl.indexOf("shorts/") + 7;
            int end = youtubeUrl.indexOf("?", start);
            if (end == -1) end = youtubeUrl.length();
            videoId = youtubeUrl.substring(start, end);
        } else if (youtubeUrl.contains("youtube.com/watch?v=")) {
            int start = youtubeUrl.indexOf("v=") + 2;
            int end = youtubeUrl.indexOf("&", start);
            if (end == -1) end = youtubeUrl.length();
            videoId = youtubeUrl.substring(start, end);
        }
        
        // Clean video ID
        if (videoId != null) {
            videoId = videoId.trim();
            videoId = videoId.replaceAll("[^a-zA-Z0-9_-]", "");
            if (!videoId.isEmpty()) {
                return videoId;
            }
        }
        
        return null;
    }
    
    private void addYouTubeWebViewPlayer(LinearLayout videoContainer, String videoId, String originalUrl, int videoWidth, int videoHeight) {
        // Create WebView for video
        WebView webView = new WebView(this);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        
        // Use mobile user agent that matches YouTube's mobile site expectations
        // This helps YouTube serve the mobile-optimized player
        String mobileUserAgent = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Mobile Safari/537.36";
        webSettings.setUserAgentString(mobileUserAgent);
        
        // Use WebChromeClient to handle video playback and fullscreen
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(android.view.View view, android.webkit.WebChromeClient.CustomViewCallback callback) {
                // Handle fullscreen video
                super.onShowCustomView(view, callback);
            }
            
            @Override
            public void onHideCustomView() {
                // Handle exit from fullscreen
                super.onHideCustomView();
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, android.webkit.WebResourceRequest request) {
                String url = request.getUrl().toString();
                // Allow YouTube domains to load in WebView
                if (url.contains("youtube.com") || url.contains("youtu.be") || 
                    url.contains("google.com") || url.contains("googleapis.com") ||
                    url.contains("gstatic.com") || url.contains("ggpht.com")) {
                    return false; // Let WebView handle it
                }
                // For other URLs, open externally
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    openYouTubeVideo(url);
                    return true;
                }
                return false;
            }
            
            @Override
            @Deprecated
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Allow YouTube domains to load in WebView
                if (url.contains("youtube.com") || url.contains("youtu.be") || 
                    url.contains("google.com") || url.contains("googleapis.com") ||
                    url.contains("gstatic.com") || url.contains("ggpht.com")) {
                    return false; // Let WebView handle it
                }
                // For other URLs, open externally
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    openYouTubeVideo(url);
                    return true;
                }
                return false;
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Ensure music is paused when YouTube page loads
                MusicManager.pause();
                // Inject JavaScript to show ONLY the video player - hide everything else
                String js = 
                    "(function() { " +
                    // Comprehensive function to hide all unwanted elements
                    "function hideAllElements() { " +
                    "  var selectors = [ " +
                    "    'header', '#header', '#masthead', " +
                    "    '#comments', '#comments-section', '.ytd-comments', 'ytd-comments', " +
                    "    '#comment', '.comment', 'ytd-comment-thread-renderer', " +
                    "    'ytd-comments-header-renderer', 'ytd-comment-simplebox-renderer', " +
                    "    '#related', '#related-videos', '.ytd-watch-next-secondary-results-renderer', " +
                    "    'ytd-watch-next-secondary-results-renderer', 'ytd-item-section-renderer', " +
                    "    '.ytp-pause-overlay', '.ytp-suggested-action', " +
                    "    'footer', '#footer-container', " +
                    "    'nav', '#navigation', '.ytd-mobile-topbar-renderer', " +
                    "    '.ytd-video-primary-info-renderer', '#title', '.title', " +
                    "    '.ytd-video-secondary-info-renderer', '#owner', '#owner-sub-count', " +
                    "    '.ytd-video-owner-renderer', '.ytd-channel-name', " +
                    "    '#description', '.ytd-video-secondary-info-renderer', " +
                    "    '.ytd-expander', '#more', '#less', " +
                    "    '.ytd-action-panel-renderer', '#actions', " +
                    "    '.ytd-menu-renderer', '.ytd-toggle-button-renderer', " +
                    "    'ytd-app', 'ytd-masthead', 'ytd-mini-guide-renderer', " +
                    "    '.ytd-watch-metadata', '.metadata', " +
                    "    'ytd-video-primary-info-renderer', 'ytd-video-secondary-info-renderer', " +
                    "    'ytd-watch-metadata', 'ytd-watch-info-text', " +
                    "    'ytd-video-meta-block', 'ytd-video-owner-renderer', " +
                    "    'ytd-sentiment-bar-renderer', 'ytd-menu-renderer', " +
                    "    'ytd-watch-flexy[flexy]', 'ytd-watch-flexy > *:not(ytd-player)', " +
                    "    '#secondary', '#secondary-inner', " +
                    "    'ytd-watch-next-secondary-results-renderer', " +
                    "    'ytd-item-section-renderer:not(:has(video))', " +
                    "    '[class*=\"comment\"]', '[id*=\"comment\"]', " +
                    "    '[class*=\"related\"]', '[id*=\"related\"]' " +
                    "  ]; " +
                    "  selectors.forEach(function(sel) { " +
                    "    try { " +
                    "      var els = document.querySelectorAll(sel); " +
                    "      els.forEach(function(el) { " +
                    "        if(el && !el.querySelector('video')) { " +
                    "          el.style.display = 'none !important'; " +
                    "          el.style.visibility = 'hidden !important'; " +
                    "          el.style.height = '0 !important'; " +
                    "          el.style.margin = '0 !important'; " +
                    "          el.style.padding = '0 !important'; " +
                    "          el.style.opacity = '0'; " +
                    "          el.setAttribute('hidden', 'true'); " +
                    "        } " +
                    "      }); " +
                    "    } catch(e) {} " +
                    "  }); " +
                    "  // Also hide any element containing 'comment' in class or id " +
                    "  try { " +
                    "    var allElements = document.querySelectorAll('*'); " +
                    "    allElements.forEach(function(el) { " +
                    "      var className = el.className || ''; " +
                    "      var id = el.id || ''; " +
                    "      if ((className.toLowerCase().indexOf('comment') !== -1 || " +
                    "           id.toLowerCase().indexOf('comment') !== -1) && " +
                    "          !el.querySelector('video')) { " +
                    "        el.style.display = 'none !important'; " +
                    "        el.style.visibility = 'hidden !important'; " +
                    "        el.style.height = '0 !important'; " +
                    "      } " +
                    "    }); " +
                    "  } catch(e) {} " +
                    "} " +
                    "hideAllElements(); " +
                    // Show only the video player container
                    "function showOnlyVideo() { " +
                    "  var playerContainer = document.querySelector('#player, #player-container, ytd-player, .html5-video-container, ytd-player-legacy'); " +
                    "  if (playerContainer) { " +
                    "    playerContainer.style.display = 'block'; " +
                    "    playerContainer.style.visibility = 'visible'; " +
                    "    playerContainer.style.width = '100%'; " +
                    "    playerContainer.style.margin = '0 auto'; " +
                    "    playerContainer.style.maxHeight = '100%'; " +
                    "  } " +
                    "} " +
                    "showOnlyVideo(); " +
                    // Ensure video player has controls and is properly sized
                    "function setupVideo() { " +
                    "  var player = document.querySelector('video'); " +
                    "  if (player) { " +
                    "    player.setAttribute('controls', 'true'); " +
                    "    player.setAttribute('playsinline', 'true'); " +
                    "    player.style.width = '100%'; " +
                    "    player.style.maxWidth = '100%'; " +
                    "    player.style.height = 'auto'; " +
                    "    player.style.display = 'block'; " +
                    "    player.style.margin = '0'; " +
                    "    player.style.padding = '0'; " +
                    "    player.controls = true; " +
                    "  } " +
                    "} " +
                    "setupVideo(); " +
                    // Make body background black and remove all padding/margins
                    "document.body.style.backgroundColor = '#000'; " +
                    "document.body.style.margin = '0'; " +
                    "document.body.style.padding = '0'; " +
                    "document.body.style.overflow = 'hidden'; " +
                    "document.documentElement.style.backgroundColor = '#000'; " +
                    "document.documentElement.style.margin = '0'; " +
                    "document.documentElement.style.padding = '0'; " +
                    "document.documentElement.style.overflow = 'hidden'; " +
                    // Hide main content container except video - more aggressive
                    "var mainContent = document.querySelector('#content, #primary, ytd-watch-flexy, ytd-watch'); " +
                    "if (mainContent) { " +
                    "  var children = mainContent.children; " +
                    "  for (var i = 0; i < children.length; i++) { " +
                    "    var child = children[i]; " +
                    "    if (child.id !== 'player' && child.id !== 'player-container' && " +
                    "        !child.querySelector('video') && !child.querySelector('ytd-player')) { " +
                    "      child.style.display = 'none !important'; " +
                    "      child.style.visibility = 'hidden !important'; " +
                    "      child.style.height = '0 !important'; " +
                    "    } " +
                    "  } " +
                    "} " +
                    // Use MutationObserver to catch dynamically loaded content (like comments)
                    "var observer = new MutationObserver(function(mutations) { " +
                    "  hideAllElements(); " +
                    "  showOnlyVideo(); " +
                    "}); " +
                    "observer.observe(document.body, { " +
                    "  childList: true, " +
                    "  subtree: true " +
                    "}); " +
                    // Scroll to top to show video
                    "window.scrollTo(0, 0); " +
                    "})();";
                
                view.evaluateJavascript(js, null);
                
                // Also inject after delays to catch dynamically loaded content (comments load late)
                view.postDelayed(() -> {
                    view.evaluateJavascript(
                        "(function() { " +
                        "function hideCommentsAndRelated() { " +
                        "  var hideSelectors = [ " +
                        "    '#related', '#related-videos', '.ytd-watch-next-secondary-results-renderer', " +
                        "    '#comments', '#comments-section', '.ytd-comments', 'ytd-comments', " +
                        "    'ytd-comment-thread-renderer', 'ytd-comments-header-renderer', " +
                        "    'ytd-comment-simplebox-renderer', '#comment', '.comment', " +
                        "    '.ytp-suggested-action', '.ytp-pause-overlay', " +
                        "    'header', 'footer', 'nav', " +
                        "    '.ytd-video-primary-info-renderer', '#title', '.title', " +
                        "    '.ytd-video-secondary-info-renderer', '#owner', '#description', " +
                        "    'ytd-video-primary-info-renderer', 'ytd-video-secondary-info-renderer', " +
                        "    '.ytd-watch-metadata', '.metadata', " +
                        "    'ytd-action-panel-renderer', '#actions', " +
                        "    '#secondary', '#secondary-inner', " +
                        "    'ytd-item-section-renderer:not(:has(video))' " +
                        "  ]; " +
                        "  hideSelectors.forEach(function(sel) { " +
                        "    try { " +
                        "      var els = document.querySelectorAll(sel); " +
                        "      els.forEach(function(el) { " +
                        "        if(el && !el.querySelector('video') && !el.querySelector('ytd-player')) { " +
                        "          el.style.display = 'none !important'; " +
                        "          el.style.visibility = 'hidden !important'; " +
                        "          el.style.height = '0 !important'; " +
                        "          el.style.opacity = '0'; " +
                        "        } " +
                        "      }); " +
                        "    } catch(e) {} " +
                        "  }); " +
                        "  // Hide any element with 'comment' in class/id " +
                        "  try { " +
                        "    var allElements = document.querySelectorAll('*'); " +
                        "    allElements.forEach(function(el) { " +
                        "      var className = (el.className && el.className.toString) ? el.className.toString() : ''; " +
                        "      var id = el.id || ''; " +
                        "      if ((className.toLowerCase().indexOf('comment') !== -1 || " +
                        "           id.toLowerCase().indexOf('comment') !== -1 || " +
                        "           className.toLowerCase().indexOf('related') !== -1 || " +
                        "           id.toLowerCase().indexOf('related') !== -1) && " +
                        "          !el.querySelector('video') && !el.querySelector('ytd-player')) { " +
                        "        el.style.display = 'none !important'; " +
                        "        el.style.visibility = 'hidden !important'; " +
                        "        el.style.height = '0 !important'; " +
                        "      } " +
                        "    }); " +
                        "  } catch(e) {} " +
                        "} " +
                        "hideCommentsAndRelated(); " +
                        "var player = document.querySelector('video'); " +
                        "if (player) { " +
                        "  player.setAttribute('controls', 'true'); " +
                        "  player.controls = true; " +
                        "  player.style.width = '100%'; " +
                        "  player.style.maxWidth = '100%'; " +
                        "} " +
                        "window.scrollTo(0, 0); " +
                        "})();", null);
                }, 1000);
                
                view.postDelayed(() -> {
                    view.evaluateJavascript(
                        "(function() { " +
                        "function finalCleanup() { " +
                        "  // Hide all comment and related elements " +
                        "  var allElements = document.querySelectorAll('*'); " +
                        "  allElements.forEach(function(el) { " +
                        "    if (el.tagName !== 'VIDEO' && el.tagName !== 'BODY' && el.tagName !== 'HTML' && " +
                        "        el.tagName !== 'SCRIPT' && el.tagName !== 'STYLE') { " +
                        "      var hasVideo = el.querySelector('video') || el.querySelector('ytd-player'); " +
                        "      var className = (el.className && el.className.toString) ? el.className.toString() : ''; " +
                        "      var id = el.id || ''; " +
                        "      var isCommentRelated = className.toLowerCase().indexOf('comment') !== -1 || " +
                        "                            id.toLowerCase().indexOf('comment') !== -1 || " +
                        "                            className.toLowerCase().indexOf('related') !== -1 || " +
                        "                            id.toLowerCase().indexOf('related') !== -1; " +
                        "      if (!hasVideo && (isCommentRelated || " +
                        "          (el.id !== 'player' && el.id !== 'player-container' && " +
                        "           !el.closest('ytd-player') && !el.closest('#player')))) { " +
                        "        var rect = el.getBoundingClientRect(); " +
                        "        if (isCommentRelated || rect.top > 400) { " +
                        "          el.style.display = 'none !important'; " +
                        "          el.style.visibility = 'hidden !important'; " +
                        "          el.style.height = '0 !important'; " +
                        "        } " +
                        "      } " +
                        "    } " +
                        "  }); " +
                        "} " +
                        "finalCleanup(); " +
                        "var player = document.querySelector('video'); " +
                        "if (player && !player.controls) { " +
                        "  player.setAttribute('controls', 'true'); " +
                        "  player.controls = true; " +
                        "} " +
                        "window.scrollTo(0, 0); " +
                        "})();", null);
                }, 2000);
                
                // Additional cleanup after 3 seconds to catch late-loading comments
                view.postDelayed(() -> {
                    view.evaluateJavascript(
                        "(function() { " +
                        "var commentElements = document.querySelectorAll('[class*=\"comment\"], [id*=\"comment\"], [class*=\"related\"], [id*=\"related\"], ytd-comments, ytd-comment-thread-renderer, ytd-watch-next-secondary-results-renderer'); " +
                        "commentElements.forEach(function(el) { " +
                        "  if (el && !el.querySelector('video') && !el.querySelector('ytd-player')) { " +
                        "    el.style.display = 'none !important'; " +
                        "    el.style.visibility = 'hidden !important'; " +
                        "    el.style.height = '0 !important'; " +
                        "    el.style.opacity = '0'; " +
                        "  } " +
                        "}); " +
                        "window.scrollTo(0, 0); " +
                        "})();", null);
                }, 3000);
            }
            
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                // Only show fallback if it's a critical error (not just resource loading errors)
                if (errorCode == android.webkit.WebViewClient.ERROR_HOST_LOOKUP || 
                    errorCode == android.webkit.WebViewClient.ERROR_CONNECT) {
                    // Network error - show fallback
                    showVideoErrorFallback(videoContainer, webView, originalUrl);
                }
                // Other errors (like missing resources) are usually fine - page can still load
            }
            
            @Override
            public void onReceivedHttpError(WebView view, android.webkit.WebResourceRequest request, android.webkit.WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                // Only show fallback for critical HTTP errors (404, 403, etc on main page)
                if (request.isForMainFrame() && errorResponse.getStatusCode() >= 400) {
                    showVideoErrorFallback(videoContainer, webView, originalUrl);
                }
            }
        });
        
        // Try loading YouTube mobile page directly - this often works better than iframe embeds
        // Mobile page allows playback without embedding restrictions
        String mobileUrl = "https://m.youtube.com/watch?v=" + videoId;
        
        // Load the mobile YouTube page directly
        webView.loadUrl(mobileUrl);
        
        // Make WebView fill the container width with proper height
        // Use a smaller height to only show video player (hide comments/related below)
        int webViewHeight = (int) (getResources().getDisplayMetrics().widthPixels * 0.5625); // 16:9 aspect ratio based on width
        
        LinearLayout.LayoutParams webViewParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            webViewHeight
        );
        webViewParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        webView.setLayoutParams(webViewParams);
        
        // Enable hardware acceleration for better video playback
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        
        videoContainer.addView(webView);
        
        // Set a tag to track if this WebView failed
        webView.setTag("youtube_player");
    }
    
    private void showVideoErrorFallback(LinearLayout videoContainer, WebView webView, String videoUrl) {
        // Remove WebView if it exists
        if (webView != null && webView.getParent() != null) {
            videoContainer.removeView(webView);
        }
        
        TextView errorView = new TextView(this);
        errorView.setText("Video cannot be embedded. Click button below to watch on YouTube.");
        errorView.setTextSize(14);
        errorView.setTextColor(getResources().getColor(android.R.color.white));
        errorView.setPadding(25, 20, 25, 10);
        errorView.setGravity(android.view.Gravity.CENTER);
        videoContainer.addView(errorView);
        
        AppCompatButton openButton = new AppCompatButton(this);
        openButton.setText("Watch on YouTube");
        openButton.setTextSize(16);
        openButton.setTextColor(getResources().getColor(android.R.color.white));
        openButton.setBackgroundResource(R.drawable.btn_long_condition_green);
        openButton.setPadding(0, 15, 0, 15);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(25, 10, 25, 0);
        openButton.setLayoutParams(buttonParams);
        openButton.setOnClickListener(v -> {
            playSound("click.mp3");
            openYouTubeVideo(videoUrl);
        });
        videoContainer.addView(openButton);
    }
    
    private String convertToEmbedUrl(String youtubeUrl) {
        if (youtubeUrl == null) return null;
        
        String videoId = null;
        
        // Handle different YouTube URL formats
        if (youtubeUrl.contains("youtu.be/")) {
            // Format: https://youtu.be/VIDEO_ID or https://youtu.be/VIDEO_ID?feature=shared
            int start = youtubeUrl.indexOf("youtu.be/") + 9;
            int end = youtubeUrl.indexOf("?", start);
            if (end == -1) end = youtubeUrl.length();
            videoId = youtubeUrl.substring(start, end);
        } else if (youtubeUrl.contains("youtube.com/shorts/")) {
            // Format: https://youtube.com/shorts/VIDEO_ID or https://youtube.com/shorts/VIDEO_ID?feature=shared
            int start = youtubeUrl.indexOf("shorts/") + 7;
            int end = youtubeUrl.indexOf("?", start);
            if (end == -1) end = youtubeUrl.length();
            videoId = youtubeUrl.substring(start, end);
            // YouTube Shorts can be embedded using the regular embed format
        } else if (youtubeUrl.contains("youtube.com/watch?v=")) {
            // Format: https://youtube.com/watch?v=VIDEO_ID
            int start = youtubeUrl.indexOf("v=") + 2;
            int end = youtubeUrl.indexOf("&", start);
            if (end == -1) end = youtubeUrl.length();
            videoId = youtubeUrl.substring(start, end);
        }
        
        // Clean video ID (remove any trailing characters that aren't part of the ID)
        if (videoId != null) {
            // Video IDs are typically 11 characters, but we'll take everything up to the first invalid character
            videoId = videoId.trim();
            // Remove any non-alphanumeric characters except dash and underscore (YouTube IDs can have these)
            videoId = videoId.replaceAll("[^a-zA-Z0-9_-]", "");
            
            if (!videoId.isEmpty()) {
                return "https://www.youtube.com/embed/" + videoId;
            }
        }
        
        return null; // Return null if conversion fails
    }
    
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    
    private String getLocalVideoFileName() {
        // Get the local video file name from raw folder (same logic as PDFViewerActivity)
        String tutLower = tutorialName.toLowerCase();
        
        if (tutLower.contains("addition") && !tutLower.contains("decimal")) {
            return "addition_video";
        } else if (tutLower.contains("multiplication") && !tutLower.contains("decimal")) {
            return "multiplication_video";
        } else if (tutLower.contains("division") && !tutLower.contains("decimal")) {
            return "division_video";
        } else if (tutLower.contains("subtraction") && !tutLower.contains("decimal")) {
            return "subtraction_video";
        } else if (tutLower.contains("percentage")) {
            return "percentage_video";
        }
        // Note: decimals are handled separately in setupUI()
        
        return null;
    }
    
    private void addLocalVideoPlayer(LinearLayout container, String videoFileName, String videoTitle) {
        // Create container for video
        LinearLayout videoContainer = new LinearLayout(this);
        videoContainer.setOrientation(LinearLayout.VERTICAL);
        videoContainer.setPadding(25, 10, 25, 30);
        videoContainer.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        
        // Add title
        TextView titleView = new TextView(this);
        titleView.setText(videoTitle);
        titleView.setTextSize(18);
        titleView.setTextColor(getResources().getColor(android.R.color.white));
        titleView.setPadding(0, 0, 0, 10);
        videoContainer.addView(titleView);
        
        // Get resource ID from raw folder
        int resourceId = getResources().getIdentifier(videoFileName, "raw", getPackageName());
        
        if (resourceId == 0) {
            // Video not found in raw folder
            TextView errorView = new TextView(this);
            errorView.setText("Local video file not found: " + videoFileName);
            errorView.setTextSize(14);
            errorView.setTextColor(getResources().getColor(android.R.color.white));
            errorView.setPadding(25, 20, 25, 20);
            videoContainer.addView(errorView);
            container.addView(videoContainer);
            return;
        }
        
        // Create VideoView for local video
        VideoView videoView = new VideoView(this);
        
        // Set video URI
        String videoPath = "android.resource://" + getPackageName() + "/" + resourceId;
        Uri videoUri = Uri.parse(videoPath);
        videoView.setVideoURI(videoUri);
        
        // Add media controller for play/pause controls
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        
        // Set VideoView dimensions - center it
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int videoWidth = (int) (screenWidth * 0.9); // 90% of screen width
        int videoHeight = (int) (videoWidth * 0.5625); // 16:9 aspect ratio
        
        LinearLayout.LayoutParams videoParams = new LinearLayout.LayoutParams(
            videoWidth,
            videoHeight
        );
        videoParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        videoView.setLayoutParams(videoParams);
        
        // Start video when prepared
        videoView.setOnPreparedListener(mp -> {
            // Video is ready, but don't auto-play - let user click play
            // Ensure music is paused when video is ready
            MusicManager.pause();
        });
        
        // Handle video playback state changes
        videoView.setOnInfoListener((mp, what, extra) -> {
            if (what == android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                // Video started playing - ensure music is paused
                MusicManager.pause();
            }
            return false;
        });
        
        // Handle video completion
        videoView.setOnCompletionListener(mp -> {
            Toast.makeText(this, "Video completed", Toast.LENGTH_SHORT).show();
            // Keep music paused while in video player
            MusicManager.pause();
            // Mark tutorial as completed when local video finishes (only for logged-in students)
            markTutorialCompletedIfNeeded();
        });
        
        videoContainer.addView(videoView);
        container.addView(videoContainer);
    }
    
    private List<String> getAdditionalVideoUrls() {
        List<String> urls = new ArrayList<>();
        String tutLower = tutorialName.toLowerCase();
        
        // For now, additional videos are the same as step-by-step
        // You can add more videos here later
        String mainUrl = getYouTubeVideoUrl();
        if (mainUrl != null) {
            urls.add(mainUrl);
        }
        
        // Add more additional videos based on tutorial type
        // Example: if you have multiple videos for the same topic
        // if (tutLower.contains("addition")) {
        //     urls.add("https://youtu.be/another_video_id");
        // }
        
        return urls;
    }
    
    private String getYouTubeVideoUrl() {
        String tutLower = tutorialName.toLowerCase();
        
        // Grade 1-2 videos
        if (gradeNum == 1) {
            if (tutLower.contains("addition")) return GRADE_1_ADDITION_VIDEO;
            if (tutLower.contains("subtraction")) return GRADE_1_SUBTRACTION_VIDEO;
        } else if (gradeNum == 2) {
            if (tutLower.contains("addition")) return GRADE_2_ADDITION_VIDEO;
            if (tutLower.contains("subtraction")) return GRADE_2_SUBTRACTION_VIDEO;
        }
        // Grade 3-4 videos
        else if (gradeNum == 3 || gradeNum == 4) {
            if (tutLower.contains("addition")) return GRADE_3_4_ADDITION_VIDEO;
            if (tutLower.contains("subtraction")) return GRADE_3_4_SUBTRACTION_VIDEO;
            if (tutLower.contains("multiplication")) return GRADE_3_4_MULTIPLICATION_VIDEO;
            if (tutLower.contains("division")) return GRADE_3_4_DIVISION_VIDEO;
        }
        // Grade 5-6 videos
        else if (gradeNum == 5 || gradeNum == 6) {
            if (tutLower.contains("addition")) return GRADE_5_6_ADDITION_VIDEO;
            if (tutLower.contains("subtraction")) return GRADE_5_6_SUBTRACTION_VIDEO;
            if (tutLower.contains("multiplication")) return GRADE_5_6_MULTIPLICATION_VIDEO;
            if (tutLower.contains("division")) return GRADE_5_6_DIVISION_VIDEO;
            if (tutLower.contains("decimal")) return GRADE_5_6_DECIMAL_VIDEO;
            if (tutLower.contains("percentage")) return GRADE_5_6_PERCENTAGE_VIDEO;
        }
        
        return null;
    }
    
    private int getGradeNumber(String grade) {
        try {
            // Handle null grade (guests) - default to grade 5 to allow all tutorials
            if (grade == null) {
                return 5; // Grade 5 allows all tutorials
            }
            
            if (grade.startsWith("grade_")) {
                String numStr = grade.replace("grade_", "")
                    .replace("one", "1").replace("two", "2")
                    .replace("three", "3").replace("four", "4")
                    .replace("five", "5").replace("six", "6");
                return Integer.parseInt(numStr);
            } else {
                return Integer.parseInt(grade);
            }
        } catch (NumberFormatException e) {
            return 5; // Default to grade 5 for guests to allow all tutorials
        }
    }
    
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    private void playSound(String fileName) {
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
            soundEffectPlayer.setOnCompletionListener(mp -> {
                mp.release();
                soundEffectPlayer = null;
            });
            soundEffectPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Keep music paused while video player is active
        MusicManager.pause();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Keep music paused while video player is active
        MusicManager.pause();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundEffectPlayer != null) {
            soundEffectPlayer.release();
        }
        // Mark tutorial as completed when student finishes viewing videos (only for logged-in students)
        // This ensures they've at least opened and viewed the tutorial videos
        markTutorialCompletedIfNeeded();
        // Resume app music when video player is closed
        MusicManager.resume();
    }
    
    // Mark tutorial as completed (only for logged-in students, not guests)
    private void markTutorialCompletedIfNeeded() {
        if (tutorialName != null && !hasMarkedCompleted) {
            // Only mark as completed for logged-in students (not guests)
            if (grade != null) {
                TutorialProgressTracker tracker = new TutorialProgressTracker(this);
                // Only mark if not already completed (avoid duplicate marking)
                if (!tracker.isTutorialCompleted(tutorialName)) {
                    tracker.markTutorialCompleted(tutorialName);
                    hasMarkedCompleted = true;
                }
            }
        }
    }
}

