package utils;

public class EnumUtil {

    public static <T extends Enum<T>> T parse(String str, Class<T> enumClass) {
        return parse(str, enumClass, null);
    }

    public static <T extends Enum<T>> T parse(String str, Class<T> enumClass, T fallback) {
        if (str != null) {
            for (T iterate : enumClass.getEnumConstants()) {
                if (str.equals(iterate.toString())) {
                    return iterate;
                }
            }
        }
        return fallback;
    }

}
