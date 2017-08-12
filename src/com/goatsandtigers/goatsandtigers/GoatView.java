package com.goatsandtigers.goatsandtigers;

import android.content.Context;
import android.view.ViewGroup;

public class GoatView extends Piece {

    private boolean isUsed;
    private Integer width;
    private Integer height;

    public GoatView(Context context, PieceDragListener pieceDragListener) {
        super(context, pieceDragListener);
        setImageResource(R.drawable.i1_buckeye);
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void markAsUsed() {
        isUsed = true;
    }

    @Override
    public boolean canBeMovedByUser() {
        return GameState.isUserGoats(getContext());
    }

    public boolean isTaken() {
        return getAlpha() == 0;
    }

    public void setTaken(boolean taken) {
        setAlpha(taken ? 0 : 1);
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        width = params.width;
        height = params.height;
        super.setLayoutParams(params);
    }

    /**
     * Hack for when width set by setLayoutParams() but not yet picked up by layout() method.
     * 
     * @return width
     */
    public int getCachedWidth() {
        return width != null ? width : getWidth();
    }

    /**
     * Hack for when height set by setLayoutParams() but not yet picked up by layout() method.
     * 
     * @return height
     */
    public int getCachedHeight() {
        return height != null ? height : getHeight();
    }
}
