package com.goatsandtigers.goatsandtigers;

import com.goatsandtigers.goatsandtigers.exception.InvalidSquareException;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.ViewGroup;
import android.widget.ImageView;

public class BoardView extends ImageView {

    private Paint linePaint;
    private Integer width, height;

    public BoardView(Context context) {
        super(context);
        linePaint = buildLinePaint();
        setImageResource(R.drawable.marble04);
    }

    private Paint buildLinePaint() {
        Paint paint = new Paint(0);
        paint.setStrokeWidth(3);
        return paint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLines(canvas);
    }

    public Point getScreenCoordsForSquare(Point square) {
        final float quarterOfWidth = getBoardWidthInPixels() / 4f;
        final float quarterOfeHeight = getBoardHeightInPixels() / 4f;
        float x = getX() + (square.x * quarterOfWidth);
        float y = getY() + (square.y * quarterOfeHeight);
        return new Point((int) x, (int) y);
    }

    private void drawLines(Canvas canvas) {
        final int width = getBoardWidthInPixels();
        final int height = getBoardHeightInPixels();
        final float quarterOfWidth = width / 4f;
        final float quarterOfeHeight = height / 4f;

        for (int i = 0; i < 5; i++) {
            // Draw vertical lines.
            float verticalLineX = Math.min(i * quarterOfWidth, (float) width - 2);
            verticalLineX = Math.max(verticalLineX, 2);
            canvas.drawLine(verticalLineX, 1, verticalLineX, height, linePaint);

            // Draw horizontal lines.
            float horizontalLineY = Math.min(i * quarterOfeHeight, (float) height - 2);
            horizontalLineY = Math.max(horizontalLineY, 2);
            canvas.drawLine(1, horizontalLineY, width, horizontalLineY, linePaint);
        }

        // Draw diagonal lines.
        final float halfWidth = width / 2f;
        final float halfHeight = height / 2f;
        canvas.drawLine(0, 0, width, height, linePaint);
        canvas.drawLine(0, height, width, 0, linePaint);
        canvas.drawLine(0, halfHeight, halfWidth, 0, linePaint);
        canvas.drawLine(halfWidth, 0, width, halfHeight, linePaint);
        canvas.drawLine(width, halfHeight, halfWidth, height, linePaint);
        canvas.drawLine(halfWidth, height, 0, halfHeight, linePaint);
    }

    public Point getSquareFromPieceDragLocation(Piece piece) throws InvalidSquareException {
        float pieceCenterX = piece.getX() + (piece.getWidth() / 2f);
        float pieceCenterY = piece.getY() + (piece.getHeight() / 2f);
        return getSquareFromScreenCoords(pieceCenterX, pieceCenterY, piece.getWidth());
    }

    public Point getSquareFromScreenCoords(float screenX, float screenY, float pieceWidth) throws InvalidSquareException {
        float x = screenX - getX();
        float y = screenY - getY();
        final float quarterOfWidth = getBoardWidthInPixels() / 4f;
        final float quarterOfHeight = getBoardHeightInPixels() / 4f;
        int squareX = -1, squareY = -1;

        for (int i = 0; i < 5; i++) {
            float verticalLineX = i * quarterOfWidth;
            if (Math.abs(verticalLineX - x) < pieceWidth) {

                squareX = i;
            }

            float horizontalLineY = i * quarterOfHeight;
            if (Math.abs(horizontalLineY - y) < pieceWidth) {
                squareY = i;
            }
        }

        if (squareX < 0 || squareX > 4 || squareY < 0 || squareY > 4) {
            throw new InvalidSquareException();
        }
        return new Point(squareX, squareY);
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
    private int getBoardWidthInPixels() {
        return width != null ? width : getWidth();
    }

    /**
     * Hack for when height set by setLayoutParams() but not yet picked up by layout() method.
     * 
     * @return height
     */
    private int getBoardHeightInPixels() {
        return height != null ? height : getHeight();
    }
}
