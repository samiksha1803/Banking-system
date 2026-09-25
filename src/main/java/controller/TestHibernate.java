package controller;

import util.JpaUtil;

public class TestHibernate {

    public static void main(String[] args) {
        JpaUtil.getEntityManager().close();
        System.out.println("Hibernate connected to InBank.");
        JpaUtil.close();
    }
}
