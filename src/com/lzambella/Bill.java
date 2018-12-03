package com.lzambella;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Bill implements Serializable {
    private Date issueDate;
    private Date dueDate;

    private String company;
    private String description;
    private double amount;

    private User issuer; // the user who issued the bill

    private boolean paymentStatus;  // whether the bill was paid or not
    public Bill(Date issueDate, Date dueDate, String company, String description, double amount, User u) {
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.company = company;
        this.description = description;
        this.amount = amount;
        issuer = u;
        paymentStatus = false;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public String getCompany() {
        return company;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public Object[] toArray() {
        return new Object[] {paymentStatus, issueDate, company, description, amount, dueDate  };
    }

    public User getIssuer() {
        return issuer;
    }
    public boolean isPaid() {
        return paymentStatus;
    }


    /**
     * Set the bill status as paid.
     */
    public void setBillPaid() {
        paymentStatus = true;
    }


}
