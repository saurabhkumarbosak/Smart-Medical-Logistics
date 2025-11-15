package in.hosdpital.transportation;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.Optional;

public class DriverDashboard extends JDialog {
    private final LogisticsManager manager;
    private final SimpleDateFormat dtf;
    private JTextArea jobArea;
    private JComboBox<String> vehicleSelect;

    public DriverDashboard(LogisticsManager manager, JFrame parent, SimpleDateFormat dtf) {
        super(parent, "🚗 Driver / Fleet Dashboard", true);
        this.manager = manager;
        this.dtf = dtf;

        setSize(640,420);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(8,8));
        setupUI();
        refreshJobs.run();
        setVisible(true);
    }
    
    private void setupUI() {
  
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        vehicleSelect = new JComboBox<>();
        manager.getVehicles().forEach(v -> vehicleSelect.addItem(v.toString()));
        JButton refreshBtn = new JButton("Refresh");
        top.add(new JLabel("Simulate driver (select vehicle):"));
        top.add(vehicleSelect);
        top.add(refreshBtn);

        jobArea = new JTextArea();
        jobArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(jobArea);

        JButton acceptBtn = new JButton("Accept Assigned Job");
        JButton completeBtn = new JButton("Mark Completed (by ID)");
        JButton sosBtn = new JButton("SOS 🚨 (Driver)");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(acceptBtn); bottom.add(completeBtn); bottom.add(sosBtn);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // Actions
        refreshBtn.addActionListener(e -> {
            vehicleSelect.removeAllItems();
            manager.getVehicles().forEach(v -> vehicleSelect.addItem(v.toString()));
            refreshJobs.run();
        });

        acceptBtn.addActionListener(e -> acceptJob());
        completeBtn.addActionListener(e -> completeJob());
        sosBtn.addActionListener(e -> sendSOS());
    }

    private final Runnable refreshJobs = () -> {
        StringBuilder sb = new StringBuilder();
        sb.append("Assigned / Pending jobs:\n\n");
        for (Request r : manager.getRequests()) {
            sb.append(r.getId()).append(" | ").append(r.getTransportCategory());
            if (r.getTransportCategory().equalsIgnoreCase("Emergency")) {
                sb.append(" (").append(r.getEmergencyType()).append(")");
            }
            sb.append(" | V: ").append(r.getAssignedVehicle())
                    .append(" | Status: ").append(r.getStatus())
                    .append(" | ").append(r.getSource())
                    .append(" -> ").append(r.getDestination())
                    .append("\n");
            if (r.isColdChain()) sb.append("   [COLD-CHAIN ITEM - temp monitored]\n");
            sb.append("-----------------------------\n");
        }
        jobArea.setText(sb.toString());
    };
    
    private void acceptJob() {
        String sel = (String) vehicleSelect.getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(this, "Select a vehicle first."); return; }
        String vid = sel.split(" ")[0].trim();

        Optional<Request> assignedRequestOpt = manager.getRequests().stream()
            .filter(r -> r.getAssignedVehicle().equals(vid) && r.getStatus().equals("Assigned"))
            .findFirst();

        if (assignedRequestOpt.isPresent()) {
            Request assignedRequest = assignedRequestOpt.get();
            manager.updateRequestStatusAndVehicle(assignedRequest, "In Transit");
            // Creates the tracking window
            new TrackingWindow(assignedRequest, manager, dtf); 
        } else {
            JOptionPane.showMessageDialog(this, "No 'Assigned' job found for " + vid);
        }
        refreshJobs.run();
    }
    
    private void completeJob() {
        String rid = JOptionPane.showInputDialog(this, "Enter Request ID to mark completed (e.g., REQ-2001):");
        if (rid == null || rid.trim().isEmpty()) return;
        
        Optional<Request> completedRequestOpt = manager.getRequests().stream()
            .filter(r -> r.getId().equalsIgnoreCase(rid.trim()))
            .findFirst();

        if (completedRequestOpt.isPresent()) {
            manager.updateRequestStatusAndVehicle(completedRequestOpt.get(), "Completed");
        } else {
            JOptionPane.showMessageDialog(this, "Request ID not found.");
        }
        refreshJobs.run();
    }
    
    private void sendSOS() {
        String sel = (String) vehicleSelect.getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(this, "Select your vehicle first."); return; }
        String vid = sel.split(" ")[0].trim();
        String backup = manager.findAnyAvailableVehicle();
        JOptionPane.showMessageDialog(this, "SOS Sent to Admin!\nYour vehicle: " + vid + "\nSuggested backup: " + backup, "SOS", JOptionPane.WARNING_MESSAGE);
    }
}

