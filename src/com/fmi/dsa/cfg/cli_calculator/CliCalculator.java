/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fmi.dsa.cfg.cli_calculator;

import static com.fmi.dsa.cfg.cli_calculator.ReadingState.*;
import com.fmi.dsa.cfg.interfaces.ResultOutput;
import com.fmi.dsa.cfg.interfaces.ExpressionInput;
import static java.lang.Character.isDigit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException;

/**
 * Global TODO: try to make the computation multi-threaded
 *
 *
 * @author Dimitar
 */
public class CliCalculator {

    private final ExpressionInput ei;
    private final ResultOutput ro;
    private String originalExpression = "";

    //TODO: use stacks instead of lists
    private List<Integer> terminal;
    private List<Integer> terminalValues;
    private final List<Character> OPERATORS;
    private int terminalPosition;

    public CliCalculator(ExpressionInput ei, ResultOutput ro) {
        this.OPERATORS = Arrays.asList('(', ')', '+', '-', '*', '/');
        this.ei = ei;
        this.ro = ro;
    }

    public void start() {

        originalExpression = ei.getInput();

        while (!originalExpression.equals("q")) {

            terminal = scanExpression(originalExpression);

            System.out.println(terminal);
            System.out.println(terminalValues);
            int result = evaluateExpression();
            ro.displayResult(String.format("%d", result));
            originalExpression = ei.getInput();

        }
    }

    private List<Integer> scanExpression(String expression) {

        terminal = new ArrayList<>();
        terminalValues = new ArrayList<>();

        ReadingState scanningState = READ_ANYTHING;
        int number = 0;
        int currentPosition = 0;
        int numberPosition = 0;

        terminal.add((int) 'S');

        while (currentPosition < expression.length()) {

            char currentCharacter = expression.charAt(currentPosition);

            switch (scanningState) {

                case READ_ANYTHING:
                    if (isDigit(currentCharacter)) {
                        scanningState = READ_NUMBER;
                        numberPosition = 0;
                    } else if (isOperator(currentCharacter)) {
                        terminal.add((int) currentCharacter);
                        currentPosition++;
                    } else if (currentCharacter == ' ') {
                        currentPosition++;
                    } else {
                        ro.displayResult("Invalid character: " + currentCharacter);
                        return null;
                    }
                    break;

                case READ_NUMBER:
                    if (isDigit(currentCharacter)) {
                        number *= 10;
                        number += Character.getNumericValue(currentCharacter);
                        currentPosition++;
                    } else {
                        terminal.add((int) 'N');
                        terminalValues.add(number);
                        number = 0;
                        scanningState = READ_ANYTHING;
                    }
                    break;
            }
        }

        if (scanningState == READ_NUMBER) {
            terminal.add((int) 'N');
            terminalValues.add(number);
            number = 0;
        }

        terminalPosition = terminal.size() - 1;
        return terminal;
    }

    private int evaluateExpression() {

        int termValue = evaluateTerm();

        if (terminal.get(terminalPosition) == '+') {
            terminalPosition--;

            return evaluateExpression() + termValue;

        }
        if (terminal.get(terminalPosition) == '-') {
            terminalPosition--;
            return evaluateExpression() - termValue;

        }
        return termValue;
    }

    private boolean isOperator(int character) {
        Character c = (char) character;
        return OPERATORS.contains(c);
    }

    private int evaluateTerm() {
        int factorValue = evaluateFactor();

        if (terminal.get(terminalPosition) == '*') {
            terminalPosition--;
            return evaluateTerm() * factorValue;

        }
        if (terminal.get(terminalPosition) == '/') {
            if (factorValue == 0) {
                ro.displayResult("Division by zero");
                throw new IllegalArgumentException("Division by zero");
            }
            terminalPosition--;
            return evaluateTerm() / factorValue;

        }
        return factorValue;

    }

    private int evaluateFactor() {
        int numberValue = 0;

        if (terminal.get(terminalPosition) == 'N') {
            numberValue = terminalValues.get(terminalValues.size() - 1);
            terminalValues.remove(terminalValues.size() - 1);
            terminalPosition--;
        } else if (terminal.get(terminalPosition) == ')') {
            terminalPosition--;
            numberValue = evaluateExpression();
            if (terminal.get(terminalPosition) == '(') {
                terminalPosition--;
            } else {
                ro.displayResult("Syntax Error");
                throw new SyntaxException("Syntax Error!");
            }
        } else {
            ro.displayResult("Syntax Error");
            throw new SyntaxException("Syntax Error!");
        }

        if (terminal.get(terminalPosition) == '-' && terminal.get(terminalPosition - 1) != 'N' && terminal.get(terminalPosition - 1) != ')') {
            terminalPosition--;
            return -numberValue;
        }
        return numberValue;
    }

}
