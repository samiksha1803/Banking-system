package util;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dto.User;

public final class Web {

    private Web() {
    }

    public static void flashSuccess(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flashSuccess", message);
    }

    public static void flashError(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flashError", message);
    }

    public static void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/" + path);
    }

    public static void remember(HttpSession session, User user) {
        user.setPassword(null);
        session.setAttribute("user", user);
    }

    public static User current(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("user");
        return value instanceof User user ? user : null;
    }
}
