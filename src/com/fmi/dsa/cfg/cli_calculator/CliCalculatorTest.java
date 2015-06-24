/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fmi.dsa.cfg.cli_calculator;

/**
 *
 * @author Dimitar
 */
public class CliCalculatorTest {

    public static void main(String[] args) {
        CliCalculator calculator = new CliCalculator(new CliInput(), new CliOutput());
        calculator.start();
    }

}
