package com.goatsandtigers.minimax;

import java.util.List;

import com.goatsandtigers.minimax.exception.NoLegalMovesException;

public abstract class Minimax<T> implements Cloneable {

    private static final int MAX_TURN = 1;
    private static final int MIN_TURN = -1;

    public static final int UNLIMITED_SEARCH_DEPTH = -1;
    public static final int MINI_HAS_WON = Integer.MAX_VALUE;
    public static final int STALE_MATE = 0;
    public static final int MAX_HAS_WON = Integer.MIN_VALUE;

    private int player = Minimax.MAX_TURN; // Must always be 1 or -1

    public final boolean isMiniTurn() {
        return player == Minimax.MIN_TURN;
    }

    public final T pickPerfectMove(int maxSearchDepth) throws NoLegalMovesException {
        if (maxSearchDepth <= 0) {
            throw new NoLegalMovesException();
        }

        List<T> moves = listAllLegalMoves();
        if (moves.isEmpty()) {
            throw new NoLegalMovesException();
        } else if (moves.size() == 1) {
            return moves.get(0);
        }

        int bestScore = player == Minimax.MAX_TURN ? Minimax.MINI_HAS_WON : Minimax.MAX_HAS_WON;
        T bestMove = null;

        for (T move : moves) {
            Minimax tempBoard = (Minimax) this.clone();
            tempBoard.doMove(move);
            int score =
                    tempBoard.evaluate(maxSearchDepth == Minimax.UNLIMITED_SEARCH_DEPTH ? Minimax.UNLIMITED_SEARCH_DEPTH : maxSearchDepth - 1,
                            new AlphaBeta());
            if (score * player < bestScore || bestMove == null) {
                bestScore = score * player;
                bestMove = move;
            }
        }
        return bestMove;
    }

    public final int evaluate(int maxSearchDepth, AlphaBeta alphaBeta) {
        int currentScore = getCurrentScore();
        if (currentScore == Minimax.MINI_HAS_WON || currentScore == Minimax.MAX_HAS_WON) {
            return currentScore;
        }
        List<T> moves = listAllLegalMoves();
        if (moves.isEmpty()) {
            return Minimax.STALE_MATE;
        }
        int bestScore = 0;
        for (T move : moves) {
            Minimax<T> tempBoard = (Minimax<T>) this.clone();
            tempBoard.doMove(move);
            int score;
            if (maxSearchDepth == 0) {
                score = tempBoard.getCurrentScore();
            } else {
                score =
                        tempBoard.evaluate(maxSearchDepth == Minimax.UNLIMITED_SEARCH_DEPTH ? Minimax.UNLIMITED_SEARCH_DEPTH : maxSearchDepth - 1,
                                alphaBeta);

                // Alpha-beta pruning
                if (player != Minimax.MIN_TURN) {
                    if (score < alphaBeta.alpha) {
                        return score;
                    } else if (score < alphaBeta.beta) {
                        alphaBeta.beta = score;
                    }
                } else {
                    if (score > alphaBeta.beta) {
                        return score;
                    } else if (score > alphaBeta.alpha) {
                        alphaBeta.alpha = score;
                    }
                }
            }
            if (score == Minimax.MINI_HAS_WON && player == -1) {
                return Minimax.MINI_HAS_WON;
            } else if (score == Minimax.MAX_HAS_WON && player == 1) {
                return Minimax.MAX_HAS_WON;
            }
            if (score * player > bestScore) {
                bestScore = score * player;
            }
        }
        return bestScore;
    }

    public Minimax<T> clone() {
        try {
            return (Minimax<T>) super.clone();
        } catch (Exception e) {
            return null;
        }
    }

    public abstract int getCurrentScore();

    public abstract List<T> listAllLegalMoves();

    public abstract void moveAction(T move);

    public final void doMove(T move) {
        moveAction(move);
        player *= -1;
    }

    private class AlphaBeta {
        int alpha = Minimax.MINI_HAS_WON;
        int beta = Minimax.MAX_HAS_WON;
    }
}