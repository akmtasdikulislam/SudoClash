package com.sudoclash.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.sudoclash.data.DataManager;
import com.sudoclash.data.RecoveryAnswers;

// Forgotten password in three short steps: 1) username + nickname, optionally "Try another way" = hobby,
// 2) a new password. Nothing is ever shown or sent back to the player: a right answer only unlocks the next step.
public class RecoveryScreen extends Widgets.Backdrop {
    private static final long serialVersionUID = 1L;

    @Override
    protected Scenery.Mood mood() {
        return Scenery.Mood.WELCOME;
    }

    private enum Step { NICKNAME, HOBBY, PASSWORD }

    private final MainPanel app;
    private final Widgets.Field userField = new Widgets.Field(Icons.Type.USER, "Your username", 18);
    private final Widgets.Field nickField = new Widgets.Field(Icons.Type.SMILE, "Your nickname", 18);
    private final Widgets.Field hobbyField = new Widgets.Field(Icons.Type.FLAG, "Your hobby", 18);
    private final PasswordField passwordField = new PasswordField(Icons.Type.CHECK, "New password", 18);
    private final PasswordField confirmField = new PasswordField(Icons.Type.CHECK, "Repeat the new password", 18);
    private final JLabel title = Theme.label("Forgot your password?", Theme.bold(24), Theme.FOREST);
    private final JLabel help = Theme.label(" ", Theme.plain(14), Theme.MUTED);
    private final JLabel stepLabel = Theme.label(" ", Theme.section(), Theme.EMERALD_DARK);
    private final JPanel userRow = new JPanel();
    private final JPanel nickRow = new JPanel();
    private final JPanel hobbyRow = new JPanel();
    private final JPanel passwordRows = new JPanel();
    private final StyledButton continueButton = new StyledButton(Icons.Type.CHECK, "CONTINUE", StyledButton.Style.PRIMARY);
    private final StyledButton otherWayButton = new StyledButton(null, "Try another way", StyledButton.Style.TEXT);
    private final StyledButton backButton = new StyledButton(null, "Back to login", StyledButton.Style.TEXT);
    private final JLabel legacyNote = Theme.label("<html><div style='width:316px'>Created your account before nickname and hobby existed? "
            + "Log in with your password and add them under Profile &gt; Recovery answers.</div></html>", Theme.small(), Theme.MUTED);
    private final Widgets.Feedback message = new Widgets.Feedback();
    private Step step = Step.NICKNAME;
    private boolean busy;

