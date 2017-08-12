package com.goatsandtigers.goatsandtigers;

import android.graphics.Point;

public class BoardModel {

    public static boolean canMoveInDirection(Point src, Direction direction) {
        if (direction == null) {
            return false;
        }
        switch (direction) {
        case N:
            return src.y > 0;
        case E:
            return src.x < 4;
        case S:
            return src.y < 4;
        case W:
            return src.x > 0;
        case NE:
            return (src.x == 1 && src.y == 1) || (src.x == 2 && src.y == 2) || (src.x == 3 && src.y == 3) || (src.x == 0 && src.y == 4)
                    || (src.x == 1 && src.y == 3) || (src.x == 3 && src.y == 1) || (src.x == 0 && src.y == 2) || (src.x == 2 && src.y == 4);
        case NW:
            return (src.x == 1 && src.y == 3) || (src.x == 2 && src.y == 2) || (src.x == 3 && src.y == 1) || (src.x == 1 && src.y == 1)
                    || (src.x == 3 && src.y == 3) || (src.x == 4 && src.y == 4) || (src.x == 2 && src.y == 4) || (src.x == 4 && src.y == 2);
        case SE:
            return (src.x == 0 && src.y == 0) || (src.x == 1 && src.y == 1) || (src.x == 2 && src.y == 2) || (src.x == 3 && src.y == 3)
                    || (src.x == 0 && src.y == 2) || (src.x == 1 && src.y == 3) || (src.x == 2 && src.y == 0) || (src.x == 3 && src.y == 1);
        case SW:
            return (src.x == 4 && src.y == 0) || (src.x == 3 && src.y == 1) || (src.x == 2 && src.y == 2) || (src.x == 1 && src.y == 3)
                    || (src.x == 1 && src.y == 1) || (src.x == 2 && src.y == 0) || (src.x == 4 && src.y == 2) || (src.x == 3 && src.y == 3);
        default:
            return false;
        }
    }
}
