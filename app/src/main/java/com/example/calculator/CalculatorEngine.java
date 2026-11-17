package com.example.calculator;

import java.math.BigDecimal;
import java.math.MathContext;

import net.objecthunter.exp4j.ExpressionBuilder;

/** Owns calculator input and results independently of Android views. */
public final class CalculatorEngine {
    private static final int MAX_INPUT_LENGTH = 256;
    private final StringBuilder input = new StringBuilder();
    private String result = "0";
    private boolean evaluated;
    private boolean error;

    /** Returns the expression exactly as entered. */
    public String getExpression() {
        return input.toString();
    }

    /** Returns the last successful result, without UI error messages. */
    public String getResult() {
        return result;
    }

    /** Indicates that the current expression could not be evaluated. */
    public boolean hasError() {
        return error;
    }

    /** Indicates that the next digit should start a new calculation. */
    public boolean isEvaluated() {
        return evaluated;
    }

    /** Restores the bounded state previously saved by the activity. */
    public void restore(String expression, String savedResult, boolean wasEvaluated,
                        boolean hadError) {
        input.setLength(0);
        if (expression != null && expression.length() <= MAX_INPUT_LENGTH) {
            input.append(expression);
        }
        result = savedResult == null ? "0" : savedResult;
        evaluated = wasEvaluated;
        error = hadError;
    }

    /** Clears both the expression and all calculation flags. */
    public void clear() {
        input.setLength(0);
        result = "0";
        evaluated = false;
        error = false;
    }

    /** Deletes one expression character and resumes editing. */
    public void delete() {
        if (input.length() > 0) {
            input.deleteCharAt(input.length() - 1);
        }
        evaluated = false;
        error = false;
    }

    /** Applies one keypad token while preventing malformed expression prefixes. */
    public void append(String token) {
        if (token != null && token.length() == 1 && "0123456789.+-*/()".contains(token)) {
            char value = token.charAt(0);
            if (evaluated) {
                input.setLength(0);
                if (isOperator(value) || value == ')') {
                    input.append(result);
                }
                evaluated = false;
            }
            error = false;

            // Leave room for implicit multiplication or a leading zero.
            if (input.length() < MAX_INPUT_LENGTH - 2) {
                if (Character.isDigit(value)) {
                    if (lastCharacter() == ')') {
                        input.append('*');
                    }
                    input.append(value);
                } else if (value == '.') {
                    appendDecimal();
                } else if (value == '(') {
                    if (Character.isDigit(lastCharacter()) || lastCharacter() == ')') {
                        input.append('*');
                    }
                    input.append('(');
                } else if (value == ')') {
                    if (canEndNumber() && unmatchedBrackets() > 0) {
                        input.append(')');
                    }
                } else {
                    appendOperator(value);
                }
            }
        }
    }

    /** Adds a decimal separator only once within the current number. */
    private void appendDecimal() {
        int index = input.length() - 1;
        while (index >= 0 && Character.isDigit(input.charAt(index))) {
            index--;
        }
        if (index < 0 || input.charAt(index) != '.') {
            if (lastCharacter() == ')') {
                input.append('*');
            }
            if (!Character.isDigit(lastCharacter())) {
                input.append('0');
            }
            input.append('.');
        }
    }

    /** Allows unary minus and replaces a pending binary operator. */
    private void appendOperator(char operator) {
        if (canEndNumber()) {
            input.append(operator);
        } else if (operator == '-' && (input.length() == 0 || lastCharacter() == '('
                || lastCharacter() == '*' || lastCharacter() == '/' || lastCharacter() == '+')) {
            input.append('-');
        } else if (isOperator(lastCharacter()) && input.length() > 1 &&
                (Character.isDigit(input.charAt(input.length() - 2)) || input.charAt(input.length() - 2) == ')')) {
            input.setCharAt(input.length() - 1, operator);
        }
    }

    /** Evaluates complete expressions and formats finite results to 12 significant digits. */
    public void calculate() {
        if (input.length() > 0) {
            try {
                if (!canEndNumber() || unmatchedBrackets() != 0) {
                    throw new IllegalArgumentException("Incomplete expression");
                }

                double value = new ExpressionBuilder(input.toString()).build().evaluate();
                if (Double.isNaN(value) || Double.isInfinite(value)) {
                    throw new ArithmeticException("Non-finite result");
                }

                BigDecimal rounded = BigDecimal.valueOf(value).round(new MathContext(12)).stripTrailingZeros();
                if (rounded.scale() <= 12 && rounded.precision() - rounded.scale() <= 12) {
                    result = rounded.toPlainString();
                } else {
                    result = rounded.toString();
                }

                evaluated = true;
                error = false;
            } catch (IllegalArgumentException | ArithmeticException exception) {
                error = true;
                evaluated = false;
            }
        }
    }

    /** Returns the trailing character or a sentinel for empty input. */
    private char lastCharacter() {
        return input.length() == 0 ? '\0' : input.charAt(input.length() - 1);
    }

    /** Checks whether the expression currently ends in a complete operand. */
    private boolean canEndNumber() {
        return Character.isDigit(lastCharacter()) || lastCharacter() == ')';
    }

    /** Counts open parentheses that still need closing. */
    private int unmatchedBrackets() {
        int count = 0;
        for (int index = 0; index < input.length(); index++) {
            if (input.charAt(index) == '(') {
                count++;
            } else if (input.charAt(index) == ')') {
                count--;
            }
        }
        return count;
    }

    /** Identifies the four supported arithmetic operators. */
    private static boolean isOperator(char value) {
        return value == '+' || value == '-' || value == '*' || value == '/';
    }
}
