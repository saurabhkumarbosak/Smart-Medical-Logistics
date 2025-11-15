package in.hosdpital.transportation;

//Request.java (FIXED)
import java.util.Date;

public class Request {
 private String id;
 private String hospital;
 private String transportCategory; // "Emergency", "Medicine", "Sample"
 private String emergencyType = "N/A"; // e.g., "Cardiac Arrest", "Road Traffic Accident"
 private String vehicleType;
 private String source;
 private String destination;
 private String assignedVehicle = "N/A";
 private String status = "Pending";
 private boolean coldChain = false;
 private double fare = 0.0;
 private Date timestamp;

 /**
  * UNIFIED Constructor for creating a new Request.
  * It handles both emergency (when emergencyType is provided) 
  * and non-emergency requests.
  */
 public Request(String id, String hospital, String transportCategory, String emergencyType, String vehicleType,
                String source, String destination, boolean coldChain) {
     this.id = id;
     this.hospital = hospital;
     this.transportCategory = transportCategory;
     this.vehicleType = vehicleType;
     this.source = source;
     this.destination = destination;
     this.coldChain = coldChain;
     this.timestamp = new Date();
     
     // Only set emergencyType if the category is Emergency
     if (transportCategory.equalsIgnoreCase("Emergency")) {
         this.emergencyType = emergencyType;
     } else {
          this.emergencyType = "N/A";
     }
 }
 
 // Getters and Setters (omitted for brevity, assume they are correct)
 public String getId() { return id; }
 public String getHospital() { return hospital; }
 public String getTransportCategory() { return transportCategory; }
 public String getEmergencyType() { return emergencyType; }
 public String getVehicleType() { return vehicleType; }
 public String getSource() { return source; }
 public String getDestination() { return destination; }
 public String getAssignedVehicle() { return assignedVehicle; }
 public void setAssignedVehicle(String assignedVehicle) { this.assignedVehicle = assignedVehicle; }
 public String getStatus() { return status; }
 public void setStatus(String status) { this.status = status; }
 public boolean isColdChain() { return coldChain; }
 public double getFare() { return fare; }
 public void setFare(double fare) { this.fare = fare; }
 public Date getTimestamp() { return timestamp; }

 public String getSourceZone() {
     if (source.toLowerCase().contains("north")) return "A";
     if (source.toLowerCase().contains("south")) return "B";
     return "C";
 }
}