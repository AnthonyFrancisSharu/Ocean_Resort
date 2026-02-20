package com.Ocean_Resort;

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

/**
 * ReservationServlet - handles all reservation operations
 * Actions: generateId, add, getAll, search, delete, markPaid
 */
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
                String newId = store.generateNextId();
                out.print("{\"reservationId\":\"" + newId + "\"}");
                break;

            case "getAll":
                List<Reservation> all = store.getAllReservations();
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
                    found = store.findById(val.trim());
                } else if ("name".equals(by)) {
                    List<Reservation> results = store.findByName(val.trim());
                    if (!results.isEmpty()) found = results.get(0);
                } else if ("contact".equals(by)) {
                    List<Reservation> results = store.findByContact(val.trim());
                    if (!results.isEmpty()) found = results.get(0);
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
    //  POST  — add | markPaid
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
                String paidId = request.getParameter("id");
                if (paidId != null && store.updateStatus(paidId.trim(), "Checked Out")) {
                    out.print("{\"success\":true,\"message\":\"Marked as paid.\"}");
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
        if (id != null && store.deleteById(id.trim())) {
            out.print("{\"success\":true,\"message\":\"Reservation deleted.\"}");
        } else {
            out.print("{\"success\":false,\"message\":\"Reservation not found.\"}");
        }
        out.flush();
    }

    // ================================================================
    //  HANDLE ADD — validates room, saves reservation, sends email
    // ================================================================
    private void handleAdd(HttpServletRequest request, PrintWriter out) throws IOException {

        // Read JSON body
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
        }

        String json = body.toString();

        // Parse all fields from JSON
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

        // Default check-in / check-out times if not provided
        if (isBlank(checkInTime))  checkInTime  = "14:00";
        if (isBlank(checkOutTime)) checkOutTime = "12:00";

        // ── Server-side validation ────────────────────────────────────
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

        // ── Validate check-out is after check-in ─────────────────────
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

        // ── ROOM AVAILABILITY CHECK ───────────────────────────────────
        // Checks if the room is already booked for the requested dates
        if (!isBlank(roomNumber)) {
            boolean available = store.isRoomAvailable(roomNumber, checkIn, checkOut);
            if (!available) {
                out.print("{\"success\":false," +
                        "\"message\":\"Room " + roomNumber + " is already booked for the selected dates. " +
                        "Please choose a different room or different dates.\"}");
                return;
            }
        }

        // ── Generate / validate Reservation ID ───────────────────────
        if (isBlank(reservationId)) {
            reservationId = store.generateNextId();
        }
        if (store.findById(reservationId) != null) {
            reservationId = store.generateNextId();
        }

        // ── Create and save reservation ───────────────────────────────
        Reservation r = new Reservation(
                reservationId, guestName, address, contactNumber, email,
                nicNumber, numGuests, roomType, roomNumber, checkIn, checkOut, specialRequests
        );

        store.addReservation(r);

        // ── Calculate billing for the email ──────────────────────────
        double ratePerNight  = getRoomRate(roomType);
        int    nights        = calculateNights(checkIn, checkOut);
        double roomCharge    = ratePerNight * nights;
        double serviceCharge = roomCharge * 0.10;
        double vat           = roomCharge * 0.08;
        double totalAmount   = roomCharge + serviceCharge + vat;

        // ── Send confirmation email (only if email was provided) ──────
        boolean emailSent = false;
        if (!isBlank(email)) {
            emailSent = emailService.sendReservationConfirmation(
                    email,
                    guestName,
                    reservationId,
                    roomNumber,
                    roomType,
                    checkIn,
                    checkInTime,
                    checkOut,
                    checkOutTime,
                    nights,
                    ratePerNight,
                    totalAmount,
                    specialRequests
            );
        }

        // ── Build final JSON response ─────────────────────────────────
        String emailStatus = isBlank(email)
                ? "No email provided."
                : (emailSent
                ? "Confirmation email sent to " + email + "."
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
    //  HELPER — Room rate per night based on room type (LKR)
    // ================================================================
    private double getRoomRate(String roomType) {
        if (roomType == null) return 8500.0;
        switch (roomType.trim()) {
            case "Standard Room":
            case "Standard":        return 8500.0;
            case "Deluxe Room":
            case "Deluxe":          return 12000.0;
            case "Suite":           return 18500.0;
            case "Presidential Suite":
            case "Presidential":    return 35000.0;
            default:                return 8500.0;
        }
    }

    // ================================================================
    //  HELPER — Number of nights between check-in and check-out
    // ================================================================
    private int calculateNights(String checkIn, String checkOut) {
        try {
            java.time.LocalDate in  = java.time.LocalDate.parse(checkIn);
            java.time.LocalDate out = java.time.LocalDate.parse(checkOut);
            long nights = java.time.temporal.ChronoUnit.DAYS.between(in, out);
            return (int) Math.max(1, nights);
        } catch (Exception e) {
            return 1;
        }
    }

    // ================================================================
    //  HELPER — Simple JSON string field parser (no external libs)
    // ================================================================
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