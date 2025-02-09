import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class StudentManagementSystem extends JFrame {
    private Connection connection;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTable recordsTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton removeButton;

    public StudentManagementSystem() {
        initializeDBConnection();
        initializeLoginPage();
    }

    private void initializeDBConnection() {
        try {
            System.out.println("Loading JDBC Driver...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/kccstd", "root", "");
            System.out.println("Connection established.");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Connection Error: " + e.getMessage());
        }
    }

    private void initializeLoginPage() {
        setTitle("Login");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Email:"));
        emailField = new JTextField();
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> login());
        panel.add(loginButton);

        add(panel);
    }

    private void login() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Svalidation WHERE email = ? AND password = ?");
            statement.setString(1, email);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                initializeRecordsPage();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid email or password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            System.out.println("Email or Password Wrong: " + e.getMessage());
        }
    }

    private void initializeRecordsPage() {
        setTitle("Student Records");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"Name", "Email", "Address", "Phone Number", "Semester", "Course"}, 0);
        recordsTable = new JTable(tableModel);
        loadRecords();

        addButton = new JButton("Add");
        removeButton = new JButton("Remove");

        addButton.addActionListener(e -> {
            if (e.getSource() == addButton) {
                addRecord();
            }
        });

        removeButton.addActionListener(e -> {
            if (e.getSource() == removeButton) {
                removeRecord();
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(recordsTable), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(panel);
        revalidate();
        repaint();
    }

    private void loadRecords() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Srecord");

            while (resultSet.next()) {
                tableModel.addRow(new Object[]{
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getString("address"),
                        resultSet.getString("phnumber"),
                        resultSet.getString("semester"),
                        resultSet.getString("course")
                });
            }
        } catch (SQLException e) {
            System.out.println("Failed to connect with the database: " + e.getMessage());
        }
    }

    private void addRecord() {
        String name = JOptionPane.showInputDialog(this, "Enter Name:");
        String email = JOptionPane.showInputDialog(this, "Enter Email:");
        String address = JOptionPane.showInputDialog(this, "Enter Address:");
        String phnumber = JOptionPane.showInputDialog(this, "Enter Phone Number:");
        String semester = JOptionPane.showInputDialog(this, "Enter Semester:");
        String course = JOptionPane.showInputDialog(this, "Enter Course:");

        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO Srecord (name, email, address, phnumber, semester, course) VALUES (?, ?, ?, ?, ?, ?)");
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, address);
            statement.setString(4, phnumber);
            statement.setString(5, semester);
            statement.setString(6, course);

            int rowsAffected = statement.executeUpdate();  // Get the number of affected rows
            if (rowsAffected > 0) {
                tableModel.addRow(new Object[]{name, email, address, phnumber, semester, course});
                JOptionPane.showMessageDialog(this, "Record added successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add record.");
            }
        } catch (SQLException e) {
            System.out.println("Error adding record: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeRecord() {
        int selectedRow = recordsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String email = (String) tableModel.getValueAt(selectedRow, 1);
            System.out.println("Removing record with email: " + email);  // Debugging log

            try {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM Srecord WHERE email = ?");
                statement.setString(1, email);

                int rowsAffected = statement.executeUpdate();  // Get the number of affected rows
                if (rowsAffected > 0) {
                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this, "Record removed successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "No record found with that email.");
                }
            } catch (SQLException e) {
                System.out.println("Error removing record: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a record to remove", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        StudentManagementSystem sms = new StudentManagementSystem();
        sms.setVisible(true);
    }
}

// javac -cp ".;A:\6th\Mini\lib\mysql-connector-j-9.2.0.jar" StudentManagementSystem.java
//>> java -cp ".;A:\6th\Mini\lib\mysql-connector-j-9.2.0.jar" StudentManagementSystem