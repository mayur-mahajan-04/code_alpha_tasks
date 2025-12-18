package CodeAlpha_HotelReservationSystem;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class Room {
    int roomNo;
    String type;
    double price;
    boolean available;

    Room(int r, String t, double p, boolean a) {
        roomNo = r;
        type = t;
        price = p;
        available = a;
    }
}

public class HotelReservationGUI extends JFrame {

    DefaultTableModel model;
    JTable table;
    java.util.List<Room> rooms = new ArrayList<Room>();

    static final String ROOM_FILE = "rooms.txt";
    static final String BOOKING_FILE = "bookings.txt";

    JTextField nameField, roomField;

    public HotelReservationGUI() {

        setTitle("Hotel Reservation System");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15,15));
        getContentPane().setBackground(new Color(240,242,247));

        // ---------- HEADER ----------
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(41,128,185));
        header.setBorder(BorderFactory.createEmptyBorder(25,30,25,30));
        
        JLabel title = new JLabel("Hotel Reservation System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        
        JLabel subtitle = new JLabel("Manage your hotel bookings efficiently");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(new Color(220,230,240));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(title, BorderLayout.CENTER);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        
        header.add(titlePanel, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ---------- MAIN CONTAINER ----------
        JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
        mainContainer.setBackground(new Color(240,242,247));
        
        // ---------- FORM PANEL ----------
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(15,20,15,20),
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52,152,219),2),
                "Booking Management",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 18),
                new Color(52,73,94)
            )
        ));
        
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,15,10,15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Customer Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        nameField = createStyledTextField();
        inputPanel.add(nameField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        JLabel roomLabel = new JLabel("Room Number:");
        roomLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputPanel.add(roomLabel, gbc);
        
        gbc.gridx = 3; gbc.weightx = 1.0;
        roomField = createStyledTextField();
        inputPanel.add(roomField, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton bookBtn = createButton("Book Room", new Color(39,174,96), new Color(46,204,113));
        JButton cancelBtn = createButton("Cancel Booking", new Color(192,57,43), new Color(231,76,60));
        JButton refreshBtn = createButton("Refresh", new Color(230,126,34), new Color(241,196,15));
        JButton viewBtn = createButton("View Bookings", new Color(142,68,173), new Color(155,89,182));
        
        buttonPanel.add(bookBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(viewBtn);
        
        formContainer.add(inputPanel, BorderLayout.NORTH);
        formContainer.add(buttonPanel, BorderLayout.CENTER);
        mainContainer.add(formContainer, BorderLayout.NORTH);

        // ---------- TABLE ----------
        model = new DefaultTableModel(
                new String[]{"Room No", "Type", "Price 'Rs'", "Status"}, 0);
        table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setGridColor(new Color(230,235,240));
        table.setSelectionBackground(new Color(52,152,219,50));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0,1));

        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 16));
        th.setBackground(new Color(52,73,94));
        th.setForeground(Color.WHITE);
        th.setPreferredSize(new Dimension(0,40));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10,20,20,20),
            BorderFactory.createLineBorder(new Color(220,225,230),1)
        ));
        scroll.getViewport().setBackground(Color.WHITE);
        mainContainer.add(scroll, BorderLayout.CENTER);
        
        add(mainContainer, BorderLayout.CENTER);

        loadRooms();
        refreshTable();

        // ---------- ACTIONS ----------
        bookBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                bookRoom();
            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelBooking();
            }
        });

        refreshBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshTable();
            }
        });

        viewBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewBookings();
            }
        });

        setVisible(true);
    }

    // ---------- STYLED COMPONENTS ----------
    JTextField createStyledTextField() {
        JTextField field = new JTextField(15);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189,195,199), 1),
            BorderFactory.createEmptyBorder(8,12,8,12)
        ));
        return field;
    }
    
    JButton createButton(String text, final Color bg, final Color hover) {
        final JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(140, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hover);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        
        return btn;
    }

    // ---------- DATA LOGIC ----------
    void loadRooms() {
        File f = new File(ROOM_FILE);
        if (!f.exists()) {
            rooms.add(new Room(101,"Standard",2000,true));
            rooms.add(new Room(102,"Standard",2000,true));
            rooms.add(new Room(103,"Standard",2000,true));
            rooms.add(new Room(104,"Standard",2000,true));
            rooms.add(new Room(105,"Standard",2000,true));
            rooms.add(new Room(106,"Standard",2000,true));
            rooms.add(new Room(201,"Deluxe",3500,true));
            rooms.add(new Room(202,"Deluxe",3500,true));
            rooms.add(new Room(203,"Deluxe",3500,true));
            rooms.add(new Room(204,"Deluxe",3500,true));
            rooms.add(new Room(301,"Suite",6000,true));
            saveRooms();
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while((line = br.readLine())!=null){
                String[] p = line.split(",");
                rooms.add(new Room(
                        Integer.parseInt(p[0]),
                        p[1],
                        Double.parseDouble(p[2]),
                        Boolean.parseBoolean(p[3])
                ));
            }
            br.close();
        } catch(Exception e){}
    }

    void saveRooms() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(ROOM_FILE));
            for(Room r:rooms){
                bw.write(r.roomNo+","+r.type+","+r.price+","+r.available);
                bw.newLine();
            }
            bw.close();
        } catch(Exception e){}
    }

    void refreshTable() {
        model.setRowCount(0);
        for(Room r:rooms){
            model.addRow(new Object[]{
                    r.roomNo, r.type, " Rs " + r.price, r.available?" O Available":" X Booked"
            });
        }
    }

    void bookRoom() {
        String name = nameField.getText();
        int roomNo;

        try {
            roomNo = Integer.parseInt(roomField.getText());
        } catch(Exception e){
            JOptionPane.showMessageDialog(this,"Invalid Room Number");
            return;
        }

        for(Room r:rooms){
            if(r.roomNo==roomNo && r.available){
                JOptionPane.showMessageDialog(this,
                        "Payment Rs"+r.price+" Successful!");
                r.available=false;
                saveRooms();
                saveBooking(roomNo,name,r.type,r.price);
                refreshTable();
                JOptionPane.showMessageDialog(this,"Room Booked Successfully!");
                return;
            }
        }
        JOptionPane.showMessageDialog(this,"Room not available");
    }

    void saveBooking(int room,String name,String type,double amt){
        try{
            BufferedWriter bw = new BufferedWriter(
                    new FileWriter(BOOKING_FILE,true));
            bw.write(room+","+name+","+type+","+amt);
            bw.newLine();
            bw.close();
        }catch(Exception e){}
    }

    void cancelBooking() {
        int roomNo;
        try{
            roomNo=Integer.parseInt(roomField.getText());
        }catch(Exception e){
            JOptionPane.showMessageDialog(this,"Invalid Room Number");
            return;
        }

        java.util.List<String> keep = new ArrayList<String>();
        boolean found=false;

        try{
            BufferedReader br=new BufferedReader(new FileReader(BOOKING_FILE));
            String line;
            while((line=br.readLine())!=null){
                if(!line.startsWith(roomNo+",")){
                    keep.add(line);
                }else found=true;
            }
            br.close();

            BufferedWriter bw=new BufferedWriter(new FileWriter(BOOKING_FILE));
            for(String s:keep){
                bw.write(s);
                bw.newLine();
            }
            bw.close();

            for(Room r:rooms){
                if(r.roomNo==roomNo) r.available=true;
            }
            saveRooms();
            refreshTable();

            JOptionPane.showMessageDialog(this,
                    found?"Booking Cancelled":"Booking Not Found");

        }catch(Exception e){}
    }

    void viewBookings() {
        StringBuilder sb=new StringBuilder();
        try{
            BufferedReader br=new BufferedReader(new FileReader(BOOKING_FILE));
            String line;
            while((line=br.readLine())!=null){
                sb.append(line).append("\n");
            }
            br.close();
        }catch(Exception e){
            sb.append("No bookings found");
        }
        JOptionPane.showMessageDialog(this,sb.toString(),
                "Bookings",JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        new HotelReservationGUI();
    }
}
