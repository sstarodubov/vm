package org.starodubov.vm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.starodubov.vm.value.Value;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.starodubov.vm.OpCodes.*;

public class VmTest {

    Vm vm;
    List<Value> constants;
    List<Integer> bytecode;

    @BeforeEach
    void beforeEach() {
        vm = new Vm();
        constants = new ArrayList<>();
    }

    @Test
    void givenNumber_whenExec_thenReturnSuccess() {
        constants = List.of(Value.number(45), Value.number(99));
        bytecode = List.of(
                OP_CONST, 1,
                OP_HALT
        );

        assertEquals(99, vm.exec(bytecode, constants).obj());
    }

    @Test
    void givenNumbers_whenAdd_thenReturnCorrectResult() {
        constants = List.of(Value.number(2), Value.number(3));
        bytecode = List.of(
                OP_CONST, 1,
                OP_CONST, 0,
                OP_ADD,
                OP_HALT
        );

        assertEquals(5L, vm.exec(bytecode, constants).obj());
    }

    @Test
    void givenNumbers_whenSub_thenReturnCorrectResult() {
        constants = List.of(Value.number(2), Value.number(3));
        bytecode = List.of(
                OP_CONST, 1,
                OP_CONST, 0,
                OP_SUB,
                OP_HALT
        );

        assertEquals(-1L, vm.exec(bytecode, constants).obj());
    }

    @Test
    void givenNumbers_whenMul_thenReturnCorrectResult() {
        constants = List.of(Value.number(2), Value.number(3));
        bytecode = List.of(
                OP_CONST, 1,
                OP_CONST, 0,
                OP_MUL,
                OP_HALT
        );

        assertEquals(6L, vm.exec(bytecode, constants).obj());
    }

    @Test
    void givenNumbers_whenDiv_thenReturnCorrectResult() {
        constants = List.of(Value.number(6), Value.number(3));
        bytecode = List.of(
                OP_CONST, 1,
                OP_CONST, 0,
                OP_DIV,
                OP_HALT
        );

        assertEquals(2L, vm.exec(bytecode, constants).obj());
    }


    @Test
    void givenNumbers_whenComplexOps_thenReturnCorrectResult() {
        constants = List.of(
                Value.number(6), Value.number(3), Value.number(8)
        );
        bytecode = List.of(
                OP_CONST, 1, OP_CONST, 0, OP_DIV,
                OP_CONST, 2, OP_ADD,
                OP_HALT
        );

        assertEquals(10L, vm.exec(bytecode, constants).obj());
    }

    @Test
    void givenString_whenHalt_thenReturnCorrectResult() {
        constants = List.of(
                Value.string("hello")
        );
        bytecode = List.of(
                OP_CONST, 0,
                OP_HALT
        );

        assertEquals("hello", vm.exec(bytecode, constants).obj());
    }

    @Test
    void givenTwoString_whenConcat_thenReturnCorrectResult() {
        constants = List.of(
                Value.string("hello"),
                Value.string(" world!")
        );

        bytecode = List.of(
                OP_CONST, 0,
                OP_CONST, 1,
                OP_ADD,
                OP_HALT
        );

        assertEquals("hello world!", vm.exec(bytecode, constants).obj());
    }

    @Test
    void compilerBytecode_nums() {
        Value exec = vm.exec("""
                5
                """);

        assertEquals(5L, exec.obj());
    }

    @Test
    void compilerBytecode_string() {
        Value exec = vm.exec("""
                "hello world"
                """);

        assertEquals("hello world", exec.obj());
    }

    @Test
    void complexExpressions() {
        Value exec = vm.exec("""
                (+ 10 (+ 40 1))
                """);

        assertEquals(51L, exec.obj());
    }


    @Test
    void complexExpressions_sub() {
        Value exec = vm.exec("""
                (- 1 (+ 40 1))
                """);

        assertEquals(40L, exec.obj());
    }


    @Test
    void complexExpressions_concat() {
        Value exec = vm.exec("""
                (
                    + "hello" " world"
                )
                
                """);

        assertEquals("hello world", exec.obj());
    }

    @Test
    void complexExpressions_correctConstantPool() {
        vm.exec("""
                (
                  +  1  1 
                )
                
                """);

        assertEquals(1, vm.constants.size());
        assertEquals(1L, vm.constants.getFirst().obj());
    }


    @Test
    void complexExpressions_correctConstantPoolWithString() {
        vm.exec("""
                (
                  +  "hello"  "hello" 
                )
                
                """);

        assertEquals(1, vm.constants.size());
        assertEquals("hello", vm.constants.getFirst().obj());
    }

    @Test
    void comparison_booleans() {
        Value result = vm.exec("""
                  true
                
                """);

        assertEquals(true, result.obj());
    }

