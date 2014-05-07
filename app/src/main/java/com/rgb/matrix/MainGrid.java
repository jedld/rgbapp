package com.rgb.matrix;

import android.util.Log;

import com.dayosoft.tiletron.app.SoundWrapper;
import com.rgb.matrix.interfaces.BoundedEntity;
import com.rgb.matrix.interfaces.GameOverDialogEventListener;
import com.rgb.matrix.interfaces.GridEventListener;
import com.rgb.matrix.menu.MainMenu;
import com.rgb.matrix.menu.MenuItem;
import com.rgb.matrix.menu.OnBackListener;
import com.rgb.matrix.menu.OnMenuSelectedListener;

import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.ScreenCapture;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;
import org.andengine.util.modifier.IModifier;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by joseph on 4/23/14.
 */
public class MainGrid extends BoundedEntity {

    public static final int QUEUE_SIZE = 7;
    public static final int TILE_SIZE_IN_DIP = 54;

    public static final int ENDLESS_MODE_TILE_SIZE = 54;
    private final MatrixOptions options;
    private final float width;

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    private final float height;
    private float rectangleTileSizeInPixels = ENDLESS_MODE_TILE_SIZE;
    private static final String TAG = MainGrid.class.getName();
    public static final Color COLOR_QUEUE_CURRENT_BORDER = new Color(0xbd / 255f, 0xbd / 255f, 0xbd / 255f);

    private final int gridWidth;
    private final int gridHeight;
    private final HashMap<String, Font> fontDictionary;
    private final VertexBufferObjectManager vertexBuffer;
    private final GameMatrix matrix;
    private final Font mFont;
    private final HashMap<String, SoundWrapper> soundAssets;
    private final GridEventListener gridEventListener;

    public GridSquare[][] getWorld() {
        return world;
    }

    public void setWorld(GridSquare[][] world) {
        this.world = world;
    }

    GridSquare world[][];
    GridSquare queueRectangles[] = new GridSquare[QUEUE_SIZE];
    private Text scoreText;
    private RectangleButton newGameButton;
    private RechargeMeter rechargeMeter;
    private MainMenu mainMenu;
    private Text chainBonusRepeaterText;
    private Rectangle chainBonusRepeaterBackground;
    private static float repeaterSizeInPixels = 15;
    private GameOver gameOverText;
    private ScreenCapture screenCapture;
    private boolean highScoreAchieved;

    public float getRectangleTileSizeInPixels() {
        return rectangleTileSizeInPixels;
    }

    public void setRectangleTileSizeInPixels(float rectangleTileSizeInPixels) {
        this.rectangleTileSizeInPixels = rectangleTileSizeInPixels;
    }

    public static float getRepeaterSizeInPixels() {
        return repeaterSizeInPixels;
    }

