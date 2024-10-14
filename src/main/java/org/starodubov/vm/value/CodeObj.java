package org.starodubov.vm.value;

import java.util.ArrayList;
import java.util.List;

public record CodeObj(
        List<Integer> bytecode,
        List<Value> constants,
        String name
) {

    public static CodeObj newCo(String name) {
       return new CodeObj(new ArrayList<>(), new ArrayList<>(), name);
    }
}
