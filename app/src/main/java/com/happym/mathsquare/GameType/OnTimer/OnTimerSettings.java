package com.happym.mathsquare.GameType.OnTimer;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.happym.mathsquare.Animation.NumBGAnimation;
import com.happym.mathsquare.Animation.VignetteEffect;
import com.happym.mathsquare.MultipleChoicePage;
import com.happym.mathsquare.R;

import java.io.IOException;

public class OnTimerSettings extends AppCompatActivity {
    private MediaPlayer soundEffectPlayer;
    private int timerValue = 1;
    private TextView timerValueText;
    private FrameLayout numberContainer, backgroundFrame;
    private NumBGAnimation numBGAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ontimer_settings);

        String operation = getIntent().getStringExtra("operation");
        String difficulty = getIntent().getStringExtra("difficulty");

        // Initialize views
        ImageButton btnMinus = findViewById(R.id.btn_minus);
        ImageButton btnPlus = findViewById(R.id.btn_plus);
        timerValueText = findViewById(R.id.timer_value);
        FrameLayout btnStart = findViewById(R.id.btn_start);

        backgroundFrame = findViewById(R.id.main);
        numberContainer = findViewById(R.id.number_container);

        // Set up number background animation
        numBGAnimation = new NumBGAnimation(this, numberContainer);
        numBGAnimation.startNumberAnimationLoop();

        // Apply vignette effect
        backgroundFrame.post(() -> {
            VignetteEffect.apply(this, backgroundFrame);
        });

        // Set up click listeners
        btnMinus.setOnClickListener(v -> {
            if (timerValue > 1) {
                timerValue--;
                updateTimerDisplay();
                playSound("click.mp3");
            }
        });

        btnPlus.setOnClickListener(v -> {
            if (timerValue < 3) {
                timerValue++;
                updateTimerDisplay();
                playSound("click.mp3");
            }
        });

        btnStart.setOnClickListener(v -> {
            playSound("click.mp3");
            Intent intent = new Intent(OnTimerSettings.this, MultipleChoicePage.class);
            intent.putExtra("operation", operation);
            // For percentage and decimal operations, pass the actual grade level
            if ("Percentage".equals(operation) || "Decimal".equals(operation) || 
                "DecimalAddition".equals(operation) || "DecimalSubtraction".equals(operation) || 
                "DecimalMultiplication".equals(operation) || "DecimalDivision".equals(operation)) {
                intent.putExtra("difficulty", difficulty);
            } else {
                intent.putExtra("difficulty", difficulty);
            }
            intent.putExtra("game_type", "OnTimer");
            intent.putExtra("heartLimit", 3);
            intent.putExtra("timerLimit", timerValue);
            intent.putExtra("questionLimit", 20);
            startActivity(intent);
        });
    }

    private void updateTimerDisplay() {
        timerValueText.setText(String.valueOf(timerValue));
    }

    private void playSound(String soundFileName) {
        try {
            if (soundEffectPlayer != null) {
                soundEffectPlayer.release();
            }
            AssetFileDescriptor afd = getAssets().openFd(soundFileName);
            soundEffectPlayer = new MediaPlayer();
            soundEffectPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            soundEffectPlayer.prepare();
            soundEffectPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
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