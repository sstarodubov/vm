package org.starodubov.vm.value;

import java.util.ArrayList;

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

    public static Value code(String name) {
        return new Value(ValueTypes.CODE, new CodeObj(new ArrayList<>(), new ArrayList<>(), name));
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

    public static CodeObj as_code(Value val) {
        return as(val, CodeObj.class);
    }

    public static <T> T as(Value val, Class<T> type) {
        return type.cast(val.obj);
    }
}
