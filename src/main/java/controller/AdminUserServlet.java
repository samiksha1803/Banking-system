package controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.UserDao;
import dto.User;
import exception.BankException;
import service.BankService;
import service.MoneyReceipt;
import util.Money;
import util.Web;

@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {

    private final UserDao users = new UserDao();
    private BankService bank;

    @Override
    public void init() {
        bank = new BankService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("users", users.search(request.getParameter("q")));
        request.setAttribute("pageTitle", "Customers");
        request.setAttribute("nav", "admin-users");
        request.getRequestDispatcher("/WEB-INF/views/admin-users.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User actor = Web.current(request.getSession(false));
        String action = request.getParameter("action");
        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            if ("deactivate".equals(action)) {
                bank.setActive(actor.getId(), userId, false);
                Web.flashSuccess(request, "Account deactivated.");
            } else if ("activate".equals(action)) {
                bank.setActive(actor.getId(), userId, true);
                Web.flashSuccess(request, "Account activated.");
            } else if ("adjust".equals(action)) {
                MoneyReceipt receipt = bank.adjust(userId,
                        request.getParameter("direction"),
                        Money.parse(request.getParameter("amount")),
                        request.getParameter("reason"));
                Web.flashSuccess(request, "Adjustment saved. Reference " + receipt.reference() + ".");
            } else {
                Web.flashError(request, "Unknown action.");
            }
        } catch (NumberFormatException ex) {
            Web.flashError(request, "Choose a customer first.");
        } catch (BankException ex) {
            Web.flashError(request, ex.getMessage());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            Web.flashError(request, "That change could not be saved.");
        }
        String query = request.getParameter("q");
        if (query == null || query.isBlank()) {
            Web.redirect(request, response, "admin/users");
        } else {
            Web.redirect(request, response,
                    "admin/users?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
        }
    }
}
