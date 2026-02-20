package com.Ocean_Resort;

/**
 * Reservation model class - represents a guest reservation
 */
public class Reservation {

    private String reservationId;
    private String guestName;
    private String address;
    private String contactNumber;
    private String email;
    private String nicNumber;
    private String numGuests;
    private String roomType;
    private String roomNumber;
    private String checkIn;
    private String checkOut;
    private String specialRequests;
    private String status; // Active, Checked Out, Pending

    // Room rates (LKR per night)
    public static final double RATE_STANDARD    = 8500.0;
    public static final double RATE_DELUXE      = 12000.0;
    public static final double RATE_SUITE       = 18500.0;
    public static final double RATE_PRESIDENTIAL = 35000.0;

    public static final double SERVICE_CHARGE_RATE = 0.10;
    public static final double VAT_RATE            = 0.08;

    // Default constructor
    public Reservation() {
        this.status = "Active";
    }

    // Parameterized constructor
    public Reservation(String reservationId, String guestName, String address,
                       String contactNumber, String email, String nicNumber,
                       String numGuests, String roomType, String roomNumber,
                       String checkIn, String checkOut, String specialRequests) {
        this.reservationId   = reservationId;
        this.guestName       = guestName;
        this.address         = address;
        this.contactNumber   = contactNumber;
        this.email           = email;
        this.nicNumber       = nicNumber;
        this.numGuests       = numGuests;
        this.roomType        = roomType;
        this.roomNumber      = roomNumber;
        this.checkIn         = checkIn;
        this.checkOut        = checkOut;
        this.specialRequests = specialRequests;
        this.status          = "Active";
    }

    // Get rate per night based on room type
    public double getRatePerNight() {
        if (roomType == null) return 0;
        switch (roomType) {
            case "Standard":     return RATE_STANDARD;
            case "Deluxe":       return RATE_DELUXE;
            case "Suite":        return RATE_SUITE;
            case "Presidential": return RATE_PRESIDENTIAL;
            default:             return 0;
        }
    }

    // Convert to JSON string manually (no external library needed)
    public String toJson() {
        return "{"
                + "\"reservationId\":\"" + safe(reservationId) + "\","
                + "\"guestName\":\"" + safe(guestName) + "\","
                + "\"address\":\"" + safe(address) + "\","
                + "\"contactNumber\":\"" + safe(contactNumber) + "\","
                + "\"email\":\"" + safe(email) + "\","
                + "\"nicNumber\":\"" + safe(nicNumber) + "\","
                + "\"numGuests\":\"" + safe(numGuests) + "\","
                + "\"roomType\":\"" + safe(roomType) + "\","
                + "\"roomNumber\":\"" + safe(roomNumber) + "\","
                + "\"checkIn\":\"" + safe(checkIn) + "\","
                + "\"checkOut\":\"" + safe(checkOut) + "\","
                + "\"specialRequests\":\"" + safe(specialRequests) + "\","
                + "\"status\":\"" + safe(status) + "\""
                + "}";
    }

    // Escape special characters for JSON
    private String safe(String val) {
        if (val == null) return "";
        return val.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    // ---- Getters & Setters ----

    public String getReservationId()   { return reservationId; }
    public void setReservationId(String r) { this.reservationId = r; }

    public String getGuestName()       { return guestName; }
    public void setGuestName(String g) { this.guestName = g; }

    public String getAddress()         { return address; }
    public void setAddress(String a)   { this.address = a; }

    public String getContactNumber()   { return contactNumber; }
    public void setContactNumber(String c) { this.contactNumber = c; }

    public String getEmail()           { return email; }
    public void setEmail(String e)     { this.email = e; }

    public String getNicNumber()       { return nicNumber; }
    public void setNicNumber(String n) { this.nicNumber = n; }

    public String getNumGuests()       { return numGuests; }
    public void setNumGuests(String n) { this.numGuests = n; }

    public String getRoomType()        { return roomType; }
    public void setRoomType(String r)  { this.roomType = r; }

    public String getRoomNumber()      { return roomNumber; }
    public void setRoomNumber(String r){ this.roomNumber = r; }

    public String getCheckIn()         { return checkIn; }
    public void setCheckIn(String c)   { this.checkIn = c; }

    public String getCheckOut()        { return checkOut; }
    public void setCheckOut(String c)  { this.checkOut = c; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String s) { this.specialRequests = s; }

    public String getStatus()          { return status; }
    public void setStatus(String s)    { this.status = s; }
}