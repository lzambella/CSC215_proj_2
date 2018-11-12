package com.lzambella;

import java.io.Serializable;

public class Bank implements Serializable {

    private String bankID;
    private double bankAmount;

    public Bank (String s, Double d) {
        bankID = s;
        bankAmount = d;
    }

    public void addFunds(double amt) {
        bankAmount += amt;
    }

    public double getAmount() {
        return bankAmount;
    }

    public String getId() {
        return bankID;
    }
}
