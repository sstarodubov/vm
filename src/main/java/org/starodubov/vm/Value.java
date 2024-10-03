package org.starodubov.vm;

public record Value(
        ValueTypes type,
        Object obj
) {
    public static <T extends Number> Value number(T number) {
        return new Value(ValueTypes.NUMBER, number);
    }

    public static <T extends String> Value string(T string) {
        return new Value(ValueTypes.STRING, string);
    }

    public static long as_number(Value val) {
        if (val.obj instanceof Number longNumber) {
            return longNumber.longValue();
        } else {
            throw new IllegalStateException("obj.obj() %s is not a obj".formatted(val.obj));
        }
    }

    public static String as_string(Value val) {
        return as(val, String.class);
    }

    public static <T> T as(Value val, Class<T> type) {
        return type.cast(val.obj);
    }
}
