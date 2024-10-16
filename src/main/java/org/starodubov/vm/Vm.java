package org.starodubov.vm;

import org.starodubov.vm.value.CodeObj;
import org.starodubov.vm.value.Value;
import org.starodubov.vm.value.ValueTypes;

import java.util.ArrayList;
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
        if (sp <= 0) {
            throw new IllegalStateException("empty stack");
        }

        --sp;
        return stack[sp];
    }

    public Value exec(String program) {
        final CodeObj code = compile(program);
        disassembler.printDisassemble(code);
        return exec(code.bytecode(), code.constants());
    }

    public Value exec(List<Integer> bytecode, List<Value> constants) {
        this.code = bytecode;
        this.constants = constants;

        int execOp;
        for (; ; ) {
            execOp = readByte();
            switch (execOp) {
                case OP_HALT -> {
                    return pop();
                }
                case OP_CONST -> push(getConst());
                case OP_ADD -> {
                    final Value oper1 = pop(), oper2 = pop();
                    if (oper1.type() == ValueTypes.STRING && oper2.type() == ValueTypes.STRING) {
                        final String t = Value.asString(oper2) + Value.asString(oper1);
                        push(Value.string(t));
                    } else if (oper1.type() == ValueTypes.NUMBER && oper2.type() == ValueTypes.NUMBER) {
                        final long t = Value.asNumber(oper1) + Value.asNumber(oper2);
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
                case OP_COMPARE -> {
                    final int op = readByte();
                    final Value oper1 = pop(), oper2 = pop();
                    if (oper1.type() == ValueTypes.NUMBER && oper2.type() == ValueTypes.NUMBER) {
                        final long n1 = Value.asNumber(oper1), n2 = Value.asNumber(oper2);
                        push(compareOp(op, n1, n2));
                    } else if (oper1.type() == ValueTypes.BOOLEAN && oper2.type() == ValueTypes.BOOLEAN) {
                        final boolean n1 = Value.asBoolean(oper1), n2 = Value.asBoolean(oper2);
                        push(compareOp(op, n1, n2));
                    } else {
                        throw new IllegalStateException("only numbers can be compared. %s, %s, op: %s"
                                .formatted(oper1, oper2, op));
                    }
                }
                case OP_JMP_IF_FALSE -> {
                    final boolean cond = Value.asBoolean(pop());
                    final int addr = readByte();
                    if (!cond) {
                        ip = addr;
                    }
                }
                case OP_JMP -> ip = readByte();
                case OP_GET_GLOBAL -> {
                    final int globalIdx = readByte();
                    push(global.get(globalIdx).value);
                }
                case OP_SET_GLOBAL -> {
                    final int globalIdx = readByte();
                    final Value v = peek();
                    global.set(globalIdx, v);
                }
                case OP_POP -> pop();
                case OP_GET_LOCAL -> {
                    final int localIdx = readByte();
                    if (localIdx < 0 || localIdx >= stack.length) {
                        throw new ArrayIndexOutOfBoundsException("GET_LOCAL: local idx = %d".formatted(localIdx));
                    }

                    push(stack[bp + localIdx]);
                }

                case OP_SET_LOCAL -> {
                    final int localIdx = readByte();
                    if (localIdx < 0 || localIdx >= stack.length) {
                        throw new ArrayIndexOutOfBoundsException("SET_LOCAL: local idx = %d".formatted(localIdx));
                    }
                    stack[localIdx] = peek();
                }
                case OP_SCOPE_EXIT -> {
                    final int count = readByte();
                    stack[sp - 1 - count] = peek();
                    popN(count);
                }
                default -> throw new IllegalStateException("unknown instruction 0x%X".formatted(execOp));
            }

        }
    }
    void popN(int count) {
        if (sp - count <= 0) {
            throw new ArrayIndexOutOfBoundsException("popN: sp - count == %d".formatted(sp -count));
        }
        sp -= count;
    }

    Value peek() {
        if (sp <= 0) {
            throw new ArrayIndexOutOfBoundsException("stack is empty");
        }
        return stack[sp - 1];
    }

    Value peek(int offset) {
        if (sp <= 0) {
            throw new ArrayIndexOutOfBoundsException("stack. sp=%s,offset=%s".formatted(sp, offset));
        }
        return stack[sp - 1 - offset];
    }

    Value compareOp(int op, boolean n1, boolean n2) {
        return switch (op) {
            case Compiler.CMP_EQ_CODE -> Value.bool(n1 == n2);
            case Compiler.CMP_NOT_EQ_CODE -> Value.bool(n1 != n2);
            default -> throw new IllegalStateException("Unexpected compare value for booleans: " + op);
        };
    }

    Value compareOp(int op, long n1, long n2) {
        return switch (op) {
            case Compiler.CMP_LESS_CODE -> Value.bool(n1 > n2);
            case Compiler.CMP_GREAT_CODE -> Value.bool(n1 < n2);
            case Compiler.CMP_EQ_CODE -> Value.bool(n1 == n2);
            case Compiler.CMP_GREAT_OR_EQ_CODE -> Value.bool(n1 <= n2);
            case Compiler.CMP_EQ_OR_LESS_CODE -> Value.bool(n1 >= n2);
            case Compiler.CMP_NOT_EQ_CODE -> Value.bool(n1 != n2);
            default -> throw new IllegalStateException("Unexpected compare value for nums: " + op);
        };
    }

    CodeObj compile(String program) {
        try {
            final var ast = (Exp) parser.parse("(begin %s )".formatted(program));
            return compiler.compile(ast);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void mathOp(final BiFunction<Long, Long, Long> mathFun) {
        final long oper1 = Value.asNumber(pop()), oper2 = Value.asNumber(pop());
        final long result = mathFun.apply(oper1, oper2);
        push(Value.number(result));
    }

    int readByte() {
        return code.get(ip++);
    }

    Value getConst() {
        return constants.get(readByte());
    }

    void setGlobalVars(GlobalVar... globalVars) {
        global.addConst(globalVars);
    }

    public Vm() {
        parser = new Parser();
        global = new Global(new ArrayList<>());
        compiler = new Compiler(global);
        stack = new Value[STACK_LIMIT];
        disassembler = new Disassembler(global);
    }

    // stack pointer
    int sp = 0;

    //instruction pointer
    int ip = 0;

    // base pointer
    int bp = 0;

    // bytecode
    List<Integer> code;

    // constant pool
    List<Value> constants;

    static final int STACK_LIMIT = 512;

    final private Disassembler disassembler;

    final private Parser parser;

    final private Compiler compiler;

    final private Value[] stack;

    final private Global global;
}
