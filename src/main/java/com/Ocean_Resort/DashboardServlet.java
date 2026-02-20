package com.Ocean_Resort;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * DashboardServlet - Returns session info + reservation stats for dashboard
 * Called by dashboard.html on page load to verify login and load stats
 */
@WebServlet("/DashboardServlet")
public class DashboardServlet extends HttpServlet {

    private final ReservationStore store = ReservationStore.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Check if user is logged in
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("loggedInUser") == null) {
            // ❌ Not logged in — tell the dashboard to redirect
            out.print("{" +
                    "\"loggedIn\":false," +
                    "\"message\":\"Session expired. Please log in again.\"" +
                    "}");
            out.flush();
            return;
        }

        // ✅ Logged in — get session data
        String username = (String) session.getAttribute("loggedInUser");
        String role     = (String) session.getAttribute("userRole");
        if (role == null) role = "STAFF";

        // Get reservation stats from store
        int    totalReservations  = store.getTotalCount();
        long   activeReservations = store.getActiveCount();
        long   todayCheckIns      = store.getTodayCheckIns();
        double totalRevenue       = store.getTotalRevenue();

        // Build JSON response
        out.print("{" +
                "\"loggedIn\":true," +
                "\"username\":\"" + username + "\"," +
                "\"role\":\"" + role + "\"," +
                "\"totalReservations\":" + totalReservations + "," +
                "\"activeReservations\":" + activeReservations + "," +
                "\"todayCheckIns\":" + todayCheckIns + "," +
                "\"totalRevenue\":" + totalRevenue +
                "}");

        out.flush();
    }
}