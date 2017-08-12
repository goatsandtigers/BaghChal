package com.goatsandtigers.minimax;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;

import com.goatsandtigers.goatsandtigers.BoardModel;
import com.goatsandtigers.goatsandtigers.Direction;
import com.goatsandtigers.goatsandtigers.Move;
import com.goatsandtigers.goatsandtigers.RandomUtils;
import com.goatsandtigers.goatsandtigers.SquareContents;

public class GoatsAndTigersMinimax extends Minimax<Move> {

    private SquareContents squareContents[][];
    private int numUnusedGoats;

    public GoatsAndTigersMinimax(SquareContents squareContents[][], int numUnusedGoats) {
        this.squareContents = squareContents;
        this.numUnusedGoats = numUnusedGoats;
    }

    @Override
    public int getCurrentScore() {
        return -((5 * getNumGoatsOnBoard()) + getNumGoatsThreatened());
    }

    private int getNumGoatsOnBoard() {
        int numGoatsOnBoard = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (squareContents[i][j] == SquareContents.GOAT)
                    numGoatsOnBoard++;
            }
        }
        return numGoatsOnBoard;
    }

    private int getNumGoatsThreatened() {
        int numGoatsThreatened = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (squareContents[i][j] != SquareContents.TIGER) {
                    continue;
                }
                for (Direction direction : Direction.values()) {
                    final int goatX = i + direction.dx;
                    final int goatY = j + direction.dy;
                    if (BoardModel.canMoveInDirection(new Point(i, j), direction) && squareContents[goatX][goatY] == SquareContents.GOAT
                            && BoardModel.canMoveInDirection(new Point(goatX, goatY), direction)
                            && squareContents[goatX + direction.dx][goatY + direction.dy] == SquareContents.EMPTY) {
                        numGoatsThreatened++;
                    }
                }
            }
        }
        return numGoatsThreatened;
    }

    @Override
    public List<Move> listAllLegalMoves() {
        List<Move> moves = isMiniTurn() ? listAllGoatMoves() : listAllTigerMoves();
        return RandomUtils.randomizeList(moves);
    }

    private List<Move> listAllGoatMoves() {
        return numUnusedGoats > 0 ? listAllGoatPhase1Moves() : listAllGoatPhase2Moves();
    }

    private List<Move> listAllGoatPhase1Moves() {
        List<Move> phase1GsoatMoves = new ArrayList<Move>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (squareContents[i][j] == SquareContents.EMPTY) {
                    phase1GsoatMoves.add(new Move(i, j));
                }
            }
        }
        return phase1GsoatMoves;
    }

    private List<Move> listAllGoatPhase2Moves() {
        List<Move> phase2GoatMoves = new ArrayList<Move>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (squareContents[i][j] != SquareContents.GOAT) {
                    continue;
                }
                Point src = new Point(i, j);
                for (Direction direction : Direction.values()) {
                    final int destX = i + direction.dx;
                    final int destY = j + direction.dy;
                    if (BoardModel.canMoveInDirection(src, direction) && squareContents[destX][destY] == SquareContents.EMPTY) {
                        phase2GoatMoves.add(new Move(src.x, src.y, destX, destY));
                    }
                }
            }
        }
        return phase2GoatMoves;
    }

    private List<Move> listAllTigerMoves() {
        List<Move> tigerMoves = new ArrayList<Move>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (squareContents[i][j] != SquareContents.TIGER) {
                    continue;
                }
                Point src = new Point(i, j);
                for (Direction direction : Direction.values()) {
                    final int destX = i + direction.dx;
                    final int destY = j + direction.dy;
                    if (BoardModel.canMoveInDirection(src, direction)) {
                        if (squareContents[destX][destY] == SquareContents.EMPTY) {
                            tigerMoves.add(new Move(src.x, src.y, destX, destY));
                        }
                        boolean canTake =
                                BoardModel.canMoveInDirection(new Point(src.x + direction.dx, src.y + direction.dy), direction)
                                        && squareContents[destX][destY] == SquareContents.GOAT
                                        && squareContents[destX + direction.dx][destY + direction.dy] == SquareContents.EMPTY;
                        if (canTake) {
                            tigerMoves.add(new Move(src.x, src.y, destX + direction.dx, destY + direction.dy));
                        }
                    }
                }
            }
        }
        return tigerMoves;
    }

    @Override
    public void moveAction(Move move) {
        if (move.isUnusedGoatBeingDropped) {
            squareContents[move.dest.x][move.dest.y] = SquareContents.GOAT;
        } else {
            if (squareContents[move.src.x][move.src.y] == SquareContents.TIGER) {
                boolean isTakingMove = Math.abs(move.src.x - move.dest.x) > 1 || Math.abs(move.src.y - move.dest.y) > 1;
                if (isTakingMove) {
                    int takenGoatX = (move.src.x + move.dest.x) / 2;
                    int takenGoatY = (move.src.y + move.dest.y) / 2;
                    squareContents[takenGoatX][takenGoatY] = SquareContents.EMPTY;
                }
            }

            squareContents[move.dest.x][move.dest.y] = squareContents[move.src.x][move.src.y];
            squareContents[move.src.x][move.src.y] = SquareContents.EMPTY;
        }
    }
}
