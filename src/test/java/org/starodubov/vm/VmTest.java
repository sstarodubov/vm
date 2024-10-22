package org.starodubov.vm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.starodubov.vm.value.Value;
import org.starodubov.vm.value.ValueTypes;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class VmTest {

    Vm vm;

    @BeforeEach
    void beforeEach() {
        vm = new Vm();
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

        assertEquals(-40L, exec.obj());
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


    @Test
    void blocksLocalVariablesWithScope() {
        var vm = new Vm();
        var result = vm.exec("""
                    (var x 5)
                    (set x (+ x 10))
                    x
                        (begin
                            (var x 100)
                            (begin
                                (var x 200)
                            x)
                        x)
                    x
                """);
        assertEquals(15L, result.obj());
    }


    @Test
    void blocksLocalVariablesWithScope2() {
        var vm = new Vm();
        var result = vm.exec("""
                    (var x 5)
                    (set x (+ x 10))
                    x
                        (begin
                            (set x 1000)
                            (var x 100)
                            (begin
                                (var x 200)
                            x)
                        x)
                    x
                """);
        assertEquals(1000L, result.obj());
    }


    @Test
    void exec() {
        var vm = new Vm();
        var result = vm.exec("""
                (var count 0)
                (begin
                    (set count (+ count 1))
                    (set count (+ count 1))
                    (set count (+ count 1))
                    (set count (+ count 1))
                    (set count (+ count 1))
                )
                count""");

        assertEquals(5L, result.obj());
    }

    @Test
    void whileLoop() {
        var vm = new Vm();
        var result = vm.exec("""
                 (var i 10)
                 (var count 0)
                 (while (> i 0)
                     (begin
                         (set i (- i 1))
                         (set count (+ count 1))
                     )
                 )
                count
                """);

        assertEquals(10L, result.obj());
    }

    @Test
    void forLoop() {
        var vm = new Vm();

        var result = vm.exec("""
                (var count 0)
                (for (var i 0) (< i 10) (set i (+ i 1))
                     (begin
                         (set count (+ count 2))
                     )
                )
                count
                """);
        assertEquals(20L, result.obj());
    }

    @Test
    void nativeFunctions() {
        var vm = new Vm();

        var result = vm.exec("""
                (var x 2)
                (square x)
                """);

        assertEquals(4L, result.obj());
    }

    @Test
    void nativeFunctions_print() {
        var vm = new Vm();

        var result = vm.exec("""
                (var x 2)
                (begin
                    (var x 10)
                    (println x)
                )
                (println x)
                """);

        assertEquals(ValueTypes.VOID, result.type());
    }

    @Test
    void simpleUserDefinedFunction() {
        var vm = new Vm();
        var result = vm.exec("""
                (def square2 (x) (* x x))
                (square2 3)
                """);

        assertEquals(9L, result.obj());
    }

    @Test
    void simple2UserDefinedFunction() {
        var vm = new Vm();
        var result = vm.exec("""
                (def sum (a b)
                    (begin
                        (var x 10)
                        (+ x (+ a b))
                    )
                )
                (sum 1 2)
                """);

        assertEquals(13L, result.obj());
    }

    @Test
    void userDefinedFunctions() {
        var vm = new Vm();
        var result = vm.exec("""
                (def factorial (x)
                     (if (== 1 x)
                         1
                         (* x (factorial (- x 1)))
                     )
                )
                (factorial 5)
                """);

        assertEquals(120L, result.obj());
    }
}
