package com.isc.console;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConsoleApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ConsoleApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        DefaultTerminalFactory terminalFactory =
                new DefaultTerminalFactory()
                        .setPreferTerminalEmulator(false);

        var terminal = terminalFactory.createTerminal();

        terminal.putCharacter('H');
        terminal.putCharacter('e');
        terminal.putCharacter('l');
        terminal.putCharacter('l');
        terminal.putCharacter('o');
        terminal.flush();

        Thread.sleep(10000);

    }
}