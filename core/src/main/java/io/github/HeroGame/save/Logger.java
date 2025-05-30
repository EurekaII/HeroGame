package io.github.HeroGame.save;

public class Logger {
    void info(String msg) { System.out.println("INFO: " + msg); }
    void error(String msg) { System.err.println("ERROR: " + msg); }
    void error(String msg, Exception e) { System.err.println("ERROR: " + msg + " - " + e.getMessage()); }
}
