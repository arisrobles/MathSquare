package com.happym.mathsquare;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.happym.mathsquare.Model.User;
import com.happym.mathsquare.utils.TeacherMigrationUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // Table and search functionality variables
    private TableLayout tableLayout;
    private FirebaseFirestore db;
    private ImageView deleteBtn, checkBtn;
    private List<TableRow> selectedRows = new ArrayList<>();
    private List<CheckBox> rowCheckBoxes = new ArrayList<>();
    private CheckBox allCheckBox, rowCheckbox;
    private boolean isDeleteButtonSelected = false;

    // Search functionality variables
    private EditText searchEditText;
    private ImageView searchIcon, clearSearchIcon;
    private List<User> allUsers = new ArrayList<>(); // Store all users for filtering
    private List<TableRow> allTableRows = new ArrayList<>(); // Store all table rows

    // Section filter variables
    private LinearLayout btnShowAdmins, btnShowTeachers, btnShowStudents;
    private String currentFilter = "STUDENT"; // ADMIN, TEACHER, STUDENT (removed ALL) - Default to STUDENT to show students first
    private TextView userCountText;

    // Header column references for dynamic visibility
    private TextView headerEmail, headerSection, headerGrade;

    public UserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserFragment newInstance(String param1, String param2) {
        UserFragment fragment = new UserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        // Initialize Firebase
        FirebaseApp.initializeApp(requireContext());
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        tableLayout = view.findViewById(R.id.tableLayout);
        deleteBtn = view.findViewById(R.id.deletesection);
        checkBtn = view.findViewById(R.id.checkdelete);
        allCheckBox = view.findViewById(R.id.rowCheckboxSelectAll);
        userCountText = view.findViewById(R.id.user_count_text);

        // Initialize header column references
        headerEmail = view.findViewById(R.id.headerEmail);
        headerSection = view.findViewById(R.id.headerSection);
        headerGrade = view.findViewById(R.id.headerGrade);

        LinearLayout signInButton1 = view.findViewById(R.id.btn_playgame_as_student);
        LinearLayout signUpButton = view.findViewById(R.id.btn_signinteacher);

        animateButtonFocus(signInButton1);
        animateButtonFocus(signUpButton);

        signInButton1.setOnClickListener(v -> {
            animateButtonClick(signInButton1);
            stopButtonFocusAnimation(signInButton1);
            Intent intent = new Intent(getContext(), AddAdminActivity.class);
            startActivity(intent);
        });

        signUpButton.setOnClickListener(v -> {
            animateButtonClick(signUpButton);
            stopButtonFocusAnimation(signUpButton);
            Intent intent = new Intent(getContext(), teacherSignUp.class);
            startActivity(intent);
        });

        // Initialize section filter buttons
        initializeSectionButtons(view);

        // Initialize delete functionality
        initializeDeleteFunctionality();

        // Initialize search functionality
        initializeSearchFunction(view);

        // Fetch all users
        Log.e("FRAGMENT_INIT", "🚨 About to call fetchAllUsers from onCreate");
        fetchAllUsers();

        // Add refresh functionality - refresh data when fragment becomes visible
        Log.e("FRAGMENT_INIT", "🚨 About to call refreshUserData from onCreate");
        refreshUserData();

        return view;
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

    // Initialize section filter buttons
    private void initializeSectionButtons(View view) {
        btnShowAdmins = view.findViewById(R.id.btn_show_admins);
        btnShowTeachers = view.findViewById(R.id.btn_show_teachers);
        btnShowStudents = view.findViewById(R.id.btn_show_students);

        // Set initial state - Students selected (default)
        updateSectionButtonStyles("STUDENT");
        updateColumnVisibility("STUDENT");

        btnShowAdmins.setOnClickListener(v -> {
            animateButtonClick(btnShowAdmins);
            currentFilter = "ADMIN";
            updateSectionButtonStyles("ADMIN");
            updateColumnVisibility(currentFilter);
            applyCurrentFilter();
        });

        btnShowTeachers.setOnClickListener(v -> {
            animateButtonClick(btnShowTeachers);
            currentFilter = "TEACHER";
            updateSectionButtonStyles("TEACHER");
            updateColumnVisibility(currentFilter);
            applyCurrentFilter();
        });

        btnShowStudents.setOnClickListener(v -> {
            animateButtonClick(btnShowStudents);
            currentFilter = "STUDENT";
            updateSectionButtonStyles("STUDENT");
            updateColumnVisibility(currentFilter);

            // Force refresh student data when clicking Students tab
            Log.e("STUDENT_TAB", "🚨🚨🚨 STUDENT TAB CLICKED - FORCING REFRESH 🚨🚨🚨");
            fetchStudentAccounts();

            applyCurrentFilter();
        });
    }

    private void updateSectionButtonStyles(String activeFilter) {
        // Reset all buttons to inactive state
        btnShowAdmins.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.box_create));
        btnShowTeachers.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.box_create));
        btnShowStudents.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.box_create));

        // Set active button style
        switch (activeFilter) {
            case "ADMIN":
                btnShowAdmins.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_condition_create));
                break;
            case "TEACHER":
                btnShowTeachers.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_condition_create));
                break;
            case "STUDENT":
                btnShowStudents.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_condition_create));
                break;
        }
    }

    private void applyCurrentFilter() {
        String searchQuery = searchEditText.getText().toString().trim();
        updateColumnVisibility(currentFilter);
        filterTableBySection(searchQuery, currentFilter);
    }

    private void updateColumnVisibility(String filter) {
        if ("TEACHER".equals(filter) || "ADMIN".equals(filter)) {
            // For teachers and admins: show Name, Email, Type only
            headerEmail.setVisibility(View.VISIBLE);
            headerSection.setVisibility(View.GONE);
            headerGrade.setVisibility(View.GONE);
            Log.d("COLUMN_VISIBILITY", "Showing Name, Email, Type columns for " + filter.toLowerCase() + "s");
        } else if ("STUDENT".equals(filter)) {
            // For students: show Name, Type, Section, Grade (hide Email and Info)
            headerEmail.setVisibility(View.GONE);
            headerSection.setVisibility(View.VISIBLE);
            headerGrade.setVisibility(View.VISIBLE);
            Log.d("COLUMN_VISIBILITY", "Showing Name, Type, Section, Grade columns for students (hiding Email and Info)");
        } else {
            // Default: show all columns
            headerEmail.setVisibility(View.VISIBLE);
            headerSection.setVisibility(View.VISIBLE);
            headerGrade.setVisibility(View.VISIBLE);
            Log.d("COLUMN_VISIBILITY", "Showing all columns for filter: " + filter);
        }
    }

    // Initialize delete functionality
    private void initializeDeleteFunctionality() {
        deleteBtn.setOnClickListener(v -> {
            // Toggle delete button selection state
            isDeleteButtonSelected = !isDeleteButtonSelected;

            if (isDeleteButtonSelected) {
                // First press: show check button and checkboxes
                // Only show header checkbox if we're not in a filtered view that hides columns
                // For now, we'll keep the header checkbox hidden to avoid layout issues
                // allCheckBox.setVisibility(View.VISIBLE);
                checkBtn.setVisibility(View.VISIBLE);
                deleteBtn.setImageResource(R.drawable.ic_cancel);

                for (CheckBox checkBox : rowCheckBoxes) {
                    checkBox.setVisibility(View.VISIBLE);
                }
            } else {
                // Second press: cancel and reset
                allCheckBox.setVisibility(View.GONE);
                checkBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.btn_green_off));
                checkBtn.setImageResource(R.drawable.ic_check);
                checkBtn.setVisibility(View.GONE);
                deleteBtn.setImageResource(R.drawable.ic_delete);

                for (CheckBox checkBox : rowCheckBoxes) {
                    checkBox.setVisibility(View.GONE);
                }
            }
        });

        checkBtn.setOnClickListener(v -> {
            if (selectedRows.isEmpty()) {
                checkBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.btn_green_off));
                checkBtn.setImageResource(R.drawable.ic_check);
                Toast.makeText(getContext(), "No data deleted.", Toast.LENGTH_SHORT).show();
            } else {
                checkBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.btn_condition_create));
                checkBtn.setImageResource(R.drawable.ic_check);

                new AlertDialog.Builder(requireContext())
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete the selected users?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteSelectedRows();
                                // Reset delete mode
                                isDeleteButtonSelected = false;
                                allCheckBox.setVisibility(View.GONE);
                                checkBtn.setVisibility(View.GONE);
                                deleteBtn.setImageResource(R.drawable.ic_delete);
                                for (CheckBox checkBox : rowCheckBoxes) {
                                    checkBox.setVisibility(View.GONE);
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        allCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleAllRows(isChecked);
        });
    }

    // Initialize search functionality
    private void initializeSearchFunction(View view) {
        searchEditText = view.findViewById(R.id.search_edittext);
        searchIcon = view.findViewById(R.id.search_icon);
        clearSearchIcon = view.findViewById(R.id.clear_search_icon);

        // Set up search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchQuery = s.toString().trim();

                // Show/hide clear button based on text presence
                if (searchQuery.isEmpty()) {
                    clearSearchIcon.setVisibility(View.GONE);
                } else {
                    clearSearchIcon.setVisibility(View.VISIBLE);
                }

                // Filter the table based on search query and current section filter
                filterTableBySection(searchQuery, currentFilter);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        // Clear search functionality
        clearSearchIcon.setOnClickListener(v -> {
            searchEditText.setText("");
            clearSearchIcon.setVisibility(View.GONE);
            filterTableBySection("", currentFilter); // Show all rows for current section
        });

        // Search icon click to focus on EditText
        searchIcon.setOnClickListener(v -> {
            searchEditText.requestFocus();
        });
    }

    // Fetch all users from different collections
    private void fetchAllUsers() {
        Log.e("FETCH_ALL", "🚨🚨🚨 FETCH ALL USERS CALLED 🚨🚨🚨");
        allUsers.clear();

        // Debug: Let's see what's actually in the Teachers collection
        debugTeachersCollection();

        // Check and migrate teachers if needed (one-time operation)
        TeacherMigrationUtil migrationUtil = new TeacherMigrationUtil();
        migrationUtil.checkAndMigrateIfNeeded();

        // Fetch Admin accounts
        Log.e("FETCH_ALL", "🔥 About to fetch admin accounts");
        fetchAdminAccounts();

        // Fetch Teacher accounts
        Log.e("FETCH_ALL", "🔥 About to fetch teacher accounts");
        fetchTeacherAccounts();

        // Fetch Student accounts
        Log.e("FETCH_ALL", "🔥 About to fetch student accounts");
        fetchStudentAccounts();
    }

    private void debugTeachersCollection() {
        Log.d("DEBUG_TEACHERS", "=== DEBUGGING TEACHERS COLLECTION ===");

        // Check what's in the main Teachers document
        db.collection("Accounts")
            .document("Teachers")
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Log.d("DEBUG_TEACHERS", "Teachers document exists");
                    if (documentSnapshot.getData() != null) {
                        Log.d("DEBUG_TEACHERS", "Teachers document data: " + documentSnapshot.getData().toString());

                        // Log all fields in the Teachers document
                        for (String key : documentSnapshot.getData().keySet()) {
                            Object value = documentSnapshot.get(key);
                            Log.d("DEBUG_TEACHERS", "Field: " + key + " = " + value);
                        }
                    } else {
                        Log.d("DEBUG_TEACHERS", "Teachers document exists but has no data");
                    }
                } else {
                    Log.d("DEBUG_TEACHERS", "Teachers document does not exist");
                }
            })
            .addOnFailureListener(e -> {
                Log.e("DEBUG_TEACHERS", "Failed to fetch Teachers document: " + e.getMessage());
            });

        // Also check if TeacherProfiles collection exists
        db.collection("TeacherProfiles")
            .limit(10)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                Log.d("DEBUG_TEACHERS", "TeacherProfiles collection has " + querySnapshot.size() + " documents");
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    Log.d("DEBUG_TEACHERS", "TeacherProfile: " + doc.getId() + " -> " + doc.getData());
                }
            })
            .addOnFailureListener(e -> {
                Log.e("DEBUG_TEACHERS", "Failed to fetch TeacherProfiles: " + e.getMessage());
            });
    }

    private void fetchAdminAccounts() {
        db.collection("Admin")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        try {
                            String firstName = doc.getString("firstName");
                            String email = doc.getString("email");
                            String documentId = doc.getId();

                            if (firstName != null && email != null) {
                                User user = new User(firstName, email, "Admin", documentId);
                                allUsers.add(user);
                            }
                        } catch (Exception e) {
                            Log.e("FETCH_ADMIN", "Error parsing admin document: " + e.getMessage());
                        }
                    }
                    Log.d("FETCH_ADMIN", "Admin accounts fetched: " + task.getResult().size());
                    updateTableIfAllDataFetched();
                } else {
                    Log.w("FETCH_ADMIN", "Error fetching admin accounts");
                }
            })
            .addOnFailureListener(e -> {
                Log.e("FETCH_ADMIN", "Firestore query failed: " + e.getMessage());
            });
    }

    private void fetchTeacherAccounts() {
        Log.d("FETCH_TEACHER", "Starting comprehensive teacher account fetch process...");

        // Use a multi-step approach to fetch teachers from all possible locations
        fetchTeachersFromAllSources();
    }

    private void fetchTeachersFromAllSources() {
        Log.d("FETCH_TEACHER", "=== COMPREHENSIVE TEACHER FETCH ===");

        // Step 1: Try to fetch from TeacherProfiles first (most reliable)
        fetchFromTeacherProfiles();

        // Step 2: Also try to fetch from TeacherEmailList to discover more teachers
        fetchFromTeacherEmailList();

        // Step 3: Try to discover teachers from the original structure
        discoverTeachersFromOriginalStructure();
    }

    private void fetchFromTeacherProfiles() {
        Log.d("FETCH_TEACHER", "Step 1: Fetching from TeacherProfiles collection...");

        db.collection("TeacherProfiles")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Log.d("FETCH_TEACHER", "TeacherProfiles query successful. Documents found: " + task.getResult().size());

                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        try {
                            String firstName = doc.getString("firstName");
                            String email = doc.getString("email");
                            String originalEmail = doc.getString("originalEmail");
                            String documentId = doc.getId();
                            // Get grade and section if assigned
                            String assignedGrade = doc.getString("assignedGrade");
                            String assignedSection = doc.getString("assignedSection");

                            Log.d("FETCH_TEACHER", "Processing TeacherProfile: " + firstName + " | " + email + " | " + originalEmail);

                            if (firstName != null) {
                                // Use originalEmail if available, otherwise use email
                                String displayEmail = originalEmail != null ? originalEmail : email;

                                // Check for duplicates before adding
                                if (!isTeacherAlreadyAdded(displayEmail)) {
                                    User user = new User(firstName, displayEmail != null ? displayEmail : "N/A", "Teacher", documentId);
                                    // Set grade and section if assigned
                                    if (assignedGrade != null) {
                                        user.setGrade(assignedGrade);
                                    }
                                    if (assignedSection != null) {
                                        user.setSection(assignedSection);
                                    }
                                    allUsers.add(user);
                                    Log.d("FETCH_TEACHER", "Added teacher from TeacherProfiles: " + firstName + " (" + displayEmail + ") Grade: " + assignedGrade + ", Section: " + assignedSection);
                                } else {
                                    Log.d("FETCH_TEACHER", "Teacher already exists, skipping: " + firstName);
                                }
                            }
                        } catch (Exception e) {
                            Log.e("FETCH_TEACHER", "Error parsing TeacherProfile document: " + e.getMessage());
                        }
                    }

                    Log.d("FETCH_TEACHER", "Completed fetching from TeacherProfiles. Total teachers so far: " + countTeachers());
                } else {
                    Log.w("FETCH_TEACHER", "TeacherProfiles query failed or returned null");
                    if (task.getException() != null) {
                        Log.e("FETCH_TEACHER", "TeacherProfiles query exception: " + task.getException().getMessage());
                    }
                }

                // Always update table after this step
                updateTableIfAllDataFetched();
            })
            .addOnFailureListener(e -> {
                Log.e("FETCH_TEACHER", "TeacherProfiles collection query failed: " + e.getMessage());
                updateTableIfAllDataFetched();
            });
    }

    // Helper method to check if a teacher is already added
    private boolean isTeacherAlreadyAdded(String email) {
        for (User existingUser : allUsers) {
            if ("Teacher".equals(existingUser.getAccountType()) &&
                existingUser.getEmail() != null &&
                existingUser.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    // Helper method to count teachers in allUsers
    private int countTeachers() {
        int count = 0;
        for (User user : allUsers) {
            if ("Teacher".equals(user.getAccountType())) {
                count++;
            }
        }
        return count;
    }

    private void fetchFromTeacherEmailList() {
        Log.d("FETCH_TEACHER", "Step 2: Fetching from TeacherEmailList collection...");

        db.collection("TeacherEmailList")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                    Log.d("FETCH_TEACHER", "TeacherEmailList found with " + task.getResult().size() + " entries");

                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        try {
                            String email = doc.getString("email");
                            if (email != null && !isTeacherAlreadyAdded(email)) {
                                Log.d("FETCH_TEACHER", "Discovered teacher email from list: " + email);
                                // Try to fetch this teacher's full profile
                                fetchTeacherFromOriginalLocation(email);
                            }
                        } catch (Exception e) {
                            Log.e("FETCH_TEACHER", "Error processing TeacherEmailList document: " + e.getMessage());
                        }
                    }
                } else {
                    Log.d("FETCH_TEACHER", "TeacherEmailList not found or empty");
                }
            })
            .addOnFailureListener(e -> {
                Log.e("FETCH_TEACHER", "Failed to fetch TeacherEmailList: " + e.getMessage());
            });
    }

    private void discoverTeachersFromOriginalStructure() {
        Log.d("FETCH_TEACHER", "Step 3: Attempting to discover teachers from original structure...");

        // Try some common teacher email patterns to discover any missed teachers
        String[] commonEmails = {
            "teacher@gmail.com",
            "naxs@gmail.com",
            "admin@gmail.com",
            "test@gmail.com",
            "demo@gmail.com"
        };

        for (String email : commonEmails) {
            if (!isTeacherAlreadyAdded(email)) {
                fetchTeacherFromOriginalLocation(email);
            }
        }
    }

    private void fetchTeacherFromOriginalLocation(String email) {
        Log.d("FETCH_TEACHER", "Fetching teacher from original location: " + email);

        db.collection("Accounts")
            .document("Teachers")
            .collection(email)
            .document("MyProfile")
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    try {
                        String firstName = documentSnapshot.getString("firstName");
                        String teacherEmail = documentSnapshot.getString("email");

                        Log.d("FETCH_TEACHER", "Found teacher in original location: " + firstName + " | " + teacherEmail);

                        if (firstName != null) {
                            String displayEmail = teacherEmail != null ? teacherEmail : email;

                            if (!isTeacherAlreadyAdded(displayEmail)) {
                                // Use the document path as a unique ID since we don't have a proper document ID
                                String docId = "original_" + email.replace(".", "_").replace("@", "_at_");
                                User user = new User(firstName, displayEmail, "Teacher", docId);
                                allUsers.add(user);
                                Log.d("FETCH_TEACHER", "Added teacher from original location: " + firstName + " (" + displayEmail + ")");

                                // Update table after adding each teacher
                                updateTableIfAllDataFetched();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("FETCH_TEACHER", "Error parsing teacher from original location " + email + ": " + e.getMessage());
                    }
                } else {
                    Log.d("FETCH_TEACHER", "No teacher found at original location: " + email);
                }
            })
            .addOnFailureListener(e -> {
                Log.d("FETCH_TEACHER", "Failed to fetch teacher from original location " + email + ": " + e.getMessage());
            });
    }

    private void fetchStudentAccounts() {
        Log.e("FETCH_STUDENT", "🚨🚨🚨 FETCH STUDENT ACCOUNTS CALLED 🚨🚨🚨");
        Log.e("FETCH_STUDENT", "Starting to fetch student accounts from Accounts/Students/MathSquare");

        // Clear existing students from allUsers before fetching
        allUsers.removeIf(user -> "Student".equals(user.getAccountType()));
        Log.e("FETCH_STUDENT", "Cleared existing students from allUsers");

        Log.e("FETCH_STUDENT", "🔥 About to execute Firestore query: Accounts/Students/MathSquare");

        db.collection("Accounts")
            .document("Students")
            .collection("MathSquare")
            .get()
            .addOnCompleteListener(task -> {
                Log.e("FETCH_STUDENT", "🔥 Firestore query completed!");
                if (task.isSuccessful() && task.getResult() != null) {
                    Log.e("FETCH_STUDENT", "🔥 Query successful. Documents found: " + task.getResult().size());

                    if (task.getResult().isEmpty()) {
                        Log.w("FETCH_STUDENT", "No student documents found in Accounts/Students/MathSquare");
                    }

                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        try {
                            // Log the document data for debugging
                            Log.d("FETCH_STUDENT", "=== Processing document ID: " + doc.getId() + " ===");
                            Log.d("FETCH_STUDENT", "Raw document data: " + doc.getData().toString());

                            // Try to get all possible field variations
                            String firstName = doc.getString("firstName");
                            String lastName = doc.getString("lastName");
                            String section = doc.getString("section");
                            String grade = doc.getString("grade");
                            String quizScore = doc.getString("quizscore");
                            String documentId = doc.getId();

                            // Also try alternative field names in case of inconsistency
                            if (firstName == null) firstName = doc.getString("firstname");
                            if (lastName == null) lastName = doc.getString("lastname");
                            if (firstName == null) firstName = doc.getString("FirstName");
                            if (lastName == null) lastName = doc.getString("LastName");

                            Log.d("FETCH_STUDENT", "Extracted fields:");
                            Log.d("FETCH_STUDENT", "  firstName: '" + firstName + "'");
                            Log.d("FETCH_STUDENT", "  lastName: '" + lastName + "'");
                            Log.d("FETCH_STUDENT", "  section: '" + section + "'");
                            Log.d("FETCH_STUDENT", "  grade: '" + grade + "'");
                            Log.d("FETCH_STUDENT", "  quizScore: '" + quizScore + "'");

                            if (firstName != null && lastName != null && !firstName.trim().isEmpty() && !lastName.trim().isEmpty()) {
                                String fullName = firstName.trim() + " " + lastName.trim();
                                String additionalInfo = "Score: " + (quizScore != null ? quizScore : "0");

                                User user = new User(fullName, null, "Student",
                                    section != null ? section.trim() : "N/A",
                                    grade != null ? grade.trim() : "N/A",
                                    additionalInfo, documentId);
                                allUsers.add(user);

                                // Create student key for duplicate tracking
                                String studentKey = fullName.toLowerCase().trim() + "_" +
                                                  (grade != null ? grade.trim().toLowerCase() : "n/a") + "_" +
                                                  (section != null ? section.trim().toLowerCase() : "n/a");

                                Log.d("FETCH_STUDENT", "✅ Successfully added student: " + fullName +
                                      " (Grade: " + grade + ", Section: " + section + ", Key: " + studentKey + ", DocId: " + documentId + ")");
                            } else {
                                Log.w("FETCH_STUDENT", "❌ Skipping document - firstName: '" + firstName + "', lastName: '" + lastName + "'");
                            }
                        } catch (Exception e) {
                            Log.e("FETCH_STUDENT", "❌ Error parsing student document " + doc.getId() + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    Log.d("FETCH_STUDENT", "=== SUMMARY ===");
                    Log.d("FETCH_STUDENT", "Total documents processed: " + task.getResult().size());
                    Log.d("FETCH_STUDENT", "Total students added to allUsers: " + allUsers.stream().mapToInt(u -> "Student".equals(u.getAccountType()) ? 1 : 0).sum());
                    Log.d("FETCH_STUDENT", "Total users in allUsers list: " + allUsers.size());
                    updateTableIfAllDataFetched();
                } else {
                    Log.w("FETCH_STUDENT", "❌ Error fetching student accounts. Task successful: " + task.isSuccessful());
                    if (task.getException() != null) {
                        Log.e("FETCH_STUDENT", "Exception: " + task.getException().getMessage());
                        task.getException().printStackTrace();
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e("FETCH_STUDENT", "❌ Firestore query failed: " + e.getMessage());
                e.printStackTrace();
            });
    }

    // These old migration methods are no longer needed - replaced by the new comprehensive fetching system

    private void updateTableIfAllDataFetched() {
        Log.d("UPDATE_TABLE", "updateTableIfAllDataFetched called");
        Log.d("UPDATE_TABLE", "Total users in allUsers: " + allUsers.size());
        Log.d("UPDATE_TABLE", "Current filter: " + currentFilter);

        // Log all users for debugging
        for (int i = 0; i < allUsers.size(); i++) {
            User user = allUsers.get(i);
            Log.d("UPDATE_TABLE", "User " + i + ": " + user.getName() + " (" + user.getAccountType() + ")");
        }

        // Remove any duplicates before updating the table
        removeDuplicateUsers();

        Log.d("UPDATE_TABLE", "After removing duplicates: " + allUsers.size() + " users");

        // Update column visibility based on current filter
        updateColumnVisibility(currentFilter);

        // Simple approach: update table after each fetch
        // In a production app, you might want to wait for all fetches to complete
        String searchQuery = searchEditText != null ? searchEditText.getText().toString().trim() : "";
        Log.d("UPDATE_TABLE", "Calling filterTableBySection with query: '" + searchQuery + "', filter: " + currentFilter);
        filterTableBySection(searchQuery, currentFilter);
    }

    // Map to track student duplicate groups for deletion
    private Map<String, List<String>> studentDuplicateGroups = new HashMap<>();
    // Store all students (including duplicates) for deletion purposes
    private List<User> allStudentsIncludingDuplicates = new ArrayList<>();

    private void removeDuplicateUsers() {
        List<User> uniqueUsers = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();
        Set<String> seenStudentKeys = new HashSet<>(); // Track student combinations
        studentDuplicateGroups.clear(); // Clear previous duplicate groups
        allStudentsIncludingDuplicates.clear(); // Clear previous student list

        for (User user : allUsers) {
            String key = ""; // Initialize key to avoid compilation error
            boolean shouldKeep = true;

            // Create different keys for different account types
            if ("Student".equals(user.getAccountType())) {
                // Store ALL students (including duplicates) for deletion purposes
                allStudentsIncludingDuplicates.add(user);

                // For students, check for duplicate name + grade + section combination
                String studentKey = user.getName().toLowerCase().trim() + "_" +
                                  user.getGrade().toLowerCase().trim() + "_" +
                                  user.getSection().toLowerCase().trim();

                // Always add to duplicate group (even if it's the first one)
                if (!studentDuplicateGroups.containsKey(studentKey)) {
                    studentDuplicateGroups.put(studentKey, new ArrayList<>());
                }
                studentDuplicateGroups.get(studentKey).add(user.getDocId());

                if (seenStudentKeys.contains(studentKey)) {
                    // Student with same name, grade, and section already exists - don't display
                    Log.d("DUPLICATE_REMOVAL", "Found duplicate student (hiding from display): " + user.getName() +
                          " (Grade: " + user.getGrade() + ", Section: " + user.getSection() + ")");
                    shouldKeep = false;
                } else {
                    // First student with this combination - display it
                    seenStudentKeys.add(studentKey);
                    Log.d("DUPLICATE_REMOVAL", "First student with combination (will display): " + user.getName() +
                          " (Grade: " + user.getGrade() + ", Section: " + user.getSection() + ")");

                    // Use name + section + grade + docId for general duplicate checking
                    key = user.getName() + "_" + user.getSection() + "_" + user.getGrade() + "_" + user.getDocId();
                }
            } else {
                // For admins and teachers, use email + account type
                key = user.getEmail() + "_" + user.getAccountType();
            }

            if (shouldKeep && ("Student".equals(user.getAccountType()) || !seenKeys.contains(key))) {
                if (!"Student".equals(user.getAccountType())) {
                    seenKeys.add(key);
                }
                uniqueUsers.add(user);
                Log.d("DUPLICATE_REMOVAL", "Keeping user: " + user.getName() + " (" + user.getAccountType() + ")");
            } else if (!"Student".equals(user.getAccountType()) && !key.isEmpty()) {
                Log.d("DUPLICATE_REMOVAL", "Removing duplicate user: " + user.getName() + " (" + user.getAccountType() + ") with key: " + key);
            }
        }

        // Log duplicate groups for debugging
        for (Map.Entry<String, List<String>> entry : studentDuplicateGroups.entrySet()) {
            Log.d("DUPLICATE_GROUPS", "Student group '" + entry.getKey() + "' has " +
                  entry.getValue().size() + " students: " + entry.getValue().toString());
        }

        Log.d("DUPLICATE_REMOVAL", "Total students fetched (including duplicates): " + allStudentsIncludingDuplicates.size());
        Log.d("DUPLICATE_REMOVAL", "Students to display (after duplicate filtering): " +
              uniqueUsers.stream().mapToInt(u -> "Student".equals(u.getAccountType()) ? 1 : 0).sum());

        if (allUsers.size() != uniqueUsers.size()) {
            Log.d("DUPLICATE_REMOVAL", "Removed " + (allUsers.size() - uniqueUsers.size()) + " duplicate users from display");
            allUsers.clear();
            allUsers.addAll(uniqueUsers);
        } else {
            Log.d("DUPLICATE_REMOVAL", "No duplicates found, keeping all " + allUsers.size() + " users");
        }
    }

    private void addRowsToTable(List<User> users) {
        try {
            // Clear existing data rows only (keep header in layout)
            int childCount = tableLayout.getChildCount();
            if (childCount > 1) {
                tableLayout.removeViews(1, childCount - 1);
            }

            allTableRows.clear();
            rowCheckBoxes.clear();
            selectedRows.clear();

            for (User user : users) {
                TableRow row = (TableRow) LayoutInflater.from(requireContext()).inflate(R.layout.row_user, null);

                rowCheckbox = row.findViewById(R.id.rowCheckbox);
                TextView nameText = row.findViewById(R.id.nameText);
                TextView emailText = row.findViewById(R.id.emailText);
                TextView accountTypeText = row.findViewById(R.id.accountTypeText);
                TextView sectionText = row.findViewById(R.id.sectionText);
                TextView gradeText = row.findViewById(R.id.gradeText);

                nameText.setText(user.getName());
                emailText.setText(user.getEmail());
                accountTypeText.setText(user.getAccountType());
                sectionText.setText(user.getSection());
                gradeText.setText(user.getGrade());

                // Set column visibility based on current filter
                if ("TEACHER".equals(currentFilter)) {
                    // For teachers: show Name, Email, Type, Grade, Section
                    emailText.setVisibility(View.VISIBLE);
                    sectionText.setVisibility(View.VISIBLE);
                    gradeText.setVisibility(View.VISIBLE);
                } else if ("ADMIN".equals(currentFilter)) {
                    // For admins: show Name, Email, Type only
                    emailText.setVisibility(View.VISIBLE);
                    sectionText.setVisibility(View.GONE);
                    gradeText.setVisibility(View.GONE);
                } else if ("STUDENT".equals(currentFilter)) {
                    // For students: show Name, Type, Section, Grade (hide Email and Info)
                    emailText.setVisibility(View.GONE);
                    sectionText.setVisibility(View.VISIBLE);
                    gradeText.setVisibility(View.VISIBLE);
                } else {
                    // Default: show all columns
                    emailText.setVisibility(View.VISIBLE);
                    sectionText.setVisibility(View.VISIBLE);
                    gradeText.setVisibility(View.VISIBLE);
                }

                row.setTag(user.getDocId());

                // Special handling for admin accounts - prevent logged-in admin from being deleted
                if ("Admin".equals(user.getAccountType())) {
                    String currentAdminEmail = sharedPreferences.getEmail(requireContext());
                    String adminEmailToCheck = user.getEmail();

                    if (currentAdminEmail != null && currentAdminEmail.equals(adminEmailToCheck)) {
                        // This is the currently logged-in admin
                        // Add visual indication (slightly different text style)
                        nameText.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                        nameText.setText(user.getName() + " (You)");

                        // Disable checkbox for the logged-in admin
                        rowCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) {
                                // Prevent selection and show message
                                buttonView.setChecked(false);
                                Toast.makeText(getContext(), "You cannot delete your own admin account", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Regular admin account - normal checkbox functionality
                        rowCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) {
                                selectedRows.add(row);
                                checkBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.btn_condition_create));
                                checkBtn.setImageResource(R.drawable.ic_delete);
                            } else {
                                selectedRows.remove(row);
                                // Uncheck the "Select All" checkbox if any row is deselected
                                allCheckBox.setOnCheckedChangeListener(null); // Temporarily remove listener
                                allCheckBox.setChecked(false);
                                allCheckBox.setOnCheckedChangeListener((buttonView1, isChecked1) -> {
                                    checkBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.btn_condition_create));
                                    checkBtn.setImageResource(R.drawable.ic_delete);
                                    toggleAllRows(isChecked1);
                                });
                            }
                        });
                    }
                } else {
                    // Non-admin accounts - normal checkbox functionality
                    rowCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            selectedRows.add(row);
                            checkBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.btn_condition_create));
                            checkBtn.setImageResource(R.drawable.ic_delete);
                        } else {
                            selectedRows.remove(row);
                            // Uncheck the "Select All" checkbox if any row is deselected
                            allCheckBox.setOnCheckedChangeListener(null); // Temporarily remove listener
                            allCheckBox.setChecked(false);
                            allCheckBox.setOnCheckedChangeListener((buttonView1, isChecked1) -> {
                                checkBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.btn_condition_create));
                                checkBtn.setImageResource(R.drawable.ic_delete);
                                toggleAllRows(isChecked1);
                            });
                        }
                    });
                }
                
                // Add edit functionality for teachers (only visible when viewing teachers)
                if ("Teacher".equals(user.getAccountType()) && "TEACHER".equals(currentFilter)) {
                    // Make the row clickable to edit teacher
                    row.setOnClickListener(v -> {
                        showEditTeacherDialog(user);
                    });
                }

                rowCheckBoxes.add(rowCheckbox); // Add the checkbox to the list
                allTableRows.add(row); // Store row for search functionality
                tableLayout.addView(row);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "An error occurred while adding rows: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace(); // Log the error for debugging purposes
        }
    }

    private void toggleAllRows(boolean isChecked) {
        selectedRows.clear(); // Clear previous selections
        String currentAdminEmail = sharedPreferences.getEmail(requireContext());

        for (int i = 0; i < rowCheckBoxes.size(); i++) {
            CheckBox checkbox = rowCheckBoxes.get(i);
            TableRow row = (TableRow) checkbox.getParent();

            checkBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.btn_condition_create));
            checkBtn.setImageResource(R.drawable.ic_delete);

            // Check if this row belongs to the logged-in admin
            String docId = (String) row.getTag();
            boolean isLoggedInAdmin = false;

            if (currentAdminEmail != null) {
                for (User user : allUsers) {
                    if (user.getDocId().equals(docId) &&
                        "Admin".equals(user.getAccountType()) &&
                        currentAdminEmail.equals(user.getEmail())) {
                        isLoggedInAdmin = true;
                        break;
                    }
                }
            }

            // Skip the logged-in admin when selecting all
            if (isLoggedInAdmin) {
                checkbox.setChecked(false); // Keep logged-in admin unchecked
            } else {
                checkbox.setChecked(isChecked); // Update other checkboxes

                // If checked, add the corresponding row to the selected rows
                if (isChecked) {
                    selectedRows.add(row);
                }
            }
        }
    }

    // Filter table based on search query and section filter
    private void filterTableBySection(String query, String sectionFilter) {
        try {
            Log.d("FILTER_TABLE", "filterTableBySection called with query: '" + query + "', filter: " + sectionFilter);
            Log.d("FILTER_TABLE", "Total users to filter: " + allUsers.size());

            // Clear current table data rows (keep header)
            int childCount = tableLayout.getChildCount();
            if (childCount > 1) {
                tableLayout.removeViews(1, childCount - 1);
            }

            // First filter by section
            List<User> sectionFilteredUsers = new ArrayList<>();

            for (User user : allUsers) {
                Log.d("FILTER_TABLE", "Checking user: " + user.getName() + " (Type: " + user.getAccountType() + ")");
                if ("ADMIN".equals(sectionFilter) && "Admin".equals(user.getAccountType())) {
                    sectionFilteredUsers.add(user);
                    Log.d("FILTER_TABLE", "Added admin: " + user.getName());
                } else if ("TEACHER".equals(sectionFilter) && "Teacher".equals(user.getAccountType())) {
                    sectionFilteredUsers.add(user);
                    Log.d("FILTER_TABLE", "Added teacher: " + user.getName());
                } else if ("STUDENT".equals(sectionFilter) && "Student".equals(user.getAccountType())) {
                    sectionFilteredUsers.add(user);
                    Log.d("FILTER_TABLE", "Added student: " + user.getName());
                }
            }

            Log.d("FILTER_TABLE", "Section filtered users: " + sectionFilteredUsers.size());

            // If query is empty, show section filtered users
            if (query.isEmpty()) {
                addRowsToTable(sectionFilteredUsers);
                updateUserCount(sectionFilteredUsers.size(), sectionFilter, "");
                Log.d("FILTER", "Section: '" + sectionFilter + "' | Results: " + sectionFilteredUsers.size() + "/" + allUsers.size());
                return;
            }

            // Then filter by search query within the section filtered results
            List<User> finalFilteredUsers = new ArrayList<>();
            String lowerCaseQuery = query.toLowerCase();

            for (User user : sectionFilteredUsers) {
                // Search in name, email, account type, section, grade, and additional info
                if (user.getName().toLowerCase().contains(lowerCaseQuery) ||
                    user.getEmail().toLowerCase().contains(lowerCaseQuery) ||
                    user.getAccountType().toLowerCase().contains(lowerCaseQuery) ||
                    user.getSection().toLowerCase().contains(lowerCaseQuery) ||
                    user.getGrade().toLowerCase().contains(lowerCaseQuery) ||
                    user.getAdditionalInfo().toLowerCase().contains(lowerCaseQuery)) {

                    finalFilteredUsers.add(user);
                }
            }

            // Add filtered rows to table
            addRowsToTable(finalFilteredUsers);

            // Update user count
            updateUserCount(finalFilteredUsers.size(), sectionFilter, query);

            // Log search results
            Log.d("SEARCH", "Section: '" + sectionFilter + "' | Query: '" + query + "' | Results: " + finalFilteredUsers.size() + "/" + sectionFilteredUsers.size());

        } catch (Exception e) {
            Log.e("SEARCH", "Error filtering table: " + e.getMessage());
            Toast.makeText(getContext(), "Error during search: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserCount(int displayedCount, String sectionFilter, String searchQuery) {
        String countText;

        if (!searchQuery.isEmpty()) {
            // Show search results count
            countText = "Found: " + displayedCount + " users";
        } else {
            // Show section count
            switch (sectionFilter) {
                case "ADMIN":
                    countText = "Admins: " + displayedCount;
                    break;
                case "TEACHER":
                    countText = "Teachers: " + displayedCount;
                    break;
                case "STUDENT":
                    countText = "Students: " + displayedCount;
                    break;
                default:
                    countText = "Users: " + displayedCount;
                    break;
            }
        }

        userCountText.setText(countText);
    }

    private void deleteSelectedRows() {
        for (TableRow row : selectedRows) {
            String docId = (String) row.getTag();

            // Find the user to determine which collection to delete from
            User userToDelete = null;
            for (User user : allUsers) {
                if (user.getDocId().equals(docId)) {
                    userToDelete = user;
                    break;
                }
            }

            if (userToDelete != null) {
                deleteUserFromFirestore(userToDelete, row);
            }
        }

        selectedRows.clear(); // Clear the list after deletion
        Toast.makeText(getContext(), "Selected users deleted successfully.", Toast.LENGTH_SHORT).show();
        allCheckBox.setVisibility(View.GONE);
        checkBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.btn_green_off));
        checkBtn.setImageResource(R.drawable.ic_check);
        checkBtn.setVisibility(View.GONE);
        deleteBtn.setImageResource(R.drawable.ic_delete);
        for (CheckBox checkBox : rowCheckBoxes) {
            checkBox.setVisibility(View.GONE);
        }
    }

    private void deleteUserFromFirestore(User user, TableRow row) {
        String accountType = user.getAccountType();
        String docId = user.getDocId();

        if ("Admin".equals(accountType)) {
            // Check if this is the currently logged-in admin
            String currentAdminEmail = sharedPreferences.getEmail(requireContext());
            String adminEmailToDelete = user.getEmail();

            if (currentAdminEmail != null && currentAdminEmail.equals(adminEmailToDelete)) {
                // Prevent deletion of the currently logged-in admin
                Toast.makeText(getContext(), "You cannot delete your own admin account while logged in.", Toast.LENGTH_LONG).show();
                Log.d("DELETE_ADMIN", "Prevented self-deletion attempt by admin: " + currentAdminEmail);
                return;
            }

            db.collection("Admin")
                .document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    tableLayout.removeView(row);
                    allUsers.remove(user);
                    isDeleteButtonSelected = false;
                    Toast.makeText(getContext(), "Admin account deleted successfully", Toast.LENGTH_SHORT).show();
                    Log.d("DELETE_ADMIN", "Successfully deleted admin: " + adminEmailToDelete);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error deleting admin: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("DELETE_ADMIN", "Failed to delete admin: " + e.getMessage());
                });
        } else if ("Student".equals(accountType)) {
            // Delete student and all duplicates in the same group
            deleteStudentAndDuplicates(user, row);
        } else if ("Teacher".equals(accountType)) {
            // Delete teacher from all possible locations
            deleteTeacherCompletely(user, row);
        }
    }

    private void deleteStudentAndDuplicates(User user, TableRow row) {
        String studentKey = user.getName().toLowerCase().trim() + "_" +
                           user.getGrade().toLowerCase().trim() + "_" +
                           user.getSection().toLowerCase().trim();

        Log.d("DELETE_STUDENT", "Deleting student and duplicates for key: " + studentKey);
        Log.d("DELETE_STUDENT", "Student to delete: " + user.getName() + " (DocId: " + user.getDocId() + ")");

        // Get all document IDs in this duplicate group
        List<String> duplicateDocIds = studentDuplicateGroups.get(studentKey);

        if (duplicateDocIds != null && !duplicateDocIds.isEmpty()) {
            Log.d("DELETE_STUDENT", "Found " + duplicateDocIds.size() + " students to delete: " + duplicateDocIds.toString());

            // Counter to track successful deletions
            final int[] deletedCount = {0};
            final int totalToDelete = duplicateDocIds.size();

            // Delete all students in the duplicate group from Firestore
            for (String docId : duplicateDocIds) {
                Log.d("DELETE_STUDENT", "Deleting document: " + docId);
                db.collection("Accounts")
                    .document("Students")
                    .collection("MathSquare")
                    .document(docId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        deletedCount[0]++;
                        Log.d("DELETE_STUDENT", "Successfully deleted student document: " + docId +
                              " (" + deletedCount[0] + "/" + totalToDelete + ")");

                        // Show final message when all deletions are complete
                        if (deletedCount[0] == totalToDelete) {
                            String message = totalToDelete > 1 ?
                                "Deleted " + totalToDelete + " duplicate students: " + user.getName() :
                                "Deleted student: " + user.getName();
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            Log.d("DELETE_STUDENT", "All student deletions completed for group: " + studentKey);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DELETE_STUDENT", "Failed to delete student document " + docId + ": " + e.getMessage());
                        Toast.makeText(getContext(), "Error deleting some students: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
            }

            // Remove from UI and local lists immediately (don't wait for Firestore)
            tableLayout.removeView(row);
            allUsers.remove(user);

            // Also remove all duplicates from the allStudentsIncludingDuplicates list
            allStudentsIncludingDuplicates.removeIf(student ->
                duplicateDocIds.contains(student.getDocId()));

            isDeleteButtonSelected = false;

        } else {
            // Fallback: delete single student if no duplicate group found
            Log.w("DELETE_STUDENT", "No duplicate group found for " + studentKey + ", deleting single student");
            db.collection("Accounts")
                .document("Students")
                .collection("MathSquare")
                .document(user.getDocId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    tableLayout.removeView(row);
                    allUsers.remove(user);
                    allStudentsIncludingDuplicates.remove(user);
                    isDeleteButtonSelected = false;
                    Toast.makeText(getContext(), "Deleted student: " + user.getName(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error deleting student: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        }
    }

    private void deleteTeacherCompletely(User user, TableRow row) {
        String docId = user.getDocId();
        String email = user.getEmail();

        Log.d("DELETE_TEACHER", "Attempting to delete teacher completely: " + user.getName() + " (" + email + ")");

        // Step 1: Delete from TeacherProfiles collection (if docId doesn't start with "original_")
        if (!docId.startsWith("original_")) {
            db.collection("TeacherProfiles")
                .document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("DELETE_TEACHER", "Deleted from TeacherProfiles: " + docId);
                })
                .addOnFailureListener(e -> {
                    Log.e("DELETE_TEACHER", "Failed to delete from TeacherProfiles: " + e.getMessage());
                });
        }

        // Step 2: Delete from original location Accounts/Teachers/{email}/MyProfile
        if (email != null && !email.equals("N/A")) {
            db.collection("Accounts")
                .document("Teachers")
                .collection(email)
                .document("MyProfile")
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("DELETE_TEACHER", "Deleted from original location: " + email);
                })
                .addOnFailureListener(e -> {
                    Log.e("DELETE_TEACHER", "Failed to delete from original location: " + e.getMessage());
                });
        }

        // Step 3: Delete from TeacherEmailList
        if (email != null && !email.equals("N/A")) {
            String safeEmail = email.replace(".", "_").replace("@", "_at_");
            db.collection("TeacherEmailList")
                .document(safeEmail)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("DELETE_TEACHER", "Deleted from TeacherEmailList: " + safeEmail);
                })
                .addOnFailureListener(e -> {
                    Log.e("DELETE_TEACHER", "Failed to delete from TeacherEmailList: " + e.getMessage());
                });
        }

        // Step 4: Remove from UI and local list
        tableLayout.removeView(row);
        allUsers.remove(user);
        isDeleteButtonSelected = false;

        Toast.makeText(getContext(), "Teacher deleted completely from all locations", Toast.LENGTH_SHORT).show();
        Log.d("DELETE_TEACHER", "Teacher deletion completed: " + user.getName());
    }

    // Method to refresh user data
    private void refreshUserData() {
        Log.d("REFRESH", "Refreshing user data...");
        Log.d("REFRESH", "Current allUsers size before clear: " + allUsers.size());
        allUsers.clear();
        Log.d("REFRESH", "Cleared allUsers, now fetching fresh data...");
        fetchAllUsers();
    }

    // Public method to manually refresh student data for testing
    public void refreshStudentData() {
        Log.d("REFRESH", "Manual refresh of student data requested");
        fetchStudentAccounts();
    }
    
    /**
     * Shows dialog to edit teacher's assigned grade and section
     */
    private void showEditTeacherDialog(User teacher) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_teacher, null);
        
        Spinner gradeSpinner = dialogView.findViewById(R.id.spinnerGrade);
        Spinner sectionSpinner = dialogView.findViewById(R.id.spinnerSection);
        TextView teacherNameText = dialogView.findViewById(R.id.textTeacherName);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        
        teacherNameText.setText(teacher.getName());
        
        // Setup grade spinner
        List<String> grades = Arrays.asList("Select Grade", "1", "2", "3", "4", "5", "6");
        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, grades);
        gradeAdapter.setDropDownViewResource(R.layout.spinner_item);
        gradeSpinner.setAdapter(gradeAdapter);
        
        // Set current grade if assigned
        if (teacher.getGrade() != null && !teacher.getGrade().equals("N/A")) {
            int gradePosition = grades.indexOf(teacher.getGrade());
            if (gradePosition > 0) {
                gradeSpinner.setSelection(gradePosition);
            }
        }
        
        // Load sections when grade is selected
        gradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String selectedGrade = grades.get(position);
                    loadSectionsForGrade(sectionSpinner, selectedGrade, teacher.getSection());
                } else {
                    sectionSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>()));
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Load sections if grade is already set
        if (teacher.getGrade() != null && !teacher.getGrade().equals("N/A")) {
            loadSectionsForGrade(sectionSpinner, teacher.getGrade(), teacher.getSection());
        }
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.RoundedAlertDialog)
            .setView(dialogView)
            .setCancelable(true)
            .create();
        
        // Set dialog window properties for better appearance
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        btnSave.setOnClickListener(v -> {
            int gradePos = gradeSpinner.getSelectedItemPosition();
            int sectionPos = sectionSpinner.getSelectedItemPosition();
            
            if (gradePos == 0) {
                Toast.makeText(requireContext(), "Please select a grade", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (sectionPos == 0 || sectionSpinner.getAdapter().getCount() == 0) {
                Toast.makeText(requireContext(), "Please select a section", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String selectedGrade = grades.get(gradePos);
            String selectedSection = (String) sectionSpinner.getSelectedItem();
            
            // Update teacher in Firebase
            updateTeacherGradeSection(teacher, selectedGrade, selectedSection);
            dialog.dismiss();
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    /**
     * Load sections for a specific grade
     */
    private void loadSectionsForGrade(Spinner sectionSpinner, String grade, String currentSection) {
        int gradeNumber;
        try {
            gradeNumber = Integer.parseInt(grade);
        } catch (NumberFormatException e) {
            return;
        }
        
        db.collection("Sections")
            .whereEqualTo("Grade_Number", gradeNumber)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<String> sections = new ArrayList<>();
                    sections.add("Select Section");
                    
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        String section = doc.getString("Section");
                        if (section != null) {
                            sections.add(section);
                        }
                    }
                    
                    ArrayAdapter<String> sectionAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, sections);
                    sectionAdapter.setDropDownViewResource(R.layout.spinner_item);
                    sectionSpinner.setAdapter(sectionAdapter);
                    
                    // Set current section if assigned
                    if (currentSection != null && !currentSection.equals("N/A")) {
                        int sectionPosition = sections.indexOf(currentSection);
                        if (sectionPosition > 0) {
                            sectionSpinner.setSelection(sectionPosition);
                        }
                    }
                }
            });
    }
    
    /**
     * Update teacher's assigned grade and section in Firebase
     */
    private void updateTeacherGradeSection(User teacher, String grade, String section) {
        db.collection("TeacherProfiles")
            .document(teacher.getDocId())
            .update("assignedGrade", grade, "assignedSection", section)
            .addOnSuccessListener(aVoid -> {
                // Update local user object
                teacher.setGrade(grade);
                teacher.setSection(section);
                
                // Refresh the table
                fetchAllUsers();
                Toast.makeText(requireContext(), "Teacher updated successfully", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Error updating teacher: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible again
        Log.d("LIFECYCLE", "UserFragment onResume - refreshing data");
        Log.d("LIFECYCLE", "Current filter is: " + currentFilter);
        refreshUserData();
    }

}