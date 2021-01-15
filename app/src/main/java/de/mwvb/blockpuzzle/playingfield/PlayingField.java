package de.mwvb.blockpuzzle.playingfield;

import android.util.Log;

import de.mwvb.blockpuzzle.block.BlockTypes;
import de.mwvb.blockpuzzle.gamepiece.GamePiece;
import de.mwvb.blockpuzzle.persistence.GamePersistence;

public class PlayingField {
    // Stammdaten
    private final int blocks;

    // Zustand
    /**
     * 1: x (nach rechts), 2: y (nach unten)
     */
    private int[][] matrix;
    private boolean gameOver = false;

    // Services
    private IPlayingFieldView view;
    private GamePersistence persistence;

    // TO-DO Idee: Jeder Block sollte ein Objekt sein, welches Eigenschaften (z.B. Farbe) und Verhalten (z.B. LockBlock) hat.

    public PlayingField(int blocks) {
        this.blocks = blocks;
        matrix = new int[blocks][blocks];
    }

    public void setView(IPlayingFieldView view) {
        this.view = view;
        this.view.setPlayingField(this);
    }

    public void setPersistence(GamePersistence persistence) {
        this.persistence = persistence;
    }

    public int get(int x, int y) {
        return matrix[x][y];
    }

    // Soll private bleiben, da nur die Game Engine die Matrix verändern darf.
    // -> Hab ich jetzt aber public machen müssen, damit Persistence darauf zugreifen kann.
    public void set(int x, int y, int value) {
        matrix[x][y] = value;
    }

    public void draw() {
        view.draw();
    }

    public void clear() {
        gameOver = false;
        for (int x = 0; x < blocks; x++) {
            for (int y = 0; y < blocks; y++) {
                set(x, y, 0);
            }
        }
        view.draw();
    }

