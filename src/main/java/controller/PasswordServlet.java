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

@WebServlet("/password")
public class PasswordServlet extends HttpServlet {

    private BankService bank;

    @Override
    public void init() {
        bank = new BankService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Change password");
        request.setAttribute("nav", "password");
        request.getRequestDispatcher("/WEB-INF/views/password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        User user = Web.current(request.getSession(false));
        try {
            bank.changePassword(user.getId(),
                    request.getParameter("currentPassword"),
                    request.getParameter("newPassword"),
                    request.getParameter("confirmPassword"));
            Web.flashSuccess(request, "Password changed.");
            Web.redirect(request, response, "password");
        } catch (BankException ex) {
            request.setAttribute("error", ex.getMessage());
            request.setAttribute("pageTitle", "Change password");
            request.setAttribute("nav", "password");
            request.getRequestDispatcher("/WEB-INF/views/password.jsp").forward(request, response);
        }
    }
}
