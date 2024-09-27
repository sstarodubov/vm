package org.starodubov.vm;

public record Value(
        ValueTypes type,
        Object number
) {
    public static <T extends Number> Value NUMBER(T number) {
        return new Value(ValueTypes.NUMBER, number);
    }
}
