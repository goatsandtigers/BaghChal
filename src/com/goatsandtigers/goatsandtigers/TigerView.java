package com.goatsandtigers.goatsandtigers;

import android.content.Context;

public class TigerView extends Piece {

    public TigerView(Context context, PieceDragListener pieceDragListener) {
        super(context, pieceDragListener);
        refreshImage(context);
    }

    public void refreshImage(Context context) {
        setImageResource(GameState.getTigerImageRes(context));
    }

    @Override
    public boolean canBeMovedByUser() {
        return !GameState.isUserGoats(getContext());
    }

}
