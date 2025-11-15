package in.hosdpital.transportation;

// BookingForm.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;

public class BookingForm extends JDialog {
    private final LogisticsManager manager;
    private final SimpleDateFormat dtf;

    public BookingForm(LogisticsManager manager, JFrame parent, SimpleDateFormat dtf) {
        super(parent, "🏥 Hospital / Clinic Booking", true);
        this.manager = manager;
        this.dtf = dtf;
        
        setSize(520,450);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());
        setupUI();
        setVisible(true);
    }
    
    private void setupUI() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8,8,8,8);
        g.fill = GridBagConstraints.HORIZONTAL;

        // Hospital Selection
        JLabel lblHospital = new JLabel("Select Hospital:");
        String[] hospitals = {
            "CityCare Hospital", "Apollo Multi-Speciality Hospital", "Fortis Healthcare",
            "AIIMS Delhi", "Max Super Speciality Hospital", "Medanta Hospital",
            "Sunrise Clinic", "Shri Ram Hospital"
        };
        JComboBox<String> hospitalBox = new JComboBox<>(hospitals);

        // Transport Category
        JLabel lblTransportCat = new JLabel("Transport Category:");
        String[] categories = {"Emergency", "Medicine", "Sample"};
        JComboBox<String> catBox = new JComboBox<>(categories);

        // Emergency Type Selector
        JLabel lblEmergencyType = new JLabel("Emergency Type:");
        String[] emergencyTypes = {
            "Cardiac Arrest / Heart Attack", 
            "Road Traffic Accident (RTA) / Major Trauma", 
            "Respiratory Distress / Severe Asthma", 
            "Stroke / Paralysis",
            "Severe Burn Injury"
        };
        JComboBox<String> emergencyBox = new JComboBox<>(emergencyTypes);
        lblEmergencyType.setVisible(false);
        emergencyBox.setVisible(false);

        // Vehicle, Source, Destination, etc.
        JLabel lblVehicle = new JLabel("Preferred Vehicle Type:");
        String[] vehiclesTypes = {"Ambulance", "Van", "Bike"};
        JComboBox<String> vehicleBox = new JComboBox<>(vehiclesTypes);

        JLabel lblSource = new JLabel("Source (e.g., North Zone Hospital):");
        JTextField srcField = new JTextField();

        JLabel lblDest = new JLabel("Destination:");
        JTextField destField = new JTextField();

        JCheckBox coldChainBox = new JCheckBox("Cold-chain item (e.g., vaccine) - enable temp tracking");

        JLabel lblDistance = new JLabel("Distance (km) - for fare demo:");
        JTextField distanceField = new JTextField("5");

        JButton bookBtn = new JButton("Book Transport");
        JButton cancelBtn = new JButton("Cancel");
        JLabel contactInfo = new JLabel("Emergency contact: 102 (Right-click for SOS)");

        // Layout
        g.gridx = 0; g.gridy = 0; add(lblHospital, g);
        g.gridx = 1; add(hospitalBox, g); 

        g.gridx = 0; g.gridy++; add(lblTransportCat, g);
        g.gridx = 1; add(catBox, g);

        g.gridx = 0; g.gridy++; add(lblEmergencyType, g);
        g.gridx = 1; add(emergencyBox, g);

        g.gridx = 0; g.gridy++; add(lblVehicle, g);
        g.gridx = 1; add(vehicleBox, g);
        
        g.gridx = 0; g.gridy++; add(lblSource, g);
        g.gridx = 1; add(srcField, g);

        g.gridx = 0; g.gridy++; add(lblDest, g);
        g.gridx = 1; add(destField, g);

        g.gridx = 0; g.gridy++; add(coldChainBox, g);
        g.gridx = 1; add(contactInfo, g);

        g.gridx = 0; g.gridy++; add(lblDistance, g);
        g.gridx = 1; add(distanceField, g);


        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.add(bookBtn); btnP.add(cancelBtn);
        g.gridx = 0; g.gridy++; g.gridwidth = 2; add(btnP, g);

        // Listener to show/hide Emergency Box
        catBox.addActionListener(e -> {
            boolean isEmergency = catBox.getSelectedItem().toString().equalsIgnoreCase("Emergency");
            lblEmergencyType.setVisible(isEmergency);
            emergencyBox.setVisible(isEmergency);
            revalidate();
            repaint();
        });

        // Actions
        cancelBtn.addActionListener(e -> dispose());
        bookBtn.addActionListener(new BookTransportAction(hospitalBox, catBox, emergencyBox, vehicleBox, srcField, destField, coldChainBox, distanceField));
        
        JPopupMenu popup = new JPopupMenu();
        JMenuItem sosItem = new JMenuItem("Send SOS to Admin (simulate)");
        sosItem.addActionListener(e -> {
            String backup = manager.findAnyAvailableVehicle();
            JOptionPane.showMessageDialog(this, "SOS Sent to Admin!\nNearest backup vehicle suggestion: " + backup, "SOS", JOptionPane.WARNING_MESSAGE);
        });
        popup.add(sosItem);
        contactInfo.setComponentPopupMenu(popup);
    }
    
    private class BookTransportAction implements ActionListener {
        private final JComboBox<String> hospitalBox, catBox, emergencyBox, vehicleBox; 
        private final JTextField srcField, destField, distanceField;
        private final JCheckBox coldChainBox;
        
        public BookTransportAction(JComboBox<String> hospitalBox, JComboBox<String> catBox, JComboBox<String> emergencyBox, JComboBox<String> vehicleBox,
                                   JTextField srcField, JTextField destField, JCheckBox coldChainBox, JTextField distanceField) {
            this.hospitalBox = hospitalBox;
            this.catBox = catBox;
            this.emergencyBox = emergencyBox;
            this.vehicleBox = vehicleBox;
            this.srcField = srcField;
            this.destField = destField;
            this.coldChainBox = coldChainBox;
            this.distanceField = distanceField;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            String hospital = (String) hospitalBox.getSelectedItem();
            String category = catBox.getSelectedItem().toString();
            String prefVehicle = vehicleBox.getSelectedItem().toString();
            String src = srcField.getText().trim();
            String dest = destField.getText().trim();
            boolean coldChain = coldChainBox.isSelected();
            
            String emergencyType = category.equalsIgnoreCase("Emergency") ? 
                                   (String) emergencyBox.getSelectedItem() : "N/A";
            
            double distance = 0;
            try { distance = Double.parseDouble(distanceField.getText().trim()); }
            catch (Exception ex) { distance = 5; }

            if (hospital == null || src.isEmpty() || dest.isEmpty() || (category.equalsIgnoreCase("Emergency") && emergencyType == null)) {
                JOptionPane.showMessageDialog(BookingForm.this, "Please select hospital and fill all required fields.", "Missing Data", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String reqId = Main.getNextRequestId();
            
            Request req = new Request(reqId, hospital, category, emergencyType, prefVehicle, src, dest, coldChain);

            // Fare calculation
            double baseFare = 50;
            double ratePerKm = prefVehicle.equals("Bike") ? 6 : (prefVehicle.equals("Van") ? 12 : 20);
            double cost = baseFare + (distance * ratePerKm);
            if (category.equalsIgnoreCase("Emergency")) cost *= 1.5; 
            req.setFare(Math.round(cost * 100.0) / 100.0);

            String assigned = manager.assignNearestVehicle(req);
            req.setAssignedVehicle(assigned);
            
            if (!assigned.equals("NO_AVAILABLE")) {
                manager.updateRequestStatusAndVehicle(req, "Assigned");
            } else {
                manager.updateRequestStatusAndVehicle(req, "Pending");
            }

            manager.getRequests().add(req);

            String msg = "Request Created: " + req.getId() + "\nAssigned Vehicle: " + assigned + "\nEmergency Type: " + req.getEmergencyType() + "\nFare (approx): ₹" + req.getFare();
            JOptionPane.showMessageDialog(BookingForm.this, msg, "Booking Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();

            if (!assigned.equals("NO_AVAILABLE")) {
                int choice = JOptionPane.showConfirmDialog(null, "Open simulated tracking for this job?", "Track", JOptionPane.YES_NO_OPTION);
                // Note: TrackingWindow must be defined or imported
                if (choice == JOptionPane.YES_OPTION) new TrackingWindow(req, manager, dtf);
            }
        }
    }
}