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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.happym.mathsquare.MainActivity;
import com.happym.mathsquare.MultipleChoicePage;

import com.happym.mathsquare.R;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import com.happym.mathsquare.sharedPreferences;
import java.util.Map;
import java.util.UUID;
public class CreateSection extends DialogFragment {
    private MediaPlayer bgMediaPlayer;
    private MediaPlayer soundEffectPlayer;
    private List<String> sectionList = new ArrayList<>();
    private boolean isUpdated;
    private SectionDialogListener listener;
    
    public interface SectionDialogListener {
        void onUpdateSection(boolean shouldRepeat);
    }
    
    public static CreateSection newInstance(boolean isUpdated) {
        CreateSection dialog = new CreateSection();
        dialog.isUpdated = isUpdated;
        return dialog;
    }
    
    public void setListener(SectionDialogListener listener) {
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
        // Set background to match the layout background color (yellowbg)
        dialog.getWindow().setBackgroundDrawableResource(R.color.yellowbg);
        // Ensure window adjusts for keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_createsection, container, false);

        
                // Firestore instance
FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        ScrollView scrollView = view.findViewById(R.id.scrollView);
        TextInputLayout sectionLayout = view.findViewById(R.id.email_address_layout);
        Spinner numberDropdownPicker = view.findViewById(R.id.numberDropdownPicker);
        TextView spinnerError = view.findViewById(R.id.spinnerError);
        AppCompatButton btbSubmit = view.findViewById(R.id.btn_submit);
        
        // Setup keyboard listener to scroll to focused field
        if (scrollView != null) {
            setupKeyboardListener(view, scrollView);
        }
        
        
        animateButtonFocus(btbSubmit);
        
        List<String> grades = Arrays.asList("Select your grade", "1", "2", "3", "4", "5", "6");
ArrayAdapter<String> adapterGrades = new ArrayAdapter<>(getContext(), R.layout.spinner_item, grades);
adapterGrades.setDropDownViewResource(R.layout.spinner_item);
numberDropdownPicker.setAdapter(adapterGrades);
        

        TextInputEditText sectionEditText = (TextInputEditText) sectionLayout.getEditText();
        if (sectionEditText != null) {
            sectionEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    sectionLayout.setError(null);
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            });
            
            // Add focus listener to scroll when keyboard appears
            if (scrollView != null) {
                sectionEditText.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) {
                        scrollView.postDelayed(() -> scrollToView(scrollView, v), 300);
                    }
                });
            }
        }
        
        // Add custom logic for resume button
        btbSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    playSound("click.mp3");
                    animateButtonPushDowm(btbSubmit);  
                    
                    boolean hasError = false;
    sectionLayout.setError(null);
    spinnerError.setVisibility(View.GONE);
                    
                    String sections = ((TextInputEditText) sectionLayout.getEditText()).getText().toString().trim();
    if (TextUtils.isEmpty(sections)) {
        sectionLayout.setError("Section is required");
        hasError = true;
    }
                    
                    
                    
                    int gradePosition = numberDropdownPicker.getSelectedItemPosition();
    String selectedGrade = (gradePosition > 0) ? numberDropdownPicker.getSelectedItem().toString() : null;
    if (selectedGrade == null) {
        spinnerError.setText("Please select a grade");
        spinnerError.setVisibility(View.VISIBLE);
        hasError = true;
    }
                        
      if (!hasError) {
    String uuid = UUID.randomUUID().toString(); // Generate a random UUID

    // Create a map with only the required fields
    HashMap<String, Object> sectionData = new HashMap<>();
    sectionData.put("Section", sections); // Field for the Section name
    sectionData.put("Grade_Number", Integer.parseInt(selectedGrade)); // Field for the Grade number
    sectionData.put("timestamp", FieldValue.serverTimestamp()); // Field for the server timestamp

    // Save the document in the "Sections" collection with the random UUID as its document ID
    db.collection("Sections")
        .document(uuid)
        .set(sectionData)
        .addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Section added successfully!", Toast.LENGTH_SHORT).show();
            dismiss();
        })
        .addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error adding section: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

                    
                
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

    /**
     * Setup keyboard listener to scroll to focused field when keyboard appears
     */
    private void setupKeyboardListener(View rootView, ScrollView scrollView) {
        if (scrollView == null || rootView == null) return;

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Get the visible height of the root view
                int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
                
                // If height difference is more than 200dp, keyboard is likely visible
                if (heightDiff > 200) {
                    // Find the currently focused view
                    View focusedView = getDialog() != null ? getDialog().getCurrentFocus() : null;
                    if (focusedView != null && scrollView != null) {
                        // Scroll to the focused view
                        scrollView.post(new Runnable() {
                            @Override
                            public void run() {
                                scrollToView(scrollView, focusedView);
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * Scroll ScrollView to show the specified view
     */
    private void scrollToView(ScrollView scrollView, View view) {
        if (scrollView == null || view == null) return;

        int[] location = new int[2];
        view.getLocationInWindow(location);
        
        // Get the location relative to ScrollView
        scrollView.getLocationInWindow(location);
        int[] viewLocation = new int[2];
        view.getLocationInWindow(viewLocation);
        
        // Calculate the scroll position
        int scrollY = viewLocation[1] - location[1] - 100; // 100dp padding from top
        
        if (scrollY > 0) {
            scrollView.smoothScrollTo(0, scrollY);
        }
    }
    
}