    public boolean match(GamePiece teil, QPosition pos) {
        for (int x = teil.getMinX(); x <= teil.getMaxX(); x++) {
            for (int y = teil.getMinY(); y <= teil.getMaxY(); y++) {
                if (teil.filled(x, y)) {
                    int ax = pos.getX() + x - teil.getMinX();
                    int ay = pos.getY() + y - teil.getMinY();
                    if (ax < 0 || ax >= blocks || ay < 0 || ay >= blocks) {
                        return false;
                    }
                    int v = get(ax, ay);
                    if (v > 0 && v < 30) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Male Teil ins Spielfeld!
     */
    public void place(GamePiece teil, QPosition pos) { // old German method name: platziere
        for (int x = teil.getMinX(); x <= teil.getMaxX(); x++) {
            for (int y = teil.getMinY(); y <= teil.getMaxY(); y++) {
                if (teil.filled(x, y)) {
                    int ax = pos.getX() + x - teil.getMinX();
                    int ay = pos.getY() + y - teil.getMinY();
                    set(ax, ay, teil.getBlockType(x, y));
                }
            }
        }
        view.draw();
    }

    public FilledBlocks getFilledBlocks() {
        FilledBlocks blocks = new FilledBlocks();
        if (checkBlockOne()) {
            blocks.getFilledBlocks().add(1);
        }
        if (checkBlockTwo()) {
            blocks.getFilledBlocks().add(2);
        }
        if (checkBlockThree()) {
            blocks.getFilledBlocks().add(3);
        }
        if (checkBlockFour()) {
            blocks.getFilledBlocks().add(4);
        }
        if (checkBlockFive()) {
            blocks.getFilledBlocks().add(5);
        }
        if (checkBlockSix()) {
            blocks.getFilledBlocks().add(6);
        }
        if (checkBlockSeven()) {
            blocks.getFilledBlocks().add(7);
        }
        if (checkBlockEight()) {
            blocks.getFilledBlocks().add(8);
        }
        if (checkBlockNine()) {
            blocks.getFilledBlocks().add(9);
        }
        return blocks;
    }

    private boolean checkBlockOne() {
        boolean result = true;
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (get(x, y) == 0) {
                    result = false;
                }
            }
        }
        Log.d("blocksCheck", "result block 1 = " + result);
        return result;
    }

    private boolean checkBlockTwo() {
        boolean result = true;
        for (int x = 3; x < 6; x++) {
            for (int y = 0; y < 3; y++) {
                if (get(x, y) == 0) {
                    result = false;
                }
            }
        }
        Log.d("blocksCheck", "result block 2 = " + result);
        return result;
    }

    private boolean checkBlockThree() {
        boolean result = true;
        for (int x = 6; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                if (get(x, y) == 0) {
                    result = false;
                }
            }
        }
        Log.d("blocksCheck", "result block 3 = " + result);
        return result;
    }

    private boolean checkBlockFour() {
        boolean result = true;
        for (int x = 0; x < 3; x++) {
            for (int y = 3; y < 6; y++) {
                if (get(x, y) == 0) {
                    result = false;
                }
            }
        }
        Log.d("blocksCheck", "result block 4 = " + result);
        return result;
    }

    private boolean checkBlockFive() {
        boolean result = true;
        for (int x = 3; x < 6; x++) {
            for (int y = 3; y < 6; y++) {
                if (get(x, y) == 0) {
                    result = false;
                }
            }
        }
        Log.d("blocksCheck", "result block 5 = " + result);
        return result;
    }

    private boolean checkBlockSix() {
        boolean result = true;
        for (int x = 6; x < 9; x++) {
            for (int y = 3; y < 6; y++) {
                if (get(x, y) == 0) {
                    result = false;
                }
            }
        }
        Log.d("blocksCheck", "result block 6 = " + result);
        return result;
    }

    private boolean checkBlockSeven() {
        boolean result = true;
        for (int x = 0; x < 3; x++) {
            for (int y = 6; y < 9; y++) {
                if (get(x, y) == 0) {
                    result = false;
                }
            }
        }
        Log.d("blocksCheck", "result block 7 = " + result);
        return result;
    }

    private boolean checkBlockEight() {
        boolean result = true;
        for (int x = 3; x < 6; x++) {
            for (int y = 6; y < 9; y++) {
                if (get(x, y) == 0) {
                    result = false;
                }
            }
        }
        Log.d("blocksCheck", "result block 8 = " + result);
        return result;
    }

    private boolean checkBlockNine() {
        boolean result = true;
        for (int x = 6; x < 9; x++) {
            for (int y = 6; y < 9; y++) {
                if (get(x, y) == 0) {
                    result = false;
                }
            }
        }
        Log.d("blocksCheck", "result block 9 = " + result);
        return result;
    }


    public FilledRows getFilledRows() {
        FilledRows temp = new FilledRows();
        for (int y = 0; y < blocks; y++) {
            if (x_filled(y)) {
                temp.getYlist().add(y);
            }
        }
        for (int x = 0; x < blocks; x++) {
            if (y_filled(x)) {
                temp.getXlist().add(x);
            }
        }
        return temp;
    }

    private boolean x_filled(int y) {
        for (int x = 0; x < blocks; x++) {
            if (get(x, y) == 0) {
                return false;
            }
        }
        return true;
    }

    private boolean y_filled(int x) {
        for (int y = 0; y < blocks; y++) {
            if (get(x, y) == 0) {
                return false;
            }
        }
        return true;
    }

    public void clearRowsAndBlocks(FilledRows fr, FilledBlocks fb) {
        for (int x : fr.getXlist()) {
            for (int y = 0; y < blocks; y++) {
                set(x, y, 0);
            }
        }
        for (int y : fr.getYlist()) {
            for (int x = 0; x < blocks; x++) {
                set(x, y, 0);
            }
        }
        for (int block : fb.getFilledBlocks()) {
            switch (block) {
                case 1:
                    for (int x = 0; x < 3; x++) {
                        for (int y = 0; y < 3; y++) {
                            set(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 3; x < 6; x++) {
                        for (int y = 0; y < 3; y++) {
                            set(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    for (int x = 6; x < 9; x++) {
                        for (int y = 0; y < 3; y++) {
                            set(x, y, 0);
                        }
                    }
                    break;
                case 4:
                    for (int x = 0; x < 3; x++) {
                        for (int y = 3; y < 6; y++) {
                            set(x, y, 0);
                        }
                    }
                    break;
                case 5:
                    for (int x = 3; x < 6; x++) {
                        for (int y = 3; y < 6; y++) {
                            set(x, y, 0);
                        }
                    }
                    break;
                case 6:
                    for (int x = 6; x < 9; x++) {
                        for (int y = 3; y < 6; y++) {
                            set(x, y, 0);
                        }
                    }
                    break;
                case 7:
                    for (int x = 0; x < 3; x++) {
                        for (int y = 6; y < 9; y++) {
                            set(x, y, 0);
                        }
                    }
                    break;
                case 8:
                    for (int x = 3; x < 6; x++) {
                        for (int y = 6; y < 9; y++) {
                            set(x, y, 0);
                        }
                    }
                    break;
                case 9:
                    for (int x = 6; x < 9; x++) {
                        for (int y = 6; y < 9; y++) {
                            set(x, y, 0);
                        }
                    }
                    break;
            }
        }
        view.clearRowsAndBlocks(fr, fb);
    }

    public int getFilled() {
        int ret = 0;
        for (int x = 0; x < blocks; x++) {
            for (int y = 0; y < blocks; y++) {
                int value = get(x, y);
                if (value > 0 && value < 30) ret++;
            }
        }
        return ret;
    }

    public void gameOver() {
        gameOver = true;
        view.draw();
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void load() {
        persistence.get().load(this);
        view.draw();
    }

    public void save() {
        persistence.get().save(this);
    }

    public int getBlocks() {
        return blocks;
    }

    public void makeOldColor() {
        for (int x = 0; x < blocks; x++) {
            for (int y = 0; y < blocks; y++) {
                if (matrix[x][y] == BlockTypes.ONE_COLOR) {
                    matrix[x][y] = BlockTypes.OLD_ONE_COLOR;
                }
            }
        }
        view.oneColor();
    }
}
