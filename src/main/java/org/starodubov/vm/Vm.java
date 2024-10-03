package org.starodubov.vm;

import java.util.List;
import java.util.function.BiFunction;

import static org.starodubov.vm.OpCodes.*;

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
        for (; ; ) {
            execOp = readByte();
            switch (execOp) {
                case OP_HALT -> {
                    return pop();
                }
                case OP_CONST -> push(getConst());
                case OP_ADD -> {
                    final Value oper1 = pop();
                    final Value oper2 = pop();
                    if (oper1.type() == ValueTypes.STRING && oper2.type() == ValueTypes.STRING) {
                        final String t = Value.as_string(oper2) + Value.as_string(oper1);
                        push(Value.string(t));
                    } else if (oper1.type() == ValueTypes.NUMBER && oper2.type() == ValueTypes.NUMBER) {
                        final long t = Value.as_number(oper1) + Value.as_number(oper2);
                        push(Value.number(t));
                    } else {
                        throw new IllegalStateException("cannot exec '%s' + '%s'".formatted(
                                oper1, oper2
                        ));
                    }
                }
                case OP_SUB -> mathOp((a, b) -> a - b);
                case OP_MUL -> mathOp((a, b) -> a * b);
                case OP_DIV -> mathOp((a, b) -> a / b);
                default -> throw new IllegalStateException("unknown instruction 0x%X".formatted(execOp));
            }
        }
    }

    void mathOp(final BiFunction<Long, Long, Long> mathFun) {
        final long oper1 = Value.as_number(pop());
        final long oper2 = Value.as_number(pop());
        final long result = mathFun.apply(oper1, oper2);
        push(Value.number(result));
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
