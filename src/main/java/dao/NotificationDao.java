package dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import dto.Notification;
import util.JpaUtil;

public class NotificationDao {

    public int unreadCount(int userId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                    "select count(n) from Notification n where n.user.id = :id and n.opened = false",
                    Long.class)
                    .setParameter("id", userId)
                    .getSingleResult();
            return count == null ? 0 : count.intValue();
        } finally {
            em.close();
        }
    }

    public List<Notification> list(int userId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "select n from Notification n where n.user.id = :id order by n.createdAt desc, n.id desc",
                    Notification.class)
                    .setParameter("id", userId)
                    .setMaxResults(100)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void markAllRead(int userId) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery(
                    "update Notification n set n.opened = true where n.user.id = :id and n.opened = false")
                    .setParameter("id", userId)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }
}