    public static void setRepeaterSizeInPixels(float repeaterSizeInPixels) {
        MainGrid.repeaterSizeInPixels = repeaterSizeInPixels;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    private int score = 0;
    private String scoreTextString;

    public MainGrid(float x, float y, float width, float height, int gridWidth, int gridHeight, GameMatrix matrix,
                    MainMenu mainMenu, HashMap<String, Font> fontDictionary,
                    HashMap<String, SoundWrapper> soundAssets,
                    VertexBufferObjectManager vertexBuffer,
                    GridEventListener gridEventListener, MatrixOptions options) {
        super(x, y);
        this.width = width;
        this.height = height;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.matrix = matrix;
        this.gridEventListener = gridEventListener;
        this.mainMenu = mainMenu;
        this.soundAssets = soundAssets;
        this.world = new GridSquare[gridWidth][gridHeight];
        this.vertexBuffer = vertexBuffer;
        this.fontDictionary = fontDictionary;
        this.highScoreAchieved = false;
        this.mFont = fontDictionary.get("score");
        this.options = options;
        setupWorld();
    }

    public void fadeOutAllRectangles() {
        for (int i = 0; i < gridWidth; i++) {
            for (int i2 = 0; i2 < gridHeight; i2++) {
                world[i][i2].animateGameOver();
            }
        }
    }

    public void setupWorld() {
        Entity gridEntity = new Entity();

        float maxGridWidth = 0, maxGridHeight = 0;

        for (int i = 0; i < gridWidth; i++) {
            for (int i2 = 0; i2 < gridHeight; i2++) {
                float rect_x = (i * getRectangleTileSizeInPixels());
                float rect_y = (i2 * getRectangleTileSizeInPixels());

                if (rect_x + getRectangleTileSizeInPixels() > maxGridWidth) {
                    maxGridWidth = rect_x + getRectangleTileSizeInPixels();
                }

                if (rect_y + getRectangleTileSizeInPixels() > maxGridHeight) {
                    maxGridHeight = rect_y + getRectangleTileSizeInPixels();
                }
                GridSquare gridSquare = new GridSquare(i, i2, rect_x, rect_y, this, fontDictionary, vertexBuffer);
                gridEntity.attachChild(gridSquare);
                world[i][i2] = gridSquare;
            }
        }

        attachChild(gridEntity);

        //center grid
        gridEntity.setX(width / 2 - maxGridWidth / 2);


        float offsetY = maxGridHeight + ObjectDimensions.szGridPaddingBottom;

        if (options.shouldShowRechargeMeter) {
            this.rechargeMeter = new RechargeMeter(width / 2 - ObjectDimensions.szRechargeMeterWidth / 2, offsetY, ObjectDimensions.szRechargeMeterWidth, 150, fontDictionary, soundAssets, vertexBuffer);
            attachChild(rechargeMeter);
        }

        if (rechargeMeter != null) {
            offsetY += rechargeMeter.getHeight() + ObjectDimensions.szRechargeMeterPaddingBottom;
        }

        for (int i = QUEUE_SIZE - 1; i >= 0; i--) {

            float rect_x = (i * (getRectangleTileSizeInPixels() + 5) + 15);
            float rect_y = offsetY;

            GridSquare gridSquare = null;
            if (i == 0) {
                Rectangle container = new Rectangle(rect_x, rect_y, getRectangleTileSizeInPixels() + 2, getRectangleTileSizeInPixels() + 2, vertexBuffer);
                Rectangle border = new Rectangle(1, 1, getRectangleTileSizeInPixels(), getRectangleTileSizeInPixels(), vertexBuffer);
                border.setColor(Color.WHITE);
                container.attachChild(border);
                container.setColor(Color.BLACK);
                container.setAlpha(0.2f);
                gridSquare = new GridSquare(-1, -1, 1, 1, this, fontDictionary, vertexBuffer);
                container.setScaleCenter((getRectangleTileSizeInPixels() + 2) / 2, (getRectangleTileSizeInPixels() + 2) / 2);
                container.setScale(1.5f);
                border.attachChild(gridSquare);
                attachChild(container);
            } else {
                gridSquare = new GridSquare(-1, -1, rect_x, rect_y, this, fontDictionary, vertexBuffer);
                gridSquare.setScale(1.1f);
                gridSquare.setScaleCenter(getRectangleTileSizeInPixels() / 2, getRectangleTileSizeInPixels() / 2);
                attachChild(gridSquare);

            }
            queueRectangles[i] = gridSquare;

        }

        offsetY += ObjectDimensions.szQueuePaddingBottom;

        String highScore = StringUtils.leftPad(Integer.toString(matrix.getHighScore()), 4, "0");
        scoreText = new Text(0, offsetY, mFont, "Score: 0000 High: " + highScore, vertexBuffer);
        scoreText.setText("Score: 0000 High: " + highScore);
        scoreText.setColor(Color.BLACK);
        attachChild(scoreText);


        chainBonusRepeaterText = new Text(0, 0, fontDictionary.get("multiplier"), "Chain Bonus X 2!", vertexBuffer);
        chainBonusRepeaterText.setX(width / 2 - (chainBonusRepeaterText.getWidth() / 2));
        chainBonusRepeaterText.setY(height / 2 - (chainBonusRepeaterText.getHeight() / 2));
        chainBonusRepeaterText.setColor(Color.RED);
        chainBonusRepeaterText.setVisible(false);

        chainBonusRepeaterBackground = new Rectangle(chainBonusRepeaterText.getX(),
                chainBonusRepeaterText.getY(), chainBonusRepeaterText.getWidth(),
                chainBonusRepeaterText.getHeight(), vertexBuffer);
        chainBonusRepeaterBackground.setColor(Color.WHITE);
        chainBonusRepeaterBackground.setAlpha(0.3f);
        chainBonusRepeaterBackground.setVisible(false);
        attachChild(chainBonusRepeaterBackground);
        attachChild(chainBonusRepeaterText);

        newGameButton = new RectangleButton(width - 170, offsetY - 15, 170, 50, vertexBuffer, mFont, "Menu");
        attachChild(newGameButton);

        screenCapture = new ScreenCapture();
        attachChild(screenCapture);

        gameOverText = new GameOver(0, 0, mFont, vertexBuffer);
        gameOverText.setX(width / 2 - (gameOverText.getWidth() / 2));
        gameOverText.setY((height / 2) - (gameOverText.getHeight() / 2));
        gameOverText.setVisible(false);
        attachChild(gameOverText);
        mainMenu.clearItems();

        mainMenu.addMenuItem("Restart", new OnMenuSelectedListener() {

            @Override
            public void onMenuItemSelected(MenuItem item) {
                mainMenu.setVisible(false);
                newGame();
            }
        });

        mainMenu.addMenuItem("Exit to Title Screen", new OnMenuSelectedListener() {
            @Override
            public void onMenuItemSelected(MenuItem item) {
                gridEventListener.onExitGrid(item);
            }
        });

        boolean defaultMusicState = true, defaultSoundState = true;

        if (gridEventListener != null) {
            defaultMusicState = gridEventListener.getMusicState();
            defaultSoundState = gridEventListener.getSoundState();
        }

        mainMenu.addMenuItem("Music", true, defaultMusicState, new OnMenuSelectedListener() {

            @Override
            public void onMenuItemSelected(MenuItem item) {
                if (gridEventListener != null) {
                    item.setState(!item.getState());
                    gridEventListener.toggleMusic(item.getState());
                }
            }
        });

        mainMenu.addMenuItem("Sounds", true, defaultSoundState, new OnMenuSelectedListener() {

            @Override
            public void onMenuItemSelected(MenuItem item) {
                if (gridEventListener != null) {
                    item.setState(!item.getState());
                    gridEventListener.toggleSounds(item.getState());
                }
            }
        });

        mainMenu.setOnBackListener(new OnBackListener() {
            @Override
            public void onBackPressed(MainMenu mainMenu) {
                mainMenu.animateHide();
            }
        });
    }

    public void shareOnFacebook() {
        gridEventListener.onScreenCaptureHighScore(gameOverText, screenCapture);
    }

    public void showChainBonus(int multiplier) {
        chainBonusRepeaterText.setVisible(true);
        chainBonusRepeaterBackground.setVisible(true);
        chainBonusRepeaterText.setText("Chain Bonus X " + multiplier + "!");
        chainBonusRepeaterText.registerEntityModifier(new AlphaModifier(2f, 0f, 1f, new IEntityModifier.IEntityModifierListener() {
            @Override
            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {

            }

            @Override
            public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                chainBonusRepeaterText.setVisible(false);
                chainBonusRepeaterBackground.setVisible(false);
            }
        }));
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
            int itemColor = adjacentColor.getTileType();
            if (adjacentColor.getTileType() == GridSquare.MULTIPLIERX4_COLORED) {
                itemColor = adjacentColor.getMultiplierColor();
            }

            if (itemColor != color) {
                if (colorTest == 0) {
                    colorTest = itemColor;
                } else {
                    if (colorTest != itemColor) return true;
                }
            }
        }
        return false;
    }

    public void resetWorldState() {
        score = 0;
        highScoreAchieved = false;
        scoreTextString = "Score: 0000";
        for (int i = 0; i < gridWidth; i++) {
            for (int i2 = 0; i2 < gridHeight; i2++) {
                world[i][i2].reset();
            }
        }
        gameOverText.setVisible(false);
        if (rechargeMeter != null) {
            rechargeMeter.resetMeterState();
        }
    }


    public void addScore(int add) {
        score += add;
        if (rechargeMeter != null) {
            rechargeMeter.addPoints(add);
        }
    }

    public String formatScoreString(int currentScore, String highScore) {
        return "Score: " + StringUtils.leftPad(Integer.toString(currentScore), 4, "0") + " High: " + highScore;
    }

    public void animateRechargeMeter() {
        if (rechargeMeter != null) {
            this.rechargeMeter.animate();
        }
    }

    public void updateScoreText(String highScore) {
        scoreTextString = formatScoreString(score, highScore);
        scoreText.setText(scoreTextString);
    }

    public synchronized void updateSelf() {
//        showGameOver();
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                world[x][y].updateSelf();
            }
        }
    }

    public boolean isValid(int grid_x, int grid_y) {
        if (grid_x < 0) return false;
        if (grid_y < 0) return false;
        if (grid_x >= gridWidth) return false;
        if (grid_y >= gridHeight) return false;
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
//        fadeOutAllRectangles();
        gameOverText.show();
    }

    public GridSquare getQueueRect(int i) {
        return queueRectangles[i];
    }

    public GridSquare tileTouched(TouchEvent pSceneTouchEvent) {
        for (int i = 0; i < gridWidth; i++) {
            for (int i2 = 0; i2 < gridHeight; i2++) {
                if (Utils.withinTouchBounds(world[i][i2], pSceneTouchEvent)) {
                    return world[i][i2];
                }
            }
        }
        return null;
    }

    public boolean onTouch(TouchEvent pSceneTouchEvent, boolean disableMoves) {
        Log.d(TAG, "onTouch " + pSceneTouchEvent.getX() + " " + pSceneTouchEvent.getY());
        if (mainMenu.isVisible()) {
            mainMenu.handleOnTouch(pSceneTouchEvent);
        } else if (!disableMoves) {
            GridSquare target = null;
            if (rechargeMeter != null && rechargeMeter.isAreaTouched(pSceneTouchEvent)) {
                useRechargeMeter();
            } else if (newGameButton.isAreaTouched(pSceneTouchEvent)) {
                showMenu();
            } else if (gameOverText.isVisible()) {
                gameOverText.handleTouch(pSceneTouchEvent, new GameOverDialogEventListener() {
                    @Override
                    public void onShare(RectangleButton shareButton) {
                        gridEventListener.onScreenCaptureHighScore(gameOverText, screenCapture);
                    }

                    @Override
                    public void onRestart(RectangleButton restartButton) {
                        newGame();
                    }
                });
            } else if ((target = tileTouched(pSceneTouchEvent)) != null) {
                int grid_x = target.getBoardPositionX();
                int grid_y = target.getBoardPositionY();

                if (matrix.blockQueue.size() > 0) {
                    if (isValid(grid_x, grid_y) && matrix.setAndGetinProgress()) {

                        NextObject object = matrix.blockQueue.remove(0);
                        matrix.fillQueue();
                        Log.d(TAG, "updating world " + grid_x + " , " + grid_y + " = " + object);
                        matrix.updateWorld(grid_x, grid_y, object, 1, false);
                        matrix.decrementInProgress();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void showMenu() {
        mainMenu.animateShow();
    }

    private void newGame() {
        matrix.resetWorld();
        matrix.populateInitial();
    }

    public void useRechargeMeter() {
        Log.d(TAG, "use super?");
        if (rechargeMeter != null && rechargeMeter.use()) {
            clearBustedSquares();
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
        if (rechargeMeter != null) {
            rechargeMeter.markStart();
        }
    }

    public boolean hasRechargeMeter() {
        if (rechargeMeter == null) return false;
        return rechargeMeter.isSuperActive();
    }

}
