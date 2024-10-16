package org.starodubov.vm.value;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record CodeObj(
        List<Integer> bytecode,
        List<Value> constants,
        String name
) {

    public static CodeObj newCo(String name) {
        return new CodeObj(new ArrayList<>(), new ArrayList<>(), name);
    }

    @Override
    public String toString() {
        final String sBytecode =
                bytecode.stream().map("0x%X"::formatted).collect(Collectors.joining(",", "[", "]"));
        return "{bytecode: %s, constants: %s, name: %s}".formatted(sBytecode, constants, name);
    }
}
