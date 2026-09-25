package filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.NotificationDao;
import dao.UserDao;
import dto.User;

@WebFilter(urlPatterns = {
        "/dashboard",
        "/deposit",
        "/withdraw",
        "/transfer",
        "/transactions",
        "/profile",
        "/password",
        "/assistant",
        "/notifications",
        "/admin",
        "/admin/*"
})
public class AuthFilter implements Filter {

    private final UserDao users = new UserDao();
    private final NotificationDao notifications = new NotificationDao();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");

        HttpSession session = http.getSession(false);
        User current = session == null ? null : (User) session.getAttribute("user");
        if (current == null) {
            String path = http.getRequestURI().substring(http.getContextPath().length());
            String login = http.getContextPath() + "/index.jsp?signin=1";
            if ("/transfer".equals(path) && "POST".equalsIgnoreCase(http.getMethod())) {
                login += "&expired=transfer";
            }
            resp.sendRedirect(login);
            return;
        }

        User fresh = users.findById(current.getId());
        if (fresh == null || !fresh.isEnabled()) {
            session.invalidate();
            resp.sendRedirect(http.getContextPath() + "/index.jsp?signin=1");
            return;
        }
        fresh.setPassword(null);
        session.setAttribute("user", fresh);
        try {
            http.setAttribute("unreadCount", notifications.unreadCount(fresh.getId()));
        } catch (RuntimeException ex) {
            http.setAttribute("unreadCount", 0);
        }

        String path = http.getRequestURI().substring(http.getContextPath().length());
        if ((path.equals("/admin") || path.startsWith("/admin/")) && !fresh.isAdmin()) {
            resp.sendRedirect(http.getContextPath() + "/dashboard");
            return;
        }
        chain.doFilter(request, response);
    }
}
