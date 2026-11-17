package com.example.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Connects the calculator keypad and displays to the calculation engine. */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final CalculatorEngine calculator = new CalculatorEngine();
    private TextView computationDisplay;
    private TextView resultDisplay;

    /** Connects keypad actions and restores any interrupted calculation. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        computationDisplay = findViewById(R.id.computation_display);
        resultDisplay = findViewById(R.id.result_display);

        int[] buttons = {
                R.id.button0,
                R.id.button1,
                R.id.button2,
                R.id.button3,
                R.id.button4,
                R.id.button5,
                R.id.button6,
                R.id.button7,
                R.id.button8,
                R.id.button9,
                R.id.buttonAdd,
                R.id.buttonSubtract,
                R.id.buttonMultiply,
                R.id.buttonDivide,
                R.id.buttonOpenBracket,
                R.id.buttonCloseBracket,
                R.id.buttonDecimal,
                R.id.buttonEquals,
                R.id.buttonAC,
                R.id.buttonC
        };

        for (int button : buttons) {
            findViewById(button).setOnClickListener(this);
        }

        if (savedInstanceState != null) {
            calculator.restore(
                    savedInstanceState.getString("expression"),
                    savedInstanceState.getString("result"),
                    savedInstanceState.getBoolean("evaluated"),
                    savedInstanceState.getBoolean("error")
            );
        }
        render();
    }

    /** Routes clear, delete, evaluate, and input actions through the same state owner. */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.buttonAC) {
            calculator.clear();
        } else if (view.getId() == R.id.buttonC) {
            calculator.delete();
        } else if (view.getId() == R.id.buttonEquals) {
            calculator.calculate();
        } else {
            calculator.append((String) view.getTag());
        }
        render();
    }

    /** Saves engine state for rotation and system-initiated activity recreation. */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("expression", calculator.getExpression());
        outState.putString("result", calculator.getResult());
        outState.putBoolean("evaluated", calculator.isEvaluated());
        outState.putBoolean("error", calculator.hasError());
        super.onSaveInstanceState(outState);
    }

    /** Renders mathematical symbols and a localized, accessible error message. */
    private void render() {
        computationDisplay.setText(calculator.getExpression().replace('*', '×').replace('/', '÷'));
        if (calculator.hasError()) {
            resultDisplay.setText(R.string.calculation_error);
        } else {
            resultDisplay.setText(calculator.getResult());
        }
    }
}
