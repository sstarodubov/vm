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
                            if (exp.list.size() == 4 ) {
                                gen(exp.list.get(3));
                            }
                            final int endBranchAddr = getOffset();
                            pathJmpAddr(endAddr, endBranchAddr);
                        }
                    }
                }
            }
            case SYMBOL -> {
                if (exp.string.equals("true") || exp.string.equals("false")) {
                    emit(OpCodes.OP_CONST);
                    emit(booleanConstIdx(exp.string.equals("true")));
                } else {
                    //todo(handle variables)
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

    void emit(int code) {
        co.bytecode().add(code);
    }

    CodeObj co;

    public static final int CMP_GREAT_CODE = 100;
    public static final int CMP_LESS_CODE = 101;
    public static final int CMP_EQ_CODE = 102;
    public static final int CMP_EQ_OR_LESS_CODE = 103;
    public static final int CMP_GREAT_OR_EQ_CODE = 104;
    public static final int CMP_NOT_EQ_CODE = 105;
}
