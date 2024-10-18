package org.starodubov.vm;

public record LocalVar(
        String name,
        int scopeLevel
) {

}
