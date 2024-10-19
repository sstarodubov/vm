package org.starodubov.vm;

import org.starodubov.vm.value.CodeObj;
import org.starodubov.vm.value.Value;
import org.starodubov.vm.value.ValueTypes;

import java.util.ArrayList;
import java.util.function.Function;

public class Compiler {

    public CodeObj compile(final Exp ast) {
        co = CodeObj.newCo("main");
        gen(ast);
        emit(OpCodes.OP_HALT);

        return co;
    }

    void gen(final Exp exp) {
        switch (exp.type) {
            case NUMBER -> {
                emit(OpCodes.OP_CONST);
                emit(numConstIdx(exp.number));
            }
            case STRING -> {
                emit(OpCodes.OP_CONST);
                emit(stringConstIdx(exp.string));
            }
            case LIST -> {
                final Exp tag = exp.list.getFirst();

                if (tag.type == ExpType.SYMBOL) {
                    final var op = tag.string;
                    switch (op) {
                        case "+" -> genBinaryOp(exp, OpCodes.OP_ADD);
                        case "-" -> genBinaryOp(exp, OpCodes.OP_SUB);
                        case "/" -> genBinaryOp(exp, OpCodes.OP_DIV);
                        case "*" -> genBinaryOp(exp, OpCodes.OP_MUL);
                        case "<" -> genCompareOp(exp, CMP_LESS_CODE);
                        case ">" -> genCompareOp(exp, CMP_GREAT_CODE);
                        case "==" -> genCompareOp(exp, CMP_EQ_CODE);
                        case ">=" -> genCompareOp(exp, CMP_GREAT_OR_EQ_CODE);
                        case "<=" -> genCompareOp(exp, CMP_EQ_OR_LESS_CODE);
                        case "!=" -> genCompareOp(exp, CMP_NOT_EQ_CODE);
                        // if <test> <consequent> <alternate>
                        case "if" -> {
                            // <test>
                            gen(exp.list.get(1));
                            emit(OpCodes.OP_JMP_IF_FALSE);
                            emit(/*address to patch*/0);

                            final int elseJmpAddr = getOffset() - 1;

                            // <consequent>
                            gen(exp.list.get(2));
                            emit(OpCodes.OP_JMP);
                            emit(/*address to patch*/0);
                            final int endAddr = getOffset() - 1;
                            final int elseBranchAddr = getOffset();
                            pathJmpAddr(elseJmpAddr, elseBranchAddr);

                            // <alternate>
                            if (exp.list.size() == 4) {
                                gen(exp.list.get(3));
                            }
                            final int endBranchAddr = getOffset();
                            pathJmpAddr(endAddr, endBranchAddr);
                        }
                        case "var" -> {

                            // global vars
                            final var varName = exp.list.get(1).string;
                            gen(exp.list.get(2));
                            if (isGlobalScope()) {
                                global.define(varName);
                                emit(OpCodes.OP_SET_GLOBAL);
                                emit(global.getGlobalIdx(varName));
                            } else {
                                co.addLocal(varName);
                                emit(OpCodes.OP_SET_LOCAL);
                                emit(co.getLocalIdx(varName));
                            }
                        }
                        case "set" -> {
                            final var varName = exp.list.get(1).string;
                            final var localIdx = co.getLocalIdx(varName);
                            if (localIdx != -1) {
                                emit(OpCodes.OP_SET_LOCAL);
                                emit(localIdx);
                            } else {
                                // global vars
                                final var varIdx = global.getGlobalIdx(varName);
                                if (varIdx == -1) {
                                    throw new IllegalStateException("variable '%s' is not defined".formatted(varName));
                                }
                                gen(exp.list.get(2));
                                emit(OpCodes.OP_SET_GLOBAL);
                                emit(varIdx);
                            }
                        }

                        case "begin" -> {
                            scopeEnter();
                            for (int i = 1; i < exp.list.size(); i++) {
                                boolean isLast = i == exp.list.size() - 1;
                                boolean isLocalDeclaration = isDeclaration(exp.list.get(i)) && !isGlobalScope();
                                gen(exp.list.get(i));
                                if (!isLast && !isLocalDeclaration) {
                                    emit(OpCodes.OP_POP);
                                }
                            }
                            scopeExit();
                        }
                        case "while" -> {
                            final int loopStartAddr = getOffset();
                            gen(exp.list.get(1));
                            emit(OpCodes.OP_JMP_IF_FALSE);
                            emit(0);
                            final int loopEndJmpAddr = getOffset() - 1;

                            gen(exp.list.get(2));

                            emit(OpCodes.OP_JMP);
                            emit(0);
                            pathJmpAddr(getOffset() - 1, loopStartAddr);

                            final int loopEndAdrr = getOffset() + 1;
                            pathJmpAddr(loopEndJmpAddr, loopEndAdrr);

                        }
                    }
                }
            }
            case SYMBOL -> {
                if (exp.string.equals("true") || exp.string.equals("false")) {
                    emit(OpCodes.OP_CONST);
                    emit(booleanConstIdx(exp.string.equals("true")));
                } else {
                    //variables
                    final var varName = exp.string;
                    final var localIdx = co.getLocalIdx(varName);
                    if (localIdx != -1) {
                        emit(OpCodes.OP_GET_LOCAL);
                        emit(localIdx);
                    } else {
                        if (!global.exist(varName)) {
                            throw new IllegalStateException("%s is not defined".formatted(varName));
                        }

                        emit(OpCodes.OP_GET_GLOBAL);
                        emit(global.getGlobalIdx(varName));
                    }
                }
            }
        }
    }

