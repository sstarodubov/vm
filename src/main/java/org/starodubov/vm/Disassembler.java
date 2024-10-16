package org.starodubov.vm;

import org.apache.commons.lang3.StringUtils;
import org.starodubov.vm.value.CodeObj;

import static org.starodubov.vm.OpCodes.*;

public class Disassembler {

    void disassemble(CodeObj co) {
        System.out.println(co);
        System.out.printf("---------------Disassembly: %s ----------------------%n", co.name());
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
            case OP_HALT, OP_ADD, OP_SUB, OP_DIV, OP_MUL -> disassembleSimple(co, opcode, offset);
            case OP_CONST -> disassembleConst(co, opcode, offset);
            default -> throw new IllegalStateException("Unexpected opcode: " + opcode);
        };
    }

    private int disassembleJmp(CodeObj co, int opcode, int offset) {
        final var sOffset = printOffset(offset);
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
        final var sOffset = printOffset(offset);
        final var sBytes = printBytes(co, offset, 2);
        final var sOpcode = OpCodes.opcodeToString(opcode);
        System.out.printf("%s%16s%27s (%s)\n", sOffset, sBytes, sOpcode,
                Compiler.compareToString(co.bytecode().get(offset + 1)));
        return offset + 2;
    }

    private int disassembleConst(CodeObj co, int opcode, int offset) {
        final var sOffset = printOffset(offset);
        final var sBytes = printBytes(co, offset, 2);
        final var sOpcode = OpCodes.opcodeToString(opcode);
        System.out.printf("%s%16s%25s (%s)\n", sOffset, sBytes, sOpcode,
                co.constants().get(co.bytecode().get(offset + 1)).obj());
        return offset + 2;
    }

    private int disassembleSimple(CodeObj co, int opcode, int offset) {
        final var sOffset = printOffset(offset);
        final var sBytes = printBytes(co, offset, 1);
        final var sOpcode = OpCodes.opcodeToString(opcode);
        System.out.println(align(sOffset, sBytes, sOpcode));
        return offset + 1;
    }

    private String printOffset(int offset) {
        return StringUtils.leftPad("%s".formatted(offset), 4, '0');
    }

    private String printBytes(CodeObj co, int offset, int count) {
        final var sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(StringUtils.rightPad("0x%X".formatted(co.bytecode().get(i + offset)), 4, '0'));
            if (i != count - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String printByteAndAddr(CodeObj co, int offset) {
        return StringUtils.rightPad("0x%X".formatted(co.bytecode().get(offset)), 4, '0')
                + " "
                + StringUtils.leftPad("%d".formatted(co.bytecode().get(offset + 1)), 4, '0');
    }
}
