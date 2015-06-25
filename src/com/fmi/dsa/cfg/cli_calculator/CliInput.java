/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fmi.dsa.cfg.cli_calculator;

import com.fmi.dsa.cfg.interfaces.ExpressionInput;
import java.util.Scanner;

/**
 *
 * @author Dimitar
 */
public class CliInput implements ExpressionInput {

    private final Scanner scan;
    private String input;

    public CliInput() {
        scan = new Scanner(System.in);
    }

    @Override
    public String getInput() {
        System.out.print("Enter an expression: ");
        input = scan.nextLine();
        return input;
    }

}
