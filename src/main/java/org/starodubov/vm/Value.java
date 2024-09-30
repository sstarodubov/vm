package org.starodubov.vm;

public record Value(
        ValueTypes type,
        Object number
) {
    public static <T extends Number> Value NUMBER(T number) {
        return new Value(ValueTypes.NUMBER, number);
    }

    public static long AS_NUMBER(Value val) {
        if (val.number instanceof Number longNumber) {
            return longNumber.longValue();
        }

        throw new IllegalStateException("val.number() %s is not a number".formatted(val.number));
    }
}
