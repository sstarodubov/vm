package org.starodubov.vm;

import java.util.List;

import static org.starodubov.vm.OpCodes.OP_CONST;
import static org.starodubov.vm.OpCodes.OP_HALT;

//4 number introduction

public class Vm {

    void push(Value v) {
        if (sp >= STACK_LIMIT) {
            throw new StackOverflowError();
        }

        stack[sp] = v;
        sp++;
    }

    Value pop() {
        if (sp < 0) {
            throw new IllegalStateException("empty stack");
        }

        --sp;
        return stack[sp];
    }

    public Value exec(String program) {
        // var ast = parser.parse(program);
        // var code,constants = compiler.compile(ast);
        return exec(code, constants);
    }

    public Value exec(byte[] bytecode, List<Value> constants) {
        this.code = bytecode;
        this.constants = constants;

        byte execOp;
        for (;;) {
            execOp = readByte();
            switch (execOp) {
                case OP_HALT -> {
                    return pop();
                }
                case OP_CONST -> push(getConst());
                default ->
                        throw new IllegalStateException("unknown instruction 0x%X".formatted(execOp));
            }
        }
    }

    byte readByte() {
       return code[ip++];
    }

    Value getConst() {
        return constants.get(readByte());
    }

    // stack pointer
    int sp = 0;

    //instruction pointer
    int ip = 0;

    // bytecode
    byte[] code;

    // constant pool
    List<Value> constants;

    static final int STACK_LIMIT = 512;

    // stack
    final Value[] stack = new Value[STACK_LIMIT];
}
