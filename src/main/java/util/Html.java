package util;

public final class Html {

    private Html() {
    }

    public static String esc(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public static String text(String value) {
        if (value == null || value.isBlank()) {
            return "—";
        }
        return esc(value);
    }
}
