package com.happym.mathsquare;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CSVProcessor extends AppCompatActivity {

    public List<MathProblem> readCSVFile(BufferedReader bufferedReader) {
        List<MathProblem> mathProblemList = new ArrayList<>();
        try {

            CSVParser csvParser = new CSVParser(bufferedReader,
                    CSVFormat.Builder.create()
                            .setIgnoreHeaderCase(true)
                            .setTrim(true)
                            .setHeader()
                            .setSkipHeaderRecord(true)
                            .setRecordSeparator(',').build());
            for (CSVRecord csvRecord : csvParser) {
                // Extracting values from the CSVRecord
                String question = csvRecord.get(0);
                String operation = csvRecord.get(1);
                String difficulty = csvRecord.get(2);
                double answer = Double.parseDouble(csvRecord.get(3));
                String choices = csvRecord.get(4);
                MathProblem mathProblem = new MathProblem();
                mathProblem.setQuestion(question);
                mathProblem.setOperation(operation);
                mathProblem.setDifficulty(difficulty);
                mathProblem.setAnswer(answer);
                mathProblem.setChoices(choices);

                mathProblemList.add(mathProblem);
            }
            // Close the CSVParser
            csvParser.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("done processing csv");
        return mathProblemList;
    }

    public List<MathProblem> getProblemsByOperation(List<MathProblem> mathProblemList, String difficultyText) {
        List<MathProblem> mathProblems = new ArrayList<>();
        for (MathProblem problem : mathProblemList) {
            if (problem.getDifficulty().equalsIgnoreCase(difficultyText)) {
                mathProblems.add(problem);
            }
        }
        // Shuffle the problems before returning
        Collections.shuffle(mathProblems);
        return mathProblems;
    }

    /**
     * Get problems by operation and difficulty, excluding already used questions
     */
    public List<MathProblem> getProblemsByOperationExcludingUsed(
            List<MathProblem> mathProblemList,
            String difficultyText,
            String operation,
            Context context) {

        QuestionTracker questionTracker = new QuestionTracker(context);
        Set<String> usedQuestions = questionTracker.getUsedQuestions(operation, difficultyText);

        android.util.Log.d("CSVProcessor", "Filtering for operation: " + operation + ", difficulty: " + difficultyText);
        android.util.Log.d("CSVProcessor", "Total problems in list: " + mathProblemList.size());

        List<MathProblem> mathProblems = new ArrayList<>();
        for (MathProblem problem : mathProblemList) {
            // Filter by both difficulty AND operation
            if (problem.getDifficulty().equalsIgnoreCase(difficultyText) &&
                problem.getOperation().equalsIgnoreCase(operation)) {
                String questionId = QuestionTracker.generateQuestionId(problem);
                // Only add if not used before
                if (!usedQuestions.contains(questionId)) {
                    mathProblems.add(problem);
                    android.util.Log.d("CSVProcessor", "Added problem: " + problem.getQuestion() + " (" + problem.getOperation() + ")");
                }
            }
        }

        android.util.Log.d("CSVProcessor", "Found " + mathProblems.size() + " matching problems");

        // Shuffle the problems before returning
        Collections.shuffle(mathProblems);
        return mathProblems;
    }

    /**
     * Mark questions as used after they have been presented to the user
     */
    public void markQuestionsAsUsed(List<MathProblem> usedProblems, String operation, String difficulty, Context context) {
        QuestionTracker questionTracker = new QuestionTracker(context);
        Set<String> questionIds = new HashSet<>();

        for (MathProblem problem : usedProblems) {
            String questionId = QuestionTracker.generateQuestionId(problem);
            questionIds.add(questionId);
        }

        questionTracker.addUsedQuestions(operation, difficulty, questionIds);
    }
}
