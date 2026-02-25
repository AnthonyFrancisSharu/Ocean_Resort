package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import util.DatabaseConnection;

/**
 * StaffStore - MySQL-backed staff storage
 */
public class StaffStore {

    private static StaffStore instance;

    private StaffStore() {}

    public static synchronized StaffStore getInstance() {
        if (instance == null) instance = new StaffStore();
        return instance;
    }

    // ── Generate next Staff ID ──
    public synchronized String generateId() {
        String sql = "SELECT staff_id FROM staff ORDER BY id DESC LIMIT 1";
        try (Connection c = DatabaseConnection.getConnection();
             Statement  s = c.createStatement();
             ResultSet  r = s.executeQuery(sql)) {
            if (r.next()) {
                String last = r.getString("staff_id"); // e.g. STF-003
                int num = Integer.parseInt(last.replace("STF-", "")) + 1;
                return "STF-" + String.format("%03d", num);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "STF-001";
    }

    // ── Add new staff ──
    public boolean addStaff(Staff staff) {
        String sql = "INSERT INTO staff (staff_id, name, email, contact, role, dept, nic, username, password, status) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)";

        String newId = generateId();

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1,  newId);
            ps.setString(2,  staff.getName());
            ps.setString(3,  staff.getEmail());
            ps.setString(4,  staff.getContact());
            ps.setString(5,  staff.getRole());
            ps.setString(6,  staff.getDept());
            ps.setString(7,  staff.getNic());
            ps.setString(8,  staff.getUsername());
            ps.setString(9,  staff.getPassword());
            ps.setString(10, staff.getStatus());

            ps.executeUpdate();
            staff.setId(newId);
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            // Duplicate username
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Get all staff ──
    public List<Staff> getAll() {
        List<Staff> list = new ArrayList<>();
        String sql = "SELECT * FROM staff ORDER BY id ASC";

        try (Connection c = DatabaseConnection.getConnection();
             Statement  s = c.createStatement();
             ResultSet  r = s.executeQuery(sql)) {

            while (r.next()) {
                Staff st = new Staff();
                st.setId     (r.getString("staff_id"));
                st.setName   (r.getString("name"));
                st.setEmail  (r.getString("email"));
                st.setContact(r.getString("contact"));
                st.setRole   (r.getString("role"));
                st.setDept   (r.getString("dept"));
                st.setNic    (r.getString("nic"));
                st.setUsername(r.getString("username"));
                st.setStatus (r.getString("status"));
                list.add(st);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── Delete staff by ID ──
    public boolean deleteStaff(String staffId) {
        String sql = "DELETE FROM staff WHERE staff_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, staffId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Validate login ──
    public Staff validateLogin(String username, String password) {
        String sql = "SELECT * FROM staff WHERE username = ? AND password = ? AND status = 'Active'";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet r = ps.executeQuery();
            if (r.next()) {
                Staff st = new Staff();
                st.setId     (r.getString("staff_id"));
                st.setName   (r.getString("name"));
                st.setRole   (r.getString("role"));
                st.setUsername(r.getString("username"));
                return st;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ── Get total count ──
    public int getCount() {
        String sql = "SELECT COUNT(*) FROM staff";
        try (Connection c = DatabaseConnection.getConnection();
             Statement  s = c.createStatement();
             ResultSet  r = s.executeQuery(sql)) {
            if (r.next()) return r.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}