    @Test
    void comparison_booleansFalse() {
        Value result = vm.exec("""
                 false 
                
                """);

        assertEquals(false, result.obj());
    }

    @Test
    void comparison_booleansCmpOp() {
        Value result = vm.exec("""
                 (> 5 10)
                """);

        assertEquals(false, result.obj());
    }


    @Test
    void comparison_booleansCmpOp1() {
        Value result = vm.exec("""
                 (> 11 10)
                """);

        assertEquals(true, result.obj());
    }

    @Test
    void comparison_booleansCmpOp2() {
        Value result = vm.exec("""
                 (>= 10 10)
                """);

        assertEquals(true, result.obj());
    }


    @Test
    void comparison_booleansCmpOp3() {
        Value result = vm.exec("""
                 (<= 10 10)
                """);

        assertEquals(true, result.obj());
    }


    @Test
    void comparison_booleansCmpOp4() {
        Value result = vm.exec("""
                 (< 10 10)
                """);

        assertEquals(false, result.obj());
    }

    @Test
    void comparison_booleansCmpOp5() {
        Value result = vm.exec("""
                 (> 10 10)
                """);

        assertEquals(false, result.obj());
    }

    @Test
    void comparison_booleansCmpOp6() {
        Value result = vm.exec("""
                 (== 10 10)
                """);

        assertEquals(true, result.obj());
    }


    @Test
    void comparison_booleansCmpOp7() {
        Value result = vm.exec("""
                 (!= 10 10)
                """);

        assertEquals(false, result.obj());
    }

    @Test
    void comparison_booleansCmpOp8() {
        Value result = vm.exec("""
                 (== true true)
                """);

        assertEquals(true, result.obj());
    }

    @Test
    void comparison_booleansCmpOp9() {
        Value result = vm.exec("""
                 (== false true)
                """);

        assertEquals(false, result.obj());
    }

    @Test
    void comparison_booleansCmpOp10() {
        Value result = vm.exec("""
                (== false  (> 10 1))
                """);

        assertEquals(false, result.obj());
    }

    @Test
    void comparison_booleansCmpOp11() {
        Value result = vm.exec("""
                (>= 101 1)
                """);

        assertEquals(true, result.obj());
    }

    @Test
    void comparison_booleansCmpOp12() {
        Value result = vm.exec("""
                (<= 101 1)
                """);

        assertEquals(false, result.obj());
    }

    @Test
    void controlFlowBranchInstruction() {
        Value result = vm.exec("""
                (if  (== 5 10) 1 2)
                """);

        assertEquals(2L, result.obj());
    }

    @Test
    void controlFlowBranchInstruction0() {
        Value result = vm.exec("""
                (if  (!= 5 10) 1 2)
                """);

        assertEquals(1L, result.obj());
    }

    @Test
    void controlFlowBranchInstruction1() {
        Value result = vm.exec("""
                (if  (> 5 10) true false)
                """);

        assertEquals(false, result.obj());
    }

    @Test
    void controlFlowBranchInstruction2() {
        Value result = vm.exec("""
                (if  (< 5 10) true false)
                """);

        assertEquals(true, result.obj());
    }

    @Test
    void controlFlowBranchInstruction3() {
        Value result = vm.exec("""
                (if  (== 5 10)  
                        ( + 1 2 ) 
                        ( + 3 4 )
                )
                """);

        assertEquals(7L, result.obj());
    }

    @Test
    void globalVariables() {
        var vm = new Vm();
        vm.setGlobalVars(new GlobalVar("x", Value.number(100)));

        var result = vm.exec("""
                x
                """);

        assertEquals(100, result.obj());
    }

    @Test
    void globalVariablesStr() {
        var vm = new Vm();
        vm.setGlobalVars(new GlobalVar("x", Value.string("hello")));

        var result = vm.exec("""
                x
                """);

        assertEquals("hello", result.obj());
    }

    @Test
    void defineGlobalVariables() {
        var vm = new Vm();
        vm.setGlobalVars(new GlobalVar("x", Value.number(100)));
        var result = vm.exec("""
                (var z (+ x 3))
                """);

        assertEquals(103L, result.obj());
    }

    @Test
    void setGlobalVariables() {
        var vm = new Vm();
        vm.setGlobalVars(new GlobalVar("x", Value.number(100)));
        var result = vm.exec("""
                (set x (+ x 3))
                """);

        assertEquals(103L, result.obj());
    }

    @Test
    void blocksLocalVariables() {
        var vm = new Vm();
        var result = vm.exec("""
                    (var x 5)
                    (set x (+ x 10))
                    x
                """);
        assertEquals(15L, result.obj());
    }
}
