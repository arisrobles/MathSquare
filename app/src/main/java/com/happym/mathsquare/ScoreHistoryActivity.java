package com.happym.mathsquare;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.happym.mathsquare.sharedPreferences;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity to display score history with date and time
 * Implements defense comment #2: History of (score) - Attempt (date & time)
 */
public class ScoreHistoryActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private ScoreHistoryAdapter adapter;
    private FirebaseFirestore db;
    private List<ScoreHistoryItem> historyItems;
    private TextView emptyStateText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_score_history);
        
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        
        recyclerView = findViewById(R.id.recyclerViewHistory);
        emptyStateText = findViewById(R.id.emptyStateText);
        
        historyItems = new ArrayList<>();
        adapter = new ScoreHistoryAdapter(historyItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        loadScoreHistory();
    }
    
    private void loadScoreHistory() {
        String firstName = sharedPreferences.getFirstN(this);
        String lastName = sharedPreferences.getLastN(this);
        String grade = sharedPreferences.getGrade(this);
        
        if (firstName == null || lastName == null) {
            Toast.makeText(this, "Student information not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Load all game types
        db.collection("Accounts")
            .document("Students")
            .collection("MathSquare")
            .whereEqualTo("firstName", firstName)
            .whereEqualTo("lastName", lastName)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    historyItems.clear();
                    
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        String gameType = doc.getString("gameType");
                        String score = doc.getString("quizscore");
                        String quizNo = doc.getString("quizno");
                        Date timestamp = doc.getDate("timestamp");
                        
                        // Get attempt number (default to 1 if not set for backwards compatibility)
                        Long attemptNumLong = doc.getLong("attemptNumber");
                        int attemptNumber = attemptNumLong != null ? attemptNumLong.intValue() : 1;
                        
                        if (timestamp != null && score != null) {
                            String dateStr = dateFormat.format(timestamp);
                            String timeStr = timeFormat.format(timestamp);
                            
                            // Format attempt number (1st, 2nd, 3rd, etc.)
                            String attemptStr = formatAttemptNumber(attemptNumber);
                            
                            ScoreHistoryItem item = new ScoreHistoryItem(
                                gameType != null ? gameType : "Unknown",
                                quizNo != null ? quizNo : "N/A",
                                score,
                                dateStr,
                                timeStr,
                                timestamp,
                                attemptStr
                            );
                            historyItems.add(item);
                        }
                    }
                    
                    adapter.notifyDataSetChanged();
                    
                    if (historyItems.isEmpty()) {
                        emptyStateText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyStateText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(this, "Error loading history: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                        Toast.LENGTH_LONG).show();
                }
            });
    }
    
    private String formatAttemptNumber(int attemptNumber) {
        if (attemptNumber % 100 >= 11 && attemptNumber % 100 <= 13) {
            return attemptNumber + "th Attempt";
        }
        switch (attemptNumber % 10) {
            case 1: return attemptNumber + "st Attempt";
            case 2: return attemptNumber + "nd Attempt";
            case 3: return attemptNumber + "rd Attempt";
            default: return attemptNumber + "th Attempt";
        }
    }
    
    public static class ScoreHistoryItem {
        private String gameType;
        private String quizNo;
        private String score;
        private String date;
        private String time;
        private Date timestamp;
        private String attemptNumber;
        
        public ScoreHistoryItem(String gameType, String quizNo, String score, 
                                String date, String time, Date timestamp, String attemptNumber) {
            this.gameType = gameType;
            this.quizNo = quizNo;
            this.score = score;
            this.date = date;
            this.time = time;
            this.timestamp = timestamp;
            this.attemptNumber = attemptNumber;
        }
        
        public String getGameType() { return gameType; }
        public String getQuizNo() { return quizNo; }
        public String getScore() { return score; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public Date getTimestamp() { return timestamp; }
        public String getAttemptNumber() { return attemptNumber; }
    }
    
    // Simple adapter class (you'll need to create the layout)
    private static class ScoreHistoryAdapter extends RecyclerView.Adapter<ScoreHistoryAdapter.ViewHolder> {
        private List<ScoreHistoryItem> items;
        
        public ScoreHistoryAdapter(List<ScoreHistoryItem> items) {
            this.items = items;
        }
        
        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_score_history, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ScoreHistoryItem item = items.get(position);
            holder.gameTypeText.setText(item.getGameType());
            holder.quizNoText.setText(item.getQuizNo() + " - " + item.getAttemptNumber());
            holder.scoreText.setText("Score: " + item.getScore());
            holder.dateText.setText("Date: " + item.getDate());
            holder.timeText.setText("Time: " + item.getTime());
        }
        
        @Override
        public int getItemCount() {
            return items.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView gameTypeText, quizNoText, scoreText, dateText, timeText;
            
            ViewHolder(View itemView) {
                super(itemView);
                gameTypeText = itemView.findViewById(R.id.textGameType);
                quizNoText = itemView.findViewById(R.id.textQuizNo);
                scoreText = itemView.findViewById(R.id.textScore);
                dateText = itemView.findViewById(R.id.textDate);
                timeText = itemView.findViewById(R.id.textTime);
            }
        }
    }
}

