package servlet;

import util.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/ReportsServlet")
public class ReportsServlet extends HttpServlet {

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

                case "auditLog":
                    out.print(getAuditLog());
                    break;

                case "revenueLog":
                    out.print(getRevenueLog());
                    break;

                default:
                    out.print("{\"error\":\"Unknown action\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }

        out.flush();
    }

    private String getAuditLog() throws SQLException {
        String sql = "SELECT * FROM reservation_audit ORDER BY changed_at DESC LIMIT 50";
        StringBuilder sb = new StringBuilder("{\"logs\":[");

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean first = true;
            while (rs.next()) {
                if (!first) sb.append(",");
                sb.append("{")
                        .append("\"actionType\":\"").append(safe(rs.getString("action_type"))).append("\",")
                        .append("\"reservationId\":\"").append(safe(rs.getString("reservation_id"))).append("\",")
                        .append("\"guestName\":\"").append(safe(rs.getString("guest_name"))).append("\",")
                        .append("\"roomType\":\"").append(safe(rs.getString("room_type"))).append("\",")
                        .append("\"status\":\"").append(safe(rs.getString("status"))).append("\",")
                        .append("\"changedAt\":\"").append(safe(rs.getString("changed_at"))).append("\"")
                        .append("}");
                first = false;
            }
        }

        sb.append("]}");
        return sb.toString();
    }

    private String getRevenueLog() throws SQLException {
        String sql = "SELECT * FROM revenue_log ORDER BY logged_at DESC LIMIT 50";
        StringBuilder sb = new StringBuilder("{\"logs\":[");

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean first = true;
            while (rs.next()) {
                if (!first) sb.append(",");
                sb.append("{")
                        .append("\"reservationId\":\"").append(safe(rs.getString("reservation_id"))).append("\",")
                        .append("\"roomType\":\"").append(safe(rs.getString("room_type"))).append("\",")
                        .append("\"nights\":").append(rs.getInt("nights")).append(",")
                        .append("\"roomCharge\":").append(rs.getDouble("room_charge")).append(",")
                        .append("\"serviceCharge\":").append(rs.getDouble("service_charge")).append(",")
                        .append("\"vatAmount\":").append(rs.getDouble("vat_amount")).append(",")
                        .append("\"totalAmount\":").append(rs.getDouble("total_amount")).append(",")
                        .append("\"loggedAt\":\"").append(safe(rs.getString("logged_at"))).append("\"")
                        .append("}");
                first = false;
            }
        }

        sb.append("]}");
        return sb.toString();
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}