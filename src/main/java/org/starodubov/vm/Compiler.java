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

    int numConstIdx(long num) {
        return constIdx(ValueTypes.NUMBER, Value::as_number, num, Value::number);
    }

    int stringConstIdx(String s) {
        return constIdx(ValueTypes.STRING, Value::as_string, s, Value::string);
    }

    void emit(int code) {
        co.bytecode().add(code);
    }

    CodeObj co;
}
