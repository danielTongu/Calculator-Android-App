package com.example.calculator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Exercises keypad behavior and arithmetic without an Android runtime. */
public class CalculatorEngineTest {
    /** Sends the same single-character tokens as the keypad. */
    private CalculatorEngine enter(String expression) {
        CalculatorEngine engine = new CalculatorEngine();
        type(engine, expression);
        return engine;
    }

    /** Adds a sequence of keypad tokens to an existing calculation. */
    private void type(CalculatorEngine engine, String expression) {
        for (int index = 0; index < expression.length(); index++) {
            engine.append(String.valueOf(expression.charAt(index)));
        }
    }

    /** Multiplication precedes addition and parentheses override precedence. */
    @Test
    public void respectsPrecedenceAndParentheses() {
        CalculatorEngine engine = enter("2+3*4");
        engine.calculate();
        assertEquals("14", engine.getResult());
        engine = enter("(2+3)*4");
        engine.calculate();
        assertEquals("20", engine.getResult());
    }

    /** Clear and deletion cannot leave a stale operator flag. */
    @Test
    public void clearAndDeleteResumeInput() {
        CalculatorEngine engine = enter("2+");
        engine.clear();
        type(engine, "3+4");
        engine.calculate();
        assertEquals("7", engine.getResult());
        engine = enter("2+");
        engine.delete();
        type(engine, "*3");
        engine.calculate();
        assertEquals("6", engine.getResult());
    }

    /** Decimal validation preserves valid numbers and normalizes their display. */
    @Test
    public void handlesDecimalsAndFloatingPointArtifacts() {
        CalculatorEngine engine = enter("0.1+0.2");
        engine.calculate();
        assertEquals("0.3", engine.getResult());
        engine = enter(".2.3");
        assertEquals("0.23", engine.getExpression());
    }

    /** Unary minus works at the beginning and after multiplication. */
    @Test
    public void supportsNegativeOperands() {
        CalculatorEngine engine = enter("-2*-3");
        engine.calculate();
        assertEquals("6", engine.getResult());
    }

    /** Pending operators can be replaced and adjacent brackets imply multiplication. */
    @Test
    public void replacesOperatorsAndMultipliesBrackets() {
        CalculatorEngine engine = enter("2+*3");
        engine.calculate();
        assertEquals("6", engine.getResult());
        engine = enter("2(3+4)");
        engine.calculate();
        assertEquals("14", engine.getResult());
    }

    /** Invalid arithmetic produces an error while retaining an editable expression. */
    @Test
    public void reportsAndRecoversFromErrors() {
        CalculatorEngine engine = enter("1/0");
        engine.calculate();
        assertTrue(engine.hasError());
        engine.delete();
        type(engine, "2");
        engine.calculate();
        assertFalse(engine.hasError());
        assertEquals("0.5", engine.getResult());
        engine = enter("(2+3");
        engine.calculate();
        assertTrue(engine.hasError());
    }

    /** Equals chains from the result while a digit starts a fresh expression. */
    @Test
    public void continuesOrStartsAfterResult() {
        CalculatorEngine engine = enter("2+3");
        engine.calculate();
        type(engine, "*4");
        engine.calculate();
        assertEquals("20", engine.getResult());
        type(engine, "+1");
        engine.calculate();
        assertEquals("21", engine.getResult());
        type(engine, "7");
        assertEquals("7", engine.getExpression());
    }

    /** Restoring a completed calculation preserves continuation behavior. */
    @Test
    public void restoresCalculationState() {
        CalculatorEngine original = enter("3+4");
        original.calculate();
        CalculatorEngine restored = new CalculatorEngine();
        restored.restore(original.getExpression(), original.getResult(),
                original.isEvaluated(), original.hasError());
        type(restored, "*2");
        restored.calculate();
        assertEquals("14", restored.getResult());
    }

    /** Empty equals is harmless and unmatched closing parentheses are ignored. */
    @Test
    public void ignoresEmptyEqualsAndUnmatchedClosingBracket() {
        CalculatorEngine engine = enter(")2)");
        assertEquals("2", engine.getExpression());
        engine.clear();
        engine.calculate();
        assertFalse(engine.hasError());
        assertEquals("0", engine.getResult());
    }
}
