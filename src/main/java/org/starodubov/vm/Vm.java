package org.starodubov.vm;

import org.starodubov.vm.value.FunctionObj;
import org.starodubov.vm.value.Value;
import org.starodubov.vm.value.ValueTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
        compile(program); //main fn
        fn = compiler.getMainFn();
        ip = 0;
        sp = 0;
        bp = sp;
        compiler.disassemble();
        return exec();
    }

    public Value exec() {
        int execOp;
        for (; ; ) {
            execOp = readByte();
            switch (execOp) {
                case OP_HALT -> {
                    return pop();
                }
                case OP_CONST -> {
                    final Value v = getConst();
                    push(v);
                }
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
                case OP_SUB -> mathOp((a, b) -> b - a);
                case OP_MUL -> mathOp((a, b) -> a * b);
                case OP_DIV -> mathOp((a, b) -> b / a);
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
                case OP_CALL -> {
                    final int argsCount = readByte();
                    final Value fnValue = peek(argsCount);
                    if (ValueTypes.NATIVE == fnValue.type()) {
                        Value.asNative(fnValue).fn().run();
                        final var result = pop();
                        popN(argsCount + 1);
                        push(result);
                    } else {
                        final FunctionObj callee = Value.asFuntion(fnValue);
                        callStack.push(new Frame(ip, bp, fn));
                        fn = callee;
                        bp = sp - argsCount - 1;
                        // jump to the beginning of the function code
                        ip = 0;
                    }
                }
                case OP_RETURN -> {
                    final Frame callerFrame = callStack.pop();
                    ip = callerFrame.ra();
                    bp = callerFrame.bp();
                    fn = callerFrame.fn();
                }
                default -> throw new IllegalStateException("unknown instruction 0x%X".formatted(execOp));
            }

        }
    }
    void popN(int count) {
        if (sp - count < 0) {
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

    void compile(String program) {
        try {
            final var ast = (Exp) parser.parse("(begin %s )".formatted(program));
            compiler.compile(ast);
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
        return fn.co().bytecode().get(ip++);
    }

    Value getConst() {
        return fn.co().constants().get(readByte());
    }

    void setGlobalVars(GlobalVar... globalVars) {
        global.addConst(globalVars);
    }

    void addNativeFunction(final String name, Runnable fn, int arity) {
        global.addNativeFunction(name, fn, arity);
    }

    public Vm() {
        parser = new Parser();
        global = new Global(new ArrayList<>());
        compiler = new Compiler(global, new Disassembler(global));
        stack = new Value[STACK_LIMIT];

        addNativeFunction("println", () -> {
            final Value x = peek();
            System.out.println(x.obj());
            push(Value.VOID);
        }, 1);

        addNativeFunction("square", () -> {
            final long x = Value.asNumber(peek());
            push(Value.number(x * x));
        }, 1);

        addNativeFunction("print", () -> {
            final Value x = peek();
            System.out.print(x.obj());
            push(Value.VOID);
        }, 1);
    }

    // stack pointer
    int sp = 0;

    //instruction pointer
    int ip = 0;

    // base pointer
    int bp = 0;

    FunctionObj fn;

    static final int STACK_LIMIT = 512;

    final private Parser parser;

    final private Compiler compiler;

    final private Value[] stack;

    final private Global global;

    final private Deque<Frame> callStack = new ArrayDeque<>();
}
