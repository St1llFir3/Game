package com.example.cs911.game.state;

public class PlayerMove {
    PlayerType player;
    String name;
    int move;

    public PlayerMove(PlayerType player, int move){
        this.player = player;
        this.move = move;
        this.name = GameState.getThisGame().getMyName();
    }

    public int getMove(){
        return move;
    }

    public PlayerType getPlayer(){
        return player;
    }

    public void setPlayerName(String name){
        this.name=name;
    }

    public String getPlayerName(){
        return name;
    }
}
