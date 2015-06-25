/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fmi.dsa.cfg.cli_calculator;

import com.fmi.dsa.cfg.interfaces.ResultOutput;

/**
 *
 * @author Dimitar
 */
public class CliOutput implements ResultOutput {

    @Override
    public void displayResult(String result) {
        System.out.println("Your result is: " + result);
    }

}
