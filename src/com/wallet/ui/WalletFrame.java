package com.wallet.ui;

import com.wallet.exception.WalletException;
import com.wallet.service.WalletService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/** Swing front-end: the Send button's ActionListener drives a transfer. */
public class WalletFrame extends JFrame {
    private final JTextField fromField = new JTextField(8);
    private final JTextField toField = new JTextField(8);
    private final JTextField amountField = new JTextField(8);
    private final JTextArea output = new JTextArea(10, 40);

    public WalletFrame(WalletService service) {
        super("Multi-User Digital Wallet");
        JButton send = new JButton("Send Money");
        JPanel form = new JPanel(new FlowLayout());
        form.add(new JLabel("From:")); form.add(fromField);
        form.add(new JLabel("To:"));   form.add(toField);
        form.add(new JLabel("Amount:")); form.add(amountField);
        form.add(send);
        output.setEditable(false);
        add(form, BorderLayout.NORTH);
        add(new JScrollPane(output), BorderLayout.CENTER);

        send.addActionListener(e -> {
            try {
                var t = service.transfer(fromField.getText().trim(), toField.getText().trim(),
                        new BigDecimal(amountField.getText().trim()));
                output.append("SUCCESS: " + t + "\n");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid amount");
            } catch (WalletException ex) {
                output.append("FAILED: " + ex.getMessage() + "\n");
            }
        });
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
    }
}
