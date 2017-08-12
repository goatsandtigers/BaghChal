package com.goatsandtigers.goatsandtigers;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

import java.util.ArrayList;
import java.util.List;

import com.goatsandtigers.goatsandtigers.exception.InvalidSquareException;
import com.goatsandtigers.goatsandtigers.exception.NoLegalGoatMovesException;
import com.goatsandtigers.goatsandtigers.exception.NoLegalTigerMovesException;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

public class GoatsAndTigersActivity extends Activity implements PieceDragListener, AnimatorListener {

    private static final int NUM_GOATS = 20;
    private static final int NUM_TIGERS = 4;
    private static final String MSG_ILLEGAL_USED_GOAT_MOVE =
            "Illegal move.\n\nPlease drop the goat into an adjacent empty square which follows the lines on the board.";
    private static final String MSG_ILLEGAL_UNUSED_GOAT_MOVE = "Illegal move.\n\nPlease drop the goat into an unused square.";
    private static final String MSG_NOT_ALL_GOATS_DROPPED_ON_BOARD =
            "Illegal move.\n\nPlese drop all goats onto the board before moving goats that are already on the board.";
    private static final long PIECE_MOVE_DURATION = 500;

    private GoatView goats[];
    private TigerView tigers[];
    private BoardView board;
    private Piece draggedPiece;
    private Point draggedPieceDragStartPos;
    private Point draggedPieceReturnLocation;
    private Point screenSize;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reset();
    }

    private void reset() {
        setContentView(createOuterView());
    }

    private View createOuterView() {
        RelativeLayout outerLayout = new RelativeLayout(this) {
            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                GoatsAndTigersActivity.this.layout(changed, l, t, r, b);
                super.onLayout(changed, l, t, r, b);
            }
        };
        createBoard(outerLayout);
        createGoats(outerLayout);
        createTigers(outerLayout);
        return outerLayout;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.goats_and_tigers, menu);
        boolean userIsGoats = GameState.isUserGoats(this);
        menu.findItem(R.id.action_play_as_goats).setChecked(userIsGoats);
        menu.findItem(R.id.action_play_as_tigers).setChecked(!userIsGoats);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_restart) {
            confirmRestart();
            return true;
        } else if (id == R.id.action_rules) {
            showRules();
            return true;
        } else if (id == R.id.action_play_as_goats) {
            confirmChangePlayerTeam("Goats", true);
        } else if (id == R.id.action_play_as_tigers) {
            confirmChangePlayerTeam("Tigers", false);
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmChangePlayerTeam(String pieceName, final boolean userIsGoats) {
        String msg = "Start a new game as " + pieceName + " now?";
        new AlertDialog.Builder(this).setTitle("Goats and Tigers").setMessage(msg).setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                GameState.storePlayAsGoats(GoatsAndTigersActivity.this, userIsGoats);
                menu.findItem(R.id.action_play_as_goats).setChecked(userIsGoats);
                menu.findItem(R.id.action_play_as_tigers).setChecked(!userIsGoats);
                restart();
            }

        }).setNegativeButton("No", null).create().show();
    }

    private void showRules() {
        String msg =
                "Pieces are placed at intersections of the lines on the board. The four tigers start in the corners of the board.\n\nGoats are dropped into empty squares one at a time. Once all goats have been dropped onto the board they may then be moved to adjacent squares following the lines of the board.\n\nTigers win by taking the goat pieces by jumping over them and landing in an empty square just as in Draughts/Checkers.\n\nGoats win by surrounding the tigers so that they cannot move.";
        new AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage(msg).setPositiveButton(android.R.string.yes, null)
                .setIcon(R.drawable.i1_buckeye).show();
    }

    private void confirmRestart() {
        String msg = "Really start again from the first level?";
        new AlertDialog.Builder(this).setTitle("Goats and Tigers").setMessage(msg).setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                restart();
            }

        }).setNegativeButton("No", null).create().show();
    }

    private void restart() {
        GameState.restart(this);
        reset();
        // TODO if allowed to play as tigers then reload the goat images for the current level
        refreshTigerImages();
        layout(false, 0, 0, screenSize.x, screenSize.y);
        if (!GameState.isUserGoats(this)) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            makeGoatMove();
        }
    }

    private void makeGoatMove() {
        try {
            GoatMove goatMove = new GoatAi(buildBoardModel(), getNumUnusedGoats()).pickTigerMove(this);
            GoatView goatToMove = findGoatForMove(goatMove);
            Point newGoatPos = board.getScreenCoordsForSquare(goatMove.dest);
            float halfPieceWidth = goatToMove.getCachedWidth() / 2f;
            goatToMove.animate().x(newGoatPos.x - halfPieceWidth).y(newGoatPos.y - halfPieceWidth).setDuration(PIECE_MOVE_DURATION).setListener(this);
        } catch (NoLegalGoatMovesException e) {
            goToNextLevel();
        }
    }

    private GoatView findGoatForMove(GoatMove goatMove) {
        if (goatMove.isUnusedGoatBeingDropped) {
            return goats[getNumUnusedGoats() - 1];
        } else {
            return getGoatAtSquare(goatMove.src.x, goatMove.src.y);
        }
    }

    private void refreshTigerImages() {
        for (int i = 0; i < NUM_TIGERS; i++) {
            tigers[i].refreshImage(this);
        }
    }

    private void createGoats(RelativeLayout outerLayout) {
        goats = new GoatView[NUM_GOATS];
        for (int i = 0; i < NUM_GOATS; i++) {
            goats[i] = createGoatView();
            outerLayout.addView(goats[i]);
        }
    }

    private GoatView createGoatView() {
        return new GoatView(this, this);
    }

    private void createTigers(RelativeLayout outerLayout) {
        tigers = new TigerView[NUM_TIGERS];
        for (int i = 0; i < NUM_TIGERS; i++) {
            tigers[i] = createTigerView();
            outerLayout.addView(tigers[i]);
        }
    }

    private TigerView createTigerView() {
        return new TigerView(this, this);
    }

    private void createBoard(RelativeLayout outerLayout) {
        board = new BoardView(this);
        outerLayout.addView(board);
    }

    private void layout(boolean changed, int l, int t, int r, int b) {
        if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {

        } else {
            layoutPortrait(l, t, r, b);
        }
    }

    private void layoutPortrait(int l, int t, int r, int b) {
        final int screenWidth = r - l;
        final int screenHeight = b - t;
        screenSize = new Point(screenWidth, screenHeight);
        final int boardWidth = calculateBoardWidthPortrait(screenWidth);
        final int pieceWidth = calculatePieceWidthPortrait(screenWidth);
        resizeBoardPortrait(boardWidth, screenWidth, screenHeight, pieceWidth);
        resizePiecesPortrait(pieceWidth);
        setGoatPositionsForScreenSizePortrait(screenWidth, screenHeight, pieceWidth);
        updateGoatPositionsFromSavedStateForScreenSizePortrait(pieceWidth, boardWidth);
        setTigerPositionsForScreenSizePortrait(pieceWidth, boardWidth);
    }

    private void resizeBoardPortrait(int boardWidth, int screenWidth, int screenHeight, int pieceWidth) {
        board.setX((screenWidth - boardWidth) / 2f);
        board.setY((screenHeight - boardWidth) / 2f);
        LayoutParams params = board.getLayoutParams();
        params.width = boardWidth;
        params.height = boardWidth;
        board.setLayoutParams(params);
    }

    private int calculateBoardWidthPortrait(int screenWidth) {
        return screenWidth * 3 / 4;
    }

    private int calculatePieceWidthPortrait(int screenWidth) {
        return screenWidth / 10;
    }

    private void resizePiecesPortrait(int pieceWidth) {
        for (int i = 0; i < NUM_GOATS; i++) {
            LayoutParams params = goats[i].getLayoutParams();
            params.width = pieceWidth;
            params.height = pieceWidth;
            goats[i].setLayoutParams(params);
        }

        for (int i = 0; i < NUM_TIGERS; i++) {
            LayoutParams params = tigers[i].getLayoutParams();
            params.width = pieceWidth;
            params.height = pieceWidth;
            tigers[i].setLayoutParams(params);
        }
    }

    private void setGoatPositionsForScreenSizePortrait(int screenWidth, int screenHeight, int pieceWidth) {
        final float file1X = screenWidth / 5f;
        final float pieceHeight = pieceWidth;
        final float halfPieceHeight = pieceHeight / 2f;
        for (int i = 0; i < NUM_GOATS; i++) {
            if (!goats[i].isUsed()) {
                int file = i % (NUM_GOATS / 4);
                int rank = i / (NUM_GOATS / 4);
                switch (rank) {
                case 0:
                    goats[i].setY(0);
                    break;
                case 1:
                    goats[i].setY(pieceHeight + 10);
                    break;
                case 2:
                    goats[i].setY(screenHeight - pieceHeight - pieceHeight - 10);
                    break;
                case 3:
                    goats[i].setY(screenHeight - pieceHeight);
                    break;
                }
                goats[i].setX((file * file1X) + halfPieceHeight);
            }
        }
    }

    private void updateGoatPositionsFromSavedStateForScreenSizePortrait(int pieceWidth, int boardWidth) {
        float halfPieceWidth = pieceWidth / 2f;
        Point[] savedGoatPositions = GameState.getSavedGoatPositions(this);
        for (int i = 0; i < 20; i++) {
            if (savedGoatPositions[i].equals(GameState.TAKEN_GOAT_POINT)) {
                goats[i].setTaken(true);
            } else if (!savedGoatPositions[i].equals(GameState.UNUSED_GOAT_POINT)) {
                Point p = getBoardPositionWithBoardWidthPortrait(savedGoatPositions[i].x, savedGoatPositions[i].y, boardWidth);
                goats[i].setX(p.x - halfPieceWidth);
                goats[i].setY(p.y - halfPieceWidth);
                goats[i].markAsUsed();
            }
        }
    }

    private void setTigerPositionsForScreenSizePortrait(int pieceWidth, int boardWidth) {
        Point[] tigerPositions = GameState.getSavedTigerPositions(this);
        float halfPieceWidth = pieceWidth / 2f;
        for (int i = 0; i < NUM_TIGERS; i++) {
            Point p = getBoardPositionWithBoardWidthPortrait(tigerPositions[i].x, tigerPositions[i].y, boardWidth);
            tigers[i].setX(p.x - halfPieceWidth);
            tigers[i].setY(p.y - halfPieceWidth);
        }
    }

    /**
     * 
     * @param file
     * @param rank
     * @param boardWidth
     *            reading the board width using board.getWidth() when called from onLayout returns the incorrect value
     *            so this value must be passed in
     * @return screen co-ordinates of the point on the board
     */
    private Point getBoardPositionWithBoardWidthPortrait(int file, int rank, int boardWidth) {
        int x = (int) board.getX() + boardWidth * file / 4;
        int y = (int) board.getY() + boardWidth * rank / 4;
        return new Point(x, y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE && draggedPiece != null) {
            draggedPiece.setX(event.getRawX() + draggedPieceDragStartPos.x);
            draggedPiece.setY(event.getRawY() + draggedPieceDragStartPos.y);
            return true;
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (draggedPiece instanceof GoatView) {
                onGoatDrop((GoatView) draggedPiece);
            }
            draggedPiece = null;
        }
        return super.onTouchEvent(event);
    }

    private void saveGameState() {
        GameState.saveGoatPositions(this, getGoatBoardCoords());
        GameState.saveTigerPositions(this, getTigerBoardCoords());
    }

    private Point[] getGoatBoardCoords() {
        Point goatBoardCoord[] = new Point[20];
        for (int i = 0; i < 20; i++) {
            try {
                if (goats[i].isTaken()) {
                    goatBoardCoord[i] = GameState.TAKEN_GOAT_POINT;
                } else {
                    goatBoardCoord[i] = board.getSquareFromPieceDragLocation(goats[i]);
                }
            } catch (InvalidSquareException e) {
                // Do nothing.
            }
        }
        return goatBoardCoord;
    }

    private Point[] getTigerBoardCoords() {
        Point tigerBoardCoords[] = new Point[4];
        for (int i = 0; i < 4; i++) {
            try {
                tigerBoardCoords[i] = board.getSquareFromPieceDragLocation(tigers[i]);
            } catch (InvalidSquareException e) {
                // Do nothing.
            }
        }
        return tigerBoardCoords;
    }

    private void onGoatDrop(GoatView goatBeingDropped) {
        if (isPieceCenterInsideBoard(goatBeingDropped) && !dropSquareEqualsSourceSquare(goatBeingDropped)) {
            if (!isLegalGoatMove(goatBeingDropped)) {
                returnDroppedGoatToStartingPosition();
            } else {
                snapGoatToGridLine(goatBeingDropped);
                goatBeingDropped.markAsUsed();
                makeTigerMove();
            }
        } else {
            returnDroppedGoatToStartingPosition();
        }
    }

    private boolean dropSquareEqualsSourceSquare(GoatView goatBeingDropped) {
        try {
            Point srcSquare = getSquareFromPieceCoords(draggedPieceReturnLocation, goatBeingDropped);
            Point targetSquare = board.getSquareFromPieceDragLocation(goatBeingDropped);
            return srcSquare.equals(targetSquare);
        } catch (InvalidSquareException e) {
            return false;
        }
    }

    private Point getSquareFromPieceCoords(Point pieceScreenCoords, Piece piece) throws InvalidSquareException {
        float pieceCenterX = pieceScreenCoords.x + (piece.getWidth() / 2f);
        float pieceCenterY = pieceScreenCoords.y + (piece.getHeight() / 2f);
        float halfPieceWidth = piece.getWidth() / 2f;
        return board.getSquareFromScreenCoords(pieceCenterX, pieceCenterY, halfPieceWidth);
    }

    private void makeTigerMove() {
        try {
            TigerMove tigerMove = new TigerAi(buildBoardModel(), getNumUnusedGoats()).pickTigerMove(this);
            TigerView tigerToMove = getTigerAtSquare(tigerMove.src.x, tigerMove.src.y);
            Point newTigerPos = board.getScreenCoordsForSquare(tigerMove.dest);
            float halfPieceWidth = tigerToMove.getWidth() / 2f;
            tigerToMove.animate().x(newTigerPos.x - halfPieceWidth).y(newTigerPos.y - halfPieceWidth).setDuration(PIECE_MOVE_DURATION)
                    .setListener(this);
            if (tigerMove.isTakingMove()) {
                Point takenGoatPos = tigerMove.getBoardCoordsOfGoatBeingTaken();
                GoatView takenGoat = getGoatAtSquare(takenGoatPos.x, takenGoatPos.y);
                takenGoat.animate().alpha(0).setDuration(PIECE_MOVE_DURATION).setListener(this);
            }
        } catch (NoLegalTigerMovesException e) {
            goToNextLevel();
        }
    }

    private int getNumUnusedGoats() {
        int numUnusedGoats = 0;
        for (GoatView goat : goats) {
            if (!goat.isUsed()) {
                numUnusedGoats++;
            }
        }
        return numUnusedGoats;
    }

    private void goToNextLevel() {
        GameState.incrementLevel(this);
        showLevelComplete();
    }

    private void showLevelComplete() {
        Intent myIntent = new Intent(this, LevelCompleteActivity.class);
        startActivity(myIntent);
    }

    private GoatView getGoatAtSquare(int x, int y) {
        return (GoatView) getPieceFromArrayAtSquare(getGoatsOnBoard(), x, y);
    }

    private GoatView[] getGoatsOnBoard() {
        List<GoatView> goatsOnBoard = new ArrayList<GoatView>();
        for (GoatView goat : goats) {
            if (goat.isUsed() && !goat.isTaken()) {
                goatsOnBoard.add(goat);
            }
        }
        return goatsOnBoard.toArray(new GoatView[goatsOnBoard.size()]);
    }

    private TigerView getTigerAtSquare(int x, int y) {
        return (TigerView) getPieceFromArrayAtSquare(tigers, x, y);
    }

    private Piece getPieceFromArrayAtSquare(Piece[] pieces, int x, int y) {
        Point targetPiecePos = board.getScreenCoordsForSquare(new Point(x, y));
        for (Piece piece : pieces) {
            float pieceCenterX = piece.getX() + (piece.getWidth() / 2f);
            float pieceCenterY = piece.getY() + (piece.getHeight() / 2f);
            if (Math.abs(pieceCenterX - targetPiecePos.x) < 20 && Math.abs(pieceCenterY - targetPiecePos.y) < 20) {
                return piece;
            }
        }
        return null;
    }

    private SquareContents[][] buildBoardModel() {
        SquareContents[][] squares = new SquareContents[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                squares[i][j] = SquareContents.EMPTY;
            }
        }

        try {
            for (GoatView goat : goats) {
                if (goat.isUsed() && !goat.isTaken()) {
                    Point goatPos = board.getSquareFromPieceDragLocation(goat);
                    squares[goatPos.x][goatPos.y] = SquareContents.GOAT;
                }
            }

            for (TigerView tiger : tigers) {
                Point tigerPos = board.getSquareFromPieceDragLocation(tiger);
                squares[tigerPos.x][tigerPos.y] = SquareContents.TIGER;
            }
        } catch (InvalidSquareException e) {
            // Do nothing.
        }

        return squares;
    }

    private void snapGoatToGridLine(Piece pieceBeingDropped) {
        try {
            float halfPieceWidth = pieceBeingDropped.getWidth() / 2f;
            Point gridPos = board.getSquareFromPieceDragLocation(pieceBeingDropped);
            Point screenPos = board.getScreenCoordsForSquare(gridPos);
            pieceBeingDropped.setX(screenPos.x - halfPieceWidth);
            pieceBeingDropped.setY(screenPos.y - halfPieceWidth);
        } catch (InvalidSquareException e) {
            // Do nothing.
        }
    }

    private void returnDroppedGoatToStartingPosition() {
        if (draggedPieceReturnLocation != null) {
            draggedPiece.animate().x(draggedPieceReturnLocation.x).y(draggedPieceReturnLocation.y).setDuration(PIECE_MOVE_DURATION).start();
        }
    }

    private boolean isPieceCenterInsideBoard(Piece piece) {
        try {
            board.getSquareFromPieceDragLocation(piece);
            return true;
        } catch (InvalidSquareException e) {
            return false;
        }
    }

    private void showMsg(String msg) {
        new AlertDialog.Builder(this).setTitle("Goats and Tigers").setMessage(msg)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing.
                    }
                }).show();
    }

    private boolean isLegalGoatMove(GoatView goat) {
        if (!goat.isUsed()) {
            try {
                Point targetSquare = board.getSquareFromPieceDragLocation(goat);
                return isSquareEmpty(targetSquare.x, targetSquare.y);
            } catch (InvalidSquareException e) {
                showMsg(MSG_ILLEGAL_UNUSED_GOAT_MOVE);
                return false;
            }
        }

        if (!isAllGoatsUsed()) {
            showMsg(MSG_NOT_ALL_GOATS_DROPPED_ON_BOARD);
            return false;
        }

        try {
            Point goatCurrentSquare = board.getSquareFromScreenCoords(draggedPieceReturnLocation.x, draggedPieceReturnLocation.y, goat.getWidth());
            Point targetSquare = board.getSquareFromPieceDragLocation(goat);
            Direction direction = Direction.getDirection(targetSquare.x - goatCurrentSquare.x, targetSquare.y - goatCurrentSquare.y);
            if (!BoardModel.canMoveInDirection(goatCurrentSquare, direction)) {
                showMsg(MSG_ILLEGAL_USED_GOAT_MOVE);
                return false;
            }
        } catch (InvalidSquareException e) {
            return false;
        }
        return true;
    }

    private boolean isAllGoatsUsed() {
        for (int i = 0; i < NUM_GOATS; i++) {
            if (!goats[i].isUsed()) {
                return false;
            }
        }
        return true;
    }

    private boolean isSquareEmpty(int x, int y) {
        for (GoatView goat : goats) {
            if (goat.isUsed() && !goat.isTaken()) {
                try {
                    Point goatSquare = board.getSquareFromPieceDragLocation(goat);
                    if (goatSquare.x == x && goatSquare.y == y) {
                        return false;
                    }
                } catch (InvalidSquareException e) {
                    // Do nothing.
                }
            }
        }

        for (TigerView tiger : tigers) {
            try {
                Point tigerSquare = board.getSquareFromPieceDragLocation(tiger);
                if (tigerSquare.x == x && tigerSquare.y == y) {
                    return false;
                }
            } catch (InvalidSquareException e) {
                // Do nothing.
            }

        }

        return true;
    }

    @Override
    public void dragStart(Piece draggedPiece, float x, float y) {
        this.draggedPiece = draggedPiece;
        draggedPieceDragStartPos = new Point((int) x, (int) y);
        draggedPieceReturnLocation = new Point((int) draggedPiece.getX(), (int) draggedPiece.getY());
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        saveGameState();
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }

}
