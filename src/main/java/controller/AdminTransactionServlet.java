package controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.TransactionDao;
import dao.UserDao;
import exception.BankException;

@WebServlet("/admin/transactions")
public class AdminTransactionServlet extends HttpServlet {

    private final TransactionDao transactions = new TransactionDao();
    private final UserDao users = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer userId = null;
        String rawId = request.getParameter("userId");
        if (rawId != null && !rawId.isBlank()) {
            try {
                userId = Integer.parseInt(rawId);
                request.setAttribute("owner", users.findById(userId));
            } catch (NumberFormatException ex) {
                request.setAttribute("error", "That customer filter is not valid.");
            }
        }
        try {
            request.setAttribute("transactions", transactions.search(
                    userId,
                    request.getParameter("type"),
                    date(request.getParameter("from")),
                    date(request.getParameter("to")),
                    request.getParameter("q")));
        } catch (BankException ex) {
            request.setAttribute("error", ex.getMessage());
        }
        request.setAttribute("showUser", Boolean.TRUE);
        request.setAttribute("pageTitle", "All transactions");
        request.setAttribute("nav", "admin-txns");
        request.getRequestDispatcher("/WEB-INF/views/admin-transactions.jsp").forward(request, response);
    }

    private LocalDate date(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException ex) {
            throw new BankException("Enter a valid date.");
        }
    }
}
