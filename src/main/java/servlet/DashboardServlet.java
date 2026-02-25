package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import dao.Reservation;
import dao.ReservationStore;
import dao.StaffStore;

/**
 * DashboardServlet - Returns session status + dashboard stats
 * GET /DashboardServlet
 */
@WebServlet("/DashboardServlet")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // ── Check session ──
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            out.print("{\"loggedIn\":false}");
            out.flush();
            return;
        }

        String username = (String) session.getAttribute("loggedInUser");
        String role     = (String) session.getAttribute("userRole");

        // ── Pull stats from MySQL via ReservationStore ──
        try {
            ReservationStore store = ReservationStore.getInstance();
            List<Reservation> all  = store.getAll();

            int total  = all.size();
            int active = 0;
            int todayCI = 0;
            double revenue = 0;

            String today = LocalDate.now().toString(); // "yyyy-MM-dd"

            for (Reservation r : all) {
                if ("Active".equals(r.getStatus())) {
                    active++;
                }
                if (today.equals(r.getCheckIn())) {
                    todayCI++;
                }
                // Calculate revenue: nights × room rate
                if (r.getCheckIn() != null && r.getCheckOut() != null) {
                    try {
                        LocalDate ci = LocalDate.parse(r.getCheckIn());
                        LocalDate co = LocalDate.parse(r.getCheckOut());
                        long nights  = co.toEpochDay() - ci.toEpochDay();
                        double rate  = getRoomRate(r.getRoomType());
                        revenue += nights * rate;
                    } catch (Exception ignored) {}
                }
            }

            // Staff count from StaffStore
            int staffCount = StaffStore.getInstance().getCount();

            out.print("{" +
                    "\"loggedIn\":true," +
                    "\"username\":\"" + username + "\"," +
                    "\"role\":\"" + role + "\"," +
                    "\"totalReservations\":" + total + "," +
                    "\"activeReservations\":" + active + "," +
                    "\"todayCheckIns\":"      + todayCI + "," +
                    "\"totalRevenue\":"       + (int) revenue + "," +
                    "\"totalStaff\":"         + staffCount +
                    "}");

        } catch (Exception e) {
            // DB error — still return logged in with zero stats
            out.print("{" +
                    "\"loggedIn\":true," +
                    "\"username\":\"" + username + "\"," +
                    "\"role\":\"" + role + "\"," +
                    "\"totalReservations\":0," +
                    "\"activeReservations\":0," +
                    "\"todayCheckIns\":0," +
                    "\"totalRevenue\":0," +
                    "\"totalStaff\":0" +
                    "}");
            e.printStackTrace();
        }

        out.flush();
    }

    // ── Room rate lookup ──
    private double getRoomRate(String roomType) {
        if (roomType == null) return 0;
        switch (roomType) {
            case "Standard":    return 8500;
            case "Deluxe":      return 12000;
            case "Suite":       return 18500;
            case "Presidential":return 35000;
            default:            return 0;
        }
    }
}