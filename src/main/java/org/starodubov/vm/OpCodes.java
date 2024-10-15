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
}
