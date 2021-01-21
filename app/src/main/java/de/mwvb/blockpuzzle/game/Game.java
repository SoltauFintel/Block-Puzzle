package de.mwvb.blockpuzzle.game;

import de.mwvb.blockpuzzle.block.BlockTypes;
import de.mwvb.blockpuzzle.gamepiece.GamePiece;
import de.mwvb.blockpuzzle.gamepiece.Holders;
import de.mwvb.blockpuzzle.gamepiece.INextGamePiece;
import de.mwvb.blockpuzzle.gamepiece.RandomGamePiece;
import de.mwvb.blockpuzzle.persistence.GamePersistence;
import de.mwvb.blockpuzzle.persistence.IPersistence;
import de.mwvb.blockpuzzle.playingfield.FilledSectors;
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

    // Zustand
    protected final PlayingField playingField = new PlayingField(blocks);
    protected final Holders holders = new Holders();
    protected int score;
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
        score = gape.loadScore();
        if (score < 0) { // Nein
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
        score = 0;
        gape.saveDelta(0);
        view.showScore(score, 0, gameOver);
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
        view.showScore(score, gape.loadDelta(), gameOver);
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
     * Drop action for gameField or parking
     * <p>
     * Throws DoesNotWorkException
     */
    public void dispatch(boolean targetIsParking, int index, GamePiece teil, QPosition xy) {
        if (gameOver) {
            return;
        }
        boolean ret;
        if (targetIsParking) {
            ret = holders.park(index); // Drop action for Parking Area
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
     * Drop Action for gameField
     *
     * @return true if gamePiece was placed, false if it's not possible
     */
    private boolean place(int index, GamePiece piece, QPosition pos) {
        final int scoreBefore = score;
        boolean ret = playingField.match(piece, pos);
        if (ret) {
            playingField.place(piece, pos);
            holders.get(index).setGamePiece(null);

            // Are there filled rows?
            FilledRows fr = playingField.getFilledRows();
            FilledSectors fb = playingField.getFilledSectors();

            // auto-gravity
            playingField.clearRowsAndBlocks(fr, fb);

            // increase score
            score += (int) ((playingField.getFilledBlocks()*10) * getScoreFactor());

            int delta = score - scoreBefore;
            gape.saveDelta(delta);
            view.showScore(score, delta, gameOver);
            view.showMoves(++moves);
        }
        return ret;
    }

    private double getScoreFactor() {
        double scoreFactor = 1;
        if (playingField.getFilledBlocks()>9 && playingField.getFilledBlocks() < 19) {
            scoreFactor = 1.5;
        } else if (playingField.getFilledBlocks()>18 && playingField.getFilledBlocks() < 31) {
            scoreFactor = 2;
        } else if (playingField.getFilledBlocks()>30) {
            scoreFactor = 3;
        }
        return scoreFactor;
    }

//    private void fewGamePiecesOnThePlayingField() {
//        if (!emptyScreenBonusActive) {
//            return;
//        }
//        // Es gibt einen Bonus, wenn nach dem Abräumen von Rows nur noch wenige Spielsteine auf dem Spielfeld sind.
//        int bonus = 0;
//        switch (playingField.getFilled()) {
//            case 0:
//                bonus = 444;
//                break;
//            case 1:
//                bonus = 111;
//                break;
//        }
//        if (bonus > 0) {
//            score += bonus;
//            emptyScreenBonusActive = false;
//            gape.get().saveEmptyScreenBonusActive(emptyScreenBonusActive);
//            view.playSound(2); // play sound "empty screen bonus"
//        }
//    }

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
        view.showScore(score, 0, gameOver); // display game over text
        playingField.gameOver(); // wenn parke die letzte Aktion war
    }

    // TO-DO überdenken. Macht vermutlich nur für das "old game" Sinn.
    private void updateHighScore() {
        IPersistence px = gape.get();
        int highscore = px.loadHighScore();
        if (score > highscore || highscore <= 0) {
            px.saveHighScore(score);
            px.saveHighScoreMoves(moves);
        } else if (score == highscore) {
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
        return score < 1;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int get(int x, int y) {
        return playingField.get(x, y);
    }

    public void save() {
        gape.saveScore(score);
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
