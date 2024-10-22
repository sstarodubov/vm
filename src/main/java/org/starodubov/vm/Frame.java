package org.starodubov.vm;

import org.starodubov.vm.value.FunctionObj;

public record Frame(
        int ra, // return address
        int bp , // base pointer
        FunctionObj fn
) {
}
