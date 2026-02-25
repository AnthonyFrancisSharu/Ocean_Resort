package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import util.DatabaseConnection;

public class ReservationStore {

    private static ReservationStore instance;
    private ReservationStore() {}

    public static synchronized ReservationStore getInstance() {
        if (instance == null) instance = new ReservationStore();
        return instance;
    }

    public synchronized String generateId() {
        String sql = "SELECT reservation_id FROM reservations ORDER BY id DESC LIMIT 1";
        try (Connection c = DatabaseConnection.getConnection();
             Statement  s = c.createStatement();
             ResultSet  r = s.executeQuery(sql)) {
            if (r.next()) {
                String last = r.getString("reservation_id");
                int num = Integer.parseInt(last.replace("RES-", "")) + 1;
                return "RES-" + String.format("%03d", num);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return "RES-001";
    }

    public boolean addReservation(Reservation res) {
        if (!isRoomAvailable(res.getRoomNumber(), res.getCheckIn(), res.getCheckOut(), null)) {
            return false;
        }
        String sql = "INSERT INTO reservations " +
                "(reservation_id, guest_name, address, contact_number, email, nic_number, " +
                "num_guests, room_type, room_number, check_in, check_out, " +
                "check_in_time, check_out_time, special_requests, status) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,'Active')";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1,  res.getReservationId());
            ps.setString(2,  res.getGuestName());
            ps.setString(3,  res.getAddress());
            ps.setString(4,  res.getContactNumber());
            ps.setString(5,  res.getEmail());
            ps.setString(6,  res.getNicNumber());
            ps.setString(7,  res.getNumGuests());          // String → VARCHAR in DB
            ps.setString(8,  res.getRoomType());
            ps.setString(9,  res.getRoomNumber());
            ps.setDate  (10, Date.valueOf(res.getCheckIn()));
            ps.setDate  (11, Date.valueOf(res.getCheckOut()));
            ps.setString(12, res.getCheckInTime());
            ps.setString(13, res.getCheckOutTime());
            ps.setString(14, res.getSpecialRequests());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Reservation> getAll() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations ORDER BY id DESC";
        try (Connection c = DatabaseConnection.getConnection();
             Statement  s = c.createStatement();
             ResultSet  r = s.executeQuery(sql)) {
            while (r.next()) {
                list.add(mapRow(r));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Reservation getById(String reservationId) {
        String sql = "SELECT * FROM reservations WHERE reservation_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, reservationId);
            ResultSet r = ps.executeQuery();
            if (r.next()) return mapRow(r);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean deleteReservation(String reservationId) {
        String sql = "DELETE FROM reservations WHERE reservation_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, reservationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean isRoomAvailable(String roomNumber, String checkIn, String checkOut, String excludeId) {
        String sql = "SELECT COUNT(*) FROM reservations " +
                "WHERE room_number = ? AND status = 'Active' " +
                "AND check_in < ? AND check_out > ? " +
                (excludeId != null ? "AND reservation_id != ?" : "");
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, roomNumber);
            ps.setDate  (2, Date.valueOf(checkOut));
            ps.setDate  (3, Date.valueOf(checkIn));
            if (excludeId != null) ps.setString(4, excludeId);
            ResultSet r = ps.executeQuery();
            if (r.next()) return r.getInt(1) == 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int getCount() {
        String sql = "SELECT COUNT(*) FROM reservations";
        try (Connection c = DatabaseConnection.getConnection();
             Statement  s = c.createStatement();
             ResultSet  r = s.executeQuery(sql)) {
            if (r.next()) return r.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ── Map a ResultSet row to a Reservation object ──
    private Reservation mapRow(ResultSet r) throws SQLException {
        Reservation res = new Reservation();
        res.setReservationId  (r.getString("reservation_id"));
        res.setGuestName      (r.getString("guest_name"));
        res.setAddress        (r.getString("address"));
        res.setContactNumber  (r.getString("contact_number"));
        res.setEmail          (r.getString("email"));
        res.setNicNumber      (r.getString("nic_number"));
        res.setNumGuests      (r.getString("num_guests"));   // read as String
        res.setRoomType       (r.getString("room_type"));
        res.setRoomNumber     (r.getString("room_number"));
        res.setCheckIn        (r.getDate  ("check_in").toString());
        res.setCheckOut       (r.getDate  ("check_out").toString());
        res.setCheckInTime    (r.getString("check_in_time"));
        res.setCheckOutTime   (r.getString("check_out_time"));
        res.setSpecialRequests(r.getString("special_requests"));
        res.setStatus         (r.getString("status"));
        return res;
    }
}