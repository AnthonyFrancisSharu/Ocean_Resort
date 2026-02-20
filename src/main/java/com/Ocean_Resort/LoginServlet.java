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
 * LoginServlet - Handles user authentication for Ocean Resort System
 * Supports two roles: Admin and Staff
 * Mapped to: /LoginServlet
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    // ---------------------------------------------------------------
    //  STAFF CREDENTIALS  — redirects to staff-dashboard.html
    // ---------------------------------------------------------------
    private static final String STAFF_USERNAME = "staff";
    private static final String STAFF_PASSWORD = "staff123";

    // ---------------------------------------------------------------
    //  ADMIN CREDENTIALS  — redirects to admin-dashboard.html
    // ---------------------------------------------------------------
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        PrintWriter out = response.getWriter();

        // Get parameters
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Server-side validation
        if (username == null || username.trim().isEmpty()) {
            out.print("{\"success\":false,\"message\":\"Username is required.\"}");
            out.flush();
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            out.print("{\"success\":false,\"message\":\"Password is required.\"}");
            out.flush();
            return;
        }

        username = username.trim();
        password = password.trim();

        // ── Check Admin credentials ──────────────────────────────────
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            HttpSession session = request.getSession(true);
            session.setAttribute("loggedInUser", username);
            session.setAttribute("userRole", "ADMIN");
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            out.print("{" +
                    "\"success\":true," +
                    "\"message\":\"Admin login successful.\"," +
                    "\"role\":\"ADMIN\"," +
                    "\"redirect\":\"admin-dashboard.html\"" +
                    "}");

            // ── Check Staff credentials ──────────────────────────────────
        } else if (STAFF_USERNAME.equals(username) && STAFF_PASSWORD.equals(password)) {
            HttpSession session = request.getSession(true);
            session.setAttribute("loggedInUser", username);
            session.setAttribute("userRole", "STAFF");
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            out.print("{" +
                    "\"success\":true," +
                    "\"message\":\"Staff login successful.\"," +
                    "\"role\":\"STAFF\"," +
                    "\"redirect\":\"staff-dashboard.html\"" +
                    "}");

            // ── Invalid credentials ──────────────────────────────────────
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{" +
                    "\"success\":false," +
                    "\"message\":\"Invalid username or password. Please try again.\"" +
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