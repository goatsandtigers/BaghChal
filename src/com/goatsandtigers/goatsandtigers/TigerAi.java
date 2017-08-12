package com.goatsandtigers.goatsandtigers;

import static com.goatsandtigers.goatsandtigers.RandomUtils.randomizeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Point;

import com.goatsandtigers.goatsandtigers.exception.NoLegalTigerMovesException;
import com.goatsandtigers.minimax.GoatsAndTigersMinimax;
import com.goatsandtigers.minimax.exception.NoLegalMovesException;

public class TigerAi {

    private SquareContents[][] board;
    private int numUnusedGoats;

    public TigerAi(SquareContents board[][], int numUnusedGoats) {
        this.board = board;
        this.numUnusedGoats = numUnusedGoats;
    }

    public TigerMove pickTigerMove(Context context) throws NoLegalTigerMovesException {
        switch (GameState.getLevel(context)) {
        case 1:
            return pickTigerMoveMinimax(2);
        case 2:
            return pickTigerMoveLevel2();
        case 3:
            return pickTigerMoveLevel3();
        case 4:
            return pickTigerMoveMinimax(10);
        case 5:
            return pickTigerMoveMinimax(6);
        default:
            return pickTigerMoveLevel6();
        }
    }

    private TigerMove pickTigerMoveMinimax(int maxSearchDepth) throws NoLegalTigerMovesException {
        TigerMove randomTakingMove = findRandomTakingMove();
        if (randomTakingMove != null) {
            return randomTakingMove;
        }

        try {
            GoatsAndTigersMinimax minimax = new GoatsAndTigersMinimax(board, numUnusedGoats);
            Move move = minimax.pickPerfectMove(maxSearchDepth);
            return new TigerMove(move.src.x, move.src.y, move.dest.x, move.dest.y);
        } catch (NoLegalMovesException e) {
            throw new NoLegalTigerMovesException();
        }
    }

    public TigerMove pickTigerMoveLevel2() throws NoLegalTigerMovesException {
        TigerMove randomTakingMove = findRandomTakingMove();
        if (randomTakingMove != null) {
            return randomTakingMove;
        }

        // Move towards center
        TigerMove moveTowardsCenter = moveTigerTowardsCenter();
        if (moveTowardsCenter != null) {
            return moveTowardsCenter;
        }

        throw new NoLegalTigerMovesException();
    }

    private TigerMove moveTigerTowardsCenter() {
        Map<Integer, TigerMove> moveToDistanceFromCenterMap = buildMoveToDistanceFromCenterMap();

        for (int i = 10; i >= -10; i--) {
            TigerMove bestTigerMove = moveToDistanceFromCenterMap.get(i);
            if (bestTigerMove != null) {
                return bestTigerMove;
            }
        }

        return null;
    }

    private Map<Integer, TigerMove> buildMoveToDistanceFromCenterMap() {
        Map<Integer, TigerMove> moveToDistanceMap = new HashMap<Integer, TigerMove>();

        for (Point tigerPos : getTigerPositionsInRandomOrder()) {
            int startingDistanceFromCenter = calculateDistanceFromCenter(tigerPos.x, tigerPos.y);
            for (Direction direction : Direction.randomizedDirections()) {
                if (!BoardModel.canMoveInDirection(tigerPos, direction)) {
                    continue;
                }
                int destX = tigerPos.x + direction.dx;
                int destY = tigerPos.y + direction.dy;
                if (board[destX][destY] != SquareContents.EMPTY) {
                    continue;
                }
                int newDistanceFromCenter = calculateDistanceFromCenter(destX, destY);
                int improvementInClosenessToCenter = startingDistanceFromCenter - newDistanceFromCenter;
                TigerMove tigerMove = new TigerMove(tigerPos.x, tigerPos.y, destX, destY);
                moveToDistanceMap.put(improvementInClosenessToCenter, tigerMove);
            }
        }
        return moveToDistanceMap;
    }

    private int calculateDistanceFromCenter(int x, int y) {
        return Math.abs(2 - x) + Math.abs(2 - y);
    }

    public TigerMove pickTigerMoveLevel3() throws NoLegalTigerMovesException {
        TigerMove randomTakingMove = findRandomTakingMove();
        if (randomTakingMove != null) {
            return randomTakingMove;
        }

        // Move towards edge
        TigerMove moveAwayFromCenter = moveTigerAwayFromCenter();
        if (moveAwayFromCenter != null) {
            return moveAwayFromCenter;
        }

        throw new NoLegalTigerMovesException();
    }

    private TigerMove moveTigerAwayFromCenter() {
        Map<Integer, TigerMove> moveToDistanceFromCenterMap = buildMoveToDistanceFromCenterMap();

        for (int i = -10; i <= 10; i++) {
            TigerMove bestTigerMove = moveToDistanceFromCenterMap.get(i);
            if (bestTigerMove != null) {
                return bestTigerMove;
            }
        }

        return null;
    }

    public TigerMove pickTigerMoveLevel6() throws NoLegalTigerMovesException {
        TigerMove randomTakingMove = findRandomTakingMove();
        if (randomTakingMove != null) {
            return randomTakingMove;
        }

        TigerMove randomNonTakingMove = findRandomNonTakingMove();
        if (randomNonTakingMove != null) {
            return randomNonTakingMove;
        }

        throw new NoLegalTigerMovesException();
    }

    private TigerMove findRandomNonTakingMove() {
        for (Point tigerPos : getTigerPositionsInRandomOrder()) {
            for (Direction direction : Direction.randomizedDirections()) {
                if (BoardModel.canMoveInDirection(tigerPos, direction)) {
                    int destX = tigerPos.x + direction.dx;
                    int destY = tigerPos.y + direction.dy;
                    try {
                        if (board[destX][destY] == SquareContents.EMPTY) {
                            return new TigerMove(tigerPos.x, tigerPos.y, destX, destY);
                        }
                    } catch (Exception e) {
                        System.out.println();
                    }
                }
            }
        }
        return null;
    }

    private TigerMove findRandomTakingMove() {
        for (Point tigerPos : getTigerPositionsInRandomOrder()) {
            for (Direction direction : Direction.randomizedDirections()) {
                if (neighbourContainsGoat(tigerPos, direction) && BoardModel.canMoveInDirection(tigerPos, direction)) {
                    int destX = tigerPos.x + (2 * direction.dx);
                    int destY = tigerPos.y + (2 * direction.dy);
                    if (isSquareInsideBoard(destX, destY) && board[destX][destY] == SquareContents.EMPTY) {
                        return new TigerMove(tigerPos.x, tigerPos.y, destX, destY);
                    }
                }
            }
        }
        return null;
    }

    private boolean neighbourContainsGoat(Point tigerPos, Direction direction) {
        int neighbourX = tigerPos.x + direction.dx;
        int neighbourY = tigerPos.y + direction.dy;
        return isSquareInsideBoard(neighbourX, neighbourY) && board[neighbourX][neighbourY] == SquareContents.GOAT;
    }

    private boolean isSquareInsideBoard(int x, int y) {
        return x >= 0 && x < 5 && y >= 0 && y < 5;
    }

    private List<Point> getTigerPositionsInRandomOrder() {
        List<Point> tigerPoints = new ArrayList<Point>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (board[i][j] == SquareContents.TIGER) {
                    tigerPoints.add(new Point(i, j));
                }
            }
        }
        return randomizeList(tigerPoints);
    }

}
