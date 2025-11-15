package in.hosdpital.transportation;
package in.hosdpital.transportation;

//LogisticsManager.java
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

public class LogisticsManager {
private final ArrayList<Vehicle> vehicles;
private final ArrayList<Request> requests;
private final SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

public LogisticsManager() {
   this.vehicles = new ArrayList<>();
   this.requests = new ArrayList<>();
   initializeMockData();
}

private void initializeMockData() {
   vehicles.add(new Vehicle("A-101", "Ambulance", "Available", "A"));
   vehicles.add(new Vehicle("A-102", "Ambulance", "Available", "B"));
   vehicles.add(new Vehicle("V-201", "Van", "Available", "A"));
   vehicles.add(new Vehicle("B-301", "Bike", "Available", "C"));
   vehicles.add(new Vehicle("V-202", "Van", "Available", "C"));
   vehicles.add(new Vehicle("A-103", "Ambulance", "En Route", "B"));
   vehicles.add(new Vehicle("A-104", "Ambulance", "Maintenance", "A"));
}

public ArrayList<Vehicle> getVehicles() { return vehicles; }
public ArrayList<Request> getRequests() { return requests; }

public String assignNearestVehicle(Request req) {
   String requiredType = req.getTransportCategory().equalsIgnoreCase("Emergency") ? "Ambulance" : req.getVehicleType();
   String requiredZone = req.getSourceZone();
   
   // Priority 1: Required Type in Same Zone
   Optional<Vehicle> assignedVehicle = vehicles.stream()
       .filter(v -> v.getType().equalsIgnoreCase(requiredType) && v.getStatus().equalsIgnoreCase("Available") && v.getZone().equals(requiredZone))
       .findFirst();

   if (assignedVehicle.isPresent()) {
       updateVehicleStatus(assignedVehicle.get(), "En Route");
       return assignedVehicle.get().getId();
   }

   // Priority 2: Required Type Anywhere
   assignedVehicle = vehicles.stream()
       .filter(v -> v.getType().equalsIgnoreCase(requiredType) && v.getStatus().equalsIgnoreCase("Available"))
       .findFirst();

   if (assignedVehicle.isPresent()) {
       updateVehicleStatus(assignedVehicle.get(), "En Route");
       return assignedVehicle.get().getId();
   }
   
   // Priority 3: Any Available Vehicle (Non-Emergency only)
   if (!req.getTransportCategory().equalsIgnoreCase("Emergency")) {
        assignedVehicle = vehicles.stream()
           .filter(v -> v.getStatus().equalsIgnoreCase("Available"))
           .findFirst();
        
        if (assignedVehicle.isPresent()) {
           updateVehicleStatus(assignedVehicle.get(), "En Route");
           return assignedVehicle.get().getId();
        }
   }
   
   return "NO_AVAILABLE";
}

public void updateVehicleStatus(Vehicle v, String newStatus) {
   v.setStatus(newStatus);
}

public void updateRequestStatusAndVehicle(Request r, String newStatus) {
   r.setStatus(newStatus);
   
   if (r.getStatus().equals("Completed") && !r.getAssignedVehicle().equals("N/A")) {
       vehicles.stream()
           .filter(v -> v.getId().equals(r.getAssignedVehicle()))
           .findFirst()
           .ifPresent(v -> updateVehicleStatus(v, "Available"));
       
       appendDeliveryReport(r);
   }
   
   appendTransportLog(r);
}

public String findAnyAvailableVehicle() {
   return vehicles.stream()
       .filter(v -> v.getStatus().equalsIgnoreCase("Available"))
       .findFirst()
       .map(v -> v.getId() + " (" + v.getType() + ") in Zone " + v.getZone())
       .orElse("NONE");
}

public void appendTransportLog(Request r) {
   try (FileWriter fw = new FileWriter("transport_logs.txt", true)) {
       fw.write("\n[" + dtf.format(new Date()) + "] RequestID: " + r.getId()
               + " | Hospital: " + r.getHospital()
               + " | Category: " + r.getTransportCategory()
               + " | Emergency Type: " + r.getEmergencyType()
               + " | Assigned: " + r.getAssignedVehicle()
               + " | Status: " + r.getStatus()
               + " | Fare: ₹" + r.getFare()
               + "\n------------------------\n");
   } catch (IOException e) { System.err.println("Error writing to transport log: " + e.getMessage()); }
}

public void appendDeliveryReport(Request r) {
   try (FileWriter fw = new FileWriter("delivery_report.txt", true)) {
       fw.write("DELIVERY REPORT - " + dtf.format(new Date()) + "\n");
       fw.write("RequestID: " + r.getId() + "\n");
       fw.write("Vehicle: " + r.getAssignedVehicle() + "\n");
       fw.write("From: " + r.getSource() + " To: " + r.getDestination() + "\n");
       fw.write("Category: " + r.getTransportCategory() + "\n");
       fw.write("Emergency Type: " + r.getEmergencyType() + "\n");
       fw.write("Fare: ₹" + r.getFare() + "\n");
       fw.write("Status: " + r.getStatus() + "\n");
       fw.write("---------------------------\n");
   } catch (IOException e) { System.err.println("Error writing delivery report: " + e.getMessage()); }
}

public void appendColdChainAlert(String reqId, int temperature) {
   try (FileWriter fw = new FileWriter("cold_chain_alerts.txt", true)) {
       fw.write("ALERT! [" + dtf.format(new Date()) + "] RequestID: " + reqId 
               + " experienced Critical Temperature: " + temperature + "°C\n");
   } catch (IOException e) { System.err.println("Error writing cold chain alert: " + e.getMessage()); }
 }
}