package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import util.DatabaseConnection;

/**
 * LoginServlet - Authenticates users against MySQL staff table
 * Role is determined by the 'role' column in the staff table:
 *   role = 'Admin'  → redirects to admin-dashboard.html
 *   anything else   → redirects to staff-dashboard.html
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // ── Basic validation ──
        if (username == null || username.trim().isEmpty()) {
            out.print("{\"success\":false,\"message\":\"Username is required.\"}");
            out.flush(); return;
        }
        if (password == null || password.trim().isEmpty()) {
            out.print("{\"success\":false,\"message\":\"Password is required.\"}");
            out.flush(); return;
        }

        username = username.trim();
        password = password.trim();

        // ── Check credentials against MySQL ──
        String sql = "SELECT * FROM staff WHERE username = ? AND password = ? AND status = 'Active'";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // ── Valid credentials found ──
                String staffId   = rs.getString("staff_id");
                String staffName = rs.getString("name");
                String role      = rs.getString("role"); // e.g. "Admin", "Manager", "Front Desk"

                // Determine session role — Admin role gets ADMIN, everyone else gets STAFF
                String sessionRole = "Admin".equalsIgnoreCase(role) ? "ADMIN" : "STAFF";
                String redirect    = "ADMIN".equals(sessionRole) ? "admin-dashboard.html" : "staff-dashboard.html";

                // Create session
                HttpSession session = request.getSession(true);
                session.setAttribute("loggedInUser", username);
                session.setAttribute("userRole",     sessionRole);
                session.setAttribute("staffName",    staffName);
                session.setAttribute("staffId",      staffId);
                session.setMaxInactiveInterval(30 * 60); // 30 minutes

                out.print("{" +
                        "\"success\":true," +
                        "\"message\":\"Login successful. Welcome, " + staffName + "!\"," +
                        "\"role\":\"" + sessionRole + "\"," +
                        "\"name\":\"" + staffName + "\"," +
                        "\"redirect\":\"" + redirect + "\"" +
                        "}");

            } else {
                // ── Invalid credentials ──
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{" +
                        "\"success\":false," +
                        "\"message\":\"Invalid username or password. Please try again.\"" +
                        "}");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{" +
                    "\"success\":false," +
                    "\"message\":\"Database error: " + e.getMessage() + "\"" +
                    "}");
        }

        out.flush();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("login.html");
    }
}