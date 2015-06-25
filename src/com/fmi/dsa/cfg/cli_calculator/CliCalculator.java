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
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
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

    private Stack<Integer> terminal;
    private Stack<Integer> terminalValues;
    private final List<Character> OPERATORS;

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

    private Stack<Integer> scanExpression(String expression) {

        terminal = new Stack<>();
        terminalValues = new Stack<>();

        ReadingState scanningState = READ_ANYTHING;
        int number = 0;
        int currentPosition = 0;

        terminal.add((int) 'S');

        while (currentPosition < expression.length()) {

            char currentCharacter = expression.charAt(currentPosition);

            switch (scanningState) {

                case READ_ANYTHING:
                    if (isDigit(currentCharacter)) {
                        scanningState = READ_NUMBER;
                    } else if (isOperator(currentCharacter)) {
                        terminal.push((int) currentCharacter);
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
                        terminal.push((int) 'N');
                        terminalValues.push(number);
                        number = 0;
                        scanningState = READ_ANYTHING;
                    }
                    break;
            }
        }

        if (scanningState == READ_NUMBER) {
            terminal.push((int) 'N');
            terminalValues.push(number);
            number = 0;
        }
        
        return terminal;
    }

    private int evaluateExpression() {

        int termValue = evaluateTerm();

        if (terminal.peek() == '+') {
            terminal.pop();

            return evaluateExpression() + termValue;

        }
        if (terminal.peek() == '-') {
            terminal.pop();
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

        if (terminal.peek() == '*') {
            terminal.pop();
            return evaluateTerm() * factorValue;

        }
        if (terminal.peek() == '/') {
            if (factorValue == 0) {
                ro.displayResult("Division by zero");
                throw new IllegalArgumentException("Division by zero");
            }
            terminal.peek();
            return evaluateTerm() / factorValue;

        }
        return factorValue;

    }

    private int evaluateFactor() {
        int numberValue = 0;

        if (terminal.peek() == 'N') {
            numberValue = terminalValues.get(terminalValues.size() - 1);
            terminalValues.remove(terminalValues.size() - 1);
            terminal.pop();
        } else if (terminal.peek() == ')') {
            terminal.pop();
            numberValue = evaluateExpression();
            if (terminal.peek() == '(') {
                terminal.pop();
            } else {
                ro.displayResult("Syntax Error");
                throw new SyntaxException("Syntax Error!");
            }
        } else {
            ro.displayResult("Syntax Error");
            throw new SyntaxException("Syntax Error!");
        }

        if (terminal.peek() == '-' && terminal.get(terminal.size() - 2) != 'N' && terminal.get(terminal.size() - 2) != ')') {
            terminal.pop();
            return -numberValue;
        }
        return numberValue;
    }

}
