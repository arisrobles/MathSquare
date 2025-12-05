package com.happym.mathsquare;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputFilter;
import android.view.ViewGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import com.happym.mathsquare.MainActivity;

import java.util.Map;
import java.util.UUID;
import com.happym.mathsquare.sharedPreferences;
import androidx.core.view.WindowCompat;
public class studentSignUp extends AppCompatActivity {

    private String selectedSectionId;
    private String selectedSectionName;
    private String selectedGradeLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.layoutstudent_sign_up);

        // Firestore instance
FirebaseFirestore db = FirebaseFirestore.getInstance();

        AutoCompleteTextView sectionChooser = findViewById(R.id.SectionChooser);
        TextInputLayout firstNameLayout = findViewById(R.id.first_name_layout);
        TextInputLayout lastNameLayout = findViewById(R.id.last_name_layout);
        AppCompatButton submitButton = findViewById(R.id.btn_submit);
        AutoCompleteTextView numberDropdownPicker = findViewById(R.id.numberDropdownPicker);
        TextView spinnerError = findViewById(R.id.spinnerError);
        TextInputEditText studentNumberInput = findViewById(R.id.student_number_input);
        TextView studentNumberError = findViewById(R.id.studentNumberError);
        TextInputLayout passwordLayout = findViewById(R.id.password_layout);
        TextInputEditText passwordInput = (TextInputEditText) passwordLayout.getEditText();

TextInputEditText firstNameEditText = (TextInputEditText) firstNameLayout.getEditText();

InputFilter noSpacesFilter = (source, start, end, dest, dstart, dend) -> {
    if (source.toString().contains(" ")) {
        return "";
    }
    return source;
};

if (firstNameEditText != null) {
    firstNameEditText.setFilters(new InputFilter[]{noSpacesFilter});
}

       TextInputEditText lastNameEditText = (TextInputEditText) lastNameLayout.getEditText();

if (lastNameEditText != null) {
    lastNameEditText.setFilters(new InputFilter[]{noSpacesFilter});
}

List<String> grades = Arrays.asList("1", "2", "3", "4", "5", "6");

ArrayAdapter<String> adapterGrades = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, grades) {

    // Override the method for the spinner’s closed view
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        // Set the padding (using pixel values here)
        int padding = dpToPx(16); // Convert 16dp to pixels
        view.setPadding(padding, padding, padding, padding);
        return view;
    }

    // Override the method for the drop-down view
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        int padding = dpToPx(16); // Convert 16dp to pixels
        view.setPadding(padding, padding, padding, padding);
        return view;
    }

    // Helper method to convert dp to pixels
    private int dpToPx(int dp) {
        return Math.round(dp * getContext().getResources().getDisplayMetrics().density);
    }
};

adapterGrades.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
numberDropdownPicker.setAdapter(adapterGrades);


        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {


        } else {


        }

