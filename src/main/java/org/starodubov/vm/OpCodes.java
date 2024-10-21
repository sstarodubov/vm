package org.starodubov.vm;

public class OpCodes {

    public static final int OP_HALT = 0x00;
    public static final int OP_CONST = 0x01;
    public static final int OP_ADD = 0x02;
    public static final int OP_SUB = 0x03;
    public static final int OP_MUL = 0x04;
    public static final int OP_DIV = 0x05;

    public static final int OP_COMPARE = 0x06;
    public static final int OP_JMP_IF_FALSE = 0x07;
    public static final int OP_JMP = 0x08;

    public static final int OP_GET_GLOBAL = 0x09;
    public static final int OP_SET_GLOBAL = 0x10;
    public static final int OP_POP = 0x11;
    public static final int OP_GET_LOCAL = 0x12;
    public static final int OP_SET_LOCAL = 0x13;

    public static final int OP_SCOPE_EXIT = 0x14;
    public static final int OP_CALL = 0x15;
    public static final int OP_RETURN = 0xA;

    public static String opcodeToString(int opcode) {
        return switch (opcode) {
            case 0x00 -> "HALT";
            case 0x01 -> "CONST";
            case 0x02 -> "ADD";
            case 0x03 -> "SUB";
            case 0x04 -> "MUL";
            case 0x05 -> "DIV";
            case 0x06 -> "COMPARE";
            case 0x07 -> "JMP_IF_FALSE";
            case 0x08 -> "JMP";
            case 0x09 -> "GET_GLOBAL";
            case 0x10 -> "SET_GLOBAL";
            case 0x11 -> "POP";
            case 0x12 -> "GET_LOCAL";
            case 0x13 -> "SET_LOCAL";
            case 0x14 -> "SCOPE_EXIT";
            case 0x15 -> "CALL";
            case 0xA -> "RETURN";
            default -> "UNKNOWN";
        };
    }
}
