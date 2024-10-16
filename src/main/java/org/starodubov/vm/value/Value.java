package org.starodubov.vm.value;

import java.util.ArrayList;

public record Value(
        ValueTypes type,
        Object obj
) {

    @Override
    public String toString() {
        return "(type=%s, obj=%s)".formatted(type, obj);
    }

    public static <T extends Number> Value number(T number) {
        return new Value(ValueTypes.NUMBER, number);
    }

    public static <T extends String> Value string(T string) {
        return new Value(ValueTypes.STRING, string);
    }

    public static Value code(String name) {
        return new Value(ValueTypes.CODE, new CodeObj(new ArrayList<>(), new ArrayList<>(), name));
    }

    public static long asNumber(Value val) {
        if (val.obj instanceof Number longNumber) {
            return longNumber.longValue();
        } else {
            throw new IllegalStateException("obj.obj() %s is not a obj".formatted(val.obj));
        }
    }

    public static Value bool(boolean val) {
        return new Value(ValueTypes.BOOLEAN, val);
    }

    public static boolean asBoolean(Value val) {
        return as(val, Boolean.class);
    }

    public static String asString(Value val) {
        return as(val, String.class);
    }

    public static CodeObj asCode(Value val) {
        return as(val, CodeObj.class);
    }

    public static <T> T as(Value val, Class<T> type) {
        return type.cast(val.obj);
    }
}
