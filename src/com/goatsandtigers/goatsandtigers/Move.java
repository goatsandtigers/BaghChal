package com.goatsandtigers.goatsandtigers;

import android.graphics.Point;

public class Move {

    public final Point src, dest;
    public final boolean isUnusedGoatBeingDropped;

    public Move(int x, int y) {
        src = null;
        dest = new Point(x, y);
        isUnusedGoatBeingDropped = true;
    }

    public Move(int x1, int y1, int x2, int y2) {
        src = new Point(x1, y1);
        dest = new Point(x2, y2);
        isUnusedGoatBeingDropped = false;
    }
}
