package org.starodubov.vm;

import org.starodubov.vm.value.CodeObj;
import org.starodubov.vm.value.Value;
import org.starodubov.vm.value.ValueTypes;

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
                    }
                }
            }
        }
    }

    void genBinaryOp(Exp exp, int opCode) {
        gen(exp.list.get(1));
        gen(exp.list.get(2));
        emit(opCode);
    }

    int getConstIdx() {
        //TODO
        return  -1;
    }

    int numConstIdx(int num) {
        for (int i = 0; i < co.constants().size(); i++) {
            if (co.constants().get(i).type() != ValueTypes.NUMBER) {
                continue;
            }

            final long existed = Value.as_number(co.constants().get(i));
            if (existed == num) {
                return i;
            }
        }

        co.constants().add(Value.number(num));
        return co.constants().size() - 1;
    }

    int stringConstIdx(String s) {
        for (int i = 0; i < co.constants().size(); i++) {
            if (co.constants().get(i).type() != ValueTypes.NUMBER) {
                continue;
            }

            final String existed = Value.as_string(co.constants().get(i));
            if (existed.equals(s)) {
                return i;
            }
        }

        co.constants().add(Value.string(s));
        return co.constants().size() - 1;
    }

    void emit(int code) {
        co.bytecode().add(code);
    }

    CodeObj co;
}
