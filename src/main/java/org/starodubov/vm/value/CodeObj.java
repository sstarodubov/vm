package org.starodubov.vm.value;

import org.starodubov.vm.LocalVar;
import org.starodubov.vm.utils.Counter;
import org.starodubov.vm.utils.DebugArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record CodeObj(
        List<Integer> bytecode,
        List<Value> constants,
        String name,
        Counter scopeLevel,
        List<LocalVar> locals,
        int arity
) {
    public CodeObj(List<Integer> bytecode, List<Value> constants, String name) {
        this(bytecode, constants, name, new Counter(), new ArrayList<>(), 0);
    }

    public static CodeObj newCo(String name, int arity) {
        return new CodeObj(new ArrayList<>(), new ArrayList<>(), name, new Counter(), new ArrayList<>(), arity);
    }
    public static CodeObj newCoWithDebugSymbols(String name, int arity) {
        return new CodeObj(new ArrayList<>(), new ArrayList<>(), name, new Counter(), new DebugArrayList<>(), arity);
    }

    @Override
    public String toString() {
        final String sBytecode =
                bytecode.stream().map("0x%X"::formatted).collect(Collectors.joining(",", "[", "]"));
        return "{bytecode: %s, constants: %s, name: %s}".formatted(sBytecode, constants, name);
    }

    public int getLocalIdx(final String name) {
        if (locals.isEmpty()) {
            return -1;
        }

        for (int i = locals().size() - 1; i >= 0; i--) {
            if (locals().get(i).name().equals(name)) {
                return i;
            }
        }

        return -1;
    }

    public void addLocal(final String varName) {
       locals().add(new LocalVar(varName, scopeLevel.value()));
    }

    public void addConst(final Value value) {
        constants().add(value);
    }
}
