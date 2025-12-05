# Student Quiz Layout & CSV Question Consistency Analysis

## 📋 Overview
This document analyzes how quizzes are displayed on the student side and how CSV questions are consistently formatted and displayed.

---

## 🎯 Student Quiz Layout Structure

### 1. **Quiz Selection Screen** (`QuizzesSection.java` / `layout_quizzes_section.xml`)

**Layout Components:**
- **Title**: "Quizzess" (header bar)
- **Quiz Level Display**: Circular badge showing quiz number (1-6)
- **Grade Level Text**: "Grade X Quiz"
- **Start Quiz Button**: "START QUIZ" (can be locked/closed/open)

**Features:**
- Quiz level determined by student's grade
- Real-time Firebase listener for quiz status (open/closed)
- Sequential quiz locking (Quiz 2 locked until Quiz 1 completed)
- Practice prerequisite checking

**Quiz Assignment by Grade:**
```
Grade 1 → Quiz 1 (Easy)
Grade 2 → Quiz 2 (Easy)
Grade 3 → Quiz 3 (Medium)
Grade 4 → Quiz 4 (Medium)
Grade 5 → Quiz 5 (Medium)
Grade 6 → Quiz 6 (Hard)
```

---

### 2. **Quiz Taking Screen** (`MultipleChoicePage.java` / `activity_multiplechoice.xml`)

**Layout Structure:**

#### **Top Bar:**
- **Home Button** (left)
- **Operation Display** (center) - Shows current operation (e.g., "Multiplication")
- **Pause Button** (right)

#### **Game Stats Bar:**
- **Lives Display** (left): Heart icon + count (e.g., "3")
- **Question Progress** (center): "X/5" or "X/Y" 
- **Timer Display** (right): Clock icon + time (e.g., "10:00")

#### **Question Display Area:**
- **Blackboard Background** (300x300dp)
  - **First Number** (`text_givenone`): Top right-aligned
  - **Operator Symbol** (`text_operator`): +, -, ×, ÷, "of" (for percentage)
  - **Second Number** (`text_giventwo`): Below operator, right-aligned
  - **Equals Line**: Horizontal white line
  - **Equals Sign** (`equal`): "=" at bottom

#### **Answer Choices:**
- **4 Choice Buttons** (2x2 grid):
  - `btn_choice1` (top-left)
  - `btn_choice2` (top-right)
  - `btn_choice3` (bottom-left)
  - `btn_choice4` (bottom-right)
- Each button: 100x100dp, white text, shadow effects

#### **Feedback Area:**
- **Feedback TextView** (`text_feedback`): Shows correct/incorrect messages (initially invisible)

---

## 📊 CSV Question Format & Consistency

### **CSV File Structure:**
CSV files are located in `app/src/main/assets/`:
- `additionProblemSet.csv`
- `subtractionProblemSet.csv`
- `multiplicationProblemSet.csv`
- `divisionProblemSet.csv`
- `percentageProblemSet.csv`
- `decimalProblemSet.csv`
- `decimalAdditionProblemSet.csv`
- `decimalSubtractionProblemSet.csv`
- `decimalMultiplicationProblemSet.csv`
- `decimalDivisionProblemSet.csv`
- `problemSet.csv` (used for percentage and mixed decimals)

### **CSV Column Format:**
```csv
question,operation,difficulty,answer,choices
```

**Example:**
```csv
5|||3,Addition,Easy,8,"8,5,10,12"
```

### **Column Details:**

1. **Question** (Column 0):
   - Format: `"num1|||num2"`
   - Example: `"5|||3"` → displays as "5 + 3 = ?"
   - Parsed by `MathProblem.setGivenNumbers()` using `split("\\|\\|\\|")`

2. **Operation** (Column 1):
   - Values: `Addition`, `Subtraction`, `Multiplication`, `Division`, `Percentage`, `Decimals`, `DecimalAddition`, `DecimalSubtraction`, `DecimalMultiplication`, `DecimalDivision`
   - Determines operator symbol displayed

3. **Difficulty** (Column 2):
   - Values: `Easy`, `Medium`, `Hard`
   - Used for filtering questions by grade level

4. **Answer** (Column 3):
   - Numeric value (can be decimal)
   - Example: `8` or `8.5`

5. **Choices** (Column 4):
   - Comma-separated string
   - Example: `"8,5,10,12"`
   - Parsed by `MathProblem.setChoicesArray()` using `split(",")`
   - Shuffled before display

---

## 🔄 Question Display Flow

### **Step 1: CSV Loading** (`CSVProcessor.readCSVFile()`)
```java
// Reads CSV file from assets
// Parses each row into MathProblem object
// Returns List<MathProblem>
```

### **Step 2: Filtering** (`CSVProcessor.getProblemsByOperationExcludingUsed()`)
```java
// Filters by operation AND difficulty
// Excludes previously used questions (tracked in SharedPreferences)
// Auto-recycles if not enough questions available
```

