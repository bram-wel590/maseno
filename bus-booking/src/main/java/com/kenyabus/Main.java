package com.kenyabus;

import com.kenyabusbooking.db.DBConnection;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class BusBookingApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private int currentUserId = -1;
    private String currentUserName = "";
    private String currentRole = "user";
    private int selectedScheduleId = -1;
    private String selectedBusName = "";
    private int totalSeats = 0;
    private List<Integer> selectedSeatsList = new ArrayList<>();

    public BusBookingApp() {
        setTitle("Kenya Bus Booking System");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) System.exit(0);
            }
        });

        setSize(450, 750);
        setMinimumSize(new Dimension(400, 650));
        setLocationRelativeTo(null);
        setResizable(true);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new LoginPanel(), "login");
        mainPanel.add(new RegisterPanel(), "register");
        mainPanel.add(new HomePanel(), "home");
        mainPanel.add(new SeatSelectionPanel(), "seat");
        mainPanel.add(new PaymentPanel(), "payment");
        mainPanel.add(new AdminDashboardPanel(), "admin");

        add(mainPanel);
        cardLayout.show(mainPanel, "login");
    }

    // ==================== BACKGROUND PANEL ====================
    private class BackgroundPanel extends JPanel {
        private Image bg;
        public BackgroundPanel(String imageName) {
            try {
                bg = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/" + imageName));
            } catch (Exception ignored) {}
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
        }
    }

    // ==================== LOGIN PANEL (b.jpg) ====================
    private class LoginPanel extends BackgroundPanel {
        public LoginPanel() { super("b.jpg"); setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(10, 20, 10, 20);

            JLabel title = new JLabel("Welcome to Kenya Bus Booking");
            title.setFont(new Font("Arial", Font.BOLD, 18));
            title.setForeground(Color.WHITE);

            JTextField phoneField = new JTextField(15);
            JPasswordField passField = new JPasswordField(15);

            JButton loginBtn = new JButton("Login");
            loginBtn.setBackground(new Color(0, 123, 255));
            loginBtn.setForeground(Color.WHITE);
            loginBtn.setFocusPainted(false);

            JButton registerBtn = new JButton("Create Account");
            registerBtn.setBackground(new Color(34, 139, 34));
            registerBtn.setForeground(Color.WHITE);

            JButton adminBtn = new JButton("Admin Login");
            adminBtn.setBackground(new Color(255, 140, 0));
            adminBtn.setForeground(Color.WHITE);

            c.gridx = 0; c.gridy = 0; add(title, c);
            c.gridy = 1; add(new JLabel("Phone (10 digits)"), c);
            c.gridy = 2; add(phoneField, c);
            c.gridy = 3; add(new JLabel("Password"), c);
            c.gridy = 4; add(passField, c);
            c.gridy = 5; add(loginBtn, c);
            c.gridy = 6; add(registerBtn, c);
            c.gridy = 7; add(adminBtn, c);

            loginBtn.addActionListener(e -> {
                String phone = phoneField.getText().trim();
                String pass = new String(passField.getPassword());
                if (phone.length() != 10 || !phone.matches("\\d+")) {
                    JOptionPane.showMessageDialog(this, "Phone must be exactly 10 digits!");
                    return;
                }
                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("SELECT user_id, name, role FROM users WHERE phone=? AND password=?");
                    ps.setString(1, phone);
                    ps.setString(2, pass);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        currentUserId = rs.getInt("user_id");
                        currentUserName = rs.getString("name");
                        currentRole = rs.getString("role");
                        JOptionPane.showMessageDialog(this, "Login successful! Welcome " + currentUserName);
                        cardLayout.show(mainPanel, "home");
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid phone or password");
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            registerBtn.addActionListener(e -> cardLayout.show(mainPanel, "register"));
            adminBtn.addActionListener(e -> cardLayout.show(mainPanel, "admin"));
        }
    }

    // ==================== REGISTER PANEL (u.jpg) ====================
    private class RegisterPanel extends BackgroundPanel {
        public RegisterPanel() { super("u.jpg"); setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(8, 20, 8, 20);

            JTextField nameField = new JTextField(15);
            JTextField phoneField = new JTextField(15);
            JPasswordField passField = new JPasswordField(15);

            // Simple math captcha
            int a = (int)(Math.random()*10)+1;
            int b = (int)(Math.random()*10)+1;
            JLabel captchaLbl = new JLabel(a + " + " + b + " = ?");
            captchaLbl.setForeground(Color.WHITE);
            JTextField captchaField = new JTextField(5);

            JButton registerBtn = new JButton("Register");
            registerBtn.setBackground(new Color(34, 139, 34));
            registerBtn.setForeground(Color.WHITE);

            // Name validation (max 20 chars, no numbers)
            nameField.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    if (nameField.getText().length() >= 20 || Character.isDigit(e.getKeyChar())) e.consume();
                }
            });
            // Phone validation
            phoneField.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    if (phoneField.getText().length() >= 10 || !Character.isDigit(e.getKeyChar())) e.consume();
                }
            });

            c.gridx = 0; c.gridy = 0; add(new JLabel("Full Name (max 20 chars)"), c);
            c.gridy = 1; add(nameField, c);
            c.gridy = 2; add(new JLabel("Phone (10 digits)"), c);
            c.gridy = 3; add(phoneField, c);
            c.gridy = 4; add(new JLabel("Password"), c);
            c.gridy = 5; add(passField, c);
            c.gridy = 6; add(captchaLbl, c);
            c.gridy = 7; add(captchaField, c);
            c.gridy = 8; add(registerBtn, c);

            registerBtn.addActionListener(e -> {
                if (!captchaField.getText().equals(String.valueOf(a + b))) {
                    JOptionPane.showMessageDialog(this, "Wrong captcha! You are a robot?");
                    return;
                }
                String name = nameField.getText().trim();
                String phone = phoneField.getText().trim();
                String pass = new String(passField.getPassword());
                if (name.isEmpty() || phone.length() != 10) {
                    JOptionPane.showMessageDialog(this, "Invalid name or phone!");
                    return;
                }
                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("INSERT INTO users (name, phone, password) VALUES (?,?,?)");
                    ps.setString(1, name);
                    ps.setString(2, phone);
                    ps.setString(3, pass);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Account created successfully! Please login.");
                    cardLayout.show(mainPanel, "login");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Phone number already exists!");
                }
            });
        }
    }

    // ==================== HOME PANEL (s.jpg) ====================
    private class HomePanel extends BackgroundPanel {
        public HomePanel() { super("s.jpg"); setLayout(new BorderLayout());

            JPanel top = new JPanel();
            JComboBox<String> fromBox = new JComboBox<>();
            JComboBox<String> toBox = new JComboBox<>();
            JButton searchBtn = new JButton("Search Buses");
            searchBtn.setBackground(new Color(0, 123, 255));
            searchBtn.setForeground(Color.WHITE);

            try (Connection conn = DBConnection.getConnection()) {
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT name FROM cities");
                while (rs.next()) {
                    fromBox.addItem(rs.getString("name"));
                    toBox.addItem(rs.getString("name"));
                }
            } catch (Exception ignored) {}

            top.add(new JLabel("From: ")); top.add(fromBox);
            top.add(new JLabel("To: ")); top.add(toBox);
            top.add(searchBtn);

            DefaultListModel<String> model = new DefaultListModel<>();
            JList<String> scheduleList = new JList<>(model);
            scheduleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            searchBtn.addActionListener(e -> {
                model.clear();
                String from = (String) fromBox.getSelectedItem();
                String to = (String) toBox.getSelectedItem();
                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(
                            "SELECT s.schedule_id, b.name, c1.name as from_city, c2.name as to_city, s.departure_date, s.departure_time, s.price " +
                                    "FROM schedules s JOIN buses b ON s.bus_id=b.bus_id " +
                                    "JOIN cities c1 ON s.from_city_id=c1.city_id " +
                                    "JOIN cities c2 ON s.to_city_id=c2.city_id " +
                                    "WHERE c1.name=? AND c2.name=?");
                    ps.setString(1, from);
                    ps.setString(2, to);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        model.addElement(rs.getString("name") + " | " + rs.getDate("departure_date") + " " +
                                rs.getTime("departure_time") + " | KES " + rs.getDouble("price") +
                                " | ID:" + rs.getInt("schedule_id"));
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            JButton selectBtn = new JButton("Select This Bus & Book Seat");
            selectBtn.setBackground(new Color(255, 140, 0));
            selectBtn.setForeground(Color.WHITE);
            selectBtn.addActionListener(e -> {
                String selected = scheduleList.getSelectedValue();
                if (selected == null) return;
                selectedScheduleId = Integer.parseInt(selected.substring(selected.lastIndexOf(":") + 1));
                // Get bus details
                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("SELECT b.name, b.total_seats FROM schedules s JOIN buses b ON s.bus_id=b.bus_id WHERE s.schedule_id=?");
                    ps.setInt(1, selectedScheduleId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        selectedBusName = rs.getString("name");
                        totalSeats = rs.getInt("total_seats");
                        cardLayout.show(mainPanel, "seat");
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            add(top, BorderLayout.NORTH);
            add(new JScrollPane(scheduleList), BorderLayout.CENTER);
            add(selectBtn, BorderLayout.SOUTH);
        }
    }

    // ==================== SEAT SELECTION PANEL (i.jpg) - with middle pathway ====================
    private class SeatSelectionPanel extends BackgroundPanel {
        public SeatSelectionPanel() { super("i.jpg"); setLayout(new BorderLayout()); }

        public void refresh() {
            removeAll();
            selectedSeatsList.clear();

            JPanel seatGrid = new JPanel(new GridLayout(0, 5, 8, 8));
            seatGrid.setOpaque(false);

            Set<Integer> booked = new HashSet<>();
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT seat_number FROM bookings WHERE schedule_id=? AND status='booked'");
                ps.setInt(1, selectedScheduleId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) booked.add(rs.getInt("seat_number"));
            } catch (Exception ignored) {}

            int seatNum = 1;
            for (int row = 0; row < (totalSeats + 3) / 4; row++) {
                for (int col = 0; col < 5; col++) {
                    if (col == 2) { // middle pathway
                        JLabel aisle = new JLabel("  ");
                        seatGrid.add(aisle);
                        continue;
                    }
                    if (seatNum > totalSeats) { seatGrid.add(new JLabel("")); continue; }

                    JButton seatBtn = new JButton(String.valueOf(seatNum));
                    seatBtn.setPreferredSize(new Dimension(45, 45));
                    if (booked.contains(seatNum)) {
                        seatBtn.setBackground(Color.RED);
                        seatBtn.setEnabled(false);
                    } else {
                        seatBtn.setBackground(new Color(34, 139, 34));
                    }

                    int finalSeat = seatNum;
                    seatBtn.addActionListener(e -> {
                        if (selectedSeatsList.contains(finalSeat)) {
                            selectedSeatsList.remove((Integer) finalSeat);
                            seatBtn.setBackground(new Color(34, 139, 34));
                        } else {
                            selectedSeatsList.add(finalSeat);
                            seatBtn.setBackground(Color.BLUE);
                        }
                    });
                    seatGrid.add(seatBtn);
                    seatNum++;
                }
            }

            JButton proceedBtn = new JButton("Proceed to Payment (" + selectedSeatsList.size() + " seats)");
            proceedBtn.setBackground(new Color(0, 123, 255));
            proceedBtn.setForeground(Color.WHITE);
            proceedBtn.addActionListener(e -> {
                if (selectedSeatsList.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Select at least one seat!");
                    return;
                }
                cardLayout.show(mainPanel, "payment");
            });

            add(new JLabel("Bus: " + selectedBusName + " (" + totalSeats + " seats) - Pathway in middle", SwingConstants.CENTER), BorderLayout.NORTH);
            add(seatGrid, BorderLayout.CENTER);
            add(proceedBtn, BorderLayout.SOUTH);
            revalidate();
            repaint();
        }
    }

    // ==================== PAYMENT PANEL (MPESA STK PUSH SIMULATED) ====================
    private class PaymentPanel extends JPanel {
        public PaymentPanel() {
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(15, 30, 15, 30);

            JLabel label = new JLabel("Pay with MPESA STK Push");
            label.setFont(new Font("Arial", Font.BOLD, 16));

            JTextField phoneField = new JTextField(currentUserId == -1 ? "0712345678" : "07xxxxxxxx", 15);
            JButton payBtn = new JButton("Send STK Push");
            payBtn.setBackground(new Color(0, 128, 0));
            payBtn.setForeground(Color.WHITE);

            c.gridx = 0; c.gridy = 0; add(label, c);
            c.gridy = 1; add(new JLabel("MPESA Phone"), c);
            c.gridy = 2; add(phoneField, c);
            c.gridy = 3; add(payBtn, c);

            payBtn.addActionListener(e -> {
                JOptionPane.showMessageDialog(this, "✅ STK Push sent to " + phoneField.getText() + "\n\nPlease enter PIN on your phone...\n\n(Simulated - payment successful in 3 seconds)");
                Timer timer = new Timer(3000, ev -> {
                    // Save booking
                    try (Connection conn = DBConnection.getConnection()) {
                        for (int seat : selectedSeatsList) {
                            PreparedStatement ps = conn.prepareStatement("INSERT INTO bookings (user_id, schedule_id, seat_number, payment_reference) VALUES (?,?,?,?)");
                            ps.setInt(1, currentUserId);
                            ps.setInt(2, selectedScheduleId);
                            ps.setInt(3, seat);
                            ps.setString(4, "MPESA_" + System.currentTimeMillis());
                            ps.executeUpdate();
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }

                    // Generate receipt
                    ReceiptGenerator.generateReceipt(currentUserName, selectedBusName, selectedSeatsList, selectedScheduleId);
                    JOptionPane.showMessageDialog(this, "Booking successful! Receipt downloaded.");
                    cardLayout.show(mainPanel, "home");
                    selectedSeatsList.clear();
                });
                timer.setRepeats(false);
                timer.start();
            });
        }
    }

    // ==================== RECEIPT GENERATOR ====================
    private static class ReceiptGenerator {
        public static void generateReceipt(String user, String bus, List<Integer> seats, int scheduleId) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream("Booking_Receipt_" + System.currentTimeMillis() + ".pdf"));
                document.open();
                document.add(new Paragraph("KENYA BUS BOOKING SYSTEM"));
                document.add(new Paragraph("----------------------------------------"));
                document.add(new Paragraph("Passenger: " + user));
                document.add(new Paragraph("Bus: " + bus));
                document.add(new Paragraph("Seats: " + seats));
                document.add(new Paragraph("Schedule ID: " + scheduleId));
                document.add(new Paragraph("Date: " + LocalDate.now()));
                document.add(new Paragraph("Amount paid via MPESA"));
                document.add(new Paragraph("Thank you for travelling with us!"));
                document.close();
                JOptionPane.showMessageDialog(null, "Receipt saved as PDF in project folder!");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private class AdminDashboardPanel extends JPanel {
        public AdminDashboardPanel() {
            setLayout(new BorderLayout());
            JTable table = new JTable();
            JButton deleteBtn = new JButton("Delete Selected Booking");
            deleteBtn.setBackground(Color.RED);
            deleteBtn.setForeground(Color.WHITE);

            loadBookings(table);

            deleteBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row == -1) return;
                int bookingId = (int) table.getValueAt(row, 0);
                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM bookings WHERE booking_id=?");
                    ps.setInt(1, bookingId);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Booking deleted");
                    loadBookings(table);
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            add(new JLabel("Admin Dashboard - All Bookings & Passengers", SwingConstants.CENTER), BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
            add(deleteBtn, BorderLayout.SOUTH);
        }

        private void loadBookings(JTable table) {
            // Simple implementation - you can expand with more columns
            String[] columns = {"ID", "User", "Bus", "Seat", "Date"};
            Object[][] data = new Object[50][5]; // placeholder
            // In real code you would query DB here
            table.setModel(new javax.swing.table.DefaultTableModel(data, columns));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BusBookingApp().setVisible(true));
    }
}
