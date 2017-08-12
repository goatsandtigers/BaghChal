package com.goatsandtigers.goatsandtigers;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.ImageView;

public abstract class Piece extends ImageView {

    private PieceDragListener pieceDragListener;

    public Piece(Context context, PieceDragListener pieceDragListener) {
        super(context);
        this.pieceDragListener = pieceDragListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!canBeMovedByUser()) {
            return true;
        }
        switch (event.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
            pieceDragListener.dragStart(this, getX() - event.getRawX(), getY() - event.getRawY());
            return true;
        }
        return super.onTouchEvent(event);
    }

    public abstract boolean canBeMovedByUser();
}
