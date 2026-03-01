package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import dao.Staff;
import dao.StaffStore;

@WebServlet("/StaffServlet")
public class StaffServlet extends HttpServlet {

    private final StaffStore store = StaffStore.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");
        if (action == null) action = "";

        try {
            switch (action) {

                case "getAll": {
                    List<Staff> all = store.getAll();
                    StringBuilder sb = new StringBuilder("{\"staff\":[");
                    for (int i = 0; i < all.size(); i++) {
                        sb.append(all.get(i).toJson());
                        if (i < all.size() - 1) sb.append(",");
                    }
                    sb.append("],\"total\":").append(all.size()).append("}");
                    out.print(sb);
                    break;
                }

                case "generateId": {
                    String nextId = store.generateId();
                    out.print("{\"staffId\":\"" + nextId + "\"}");
                    break;
                }

                default:
                    out.print("{\"error\":\"Unknown action: " + action + "\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"success\":false,\"message\":\"Server error: " + e.getMessage() + "\"}");
        }

        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");
        if (action == null) action = "";

        try {
            switch (action) {

                case "add": {
                    StringBuilder body = new StringBuilder();
                    try (BufferedReader reader = request.getReader()) {
                        String line;
                        while ((line = reader.readLine()) != null) body.append(line);
                    }

                    String json = body.toString();
                    System.out.println("[StaffServlet] Received JSON: " + json);

                    String name     = parseField(json, "name");
                    String email    = parseField(json, "email");
                    String contact  = parseField(json, "contact");
                    String role     = parseField(json, "role");
                    String dept     = parseField(json, "dept");
                    String nic      = parseField(json, "nic");
                    String username = parseField(json, "username");
                    String password = parseField(json, "password");
                    String status   = parseField(json, "status");

                    System.out.println("[StaffServlet] Parsed — name=" + name + ", username=" + username);

                    if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                        out.print("{\"success\":false,\"message\":\"Name, username and password are required.\"}");
                        break;
                    }

                    Staff s = new Staff();
                    s.setName(name);
                    s.setEmail(email);
                    s.setContact(contact);
                    s.setRole(role);
                    s.setDept(dept);
                    s.setNic(nic);
                    s.setUsername(username);
                    s.setPassword(password);
                    s.setStatus(status.isEmpty() ? "Active" : status);

                    boolean added = store.addStaff(s);
                    System.out.println("[StaffServlet] addStaff result: " + added);

                    if (added) {
                        out.print("{\"success\":true," +
                                "\"message\":\"Staff member added successfully.\"," +
                                "\"staffId\":\"" + s.getId() + "\"}");
                    } else {
                        out.print("{\"success\":false," +
                                "\"message\":\"Username already exists. Please choose a different username.\"}");
                    }
                    break;
                }

                // ── EDIT staff profile details ────────────────────────────────────────
                case "edit": {
                    StringBuilder body = new StringBuilder();
                    try (BufferedReader reader = request.getReader()) {
                        String line;
                        while ((line = reader.readLine()) != null) body.append(line);
                    }

                    String json = body.toString();
                    System.out.println("[StaffServlet] edit JSON: " + json);

                    String id      = parseField(json, "id");
                    String name    = parseField(json, "name");
                    String email   = parseField(json, "email");
                    String contact = parseField(json, "contact");
                    String dept    = parseField(json, "dept");
                    String nic     = parseField(json, "nic");

                    Staff existing = store.getById(id);
                    if (existing == null) {
                        out.print("{\"success\":false,\"message\":\"Staff member not found.\"}");
                        break;
                    }

                    existing.setName(name);
                    existing.setEmail(email);
                    existing.setContact(contact);
                    existing.setDept(dept);
                    existing.setNic(nic);

                    boolean updated = store.updateStaff(existing);
                    if (updated) {
                        out.print("{\"success\":true,\"message\":\"Profile updated successfully.\"}");
                    } else {
                        out.print("{\"success\":false,\"message\":\"Failed to update profile.\"}");
                    }
                    break;
                }

                // ── CHANGE PASSWORD ───────────────────────────────────────────────────
                case "changePassword": {
                    StringBuilder body = new StringBuilder();
                    try (BufferedReader reader = request.getReader()) {
                        String line;
                        while ((line = reader.readLine()) != null) body.append(line);
                    }

                    String json = body.toString();
                    System.out.println("[StaffServlet] changePassword JSON: " + json);

                    String id          = parseField(json, "id");
                    String username    = parseField(json, "username");
                    String oldPassword = parseField(json, "oldPassword");
                    String newPassword = parseField(json, "password");

                    System.out.println("[StaffServlet] changePassword — id=" + id
                            + ", username=" + username
                            + ", oldPassword=" + oldPassword);

                    // Find the staff member by id or username
                    Staff staff = store.getById(id);
                    if (staff == null) {
                        staff = store.getByUsername(username);
                    }

                    if (staff == null) {
                        out.print("{\"success\":false,\"message\":\"Staff member not found.\"}");
                        break;
                    }

                    System.out.println("[StaffServlet] DB password=" + staff.getPassword());

                    // Compare plain-text passwords (trim to avoid whitespace issues)
                    if (!staff.getPassword().trim().equals(oldPassword.trim())) {
                        out.print("{\"success\":false,\"message\":\"Current password is incorrect.\"}");
                        break;
                    }

                    if (newPassword.isEmpty() || newPassword.length() < 8) {
                        out.print("{\"success\":false,\"message\":\"New password must be at least 8 characters.\"}");
                        break;
                    }

                    staff.setPassword(newPassword);
                    boolean updated = store.updateStaff(staff);

                    if (updated) {
                        out.print("{\"success\":true,\"message\":\"Password changed successfully.\"}");
                    } else {
                        out.print("{\"success\":false,\"message\":\"Failed to update password.\"}");
                    }
                    break;
                }

                case "delete": {
                    String id = request.getParameter("id");
                    if (id == null || id.isEmpty()) {
                        out.print("{\"success\":false,\"message\":\"Staff ID is required.\"}");
                        break;
                    }
                    boolean deleted = store.deleteStaff(id);
                    if (deleted) {
                        out.print("{\"success\":true,\"message\":\"Staff member removed.\"}");
                    } else {
                        out.print("{\"success\":false,\"message\":\"Staff member not found.\"}");
                    }
                    break;
                }

                default:
                    out.print("{\"error\":\"Unknown action: " + action + "\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"success\":false,\"message\":\"Server error: " + e.getMessage() + "\"}");
        }

        out.flush();
    }

    private String parseField(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "";
        return json.substring(start, end).trim();
    }
}