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

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private BankService bank;

    @Override
    public void init() {
        bank = new BankService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fullName = request.getParameter("fullName");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirmPassword");
        try {
            User user = bank.register(fullName, username, email, phone, password, confirm);
            Web.flashSuccess(request,
                    "Account created. Your account number is " + user.getAccountNumber() + ". Sign in to continue.");
            Web.redirect(request, response, "index.jsp");
        } catch (BankException ex) {
            keepForm(request, fullName, username, email, phone, ex.getMessage());
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            keepForm(request, fullName, username, email, phone,
                    "The account could not be saved. Check that PostgreSQL is running, then try again.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }

    private void keepForm(HttpServletRequest request, String fullName, String username,
            String email, String phone, String message) {
        request.setAttribute("error", message);
        request.setAttribute("fullName", fullName);
        request.setAttribute("username", username);
        request.setAttribute("email", email);
        request.setAttribute("phone", phone);
    }
}
