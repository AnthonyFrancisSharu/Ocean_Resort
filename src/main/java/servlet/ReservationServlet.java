package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import dao.Reservation;
import dao.ReservationStore;
import util.EmailService;
import util.DatabaseConnection;
import java.util.stream.Collectors;

@WebServlet("/ReservationServlet")
public class ReservationServlet extends HttpServlet {

    private final ReservationStore store        = ReservationStore.getInstance();
    private final EmailService     emailService = new EmailService();

    // ================================================================
    //  GET  — generateId | getAll | search
    // ================================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isLoggedIn(request)) {
            sendJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "{\"success\":false,\"message\":\"Not authenticated.\"}");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action) {

            case "generateId":
                // ✅ generateId() — correct method name
                out.print("{\"reservationId\":\"" + store.generateId() + "\"}");
                break;

            case "getAll":
                // ✅ getAll() — correct method name
                List<Reservation> all = store.getAll();
                StringBuilder sb = new StringBuilder("{\"reservations\":[");
                for (int i = 0; i < all.size(); i++) {
                    sb.append(all.get(i).toJson());
                    if (i < all.size() - 1) sb.append(",");
                }
                sb.append("]}");
                out.print(sb.toString());
                break;

            case "search":
                String by  = request.getParameter("by");
                String val = request.getParameter("value");

                if (val == null || val.trim().isEmpty()) {
                    out.print("{\"success\":false,\"message\":\"Search value is empty.\"}");
                    break;
                }

                Reservation found = null;

                if ("id".equals(by)) {
                    // ✅ getById() — correct method name
                    found = store.getById(val.trim());

                } else if ("name".equals(by)) {
                    // ✅ filter from getAll() — no findByName() needed
                    String lower = val.trim().toLowerCase();
                    found = store.getAll().stream()
                            .filter(r -> r.getGuestName() != null &&
                                    r.getGuestName().toLowerCase().contains(lower))
                            .findFirst().orElse(null);

                } else if ("contact".equals(by)) {
                    // ✅ filter from getAll() — no findByContact() needed
                    found = store.getAll().stream()
                            .filter(r -> val.trim().equals(r.getContactNumber()))
                            .findFirst().orElse(null);
                }

                if (found != null) {
                    out.print("{\"success\":true,\"reservation\":" + found.toJson() + "}");
                } else {
                    out.print("{\"success\":false,\"message\":\"No reservation found.\"}");
                }
                break;

            default:
                out.print("{\"success\":false,\"message\":\"Unknown action.\"}");
                break;
        }

        out.flush();
    }

    // ================================================================
    //  POST  — add | markPaid | delete
    // ================================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isLoggedIn(request)) {
            sendJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "{\"success\":false,\"message\":\"Not authenticated.\"}");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action) {

            case "add":
                handleAdd(request, out);
                break;

            case "markPaid":
                // ✅ updateStatus() added to ReservationStore below
                String paidId = request.getParameter("id");
                if (!isBlank(paidId) && updateStatus(paidId.trim(), "Checked Out")) {
                    out.print("{\"success\":true,\"message\":\"Marked as paid.\"}");
                } else {
                    out.print("{\"success\":false,\"message\":\"Reservation not found.\"}");
                }
                break;

            case "delete":
                // ✅ deleteReservation() — correct method name
                String delId = request.getParameter("id");
                if (!isBlank(delId) && store.deleteReservation(delId.trim())) {
                    out.print("{\"success\":true,\"message\":\"Reservation deleted.\"}");
                } else {
                    out.print("{\"success\":false,\"message\":\"Reservation not found.\"}");
                }
                break;

            default:
                out.print("{\"success\":false,\"message\":\"Unknown action.\"}");
                break;
        }

        out.flush();
    }

    // ================================================================
    //  DELETE  — delete by id
    // ================================================================
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isLoggedIn(request)) {
            sendJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "{\"success\":false,\"message\":\"Not authenticated.\"}");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String id = request.getParameter("id");
        // ✅ deleteReservation() — correct method name
        if (!isBlank(id) && store.deleteReservation(id.trim())) {
            out.print("{\"success\":true,\"message\":\"Reservation deleted.\"}");
        } else {
            out.print("{\"success\":false,\"message\":\"Reservation not found.\"}");
        }
        out.flush();
    }

    // ================================================================
    //  HANDLE ADD
    // ================================================================
    private void handleAdd(HttpServletRequest request, PrintWriter out) throws IOException {

        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
        }

        String json = body.toString();

        String reservationId   = parseJson(json, "reservationId");
        String guestName       = parseJson(json, "guestName");
        String address         = parseJson(json, "address");
        String contactNumber   = parseJson(json, "contactNumber");
        String email           = parseJson(json, "email");
        String nicNumber       = parseJson(json, "nicNumber");
        String numGuests       = parseJson(json, "numGuests");
        String roomType        = parseJson(json, "roomType");
        String roomNumber      = parseJson(json, "roomNumber");
        String checkIn         = parseJson(json, "checkIn");
        String checkOut        = parseJson(json, "checkOut");
        String checkInTime     = parseJson(json, "checkInTime");
        String checkOutTime    = parseJson(json, "checkOutTime");
        String specialRequests = parseJson(json, "specialRequests");

        if (isBlank(checkInTime))  checkInTime  = "14:00";
        if (isBlank(checkOutTime)) checkOutTime = "12:00";

        // ── Validation ──
        if (isBlank(guestName) || isBlank(contactNumber) || isBlank(roomType)
                || isBlank(checkIn) || isBlank(checkOut)) {
            out.print("{\"success\":false,\"message\":\"Required fields are missing.\"}");
            return;
        }

        if (!contactNumber.matches("[0-9]{10}")) {
            out.print("{\"success\":false,\"message\":\"Invalid contact number. Must be 10 digits.\"}");
            return;
        }

        if (!isBlank(email) && !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            out.print("{\"success\":false,\"message\":\"Invalid email address format.\"}");
            return;
        }

        try {
            java.time.LocalDate inDate  = java.time.LocalDate.parse(checkIn);
            java.time.LocalDate outDate = java.time.LocalDate.parse(checkOut);
            if (!outDate.isAfter(inDate)) {
                out.print("{\"success\":false,\"message\":\"Check-out date must be after check-in date.\"}");
                return;
            }
        } catch (Exception e) {
            out.print("{\"success\":false,\"message\":\"Invalid date format. Use YYYY-MM-DD.\"}");
            return;
        }

        // ── Room availability check ──
        if (!isBlank(roomNumber)) {
            // ✅ isRoomAvailable() with correct 4-param signature (excludeId = null)
            boolean available = store.isRoomAvailable(roomNumber, checkIn, checkOut, null);
            if (!available) {
                out.print("{\"success\":false," +
                        "\"message\":\"Room " + roomNumber + " is already booked for the selected dates.\"}");
                return;
            }
        }

        // ── Generate reservation ID ──
        if (isBlank(reservationId)) {
            reservationId = store.generateId();
        }
        // ✅ getById() — correct method name
        if (store.getById(reservationId) != null) {
            reservationId = store.generateId();
        }

        // ── Build Reservation using setters (no parametrised constructor) ──
        // ✅ No constructor with args — use setters to match new Reservation.java
        Reservation r = new Reservation();
        r.setReservationId(reservationId);
        r.setGuestName(guestName);
        r.setAddress(address);
        r.setContactNumber(contactNumber);
        r.setEmail(email);
        r.setNicNumber(nicNumber);
        r.setNumGuests(numGuests);
        r.setRoomType(roomType);
        r.setRoomNumber(roomNumber);
        r.setCheckIn(checkIn);
        r.setCheckOut(checkOut);
        r.setCheckInTime(checkInTime);
        r.setCheckOutTime(checkOutTime);
        r.setSpecialRequests(specialRequests);
        r.setStatus("Active");

        // ✅ addReservation() — correct method name
        store.addReservation(r);

        // ── Billing calculation ──
        double ratePerNight  = getRoomRate(roomType);
        int    nights        = calculateNights(checkIn, checkOut);
        double roomCharge    = ratePerNight * nights;
        double serviceCharge = roomCharge * 0.10;
        double vat           = roomCharge * 0.08;
        double totalAmount   = roomCharge + serviceCharge + vat;

        // ── Send email ──
        boolean emailSent = false;
        if (!isBlank(email)) {
            emailSent = emailService.sendReservationConfirmation(
                    email, guestName, reservationId,
                    roomNumber, roomType,
                    checkIn, checkInTime,
                    checkOut, checkOutTime,
                    nights, ratePerNight, totalAmount,
                    specialRequests
            );
        }

        String emailStatus = isBlank(email) ? "No email provided."
                : (emailSent ? "Confirmation email sent to " + email + "."
                : "Reservation saved but email could not be sent.");

        out.print("{" +
                "\"success\":true," +
                "\"reservationId\":\"" + reservationId + "\"," +
                "\"message\":\"Reservation saved. " + emailStatus + "\"," +
                "\"emailSent\":" + emailSent + "," +
                "\"totalAmount\":" + totalAmount +
                "}");
    }

    // ================================================================
    //  updateStatus — updates reservation status in MySQL
    // ================================================================
    private boolean updateStatus(String reservationId, String newStatus) {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
        try (java.sql.Connection c = DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, reservationId);
            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================================================================
    //  HELPERS
    // ================================================================
    private double getRoomRate(String roomType) {
        if (roomType == null) return 8500.0;
        switch (roomType.trim()) {
            case "Standard Room":
            case "Standard":          return 8500.0;
            case "Deluxe Room":
            case "Deluxe":            return 12000.0;
            case "Suite":             return 18500.0;
            case "Presidential Suite":
            case "Presidential":      return 35000.0;
            default:                  return 8500.0;
        }
    }

    private int calculateNights(String checkIn, String checkOut) {
        try {
            java.time.LocalDate in  = java.time.LocalDate.parse(checkIn);
            java.time.LocalDate out = java.time.LocalDate.parse(checkOut);
            return (int) Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(in, out));
        } catch (Exception e) { return 1; }
    }

    private String parseJson(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return "";
        return json.substring(start, end)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("loggedInUser") != null;
    }

    private void sendJson(HttpServletResponse response, int status, String json) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.getWriter().print(json);
    }
}