// ---------------- Tracking Window Class ----------------
// Defined here since it's only used by DriverDashboard and BookingForm
class TrackingWindow extends JDialog {
    private final Request req;
    private final LogisticsManager manager;
    private final SimpleDateFormat dtf;
    private Timer timer;

    public TrackingWindow(Request req, LogisticsManager manager, SimpleDateFormat dtf) {
        super();
        this.req = req;
        this.manager = manager;
        this.dtf = dtf;

        setTitle("📍 Tracking - " + req.getId());
        setSize(480,260);
        setLayout(new BorderLayout(8,8));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        setupTrackingUI();
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (timer != null) timer.stop();
            }
        });
        
        setVisible(true);
    }
    
    private void setupTrackingUI() {
        // ... (UI setup, same as previous version)
        JLabel info = new JLabel("Tracking for " + req.getId() + " | Vehicle: " + req.getAssignedVehicle() +
                " | Status: " + req.getStatus(), SwingConstants.CENTER);
        
        info.setFont(new Font("SansSerif", Font.BOLD, 12));

        JProgressBar progress = new JProgressBar(0, 100);
        progress.setStringPainted(true);
        JButton startBtn = new JButton("Start Simulation");
        JButton closeBtn = new JButton("Close");

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(startBtn); south.add(closeBtn);

        JTextArea telemetry = new JTextArea();
        telemetry.setEditable(false);
        JScrollPane scrol = new JScrollPane(telemetry);
        scrol.setPreferredSize(new Dimension(400, 80));

        add(info, BorderLayout.NORTH);
        add(progress, BorderLayout.CENTER);
        add(scrol, BorderLayout.SOUTH);
        add(south, BorderLayout.PAGE_END);

        startBtn.addActionListener(e -> startSimulation(startBtn, info, progress, telemetry));
        closeBtn.addActionListener(e -> {
            if (timer != null) timer.stop();
            dispose();
        });
    }

    private void startSimulation(JButton startBtn, JLabel info, JProgressBar progress, JTextArea telemetry) {
        startBtn.setEnabled(false);
        telemetry.append("Simulation started at " + dtf.format(new Date()) + "\n");
        progress.setValue(0);
        
        timer = new Timer(700, new ActionListener() {
            int pct = 0;
            Random rnd = new Random();
            boolean alertTriggered = false;
            
            public void actionPerformed(ActionEvent ev) {
                if (req.getStatus().equals("Completed")) {
                    timer.stop();
                    startBtn.setEnabled(false);
                    return;
                }
                
                pct += 8 + rnd.nextInt(7); 
                if (pct > 100) pct = 100;
                progress.setValue(pct);
                telemetry.append("[" + dtf.format(new Date()) + "] Progress: " + pct + "%\n");
                
                if (req.isColdChain()) {
                    int temp = 2 + rnd.nextInt(7); 
                    
                    if (pct > 30 && rnd.nextDouble() < 0.1 && !alertTriggered) {
                        temp = 15 + rnd.nextInt(10); 
                        telemetry.append("   ⚠️ CRITICAL TEMP ALERT: " + temp + "°C\n");
                        // manager.appendColdChainAlert is available via the passed manager instance
                        manager.appendColdChainAlert(req.getId(), temp);
                        JOptionPane.showMessageDialog(TrackingWindow.this, "CRITICAL COLD-CHAIN FAILURE! Admin Alerted. Temp: " + temp + "°C", "ALERT", JOptionPane.ERROR_MESSAGE);
                        alertTriggered = true;
                    } else {
                        telemetry.append("   Cold-chain temp: " + temp + "°C\n");
                    }
                }
                
                if (pct >= 100) {
                    timer.stop();
                    telemetry.append("Delivery completed at " + dtf.format(new Date()) + "\n");
                    manager.updateRequestStatusAndVehicle(req, "Completed");
                    info.setText("Tracking for " + req.getId() + " | Vehicle: " + req.getAssignedVehicle() +
                        " | Status: Completed");
                    JOptionPane.showMessageDialog(TrackingWindow.this, "Simulation finished. Request marked Completed.");
                }
            }
        });
        timer.start(); 
    }
}