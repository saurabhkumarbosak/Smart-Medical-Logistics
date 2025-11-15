package in.hosdpital.transportation;

//Vehicle.java
public class Vehicle {
 private String id;
 private String type;      // "Ambulance", "Van", "Bike"
 private String status;    // "Available", "En Route", "Maintenance"
 private String zone;      // A, B, C for simulated location

 public Vehicle(String id, String type, String status, String zone) {
     this.id = id;
     this.type = type;
     this.status = status;
     this.zone = zone;
 }

 // Getters and Setters
 public String getId() { return id; }
 public String getType() { return type; }
 public String getStatus() { return status; }
 public void setStatus(String status) { this.status = status; }
 public String getZone() { return zone; }

 @Override
 public String toString() { return id + " (" + type + ") - " + status + " in Zone " + zone; }
}