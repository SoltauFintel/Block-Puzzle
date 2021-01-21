package de.mwvb.blockpuzzle.playingfield;

import de.mwvb.blockpuzzle.gamepiece.GamePiece;
import de.mwvb.blockpuzzle.persistence.GamePersistence;
import timber.log.Timber;

public class PlayingField {

    private final int blocks;
    private int filledBlocks;

    /**
     * 1: x (to the right), 2: y (down)
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

    public boolean match(GamePiece gamePiece, QPosition pos) {
        for (int x = gamePiece.getMinX(); x <= gamePiece.getMaxX(); x++) {
            for (int y = gamePiece.getMinY(); y <= gamePiece.getMaxY(); y++) {
                if (gamePiece.filled(x, y)) {
                    int ax = pos.getX() + x - gamePiece.getMinX();
                    int ay = pos.getY() + y - gamePiece.getMinY();
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
     * draw gamePiece on the playingField
     */
    public void place(GamePiece gamePiece, QPosition pos) { // old German method name: platziere
        for (int x = gamePiece.getMinX(); x <= gamePiece.getMaxX(); x++) {
            for (int y = gamePiece.getMinY(); y <= gamePiece.getMaxY(); y++) {
                if (gamePiece.filled(x, y)) {
                    int ax = pos.getX() + x - gamePiece.getMinX();
                    int ay = pos.getY() + y - gamePiece.getMinY();
                    set(ax, ay, gamePiece.getBlockType(x, y));
                }
            }
        }
        view.draw();
    }

    public FilledSectors getFilledSectors() {
        FilledSectors sectors = new FilledSectors();
        if (checkSectorOne()) {
            sectors.getFilledSectors().add(1);
        }
        if (checkSectorTwo()) {
            sectors.getFilledSectors().add(2);
        }
        if (checkSectorThree()) {
            sectors.getFilledSectors().add(3);
        }
        if (checkSectorFour()) {
            sectors.getFilledSectors().add(4);
        }
        if (checkSectorFive()) {
            sectors.getFilledSectors().add(5);
        }
        if (checkSectorSix()) {
            sectors.getFilledSectors().add(6);
        }
        if (checkSectorSeven()) {
            sectors.getFilledSectors().add(7);
        }
        if (checkSectorEight()) {
            sectors.getFilledSectors().add(8);
        }
        if (checkSectorNine()) {
            sectors.getFilledSectors().add(9);
        }
        return sectors;
    }

