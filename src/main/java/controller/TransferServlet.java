package controller;

import java.io.IOException;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.UserDao;
import dto.User;
import exception.BankException;
import service.BankService;
import service.TransferReceipt;
import util.Money;
import util.Web;

public class TransferServlet extends HttpServlet {

    private BankService bank;
    private final UserDao users = new UserDao();

    @Override
    public void init() {
        bank = new BankService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Transfer");
        request.setAttribute("nav", "transfer");
        request.getRequestDispatcher("/WEB-INF/views/transfer.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = Web.current(session);
        if (user == null) {
            Web.flashError(request, "Your session ended before the transfer was sent. Sign in and try again.");
            Web.redirect(request, response, "index.jsp?signin=1&expired=transfer");
            return;
        }

        String account = request.getParameter("accountNumber");
        String note = request.getParameter("note");
        try {
            BigDecimal amount = Money.parse(request.getParameter("amount"));
            TransferReceipt receipt = bank.transfer(user.getId(), account, amount, note);
            User fresh = users.findById(user.getId());
            if (fresh != null) {
                Web.remember(session, fresh);
            }
            Web.flashSuccess(request, "Sent " + Money.inr(amount) + " to " + receipt.recipientName()
                    + " (" + receipt.recipientAccount() + "). Reference " + receipt.reference()
                    + ". New balance " + Money.inr(receipt.balance()) + ".");
            Web.redirect(request, response, "dashboard");
        } catch (BankException ex) {
            request.setAttribute("error", ex.getMessage());
            request.setAttribute("accountNumber", account);
            request.setAttribute("amount", request.getParameter("amount"));
            request.setAttribute("note", note);
            request.setAttribute("pageTitle", "Transfer");
            request.setAttribute("nav", "transfer");
            request.getRequestDispatcher("/WEB-INF/views/transfer.jsp").forward(request, response);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            Web.flashError(request, "The transfer could not be completed. Please try again.");
            Web.redirect(request, response, "transfer");
        }
    }
}
