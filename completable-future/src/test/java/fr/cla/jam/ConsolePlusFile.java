package fr.cla.jam;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class ConsolePlusFile extends PrintStream {

    public ConsolePlusFile(String fileName) throws FileNotFoundException {
        super(fileName);
    }
    
    @Override public PrintStream printf(String format, Object ... args) {
        System.out.printf(format, args);
        return super.printf(format, args);
    }
    
}
