package controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dto.User;
import exception.BankException;
import service.BankService;
import util.Web;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private BankService bank;

    @Override
    public void init() {
        bank = new BankService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Profile");
        request.setAttribute("nav", "profile");
        request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = Web.current(request.getSession(false));
        try {
            bank.updateProfile(user.getId(),
                    request.getParameter("fullName"),
                    request.getParameter("email"),
                    request.getParameter("phone"));
            Web.flashSuccess(request, "Profile updated.");
            Web.redirect(request, response, "profile");
        } catch (BankException ex) {
            request.setAttribute("error", ex.getMessage());
            request.setAttribute("pageTitle", "Profile");
            request.setAttribute("nav", "profile");
            request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
        }
    }
}
