package de.mwvb.blockpuzzle.game;

import java.util.List;

import de.mwvb.blockpuzzle.block.BlockTypes;
import de.mwvb.blockpuzzle.gamepiece.GamePiece;
import de.mwvb.blockpuzzle.gamepiece.Holders;
import de.mwvb.blockpuzzle.gamepiece.INextGamePiece;
import de.mwvb.blockpuzzle.gamepiece.RandomGamePiece;
import de.mwvb.blockpuzzle.persistence.GamePersistence;
import de.mwvb.blockpuzzle.persistence.IPersistence;
import de.mwvb.blockpuzzle.playingfield.FilledRows;
import de.mwvb.blockpuzzle.playingfield.PlayingField;
import de.mwvb.blockpuzzle.playingfield.QPosition;

/**
 * Block Puzzle game logic
 * <p>
 * This is the old game and the base class for the Stone Wars game.
 */
// Eigentlich ist das eine GameEngine.
// Zu unübersichtlich, weil zu viele Methoden. Vll kann man ein Core-GameEngine machen und Listener? Vll auch die Initialisierung rausziehen?
public class Game {
    // Stammdaten (read only)
    public static final int blocks = 9;
    private final BlockTypes blockTypes = new BlockTypes(null);
    public static final int GPB_SCORE_FACTOR = 1;
    public static final int HITS_SCORE_FACTOR = 10;

    // Zustand
    protected final PlayingField playingField = new PlayingField(blocks);
    protected final Holders holders = new Holders();
    protected int punkte;
    protected int moves;
    protected boolean emptyScreenBonusActive = false;
    protected boolean gameOver = false; // wird nicht persistiert
    protected boolean won = false;
    private boolean dragAllowed = true;

    // Services
    protected GamePersistence gape; // "ga" for game + "pe" for persistence
    protected final IGameView view;
    protected INextGamePiece nextGamePiece;

    // Spielaufbau ----

    public Game(IGameView view) {
        this(view, null);
    }

    public Game(IGameView view, IPersistence persistence) {
        this.view = view;
        gape = new GamePersistence(persistence, view);
        playingField.setPersistence(gape);
        holders.setPersistence(gape);
    }

    // New Game ----

    // called by MainActivity.onResume()
    public void initGame() {
        initGameAndPersistence(); // Bei Stone Wars wird hier der Planet und die GameDefinition festgelegt.
        holders.setView(view);
        playingField.setView(view.getPlayingFieldView());
        nextGamePiece = getNextGamePieceGenerator();

        // Gibt es einen Spielstand?
        punkte = gape.loadScore();
        if (punkte < 0) { // Nein
            newGame(); // Neues Spiel starten!
        } else {
            loadGame(true, true); // Spielstand laden
        }
    }

    protected void initGameAndPersistence() {
        gape.setGameID_oldGame();
    }

    public boolean isNewGameButtonVisible() {
        return true;
    }

    protected INextGamePiece getNextGamePieceGenerator() {
        return new RandomGamePiece();
    }

    /**
     * Benutzer startet freiwillig oder nach GameOver neues Spiel.
     */
    public void newGame() {
        doNewGame();
        offer();
        save();
    }

    protected void doNewGame() {
        gameOver = false;
        gape.get().saveGameOver(gameOver);
        punkte = 0;
        gape.saveDelta(0);
        view.showScore(punkte, 0, gameOver);
        initNextGamePieceForNewGame();

        initPlayingField();

        moves = 0;
        view.showMoves(moves);
        emptyScreenBonusActive = false;
        gape.get().saveEmptyScreenBonusActive(emptyScreenBonusActive);

        holders.clearParking();
    }

    protected void initNextGamePieceForNewGame() {
        nextGamePiece.reset();
    }

    protected void initPlayingField() {
        playingField.clear();
    }

