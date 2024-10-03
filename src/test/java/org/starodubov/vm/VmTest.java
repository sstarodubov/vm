package org.starodubov.vm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.starodubov.vm.OpCodes.*;

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
        constants = List.of(Value.number(45), Value.number(99));
        bytecode = new byte[]{
                OP_CONST, 1,
                OP_HALT
        };

        assertEquals(99, vm.exec(bytecode, constants).obj());
    }

    @Test
    void givenNumbers_whenAdd_thenReturnCorrectResult() {
        constants = List.of(Value.number(2), Value.number(3));
        bytecode = new byte[]{
                OP_CONST, 1,
                OP_CONST, 0,
                OP_ADD,
                OP_HALT
        };

        assertEquals(5L, vm.exec(bytecode, constants).obj());
    }

    @Test
    void givenNumbers_whenSub_thenReturnCorrectResult() {
        constants = List.of(Value.number(2), Value.number(3));
        bytecode = new byte[]{
                OP_CONST, 1,
                OP_CONST, 0,
                OP_SUB,
                OP_HALT
        };

        assertEquals(-1L, vm.exec(bytecode, constants).obj());
    }

    @Test
    void givenNumbers_whenMul_thenReturnCorrectResult() {
        constants = List.of(Value.number(2), Value.number(3));
        bytecode = new byte[]{
                OP_CONST, 1,
                OP_CONST, 0,
                OP_MUL,
                OP_HALT
        };

        assertEquals(6L, vm.exec(bytecode, constants).obj());
    }

    @Test
    void givenNumbers_whenDiv_thenReturnCorrectResult() {
        constants = List.of(Value.number(6), Value.number(3));
        bytecode = new byte[]{
                OP_CONST, 1,
                OP_CONST, 0,
                OP_DIV,
                OP_HALT
        };

        assertEquals(2L, vm.exec(bytecode, constants).obj());
    }


    @Test
    void givenNumbers_whenComplexOps_thenReturnCorrectResult() {
        constants = List.of(
                Value.number(6), Value.number(3), Value.number(8)
        );
        bytecode = new byte[]{
                OP_CONST, 1, OP_CONST, 0, OP_DIV,
                OP_CONST, 2, OP_ADD,
                OP_HALT
        };

        assertEquals(10L, vm.exec(bytecode, constants).obj());
    }

    @Test
    void givenString_whenHalt_thenReturnCorrectResult() {
        constants = List.of(
                Value.string("hello")
        );
        bytecode = new byte[]{
                OP_CONST, 0,
                OP_HALT
        };

        assertEquals("hello", vm.exec(bytecode, constants).obj());
    }

    @Test
    void givenTwoString_whenConcat_thenReturnCorrectResult() {
        constants = List.of(
                Value.string("hello"),
                Value.string(" world!")
        );

        bytecode = new byte[]{
                OP_CONST, 0,
                OP_CONST, 1,
                OP_ADD,
                OP_HALT
        };

        assertEquals("hello world!", vm.exec(bytecode, constants).obj());
    }
}
