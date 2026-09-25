package listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import service.BankService;
import util.JpaUtil;

@WebListener
public class AppListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            new BankService().prepareDatabase();
            System.out.println("InBank is ready.");
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw new IllegalStateException(
                    "InBank could not prepare the PostgreSQL database 'inbank'. "
                            + "Check that PostgreSQL is running and persistence.xml matches your pgAdmin login.",
                    ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        JpaUtil.close();
    }
}