    private boolean checkSectorOne() {
        boolean result = true;
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (get(x, y) == 0) {
                    result = false;
                }
            }
        }
        Timber.d("result Sector 1 = %s", result);
        return result;
    }

    private boolean checkSectorTwo() {
        boolean result = true;
        for (int x = 3; x < 6; x++) {
            for (int y = 0; y < 3; y++) {
                if (get(x, y) == 0) {
                    result = false;
                }
            }
        }
        Timber.d("result Sector 2 = %s", result);
        return result;
    }

    private boolean checkSectorThree() {
        boolean result = true;
        for (int x = 6; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                if (get(x, y) == 0) {
                    result = false;
                }
            }
        }
        Timber.d("result Sector 3 = %s", result);
        return result;
    }

    private boolean checkSectorFour() {
        boolean result = true;
        for (int x = 0; x < 3; x++) {
            for (int y = 3; y < 6; y++) {
                if (get(x, y) == 0) {
                    result = false;
                }
            }
        }
        Timber.d("result Sector 4 = %s", result);
        return result;
    }

    private boolean checkSectorFive() {
        boolean result = true;
        for (int x = 3; x < 6; x++) {
            for (int y = 3; y < 6; y++) {
                if (get(x, y) == 0) {
                    result = false;
                }
            }
        }
        Timber.d("result Sector 5 = %s", result);
        return result;
    }

    private boolean checkSectorSix() {
        boolean result = true;
        for (int x = 6; x < 9; x++) {
            for (int y = 3; y < 6; y++) {
                if (get(x, y) == 0) {
                    result = false;
                }
            }
        }
        Timber.d("result Sector 6 = %s", result);
        return result;
    }

    private boolean checkSectorSeven() {
        boolean result = true;
        for (int x = 0; x < 3; x++) {
            for (int y = 6; y < 9; y++) {
                if (get(x, y) == 0) {
                    result = false;
                }
            }
        }
        Timber.d("result Sector 7 = %s", result);
        return result;
    }

    private boolean checkSectorEight() {
        boolean result = true;
        for (int x = 3; x < 6; x++) {
            for (int y = 6; y < 9; y++) {
                if (get(x, y) == 0) {
                    result = false;
                }
            }
        }
        Timber.d("result Sector 8 = %s", result);
        return result;
    }

    private boolean checkSectorNine() {
        boolean result = true;
        for (int x = 6; x < 9; x++) {
            for (int y = 6; y < 9; y++) {
                if (get(x, y) == 0) {
                    result = false;
                }
            }
        }
        Timber.d("result Sector 9 = %s", result);
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

    public void clearRowsAndBlocks(FilledRows fr, FilledSectors fb) {
        filledBlocks = 0;
        for (int x : fr.getXlist()) {
            for (int y = 0; y < blocks; y++) {
                if (get(x, y) == 1) {
                    filledBlocks++;
                }
                set(x, y, 0);
            }
        }
        for (int y : fr.getYlist()) {
            for (int x = 0; x < blocks; x++) {
                if (get(x, y) == 1) {
                    filledBlocks++;
                }
                set(x, y, 0);
            }
        }
        for (int block : fb.getFilledSectors()) {
            switch (block) {
                case 1:
                    for (int x = 0; x < 3; x++) {
                        for (int y = 0; y < 3; y++) {
                            if (get(x, y) ==1) {
                                filledBlocks++;
                            }
                            set(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 3; x < 6; x++) {
                        for (int y = 0; y < 3; y++) {
                            if (get(x, y) ==1) {
                                filledBlocks++;
                            }
                            set(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    for (int x = 6; x < 9; x++) {
                        for (int y = 0; y < 3; y++) {
                            if (get(x, y) ==1) {
                                filledBlocks++;
                            }
                            set(x, y, 0);
                        }
                    }
                    break;
                case 4:
                    for (int x = 0; x < 3; x++) {
                        for (int y = 3; y < 6; y++) {
                            if (get(x, y) ==1) {
                                filledBlocks++;
                            }
                            set(x, y, 0);
                        }
                    }
                    break;
                case 5:
                    for (int x = 3; x < 6; x++) {
                        for (int y = 3; y < 6; y++) {
                            if (get(x, y) ==1) {
                                filledBlocks++;
                            }
                            set(x, y, 0);
                        }
                    }
                    break;
                case 6:
                    for (int x = 6; x < 9; x++) {
                        for (int y = 3; y < 6; y++) {
                            if (get(x, y) ==1) {
                                filledBlocks++;
                            }
                            set(x, y, 0);
                        }
                    }
                    break;
                case 7:
                    for (int x = 0; x < 3; x++) {
                        for (int y = 6; y < 9; y++) {
                            if (get(x, y) ==1) {
                                filledBlocks++;
                            }
                            set(x, y, 0);
                        }
                    }
                    break;
                case 8:
                    for (int x = 3; x < 6; x++) {
                        for (int y = 6; y < 9; y++) {
                            if (get(x, y) ==1) {
                                filledBlocks++;
                            }
                            set(x, y, 0);
                        }
                    }
                    break;
                case 9:
                    for (int x = 6; x < 9; x++) {
                        for (int y = 6; y < 9; y++) {
                            if (get(x, y) ==1) {
                                filledBlocks++;
                            }
                            set(x, y, 0);
                        }
                    }
                    break;
            }
        }
        Timber.d("filled blocks = %s", filledBlocks);
    }

    public int getFilledBlocks() {
        return filledBlocks;
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
}
