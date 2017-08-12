package com.goatsandtigers.goatsandtigers;

import android.graphics.Point;

public class TigerMove extends Move {

    public TigerMove(int x1, int y1, int x2, int y2) {
        super(x1, y1, x2, y2);
    }

    public boolean isTakingMove() {
        return Math.abs(src.x - dest.x) > 1 || Math.abs(src.y - dest.y) > 1;
    }

    public Point getBoardCoordsOfGoatBeingTaken() {
        int x = (src.x + dest.x) / 2;
        int y = (src.y + dest.y) / 2;
        return new Point(x, y);
    }
}
