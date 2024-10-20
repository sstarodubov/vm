package org.starodubov.vm.value;

public record NativeObj(
        String name,
        Runnable fn,
        int arity
) {
}
