package controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.NotificationDao;
import dto.User;
import service.BankService;
import util.Web;

@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {

    private final NotificationDao notifications = new NotificationDao();
    private final BankService bank = new BankService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = Web.current(request.getSession(false));
        request.setAttribute("notes", notifications.list(user.getId()));
        notifications.markAllRead(user.getId());
        request.setAttribute("mailOn", bank.isMailEnabled());
        request.setAttribute("pageTitle", "Alerts");
        request.setAttribute("nav", "alerts");
        request.getRequestDispatcher("/WEB-INF/views/notifications.jsp").forward(request, response);
    }
}
