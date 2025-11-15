package in.hosdpital.transportation;

//Main.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Optional;

public class Main extends JFrame {

 private final LogisticsManager manager;
 private static int requestCounter = 2000;
 private final SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

 public Main() {
     super("Smart Medical Transport & Logistics System (Uber Health Logistics)");
     this.manager = new LogisticsManager();

     setDefaultCloseOperation(EXIT_ON_CLOSE);
     setSize(600, 380);
     setLocationRelativeTo(null);
     setLayout(new BorderLayout(10,10));

     setupMainMenu();

     setVisible(true);
 }
 
 private void setupMainMenu() {
     // UI setup (title, buttons, info panel)
     JLabel title = new JLabel("Smart Medical Transport & Logistics System", SwingConstants.CENTER);
     title.setFont(new Font("SansSerif", Font.BOLD, 18));
     
     JLabel subtitle = new JLabel("Manage patients, medical goods & emergency vehicles", SwingConstants.CENTER);
     subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
     
     JPanel titlePanel = new JPanel(new GridLayout(2, 1));
     titlePanel.add(title);
     titlePanel.add(subtitle);

     JPanel buttonPanel = new JPanel(new GridLayout(1,3,12,12));
     JButton hospitalBtn = new JButton("🏥 Hospital / Clinic");
     JButton driverBtn = new JButton("🚗 Driver Dashboard");
     JButton adminBtn = new JButton("🏢 Admin / Logistics");

     // Open different windows
     hospitalBtn.addActionListener(e -> new BookingForm(manager, this, dtf));
     driverBtn.addActionListener(e -> new DriverDashboard(manager, this, dtf));
     adminBtn.addActionListener(e -> openAdminPanel());

     buttonPanel.add(hospitalBtn);
     buttonPanel.add(driverBtn);
     buttonPanel.add(adminBtn);

     JTextArea info = new JTextArea(
             "Project Objective:\nManage Emergency dispatch, medical goods transport, patient rides & prioritized deliveries.\n\n" +
             "Features:\n- Smarter Assignment (Zone-based)\n- Cold-Chain Alerts\n- File Logging"
     );
     info.setEditable(false);
     info.setBackground(getContentPane().getBackground());

     add(titlePanel, BorderLayout.NORTH);
     add(buttonPanel, BorderLayout.CENTER);
     add(info, BorderLayout.SOUTH);
 }
 
 // --- Admin Panel ---
 private void openAdminPanel() {
     JFrame f = new JFrame("🏢 Admin / Logistics Panel");
     f.setSize(1000, 420);
     f.setLocationRelativeTo(this);
     f.setLayout(new BorderLayout(8,8));

     String[] cols = {"Request ID", "Hospital", "Category", "Emergency Type", "Req. Vehicle", "From (Zone)", "To", "Assigned Vehicle", "Vehicle Zone", "Status", "Time", "Fare"};
     DefaultTableModel model = new DefaultTableModel(cols, 0);
     
     JTable table = new JTable(model);
     JScrollPane scroll = new JScrollPane(table);

     JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
     JButton refreshBtn = new JButton("Refresh");
     JButton exportBtn = new JButton("View Cold-Chain Alerts/Reports");
     JButton statsBtn = new JButton("Show Stats");
     top.add(refreshBtn); top.add(exportBtn); top.add(statsBtn);

     // Refresh logic
     refreshBtn.addActionListener(e -> {
         model.setRowCount(0);
         for (Request r : manager.getRequests()) {
             String assignedZone = "N/A";
             Optional<Vehicle> vOpt = manager.getVehicles().stream().filter(v -> v.getId().equals(r.getAssignedVehicle())).findFirst();
             if (vOpt.isPresent()) assignedZone = vOpt.get().getZone();
             
             model.addRow(new Object[]{
                     r.getId(), r.getHospital(), r.getTransportCategory(), r.getEmergencyType(), r.getVehicleType(),
                     r.getSource() + " (" + r.getSourceZone() + ")", r.getDestination(), r.getAssignedVehicle(), assignedZone, r.getStatus(),
                     dtf.format(r.getTimestamp()), r.getFare()
             });
         }
     });

     f.add(top, BorderLayout.NORTH);
     f.add(scroll, BorderLayout.CENTER);
     refreshBtn.doClick(); 
     f.setVisible(true);
 }

 public static void main(String[] args) {
     SwingUtilities.invokeLater(Main::new);
 }
 
 // Helper to generate IDs
 public static String getNextRequestId() {
     return "REQ-" + (requestCounter++);
 }
}