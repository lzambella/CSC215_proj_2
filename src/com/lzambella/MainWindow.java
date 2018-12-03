package com.lzambella;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

public class MainWindow extends JFrame {
    private JTable dataTable;
    // column names for bills
    private String[] columnNames = {"ID", "Status", "Date issued", "Company", "Description", "Amount", "Due date"};
    private DefaultTableModel model;
    private User authUser;
    private Vector<User> userList;

    public MainWindow(User auth, Vector<User> userList) {
        super();
        authUser = auth;
        this.userList = userList;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Authenticated as " + authUser.getUserName() + ". Funds: " + authUser.getBank().getAmount());

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Settings");
        JMenuItem menuItem = new JMenuItem("Add Funds");

        menuItem.addActionListener(e -> {
            addFundsWindow();
        });

        menu.add(menuItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(600,400));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2)); // have two buttons at the bottom

        dataTable = new JTable(new DefaultTableModel(getData(authUser.getBills()), columnNames));
        dataTable.removeEditor();
        model = (DefaultTableModel)  dataTable.getModel(); // table model for adding new rows dynamically

        JButton payBillButton = new JButton("Pay selected bill");
        JButton sendInvoiceButton = new JButton("Send invoice");

        buttonPanel.add(payBillButton);
        buttonPanel.add(sendInvoiceButton);

        add(dataTable.getTableHeader(), BorderLayout.PAGE_START);
        add(dataTable, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.PAGE_END);
        dataTable.setAutoCreateRowSorter(true);
        payBillButton.addActionListener(e -> {
            int selectedIndex = dataTable.getSelectedRow();
            // get the ID of the bill in the table display
            int bill_ID = (int) model.getValueAt(selectedIndex, 0);

            if (authUser.getBills().get(selectedIndex).isPaid())
                JOptionPane.showMessageDialog(this, String.format("Bill had already been paid."));
            else
                payBillWindow(bill_ID);
        });

        sendInvoiceButton.addActionListener(e -> {
            sendInvoiceWindow();
        });



        setVisible(true);


    }

    /**
     * Creates a window that confirms a bill payment for the selected index
     */
    private void payBillWindow(int index) {
        // Get the selected bill
        Bill tempBill = authUser.getBills().get(index);
        double billAmt = tempBill.getAmount();
        // Get the values and bind to jlabel
        JLabel dateLabel = new JLabel("Issued Date: " + tempBill.getIssueDate().toString());
        JLabel companyLabel = new JLabel("Issuer: " + tempBill.getCompany());
        JLabel descriptionLabel = new JLabel("Description: " + tempBill.getDescription());
        JLabel amountLabel = new JLabel("Bill Amount: " + String.format("%.2f",tempBill.getAmount()));
        JLabel dueDateLabel = new JLabel("Due Date: " + tempBill.getDueDate());

        // get the users current balance
        double balance = authUser.getBank().getAmount();
        JLabel userBalanceLabel = new JLabel(String.format("Balance on account: %.2f", balance));

        JButton payBillButton = new JButton("Pay Selected Bill");

        JFrame frame = new JFrame();
        frame.setLayout(new GridLayout(7,1));
        frame.setMinimumSize(new Dimension(400,600));

        frame.add(dateLabel);
        frame.add(companyLabel);
        frame.add(descriptionLabel);
        frame.add(amountLabel);
        frame.add(dueDateLabel);
        frame.add(userBalanceLabel);
        frame.add(payBillButton);

        frame.setVisible(true);
        // handle button press
        payBillButton.addActionListener(e -> {
            // check if user balance is greater than due amount
            if (balance > billAmt) {
                authUser.getBank().addFunds(-1 * billAmt); // subtract balance from user
                User temp = tempBill.getIssuer();
                temp.getBank().addFunds(billAmt);               // Give the issuer the money
                tempBill.setBillPaid();                         // set the bill as paid
                AppServer.saveData();
                //model.removeRow(index);
                frame.setVisible(false);
            }
        });




    }

    /**
     * Create a window that allows the user to send an invoice to another user
     */
    private void sendInvoiceWindow() {
        JFrame frame = new JFrame("Send invoice");
        JLabel descriptionLabel = new JLabel("Description:");
        JLabel amountLabel = new JLabel("Bill Amount:");
        JLabel receiptantLabel = new JLabel("Receiptant:");
        JTextField descriptionText = new JTextField();
        JTextField amountText = new JTextField();
        JTextField receiptantText = new JTextField();

        JButton sendInvoice = new JButton("Send Invoice");

        frame.setLayout(new GridLayout(4, 2));
        frame.setMinimumSize(new Dimension(400,200));

        frame.add(descriptionLabel);
        frame.add(descriptionText);
        frame.add(receiptantLabel);
        frame.add(receiptantText);
        frame.add(amountLabel);
        frame.add(amountText);

        frame.add(sendInvoice);

        sendInvoice.addActionListener(e -> {
            try {
                String description = descriptionText.getText();
                double amt = Double.parseDouble(amountText.getText());
                User receiptant = userList.stream().filter(s -> s.getUserName().equalsIgnoreCase(receiptantText.getText())).findFirst().get();
                String company = authUser.getUserName();
                Date issueDate = new Date();

                Bill tempBill = new Bill(issueDate, issueDate, company, description, amt, authUser); // create the new bill
                receiptant.issueBill(tempBill); // add the bill to the receiving user
                JOptionPane.showMessageDialog(frame, String.format("Issued the bill successfully"));
                AppServer.saveData();
                frame.setVisible(false);
            } catch (Exception x) {
                System.out.println(x);
            }
        });
        frame.setVisible(true);
    }

    private void addFundsWindow() {
        JFrame frame = new JFrame("Add funds");
        JTextField field = new JTextField();
        JButton button = new JButton("Add funds");
        frame.setMinimumSize(new Dimension(300,75));
        frame.setLayout(new GridLayout(1, 2));
        frame.add(field);
        frame.add(button);
        frame.setVisible(true);
        button.addActionListener(e -> {
            authUser.getBank().addFunds(Double.parseDouble(field.getText()));
            this.setTitle("Authenticated as " + authUser.getUserName() + ". Funds: " + authUser.getBank().getAmount());
            AppServer.saveData();
            frame.setVisible(false);
        });
    }

    /*
    * Gets all the bills for the user
     */
    private Object[][] getData(Vector<Bill> bills) {
        int dataAmount = bills.size(); // Get number of rows datatable will have
        Object[][] data = new Object[dataAmount][7];
        int shamt = 0;

        for (int i = 0; i < dataAmount; i++) {
            // initialize a temp object array to bind data to the data table
            Object[] tmp = new Object[7];
            tmp[0] = i;
            tmp[1] = bills.get(i).isPaid() ? "Paid" : "Not Paid";
            tmp[2] = bills.get(i).getIssueDate();
            tmp[3] = bills.get(i).getCompany();
            tmp[4] = bills.get(i).getDescription();
            tmp[5] = bills.get(i).getAmount();
            tmp[6] = bills.get(i).getDueDate();

            data[i] = tmp;
        }
        return data;
    }
}
