package dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import dto.Transaction;
import util.JpaUtil;

public class TransactionDao {

    public List<Transaction> recent(int userId, int limit) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "select t from Transaction t join fetch t.user u where u.id = :id order by t.createdAt desc, t.id desc",
                    Transaction.class)
                    .setParameter("id", userId)
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Transaction> latest(int limit) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "select t from Transaction t join fetch t.user u order by t.createdAt desc, t.id desc",
                    Transaction.class)
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Transaction> search(Integer userId, String type, LocalDate from, LocalDate to, String query) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "select t from Transaction t join fetch t.user u where 1 = 1");
            Map<String, Object> params = new LinkedHashMap<>();
            if (userId != null) {
                jpql.append(" and u.id = :userId");
                params.put("userId", userId);
            }
            if (type != null && !type.isBlank()) {
                jpql.append(" and t.type = :type");
                params.put("type", type);
            }
            if (from != null) {
                jpql.append(" and t.createdAt >= :from");
                params.put("from", from.atStartOfDay());
            }
            if (to != null) {
                jpql.append(" and t.createdAt < :to");
                params.put("to", to.plusDays(1).atStartOfDay());
            }
            if (query != null && !query.isBlank()) {
                jpql.append(" and (lower(coalesce(t.referenceNumber, '')) like :q"
                        + " or lower(coalesce(t.note, '')) like :q"
                        + " or lower(coalesce(t.counterpartyAccount, '')) like :q"
                        + " or lower(coalesce(t.counterpartyName, '')) like :q"
                        + " or lower(u.username) like :q"
                        + " or lower(coalesce(u.fullName, '')) like :q"
                        + " or coalesce(u.accountNumber, '') like :q)");
                String cleaned = query.toLowerCase().replace("%", "").replace("_", "").trim();
                params.put("q", "%" + cleaned + "%");
            }
            jpql.append(" order by t.createdAt desc, t.id desc");
            TypedQuery<Transaction> typed = em.createQuery(jpql.toString(), Transaction.class);
            params.forEach(typed::setParameter);
            typed.setMaxResults(200);
            return typed.getResultList();
        } finally {
            em.close();
        }
    }

    public long countSince(LocalDateTime start) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                    "select count(t) from Transaction t where t.createdAt >= :start", Long.class)
                    .setParameter("start", start)
                    .getSingleResult();
            return count == null ? 0 : count;
        } finally {
            em.close();
        }
    }

    public long countAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery("select count(t) from Transaction t", Long.class)
                    .getSingleResult();
            return count == null ? 0 : count;
        } finally {
            em.close();
        }
    }
}
