package org.starodubov.vm;

import org.apache.commons.lang3.StringUtils;
import org.starodubov.vm.utils.DebugArrayList;
import org.starodubov.vm.value.CodeObj;

import static org.starodubov.vm.OpCodes.*;

public class Disassembler {

    void printDisassemble(CodeObj co) {
        System.out.printf("---------------Disassembly: %s ----------------------%n", co.name());
        System.out.println("constants= " + co.constants());
        System.out.println(global);
        System.out.printf("%s%10s%30s\n", "offset", "bytes", "opcode");
        int offset = 0;
        while (offset < co.bytecode().size()) {
            offset = disassembleInstruction(co, offset);
        }
        System.out.println();
    }

    private int disassembleInstruction(CodeObj co, int offset) {
        final var opcode = co.bytecode().get(offset);
        return switch (opcode) {
            case OP_JMP_IF_FALSE, OP_JMP -> disassembleJmp(co, opcode, offset);
            case OP_COMPARE -> disassembleCompare(co, opcode, offset);
            case OP_HALT, OP_ADD, OP_SUB, OP_DIV, OP_MUL, OP_POP, OP_RETURN -> disassembleSimple(co, opcode, offset);
            case OP_CONST -> disassembleConst(co, opcode, offset);
            case OP_SET_GLOBAL, OP_GET_GLOBAL -> disassembleGlobal(co, opcode, offset);
            case OP_SET_LOCAL, OP_GET_LOCAL -> disassembleLocal(co, opcode, offset);
            case OP_SCOPE_EXIT, OP_CALL -> disassembleWord(co, opcode, offset);
            default -> throw new IllegalStateException("Unexpected opcode: " + opcode);
        };
    }

    private int disassembleLocal(CodeObj co, int opcode, int offset) {
        final var out = new StringBuilder(
                  align(offsetToStr(offset), bytesToStr(co, offset, 2), opcodeToString(opcode))
                );

        if (co.locals() instanceof DebugArrayList<LocalVar>) {
            out.append(" (%s)".formatted(co.locals().get(co.bytecode().get(offset + 1)).name()));
        }

        System.out.println(out);

        return offset + 2;
    }

    private int disassembleGlobal(CodeObj co, int opcode, int offset) {
        System.out.println(
                align(offsetToStr(offset), bytesToStr(co, offset, 2), opcodeToString(opcode)) +
                        " (%s)".formatted(global.get(co.bytecode().get(offset + 1)).name)
        );
        return offset + 2;
    }

    private int disassembleJmp(CodeObj co, int opcode, int offset) {
        final var sOffset = offsetToStr(offset);
        final var sBytes = printByteAndAddr(co, offset);
        final var sOpcode = OpCodes.opcodeToString(opcode);
        System.out.println(align(sOffset, sBytes, sOpcode));
        return offset + 2;
    }

    private String align(String sOffset, String sBytes, String sOpcode) {
        return StringUtils.rightPad(sOffset, 11, ' ')
                + StringUtils.rightPad(sBytes, 29, ' ')
                + sOpcode;
    }

    private int disassembleCompare(CodeObj co, int opcode, int offset) {
        final var sOffset = offsetToStr(offset);
        final var sBytes = bytesToStr(co, offset, 2);
        final var sOpcode = OpCodes.opcodeToString(opcode);
        System.out.println(
                align(sOffset, sBytes, sOpcode) +
                        " (%s)".formatted(Compiler.compareToString(co.bytecode().get(offset + 1)))

        );
        return offset + 2;
    }

    private int disassembleWord(CodeObj co, int opcode, int offset) {
        final var sOffset = offsetToStr(offset);
        final var sBytes = bytesToStr(co, offset, 2);
        final var sOpcode = OpCodes.opcodeToString(opcode);
        System.out.println(align(sOffset, sBytes, sOpcode));
        return offset + 2;
    }

    private int disassembleConst(CodeObj co, int opcode, int offset) {
        final var sOffset = offsetToStr(offset);
        final var sBytes = bytesToStr(co, offset, 2);
        final var sOpcode = OpCodes.opcodeToString(opcode);
        System.out.println(
                align(sOffset, sBytes, sOpcode) +
                        " (%s)".formatted(co.constants().get(co.bytecode().get(offset + 1)).obj()));
        return offset + 2;
    }

    private int disassembleSimple(CodeObj co, int opcode, int offset) {
        final var sOffset = offsetToStr(offset);
        final var sBytes = bytesToStr(co, offset, 1);
        final var sOpcode = OpCodes.opcodeToString(opcode);
        System.out.println(align(sOffset, sBytes, sOpcode));
        return offset + 1;
    }

    private String offsetToStr(int offset) {
        return "0x" + StringUtils.leftPad("%X".formatted(offset), 4, '0');
    }

    private String bytesToStr(CodeObj co, int offset, int count) {
        final var sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append("0x")
              .append(StringUtils.leftPad("%X".formatted(co.bytecode().get(i + offset)), 2, '0'));
            if (i != count - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String printByteAndAddr(CodeObj co, int offset) {
        return bytesToStr(co, offset, 1)
                + " "
                + offsetToStr(co.bytecode().get(offset + 1));
    }

    private final Global global;

    public Disassembler(Global global) {
        this.global = global;
    }
}
