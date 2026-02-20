package com.Ocean_Resort;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Properties;

/**
 * EmailService - Handles sending reservation confirmation emails to guests.
 * Uses Gmail SMTP with Jakarta Mail (JavaMail) API.
 *
 * Setup required:
 *  1. Add jakarta.mail dependency to pom.xml
 *  2. Enable 2-Step Verification on your Gmail account
 *  3. Generate an App Password from Google Account > Security > App Passwords
 *  4. Replace SENDER_EMAIL and SENDER_APP_PASSWORD below with your details
 */
public class EmailService {

    // ---------------------------------------------------------------
    // CONFIGURE THESE WITH YOUR GMAIL DETAILS
    // ---------------------------------------------------------------
    private static final String SENDER_EMAIL    = "sharuanthonyfrancis@gmail.com";   // Your Gmail
    private static final String SENDER_APP_PASSWORD = "werk wmjz mdnj dkgl"; // Gmail App Password (16 chars)
    private static final String HOTEL_NAME      = "Ocean View Resort";
    private static final String HOTEL_PHONE     = "+94 91 234 5678";
    private static final String HOTEL_ADDRESS   = "Beach Road, Galle, Sri Lanka";
    private static final String HOTEL_EMAIL     = "reservations@oceanresort.lk";
    // ---------------------------------------------------------------