    public RecoveryScreen(MainPanel app) {
        super(new GridBagLayout());
        this.app = app;

        Widgets.Card card = new Widgets.Card(new BorderLayout(), 40);
        JPanel content = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(360, super.getPreferredSize().height);
            }
        };
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        stepLabel.setAlignmentX(LEFT_ALIGNMENT);
        title.setAlignmentX(LEFT_ALIGNMENT);
        help.setAlignmentX(LEFT_ALIGNMENT);
        content.add(stepLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(help);
        content.add(Box.createVerticalStrut(16));

        fillRow(userRow, "USERNAME", userField);
        fillRow(nickRow, "NICKNAME", nickField);
        fillRow(hobbyRow, "HOBBY", hobbyField);
        passwordRows.setOpaque(false);
        passwordRows.setLayout(new BoxLayout(passwordRows, BoxLayout.Y_AXIS));
        passwordRows.setAlignmentX(LEFT_ALIGNMENT);
        passwordRows.add(caption("NEW PASSWORD"));
        passwordRows.add(Box.createVerticalStrut(6));
        passwordRows.add(input(passwordField));
        JLabel rule = Theme.label("At least " + DataManager.MIN_PASSWORD + " characters", Theme.small(), Theme.MUTED);
        rule.setAlignmentX(LEFT_ALIGNMENT);
        passwordRows.add(Box.createVerticalStrut(3));
        passwordRows.add(rule);
        passwordRows.add(Box.createVerticalStrut(10));
        passwordRows.add(caption("REPEAT NEW PASSWORD"));
        passwordRows.add(Box.createVerticalStrut(6));
        passwordRows.add(input(confirmField));
        content.add(userRow);
        content.add(nickRow);
        content.add(hobbyRow);
        content.add(passwordRows);
        content.add(Box.createVerticalStrut(2));
        message.setAlignmentX(LEFT_ALIGNMENT);
        message.setMaxWidth(360);
        content.add(message);
        content.add(Box.createVerticalStrut(4));
        continueButton.stretch();
        continueButton.setAlignmentX(LEFT_ALIGNMENT);
        content.add(continueButton);
        content.add(Box.createVerticalStrut(6));
        content.add(centered(otherWayButton));
        content.add(centered(backButton));
        content.add(Box.createVerticalStrut(8));
        legacyNote.setAlignmentX(LEFT_ALIGNMENT);
        content.add(legacyNote);

        card.add(content, BorderLayout.CENTER);
        add(card, new GridBagConstraints());

        passwordField.setRevealListener(confirmField::setRevealed);
        confirmField.setRevealListener(passwordField::setRevealed);
        continueButton.addActionListener(e -> submit());
        userField.addActionListener(e -> submit());
        nickField.addActionListener(e -> submit());
        hobbyField.addActionListener(e -> submit());
        confirmField.addActionListener(e -> submit());
        otherWayButton.addActionListener(e -> switchWay());
        backButton.setToolTipText("Return to the login screen");
        backButton.addActionListener(e -> app.showLogin());
        showStep(Step.NICKNAME);
    }

    private void fillRow(JPanel row, String text, JComponent field) {
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.add(caption(text));
        row.add(Box.createVerticalStrut(6));
        row.add(input(field));
        row.add(Box.createVerticalStrut(10));
    }

    private JPanel centered(JComponent button) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.add(button);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private JLabel caption(String text) {
        JLabel label = Theme.label(text, Theme.section(), Theme.MUTED);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private JComponent input(JComponent field) {
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        return field;
    }

    private void showStep(Step next) {
        step = next;
        userRow.setVisible(next != Step.PASSWORD);
        nickRow.setVisible(next == Step.NICKNAME);
        hobbyRow.setVisible(next == Step.HOBBY);
        passwordRows.setVisible(next == Step.PASSWORD);
        otherWayButton.setVisible(next != Step.PASSWORD);
        legacyNote.setVisible(next != Step.PASSWORD);
        if (next == Step.NICKNAME) {
            stepLabel.setText("STEP 1 OF 2 - VERIFY IT'S YOU");
            help.setText("<html><div style='width:316px'>Enter your username and the nickname you chose when you created the account.</div></html>");
            continueButton.setLabel(Icons.Type.CHECK, "CONTINUE");
            continueButton.setToolTipText("Check your nickname");
            otherWayButton.setLabel(null, "Try another way");
            otherWayButton.setToolTipText("Verify with your hobby instead of your nickname");
        } else if (next == Step.HOBBY) {
            stepLabel.setText("STEP 1 OF 2 - ANOTHER WAY");
            help.setText("<html><div style='width:316px'>Enter your username and the hobby you chose when you created the account.</div></html>");
            continueButton.setLabel(Icons.Type.CHECK, "CONTINUE");
            continueButton.setToolTipText("Check your hobby");
            otherWayButton.setLabel(null, "Use my nickname instead");
            otherWayButton.setToolTipText("Verify with your nickname");
        } else {
            stepLabel.setText("STEP 2 OF 2 - NEW PASSWORD");
            help.setText("<html><div style='width:316px'>Thanks, that matches. Choose a new password.</div></html>");
            continueButton.setLabel(Icons.Type.CHECK, "RESET PASSWORD");
            continueButton.setToolTipText("Save the new password");
        }
        revalidate();
        repaint();
    }

    private void switchWay() {
        if (busy) {
            return;
        }
        message.clear();
        showStep(step == Step.NICKNAME ? Step.HOBBY : Step.NICKNAME);
        SwingUtilities.invokeLater((step == Step.HOBBY ? hobbyField : nickField)::requestFocusInWindow);
    }

    private void submit() {
        if (busy) {
            return;
        }
        String user = userField.getText().trim();
        if (step == Step.PASSWORD) {
            String first = passwordField.getPasswordText();
            if (first.length() < DataManager.MIN_PASSWORD || first.length() > DataManager.MAX_PASSWORD) {
                warn("The new password needs " + DataManager.MIN_PASSWORD + "-" + DataManager.MAX_PASSWORD
                        + " characters.");
            } else if (!first.equals(confirmField.getPasswordText())) {
                warn("Passwords do not match.");
            } else {
                app.resetPassword(user, first);
            }
            return;
        }
        boolean nick = step == Step.NICKNAME;
        String answer = (nick ? nickField : hobbyField).getText();
        if (!DataManager.isValidUsername(user)) {
            warn("Enter your username (2-16 letters, numbers or _).");
        } else if (RecoveryAnswers.normalize(answer).length() < RecoveryAnswers.MIN_LENGTH) {
            warn(nick ? "Enter your nickname." : "Enter your hobby.");
        } else {
            app.verifyRecovery(user, nick ? DataManager.RecoveryMethod.NICKNAME : DataManager.RecoveryMethod.HOBBY,
                    answer);
        }
    }

    private void warn(String text) {
        message.showSticky(Widgets.Feedback.Kind.WARNING, text);
    }

    public void setBusy(boolean value, String text) {
        busy = value;
        for (JComponent c : new JComponent[] {userField, nickField, hobbyField, passwordField, confirmField,
            continueButton, otherWayButton, backButton}) {
            c.setEnabled(!value);
        }
        if (value) {
            message.showSticky(Widgets.Feedback.Kind.INFO, text);
        } else {
            message.clear();
        }
    }

    // A wrong answer (or a block after too many): the answer fields are emptied, the message stays.
    public void showError(String text) {
        setBusy(false, null);
        nickField.setText("");
        hobbyField.setText("");
        message.showSticky(Widgets.Feedback.Kind.ERROR, text);
    }

    // Step 2 failed. When the permit is no longer valid the player has to start again.
    public void showFailure(String text, boolean startOver) {
        setBusy(false, null);
        passwordField.setText("");
        confirmField.setText("");
        if (startOver) {
            showStep(Step.NICKNAME);
        }
        message.showSticky(Widgets.Feedback.Kind.ERROR, text);
    }

    // The answer was right: ask for the new password.
    public void onVerified() {
        message.clear();
        showStep(Step.PASSWORD);
        SwingUtilities.invokeLater(passwordField::requestFocusInWindow);
    }

    public void onShow(String username) {
        busy = false;
        setBusy(false, null);
        userField.setText(username == null ? "" : username);
        nickField.setText("");
        hobbyField.setText("");
        passwordField.setText("");
        confirmField.setText("");
        passwordField.setRevealed(false);
        confirmField.setRevealed(false);
        showStep(Step.NICKNAME);
        SwingUtilities.invokeLater(userField::requestFocusInWindow);
    }
}
