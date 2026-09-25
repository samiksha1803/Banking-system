package controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dto.User;
import exception.BankException;
import service.BankService;
import util.Web;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private BankService bank;

    @Override
    public void init() {
        bank = new BankService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Web.redirect(request, response, "index.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User user = bank.login(request.getParameter("username"), request.getParameter("password"));
            HttpSession previous = request.getSession(false);
            if (previous != null) {
                User old = Web.current(previous);
                if (old != null && old.getId() == user.getId()) {
                    Web.remember(previous, user);
                    Web.redirect(request, response, user.isAdmin() ? "admin" : "dashboard");
                    return;
                }
                previous.invalidate();
            }
            HttpSession session = request.getSession(true);
            Web.remember(session, user);
            Web.redirect(request, response, user.isAdmin() ? "admin" : "dashboard");
        } catch (BankException ex) {
            Web.flashError(request, ex.getMessage());
            Web.redirect(request, response, "index.jsp");
        }
    }
}
