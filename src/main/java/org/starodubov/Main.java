package org.starodubov;

import org.starodubov.vm.Parser;
import org.starodubov.vm.Vm;

import java.text.ParseException;

public class Main {
    public static void main(String[] args) throws ParseException {
        var parser = new Parser();
        Object helloWorld = parser.parse(
                """
                        (+ "hello" 2)
                        """
        );

        var vm = new Vm();
        vm.exec("""
                
                42
                
                """);
    }
}