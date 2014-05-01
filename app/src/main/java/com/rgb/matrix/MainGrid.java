package com.rgb.matrix;

import android.util.Log;

import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by joseph on 4/23/14.
 */
public class MainGrid extends Entity {

    public static final int QUEUE_SIZE = 7;
    public static final int RECT_SIZE = 54;
    private static final String TAG = MainGrid.class.getName();
    public static final Color COLOR_QUEUE_CURRENT_BORDER = new Color(0xbd / 255f, 0xbd / 255f, 0xbd / 255f);

    private final int gridWidth;
    private final int gridHeight;
    private final HashMap<String, Font> fontDictionary;
    private final VertexBufferObjectManager vertexBuffer;
    private final GameMatrix matrix;
    private final Font mFont;
    GridSquare world[][];
    GridSquare queueRectangles[] = new GridSquare[QUEUE_SIZE];
    private Text scoreText;
    private Text gameOverText;
    private RectangleButton newGameButton;
    private RechargeMeter rechargeMeter;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    private int score = 0;
    private String scoreTextString;

    public MainGrid(float x, float y, int gridWidth, int gridHeight, GameMatrix matrix, HashMap<String, Font> fontDictionary, VertexBufferObjectManager vertexBuffer) {
        super(x, y);
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.matrix = matrix;
        this.world = new GridSquare[gridWidth][gridHeight];
        this.vertexBuffer = vertexBuffer;
        this.fontDictionary = fontDictionary;
        this.mFont = fontDictionary.get("score");
        setupWorld();
    }

    public void setupWorld() {
        for (int i = 0; i < gridWidth; i++) {
            for (int i2 = 0; i2 < gridHeight; i2++) {
                float rect_x = (i * RECT_SIZE);
                float rect_y = (i2 * RECT_SIZE);
                GridSquare gridSquare = new GridSquare(i, i2, rect_x, rect_y, this, fontDictionary, vertexBuffer);
                attachChild(gridSquare);
                world[i][i2] = gridSquare;
            }
        }

        int offsetY = (gridHeight * RECT_SIZE) + 10;

        this.rechargeMeter = new RechargeMeter(0, offsetY, gridWidth * RECT_SIZE, 100, fontDictionary, vertexBuffer);
        attachChild(rechargeMeter);

        offsetY+=rechargeMeter.getHeight() +5;

        for (int i = QUEUE_SIZE - 1; i >= 0; i--) {

            float rect_x = (i * (RECT_SIZE + 5));
            float rect_y = offsetY;

            GridSquare gridSquare = null;
            if (i == 0) {
                Rectangle container = new Rectangle(rect_x, rect_y, RECT_SIZE + 2, RECT_SIZE + 2, vertexBuffer);
                Rectangle border = new Rectangle(1, 1, RECT_SIZE, RECT_SIZE, vertexBuffer);
                border.setColor(Color.WHITE);
                container.attachChild(border);
                container.setColor(Color.BLACK);
                container.setAlpha(0.2f);
                gridSquare = new GridSquare(-1, -1, 1, 1, this, fontDictionary, vertexBuffer);
                container.setScaleCenter( (RECT_SIZE + 2) /2, (RECT_SIZE + 2)/2);
                container.setScale(1.5f);
                border.attachChild(gridSquare);
                attachChild(container);
            } else {
                gridSquare = new GridSquare(-1, -1, rect_x, rect_y, this, fontDictionary, vertexBuffer);
                gridSquare.setScale(1.1f);
                gridSquare.setScaleCenter(RECT_SIZE/2,RECT_SIZE/2);
                attachChild(gridSquare);

            }
            queueRectangles[i] = gridSquare;

        }

        offsetY += 100;

        String highScore = StringUtils.leftPad(Integer.toString(matrix.getHighScore()), 4, "0");
        scoreText = new Text(0, offsetY, mFont, "Score: 0000 High: " + highScore, vertexBuffer);
        scoreText.setText("Score: 0000 High: " + highScore);
        scoreText.setColor(Color.BLACK);
        attachChild(scoreText);

        int textOffet = (gridHeight * RECT_SIZE) / 2;

        gameOverText = new Text((gridWidth * RECT_SIZE) / 2, textOffet, mFont, "GAME OVER", vertexBuffer);
        gameOverText.setColor(Color.RED);
        gameOverText.setVisible(false);
        attachChild(gameOverText);

        newGameButton = new RectangleButton((gridWidth * RECT_SIZE) - 170, offsetY - 15, 170, 50, vertexBuffer, mFont, "New Game");
        attachChild(newGameButton);
    }

    public Entity drawRect(float x, float y, float x1, float y1, Color color, float pLineWidth) {
        Entity entity = new Entity(x, y);
        Line line1 = new Line(x, y, x, y1, vertexBuffer);
        line1.setLineWidth(pLineWidth);
        line1.setColor(color);

        Line line2 = new Line(x, y, x1, y, vertexBuffer);
        line2.setLineWidth(pLineWidth);
        line2.setColor(color);

        Line line3 = new Line(x1, y, x1, y1, vertexBuffer);
        line3.setLineWidth(pLineWidth);
        line3.setColor(color);

        Line line4 = new Line(x, y1, x1, y1, vertexBuffer);
        line4.setLineWidth(pLineWidth);
        line4.setColor(color);

        entity.attachChild(line1);
        entity.attachChild(line2);
        entity.attachChild(line3);
        entity.attachChild(line4);
        return entity;
    }

