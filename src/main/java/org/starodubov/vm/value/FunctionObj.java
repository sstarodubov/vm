package org.starodubov.vm.value;

public record FunctionObj(
       CodeObj co
) {

    @Override
    public String toString() {
        return co.name() + "/" + co.arity();

    }
}