    /**
     * Sends a reservation confirmation email to the guest.
     *
     * @param guestEmail      Guest's email address
     * @param guestName       Guest's full name
     * @param reservationId   Unique reservation ID
     * @param roomNumber      Room number assigned
     * @param roomType        Type of room (e.g., Deluxe, Suite)
     * @param checkInDate     Check-in date (e.g., "2025-12-20")
     * @param checkInTime     Check-in time (e.g., "14:00")
     * @param checkOutDate    Check-out date (e.g., "2025-12-25")
     * @param checkOutTime    Check-out time (e.g., "12:00")
     * @param numberOfNights  Total nights of stay
     * @param ratePerNight    Room rate per night in LKR
     * @param totalAmount     Total bill amount in LKR
     * @param specialRequests Any special requests from the guest
     * @return true if email sent successfully, false if failed
     */
    public boolean sendReservationConfirmation(
            String guestEmail,
            String guestName,
            String reservationId,
            String roomNumber,
            String roomType,
            String checkInDate,
            String checkInTime,
            String checkOutDate,
            String checkOutTime,
            int    numberOfNights,
            double ratePerNight,
            double totalAmount,
            String specialRequests
    ) {
        // Configure Gmail SMTP properties
        Properties props = new Properties();
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");

        // Create authenticated session
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
            }
        });

        try {
            // Build the email
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, HOTEL_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(guestEmail));
            message.setSubject("Reservation Confirmation - " + HOTEL_NAME + " | " + reservationId);

            // Build HTML email body
            String htmlBody = buildEmailBody(
                    guestName, reservationId, roomNumber, roomType,
                    checkInDate, checkInTime, checkOutDate, checkOutTime,
                    numberOfNights, ratePerNight, totalAmount, specialRequests
            );

            // Set content as HTML
            message.setContent(htmlBody, "text/html; charset=utf-8");

            // Send the email
            Transport.send(message);

            System.out.println("✅ Confirmation email sent to: " + guestEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Failed to send email to " + guestEmail + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Builds the HTML email body with reservation details.
     */
    private String buildEmailBody(
            String guestName,
            String reservationId,
            String roomNumber,
            String roomType,
            String checkInDate,
            String checkInTime,
            String checkOutDate,
            String checkOutTime,
            int numberOfNights,
            double ratePerNight,
            double totalAmount,
            String specialRequests
    ) {
        // Format currency
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);
        String formattedRate  = "LKR " + currencyFormat.format(ratePerNight);
        String formattedTotal = "LKR " + currencyFormat.format(totalAmount);

        // Calculate charges breakdown
        double roomCharge    = ratePerNight * numberOfNights;
        double serviceCharge = roomCharge * 0.10;
        double vat           = roomCharge * 0.08;

        String formattedRoomCharge    = "LKR " + currencyFormat.format(roomCharge);
        String formattedServiceCharge = "LKR " + currencyFormat.format(serviceCharge);
        String formattedVat           = "LKR " + currencyFormat.format(vat);

        String specialRequestsRow = (specialRequests != null && !specialRequests.trim().isEmpty())
                ? "<tr><td style='padding:10px 0;color:#666;border-bottom:1px solid #eee;'>Special Requests</td>" +
                "<td style='padding:10px 0;font-weight:600;color:#333;border-bottom:1px solid #eee;'>" + specialRequests + "</td></tr>"
                : "";

        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'></head>" +
                "<body style='margin:0;padding:0;background:#f4f7fb;font-family:Segoe UI,Arial,sans-serif;'>" +

                // Outer wrapper
                "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f4f7fb;padding:30px 0;'><tr><td align='center'>" +
                "<table width='620' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);'>" +

                // Header
                "<tr><td style='background:linear-gradient(135deg,#0f4c75,#1b6ca8);padding:35px 40px;text-align:center;'>" +
                "<h1 style='color:#ffffff;margin:0;font-size:26px;letter-spacing:1px;'>🌊 " + HOTEL_NAME + "</h1>" +
                "<p style='color:rgba(255,255,255,0.8);margin:8px 0 0;font-size:14px;letter-spacing:1px;'>RESERVATION CONFIRMATION</p>" +
                "</td></tr>" +

                // Greeting
                "<tr><td style='padding:35px 40px 20px;'>" +
                "<p style='font-size:16px;color:#333;margin:0 0 8px;'>Dear <strong>" + guestName + "</strong>,</p>" +
                "<p style='font-size:14px;color:#666;line-height:1.7;margin:0;'>Thank you for choosing <strong>" + HOTEL_NAME + "</strong>. " +
                "Your reservation has been confirmed. Please find your booking details below.</p>" +
                "</td></tr>" +

                // Reservation ID banner
                "<tr><td style='padding:0 40px 25px;'>" +
                "<div style='background:#f0f7ff;border:1px dashed #1b6ca8;border-radius:10px;padding:16px 20px;text-align:center;'>" +
                "<p style='margin:0;font-size:12px;color:#888;letter-spacing:1px;text-transform:uppercase;'>Reservation ID</p>" +
                "<p style='margin:6px 0 0;font-size:24px;font-weight:700;color:#0f4c75;letter-spacing:2px;'>" + reservationId + "</p>" +
                "</div></td></tr>" +

                // Check-in / Check-out boxes
                "<tr><td style='padding:0 40px 25px;'>" +
                "<table width='100%' cellpadding='0' cellspacing='0'><tr>" +

                // Check-in box
                "<td width='48%' style='background:#e8f5e9;border-radius:10px;padding:18px 20px;vertical-align:top;'>" +
                "<p style='margin:0;font-size:11px;color:#888;text-transform:uppercase;letter-spacing:1px;'>✅ Check-In</p>" +
                "<p style='margin:6px 0 3px;font-size:18px;font-weight:700;color:#2e7d32;'>" + checkInDate + "</p>" +
                "<p style='margin:0;font-size:13px;color:#555;'>⏰ " + checkInTime + " onwards</p>" +
                "</td>" +

                "<td width='4%'></td>" +

                // Check-out box
                "<td width='48%' style='background:#fff3e0;border-radius:10px;padding:18px 20px;vertical-align:top;'>" +
                "<p style='margin:0;font-size:11px;color:#888;text-transform:uppercase;letter-spacing:1px;'>🔑 Check-Out</p>" +
                "<p style='margin:6px 0 3px;font-size:18px;font-weight:700;color:#e65100;'>" + checkOutDate + "</p>" +
                "<p style='margin:0;font-size:13px;color:#555;'>⏰ By " + checkOutTime + "</p>" +
                "</td>" +

                "</tr></table></td></tr>" +

                // Reservation details table
                "<tr><td style='padding:0 40px 25px;'>" +
                "<h3 style='font-size:15px;color:#1a1a2e;margin:0 0 15px;padding-bottom:10px;border-bottom:2px solid #f0f0f0;'>Booking Details</h3>" +
                "<table width='100%' cellpadding='0' cellspacing='0'>" +

                "<tr><td style='padding:10px 0;color:#666;border-bottom:1px solid #eee;width:45%;'>Guest Name</td>" +
                "<td style='padding:10px 0;font-weight:600;color:#333;border-bottom:1px solid #eee;'>" + guestName + "</td></tr>" +

                "<tr><td style='padding:10px 0;color:#666;border-bottom:1px solid #eee;'>Room Number</td>" +
                "<td style='padding:10px 0;font-weight:600;color:#333;border-bottom:1px solid #eee;'>" + roomNumber + "</td></tr>" +

                "<tr><td style='padding:10px 0;color:#666;border-bottom:1px solid #eee;'>Room Type</td>" +
                "<td style='padding:10px 0;font-weight:600;color:#333;border-bottom:1px solid #eee;'>" + roomType + "</td></tr>" +

                "<tr><td style='padding:10px 0;color:#666;border-bottom:1px solid #eee;'>Duration</td>" +
                "<td style='padding:10px 0;font-weight:600;color:#333;border-bottom:1px solid #eee;'>" + numberOfNights + " Night(s)</td></tr>" +

                "<tr><td style='padding:10px 0;color:#666;border-bottom:1px solid #eee;'>Rate Per Night</td>" +
                "<td style='padding:10px 0;font-weight:600;color:#333;border-bottom:1px solid #eee;'>" + formattedRate + "</td></tr>" +

                specialRequestsRow +

                "</table></td></tr>" +

                // Bill breakdown
                "<tr><td style='padding:0 40px 25px;'>" +
                "<h3 style='font-size:15px;color:#1a1a2e;margin:0 0 15px;padding-bottom:10px;border-bottom:2px solid #f0f0f0;'>Bill Summary</h3>" +
                "<table width='100%' cellpadding='0' cellspacing='0'>" +

                "<tr><td style='padding:8px 0;color:#666;'>Room Charge (" + numberOfNights + " nights)</td>" +
                "<td style='padding:8px 0;text-align:right;color:#444;'>" + formattedRoomCharge + "</td></tr>" +

                "<tr><td style='padding:8px 0;color:#666;'>Service Charge (10%)</td>" +
                "<td style='padding:8px 0;text-align:right;color:#444;'>" + formattedServiceCharge + "</td></tr>" +

                "<tr><td style='padding:8px 0;color:#666;'>VAT (8%)</td>" +
                "<td style='padding:8px 0;text-align:right;color:#444;'>" + formattedVat + "</td></tr>" +

                "<tr><td colspan='2' style='border-top:2px solid #eee;padding-top:5px;'></td></tr>" +

                "<tr><td style='padding:10px 0;font-size:16px;font-weight:700;color:#0f4c75;'>Total Amount Due</td>" +
                "<td style='padding:10px 0;text-align:right;font-size:16px;font-weight:700;color:#0f4c75;'>" + formattedTotal + "</td></tr>" +

                "</table></td></tr>" +

                // Important note
                "<tr><td style='padding:0 40px 30px;'>" +
                "<div style='background:#fff8e1;border-left:4px solid #ffc107;border-radius:0 10px 10px 0;padding:14px 18px;'>" +
                "<p style='margin:0;font-size:13px;color:#7d5a00;line-height:1.7;'>" +
                "<strong>📌 Important:</strong> Please present this confirmation email or your Reservation ID at the front desk upon arrival. " +
                "Payment is due at check-in. Early check-in and late check-out are subject to availability.</p>" +
                "</div></td></tr>" +

                // Contact info
                "<tr><td style='background:#f8fbff;padding:25px 40px;border-top:1px solid #eee;'>" +
                "<p style='margin:0 0 10px;font-size:13px;font-weight:700;color:#0f4c75;'>Need help? Contact us:</p>" +
                "<p style='margin:0;font-size:13px;color:#666;line-height:1.8;'>" +
                "📞 " + HOTEL_PHONE + "<br>" +
                "📧 " + HOTEL_EMAIL + "<br>" +
                "📍 " + HOTEL_ADDRESS +
                "</p></td></tr>" +

                // Footer
                "<tr><td style='background:#0f4c75;padding:18px 40px;text-align:center;'>" +
                "<p style='margin:0;font-size:12px;color:rgba(255,255,255,0.6);'>" +
                "© 2025 " + HOTEL_NAME + " | This is an automated email, please do not reply directly." +
                "</p></td></tr>" +

                "</table>" +
                "</td></tr></table>" +
                "</body></html>";
    }
}