    private boolean isDeclaration(final Exp exp) {
        return isVarDeclaration(exp);
    }

    private boolean isVarDeclaration(final Exp exp) {
        return isTaggedList(exp, "var");
    }

    private boolean isTaggedList(final Exp exp, final String tag) {
        return exp.type == ExpType.LIST &&
                !exp.list.isEmpty() &&
                exp.list.getFirst().type == ExpType.SYMBOL &&
                exp.list.getFirst().string.equals(tag);
    }

    private boolean isGlobalScope() {
        return co.name().equals("main") && co.scopeLevel().value() == 1;
    }

    private void scopeExit() {
        final int varsCount = getVarsCountOnScopeExit();
        if (varsCount > 0) {
            emit(OpCodes.OP_SCOPE_EXIT);
            emit(varsCount);
        }
        co.scopeLevel().dec();
    }

    private int getVarsCountOnScopeExit() {
        int count = 0;

        while(!co.locals().isEmpty() && co.locals().getLast().scopeLevel() == co.scopeLevel().value()) {
           count++;
           co.locals().removeLast();
        }

        return count;
    }

    private void scopeEnter() {
        co.scopeLevel().inc();
    }

    void pathJmpAddr(int offset, int value) {
        writeByteAtOffset(offset, value);
    }

    void writeByteAtOffset(int offset, int value) {
        co.bytecode().set(offset, value);
    }

    int getOffset() {
        return co.bytecode().size();
    }

    void genCompareOp(Exp exp, int compareCode) {
        gen(exp.list.get(1));
        gen(exp.list.get(2));
        emit(OpCodes.OP_COMPARE);
        emit(compareCode);
    }

    void genBinaryOp(Exp exp, int opCode) {
        gen(exp.list.get(1));
        gen(exp.list.get(2));
        emit(opCode);
    }

    <T> int constIdx(ValueTypes type, Function<Value, T> asFn,
                     T val, Function<T, Value> convertFn) {
        for (int i = 0; i < co.constants().size(); i++) {
            if (co.constants().get(i).type() != type) {
                continue;
            }

            final T existed = asFn.apply(co.constants().get(i));
            if (existed.equals(val)) {
                return i;
            }
        }

        co.constants().add(convertFn.apply(val));
        return co.constants().size() - 1;
    }

    int booleanConstIdx(boolean val) {
        return constIdx(ValueTypes.BOOLEAN, Value::asBoolean, val, Value::bool);
    }

    int numConstIdx(long num) {
        return constIdx(ValueTypes.NUMBER, Value::asNumber, num, Value::number);
    }

    int stringConstIdx(String s) {
        return constIdx(ValueTypes.STRING, Value::asString, s, Value::string);
    }

    void emit(int byteVal) {
        co.bytecode().add(byteVal);
    }


    public static final int CMP_GREAT_CODE = 1;
    public static final int CMP_LESS_CODE = 2;
    public static final int CMP_EQ_CODE = 3;
    public static final int CMP_EQ_OR_LESS_CODE = 4;
    public static final int CMP_GREAT_OR_EQ_CODE = 5;
    public static final int CMP_NOT_EQ_CODE = 6;

    public static String compareToString(int compareCode) {
        return switch (compareCode) {
            case CMP_LESS_CODE -> "<";
            case CMP_GREAT_CODE -> ">";
            case CMP_EQ_CODE -> "==";
            case CMP_GREAT_OR_EQ_CODE -> ">=";
            case CMP_EQ_OR_LESS_CODE -> "<=";
            case CMP_NOT_EQ_CODE -> "!=";
            default -> "unknown";
        };
    }

    CodeObj co;
    Global global;

    public Compiler(Global global) {
        this.global = global;
    }
}
