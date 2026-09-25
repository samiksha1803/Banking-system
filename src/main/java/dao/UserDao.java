package dao;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import dto.User;
import util.JpaUtil;

public class UserDao {

    public User findById(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }

    public User findByUsername(String username) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<User> users = em.createQuery(
                    "select u from User u where lower(u.username) = :name", User.class)
                    .setParameter("name", username.toLowerCase())
                    .setMaxResults(1)
                    .getResultList();
            return users.isEmpty() ? null : users.get(0);
        } finally {
            em.close();
        }
    }

    public boolean usernameTaken(String username) {
        return count("select count(u) from User u where lower(u.username) = :value",
                username.toLowerCase()) > 0;
    }

    public boolean emailTaken(String email) {
        return count("select count(u) from User u where lower(u.email) = :value",
                email.toLowerCase()) > 0;
    }

    public boolean emailTakenByOther(String email, int userId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                    "select count(u) from User u where lower(u.email) = :email and u.id <> :id",
                    Long.class)
                    .setParameter("email", email.toLowerCase())
                    .setParameter("id", userId)
                    .getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }

    public boolean accountTaken(String accountNumber) {
        return count("select count(u) from User u where u.accountNumber = :value", accountNumber) > 0;
    }

    public void updatePassword(int userId, String hash) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            User user = em.find(User.class, userId);
            if (user != null) {
                user.setPassword(hash);
            }
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

    public List<User> search(String query) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("select u from User u");
            boolean filtered = query != null && !query.isBlank();
            if (filtered) {
                jpql.append(" where lower(u.username) like :q"
                        + " or lower(coalesce(u.fullName, '')) like :q"
                        + " or lower(coalesce(u.email, '')) like :q"
                        + " or coalesce(u.phone, '') like :q"
                        + " or coalesce(u.accountNumber, '') like :q");
            }
            jpql.append(" order by u.id desc");
            var typed = em.createQuery(jpql.toString(), User.class);
            if (filtered) {
                String cleaned = query.toLowerCase().replace("%", "").replace("_", "").trim();
                typed.setParameter("q", "%" + cleaned + "%");
            }
            typed.setMaxResults(200);
            return typed.getResultList();
        } finally {
            em.close();
        }
    }

    public long countCustomers() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                    "select count(u) from User u where u.role is null or u.role <> 'ADMIN'",
                    Long.class).getSingleResult();
            return count == null ? 0 : count;
        } finally {
            em.close();
        }
    }

    public BigDecimal sumBalances() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Object sum = em.createQuery("select sum(u.balance) from User u").getSingleResult();
            if (sum == null) {
                return BigDecimal.ZERO;
            }
            if (sum instanceof BigDecimal value) {
                return value;
            }
            return new BigDecimal(sum.toString());
        } finally {
            em.close();
        }
    }

    public List<User> recent(int limit) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("select u from User u order by u.id desc", User.class)
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    private long count(String jpql, String value) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery(jpql, Long.class)
                    .setParameter("value", value)
                    .getSingleResult();
            return count == null ? 0 : count;
        } finally {
            em.close();
        }
    }
}
