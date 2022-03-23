package io.github.gerardpi.clargs;

import com.google.common.base.Preconditions;

import java.util.regex.Pattern;

public interface Key {
    String getShortKey();
    String getLongKey();

    default boolean matches(String key) {
        Preconditions.checkNotNull(key);
        return ("-" + getShortKey()).equals(key) || ("--" + getLongKey()).equals(key);
    }

    Pattern RE_KEY = Pattern.compile("(-|--)([a-z]+)");

    default boolean isKey(String something) {
        return RE_KEY.matcher(something).matches();
    }
}