    protected void loadGame(boolean loadNextGamePiece, boolean checkGame) {
        gameOver = gape.loadGameOver();
        view.showScore(punkte, gape.loadDelta(), gameOver);
        moves = gape.loadMoves();
        emptyScreenBonusActive = gape.get().loadEmptyScreenBonusActive();
        view.showMoves(moves);
        if (loadNextGamePiece) {
            nextGamePiece.load();
        }
        playingField.load();
        holders.load();
        if (checkGame) {
            checkGame();
        }
    }

    /**
     * 3 neue zufällige Spielsteine anzeigen
     */
    protected void offer() { // old German method name: vorschlag
        for (int i = 1; i <= 3; i++) {
            holders.get(i).setGamePiece(nextGamePiece.next(blockTypes));
        }

        if (!gameOver && holders.is123Empty()) {
            // Ein etwaiger letzter geparkter Stein wird aus dem Spiel genommen, da dieser zur Vereinfachung keine Rolle mehr spielen soll.
            // Mag vorteilhaft oder unvorteilhaft sein, aber ich definier die Spielregeln einfach so!
            // Vorteilhaft weil man mit dem letzten Stein noch mehr Punkte als der Gegner bekommen könnte.
            // Unvorteilhaft weil man mit dem letzten Stein noch ein Spielfeld-voll-Game-over erzielen könnte.
            holders.clearParking();

            onGameOver();
        }
    }

    /**
     * Drop Aktion für Spielfeld oder Parking
     * <p>
     * Throws DoesNotWorkException
     */
    public void dispatch(boolean targetIsParking, int index, GamePiece teil, QPosition xy) {
        if (gameOver) {
            return;
        }
        boolean ret;
        if (targetIsParking) {
            ret = holders.park(index); // Drop Aktion für Parking Area
        } else {
            ret = place(index, teil, xy);
        }
        if (ret) {
            postDispatch();
        } else {
            throw new DoesNotWorkException();
        }
    }

    protected void postDispatch() {
        if (holders.is123Empty()) {
            offer();
        }
        checkGame();
        save();
    }

    /**
     * Drop Aktion für Spielfeld
     *
     * @return true wenn Spielstein platziert wurde, false wenn dies nicht möglich ist
     */
    private boolean place(int index, GamePiece teil, QPosition pos) { // old German name: platziere
        final int punkteVorher = punkte;
        boolean ret = playingField.match(teil, pos);
        if (ret) {
            playingField.place(teil, pos);
            holders.get(index).setGamePiece(null);

            // Gibt es gefüllte Rows?
            FilledRows f = playingField.getFilledRows();

            // Punktzahl erhöhen
            punkte += teil.getPunkte() * getGamePieceBlocksScoreFactor() + f.getHits() * getHitsScoreFactor();
            rowsAdditionalBonus(f.getXHits(), f.getYHits());

            // auto-gravity
            playingField.clearRows(f,null);
            // Action wird erst wenige Millisekunden später fertig!

            if (!emptyScreenBonusActive && playingField.getFilled() > (blocks * blocks * 0.40f)) { // More than 40% filled: fewGamePiecesOnThePlayingField bonus is active
                emptyScreenBonusActive = true;
                gape.get().saveEmptyScreenBonusActive(emptyScreenBonusActive);
                view.playSound(1); // play sound "more than 40%"
            }
            if (f.getHits() > 0) {
                fewGamePiecesOnThePlayingField();
            }

            int delta = punkte - punkteVorher;
            gape.saveDelta(delta);
            view.showScore(punkte, delta, gameOver);
            view.showMoves(++moves);
        }
        return ret;
    }

    protected int getGamePieceBlocksScoreFactor() {
        return GPB_SCORE_FACTOR;
    }

    protected int getHitsScoreFactor() {
        return HITS_SCORE_FACTOR;
    }

    protected void rowsAdditionalBonus(int xrows, int yrows) {
        switch (xrows + yrows) {
            case 0:
            case 1:
                break; // 0-1 kein Bonus
            // Bonuspunkte wenn mehr als 2 Rows gleichzeitig abgeräumt werden.
            // Fällt mir etwas schwer zu entscheiden wieviel Punkte das jeweils wert ist.
            case 2:
                punkte += 12;
                break;
            case 3:
                punkte += 17;
                break;
            case 4:
                punkte += 31;
                break;
            case 5:
                punkte += 44;
                break;
            default:
                punkte += 22;
                break;
        }
        if (xrows > 0 && yrows > 0) {
            punkte += 10;
        }
        // TODO Reihe mit gleicher Farbe (ohne oldOneColor) könnte weiteren Bonus auslösen.
    }

