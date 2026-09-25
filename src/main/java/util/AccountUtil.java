package util;

import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class AccountUtil {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private AccountUtil() {
    }

    public static String accountNumber(Set<String> used) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < 40; attempt++) {
            StringBuilder number = new StringBuilder("6210");
            for (int i = 0; i < 8; i++) {
                number.append(random.nextInt(10));
            }
            String value = number.toString();
            if (used == null || !used.contains(value)) {
                return value;
            }
        }
        throw new IllegalStateException("Could not allocate an account number");
    }

    public static String reference() {
        int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "TXN" + TimeUtil.now().format(STAMP) + suffix;
    }
}
