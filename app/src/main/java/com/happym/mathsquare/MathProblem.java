package com.happym.mathsquare;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class MathProblem implements Parcelable {

    private String question;
    private String operation;
    private String difficulty;
    private double answer;
    private String choices;
    private double[] givenNumbers;
    private String[] choicesArray;
    private String userAnswer;
    private boolean isCorrect;

    public MathProblem() {}

    public MathProblem(String question, String operation, String difficulty, double answer, String choices) {
        this.question = question;
        this.operation = operation;
        this.difficulty = difficulty;
        this.answer = answer;
        this.choices = choices;
        setGivenNumbers(question);
        setChoicesArray(choices);
    }

    protected MathProblem(Parcel in) {
        question = in.readString();
        operation = in.readString();
        difficulty = in.readString();
        answer = in.readDouble();
        choices = in.readString();
        givenNumbers = in.createDoubleArray();
        choicesArray = in.createStringArray();
        userAnswer = in.readString();
        isCorrect = in.readByte() != 0;
    }

    public static final Creator<MathProblem> CREATOR = new Creator<MathProblem>() {
        @Override
        public MathProblem createFromParcel(Parcel in) {
            return new MathProblem(in);
        }

        @Override
        public MathProblem[] newArray(int size) {
            return new MathProblem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(question);
        dest.writeString(operation);
        dest.writeString(difficulty);
        dest.writeDouble(answer);
        dest.writeString(choices);
        dest.writeDoubleArray(givenNumbers);
        dest.writeStringArray(choicesArray);
        dest.writeString(userAnswer);
        dest.writeByte((byte) (isCorrect ? 1 : 0));
    }

    // Getters and setters
    public String getQuestion() { return question; }

    public void setQuestion(String question) {
        this.question = question;
        setGivenNumbers(question);
    }

    public String getOperation() { return operation; }

    public void setOperation(String operation) { this.operation = operation; }

    public String getDifficulty() { return difficulty; }

    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public double getAnswer() { return answer; }

    public void setAnswer(double answer) { this.answer = answer; }

    public String getChoices() { return choices; }

    public void setChoices(String choices) {
        this.choices = choices;
        setChoicesArray(choices);
    }

    public double[] getGivenNumbers() { return givenNumbers; }

    public void setGivenNumbers(String questionString) {
        String[] questionGivens = questionString.split("\\|\\|\\|");
        double[] numbers = new double[questionGivens.length];
        for (int i = 0; i < questionGivens.length; i++) {
            numbers[i] = Double.parseDouble(questionGivens[i].trim());
        }
        this.givenNumbers = numbers;
    }

    public String[] getChoicesArray() { return choicesArray; }

    public void setChoicesArray(String choicesString) {
        this.choicesArray = choicesString.split(",");
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
        this.isCorrect = String.valueOf(answer).equals(userAnswer);
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public String getCorrectAnswer() {
        // Check if it's a decimal operation
        if (isDecimalOperation()) {
            return String.valueOf(answer);
        } else {
            // For regular operations, remove .0 if the answer is a whole number
            if (answer == (int) answer) {
                return String.valueOf((int) answer);
            } else {
                return String.valueOf(answer);
            }
        }
    }

    private boolean isDecimalOperation() {
        String op = operation.toLowerCase();
        return op.equals("decimal") ||
               op.equals("decimals") ||
               op.equals("decimaladdition") ||
               op.equals("decimalsubtraction") ||
               op.equals("decimalmultiplication") ||
               op.equals("decimaldivision");
    }

    public String getFormattedQuestion() {
        String operationSymbol;
        boolean isDecimalOp = isDecimalOperation();

        // Format given numbers based on operation type
        String num1 = formatNumber(givenNumbers[0], isDecimalOp);
        String num2 = formatNumber(givenNumbers[1], isDecimalOp);

        switch (operation.toLowerCase()) {
            case "addition":
                operationSymbol = "+";
                break;
            case "subtraction":
                operationSymbol = "-";
                break;
            case "multiplication":
                operationSymbol = "×";
                break;
            case "division":
                operationSymbol = "÷";
                break;
            case "percentage":
                // Format as "X% of Y = ?"
                return formatNumber(givenNumbers[0], false) + "% of " + formatNumber(givenNumbers[1], false) + " = ?";
            case "percentages":
                // Alternative spelling support
                return formatNumber(givenNumbers[0], false) + "% of " + formatNumber(givenNumbers[1], false) + " = ?";
            case "decimals":
                // For mixed decimal operations, determine the operation based on the numbers
                return formatMixedDecimalOperation(num1, num2);
            case "decimal":
                return num1 + " + " + num2;
            case "decimaladdition":
                return num1 + " + " + num2;
            case "decimalsubtraction":
                return num1 + " - " + num2;
            case "decimalmultiplication":
                return num1 + " × " + num2;
            case "decimaldivision":
                return num1 + " ÷ " + num2;
            default:
                operationSymbol = "+";
        }
        return num1 + " " + operationSymbol + " " + num2 + " = ?";
    }

    private String formatMixedDecimalOperation(String num1, String num2) {
        // For mixed decimal operations, determine the operation based on the answer
        double n1 = Double.parseDouble(num1);
        double n2 = Double.parseDouble(num2);

        // Check which operation gives us the correct answer (with tolerance for floating point)
        if (Math.abs((n1 + n2) - answer) < 0.001) {
            return num1 + " + " + num2 + " = ?";
        } else if (Math.abs((n1 - n2) - answer) < 0.001) {
            return num1 + " - " + num2 + " = ?";
        } else if (Math.abs((n1 * n2) - answer) < 0.001) {
            return num1 + " × " + num2 + " = ?";
        } else if (n2 != 0 && Math.abs((n1 / n2) - answer) < 0.001) {
            return num1 + " ÷ " + num2 + " = ?";
        } else {
            // If we can't determine, check the closest match
            double addDiff = Math.abs((n1 + n2) - answer);
            double subDiff = Math.abs((n1 - n2) - answer);
            double mulDiff = Math.abs((n1 * n2) - answer);
            double divDiff = n2 != 0 ? Math.abs((n1 / n2) - answer) : Double.MAX_VALUE;

            double minDiff = Math.min(Math.min(addDiff, subDiff), Math.min(mulDiff, divDiff));

            if (minDiff == addDiff) {
                return num1 + " + " + num2 + " = ?";
            } else if (minDiff == subDiff) {
                return num1 + " - " + num2 + " = ?";
            } else if (minDiff == mulDiff) {
                return num1 + " × " + num2 + " = ?";
            } else {
                return num1 + " ÷ " + num2 + " = ?";
            }
        }
    }

    private String formatNumber(double number, boolean isDecimalOperation) {
        if (isDecimalOperation) {
            return String.valueOf(number);
        } else {
            // For regular operations, remove .0 if the number is a whole number
            if (number == (int) number) {
                return String.valueOf((int) number);
            } else {
                return String.valueOf(number);
            }
        }
    }

    public List<String> getFormattedChoices() {
        List<String> formattedChoices = new ArrayList<>();
        boolean isDecimalOp = isDecimalOperation();
        for (String choice : choicesArray) {
            try {
                double choiceValue = Double.parseDouble(choice.trim());
                formattedChoices.add(formatNumber(choiceValue, isDecimalOp));
            } catch (NumberFormatException e) {
                formattedChoices.add(choice);
            }
        }
        return formattedChoices;
    }

    public String[] getFormattedGivenNumbers() {
        boolean isDecimalOp = isDecimalOperation();
        String[] formattedNumbers = new String[givenNumbers.length];
        for (int i = 0; i < givenNumbers.length; i++) {
            formattedNumbers[i] = formatNumber(givenNumbers[i], isDecimalOp);
        }
        return formattedNumbers;
    }

    @Override
    public String toString() {
        return "MathProblem{" +
                "question='" + question + '\'' +
                ", operation='" + operation + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", answer=" + answer +
                ", choices='" + choices + '\'' +
                ", givenNumbers=" + Arrays.toString(givenNumbers) +
                ", choicesArray=" + Arrays.toString(choicesArray) +
                ", userAnswer='" + userAnswer + '\'' +
                ", isCorrect=" + isCorrect +
                '}';
    }
}
