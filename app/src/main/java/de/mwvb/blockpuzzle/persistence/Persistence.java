package de.mwvb.blockpuzzle.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import de.mwvb.blockpuzzle.gamepiece.GamePiece;
import de.mwvb.blockpuzzle.playingfield.PlayingField;

// Ich möchte das Laden und Speichern an _einer_ Stelle haben, damit ich es schneller finden kann.
// Ordner: /data/data/YOUR_PACKAGE_NAME/shared_prefs/YOUR_PREFS_NAME.xml
// TODO Neue Idee für Persistenz: Mehr mit Objekten arbeiten, die per GSON seralisiert werden. Speichern in Files. Denn so komm ich überall an die Persistenz dran.
//      Ich brauch dann eine Migration beim Programmstart.
public class Persistence implements IPersistence {
    private static final String NAME = "GAMEDATA_2";
    // Global data ----
    private static final String GLOBAL_OLD_GAME = "/oldGame";
    private static final String GLOBAL_PLAYERNAME = "/playername";
    private static final String GLOBAL_GAME_SOUNDS = "/gameSounds";
    // Game specific data ----
    private static final String SCORE = "score";
    private static final String DELTA = "delta";
    private static final String MOVES = "moves";
    private static final String EMPTY_SCREEN_BONUS_ACTIVE = "emptyScreenBonusActive";
    private static final String GAME_OVER = "gameOver";
    private static final String HIGHSCORE_SCORE = "highscore";
    private static final String HIGHSCORE_MOVES = "highscoreMoves";
    private static final String GAMEPIECEVIEW = "gamePieceView";
    private static final String PLAYINGFIELD = "playingField";
    private static final String OWNER_NAME = "owner_name";
    private static final String OWNER_SCORE = "owner_score"; // enemy score
    private static final String OWNER_MOVES = "owner_moves";

    private Context owner;
    private SharedPreferences __pref; // only access by pref() !
    private String prefix = "";

    // Die Kommentare sind mglw. tlw. überholt (22.10.20)

    public Persistence(Context owner) {
        this.owner = owner;
        // Ich kann hier nicht sofort auf getSharedPreferences() zugreifen.
    }

    private SharedPreferences pref() {
        if (__pref == null) {
            __pref = owner.getSharedPreferences(NAME, Context.MODE_PRIVATE);
            owner = null; // not need any more
        }
        return __pref;
    }

    @Override
    public void setGameID_oldGame() {
        prefix = "";
    }



    private String name(String name) {
        if (name.startsWith("/")) { // global parameter -> don't prepend game-specific suffix
            return name.substring(1);
        }
        return prefix + name;
    }

    /**
     * Es wird gespeichert wenn:
     * neues Spiel (empty parking), offer (set), place (empty), parken (move)
     */
    @Override
    public void save(int index, GamePiece p) {
        StringBuilder d = new StringBuilder();
        if (p != null) {
            String k = "";
            for (int x = 0; x < GamePiece.max; x++) {
                for (int y = 0; y < GamePiece.max; y++) {
                    d.append(k);
                    k = ",";
                    d.append(p.getBlockType(x, y));
                }
            }
        }
        save(GAMEPIECEVIEW + index, d);
    }

    /**
     * Es wird geladen wenn:
     * MainActivity.onCreate
     */
    @Override
    public GamePiece load(int index) {
        GamePiece p = null;
        String d = getString(GAMEPIECEVIEW + index);
        if (d != null && !d.isEmpty()) {
            p = new GamePiece();
            String[] w = d.split(",");
            int i = 0;
            for (int x = 0; x < GamePiece.max; x++) {
                for (int y = 0; y < GamePiece.max; y++) {
                    p.setBlockType(x, y, Integer.parseInt(w[i++]));
                }
            }
        }
        return p;
    }

    /**
     * Es wird gespeichert wenn:
     * clear (newGame), place, gravitation (place), clearRows (place)
     */
    @Override
    public void save(PlayingField f) {
        StringBuilder d = new StringBuilder();
        String k = "";
        final int blocks = f.getBlocks();
        for (int x = 0; x < blocks; x++) {
            for (int y = 0; y < blocks; y++) {
                d.append(k);
                k = ",";
                d.append(f.get(x, y));
            }
        }
        save(PLAYINGFIELD, d);
    }

