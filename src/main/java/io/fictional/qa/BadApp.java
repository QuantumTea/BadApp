package io.fictional.qa;

/**
 * Created with IntelliJ IDEA.
 * User: Alison Hawke
 * 
 * Description: This is an intentionally buggy program for QA training in exploratory testing.
 * The Close button (top right red X or circle) doesn't work
 * App closes randomly, leaving log file with a stack trace on the desktop
 * Message popup is badly misspelled
 * Exit button always fails the first time and works 30% of the time after that
 * When the exit button fails, the app gets 50px wider
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static javax.swing.JOptionPane.showMessageDialog;

public class BadApp extends JFrame
{
    private static int failedCloseAttemptsCounter = 0;
    private static JLabel failedCloseAttemptsLabel;
    private static int timeToFatalException = 0;
    private static int survivedExceptionAttempts = 0;
    private static double finalRandomNumber = 0;
    private static String systemLineSeparator = System.getProperty("line.separator");
    private PrintWriter printWriter;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss a");
    private final String pathToDesktop = System.getProperty("user.home")
            + System.getProperty("file.separator") + "Desktop"
            + System.getProperty("file.separator") + "LessBadApp error log.txt";

    public BadApp() {
        super("Welcome to the BadApp");
        ConstructContentPane();
        GenerateMenuBar();
        SetUpTimerToGenerateExceptions();
    }

    public static void main(String[] args) {
        BadApp app;
        app = new BadApp();
        app.setVisible(true);
    }

    private void ConstructContentPane() {
        setSize(450, 300);
        final Container contentPane = getContentPane();
        final JButton launchMessageWindowButton = new JButton("Launch message window");
        launchMessageWindowButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                launchMisspelledMessagePopup();
            }
        });
        contentPane.add(launchMessageWindowButton, BorderLayout.NORTH);

        final JButton faultyExitButton = new JButton("Exit");
        faultyExitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AttemptToCloseTheApp();
            }
        });
        contentPane.add(faultyExitButton, BorderLayout.SOUTH);

        failedCloseAttemptsLabel = new JLabel();
        contentPane.add(failedCloseAttemptsLabel, BorderLayout.CENTER);

        // disable the red X at the top right of the window
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    private void SetUpTimerToGenerateExceptions() {
        int delay = 5000; //milliseconds
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    timeToFatalException += 5;
                    SeeIfAppWillClose();
                } catch (Exception e) {
                    dealWithException(e);
                }
            }
        };
        new Timer(delay, taskPerformer).start();
    }

    private void GenerateMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        JMenu forbiddenMenu = new JMenu("Do not click this menu");
        menuBar.add(forbiddenMenu);

        JMenuItem aboutAction = new JMenuItem("About the BadApp");
        helpMenu.add(aboutAction);
        aboutAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayAboutBox();
            }
        });

        JMenuItem forbiddenAction = new JMenuItem("I'm serious, don't click this");
        forbiddenMenu.add(forbiddenAction);
        forbiddenAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                youClickedTheForbiddenMenu();
            }
        });
    }

    private void launchMisspelledMessagePopup() {
        JOptionPane.showMessageDialog(this, "Yu launchd a messsge window, wel done.", "Alart!", JOptionPane.QUESTION_MESSAGE);
    }

    private void displayAboutBox() {
        JOptionPane.showMessageDialog(this, "LessBadApp was written by Alison Hawke",
                "About the LessBadApp", JOptionPane.ERROR_MESSAGE);
    }

    private void youClickedTheForbiddenMenu() {
        // see if you can change the OK button to an "I'm sorry"
        showMessageDialog(this, "Ouch! \n\nI told you not to click that.", "Ouch!", JOptionPane.WARNING_MESSAGE);
    }

    private void dealWithException(Exception exception) {
        writeErrorLog(exception);
        System.exit(0);
    }

    private void SeeIfAppWillClose() throws Exception {
        double randomNumber = GetRandomNumber();

        // throws exception 10% of the time and goes off every five seconds
        if (randomNumber < 0.9) {
            survivedExceptionAttempts++;
        } else {
            finalRandomNumber = randomNumber;
            throw new Exception("Something has gone horribly wrong.");
        }
    }

    private void AttemptToCloseTheApp() {

        if (FailFirstTwoCloseAttemptsWhenCounterIsOneOrTwo()) return;
        if (Fail70PercentOfTime()) return;
        IncrementCounterToLimitOf20();
        MakeTheWindowWider();
    }

    private void IncrementCounterToLimitOf20() {
        failedCloseAttemptsCounter++;
        if (failedCloseAttemptsCounter == 20) {
            writeErrorLog("Nothing to see here");
            System.exit(0);
        }
    }

    private boolean Fail70PercentOfTime() {
        if (GetRandomNumber() < 0.03) {
            writeErrorLog("Exit button worked");
            System.exit(0);
            return true;
        }
        return false;
    }

    double GetRandomNumber() {
        // Returns a number between 0.0 and 0.1
        return Math.random();
    }

    private boolean FailFirstTwoCloseAttemptsWhenCounterIsOneOrTwo() {
        if ((failedCloseAttemptsCounter == 0) || (failedCloseAttemptsCounter == 1)) {
            FailTheFirstTwoCloseAttempts();
            return true;
        }
        return false;
    }

    private void FailTheFirstTwoCloseAttempts() {
        failedCloseAttemptsCounter++;
        MakeTheWindowWider();
    }

    private void MakeTheWindowWider() {
        int newWidth = this.getWidth() + 50;
        int oldHeight = this.getHeight();
        this.setSize(newWidth, oldHeight);
        failedCloseAttemptsLabel.setText("    Failed attempts to close this window: " + failedCloseAttemptsCounter);
    }

    private void writeErrorLog(String message)
    {
        try
        {
            // true to append, false to write a new file
            printWriter = new PrintWriter(new BufferedWriter(new FileWriter(pathToDesktop, true)));
            writeSystemInformation(dateFormat, printWriter, message);
            printWriter.println("-------------------------------------------------------");
            printWriter.close();
        } catch (IOException ioe)
        {
            System.out.println("IOException, log file not written");
            ioe.printStackTrace();
        }
    }

    private void writeErrorLog(Exception exception)
    {
        try
        {
            printWriter = new PrintWriter(new BufferedWriter(new FileWriter(pathToDesktop, true)));
            writeSystemInformation(dateFormat, printWriter, "*the exceptional sound of crickets chirping*");
            exception.printStackTrace(printWriter);
            printWriter.println("-------------------------------------------------------");
            printWriter.close();
        } catch (IOException ioe)
        {
            System.out.println("IOException, log file not written");
            ioe.printStackTrace();
        }
    }

    private void writeSystemInformation(DateFormat dateFormat, PrintWriter p, String message) {
        p.println("*** Errror log for IntentionallyBadApp: " + "Something went horribly wrong");
        // intentional spelling error, even in the stack trace
        p.println("Current system time is: " + dateFormat.format(new Date()));
        p.println("Number of failed exit attempts was " + failedCloseAttemptsCounter
                + systemLineSeparator);
        p.println("Additional system message: " + message + systemLineSeparator);

        p.println("Time from launch to fatal exception was " + timeToFatalException + " seconds.");
        p.println("Survived exception " + survivedExceptionAttempts + " time(s)");
        p.println("Final random number was: " + finalRandomNumber
                + systemLineSeparator);

        p.println("The logged in user is " + System.getProperty("user.name"));
        p.println("IntentionallyBadApp was running on " + System.getProperty("os.name"));
        p.println("Operating system architecture is " + System.getProperty("os.arch")
                + systemLineSeparator);

        p.println("Java version is " + System.getProperty("java.version"));
        p.println("Java Virtual Machine version is " + System.getProperty("java.vm.version"));
        p.println("Java Runtime Environment version is " + System.getProperty("java.specification.version"));
        p.println("Java vendor is " + System.getProperty("java.vendor")
                + systemLineSeparator);
    }
}
