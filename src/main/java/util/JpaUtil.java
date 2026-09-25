package util;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public final class JpaUtil {

    private static final EntityManagerFactory FACTORY = createFactory();

    private JpaUtil() {
    }

    private static EntityManagerFactory createFactory() {
        Map<String, Object> overrides = new HashMap<>();
        putEnv(overrides, "javax.persistence.jdbc.url", "INBANK_JDBC_URL");
        putEnv(overrides, "javax.persistence.jdbc.user", "INBANK_DB_USER");
        putEnv(overrides, "javax.persistence.jdbc.password", "INBANK_DB_PASSWORD");
        applyDatabaseUrl(overrides);
        if (overrides.isEmpty()) {
            return Persistence.createEntityManagerFactory("inbank");
        }
        return Persistence.createEntityManagerFactory("inbank", overrides);
    }

    /** Render/Heroku-style DATABASE_URL → JDBC (when INBANK_JDBC_URL is not set). */
    private static void applyDatabaseUrl(Map<String, Object> overrides) {
        if (overrides.containsKey("javax.persistence.jdbc.url")) {
            return;
        }
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }
        String raw = databaseUrl.trim();
        if (raw.startsWith("postgres://")) {
            raw = "postgresql://" + raw.substring("postgres://".length());
        }
        if (!raw.startsWith("postgresql://")) {
            return;
        }
        try {
            URI uri = new URI(raw.replaceFirst("^postgresql:", "http:"));
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath();
            String db = (path != null && path.length() > 1) ? path.substring(1) : "inbank";
            StringBuilder jdbc = new StringBuilder("jdbc:postgresql://")
                    .append(host).append(':').append(port).append('/').append(db);
            int query = raw.indexOf('?');
            if (query >= 0) {
                jdbc.append(raw.substring(query));
            } else {
                jdbc.append("?sslmode=require");
            }
            overrides.put("javax.persistence.jdbc.url", jdbc.toString());
            String userInfo = uri.getUserInfo();
            if (userInfo != null) {
                int colon = userInfo.indexOf(':');
                if (colon > 0) {
                    overrides.putIfAbsent("javax.persistence.jdbc.user", userInfo.substring(0, colon));
                    overrides.putIfAbsent("javax.persistence.jdbc.password", userInfo.substring(colon + 1));
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Could not parse DATABASE_URL for JPA", ex);
        }
    }

    private static void putEnv(Map<String, Object> map, String key, String envName) {
        String value = System.getenv(envName);
        if (value != null && !value.isBlank()) {
            map.put(key, value.trim());
        }
    }

    public static EntityManager getEntityManager() {
        return FACTORY.createEntityManager();
    }

    public static void close() {
        if (FACTORY.isOpen()) {
            FACTORY.close();
        }
    }
}