    private void fewGamePiecesOnThePlayingField() {
        if (!emptyScreenBonusActive) {
            return;
        }
        // Es gibt einen Bonus, wenn nach dem Abräumen von Rows nur noch wenige Spielsteine auf dem Spielfeld sind.
        int bonus = 0;
        switch (playingField.getFilled()) {
            case 0:
                bonus = 444;
                break;
            case 1:
                bonus = 111;
                break;
        }
        if (bonus > 0) {
            punkte += bonus;
            emptyScreenBonusActive = false;
            gape.get().saveEmptyScreenBonusActive(emptyScreenBonusActive);
            view.playSound(2); // play sound "empty screen bonus"
        }
    }

    /**
     * check for game over
     */
    protected void checkGame() {
        // es muss ein Spielstein noch rein gehen
        boolean a = moveImpossible(1);
        boolean b = moveImpossible(2);
        boolean c = moveImpossible(3);
        boolean d = moveImpossible(-1);
        if (a && b && c && d && !holders.isParkingFree()) {
            onGameOver();
        }
    }

    protected void onGameOver() {
        gameOver = true;
        updateHighScore();
        gape.saveDelta(0);
        view.showScore(punkte, 0, gameOver); // display game over text
        playingField.gameOver(); // wenn parke die letzte Aktion war
    }

    // TO-DO überdenken. Macht vermutlich nur für das "old game" Sinn.
    private void updateHighScore() {
        IPersistence px = gape.get();
        int highscore = px.loadHighScore();
        if (punkte > highscore || highscore <= 0) {
            px.saveHighScore(punkte);
            px.saveHighScoreMoves(moves);
        } else if (punkte == highscore) {
            int hMoves = px.loadHighScoreMoves();
            if (moves < hMoves || hMoves <= 0) {
                px.saveHighScoreMoves(moves);
            }
        }
    }

    private boolean moveImpossible(int index) {
        GamePiece teil = holders.get(index).getGamePiece();
        int result = moveImpossibleR(teil);
        if (result != 2) {
            holders.get(index).grey(result == 1 || result == -1);
        }
        return result > 0;
    }

    /**
     * @param teil game piece
     * @return 2: game piece view is empty, 1: game piece does not fit in (grey true!),
     * 0: game piece fits in (ro is 1, grey false!),
     * -1: game piece fits in (ro is > 1, grey true!).
     * >0: return true (move is impossible), else return false (move is possible).
     */
    int moveImpossibleR(GamePiece teil) {
        if (teil == null) {
            return 2; // GamePieceView is empty
        }
        for (int ro = 1; ro <= 4; ro++) { // try all 4 rotations
            for (int x = 0; x < blocks; x++) {
                for (int y = 0; y < blocks; y++) {
                    if (playingField.match(teil, new QPosition(x, y))) {
                        // GamePiece fits into playing field.
                        // original rotation (ro=1): not grey
                        // rotated (ro>1): grey, because with original rotation it doesn't fit
                        // and therefore I want to inform the player that he must rotate until
                        // it's not grey (or it's game over but there's a game over sound
                        // and he cannot rotate any more).
                        return ro > 1 ? -1 : 0; // Spielstein passt rein
                    }
                }
            }
        }
        return 1; // There's no space for the game piece in the playing field.
    }

    public boolean lessScore() {
        return punkte < 10;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int get(int x, int y) {
        return playingField.get(x, y);
    }

    public void save() {
        gape.saveScore(punkte);
        gape.saveMoves(moves);
        playingField.save();
        holders.save();
    }

    public boolean gameCanBeWon() {
        return false;
    }

    public boolean isWon() {
        return won;
    }

    public boolean isDragAllowed() {
        return dragAllowed;
    }

}
