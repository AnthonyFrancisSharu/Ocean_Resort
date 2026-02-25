package dao;

public class Reservation {

    private String reservationId;
    private String guestName;
    private String address;
    private String contactNumber;
    private String email;
    private String nicNumber;
    private String numGuests;      // String to match JSON parsing
    private String roomType;
    private String roomNumber;
    private String checkIn;
    private String checkOut;
    private String checkInTime;
    private String checkOutTime;
    private String specialRequests;
    private String status;

    public Reservation() {}

    // ── Getters ──
    public String getReservationId()   { return reservationId; }
    public String getGuestName()       { return guestName; }
    public String getAddress()         { return address; }
    public String getContactNumber()   { return contactNumber; }
    public String getEmail()           { return email; }
    public String getNicNumber()       { return nicNumber; }
    public String getNumGuests()       { return numGuests; }
    public String getRoomType()        { return roomType; }
    public String getRoomNumber()      { return roomNumber; }
    public String getCheckIn()         { return checkIn; }
    public String getCheckOut()        { return checkOut; }
    public String getCheckInTime()     { return checkInTime; }
    public String getCheckOutTime()    { return checkOutTime; }
    public String getSpecialRequests() { return specialRequests; }
    public String getStatus()          { return status; }

    // ── Setters ──
    public void setReservationId(String v)   { this.reservationId = v; }
    public void setGuestName(String v)       { this.guestName = v; }
    public void setAddress(String v)         { this.address = v; }
    public void setContactNumber(String v)   { this.contactNumber = v; }
    public void setEmail(String v)           { this.email = v; }
    public void setNicNumber(String v)       { this.nicNumber = v; }
    public void setNumGuests(String v)       { this.numGuests = v; }
    public void setRoomType(String v)        { this.roomType = v; }
    public void setRoomNumber(String v)      { this.roomNumber = v; }
    public void setCheckIn(String v)         { this.checkIn = v; }
    public void setCheckOut(String v)        { this.checkOut = v; }
    public void setCheckInTime(String v)     { this.checkInTime = v; }
    public void setCheckOutTime(String v)    { this.checkOutTime = v; }
    public void setSpecialRequests(String v) { this.specialRequests = v; }
    public void setStatus(String v)          { this.status = v; }

    public String toJson() {
        return "{" +
                "\"reservationId\":\""   + safe(reservationId)   + "\"," +
                "\"guestName\":\""       + safe(guestName)        + "\"," +
                "\"address\":\""         + safe(address)          + "\"," +
                "\"contactNumber\":\""   + safe(contactNumber)    + "\"," +
                "\"email\":\""           + safe(email)            + "\"," +
                "\"nicNumber\":\""       + safe(nicNumber)        + "\"," +
                "\"numGuests\":\""       + safe(numGuests)        + "\"," +
                "\"roomType\":\""        + safe(roomType)         + "\"," +
                "\"roomNumber\":\""      + safe(roomNumber)       + "\"," +
                "\"checkIn\":\""         + safe(checkIn)          + "\"," +
                "\"checkOut\":\""        + safe(checkOut)         + "\"," +
                "\"checkInTime\":\""     + safe(checkInTime)      + "\"," +
                "\"checkOutTime\":\""    + safe(checkOutTime)     + "\"," +
                "\"specialRequests\":\"" + safe(specialRequests)  + "\"," +
                "\"status\":\""          + safe(status)           + "\"" +
                "}";
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}