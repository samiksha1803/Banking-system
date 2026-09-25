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
import dto.User;
import exception.BankException;
import util.Web;

@WebServlet("/transactions")
public class TransactionServlet extends HttpServlet {

    private final TransactionDao transactions = new TransactionDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = Web.current(request.getSession(false));
        try {
            request.setAttribute("transactions", transactions.search(
                    user.getId(),
                    request.getParameter("type"),
                    date(request.getParameter("from")),
                    date(request.getParameter("to")),
                    request.getParameter("q")));
        } catch (BankException ex) {
            request.setAttribute("error", ex.getMessage());
        }
        request.setAttribute("pageTitle", "Transactions");
        request.setAttribute("nav", "transactions");
        request.setAttribute("showUser", Boolean.FALSE);
        request.getRequestDispatcher("/WEB-INF/views/transactions.jsp").forward(request, response);
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
