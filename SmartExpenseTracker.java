import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SmartExpenseTracker extends JFrame {
    
    private JTextField txtAmount, txtDescription, txtBudgetLimit;
    private JComboBox<String> cmbCategory;
    private JLabel lblCurrentMonth;
    private JButton btnAddExpense, btnSetBudget, btnClearData;

    private static final String URL = "jdbc:mysql://localhost:3306/ExpenseTrackerDB?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root"; 
    private static final String PASSWORD = "Password"; 

    private String currentMonthYear;

    public SmartExpenseTracker() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        currentMonthYear = LocalDate.now().format(formatter);

        setTitle("Smart Expense Tracker");
        setSize(500, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        Color bgColor = new Color(243, 246, 250);
        Color headerColor = new Color(30, 58, 138);
        Color cardColor = Color.WHITE;
        Color primaryBlue = new Color(37, 99, 235);
        Color successGreen = new Color(16, 185, 129);
        Color dangerRed = new Color(220, 38, 38);
        Color textDark = new Color(31, 41, 55);

        Font titleFont = new Font("Segoe UI", Font.BOLD, 20);
        Font sectionFont = new Font("Segoe UI", Font.BOLD, 14);
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font btnFont = new Font("Segoe UI", Font.BOLD, 13);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(bgColor);
        setContentPane(mainContainer);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(headerColor);
        headerPanel.setBorder(new EmptyBorder(18, 22, 18, 22));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        JLabel lblAppTitle = new JLabel("Smart Expense Tracker");
        lblAppTitle.setFont(titleFont);
        lblAppTitle.setForeground(Color.WHITE);

        JLabel lblSubTitle = new JLabel("Personal Budget & Expense Manager");
        lblSubTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubTitle.setForeground(new Color(191, 219, 254));

        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        titleBox.setOpaque(false);
        titleBox.add(lblAppTitle);
        titleBox.add(lblSubTitle);
        headerPanel.add(titleBox, BorderLayout.WEST);

        mainContainer.add(headerPanel);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(bgColor);
        contentPanel.setBorder(new EmptyBorder(18, 22, 18, 22));
        mainContainer.add(contentPanel);

        JPanel pnlBudget = createCardPanel(cardColor, "1. Set Monthly Budget Limit", sectionFont, headerColor);
        pnlBudget.setLayout(new GridLayout(3, 2, 12, 12));

        JLabel l1 = new JLabel("Current Month:");
        l1.setFont(labelFont);
        l1.setForeground(textDark);
        pnlBudget.add(l1);

        lblCurrentMonth = new JLabel(currentMonthYear);
        lblCurrentMonth.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCurrentMonth.setForeground(primaryBlue);
        pnlBudget.add(lblCurrentMonth);

        JLabel l2 = new JLabel("Limit Amount (₹):");
        l2.setFont(labelFont);
        l2.setForeground(textDark);
        pnlBudget.add(l2);

        txtBudgetLimit = styleTextField(inputFont);
        pnlBudget.add(txtBudgetLimit);

        pnlBudget.add(new JLabel(""));
        btnSetBudget = styleButton("Set Monthly Budget", primaryBlue, btnFont);
        pnlBudget.add(btnSetBudget);

        contentPanel.add(pnlBudget);
        contentPanel.add(Box.createVerticalStrut(16));

        JPanel pnlExpense = createCardPanel(cardColor, "2. Log New Expense", sectionFont, headerColor);
        pnlExpense.setLayout(new GridLayout(4, 2, 12, 12));

        JLabel l3 = new JLabel("Select Category:");
        l3.setFont(labelFont);
        l3.setForeground(textDark);
        pnlExpense.add(l3);

        String[] categories = {"Food", "Travel", "Bills/Fees", "Entertainment", "Shopping", "Others"};
        cmbCategory = new JComboBox<>(categories);
        cmbCategory.setFont(inputFont);
        cmbCategory.setBackground(Color.WHITE);
        pnlExpense.add(cmbCategory);

        JLabel l4 = new JLabel("Description / Note:");
        l4.setFont(labelFont);
        l4.setForeground(textDark);
        pnlExpense.add(l4);

        txtDescription = styleTextField(inputFont);
        pnlExpense.add(txtDescription);

        JLabel l5 = new JLabel("Amount (₹):");
        l5.setFont(labelFont);
        l5.setForeground(textDark);
        pnlExpense.add(l5);

        txtAmount = styleTextField(inputFont);
        pnlExpense.add(txtAmount);

        pnlExpense.add(new JLabel(""));
        btnAddExpense = styleButton("Add Expense", successGreen, btnFont);
        pnlExpense.add(btnAddExpense);

        contentPanel.add(pnlExpense);
        contentPanel.add(Box.createVerticalStrut(16));

        JPanel pnlAdmin = createCardPanel(cardColor, "3. System Actions", sectionFont, headerColor);
        pnlAdmin.setLayout(new FlowLayout(FlowLayout.CENTER));
        btnClearData = styleButton("Clear Database Data", dangerRed, btnFont);
        btnClearData.setPreferredSize(new Dimension(210, 36));
        pnlAdmin.add(btnClearData);

        contentPanel.add(pnlAdmin);

        btnSetBudget.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double limit = Double.parseDouble(txtBudgetLimit.getText().trim());
                    if (limit <= 0) {
                        JOptionPane.showMessageDialog(null, "Please enter an amount greater than 0!", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (saveBudgetToDB(currentMonthYear, limit)) {
                        JOptionPane.showMessageDialog(null, "Monthly Budget set for " + currentMonthYear + ": ₹" + limit);
                        txtBudgetLimit.setText("");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid numeric amount!", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnAddExpense.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String cat = cmbCategory.getSelectedItem().toString();
                    String desc = txtDescription.getText().trim();
                    double amt = Double.parseDouble(txtAmount.getText().trim());

                    if (amt <= 0) {
                        JOptionPane.showMessageDialog(null, "Please enter an amount greater than 0!", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (saveExpenseToDB(amt, cat, desc)) {
                        JOptionPane.showMessageDialog(null, "Expense logged under '" + cat + "' successfully!");

                        double monthlyBudgetLimit = getMonthlyBudgetLimitFromDB(currentMonthYear);
                        double totalMonthlySpent = getTotalMonthlyExpenseFromDB();

                        if (monthlyBudgetLimit > 0 && totalMonthlySpent > monthlyBudgetLimit) {
                            JOptionPane.showMessageDialog(null, 
                                "⚠️ MONTHLY BUDGET LIMIT EXCEEDED!\n" +
                                "Your total expenses for " + currentMonthYear + " reached ₹" + totalMonthlySpent + ".\n" +
                                "This is more than your set budget of ₹" + monthlyBudgetLimit + "!", 
                                "Budget Limit Crossed!", JOptionPane.WARNING_MESSAGE);
                        }

                        txtDescription.setText("");
                        txtAmount.setText("");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid numeric amount for expense!", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnClearData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(null, 
                    "Are you sure you want to delete all expenses and budget logs?", 
                    "Confirm Clear", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    clearAllDataFromDB();
                }
            }
        });
    }

    private JPanel createCardPanel(Color bg, String title, Font titleFont, Color titleColor) {
        JPanel panel = new JPanel();
        panel.setBackground(bg);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                new LineBorder(new Color(209, 213, 219), 1, true),
                " " + title + " ",
                0, 0, titleFont, titleColor
            ),
            new EmptyBorder(12, 15, 12, 15)
        ));
        return panel;
    }

    private JTextField styleTextField(Font font) {
        JTextField tf = new JTextField();
        tf.setFont(font);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(156, 163, 175), 1, true),
            new EmptyBorder(5, 8, 5, 8)
        ));
        return tf;
    }

    private JButton styleButton(String text, Color bg, Font font) {
        JButton btn = new JButton(text);
        btn.setFont(font);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private boolean saveBudgetToDB(String monthYear, double limitAmount) {
        String query = "INSERT INTO Budget (month_year, limit_amount) VALUES (?, ?) " +
                       "ON DUPLICATE KEY UPDATE limit_amount = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, monthYear);
            pstmt.setDouble(2, limitAmount);
            pstmt.setDouble(3, limitAmount);
            pstmt.executeUpdate();
            return true;
        } catch (Exception ex) { 
            ex.printStackTrace(); 
            JOptionPane.showMessageDialog(null, "Database Error: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean saveExpenseToDB(double amount, String category, String description) {
        String query = "INSERT INTO Expenses (amount, category, description) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, category);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
            return true;
        } catch (Exception ex) { 
            ex.printStackTrace(); 
            JOptionPane.showMessageDialog(null, "Database Error: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private double getMonthlyBudgetLimitFromDB(String monthYear) {
        String query = "SELECT limit_amount FROM Budget ORDER BY id DESC LIMIT 1";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) return rs.getDouble("limit_amount");
        } catch (Exception ex) { 
            ex.printStackTrace(); 
        }
        return 0;
    }

    private double getTotalMonthlyExpenseFromDB() {
        String query = "SELECT COALESCE(SUM(amount), 0) FROM Expenses";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception ex) { 
            ex.printStackTrace(); 
        }
        return 0;
    }

    private void clearAllDataFromDB() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM Expenses");
            stmt.executeUpdate("DELETE FROM Budget");
            JOptionPane.showMessageDialog(null, "Database cleared successfully! All logs deleted.");
        } catch (Exception ex) { 
            ex.printStackTrace(); 
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SmartExpenseTracker().setVisible(true);
            }
        });
    }
}