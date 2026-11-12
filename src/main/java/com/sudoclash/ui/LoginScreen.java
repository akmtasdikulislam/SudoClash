package com.sudoclash.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.geom.Ellipse2D;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.sudoclash.data.DataManager;
import com.sudoclash.data.RecoveryAnswers;

// Log in with a username and password, or switch to "create account".
public class LoginScreen extends Widgets.Backdrop {
    private static final long serialVersionUID = 1L;

    @Override
    protected Scenery.Mood mood() {
        return Scenery.Mood.WELCOME;
    }

    private final MainPanel app;
    private final Widgets.Field field = new Widgets.Field(Icons.Type.USER, "Enter your username", 18);
    private final PasswordField passwordField = new PasswordField(Icons.Type.CHECK, "Password", 18);
    private final PasswordField confirmField = new PasswordField(Icons.Type.CHECK, "Repeat the password", 18);
    private final Widgets.Field nickField = new Widgets.Field(Icons.Type.SMILE, "A nickname only you would use", 18);
    private final Widgets.Field hobbyField = new Widgets.Field(Icons.Type.FLAG, "Your favourite hobby", 18);
    private final JLabel nickCaption = Theme.label("NICKNAME", Theme.section(), Theme.MUTED);
    private final JLabel hobbyCaption = Theme.label("HOBBY", Theme.section(), Theme.MUTED);
    private final JLabel answersHint = Theme.label("<html><div style='width:316px'>These two answers reset your password if you forget it. Keep them private.</div></html>",
            Theme.small(), Theme.MUTED);
    private final JLabel confirmCaption = Theme.label("REPEAT PASSWORD", Theme.section(), Theme.MUTED);
    private final JLabel passwordHint = Theme.label("At least " + DataManager.MIN_PASSWORD
            + " characters (letters or numbers)", Theme.small(), Theme.MUTED);
    private final StyledButton loginButton = new StyledButton(Icons.Type.PLAY, "LOGIN", StyledButton.Style.PRIMARY);
    private final StyledButton switchButton = new StyledButton(null, "New here? Create an account",
            StyledButton.Style.TEXT);
    private final StyledButton forgotButton = new StyledButton(null, "Forgot password?", StyledButton.Style.TEXT);
    private final JLabel storageLabel = Theme.label(" ", Theme.small(), Theme.MUTED);
    private final Widgets.Feedback message = new Widgets.Feedback();
    private final JLabel headerTitle = Theme.label("Welcome Back!", Theme.bold(26), Theme.TEXT);
    private final JLabel headerSub = Theme.label("Sign in to continue", Theme.plain(15), Theme.MUTED);
    private final JPanel brandBox = new JPanel();
    private final Art.Pic brandHero = new Art.Pic(Art.Image.HERO, 330, 330);
    private JComponent cardHolder;
    private boolean wideLayout = true;
    private javax.swing.Timer lockTimer;
    private boolean creating;
    private boolean busy;
    // Spacers that belong to the create-account fields: hidden together with them, so no empty gap is left.
    private final java.util.List<java.awt.Component> creatingGaps = new java.util.ArrayList<>();

    private java.awt.Component gap(int height) {
        java.awt.Component c = Box.createVerticalStrut(height);
        creatingGaps.add(c);
        return c;
    }

    // True while the program itself changes the fields (not the player), so no live message is changed.
    private boolean programmatic;