### **Step 3: Question Selection** (`MultipleChoicePage.setupProblemSetList()`)
```java
// Selects 5 questions per operation for quizzes
// Shuffles the selected questions
// Creates problemSet list
```

### **Step 4: Display** (`MultipleChoicePage.generateNewQuestion()` / `generateNewQuestionList()`)

**For Regular Operations:**
```java
// Parse question: "5|||3" → [5, 3]
givenOneTextView.setText("5");
givenTwoTextView.setText("3");
text_operator.setText("+"); // Set based on operation
```

**For Percentage:**
```java
// Format: "50% of 10"
givenOneTextView.setText("50%");
givenTwoTextView.setText("10");
text_operator.setText("of");
```

**For Decimals:**
```java
// Auto-detect operation by checking answer
// Format: "5.5 + 3.2" or "5.5 - 3.2", etc.
givenOneTextView.setText("5.5");
givenTwoTextView.setText("3.2");
text_operator.setText("+"); // Determined by answer calculation
```

**For Choices:**
```java
// Parse: "8,5,10,12" → ["8", "5", "10", "12"]
// Shuffle and assign to 4 buttons
btnChoice1.setText("8");
btnChoice2.setText("5");
btnChoice3.setText("10");
btnChoice4.setText("12");
```

---

## ✅ Consistency Points

### **1. Question Format Consistency:**
- ✅ All CSV questions use `"num1|||num2"` format
- ✅ Consistent parsing using `split("\\|\\|\\|")`
- ✅ Numbers stored as `double[]` in `MathProblem.givenNumbers`

### **2. Display Consistency:**
- ✅ All questions displayed on same blackboard layout
- ✅ Consistent number formatting (removes `.0` for whole numbers)
- ✅ Decimal operations preserve decimal formatting
- ✅ Percentage operations use special "X% of Y" format

### **3. Operation Symbol Consistency:**
- ✅ Addition: `+`
- ✅ Subtraction: `-`
- ✅ Multiplication: `×`
- ✅ Division: `÷`
- ✅ Percentage: `of`
- ✅ Decimals: Auto-detected based on answer

### **4. Choice Display Consistency:**
- ✅ Always 4 choices
- ✅ Always shuffled before display
- ✅ Consistent button styling (100x100dp, white text, shadow)
- ✅ Choices formatted same as numbers (remove `.0` for whole numbers)

### **5. Progress Tracking Consistency:**
- ✅ Quiz mode: Shows "X/5" per operation
- ✅ Practice mode: Shows "X/Y" total questions
- ✅ Consistent progress calculation

---

## 🎨 Visual Layout Summary

```
┌─────────────────────────────────────┐
│ [Home]  Operation Display  [Pause]  │
├─────────────────────────────────────┤
│ Lives: 3    Progress: 2/5    Timer  │
├─────────────────────────────────────┤
│                                     │
│         ┌─────────────┐           │
│         │   Blackboard │           │
│         │              │           │
│         │     55       │           │
│         │      +       │           │
│         │    999       │           │
│         │   ─────      │           │
│         │     =        │           │
│         └─────────────┘           │
│                                     │
│      [7]        [15]               │
│                                     │
│    [777]        [50]               │
│                                     │
└─────────────────────────────────────┘
```

---

## 🔍 Key Files Reference

### **Layout Files:**
- `layout_quizzes_section.xml` - Quiz selection screen
- `activity_multiplechoice.xml` - Quiz taking screen

### **Java Files:**
- `QuizzesSection.java` - Quiz selection logic
- `MultipleChoicePage.java` - Quiz taking logic
- `CSVProcessor.java` - CSV parsing and filtering
- `MathProblem.java` - Question data model

### **CSV Files:**
- Located in `app/src/main/assets/`
- 11 different CSV files for different operations

---

## 📝 Notes

1. **Question Recycling**: If not enough unused questions available, system automatically clears used questions and recycles them.

2. **Operation Switching**: In quiz mode, switches operations every 5 questions (5 questions per operation).

3. **Special Formatting**: Percentage and decimal operations have special display logic to handle their unique formats.

4. **Answer Validation**: Uses epsilon comparison (0.01) for double precision when checking answers.

5. **Timer & Lives**: Quiz mode has timer and lives system (3 lives by default).

---

## ✅ Conclusion

The student quiz layout is **consistent** across all CSV questions:
- ✅ Same blackboard display format
- ✅ Consistent question parsing (`|||` separator)
- ✅ Consistent choice display (4 shuffled buttons)
- ✅ Consistent number formatting
- ✅ Consistent operation symbol display
- ✅ Consistent progress tracking

All CSV questions follow the same format and are displayed using the same UI components, ensuring a uniform user experience.

