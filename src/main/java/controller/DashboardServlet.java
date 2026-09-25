package controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.TransactionDao;
import dto.User;
import util.Web;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final TransactionDao transactions = new TransactionDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = Web.current(request.getSession(false));
        request.setAttribute("pageTitle", "Dashboard");
        request.setAttribute("nav", "dashboard");
        request.setAttribute("transactions", transactions.recent(user.getId(), 5));
        request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
    }
}
