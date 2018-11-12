package com.lzambella;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

class AppServer {

    private static Vector<User> userList = new Vector<User>();
    private User authenticatedUser;

    /*
    * Authentication window
    */
    public void initAuthentication() {
        loadData();
        JFrame frame = new JFrame("Login");
        frame.setMinimumSize(new Dimension(300,100));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextField uName = new JTextField();
        JPasswordField pWord = new JPasswordField();
        JButton submit = new JButton("Submit");

        JLabel label1 = new JLabel("Username:");
        JLabel label2 = new JLabel("Password:");

        submit.addActionListener(e -> {
            // If the built in account has been used
            if (uName.getText().equalsIgnoreCase("root") && Arrays.equals("toor".toCharArray(), pWord.getPassword())) {
                System.out.println("Emergency mode enabled");
                bootstrap();
            } else {
                try {
                    User temp = userList.stream().filter(x -> x.getUserName().equalsIgnoreCase(uName.getText())).findFirst().get();
                    String pass = new String(pWord.getPassword());
                    boolean b = temp.authenticate(pass);
                    if (b) {
                        authenticatedUser = temp;
                        System.out.printf("Successfully logged in as %s\n", temp.getUserName());
                        MainWindow gui = new MainWindow(authenticatedUser, userList); // set the auth user for the main window
                        frame.setVisible(false);

                    } else System.out.println("Wrong username/password");
                } catch (Exception z) {
                    System.err.println(z);
                }
            }
        });

        frame.setLayout(new GridLayout(3,2));
        frame.add(label1);
        frame.add(uName);

        frame.add(label2);
        frame.add(pWord);

        frame.add(submit);
        frame.add(new JSeparator(SwingConstants.VERTICAL));
        frame.setVisible(true);
    }

    /**
     First time logging on with the top level credentials, force user to make a new account
     */
    private void bootstrap() {
        JFrame frame = new JFrame("Create new account");
        frame.setLayout(new GridLayout(5,2));

        frame.setMinimumSize(new Dimension(300,100));

        JTextField field1 = new JTextField();
        JPasswordField pWord = new JPasswordField();
        JPasswordField pWordConfirm = new JPasswordField();

        JLabel label1 = new JLabel("Username:");
        JLabel label2 = new JLabel("Password;");
        JLabel label3 = new JLabel("Confirm:");

        JButton button = new JButton("Submit");

        button.addActionListener(e -> {
            // If password match then create and save the new account
            try {
                if (Arrays.equals(pWord.getPassword(), pWordConfirm.getPassword())) {
                    User temp = new User(field1.getText(), new String(pWord.getPassword()));


                    // Check if the user already exists
                    if (userList.stream().anyMatch(p -> p.getUserName().equals(temp.getUserName()))) {
                        System.out.println("User already exists, ignoring");
                        frame.setVisible(false);
                        return;
                    }
                    // Add the employee and save to file
                    userList.add(temp);
                    saveData();
                    // Close the frame but display a message
                    JOptionPane.showMessageDialog(frame, String.format("User \"%s\" created, returning to login.", temp.getUserName()));
                    frame.setVisible(false);
                }
            } catch (Exception z) {
                System.out.println(z);
            }
        });

        frame.add(label1);
        frame.add(field1);

        frame.add(label2);
        frame.add(pWord);

        frame.add(label3);
        frame.add(pWordConfirm);

        frame.add(button);

        frame.setVisible(true);
    }
    /**
     * Reads user data into the vector list
     */
    private void loadData() {
        try {
            FileInputStream stream = new FileInputStream("UserData.dat");
            ObjectInputStream i = new ObjectInputStream(stream);


            userList = (Vector<User>) i.readObject();

            i.close();
            stream.close();
            System.out.println("Loaded data into array");

        } catch (IOException e) {
            System.out.println(e);
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
        }
    }

    /**
     * save the user and employee data into the database
     */
    public static void saveData() {
        try {
            FileOutputStream stream = new FileOutputStream("UserData.dat");
            ObjectOutputStream i = new ObjectOutputStream(stream);

            i.writeObject(userList);

            i.close();
            stream.close();
            System.out.println("Successfully saved the data");
        } catch (IOException e) {
            System.out.println(e);
        }
    }

}
