package com.example.cs911.game.state;

public class Board {
    public final static int MAX = 9;
    private PlayerType[] cells = new PlayerType[MAX];

    public Board(){
        for(int i=0;i<MAX;i++)
            cells[i] = PlayerType.FREE;
    }

    public void reset(){
        for(int i=0;i<MAX;i++)
            cells[i] = PlayerType.FREE;
    }

    public void move(PlayerType player, int move){
        cells[move] = player;
    }

    public PlayerType checkWin(){
        //top row
        if((cells[0] == cells[1]) && (cells[1] == cells[2])){
            return cells[0];}
        //middle row
        else if((cells[3] == cells[4]) && (cells[4] == cells[5])){return cells[3];}
        //bottom row
        else if((cells[6] == cells[7]) && (cells[7] == cells[8])){return cells[6];}
        //first col
        else if((cells[0] == cells[3]) && (cells[3] == cells[6])){return cells[0];}
        //second col
        else if((cells[1] == cells[4]) && (cells[4] == cells[7])){return cells[1];}
        //third col
        else if((cells[2] == cells[5]) && (cells[5] == cells[8])){return cells[2];}
        //L2R diag
        else if((cells[0] == cells[4]) && (cells[4] == cells[8])){return cells[0];}
        //R2L diag
        else if((cells[2] == cells[4]) && (cells[4] == cells[6])){return cells[2];}
        return PlayerType.FREE;
    }

    public boolean[] getEmptySquares(){
        boolean[] emptySquares = new boolean[cells.length];
        for(int i = 0; i< cells.length; i++){
            if(cells[i] == PlayerType.FREE){
                emptySquares[i] = true;
            }
            else
                emptySquares[i] = false;
        }
        return emptySquares;
    }
}
