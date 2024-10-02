package org.starodubov;

import org.starodubov.vm.Parser;
import org.starodubov.vm.Vm;

import java.text.ParseException;

public class Main {
    public static void main(String[] args) throws ParseException {
        var parser = new Parser();
        Object helloWorld = parser.parse(
                """
                        (print "hello world")
                        """
        );

        /*
        var vm = new Vm();
        vm.exec("""
                
                42
                
                """);

         */
    }
}