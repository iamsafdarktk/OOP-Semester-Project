import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.Random;

/**
 * Interactive Exam Portal System
 * A comprehensive Java-based online examination platform
 * 
 * Features:
 * - Teacher Portal: Create and manage quizzes
 * - Student Portal: Attempt quizzes and view results
 * - Question Bank: Manage reusable questions
 * - Auto-Grading: Instant score calculation
 * - Results Tracking: Student performance analytics
 * 
 * @author Shahzaib Ullah Khattak & Muhammad Safdar Khan
 * @version 1.0
 */
public class ExamPortal extends JFrame {

    // ================= DATABASE =================
    /**
     * Database connection utility class
     */
    static class DB {
        /**
         * Establishes connection to MySQL database
         * @return Connection object or null if connection fails
         */
        static Connection getConnection() {
            try {
                return DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/Quiz",
                        "root",
                        "1122@"
                );
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage());
                return null;
            }
        }
    }

    // ================= GLOBAL VARIABLES =================
    CardLayout card = new CardLayout();
    JPanel mainPanel = new JPanel(card);

    JTextField loginUser = new JTextField();
    JPasswordField loginPass = new JPasswordField();

    int currentUserId = -1;

    Question[] currentQuiz;
    int totalLoadedQuestions = 0;

    int qIndex = 0;
    int score = 0;
    int perQ = 0;
    int quizId = -1;

    // ================= CUSTOM COLORS =================
    Color bgLight = new Color(236, 240, 241);
    Color textDark = new Color(44, 62, 80);
    Color primaryBlue = new Color(41, 128, 185);
    Color successGreen = new Color(39, 174, 96);
    Color warningOrange = new Color(211, 84, 0);
    Color dangerRed = new Color(192, 57, 43);
    Color purpleAccent = new Color(142, 68, 173);

    // ================= QUESTION MODEL =================
    /**
     * Question class - Encapsulates question data
     * Demonstrates encapsulation principle
     */
    class Question {
        String q, a, b, c, d, correct, userAns;

        /**
         * Constructor for Question
         * @param q Question text
         * @param a Option A
         * @param b Option B
         * @param c Option C
         * @param d Option D
         * @param correct Correct answer
         */
        Question(String q, String a, String b, String c, String d, String correct) {
            this.q = q; this.a = a; this.b = b; this.c = c; this.d = d;
            this.correct = correct; this.userAns = "";
        }
    }

    // ================= UI HELPER =================
    /**
     * Styles a JButton with custom colors and fonts
     * @param btn Button to style
     * @param bg Background color
     * @param fg Foreground color
     */
    void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // ================= CONSTRUCTOR =================
    /**
     * Constructor - Initializes the main application window
     */
    public ExamPortal() {
        setTitle("Interactive Exam Portal System");
        setSize(1000, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainPanel.add(loginPanel(), "login");
        mainPanel.add(teacherPanel(), "teacher");
        mainPanel.add(studentPanel(), "student");

        add(mainPanel);
        card.show(mainPanel, "login");
        setVisible(true);
    }

    // ================= LOGIN PANEL =================
    /**
     * Creates the login panel UI
     * @return JPanel with login interface
     */
    JPanel loginPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(bgLight);

        JLabel t = new JLabel("EXAM PORTAL", SwingConstants.CENTER);
        t.setBounds(350, 60, 300, 50);
        t.setFont(new Font("Arial", Font.BOLD, 36));
        t.setForeground(primaryBlue);
        p.add(t);

        JLabel ul = new JLabel("Username:");
        ul.setBounds(300, 160, 120, 30);
        ul.setFont(new Font("Arial", Font.BOLD, 18));
        ul.setForeground(textDark);
        p.add(ul);

        loginUser.setBounds(420, 160, 230, 35);
        loginUser.setFont(new Font("Arial", Font.PLAIN, 16));

        JLabel pl = new JLabel("Password:");
        pl.setBounds(300, 220, 120, 30);
        pl.setFont(new Font("Arial", Font.BOLD, 18));
        pl.setForeground(textDark);
        p.add(pl);

        loginPass.setBounds(420, 220, 230, 35);
        loginPass.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton l1 = new JButton("Login as Teacher");
        JButton l2 = new JButton("Login as Student");
        JButton registerBtn = new JButton("Create New Account");

        l1.setBounds(300, 290, 170, 45);
        l2.setBounds(480, 290, 170, 45);
        registerBtn.setBounds(300, 350, 350, 40);

        styleButton(l1, warningOrange, Color.WHITE);
        styleButton(l2, successGreen, Color.WHITE);
        styleButton(registerBtn, primaryBlue, Color.WHITE);

        p.add(loginUser);
        p.add(loginPass);
        p.add(l1);
        p.add(l2);
        p.add(registerBtn);

        l1.addActionListener(e -> login("teachers"));
        l2.addActionListener(e -> login("students"));
        registerBtn.addActionListener(e -> createAccount());

        return p;
    }

    // ================= ACCOUNT CREATION =================
    /**
     * Creates a new user account (Teacher or Student)
     */
    void createAccount() {
        JTextField regUser = new JTextField();
        JPasswordField regPass = new JPasswordField();
        String[] roles = {"Student", "Teacher"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);

        Object[] message = {
                "Select Role:", roleCombo,
                "Username:", regUser,
                "Password:", regPass
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Create New Account", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String u = regUser.getText().trim();
            String p = new String(regPass.getPassword()).trim();
            String role = roleCombo.getSelectedItem().toString();
            String table = role.equals("Teacher") ? "teachers" : "students";

            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and Password cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Connection con = DB.getConnection();
                if (con == null) return;

                // Check if username already exists
                PreparedStatement check = con.prepareStatement("SELECT id FROM " + table + " WHERE username=?");
                check.setString(1, u);
                ResultSet rs = check.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Username already exists! Please choose a different one.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Insert new account
                PreparedStatement ps = con.prepareStatement("INSERT INTO " + table + " (username, password) VALUES (?, ?)");
                ps.setString(1, u);
                ps.setString(2, p);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Account successfully created! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Registration Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Authenticates user login
     * @param table Database table name (teachers or students)
     */
    void login(String table) {
        try {
            Connection con = DB.getConnection();
            if (con == null) return;

            PreparedStatement ps = con.prepareStatement(
                    "SELECT id FROM " + table + " WHERE username=? AND password=?"
            );

            ps.setString(1, loginUser.getText());
            ps.setString(2, new String(loginPass.getPassword()));

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                currentUserId = rs.getInt("id");
                card.show(mainPanel, table.equals("teachers") ? "teacher" : "student");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e);
        }
    }

    // ================= TEACHER PANEL =================
    /**
     * Creates the teacher portal UI
     * @return JPanel with teacher dashboard
     */
    JPanel teacherPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(bgLight);

        JPanel side = new JPanel(new GridLayout(8, 1, 5, 5));
        side.setBackground(textDark);
        side.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextArea area = new JTextArea();
        area.setLineWrap(true);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        area.setBackground(new Color(253, 253, 253));
        area.setForeground(textDark);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryBlue, 2), "Teacher Dashboard"));

        JButton addQ = new JButton("Add Single Question");
        JButton bulkQ = new JButton("Paste Questions");
        JButton createQ = new JButton("Create New Quiz");
        JButton viewQ = new JButton("View My Quizzes");
        JButton editQ = new JButton("Edit Existing Quiz");
        JButton viewRes = new JButton("View Student Results");
        JButton delQ = new JButton("Delete from Bank");
        JButton logout = new JButton("Logout");

        styleButton(addQ, primaryBlue, Color.WHITE);
        styleButton(bulkQ, primaryBlue, Color.WHITE);
        styleButton(createQ, successGreen, Color.WHITE);
        styleButton(viewQ, purpleAccent, Color.WHITE);
        styleButton(editQ, warningOrange, Color.WHITE);
        styleButton(viewRes, purpleAccent, Color.WHITE);
        styleButton(delQ, dangerRed, Color.WHITE);
        styleButton(logout, textDark, Color.WHITE);
        logout.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));

        side.add(addQ); side.add(bulkQ); side.add(createQ); side.add(viewQ);
        side.add(editQ); side.add(viewRes); side.add(delQ); side.add(logout);

        root.add(side, BorderLayout.WEST);
        root.add(scroll, BorderLayout.CENTER);

        addQ.addActionListener(e -> addQuestion());
        bulkQ.addActionListener(e -> bulkAddQuestions());
        createQ.addActionListener(e -> createQuiz(area));
        viewQ.addActionListener(e -> showMyQuizzes(area));
        editQ.addActionListener(e -> editQuiz());
        viewRes.addActionListener(e -> viewStudentResults(area));
        delQ.addActionListener(e -> deleteQuestionFromBank());
        logout.addActionListener(e -> logout());

        return root;
    }

    // ================= TEACHER METHODS =================
    /**
     * Adds a single question to the question bank
     */
    void addQuestion() {
        try {
            Connection con = DB.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO question_bank (subject, question, a, b, c, d, correct) VALUES(?,?,?,?,?,?,?)");
            ps.setString(1, JOptionPane.showInputDialog("Subject"));
            ps.setString(2, JOptionPane.showInputDialog("Question Text"));
            ps.setString(3, JOptionPane.showInputDialog("Option A"));
            ps.setString(4, JOptionPane.showInputDialog("Option B"));
            ps.setString(5, JOptionPane.showInputDialog("Option C"));
            ps.setString(6, JOptionPane.showInputDialog("Option D"));
            ps.setString(7, JOptionPane.showInputDialog("Correct Answer (Exact Match)"));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Question added successfully to the bank!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e);
        }
    }

    /**
     * Bulk imports questions from text in specified format
     */
    void bulkAddQuestions() {
        JTextArea ta = new JTextArea(20, 50);
        ta.setText("Paste questions here. FORMAT MUST BE EXACTLY LIKE THIS:\n\n" +
                "Subject: General\nQ: What is 2+2?\nA: 1\nB: 2\nC: 3\nD: 4\nAns: 4\n\n");

        int res = JOptionPane.showConfirmDialog(this, new JScrollPane(ta), "Bulk Paste Questions", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                Connection con = DB.getConnection();
                String[] blocks = ta.getText().split("\\n\\s*\\n");
                int count = 0;

                for (String block : blocks) {
                    if (block.trim().isEmpty() || block.startsWith("Paste")) continue;
                    String[] lines = block.split("\\n");
                    String sub = "", q = "", a = "", b = "", c = "", d = "", ans = "";
                    for (String line : lines) {
                        if (line.startsWith("Subject:")) sub = line.substring(8).trim();
                        else if (line.startsWith("Q:")) q = line.substring(2).trim();
                        else if (line.startsWith("A:")) a = line.substring(2).trim();
                        else if (line.startsWith("B:")) b = line.substring(2).trim();
                        else if (line.startsWith("C:")) c = line.substring(2).trim();
                        else if (line.startsWith("D:")) d = line.substring(2).trim();
                        else if (line.startsWith("Ans:")) ans = line.substring(4).trim();
                    }
                    if (!q.isEmpty() && !ans.isEmpty()) {
                        PreparedStatement ps = con.prepareStatement("INSERT INTO question_bank (subject, question, a, b, c, d, correct) VALUES(?,?,?,?,?,?,?)");
                        ps.setString(1, sub); ps.setString(2, q); ps.setString(3, a); ps.setString(4, b);
                        ps.setString(5, c); ps.setString(6, d); ps.setString(7, ans);
                        ps.executeUpdate();
                        count++;
                    }
                }
                JOptionPane.showMessageDialog(this, count + " questions successfully imported!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error parsing text: " + e.getMessage());
            }
        }
    }

    /**
     * Creates a new quiz from the question bank
     * @param area Text area to display results
     */
    void createQuiz(JTextArea area) {
        try {
            Connection con = DB.getConnection();
            String subject = JOptionPane.showInputDialog("Enter Subject (must match bank exactly):");
            String title = JOptionPane.showInputDialog("Quiz Title:");
            String password = String.format("%04d", new Random().nextInt(10000));
            int n = Integer.parseInt(JOptionPane.showInputDialog("Number of Questions:"));
            int marks = Integer.parseInt(JOptionPane.showInputDialog("Total Marks for this Quiz:"));

            String code = "QZ" + new Random().nextInt(9999);

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO quizzes(subject,title,code,password,total_questions,max_marks,teacher_id) VALUES(?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, subject); ps.setString(2, title); ps.setString(3, code); ps.setString(4, password);
            ps.setInt(5, n); ps.setInt(6, marks); ps.setInt(7, currentUserId);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            quizId = rs.getInt(1);

            PreparedStatement ps2 = con.prepareStatement("SELECT * FROM question_bank WHERE subject=? ORDER BY RAND() LIMIT ?");
            ps2.setString(1, subject); ps2.setInt(2, n);
            ResultSet rq = ps2.executeQuery();

            area.setText(" QUIZ CREATED SUCCESSFULLY \n\nCode to share: " + code + "\nPassword to share: " + password + "\n\nQuestions Included:\n");

            while (rq.next()) {
                PreparedStatement ins = con.prepareStatement("INSERT INTO questions (quiz_id, question, a, b, c, d, correct) VALUES(?,?,?,?,?,?,?)");
                ins.setInt(1, quizId); ins.setString(2, rq.getString("question"));
                ins.setString(3, rq.getString("a")); ins.setString(4, rq.getString("b"));
                ins.setString(5, rq.getString("c")); ins.setString(6, rq.getString("d"));
                ins.setString(7, rq.getString("correct"));
                ins.executeUpdate();
                area.append(" ✔ " + rq.getString("question") + "\n");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e);
        }
    }

    /**
     * Displays all quizzes created by the current teacher
     * @param area Text area to display results
     */
    void showMyQuizzes(JTextArea area) {
        try {
            Connection con = DB.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM quizzes WHERE teacher_id=?");
            ps.setInt(1, currentUserId);
            ResultSet rs = ps.executeQuery();
            area.setText(" YOUR CREATED QUIZZES \n");

            while (rs.next()) {
                int qid = rs.getInt("id");
                area.append("\n=========================================\n");
                area.append(" Quiz ID: " + qid + " | Title: " + rs.getString("title") + "\n");
                area.append(" Code: " + rs.getString("code") + "  |  Password: " + rs.getString("password") + "\n");
                area.append(" Total Questions: " + rs.getInt("total_questions") + " | Max Marks: " + rs.getInt("max_marks") + "\n\n");

                PreparedStatement ps2 = con.prepareStatement("SELECT * FROM questions WHERE quiz_id=?");
                ps2.setInt(1, qid);
                ResultSet rq = ps2.executeQuery();
                while (rq.next()) {
                    area.append("  [ID: " + rq.getInt("id") + "] " + rq.getString("question") + "\n");
                }
                area.append("=========================================\n");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e);
        }
    }

    /**
     * Edits an existing quiz (title, questions, etc.)
     */
    void editQuiz() {
        try {
            Connection con = DB.getConnection();
            String quizIdStr = JOptionPane.showInputDialog("Enter Quiz ID to Edit:");
            if (quizIdStr == null || quizIdStr.isEmpty()) return;
            int qzId = Integer.parseInt(quizIdStr);
            String[] options = {"Change Title/Code", "Edit a Question", "Add New Question", "Remove Question", "Done"};

            while (true) {
                int choice = JOptionPane.showOptionDialog(this, "Editing Quiz ID: " + qzId, "Interactive Edit Menu",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

                if (choice == 0) {
                    String title = JOptionPane.showInputDialog("New Title (leave blank to skip):");
                    if(title != null && !title.isEmpty()) {
                        PreparedStatement p1 = con.prepareStatement("UPDATE quizzes SET title=? WHERE id=?");
                        p1.setString(1, title); p1.setInt(2, qzId); p1.executeUpdate();
                    }
                } else if (choice == 1) {
                    int qId = Integer.parseInt(JOptionPane.showInputDialog("Enter Question ID to modify:"));
                    String newQ = JOptionPane.showInputDialog("New Question Text:");
                    String newA = JOptionPane.showInputDialog("Option A:");
                    String newB = JOptionPane.showInputDialog("Option B:");
                    String newC = JOptionPane.showInputDialog("Option C:");
                    String newD = JOptionPane.showInputDialog("Option D:");
                    String newCorrect = JOptionPane.showInputDialog("Correct Answer:");

                    PreparedStatement ps2 = con.prepareStatement("UPDATE questions SET question=?, a=?, b=?, c=?, d=?, correct=? WHERE id=? AND quiz_id=?");
                    ps2.setString(1, newQ); ps2.setString(2, newA); ps2.setString(3, newB);
                    ps2.setString(4, newC); ps2.setString(5, newD); ps2.setString(6, newCorrect);
                    ps2.setInt(7, qId); ps2.setInt(8, qzId);
                    if(ps2.executeUpdate() > 0) JOptionPane.showMessageDialog(this, "Question Updated!");
                } else if (choice == 2) {
                    String newQ = JOptionPane.showInputDialog("Question Text:");
                    String newA = JOptionPane.showInputDialog("Option A:");
                    String newB = JOptionPane.showInputDialog("Option B:");
                    String newC = JOptionPane.showInputDialog("Option C:");
                    String newD = JOptionPane.showInputDialog("Option D:");
                    String newCorrect = JOptionPane.showInputDialog("Correct Answer:");

                    PreparedStatement ins = con.prepareStatement("INSERT INTO questions (quiz_id, question, a, b, c, d, correct) VALUES(?,?,?,?,?,?,?)");
                    ins.setInt(1, qzId); ins.setString(2, newQ); ins.setString(3, newA);
                    ins.setString(4, newB); ins.setString(5, newC); ins.setString(6, newD); ins.setString(7, newCorrect);
                    ins.executeUpdate();

                    updateQuizTotalCount(con, qzId);
                    JOptionPane.showMessageDialog(this, "Question Added directly to Quiz!");
                } else if (choice == 3) {
                    int qId = Integer.parseInt(JOptionPane.showInputDialog("Enter Question ID to Remove:"));
                    PreparedStatement del = con.prepareStatement("DELETE FROM questions WHERE id=? AND quiz_id=?");
                    del.setInt(1, qId); del.setInt(2, qzId);
                    if(del.executeUpdate() > 0) {
                        updateQuizTotalCount(con, qzId);
                        JOptionPane.showMessageDialog(this, "Question Removed from Quiz!");
                    }
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Cancelled or Error: " + e.getMessage());
        }
    }

    /**
     * Updates the total question count for a quiz
     * @param con Database connection
     * @param qzId Quiz ID
     * @throws SQLException
     */
    void updateQuizTotalCount(Connection con, int qzId) throws Exception {
        PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM questions WHERE quiz_id=?");
        ps.setInt(1, qzId);
        ResultSet rs = ps.executeQuery();
        if(rs.next()) {
            PreparedStatement ps2 = con.prepareStatement("UPDATE quizzes SET total_questions=? WHERE id=?");
            ps2.setInt(1, rs.getInt(1)); ps2.setInt(2, qzId); ps2.executeUpdate();
        }
    }

    /**
     * Deletes a question from the question bank
     */
    void deleteQuestionFromBank() {
        try {
            int id = Integer.parseInt(JOptionPane.showInputDialog("Enter Question ID from Bank to Delete:"));
            Connection con = DB.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM question_bank WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Deleted successfully from Bank.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e);
        }
    }

    /**
     * Views student results for a specific quiz
     * @param area Text area to display results
     */
    void viewStudentResults(JTextArea area) {
        try {
            String qzIdStr = JOptionPane.showInputDialog("Enter Quiz ID to check results:");
            if (qzIdStr == null || qzIdStr.isEmpty()) return;
            int qzId = Integer.parseInt(qzIdStr);

            Connection con = DB.getConnection();
            PreparedStatement check = con.prepareStatement("SELECT title, max_marks FROM quizzes WHERE id=? AND teacher_id=?");
            check.setInt(1, qzId);
            check.setInt(2, currentUserId);
            ResultSet rsCheck = check.executeQuery();

            if(!rsCheck.next()) {
                JOptionPane.showMessageDialog(this, "Invalid Quiz ID or this is not your quiz.", "Access Denied", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String quizTitle = rsCheck.getString("title");
            int maxMarks = rsCheck.getInt("max_marks");

            PreparedStatement ps = con.prepareStatement(
                    "SELECT s.username, r.score FROM results r JOIN students s ON r.student_id = s.id WHERE r.quiz_id = ?"
            );
            ps.setInt(1, qzId);
            ResultSet rs = ps.executeQuery();

            area.setText("RESULTS FOR: " + quizTitle + " (Max Marks: " + maxMarks + ") \n\n");
            boolean hasResults = false;

            while(rs.next()) {
                hasResults = true;
                area.append(" Student: " + rs.getString("username") + " |  Score: " + rs.getInt("score") + "\n");
                area.append("--------------------------------------------------\n");
            }

            if(!hasResults) {
                area.append("No students have attempted this quiz yet.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e);
        }
    }

    // ================= STUDENT PANEL =================
    /**
     * Creates the student portal UI
     * @return JPanel with student dashboard
     */
    JPanel studentPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(bgLight);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        top.setBackground(textDark);

        JButton startBtn = new JButton("Attempt a Quiz");
        JButton resultsBtn = new JButton("My Result History");
        JButton logoutBtn = new JButton("Logout");

        styleButton(startBtn, successGreen, Color.WHITE);
        styleButton(resultsBtn, primaryBlue, Color.WHITE);
        styleButton(logoutBtn, dangerRed, Color.WHITE);

        top.add(startBtn);
        top.add(resultsBtn);
        top.add(logoutBtn);

        JTextArea stArea = new JTextArea();
        stArea.setEditable(false);
        stArea.setLineWrap(true);
        stArea.setFont(new Font("Arial", Font.PLAIN, 15));
        stArea.setBackground(new Color(253, 253, 253));

        JScrollPane scroll = new JScrollPane(stArea);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        p.add(top, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);

        startBtn.addActionListener(e -> startQuiz());
        resultsBtn.addActionListener(e -> showMyResults(stArea));
        logoutBtn.addActionListener(e -> logout());

        return p;
    }

    /**
     * Shows student's quiz history and results
     * @param area Text area to display results
     */
    void showMyResults(JTextArea area) {
        try {
            Connection con = DB.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT q.title, q.code, q.max_marks, r.score FROM results r JOIN quizzes q ON r.quiz_id = q.id WHERE r.student_id = ?"
            );
            ps.setInt(1, currentUserId);
            ResultSet rs = ps.executeQuery();

            area.setText(" YOUR QUIZ HISTORY \n\n");
            boolean found = false;

            while(rs.next()) {
                found = true;
                area.append(  " Quiz    : " + rs.getString("title")
                            + " Code    : " + rs.getString("code") + ")\n");
                area.append(  " Score   : " + rs.getInt("score") + " out of " + rs.getInt("max_marks") + "\n");
                area.append("--------------------------------------------------\n");
            }

            if(!found) {
                area.setText("You haven't attempted any quizzes yet.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e);
        }
    }

    // ================= START QUIZ =================
    /**
     * Starts a quiz attempt by verifying code and password
     */
    void startQuiz() {
        try {
            String code = JOptionPane.showInputDialog("Enter Quiz Code:");
            if (code == null) return;

            Connection con = DB.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM quizzes WHERE code=?");
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Quiz Code Not Found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String pass = JOptionPane.showInputDialog("Enter Quiz Password:");
            if (pass == null) return;

            if (!pass.equals(rs.getString("password"))) {
                JOptionPane.showMessageDialog(this, "Wrong Password", "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }

            quizId = rs.getInt("id");
            int totalQuestionsInDB = rs.getInt("total_questions");

            if (totalQuestionsInDB == 0) {
                JOptionPane.showMessageDialog(this, "This quiz has no questions.", "Empty Quiz", JOptionPane.WARNING_MESSAGE);
                return;
            }

            perQ = rs.getInt("max_marks") / totalQuestionsInDB;

            currentQuiz = new Question[totalQuestionsInDB];
            totalLoadedQuestions = 0;
            qIndex = 0;
            score = 0;

            PreparedStatement ps2 = con.prepareStatement("SELECT * FROM questions WHERE quiz_id=?");
            ps2.setInt(1, quizId);
            ResultSet rq = ps2.executeQuery();

            while (rq.next() && totalLoadedQuestions < totalQuestionsInDB) {
                currentQuiz[totalLoadedQuestions] = new Question(
                        rq.getString("question"), rq.getString("a"), rq.getString("b"),
                        rq.getString("c"), rq.getString("d"), rq.getString("correct")
                );
                totalLoadedQuestions++;
            }

            showNextQuestion();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e);
        }
    }

    // ================= MCQ DISPLAY UI =================
    /**
     * Displays the next question in the quiz
     * Handles quiz completion and feedback
     */
    void showNextQuestion() {

        if (qIndex >= totalLoadedQuestions || currentQuiz[qIndex] == null) {
            saveResult();
            showFeedback();
            return;
        }

        Question q = currentQuiz[qIndex];

        JFrame f = new JFrame("Live Quiz - Question " + (qIndex + 1) + " of " + totalLoadedQuestions);
        f.setSize(550, 450);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JPanel p = new JPanel(new GridLayout(5, 1, 15, 15));
        p.setBackground(bgLight);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel l = new JLabel("<html><b>Q" + (qIndex + 1) + ": " + q.q + "</b></html>");
        l.setFont(new Font("Arial", Font.PLAIN, 16));
        l.setForeground(textDark);

        JButton a = new JButton(q.a);
        JButton b = new JButton(q.b);
        JButton c = new JButton(q.c);
        JButton d = new JButton(q.d);

        styleButton(a, Color.WHITE, textDark); a.setBorder(BorderFactory.createLineBorder(primaryBlue, 2));
        styleButton(b, Color.WHITE, textDark); b.setBorder(BorderFactory.createLineBorder(primaryBlue, 2));
        styleButton(c, Color.WHITE, textDark); c.setBorder(BorderFactory.createLineBorder(primaryBlue, 2));
        styleButton(d, Color.WHITE, textDark); d.setBorder(BorderFactory.createLineBorder(primaryBlue, 2));

        p.add(l); p.add(a); p.add(b); p.add(c); p.add(d);
        f.add(p);

        java.awt.event.ActionListener al = e -> {
            JButton clicked = (JButton) e.getSource();
            q.userAns = clicked.getText();

            if (q.userAns.equalsIgnoreCase(q.correct)) {
                score += perQ;
            }

            qIndex++;
            f.dispose();
            showNextQuestion();
        };

        a.addActionListener(al); b.addActionListener(al);
        c.addActionListener(al); d.addActionListener(al);

        f.setVisible(true);
    }

    /**
     * Shows detailed feedback after quiz completion
     * it will display score and correct answers for each question
     */
    void showFeedback() {
        JTextArea fa = new JTextArea(15, 45);
        fa.setEditable(false);
        fa.setFont(new Font("Monospaced", Font.PLAIN, 14));
        fa.setBackground(new Color(250, 250, 250));

        int maxPossible = perQ * totalLoadedQuestions;
        fa.append(" QUIZ FINISHED! \n\n");
        fa.append(" Your Final Score: " + score + " / " + maxPossible + "\n\n");
        fa.append("========== DETAILED FEEDBACK ==========\n\n");

        for(int i = 0; i < totalLoadedQuestions; i++) {
            Question q = currentQuiz[i];
            fa.append("Q" + (i+1) + ": " + q.q + "\n");
            fa.append("Your Answer: " + q.userAns);

            if(q.userAns.equalsIgnoreCase(q.correct)) {
                fa.append("    CORRECT \n");
            } else {
                fa.append("    WRONG \n");
                fa.append("Correct Answer was: " + q.correct + "\n");
            }
            fa.append("--------------------------------------------------\n");
        }

        UIManager.put("OptionPane.background", bgLight);
        UIManager.put("Panel.background", bgLight);
        JOptionPane.showMessageDialog(this, new JScrollPane(fa), "Quiz Feedback & Results", JOptionPane.INFORMATION_MESSAGE);

        currentQuiz = null;
        quizId = -1;
    }

    /**
     * Saves the quiz result to the database
     */
    void saveResult() {
        try {
            Connection con = DB.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO results (student_id, quiz_id, score) VALUES(?,?,?)");
            ps.setInt(1, currentUserId);
            ps.setInt(2, quizId);
            ps.setInt(3, score);
            ps.executeUpdate();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Result Save Error: " + e.getMessage());
        }
    }

    /**
     * Logs out the current user
     */
    void logout() {
        currentUserId = -1;
        loginUser.setText("");
        loginPass.setText("");
        card.show(mainPanel, "login");
    }

    // ================= MAIN =================
    /**
     * Main method - Entry point of the application
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new ExamPortal());
    }
}
