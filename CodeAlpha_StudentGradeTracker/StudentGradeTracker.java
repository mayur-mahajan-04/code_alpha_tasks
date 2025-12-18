package CodeAlpha_StudentGradeTracker;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class StudentGradeTracker extends JFrame {

    private JTextField nameField, marksField;
    private JButton addButton, summaryButton;
    private JTable table;
    private DefaultTableModel model;

    private ArrayList<String> studentNames = new ArrayList<>();
    private ArrayList<Integer> studentMarks = new ArrayList<>();

    public StudentGradeTracker() {

        // ---------- WINDOW ----------
        setTitle("Student Grade Tracker");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));

        // ---------- TITLE ----------
        JLabel title = new JLabel("Student Grade Tracker", JLabel.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setBorder(new EmptyBorder(15, 10, 15, 10));
        title.setForeground(new Color(52, 73, 94));
        add(title, BorderLayout.NORTH);

        // ---------- INPUT PANEL ----------
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        inputPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLbl = new JLabel("Student Name:");
        nameLbl.setFont(new Font("SansSerif", Font.PLAIN, 16));
        inputPanel.add(nameLbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        nameField = new JTextField(20);
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        inputPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel marksLbl = new JLabel("Marks (0-100):");
        marksLbl.setFont(new Font("SansSerif", Font.PLAIN, 16));
        inputPanel.add(marksLbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        marksField = new JTextField(20);
        marksField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        inputPanel.add(marksField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.5;
        addButton = createButton("Add Student", new Color(52, 152, 219));
        inputPanel.add(addButton, gbc);

        gbc.gridx = 1;
        summaryButton = createButton("Show Summary", new Color(46, 204, 113));
        inputPanel.add(summaryButton, gbc);

        add(inputPanel, BorderLayout.NORTH);

        // ---------- TABLE SECTION ----------
        model = new DefaultTableModel(new String[]{"Student Name", "Marks"}, 0);

        table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 15));
        table.setRowHeight(28);
        table.setGridColor(new Color(220, 220, 220));

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(52, 73, 94));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 16));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(10, 20, 20, 20));
        add(scroll, BorderLayout.CENTER);

        // ---------- ACTION LISTENERS ----------
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addStudent();
            }
        });
        summaryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showSummary();
            }
        });

        setVisible(true);
    }

    // CUSTOM ROUNDED COLOR BUTTON
    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(bg.darker(), 2));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Rounded corners
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.putClientProperty("JComponent.roundRect", true);

        return btn;
    }

    // ADDING STUDENT
    private void addStudent() {
        String name = nameField.getText().trim();
        String marksText = marksField.getText().trim();

        if (name.isEmpty() || marksText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter name and marks", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int marks = Integer.parseInt(marksText);

            if (marks < 0 || marks > 100) {
                JOptionPane.showMessageDialog(this, "Marks must be between 0 and 100!", "Invalid Marks", JOptionPane.WARNING_MESSAGE);
                return;
            }

            studentNames.add(name);
            studentMarks.add(marks);

            model.addRow(new Object[]{name, marks});

            nameField.setText("");
            marksField.setText("");

            JOptionPane.showMessageDialog(this, "Student Added Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid marks! Enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // SUMMARY FUNCTION
    private void showSummary() {
        if (studentNames.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records found!", "Summary", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int total = 0;
        int highest = studentMarks.get(0);
        int lowest = studentMarks.get(0);

        for (int m : studentMarks) {
            total += m;
            if (m > highest) highest = m;
            if (m < lowest) lowest = m;
        }

        double avg = (double) total / studentMarks.size();

        StringBuilder sb = new StringBuilder();
        sb.append("SUMMARY REPORT\n\n");
        sb.append("Total Students: ").append(studentNames.size()).append("\n");
        sb.append("Average Marks: ").append(String.format("%.2f", avg)).append("\n");
        sb.append("Highest Marks: ").append(highest).append("\n");
        sb.append("Lowest Marks: ").append(lowest);

        JOptionPane.showMessageDialog(this, sb.toString(), "Summary Report", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        new StudentGradeTracker();
    }
} 