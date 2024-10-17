package org.starodubov.vm;

import org.starodubov.vm.value.CodeObj;
import org.starodubov.vm.value.Value;
import org.starodubov.vm.value.ValueTypes;

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
                            global.define(varName);

                            gen(exp.list.get(2));
                            emit(OpCodes.OP_SET_GLOBAL);
                            emit(global.getGlobalIdx(varName));

                            //todo(local vars)
                        }
                        case "set" -> {
                            // global vars
                            final var varName = exp.list.get(1).string;
                            final var varIdx = global.getGlobalIdx(varName);
                            if (varIdx == -1) {
                                throw new IllegalStateException("variable '%s' is not defined".formatted(varName));
                            }
                            gen(exp.list.get(2));
                            emit(OpCodes.OP_SET_GLOBAL);
                            emit(varIdx);

                            //todo(local vars)
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
                    if (!global.exist(exp.string)) {
                       throw new IllegalStateException("%s is not defined".formatted(exp.string));
                    }

                    emit(OpCodes.OP_GET_GLOBAL);
                    emit(global.getGlobalIdx(exp.string));
                }
            }
        }
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
