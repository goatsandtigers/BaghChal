package com.goatsandtigers.goatsandtigers;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.goatsandtigers.goatsandtigers.exception.NoLegalGoatMovesException;

public class GoatAi {

    private SquareContents[][] board;
    private int numUnusedGoats;

    public GoatAi(SquareContents board[][], int numUnusedGoats) {
        this.board = board;
        this.numUnusedGoats = numUnusedGoats;
    }

    public GoatMove pickTigerMove(Context context) throws NoLegalGoatMovesException {
        if (isFirstMove()) {
            return pickOpeningMove(context);
        }
        return null;
    }

    private GoatMove pickOpeningMove(Context context) {
        int level = GameState.getLevel(context);
        if (level < 2) {
            return pickRandomOpeningMoveStupid();
        } else {
            return pickRandomOpeningMoveSensible();
        }
    }

    private GoatMove pickRandomOpeningMoveStupid() {
        int r = (int) (Math.random() * 5);
        switch (r) {
        case 0:
            return new GoatMove(2, 0);
        case 1:
            return new GoatMove(2, 4);
        case 2:
            return new GoatMove(0, 2);
        case 3:
            return new GoatMove(4, 2);
        default:
            return new GoatMove(2, 2);
        }
    }

    private GoatMove pickRandomOpeningMoveSensible() {
        int r = (int) (Math.random() * 5);
        switch (r) {
        case 0:
            return new GoatMove(2, 0);
        case 1:
            return new GoatMove(2, 4);
        case 2:
            return new GoatMove(0, 2);
        default:
            return new GoatMove(4, 2);
        }
    }

    private boolean isFirstMove() {
        return numUnusedGoats == 20;
    }
}
