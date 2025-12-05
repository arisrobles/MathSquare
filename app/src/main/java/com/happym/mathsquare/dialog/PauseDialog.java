package com.happym.mathsquare.dialog;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.happym.mathsquare.MainActivity;
import com.happym.mathsquare.MultipleChoicePage;

import com.happym.mathsquare.R;
import java.io.IOException;

public class PauseDialog extends DialogFragment {
    private boolean isPaused;
    private int currentQuestions;
    private PauseDialogListener listener;
private MediaPlayer soundEffectPlayer;
    public interface PauseDialogListener {
        void onRepeatGame(boolean shouldRepeat);
        void onResumeGame(boolean resumeGame);
    }

    // Use this factory method to create a new instance of the dialog
    public static PauseDialog newInstance(boolean isPaused, int currentQuestions) {
        PauseDialog dialog = new PauseDialog();
        dialog.isPaused = isPaused;
        dialog.currentQuestions = currentQuestions;
        return dialog;
    }
    
    public void setListener(PauseDialogListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setStatusBarColor(Color.TRANSPARENT);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Optional: To make the background transparent
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_pause, container, false);

        TextView textQuestion = view.findViewById(R.id.current_questions);
        Button btnResume = view.findViewById(R.id.btn_playgame);
        LinearLayout btnRepeatGame = view.findViewById(R.id.btn_Repeat);
        Button btnQuitsGame = view.findViewById(R.id.btn_exitgame);
        
        animateButtonFocus(btnResume);
        animateButtonFocus(btnRepeatGame);
        animateButtonFocus(btnQuitsGame);
        
        // Set the current questions text
        if (isPaused) {
            textQuestion.setText("Questions left: " + currentQuestions + "/10");
        }

        // Add custom logic for resume button
        btnResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    playSound("click.mp3");
                    animateButtonPushDowm(btnResume);  
                dismiss(); // Close dialog on resume
                    if (listener != null) {
                    listener.onResumeGame(true);
                }
            }
        });

        btnRepeatGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   playSound("click.mp3");
                    animateButtonPushDowm(btnRepeatGame);  
                dismiss(); // Close the dialog
                if (listener != null) {
                    listener.onRepeatGame(true); // Pass true back to the activity
                }
            }
        });

        btnQuitsGame.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // Create an AlertDialog to confirm the action
        new AlertDialog.Builder(getContext())
            .setTitle("Quit Game")
            .setMessage("Are you sure? You will lose all of your progress.")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // User confirmed, so dismiss the pause dialog
                    dismiss();
playSound("click.mp3");
                   animateButtonPushDowm(btnQuitsGame); 
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    // Finish the current activity if it is an instance of MultipleChoicePage
                    if (getActivity() instanceof MultipleChoicePage) {
                        getActivity().finish();
                    }
                }
            })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                               playSound("click.mp3");
                    // User canceled, dismiss the alert dialog only
                                animateButtonPushDowm(btnQuitsGame); 
                    dialog.dismiss();
                }
            })
            .show();
    }
});


        return view;
    }
    private void playSound(String fileName) {
        // Stop any previous sound effect before playing a new one
        if (soundEffectPlayer != null) {
            soundEffectPlayer.release();
            soundEffectPlayer = null;
        }

        try {
            AssetFileDescriptor afd = getContext().getAssets().openFd(fileName);
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
      
}

