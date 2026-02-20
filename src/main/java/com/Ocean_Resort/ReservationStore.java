package com.Ocean_Resort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * ReservationStore - In-memory data store (Singleton pattern)
 * Holds all reservations during the server session.
 * For production: replace with database calls.
 */
public class ReservationStore {

    private static ReservationStore instance;
    private final List<Reservation> reservations;
    private final AtomicInteger idCounter;

    private ReservationStore() {
        reservations = new ArrayList<>();
        idCounter = new AtomicInteger(1);
        loadSampleData();
    }

    // Singleton getInstance
    public static synchronized ReservationStore getInstance() {
        if (instance == null) {
            instance = new ReservationStore();
        }
        return instance;
    }

    // Load sample reservations for demo
    private void loadSampleData() {
        Reservation r1 = new Reservation(
                "RES-001", "Kasun Perera", "No 12, Galle Road, Colombo 03",
                "0711234567", "kasun@email.com", "991234567V",
                "2", "Deluxe", "201",
                "2025-02-01", "2025-02-05", "Sea view room preferred."
        );
        r1.setStatus("Active");

        Reservation r2 = new Reservation(
                "RES-002", "Nimal Silva", "45/A, Temple Road, Kandy",
                "0722345678", "nimal@email.com", "881122334V",
                "1", "Standard", "102",
                "2025-01-28", "2025-02-02", ""
        );
        r2.setStatus("Checked Out");

        Reservation r3 = new Reservation(
                "RES-003", "Sanduni Fernando", "22, Beach Road, Galle",
                "0733456789", "sanduni@email.com", "200034500012",
                "4", "Suite", "305",
                "2025-02-10", "2025-02-14", "Extra bed required."
        );
        r3.setStatus("Pending");

        reservations.add(r1);
        reservations.add(r2);
        reservations.add(r3);
        idCounter.set(4);
    }

    // Generate next reservation ID
    public String generateNextId() {
        return String.format("RES-%03d", idCounter.getAndIncrement());
    }

    // Add new reservation
    public synchronized void addReservation(Reservation r) {
        reservations.add(r);
    }

    // ================================================================
    //  ROOM AVAILABILITY CHECK
    //  Returns true  → room is AVAILABLE  (safe to book)
    //  Returns false → room is NOT available (dates overlap)
    //
    //  Logic: Two bookings overlap when:
    //    newCheckIn  < existingCheckOut
    //    AND
    //    newCheckOut > existingCheckIn
    //
    //  We SKIP reservations that are "Checked Out" because that guest
    //  has already left — the room is physically free again.
    // ================================================================
    public synchronized boolean isRoomAvailable(String roomNumber,
                                                String checkIn,
                                                String checkOut) {
        // If room number is blank, skip the check
        if (roomNumber == null || roomNumber.trim().isEmpty()) return true;

        try {
            LocalDate newIn  = LocalDate.parse(checkIn);
            LocalDate newOut = LocalDate.parse(checkOut);

            for (Reservation existing : reservations) {

                // Skip if different room
                if (!existing.getRoomNumber().trim()
                        .equalsIgnoreCase(roomNumber.trim())) continue;

                // Skip checked-out reservations — room is free
                if ("Checked Out".equalsIgnoreCase(existing.getStatus())) continue;

                // Parse existing dates
                LocalDate existIn  = LocalDate.parse(existing.getCheckIn());
                LocalDate existOut = LocalDate.parse(existing.getCheckOut());

                // Overlap check
                // newIn < existOut  AND  newOut > existIn  → OVERLAP
                if (newIn.isBefore(existOut) && newOut.isAfter(existIn)) {
                    return false; // Room is NOT available
                }
            }
        } catch (Exception e) {
            // If dates can't be parsed, allow booking (servlet validates dates separately)
            return true;
        }

        return true; // Room is available
    }

    // Get all reservations
    public synchronized List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations);
    }

    // Find by reservation ID
    public synchronized Reservation findById(String id) {
        return reservations.stream()
                .filter(r -> r.getReservationId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    // Find by guest name (partial, case-insensitive)
    public synchronized List<Reservation> findByName(String name) {
        String lc = name.toLowerCase();
        return reservations.stream()
                .filter(r -> r.getGuestName().toLowerCase().contains(lc))
                .collect(Collectors.toList());
    }

    // Find by contact number
    public synchronized List<Reservation> findByContact(String contact) {
        return reservations.stream()
                .filter(r -> r.getContactNumber().contains(contact))
                .collect(Collectors.toList());
    }

    // Delete a reservation
    public synchronized boolean deleteById(String id) {
        return reservations.removeIf(r -> r.getReservationId().equalsIgnoreCase(id));
    }

    // Update status
    public synchronized boolean updateStatus(String id, String status) {
        Reservation r = findById(id);
        if (r != null) {
            r.setStatus(status);
            return true;
        }
        return false;
    }

    // Get count
    public synchronized int getTotalCount() { return reservations.size(); }

    // Get active count
    public synchronized long getActiveCount() {
        return reservations.stream().filter(r -> "Active".equals(r.getStatus())).count();
    }

    // Get today's check-ins
    public synchronized long getTodayCheckIns() {
        String today = LocalDate.now().toString();
        return reservations.stream().filter(r -> today.equals(r.getCheckIn())).count();
    }

    // Calculate total revenue from all reservations
    public synchronized double getTotalRevenue() {
        double total = 0;
        for (Reservation r : reservations) {
            try {
                LocalDate in  = LocalDate.parse(r.getCheckIn());
                LocalDate out = LocalDate.parse(r.getCheckOut());
                long nights = java.time.temporal.ChronoUnit.DAYS.between(in, out);
                total += nights * r.getRatePerNight();
            } catch (Exception ignored) {}
        }
        return total;
    }
}