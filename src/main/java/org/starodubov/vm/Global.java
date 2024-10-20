package org.starodubov.vm;
import org.starodubov.vm.value.NativeObj;
import org.starodubov.vm.value.Value;
import org.starodubov.vm.value.ValueTypes;

import java.util.List;

public record Global(
        List<GlobalVar> globals
) {

    public GlobalVar get(int idx) {
        return globals.get(idx);
    }

    public void set(int idx, Value val) {
        globals.get(idx).value = val;
    }

    public void addConst(String name, Value value) {
        if (exist(name)) {
            return;
        }

        globals.add(new GlobalVar(name, value));
    }

    public void addConst(GlobalVar... vars) {
       for (var v : vars) {
           if (!exist(v.name)) {
               globals.add(v);
           }
       }
    }

    public boolean exist(String name) {
        for (var v : globals) {
            if (v.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public int getGlobalIdx(String name) {
        if (globals.isEmpty()) {
            return -1;
        }

        for (int i = 0; i < globals.size(); i++) {
            if (globals.get(i).name.equals(name)) {
                return i;
            }
        }

        return -1;
    }

    public void define(String name) {
        final var idx = getGlobalIdx(name);
        if (idx != -1) {
            return;
        }

        globals.add(new GlobalVar(name, Value.number(0)));
    }

    public void addNativeFunction(final String name, final Runnable fn, final int arity) {
        if (exist(name)) {
            return;
        }

        globals.add(new GlobalVar(name, new Value(ValueTypes.NATIVE, new NativeObj(name, fn, arity))));
    }
}
