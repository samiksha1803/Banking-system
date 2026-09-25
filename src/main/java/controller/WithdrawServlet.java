package controller;

import java.io.IOException;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dto.User;
import exception.BankException;
import service.BankService;
import service.MoneyReceipt;
import util.Money;
import util.Web;

@WebServlet("/withdraw")
public class WithdrawServlet extends HttpServlet {

    private BankService bank;

    @Override
    public void init() {
        bank = new BankService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Web.redirect(request, response, "dashboard");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = Web.current(request.getSession(false));
        try {
            BigDecimal amount = Money.parse(request.getParameter("amount"));
            MoneyReceipt receipt = bank.withdraw(user.getId(), amount);
            Web.flashSuccess(request, "Withdrew " + Money.inr(amount)
                    + ". Reference " + receipt.reference()
                    + ". New balance " + Money.inr(receipt.balance()) + ".");
        } catch (BankException ex) {
            Web.flashError(request, ex.getMessage());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            Web.flashError(request, "The withdrawal could not be completed. Please try again.");
        }
        Web.redirect(request, response, "dashboard");
    }
}