    public GridSquare getSquareAt(int x, int y) {
        return world[x][y];
    }


    public boolean isRGB(int x, int y, int color) {
        Log.d(TAG, "isRGB = " + color);
        ArrayList<GridSquare> adjacentSquares = new ArrayList<GridSquare>();
        if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {

            if (x > 0 && world[x - 1][y].isColoredTile()) {
                adjacentSquares.add(world[x - 1][y]);
            }

            if (x < gridWidth - 1 && world[x + 1][y].isColoredTile()) {
                adjacentSquares.add(world[x + 1][y]);
            }

            if (y > 0 && world[x][y - 1].isColoredTile()) {
                adjacentSquares.add(world[x][y - 1]);
            }

            if (y < gridHeight - 1 && world[x][y + 1].isColoredTile()) {
                adjacentSquares.add(world[x][y + 1]);
            }

        }
        int colorTest = 0;
        for (GridSquare adjacentColor : adjacentSquares) {
            Log.d(TAG, "test = " + adjacentColor);
            if (adjacentColor.getTileType() != color) {
                if (colorTest == 0) {
                    colorTest = adjacentColor.getTileType();
                } else {
                    if (colorTest != adjacentColor.getTileType()) return true;
                }
            }
        }
        return false;
    }

    public void reset() {
        score = 0;
        scoreTextString = "Score: 0000";
        for (int i = 0; i < gridWidth; i++) {
            for (int i2 = 0; i2 < gridHeight; i2++) {
                world[i][i2].reset();
            }
        }
        gameOverText.setVisible(false);
        rechargeMeter.reset();
    }




    public void addScore(int add) {
        score += add;
        rechargeMeter.addPoints(add);
    }

    public String formatScoreString(int currentScore, String highScore) {
        return "Score: " + StringUtils.leftPad(Integer.toString(currentScore), 4, "0") + " High: " + highScore;
    }

    public void animateRechargeMeter() {
        this.rechargeMeter.animate();
    }

    public void updateScoreText(String highScore) {
        scoreTextString = formatScoreString(score, highScore);
        scoreText.setText(scoreTextString);
    }

    public synchronized void updateSelf() {
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                world[x][y].updateSelf();
            }
        }
    }

    public boolean isValid(int grid_x, int grid_y) {
        if (grid_x < 0) return false;
        if (grid_y < 0) return false;
        if (!world[grid_x][grid_y].isEmpty()) return false;

//        if (grid_x >= 0 && grid_y >= 0 && grid_x < gridWidth && grid_y < gridHeight) {
//
//            if (!isEmpty(grid_x - 1, grid_y)) {
//                return true;
//            } else if (!isEmpty(grid_x + 1, grid_y)) {
//                return true;
//            } else if (!isEmpty(grid_x, grid_y - 1)) {
//                return true;
//            } else if (!isEmpty(grid_x, grid_y + 1)) {
//                return true;
//            } else if (!isEmpty(grid_x - 1, grid_y - 1)) {
//                return true;
//            } else if (!isEmpty(grid_x + 1, grid_y + 1)) {
//                return true;
//            } else if (!isEmpty(grid_x + 1, grid_y - 1)) {
//                return true;
//            } else if (!isEmpty(grid_x - 1, grid_y + 1)) {
//                return true;
//            }
//        }
//        return false;
        return true;
    }

    public boolean isEmpty(int x, int y) {
        if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {
            return world[x][y].isEmpty();
        }
        return true;
    }

    public void showGameOver() {
        gameOverText.setVisible(true);
    }

    public GridSquare getQueueRect(int i) {
        return queueRectangles[i];
    }

    public void onTouch(TouchEvent pSceneTouchEvent) {
        Log.d(TAG, "onTouch " + pSceneTouchEvent.getX() + " " + pSceneTouchEvent.getY());
        if (rechargeMeter.isAreaTouched(pSceneTouchEvent)) {
            Log.d(TAG, "use super?");
            if (rechargeMeter.use()) {
                clearBustedSquares();
            }
        } else
        if (newGameButton.isAreaTouched(pSceneTouchEvent)) {
            matrix.resetWorld();
            matrix.populateInitial();

        } else if (getX() <= pSceneTouchEvent.getX() && getY() <= pSceneTouchEvent.getY() &&
                getX() + gridWidth * MainGrid.RECT_SIZE >= pSceneTouchEvent.getX() && getY() + gridHeight * MainGrid.RECT_SIZE >= pSceneTouchEvent.getY()) {

            float normalized_x = pSceneTouchEvent.getX() - getX();
            float normalized_y = pSceneTouchEvent.getY() - getY();

            int grid_x = (int) normalized_x / MainGrid.RECT_SIZE;
            int grid_y = (int) normalized_y / MainGrid.RECT_SIZE;

            if (isValid(grid_x, grid_y)) {

                NextObject object = matrix.blockQueue.remove(0);
                matrix.fillQueue();
                Log.d(TAG, "updating world " + grid_x + " , " + grid_y + " = " + object);
                matrix.updateWorld(grid_x, grid_y, object, false);


            }
        }
    }

    private void clearBustedSquares() {
        matrix.clearBustedSquares();
    }

    public int getMaxPositionX() {
        return gridWidth;
    }

    public int getMaxPositionY() {
        return gridHeight;
    }

    public Text getScoreText() {
        return scoreText;
    }

    public void rechargeMeterMark() {
        rechargeMeter.markStart();
    }
}
