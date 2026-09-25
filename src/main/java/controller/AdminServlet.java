package controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.TransactionDao;
import dao.UserDao;
import util.TimeUtil;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

    private final UserDao users = new UserDao();
    private final TransactionDao transactions = new TransactionDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("customerCount", users.countCustomers());
        request.setAttribute("totalBalance", users.sumBalances());
        request.setAttribute("todayCount", transactions.countSince(TimeUtil.now().toLocalDate().atStartOfDay()));
        request.setAttribute("allCount", transactions.countAll());
        request.setAttribute("transactions", transactions.latest(8));
        request.setAttribute("recentUsers", users.recent(5));
        request.setAttribute("showUser", Boolean.TRUE);
        request.setAttribute("pageTitle", "Admin");
        request.setAttribute("nav", "admin");
        request.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(request, response);
    }
}
