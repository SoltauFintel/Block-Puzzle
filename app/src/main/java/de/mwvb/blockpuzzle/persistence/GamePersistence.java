package de.mwvb.blockpuzzle.persistence;

import android.content.ContextWrapper;

import org.jetbrains.annotations.NotNull;

import de.mwvb.blockpuzzle.game.IGameView;

/**
 * Sicherstellen, dass der richtige Planet bzw. das richtige Game angesprochen wird. Die Persistence Implementierung ist da zu wackelig.
 * Ich verstehe noch nicht so richtig wie man Application State und Persistence bei Android macht.
 */
public class GamePersistence {
    private final IPersistence persistence;
    /** 0: non initialized mode, 1: old game, 2: Stone Wars */
    private int oldGame;
    /** only set in oldGame=2 mode */

    public GamePersistence(IPersistence persistence, IGameView view) {
        this.persistence = persistence == null ? new Persistence((ContextWrapper) view) : persistence;
        oldGame = 0;
    }

    @NotNull
    public IPersistence get() {
        prepare("get");
        return persistence;
    }

    public void setGameID_oldGame() {
        persistence.setGameID_oldGame();
        oldGame = 1;
    }



    private void prepare(String caller) {
        if (oldGame == 1) {
            persistence.setGameID_oldGame();
        } else {
            System.err.println("WARNING: GamePersistence in wrong mode! caller: " + caller);
        }
    }

    public int loadScore() {
        prepare("loadScore");
        return persistence.loadScore();
    }

    public void saveScore(int punkte) {
        prepare("saveScore");
        persistence.saveScore(punkte);
    }

    public void saveDelta(int v) {
        prepare("saveDelta");
        persistence.saveDelta(v);
    }

    public int loadDelta() {
        prepare("loadDelta");
        return persistence.loadDelta();
    }

    public int loadMoves() {
        prepare("loadMoves");
        return persistence.loadMoves();
    }

    public void saveMoves(int v) {
        prepare("saveMoves");
        persistence.saveMoves(v);
    }

    public boolean loadGameOver() {
        prepare("loadGameOver");
        return persistence.loadGameOver();
    }
}