    public LoginScreen(MainPanel app) {
        super(new GridBagLayout());
        this.app = app;

        Widgets.Card card = new Widgets.Card(new BorderLayout(), 40);
        // Only the width is fixed; the height follows the content (it changes in "create account" mode).
        JPanel content = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(360, super.getPreferredSize().height);
            }
        };
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        headerTitle.setAlignmentX(CENTER_ALIGNMENT);
        headerSub.setAlignmentX(CENTER_ALIGNMENT);

        JLabel caption = Theme.label("USERNAME", Theme.section(), Theme.MUTED);
        JLabel passwordCaption = Theme.label("PASSWORD", Theme.section(), Theme.MUTED);
        caption.setAlignmentX(LEFT_ALIGNMENT);
        passwordCaption.setAlignmentX(LEFT_ALIGNMENT);
        confirmCaption.setAlignmentX(LEFT_ALIGNMENT);
        nickCaption.setAlignmentX(LEFT_ALIGNMENT);
        hobbyCaption.setAlignmentX(LEFT_ALIGNMENT);
        answersHint.setAlignmentX(LEFT_ALIGNMENT);
        passwordHint.setAlignmentX(LEFT_ALIGNMENT);
        for (JComponent input : new JComponent[] {field, passwordField, confirmField, nickField, hobbyField}) {
            input.setAlignmentX(LEFT_ALIGNMENT);
            input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        }
        // One eye shows or hides both password fields.
        passwordField.setRevealListener(confirmField::setRevealed);
        confirmField.setRevealListener(passwordField::setRevealed);
        field.setToolTipText("2-16 letters, numbers or underscore");
        passwordField.setToolTipText(DataManager.MIN_PASSWORD + "-" + DataManager.MAX_PASSWORD + " characters");
        message.setAlignmentX(LEFT_ALIGNMENT);
        message.setMaxWidth(360);
        message.setCollapsible(true);
        loginButton.stretch();
        loginButton.setAlignmentX(LEFT_ALIGNMENT);
        loginButton.setToolTipText("Log in and open the main menu");
        switchButton.setAlignmentX(CENTER_ALIGNMENT);
        switchButton.setToolTipText("Switch between logging in and creating an account");

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(CENTER_ALIGNMENT);
        form.setMaximumSize(new Dimension(360, 720));
        form.add(caption);
        form.add(Box.createVerticalStrut(6));
        form.add(field);
        form.add(Box.createVerticalStrut(10));
        form.add(passwordCaption);
        form.add(Box.createVerticalStrut(6));
        form.add(passwordField);
        form.add(gap(3));
        form.add(passwordHint);
        form.add(gap(10));
        form.add(confirmCaption);
        form.add(gap(6));
        form.add(confirmField);
        form.add(gap(10));
        form.add(nickCaption);
        form.add(gap(6));
        form.add(nickField);
        form.add(gap(10));
        form.add(hobbyCaption);
        form.add(gap(6));
        form.add(hobbyField);
        form.add(gap(4));
        form.add(answersHint);
        form.add(Box.createVerticalStrut(2));
        form.add(message);
        form.add(Box.createVerticalStrut(4));
        form.add(loginButton);
        form.add(Box.createVerticalStrut(2));
        forgotButton.setToolTipText("Reset your password with your nickname or hobby");
        // Children of one box must share the same alignment, so the centred link sits in a row of its own.
        JPanel forgotRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        forgotRow.setOpaque(false);
        forgotRow.setAlignmentX(LEFT_ALIGNMENT);
        forgotRow.add(forgotButton);
        forgotRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, forgotRow.getPreferredSize().height));
        form.add(forgotRow);

        content.add(headerTitle);
        content.add(Box.createVerticalStrut(2));
        content.add(headerSub);
        content.add(Box.createVerticalStrut(20));
        content.add(form);
        content.add(Box.createVerticalStrut(8));
        content.add(switchButton);
        content.add(Box.createVerticalStrut(6));
        storageLabel.setAlignmentX(CENTER_ALIGNMENT);
        content.add(storageLabel);
        card.add(content, BorderLayout.CENTER);
        // The ninja and the wordmark sit beside the card (above it in a narrow window).
        brandHero.setAlignmentX(CENTER_ALIGNMENT);
        brandBox.setOpaque(false);
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        JLabel word = Theme.label("SudoClash", Theme.wordmark(44), Theme.FOREST);
        word.setAlignmentX(CENTER_ALIGNMENT);
        JLabel tag = Theme.label("Competitive Sudoku", Theme.plain(16).deriveFont(java.awt.Font.ITALIC), Theme.MUTED);
        tag.setAlignmentX(CENTER_ALIGNMENT);
        brandBox.add(brandHero);
        brandBox.add(Box.createVerticalStrut(2));
        brandBox.add(word);
        brandBox.add(tag);
        cardHolder = card;
        arrange();
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                arrange();
            }
        });

        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validateInput();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateInput();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validateInput();
            }
        };
        field.getDocument().addDocumentListener(listener);
        passwordField.getDocument().addDocumentListener(listener);
        confirmField.getDocument().addDocumentListener(listener);
        nickField.getDocument().addDocumentListener(listener);
        hobbyField.getDocument().addDocumentListener(listener);
        field.addActionListener(e -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(e -> {
            if (creating) {
                confirmField.requestFocusInWindow();
            } else {
                submit();
            }
        });
        confirmField.addActionListener(e -> nickField.requestFocusInWindow());
        nickField.addActionListener(e -> hobbyField.requestFocusInWindow());
        hobbyField.addActionListener(e -> submit());
        loginButton.addActionListener(e -> submit());
        switchButton.addActionListener(e -> setCreating(!creating));
        forgotButton.addActionListener(e -> app.showRecovery());
        setCreating(false);
    }

    // Wide window: brand left, card right. Narrow window: a small brand above the card.
    private void arrange() {
        boolean wide = getWidth() == 0 || getWidth() >= 900;
        if (cardHolder == null || (wide == wideLayout && getComponentCount() > 0)) {
            return;
        }
        wideLayout = wide;
        removeAll();
        brandHero.setVisible(wide || getHeight() > 900);
        GridBagConstraints c = new GridBagConstraints();
        if (wide) {
            c.gridx = 0;
            c.insets = new java.awt.Insets(0, 0, 0, 70);
            add(brandBox, c);
            c.gridx = 1;
            c.insets = new java.awt.Insets(0, 0, 0, 0);
            add(cardHolder, c);
        } else {
            c.gridx = 0;
            c.gridy = 0;
            add(brandBox, c);
            c.gridy = 1;
            add(cardHolder, c);
        }
        revalidate();
        repaint();
    }

    public static boolean isValidName(String name) {
        return DataManager.isValidUsername(name);
    }

    private void setCreating(boolean value) {
        creating = value;
        headerTitle.setText(value ? "Create Account" : "Welcome Back!");
        headerSub.setText(value ? "Join SudoClash" : "Sign in to continue");
        confirmCaption.setVisible(value);
        confirmField.setVisible(value);
        for (JComponent c : new JComponent[] {nickCaption, nickField, hobbyCaption, hobbyField, answersHint}) {
            c.setVisible(value);
        }
        nickField.setText("");
        hobbyField.setText("");
        passwordHint.setVisible(value);
        for (java.awt.Component g : creatingGaps) {
            g.setVisible(value);
        }
        forgotButton.setVisible(!value);
        programmatic = true;
        confirmField.setText("");
        programmatic = false;
        stopLockTimer();
        passwordField.setPlaceholder(value ? "Choose a password (min " + DataManager.MIN_PASSWORD + " characters)"
                : "Password");
        loginButton.setLabel(value ? Icons.Type.PLUS : Icons.Type.PLAY, value ? "CREATE ACCOUNT" : "LOGIN");
        loginButton.setToolTipText(value ? "Create the account and log in" : "Log in and open the main menu");
        switchButton.setLabel(null, value ? "Already have an account? Log in" : "New here? Create an account");
        message.clear();
        validateInput();
        revalidate();
        repaint();
    }

    private String password() {
        return passwordField.getPasswordText();
    }

    // Live help while typing. A message only appears when what you typed can no longer become correct:
    // "Sha" is fine for a username (you are still typing), and half of the repeated password is fine too.
    // The button stays enabled: pressing it explains everything that is missing.
    private void validateInput() {
        if (programmatic) {
            return;
        }
        // The player is typing again: an old message (for example "Wrong password") or countdown is outdated.
        stopLockTimer();
        String text = field.getText().trim();
        String first = password();
        String again = confirmField.getPasswordText();
        boolean badName = !text.isEmpty() && (!text.matches("[A-Za-z0-9_]+") || text.length() > 16);
        boolean mismatch = creating && !again.isEmpty() && !first.startsWith(again) && !again.startsWith(first);
        if (badName) {
            message.showSticky(Widgets.Feedback.Kind.WARNING, "Use 2-16 letters, numbers or _ for the username");
        } else if (creating && !first.isEmpty() && first.length() < DataManager.MIN_PASSWORD) {
            message.showSticky(Widgets.Feedback.Kind.INFO, "Password: " + first.length() + "/"
                    + DataManager.MIN_PASSWORD + " characters so far");
        } else if (mismatch || (creating && confirmField.isFocusOwner() && again.length() > first.length())) {
            message.showSticky(Widgets.Feedback.Kind.WARNING, "Passwords do not match.");
        } else if (creating && !again.isEmpty() && again.equals(first)) {
            message.showSticky(Widgets.Feedback.Kind.SUCCESS, "Passwords match.");
        } else if (!busy) {
            message.clear();
        }
    }

    private void submit() {
        if (busy) {
            return;
        }
        String text = field.getText().trim();
        if (text.isEmpty()) {
            message.showSticky(Widgets.Feedback.Kind.WARNING, "Please enter a username");
        } else if (!isValidName(text)) {
            message.showSticky(Widgets.Feedback.Kind.WARNING, "Use 2-16 letters, numbers or _");
        } else if (password().isEmpty()) {
            message.showSticky(Widgets.Feedback.Kind.WARNING, "Please enter your password");
        } else if (creating && password().length() < DataManager.MIN_PASSWORD) {
            message.showSticky(Widgets.Feedback.Kind.WARNING,
                    "The password needs at least " + DataManager.MIN_PASSWORD + " characters");
        } else if (creating && !password().equals(confirmField.getPasswordText())) {
            message.showSticky(Widgets.Feedback.Kind.WARNING, "Passwords do not match.");
        } else if (creating && RecoveryAnswers.nicknameProblem(nickField.getText()) != null) {
            message.showSticky(Widgets.Feedback.Kind.WARNING, RecoveryAnswers.nicknameProblem(nickField.getText()));
        } else if (creating && RecoveryAnswers.hobbyProblem(hobbyField.getText()) != null) {
            message.showSticky(Widgets.Feedback.Kind.WARNING, RecoveryAnswers.hobbyProblem(hobbyField.getText()));
        } else if (creating) {
            app.authenticate(text, password(), true, nickField.getText().trim(), hobbyField.getText().trim());
        } else {
            app.authenticate(text, password(), false);
        }
    }

    // Called by MainPanel while it works in the background.
    public void setBusy(boolean value, String text) {
        busy = value;
        field.setEnabled(!value);
        passwordField.setEnabled(!value);
        confirmField.setEnabled(!value);
        nickField.setEnabled(!value);
        hobbyField.setEnabled(!value);
        switchButton.setEnabled(!value);
        forgotButton.setEnabled(!value);
        loginButton.setEnabled(!value);
        if (value) {
            message.showSticky(Widgets.Feedback.Kind.INFO, text);
        } else {
            validateInput();
        }
    }

    // The answer of the authentication layer ("Wrong password. 3 attempts left."). It stays on the screen
    // until the player types again. The password field is emptied first, so that does not erase the message.
    public void showError(String text) {
        setBusy(false, null);
        clearPasswordFields();
        message.showSticky(Widgets.Feedback.Kind.ERROR, text);
        SwingUtilities.invokeLater(passwordField::requestFocusInWindow);
    }

    // Blocked after too many wrong attempts: shows a live countdown.
    public void showLocked(long seconds) {
        setBusy(false, null);
        clearPasswordFields();
        stopLockTimer();
        final long[] left = {seconds};
        message.showSticky(Widgets.Feedback.Kind.ERROR, "Too many wrong attempts. Try again in "
                + com.sudoclash.exception.LockedOutException.format(left[0]) + ".");
        lockTimer = new javax.swing.Timer(1000, e -> {
            left[0]--;
            if (left[0] <= 0) {
                stopLockTimer();
                message.showSticky(Widgets.Feedback.Kind.INFO, "You can try again now.");
            } else {
                message.showSticky(Widgets.Feedback.Kind.ERROR, "Too many wrong attempts. Try again in "
                        + com.sudoclash.exception.LockedOutException.format(left[0]) + ".");
            }
        });
        lockTimer.start();
    }

    private void stopLockTimer() {
        if (lockTimer != null) {
            lockTimer.stop();
            lockTimer = null;
        }
    }

    private void clearPasswordFields() {
        programmatic = true;
        passwordField.setText("");
        confirmField.setText("");
        programmatic = false;
    }

    // Shows what kind of storage is used. A warning is red: the data would be lost when the game closes.
    public void setStorageStatus(String text, boolean warning) {
        storageLabel.setText(text);
        storageLabel.setForeground(warning ? Theme.ERROR : Theme.MUTED);
    }

    // A friendly message that stays until the player types again (for example after a password reset).
    public void showNotice(String text) {
        message.showSticky(Widgets.Feedback.Kind.SUCCESS, text);
    }

    public void prefill(String username) {
        programmatic = true;
        field.setText(username);
        programmatic = false;
    }

    public void onShow() {
        busy = false;
        stopLockTimer();
        field.setEnabled(true);
        passwordField.setEnabled(true);
        confirmField.setEnabled(true);
        nickField.setEnabled(true);
        hobbyField.setEnabled(true);
        switchButton.setEnabled(true);
        forgotButton.setEnabled(true);
        loginButton.setEnabled(true);
        programmatic = true;
        field.setText("");
        passwordField.setText("");
        confirmField.setText("");
        programmatic = false;
        passwordField.setRevealed(false);
        confirmField.setRevealed(false);
        setCreating(false);
        SwingUtilities.invokeLater(field::requestFocusInWindow);
    }
}
