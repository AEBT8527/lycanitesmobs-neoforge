package com.lycanitesmobs.core.manager;

public class CommandManager {
    private static CommandManager INSTANCE;

    public static CommandManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CommandManager();
        }
        return INSTANCE;
    }


}
