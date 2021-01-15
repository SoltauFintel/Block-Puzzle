package de.mwvb.blockpuzzle.gamepiece;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.mwvb.blockpuzzle.block.BlockTypes;
import de.mwvb.blockpuzzle.playingfield.QPosition;

/**
 * Game pieces can have different shapes with maximum size to 5x5 blocks and minimum size of 1x1.
 */
public class GamePiece {
    /** maximum width and height */
    public static final int max = 5;
    /** 1: x (to right), 2: y (to bottom) */
    private int[][] matrix = new int[max][max];
    /** rotate temp matrix */
    private int[][] neu = new int[max][max];
    private int minimumMoves = 0; // Wird nicht persistiert, da dieser Wert nicht mehr von Bedeutung ist, sobald der Spielstein im GamePieceView gelandet ist.
    private String name; // Wird nicht persistiert, da dieser Wert nicht mehr von Bedeutung ist, sobald der Spielstein im GamePieceView gelandet ist.

    public GamePiece() {
        for (int x = 0; x < max; x++) {
            for (int y = 0; y < max; y++) {
                matrix[x][y] = 0;
            }
        }
    }

    public GamePiece copy() {
        try {
            GamePiece n = (GamePiece) Class.forName(this.getClass().getName()).newInstance();
            for (int x = 0; x < max; x++) {
                System.arraycopy(matrix[x], 0, n.matrix[x], 0, max);
            }
            n.minimumMoves = minimumMoves;
            n.name = name;
            return n;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name == null ? this.getClass().getSimpleName() : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean filled(int x, int y) {
        return matrix[x][y] != 0;
    }

    public int getBlockType(int x, int y) {
        return matrix[x][y];
    }

    /**
     * @param value 0: leer, 1: Block
     *              weitere Werte für Boni denkbar:
     *              2: Dreh-Boni, 3: Dampfwalze, 4: Dynamit, 5: Bonuspunkte
     */
    public void setBlockType(int x, int y, int value) {
        matrix[x][y] = value;
    }

    public void color(int newBlockType) {
        for (int y = 0; y < GamePiece.max; y++) {
            for (int x = 0; x < GamePiece.max; x++) {
                if (matrix[x][y] != 0) {
                    matrix[x][y] = newBlockType;
                }
            }
        }
    }

    public int getMinX() {
        int ret = 0;
        for (int x = 0; x < max; x++) {
            for (int y = 0; y < max; y++) {
                if (filled(x, y)) {
                    return x;
                }
            }
        }
        return -1;
    }

    /** use <= */
    public int getMaxX() {
        for (int x = max - 1; x >= 0; x--) {
            for (int y = 0; y < max; y++) {
                if (filled(x, y)) {
                    return x;
                }
            }
        }
        return -1;
    }

    public int getMinY() {
        for (int y = 0; y < max; y++) {
            for (int x = 0; x < max; x++) {
                if (filled(x, y)) {
                    return y;
                }
            }
        }
        return -1;
    }

    /** use <= */
    public int getMaxY() {
        for (int y = max - 1; y >= 0; y--) {
            for (int x = 0; x < max; x++) {
                if (filled(x, y)) {
                    return y;
                }
            }
        }
        return -1;
    }

    public int getPunkte() {
        int ret = 0;
        for (int x = 0; x < max; x++) {
            for (int y = 0; y < max; y++) {
                if (filled(x, y)) {
                    ret++;
                }
            }
        }
        return ret;
    }

    public int getMinimumMoves() {
        return minimumMoves;
    }

    public void setMinimumMoves(int v) {
        minimumMoves = v;
    }

    @NotNull
    public List<QPosition> getAllFilledBlocks() {
        List<QPosition> filledBlocks = new ArrayList<>();
        final int minX = getMinX();
        final int maxX = getMaxX();
        final int minY = getMinY();
        final int maxY = getMaxY();
        if (minX > -1 && maxX > -1 && minY > -1 && maxY > -1) {
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    if (filled(x, y)) {
                        filledBlocks.add(new QPosition(x, y));
                    }
                }
            }
        }
        return filledBlocks;
    }


    @NotNull
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
