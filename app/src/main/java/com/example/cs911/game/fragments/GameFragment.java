package com.example.cs911.game.fragments;

import android.app.Fragment;
import android.widget.ImageButton;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.example.cs911.game.R;
import com.example.cs911.game.state.Board;
import com.example.cs911.game.state.GameState;
import com.example.cs911.game.state.PlayerMove;
import com.example.cs911.game.state.PlayerState;
import com.example.cs911.game.state.PlayerType;
import com.google.gson.Gson;

public class GameFragment extends Fragment {
    private View mContentView = null;
    private  boolean isServer;
    private DeviceDetailFragment deviceDetailFragment;
    private static GameFragment gameFragment;
    private Gson gson;
    private PlayerState me;
    private static int[] playerImages = {R.drawable.pandahead,R.drawable.cross};


    private static int[] cells = {
            R.id.sq_1, R.id.sq_2, R.id.sq_3,
            R.id.sq_4, R.id.sq_5, R.id.sq_6,
            R.id.sq_7, R.id.sq_8, R.id.sq_9
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.game_board_frag, null);
        gson = new Gson();
        setupBtnClicks();
        gameFragment = this;
        GameState.getThisGame();
        return mContentView;
    }

    private void setupBtnClicks() {
        for(int i = 0; i< cells.length; i++) {
            mContentView.findViewById(cells[i]).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deviceDetailFragment = (DeviceDetailFragment)getFragmentManager().
                            findFragmentById(R.id.frag_detail);
                    ImageButton iBtn = (ImageButton) v;
                    //first time set player detail
                    if(me==null) {
                        me = new PlayerState(GameState.getThisGame().getMyName());
                        if (DeviceDetailFragment.isServer()) {
                            isServer=true;
                            me.setPlayerType(PlayerType.PANDA);
                            enableInterface(false);
                        }
                        else {
                            isServer=false;
                            me.setPlayerType(PlayerType.BAMBOO);
                            enableInterface(true);
                        }
                        GameState.getThisGame().setPlayerDetail(me.getPlayerType());
                    }
                    iBtn.setImageResource(playerImages[me.getPlayerType().getValue()]);
                    iBtn.setOnClickListener(null);
                    enableInterface(false);
                    for (int i = 0; i < Board.MAX; i++) {
                        if (cells[i] == iBtn.getId()) {
                            PlayerMove pMove = new PlayerMove(me.getPlayerType(), i);
                            GameState.getThisGame().processPlayerMove(pMove);
                            if(GameState.getThisGame().checkForWinner() != PlayerType.NO_WINNER) {
                                if(GameState.getThisGame().checkForWinner() == PlayerType.PANDA) {
                                    Toast.makeText(gameFragment.mContentView.getContext(), "Skull Won", Toast.LENGTH_LONG).show();
                                }
                                if(GameState.getThisGame().checkForWinner() == PlayerType.BAMBOO) {
                                    Toast.makeText(gameFragment.mContentView.getContext(), "Cross Won", Toast.LENGTH_LONG).show();
                                }
                                gameOverRoutine();
                            }
                            if(isServer)
                                deviceDetailFragment.sendServerMove(gson.toJson(pMove));
                            else
                                deviceDetailFragment.sendClientMove(gson.toJson(pMove));
                        }
                    }
                }
            });
        }
    }

    public static void handleOpponentMove(PlayerMove playerMove){
        Activity activity = gameFragment.getActivity();
        View view = gameFragment.mContentView;
        final ImageButton btn = (ImageButton) view.findViewById(cells[playerMove.getMove()]);
        final PlayerMove pMove = playerMove;
        GameState.getThisGame().processPlayerMove(pMove);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btn.setImageResource(playerImages[pMove.getPlayer().getValue()]);
                btn.setOnClickListener(null);
                if(GameState.getThisGame().checkForWinner() != PlayerType.NO_WINNER) {
                    if(GameState.getThisGame().checkForWinner() == PlayerType.PANDA) {
                        Toast.makeText(gameFragment.mContentView.getContext(), "Skull Won", Toast.LENGTH_LONG).show();
                    }
                    if(GameState.getThisGame().checkForWinner() == PlayerType.BAMBOO) {
                        Toast.makeText(gameFragment.mContentView.getContext(), "Cross Won", Toast.LENGTH_LONG).show();
                    }
                    gameOverRoutine();
                }
                else
                    enableInterface(true);
            }
        });
    }

    public static void enableInterface(boolean set){
        boolean[] enabledSquares = GameState.getThisGame().getEmptySquares();
        for(int i = 0; i< cells.length; i++)
            if(enabledSquares[i]){
                ImageButton iBtn = (ImageButton) gameFragment.mContentView.findViewById(cells[i]);
                iBtn.setEnabled(set);
            }
    }

    public static void gameOverRoutine() {
        GameState.getThisGame().newGame();
        resetBoard();
        enableInterface(!gameFragment.isServer);
    }

    private static void resetBoard(){
        for(int i = 0; i< cells.length; i++){
            ImageButton iBtn = (ImageButton) gameFragment.mContentView.findViewById(cells[i]);
            iBtn.setImageResource(R.drawable.empty);
        }
    }

    private void setPlayerNames(){

    }
}
