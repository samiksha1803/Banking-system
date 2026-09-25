package util;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
        applyRenderDatabaseEnv(overrides);
        applyDatabaseUrl(overrides);
        if (overrides.isEmpty()) {
            return Persistence.createEntityManagerFactory("inbank");
        }
        return Persistence.createEntityManagerFactory("inbank", overrides);
    }

    /** Render Blueprint: INBANK_DB_HOST, PORT, NAME, USER, PASSWORD (avoids broken DATABASE_URL parsing). */
    private static void applyRenderDatabaseEnv(Map<String, Object> overrides) {
        if (overrides.containsKey("javax.persistence.jdbc.url")) {
            return;
        }
        String host = env("INBANK_DB_HOST");
        if (host == null) {
            return;
        }
        String port = env("INBANK_DB_PORT");
        String name = env("INBANK_DB_NAME");
        if (name == null) {
            name = "inbank";
        }
        int portNum = 5432;
        if (port != null && !port.isBlank()) {
            portNum = Integer.parseInt(port.trim());
        }
        String jdbc = "jdbc:postgresql://" + host.trim() + ":" + portNum + "/" + name.trim()
                + "?sslmode=require";
        overrides.put("javax.persistence.jdbc.url", jdbc);
        putEnv(overrides, "javax.persistence.jdbc.user", "INBANK_DB_USER");
        putEnv(overrides, "javax.persistence.jdbc.password", "INBANK_DB_PASSWORD");
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
                    String user = URLDecoder.decode(userInfo.substring(0, colon), StandardCharsets.UTF_8);
                    String password = URLDecoder.decode(userInfo.substring(colon + 1), StandardCharsets.UTF_8);
                    overrides.putIfAbsent("javax.persistence.jdbc.user", user);
                    overrides.putIfAbsent("javax.persistence.jdbc.password", password);
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Could not parse DATABASE_URL for JPA", ex);
        }
    }

    private static void putEnv(Map<String, Object> map, String key, String envName) {
        String value = env(envName);
        if (value != null) {
            map.put(key, value);
        }
    }

    private static String env(String envName) {
        String value = System.getenv(envName);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
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
