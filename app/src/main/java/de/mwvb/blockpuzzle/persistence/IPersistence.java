package de.mwvb.blockpuzzle.persistence;

import de.mwvb.blockpuzzle.gamepiece.GamePiece;
import de.mwvb.blockpuzzle.playingfield.PlayingField;

public interface IPersistence {

    // GAME SPECIFIC ----

    // TODO Die setGameID() Methoden ist ein doofes Konstrukt. Ich sollte die Game-Daten besser zusammenhängend speichern und mich nicht auf eine zuvor gesetzte ID verlassen!

    /** gameDefinitionIndex: use selected game */

    void setGameID_oldGame();

    int loadScore();
    void saveScore(int punkte);
    int loadDelta();
    void saveDelta(int delta);
    int loadMoves();
    void saveMoves(int moves);
    boolean loadEmptyScreenBonusActive();
    void saveEmptyScreenBonusActive(boolean v);
    int loadHighScore();
    void saveHighScore(int punkte);
    int loadHighScoreMoves();
    void saveHighScoreMoves(int moves);
    /** Wenn true hat der Spieler es verkackt. */
    boolean loadGameOver();
    void saveGameOver(boolean gameOver);

    void load(PlayingField f);
    void save(PlayingField f);

    GamePiece load(int index);
    void save(int index, GamePiece p);

    void saveOwner(int score, int moves, String name);

    /**
     * @param playername null or empty value won't be saved
     */
    void savePlayerName(String playername);
    boolean isGameSoundOn();


    /**
     * @param v 0: no selection (show start screen), 1: old game, 2: Stone Wars game
     */
    void saveOldGame(int v);
    int loadOldGame();

}
