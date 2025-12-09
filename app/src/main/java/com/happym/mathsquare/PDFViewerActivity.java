package com.happym.mathsquare;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.MediaController;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import com.happym.mathsquare.utils.TutorialProgressTracker;

public class PDFViewerActivity extends AppCompatActivity {

    private ImageView pdfImageView;
    private AppCompatButton btnNext, btnPrevious, btnFullScreen, btnClose;
    private TextView pageInfo;
    private LinearLayout controlPanel;
    private int currentPage = 0;
    private int totalPages = 0;
    private boolean isFullScreen = false;
    private String pdfFileName;

    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPdfPage;
    private ParcelFileDescriptor parcelFileDescriptor;

    private boolean controlsVisible = true;
    private Runnable hideControlsRunnable;

    private MediaPlayer mediaPlayer;

    private VideoView videoView;
    private boolean isVideoShowing = false;
    private boolean showVideoAfter = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.layout_pdf_viewer);

        // Get PDF file name from intent
        pdfFileName = getIntent().getStringExtra("PDF_FILE");
        String tutorialName = getIntent().getStringExtra("TUTORIAL_NAME");
        showVideoAfter = getIntent().getBooleanExtra("SHOW_VIDEO_AFTER", true); // Default true for backwards compatibility
        if (pdfFileName == null) {
            pdfFileName = "addition.pdf";
        }
        
        // Mark tutorial as completed when viewer is opened
        if (tutorialName != null) {
            TutorialProgressTracker tracker = new TutorialProgressTracker(this);
            tracker.markTutorialCompleted(tutorialName);
        }

        // Initialize views
        pdfImageView = findViewById(R.id.pdfView);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnFullScreen = findViewById(R.id.btnFullScreen);
        btnClose = findViewById(R.id.btnClose);
        pageInfo = findViewById(R.id.pageInfo);
        controlPanel = findViewById(R.id.controlPanel);
        videoView = findViewById(R.id.videoView_tutorial);

        // Load PDF
        try {
            openPdfRenderer();
            showPage(0);
        } catch (IOException e) {
            Toast.makeText(this, "Error loading PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        // Set button listeners
        btnNext.setOnClickListener(v -> nextPage());
        btnPrevious.setOnClickListener(v -> previousPage());
        btnFullScreen.setOnClickListener(v -> toggleFullScreen());
        btnClose.setOnClickListener(v -> finish());
        
        // Register back button handler for PDF/Video navigation
        androidx.activity.OnBackPressedCallback callback = new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isVideoShowing) {
                    // If video is showing, go back to last PDF page
                    hideVideo();
                    showPage(totalPages - 1);
                } else if (isFullScreen) {
                    toggleFullScreen();
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        // Add touch listener to PDF view to toggle controls in landscape
        pdfImageView.setOnClickListener(v -> {
            if (isFullScreen) {
                toggleControls();
            }
        });

        // Initialize auto-hide runnable
        hideControlsRunnable = this::hideControls;

        // Pause global music when opening PDF
        MusicManager.pause();

        // Start PDF viewer background music
        startBackgroundMusic();
    }

    private void openPdfRenderer() throws IOException {
        // Get resource ID from raw folder
        String fileName = pdfFileName.replace(".pdf", "");
        int resourceId = getResources().getIdentifier(fileName, "raw", getPackageName());

        if (resourceId == 0) {
            throw new IOException("PDF file not found in raw folder");
        }

        // Copy PDF from resources to cache directory
        File file = new File(getCacheDir(), pdfFileName);
        if (!file.exists()) {
            InputStream asset = getResources().openRawResource(resourceId);
            FileOutputStream output = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();
        }

        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        pdfRenderer = new PdfRenderer(parcelFileDescriptor);
        totalPages = pdfRenderer.getPageCount();
    }

    private void showPage(int index) {
        if (pdfRenderer == null || index < 0 || index >= pdfRenderer.getPageCount()) {
            return;
        }

        // Close previous page if open
        if (currentPdfPage != null) {
            currentPdfPage.close();
        }

        // Open the page
        currentPdfPage = pdfRenderer.openPage(index);

        // Create bitmap for rendering
        Bitmap bitmap = Bitmap.createBitmap(
            currentPdfPage.getWidth() * 2,
            currentPdfPage.getHeight() * 2,
            Bitmap.Config.ARGB_8888
        );

        // Render PDF page to bitmap
        currentPdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        // Display bitmap
        pdfImageView.setImageBitmap(bitmap);

        // Update page info
        currentPage = index;
        pageInfo.setText("Page " + (index + 1) + " / " + totalPages);
    }

    private void nextPage() {
        if (currentPage < totalPages - 1) {
            showPage(currentPage + 1);
            // Reset auto-hide timer when navigating
            if (isFullScreen && controlsVisible) {
                scheduleHideControls();
            }
        } else {
            // Last page reached - show video only if showVideoAfter is true
            if (showVideoAfter && pdfFileName != null && hasVideo()) {
                showTutorialVideo();
            } else {
                Toast.makeText(this, "Last page reached", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void previousPage() {
        if (isVideoShowing) {
            // If video is showing, go back to last PDF page
            hideVideo();
            showPage(totalPages - 1);
        } else if (currentPage > 0) {
            showPage(currentPage - 1);
            // Reset auto-hide timer when navigating
            if (isFullScreen && controlsVisible) {
                scheduleHideControls();
            }
        } else {
            Toast.makeText(this, "First page reached", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleFullScreen() {
        isFullScreen = !isFullScreen;

        if (isFullScreen) {
            // Enter full screen - switch to landscape
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            hideSystemUI();

            // Post with delay to ensure orientation change completes
            pdfImageView.postDelayed(() -> {
                // Show controls initially, then auto-hide after 3 seconds
                showControls();
                scheduleHideControls();
            }, 300);
        } else {
            // Exit full screen - switch back to portrait
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            showSystemUI();

            // Post with delay to ensure orientation change completes
            pdfImageView.postDelayed(() -> {
                // Show all controls in portrait mode
                controlPanel.setVisibility(View.VISIBLE);
                btnFullScreen.setVisibility(View.VISIBLE);
                btnClose.setVisibility(View.VISIBLE);
                controlsVisible = true;
                // Cancel auto-hide
                pdfImageView.removeCallbacks(hideControlsRunnable);
            }, 300);
        }
    }

    private void toggleControls() {
        if (controlsVisible) {
            hideControls();
        } else {
            showControls();
            scheduleHideControls();
        }
    }

    private void showControls() {
        controlPanel.setVisibility(View.VISIBLE);
        btnFullScreen.setVisibility(View.VISIBLE);
        btnClose.setVisibility(View.VISIBLE);
        controlsVisible = true;
    }

    private void hideControls() {
        if (isFullScreen) {
            controlPanel.setVisibility(View.GONE);
            btnFullScreen.setVisibility(View.GONE);
            btnClose.setVisibility(View.GONE);
            controlsVisible = false;
        }
    }

    private void scheduleHideControls() {
        // Remove any pending hide callbacks
        pdfImageView.removeCallbacks(hideControlsRunnable);
        // Schedule hide after 3 seconds
        if (isFullScreen) {
            pdfImageView.postDelayed(hideControlsRunnable, 3000);
        }
    }

    private void hideSystemUI() {
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            windowInsetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void showSystemUI() {
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars());
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private boolean hasVideo() {
        if (pdfFileName == null) return false;

        String lowerFileName = pdfFileName.toLowerCase();

        // Check if this PDF has a corresponding video
        return lowerFileName.contains("addition") ||
               lowerFileName.contains("multiplication") ||
               lowerFileName.contains("division") ||
               lowerFileName.contains("subtraction") ||
               lowerFileName.contains("percentage") ||
               lowerFileName.contains("decimals");
    }

    private void showTutorialVideo() {
        String videoFileName = null;

        // Determine which video to show based on the PDF file name
        String lowerFileName = pdfFileName.toLowerCase();

        if (lowerFileName.contains("addition") && !lowerFileName.contains("decimal")) {
            videoFileName = "addition_video";
        } else if (lowerFileName.contains("multiplication") && !lowerFileName.contains("decimal")) {
            videoFileName = "multiplication_video";
        } else if (lowerFileName.contains("division") && !lowerFileName.contains("decimal")) {
            videoFileName = "division_video";
        } else if (lowerFileName.contains("subtraction") && !lowerFileName.contains("decimal")) {
            videoFileName = "subtraction_video";
        } else if (lowerFileName.contains("percentage")) {
            videoFileName = "percentage_video";
        } else if (lowerFileName.contains("decimals")) {
            // For decimals, show all three videos in sequence
            showDecimalVideos();
            return;
        }

        if (videoFileName != null) {
            showSingleVideo(videoFileName);
        } else {
            Toast.makeText(this, "Tutorial video not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSingleVideo(String videoFileName) {
        // Get resource ID from raw folder
        int resourceId = getResources().getIdentifier(videoFileName, "raw", getPackageName());

        if (resourceId != 0) {
            // Hide PDF view
            pdfImageView.setVisibility(View.GONE);

            // Show video view
            videoView.setVisibility(View.VISIBLE);
            isVideoShowing = true;

            // Set video URI
            String videoPath = "android.resource://" + getPackageName() + "/" + resourceId;
            Uri videoUri = Uri.parse(videoPath);
            videoView.setVideoURI(videoUri);

            // Add media controller for play/pause controls
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);

            // Start video automatically
            videoView.setOnPreparedListener(mp -> {
                // Pause app music when video starts
                MusicManager.pause();
                videoView.start();
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
                // Keep music paused while video is showing
                MusicManager.pause();
            });

            // Update page info
            pageInfo.setText("Tutorial Video");
            btnNext.setEnabled(false);
        } else {
            Toast.makeText(this, "Tutorial video not found", Toast.LENGTH_SHORT).show();
        }
    }

    private int currentDecimalVideoIndex = 0;
    private String[] decimalVideos = {"decimal_addition", "decimal_multiplication", "decimal_subtraction"};

    private void showDecimalVideos() {
        currentDecimalVideoIndex = 0;
        showDecimalVideo(currentDecimalVideoIndex);
    }

    private void showDecimalVideo(int index) {
        if (index >= decimalVideos.length) {
            Toast.makeText(this, "All decimal videos completed", Toast.LENGTH_SHORT).show();
            return;
        }

        String videoFileName = decimalVideos[index];
        int resourceId = getResources().getIdentifier(videoFileName, "raw", getPackageName());

        if (resourceId != 0) {
            // Hide PDF view
            pdfImageView.setVisibility(View.GONE);

            // Show video view
            videoView.setVisibility(View.VISIBLE);
            isVideoShowing = true;

            // Set video URI
            String videoPath = "android.resource://" + getPackageName() + "/" + resourceId;
            Uri videoUri = Uri.parse(videoPath);
            videoView.setVideoURI(videoUri);

            // Add media controller for play/pause controls
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);

            // Start video automatically
            videoView.setOnPreparedListener(mp -> {
                // Pause app music when video starts
                MusicManager.pause();
                videoView.start();
            });
            
            // Handle video playback state changes
            videoView.setOnInfoListener((mp, what, extra) -> {
                if (what == android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    // Video started playing - ensure music is paused
                    MusicManager.pause();
                }
                return false;
            });

            // Handle video completion - play next decimal video
            videoView.setOnCompletionListener(mp -> {
                // Keep music paused
                MusicManager.pause();
                currentDecimalVideoIndex++;
                if (currentDecimalVideoIndex < decimalVideos.length) {
                    Toast.makeText(this, "Playing next decimal video...", Toast.LENGTH_SHORT).show();
                    // Play next video after a short delay
                    videoView.postDelayed(() -> showDecimalVideo(currentDecimalVideoIndex), 1000);
                } else {
                    Toast.makeText(this, "All decimal videos completed", Toast.LENGTH_SHORT).show();
                }
            });

            // Update page info
            String videoTitle = "";
            switch (index) {
                case 0:
                    videoTitle = "Decimal Addition Video";
                    break;
                case 1:
                    videoTitle = "Decimal Multiplication Video";
                    break;
                case 2:
                    videoTitle = "Decimal Subtraction Video";
                    break;
            }
            pageInfo.setText(videoTitle);
            btnNext.setEnabled(false);
        } else {
            Toast.makeText(this, "Decimal video not found: " + videoFileName, Toast.LENGTH_SHORT).show();
        }
    }

    private void hideVideo() {
        if (videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        videoView.setVisibility(View.GONE);
        pdfImageView.setVisibility(View.VISIBLE);
        isVideoShowing = false;
        btnNext.setEnabled(true);
        currentDecimalVideoIndex = 0; // Reset decimal video index
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Handle orientation change
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape mode
            if (isFullScreen) {
                // Apply fullscreen settings after layout is ready
                pdfImageView.postDelayed(() -> {
                    hideSystemUI();
                    showControls();
                    scheduleHideControls();
                }, 100);
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // In portrait mode
            if (!isFullScreen) {
                // Make sure controls are visible in portrait
                pdfImageView.postDelayed(() -> {
                    showSystemUI();
                    controlPanel.setVisibility(View.VISIBLE);
                    btnFullScreen.setVisibility(View.VISIBLE);
                    btnClose.setVisibility(View.VISIBLE);
                    controlsVisible = true;
                    pdfImageView.removeCallbacks(hideControlsRunnable);
                }, 100);
            }
        }
    }

    private void startBackgroundMusic() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.tutorial);
                if (mediaPlayer != null) {
                    mediaPlayer.setLooping(true); // Loop the music
                    mediaPlayer.setVolume(0.5f, 0.5f); // Set volume to 50%
                    mediaPlayer.start();
                }
            } else if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pauseBackgroundMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private void stopBackgroundMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicManager.pause();
        startBackgroundMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseBackgroundMusic();

        // Pause video if playing
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop background music
        stopBackgroundMusic();

        // Stop video if playing
        if (videoView != null && videoView.isPlaying()) {
            videoView.stopPlayback();
        }

        // Close PDF renderer
        try {
            if (currentPdfPage != null) {
                currentPdfPage.close();
            }
            if (pdfRenderer != null) {
                pdfRenderer.close();
            }
            if (parcelFileDescriptor != null) {
                parcelFileDescriptor.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Resume global music
        MusicManager.resume();
    }

    @Override
    @Deprecated
    public void onBackPressed() {
        if (isVideoShowing) {
            // If video is showing, go back to last PDF page
            hideVideo();
            showPage(totalPages - 1);
        } else if (isFullScreen) {
            toggleFullScreen();
        } else {
            super.onBackPressed();
        }
    }
}

