package org.starodubov.vm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.starodubov.vm.OpCodes.OP_CONST;
import static org.starodubov.vm.OpCodes.OP_HALT;

public class VmTest {

    Vm vm;
    List<Value> constants;
    byte[] bytecode;

    @BeforeEach
    void beforeEach() {
        vm = new Vm();
        constants = new ArrayList<>();
    }
    @Test
    void givenNumber_whenExec_thenReturnSuccess() {
        constants.add(Value.NUMBER(45));
        bytecode = new byte[] {
                OP_CONST, 0,
                OP_HALT
        };

        assertEquals(45, vm.exec(bytecode, constants).number());
    }
}