    /**
     * Es wird geladen wenn:
     * MainActivity.onCreate
     */
    @Override
    public void load(PlayingField f) {
        String d = getString(PLAYINGFIELD);
        final int blocks = f.getBlocks();
        if (d == null || d.isEmpty()) {
            for (int x = 0; x < blocks; x++) {
                for (int y = 0; y < blocks; y++) {
                    f.set(x, y, 0);
                }
            }
        } else {
            String[] w = d.split(",");
            int i = 0;
            for (int x = 0; x < blocks; x++) {
                for (int y = 0; y < blocks; y++) {
                    f.set(x, y, Integer.parseInt(w[i++]));
                }
            }
        }
    }

    /**
     * Es wird geladen wenn:
     * MainActivity.onCreate
     * @return negative value means that there is no GAMEDATA
     */
    @Override
    public int loadScore() {
        return getInt(SCORE, -9999);
    }

    /**
     * Es wird gespeichert wenn:
     * place (punkte erhöht), newGame (punkte=0)
     */
    @Override
    public void saveScore(int punkte) {
        putInt(SCORE, punkte);
    }

    @Override
    public int loadDelta() {
        return getInt(DELTA, 0);
    }

    @Override
    public void saveDelta(int delta) {
        putInt(DELTA, delta);
    }

    @Override
    public int loadHighScore() {
        return getInt(HIGHSCORE_SCORE, 0);
    }

    @Override
    public void saveHighScore(int punkte) {
        putInt(name(HIGHSCORE_SCORE), punkte);
    }

    @Override
    public int loadHighScoreMoves() {
        return getInt(HIGHSCORE_MOVES, 0);
    }

    @Override
    public void saveHighScoreMoves(int moves) {
        putInt(HIGHSCORE_MOVES, moves);
    }

    @Override
    public int loadMoves() {
        return getInt(MOVES, 0);
    }

    @Override
    public void saveMoves(int moves) {
        putInt(MOVES, moves);
    }

    @Override
    public boolean loadEmptyScreenBonusActive() {
        return getBoolean(EMPTY_SCREEN_BONUS_ACTIVE);
    }

    @Override
    public void saveEmptyScreenBonusActive(boolean v) {
        putBoolean(EMPTY_SCREEN_BONUS_ACTIVE, v);
    }

    @Override
    public void savePlayerName(String playername) {
        if (playername == null || playername.trim().isEmpty()) return;
        SharedPreferences.Editor edit = pref().edit();
        edit.putString(name(GLOBAL_PLAYERNAME), playername);
        edit.apply();
    }



    @Override
    public void saveOldGame(int v) {
        putInt(GLOBAL_OLD_GAME, v);
    }

    @Override
    public int loadOldGame() {
        return getInt(GLOBAL_OLD_GAME, 0);
    }

    @Override
    public boolean loadGameOver() {
        return getBoolean(GAME_OVER);
    }

    @Override
    public void saveGameOver(boolean gameOver) {
        putBoolean(GAME_OVER, gameOver);
    }

    @Override
    public boolean isGameSoundOn() {
        // default: with game sounds
        return getInt(GLOBAL_GAME_SOUNDS, 1) == 1;
    }

    @Override
    public void saveOwner(int score, int moves, String name) {
        putInt(OWNER_SCORE, score);
        putInt(OWNER_MOVES, moves);
        SharedPreferences.Editor edit = pref().edit();
        edit.putString(name(OWNER_NAME), name);
        edit.apply();
    }

    private String getString(String name) {
        return pref().getString(name(name), "");
    }

    private int getInt(String name, int defVal) {
        return pref().getInt(name(name), defVal);
    }

    private boolean getBoolean(String name) {
        return getInt(name, 0) == 1;
    }

    private void putInt(String name, int val) {
        SharedPreferences.Editor edit = pref().edit();
        edit.putInt(name(name), val);
        edit.apply();
    }


    private void putBoolean(String name, boolean val) {
        putInt(name, val ? 1 : 0);
    }

    // TODO überall diese Methode verwenden
    private void putString(String name, String value) {
        SharedPreferences.Editor edit = pref().edit();
        edit.putString(name(name), value);
        edit.apply();
    }

    private void save(String name, StringBuilder s) {
        putString(name, s.toString());
    }
}
