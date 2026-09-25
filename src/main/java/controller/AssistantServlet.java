package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.UserDao;
import dto.User;
import service.AssistantService;
import util.ChatTurn;
import util.Web;

@WebServlet("/assistant")
public class AssistantServlet extends HttpServlet {

    private final AssistantService assistant = new AssistantService();
    private final UserDao users = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("1".equals(request.getParameter("clear"))) {
            request.getSession().removeAttribute("chat");
            Web.redirect(request, response, "assistant");
            return;
        }
        String ask = request.getParameter("ask");
        if (ask != null && !ask.isBlank()) {
            talk(request, ask);
            Web.redirect(request, response, "assistant");
            return;
        }
        show(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        talk(request, request.getParameter("message"));
        Web.redirect(request, response, "assistant");
    }

    private void talk(HttpServletRequest request, String message) {
        String text = message == null ? "" : message.trim();
        if (text.length() > 300) {
            text = text.substring(0, 300);
        }
        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        List<ChatTurn> chat = (List<ChatTurn>) session.getAttribute("chat");
        if (chat == null) {
            chat = new ArrayList<>();
        }
        User sessionUser = Web.current(session);
        User fresh = users.findById(sessionUser.getId());
        String reply = text.isEmpty()
                ? "Type a question first."
                : assistant.reply(fresh, text);
        chat.add(new ChatTurn("user", text.isEmpty() ? "(empty message)" : text));
        chat.add(new ChatTurn("assistant", reply));
        while (chat.size() > 20) {
            chat.remove(0);
        }
        session.setAttribute("chat", chat);
    }

    private void show(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Assistant");
        request.setAttribute("nav", "assistant");
        request.getRequestDispatcher("/WEB-INF/views/assistant.jsp").forward(request, response);
    }
}
