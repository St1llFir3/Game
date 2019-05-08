package com.example.cs911.game.state;

public enum PlayerType {
    BAMBOO(0),
    PANDA(1),
    FREE(2),
    NO_WINNER(3);

    private final int id;

    PlayerType(int id) {
        this.id = id;
    }

    public int getValue() {
        return id;
    }
}