numberDropdownPicker.setOnItemClickListener((adapterView, view, position, id) -> {
    String selectedGrade = adapterGrades.getItem(position);

    if (selectedGrade.matches("[1-6]")) {
        int gradeNumber = Integer.parseInt(selectedGrade);

        db.collection("Sections")
            .whereEqualTo("Grade_Number", gradeNumber)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<String> sectionNames = new ArrayList<>();
                Map<String, String> sectionIdMap = new HashMap<>();

                for (QueryDocumentSnapshot doc : querySnapshot) {
                    String section = doc.getString("Section");
                    if (section != null) {
                        sectionNames.add(section);
                        sectionIdMap.put(section, doc.getId());
                    }
                }

                if (!sectionNames.isEmpty()) {
                    ArrayAdapter<String> sectionAdapter = new ArrayAdapter<>(studentSignUp.this, android.R.layout.simple_dropdown_item_1line, sectionNames);
                    sectionChooser.setAdapter(sectionAdapter);

                    sectionChooser.setOnItemClickListener((adapterView1, view1, i, l) -> {
                        String selectedSection = sectionAdapter.getItem(i);
                        String documentId = sectionIdMap.get(selectedSection);

                        selectedSectionId = documentId;
                        selectedSectionName = selectedSection != null ? selectedSection.trim() : null; // Trim section name for consistency
                        selectedGradeLevel = selectedGrade;
                    });
                } else {
                    Toast.makeText(studentSignUp.this, "No sections found for grade " + selectedGrade, Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> Toast.makeText(studentSignUp.this, "Error fetching sections: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
});



// Set up SectionChooser to show dropdown when clicked
sectionChooser.setOnClickListener(v -> sectionChooser.showDropDown());


        // Clear errors on text change for firstName and lastName
        ((TextInputEditText) firstNameLayout.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                firstNameLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        ((TextInputEditText) lastNameLayout.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                lastNameLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        animateButtonFocus(submitButton);



        submitButton.setOnClickListener(v -> {
            boolean hasError = false;

            // Reset errors and hide spinner error initially
            firstNameLayout.setError(null);
            lastNameLayout.setError(null);
            sectionChooser.setError(null);
            passwordLayout.setError(null);
            spinnerError.setVisibility(View.GONE);
            studentNumberError.setVisibility(View.GONE);

            // Validate First Name
            String firstName = ((TextInputEditText) firstNameLayout.getEditText()).getText().toString().trim();
            if (TextUtils.isEmpty(firstName)) {
                firstNameLayout.setError("Student First Name is required");
                animateShakeRotateEditTextErrorAnimation(firstNameLayout);
                hasError = true;
            }

            // Validate Last Name
            String lastName = ((TextInputEditText) lastNameLayout.getEditText()).getText().toString().trim();
            if (TextUtils.isEmpty(lastName)) {
                lastNameLayout.setError("Student Last Name is required");
                animateShakeRotateEditTextErrorAnimation(lastNameLayout);
                hasError = true;
            }

            // Validate Section
            String section = sectionChooser.getText().toString().trim();
            if (TextUtils.isEmpty(section)) {
                sectionChooser.setError("Student Section is required");
                animateShakeRotateEditTextErrorAnimation(sectionChooser);
                hasError = true;
            }

            // Validate Spinner selection (Grade)
            String grade = numberDropdownPicker.getText().toString().trim();
if (TextUtils.isEmpty(grade) || !grade.matches("[1-6]")) {
    spinnerError.setText("Please select your grade");
    spinnerError.setVisibility(View.VISIBLE);
    hasError = true;
}

            // Validate Student Number
            String studentNumber = studentNumberInput != null ? 
                studentNumberInput.getText().toString().trim() : "";
            if (TextUtils.isEmpty(studentNumber)) {
                studentNumberError.setText("Student number is required");
                studentNumberError.setVisibility(View.VISIBLE);
                hasError = true;
            }

            // Validate Password
            String password = passwordInput != null ? 
                passwordInput.getText().toString() : "";
            if (TextUtils.isEmpty(password)) {
                passwordLayout.setError("Password is required");
                animateShakeRotateEditTextErrorAnimation(passwordLayout);
                hasError = true;
            } else if (password.length() < 6) {
                passwordLayout.setError("Password must be at least 6 characters");
                animateShakeRotateEditTextErrorAnimation(passwordLayout);
                hasError = true;
            }

            // If no errors, proceed with validation and saving
            if (!hasError) {
                // Use fallback values if selectedSectionName or selectedGradeLevel are null
                // Trim section name to ensure consistency with teacher side
                String finalSectionName = (selectedSectionName != null) ? selectedSectionName.trim() : section.trim();
                String finalGradeLevel = (selectedGradeLevel != null) ? selectedGradeLevel : grade;

                // Validate student number exists in Firestore and check if already registered
                validateStudentNumber(studentNumber, password, finalSectionName, finalGradeLevel, 
                    firstName, lastName, section, grade, selectedSectionId, submitButton);
            } else {
                // Don't proceed if there are validation errors
                return;
            }
        });
    }
    
    private void validateStudentNumber(String studentNumber, String password, String finalSectionName, 
            String finalGradeLevel, String firstName, String lastName, String section, 
            String grade, String selectedSectionId, AppCompatButton submitButton) {
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Ensure student number is trimmed (double-check for safety)
        String trimmedStudentNumber = studentNumber != null ? studentNumber.trim() : "";
        
        // Ensure section name is trimmed for consistent matching (teacher side also trims)
        String trimmedSectionName = finalSectionName != null ? finalSectionName.trim() : "";
        
        Log.d("STUDENT_VALIDATION", "🔍 Validating student number...");
        Log.d("STUDENT_VALIDATION", "  - Student Number (input): '" + studentNumber + "'");
        Log.d("STUDENT_VALIDATION", "  - Student Number (trimmed): '" + trimmedStudentNumber + "'");
        Log.d("STUDENT_VALIDATION", "  - Section Name: '" + trimmedSectionName + "'");
        Log.d("STUDENT_VALIDATION", "  - Grade: " + finalGradeLevel);
        
        // Check if student number exists in the section
        db.collection("Sections")
            .whereEqualTo("Section", trimmedSectionName)
            .whereEqualTo("Grade_Number", Integer.parseInt(finalGradeLevel))
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    int resultCount = task.getResult().size();
                    Log.d("STUDENT_VALIDATION", "📊 Section query returned " + resultCount + " matching section(s)");
                    
                    if (!task.getResult().isEmpty()) {
                        String sectionDocId = task.getResult().getDocuments().get(0).getId();
                        String foundSectionName = task.getResult().getDocuments().get(0).getString("Section");
                        Long foundGradeNum = task.getResult().getDocuments().get(0).getLong("Grade_Number");
                        
                        Log.d("STUDENT_VALIDATION", "✅ Section found!");
                        Log.d("STUDENT_VALIDATION", "  - Section ID: " + sectionDocId);
                        Log.d("STUDENT_VALIDATION", "  - Section Name (from DB): '" + foundSectionName + "'");
                        Log.d("STUDENT_VALIDATION", "  - Grade Number: " + foundGradeNum);
                        Log.d("STUDENT_VALIDATION", "  - Query path: Sections/" + sectionDocId + "/StudentNumbers");
                        Log.d("STUDENT_VALIDATION", "  - Searching for studentNumber: '" + trimmedStudentNumber + "'");
                        
                        // Check student numbers collection in this section
                        db.collection("Sections")
                            .document(sectionDocId)
                            .collection("StudentNumbers")
                            .get() // Get ALL student numbers first for debugging
                            .addOnSuccessListener(allNumbersSnapshot -> {
                                Log.d("STUDENT_VALIDATION", "📋 Total student numbers in section: " + allNumbersSnapshot.size());
                                Log.d("STUDENT_VALIDATION", "📋 Available student numbers:");
                                for (QueryDocumentSnapshot doc : allNumbersSnapshot) {
                                    String savedNumber = doc.getString("studentNumber");
                                    Log.d("STUDENT_VALIDATION", "  - '" + savedNumber + "' (length: " + 
                                        (savedNumber != null ? savedNumber.length() : 0) + 
                                        ", equals search? " + (trimmedStudentNumber.equals(savedNumber != null ? savedNumber.trim() : "")) + ")");
                                }
                                Log.d("STUDENT_VALIDATION", "  - Searching for: '" + trimmedStudentNumber + "' (length: " + trimmedStudentNumber.length() + ")");
                                
                                // Now do the actual query
                                db.collection("Sections")
                                    .document(sectionDocId)
                                    .collection("StudentNumbers")
                                    .whereEqualTo("studentNumber", trimmedStudentNumber)
                                    .get()
                                    .addOnCompleteListener(validationTask -> {
                                        if (validationTask.isSuccessful()) {
                                            int studentNumberCount = validationTask.getResult().size();
                                            Log.d("STUDENT_VALIDATION", "📊 Student number query returned " + studentNumberCount + " document(s)");
                                            
                                            if (studentNumberCount == 0) {
                                                // Student number not found
                                                Log.w("STUDENT_VALIDATION", "❌ Student number not found in query!");
                                                Log.w("STUDENT_VALIDATION", "  - Searched for: '" + trimmedStudentNumber + "'");
                                                Log.w("STUDENT_VALIDATION", "  - This might be a case sensitivity or exact match issue");
                                                
                                                TextView studentNumberError = findViewById(R.id.studentNumberError);
                                                studentNumberError.setText("Student number not found. Please contact your teacher.");
                                                studentNumberError.setVisibility(View.VISIBLE);
                                            } else {
                                                // Student number exists, check if already registered
                                                Log.d("STUDENT_VALIDATION", "✅ Student number found! Checking if already registered...");
                                                checkIfStudentIdAlreadyRegistered(trimmedStudentNumber, password, finalSectionName, 
                                                    finalGradeLevel, firstName, lastName, section, grade, selectedSectionId, 
                                                    submitButton, sectionDocId);
                                            }
                                        } else {
                                            Log.e("STUDENT_VALIDATION", "❌ Error querying student numbers", validationTask.getException());
                                            Toast.makeText(this, "Error validating student number: " + 
                                                validationTask.getException().getMessage(), 
                                                Toast.LENGTH_LONG).show();
                                        }
                                    });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("STUDENT_VALIDATION", "❌ Error fetching all student numbers", e);
                                // Still try the query
                                db.collection("Sections")
                                    .document(sectionDocId)
                                    .collection("StudentNumbers")
                                    .whereEqualTo("studentNumber", trimmedStudentNumber)
                                    .get()
                                    .addOnCompleteListener(validationTask -> {
                                        if (validationTask.isSuccessful()) {
                                            if (validationTask.getResult().isEmpty()) {
                                                TextView studentNumberError = findViewById(R.id.studentNumberError);
                                                studentNumberError.setText("Student number not found. Please contact your teacher.");
                                                studentNumberError.setVisibility(View.VISIBLE);
                                            } else {
                                                // Student number exists, check if already registered
                                                checkIfStudentIdAlreadyRegistered(trimmedStudentNumber, password, finalSectionName, 
                                                    finalGradeLevel, firstName, lastName, section, grade, selectedSectionId, 
                                                    submitButton, sectionDocId);
                                            }
                                        } else {
                                            Toast.makeText(this, "Error validating student number: " + 
                                                validationTask.getException().getMessage(), 
                                                Toast.LENGTH_LONG).show();
                                        }
                                    });
                            });
                    } else {
                        Log.w("STUDENT_VALIDATION", "❌ Section not found!");
                        Log.w("STUDENT_VALIDATION", "  - Searched for: Section='" + trimmedSectionName + "', Grade=" + finalGradeLevel);
                        Toast.makeText(this, "Section not found: " + trimmedSectionName + " (Grade " + finalGradeLevel + ")", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e("STUDENT_VALIDATION", "❌ Error querying sections", task.getException());
                    Toast.makeText(this, "Error finding section: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                        Toast.LENGTH_LONG).show();
                }
            });
    }
    
    /**
     * Check if student ID is already registered
     */
    private void checkIfStudentIdAlreadyRegistered(String studentNumber, String password, 
            String finalSectionName, String finalGradeLevel, String firstName, String lastName, 
            String section, String grade, String selectedSectionId, AppCompatButton submitButton, 
            String sectionDocId) {
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Check if student number is already registered in Accounts/Students/MathSquare
        db.collection("Accounts")
            .document("Students")
            .collection("MathSquare")
            .whereEqualTo("studentNumber", studentNumber)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        // Student ID is already registered
                        Log.w("STUDENT_REGISTRATION", "❌ Student ID already registered: " + studentNumber);
                        TextView studentNumberError = findViewById(R.id.studentNumberError);
                        studentNumberError.setText("This student ID is already registered. Please sign in instead.");
                        studentNumberError.setVisibility(View.VISIBLE);
                    } else {
                        // Student ID is not registered, proceed with registration
                        Log.d("STUDENT_REGISTRATION", "✅ Student ID not registered, proceeding with registration...");
                        proceedWithRegistration(finalSectionName, finalGradeLevel, 
                            firstName, lastName, section, grade, selectedSectionId, 
                            submitButton, studentNumber, password, sectionDocId);
                    }
                } else {
                    Log.e("STUDENT_REGISTRATION", "❌ Error checking if student ID is registered", task.getException());
                    Toast.makeText(this, "Error checking student ID. Please try again.", Toast.LENGTH_LONG).show();
                }
            });
    }
    
    private void proceedWithRegistration(String finalSectionName, String finalGradeLevel,
            String firstName, String lastName, String section, String grade, 
            String selectedSectionId, AppCompatButton submitButton, String studentNumber, 
            String password, String sectionDocId) {
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Prepare data for Firestore (Student Data)
        String uuid = UUID.randomUUID().toString();
                HashMap<String, Object> studentData = new HashMap<>();
                studentData.put("firstName", firstName);
                studentData.put("lastName", lastName);
                studentData.put("section", finalSectionName);
                studentData.put("grade", finalGradeLevel);
        studentData.put("studentNumber", studentNumber);
        studentData.put("password", password); // Save password (in production, should be hashed)
                studentData.put("quizno", "N/A");
                studentData.put("timestamp", FieldValue.serverTimestamp());
                studentData.put("quizscore", "0");

                db.collection("Accounts").document("Students").collection("MathSquare")
                        .document(uuid)
                        .set(studentData)
                        .addOnSuccessListener(documentReference -> {
                            Log.d("STUDENT_SIGNUP", "Student saved successfully with UUID: " + uuid);
                            
                            // Mark student number as registered in Sections collection
                            markStudentNumberAsRegistered(studentNumber, sectionDocId);
                            
                            if (selectedSectionId != null) {
                                saveStudentToSection(firstName, lastName, selectedSectionId);
                            }

                            animateButtonPushDowm(submitButton);

                            sharedPreferences.StudentIsSetLoggedIn(studentSignUp.this, true);
                            sharedPreferences.setLoggedIn(studentSignUp.this, false);
                            sharedPreferences.saveSection(studentSignUp.this, section);
                            sharedPreferences.saveGrade(studentSignUp.this, grade);
                            sharedPreferences.saveFirstN(studentSignUp.this, firstName);
                            sharedPreferences.saveLastN(studentSignUp.this, lastName);
                            sharedPreferences.saveStudentNumber(studentSignUp.this, studentNumber); // Save student number to SharedPreferences

                            Intent intent = new Intent(this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(this, "Welcome Student!", Toast.LENGTH_SHORT).show();
                            stopButtonFocusAnimation(submitButton);
                            animateButtonFocus(submitButton);

                        })
                        .addOnFailureListener(e -> {
                            Log.e("STUDENT_SIGNUP", "Failed to save student data: " + e.getMessage());
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to save student data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Mark student number as registered in the section's StudentNumbers collection
     */
    private void markStudentNumberAsRegistered(String studentNumber, String sectionDocId) {
        if (sectionDocId == null || studentNumber == null) {
            Log.w("STUDENT_REGISTRATION", "Cannot mark student number as registered: sectionDocId or studentNumber is null");
            return;
        }
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Find the student number document and mark it as registered
        db.collection("Sections")
            .document(sectionDocId)
            .collection("StudentNumbers")
            .whereEqualTo("studentNumber", studentNumber.trim())
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    // Update the first matching document to mark it as registered
                    String docId = task.getResult().getDocuments().get(0).getId();
                    db.collection("Sections")
                        .document(sectionDocId)
                        .collection("StudentNumbers")
                        .document(docId)
                        .update("isRegistered", true, "registeredAt", FieldValue.serverTimestamp())
                        .addOnSuccessListener(aVoid -> {
                            Log.d("STUDENT_REGISTRATION", "✅ Student number marked as registered: " + studentNumber);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("STUDENT_REGISTRATION", "❌ Failed to mark student number as registered", e);
                        });
                } else {
                    Log.w("STUDENT_REGISTRATION", "⚠️ Student number document not found to mark as registered");
                }
            });
    }

    private void saveStudentToSection(String firstName, String lastName, String sectionDocId) {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    CollectionReference studentsCollection = db.collection("Sections")
        .document(sectionDocId)
        .collection("Students");

    // Use fallback values if selectedSectionName or selectedGradeLevel are null
    String finalSectionName = (selectedSectionName != null) ? selectedSectionName : "Unknown";
    String finalGradeLevel = (selectedGradeLevel != null) ? selectedGradeLevel : "Unknown";

    Log.d("SAVE_TO_SECTION", "Saving student to section: " + sectionDocId);
    Log.d("SAVE_TO_SECTION", "Student: " + firstName + " " + lastName + ", Section: " + finalSectionName + ", Grade: " + finalGradeLevel);

    //Student data map.
    Map<String, Object> studentData = new HashMap<>();
    studentData.put("firstName", firstName);
    studentData.put("lastName", lastName);
    studentData.put("section", finalSectionName);
    studentData.put("grade", finalGradeLevel);
    studentData.put("timestamp", FieldValue.serverTimestamp());

    studentsCollection.add(studentData)
        .addOnSuccessListener(documentReference -> {
            Log.d("SAVE_TO_SECTION", "Student added to section with ID: " + documentReference.getId());
            Toast.makeText(this, "Student added successfully!", Toast.LENGTH_SHORT).show();
        })
        .addOnFailureListener(e -> {
            Log.e("SAVE_TO_SECTION", "Failed to save student to section: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Failed to save student: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }


    // Shake and rotate animation for error fields
    private void animateShakeRotateEditTextErrorAnimation(View view) {
        AnimatorSet animatorSet = new AnimatorSet();

        // Shake animation
        ObjectAnimator shakeAnimator = ObjectAnimator.ofFloat(view, "translationX", 0, 20f, -20f, 20f, -20f, 0);
        shakeAnimator.setDuration(350);

        // Rotate animation
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(view, "rotation", 0, 5f, -5f, 5f, -5f, 0);
        rotateAnimator.setDuration(350);

        // Play both animations together
        animatorSet.playTogether(shakeAnimator, rotateAnimator);
        animatorSet.start();
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
    scaleX.setDuration(4000);
    scaleY.setDuration(4000);

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
