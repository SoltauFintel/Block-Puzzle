package de.mwvb.blockpuzzle.playingfield;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import de.mwvb.blockpuzzle.R;
import de.mwvb.blockpuzzle.block.BlockDrawParameters;
import de.mwvb.blockpuzzle.block.BlockDrawerStrategy;
import de.mwvb.blockpuzzle.block.BlockTypes;
import de.mwvb.blockpuzzle.block.ColorBlockDrawer;
import de.mwvb.blockpuzzle.block.EmptyBlockDrawer;
import de.mwvb.blockpuzzle.block.IBlockDrawer;
import de.mwvb.blockpuzzle.game.Game;
import de.mwvb.blockpuzzle.sound.SoundService;

/**
 * playingField is 9*9 rectangle
 * You can drag gamePieces onto playingField.
 * each block on the playingField has a value: 0 = empty, 1 = occupied.
 *
 * PlayingField's size is 300*300dp. At the bottom it's 2 rows bigger (60dp) in order that
 * Drag'n'Drop works correctly
 */
public class PlayingFieldView extends View implements IPlayingFieldView {
    public static final int w = 300; // dp
    Bitmap field;
    Rect rectDst;
    Paint paint;
    private final SoundService soundService = new SoundService();
    private FilledRows filledRows;
    private FilledSectors filledBlocks;
    private int mode = 0;
    private final IBlockDrawer empty = new EmptyBlockDrawer(this);
    private IBlockDrawer grey;
    private BlockTypes blockTypes = new BlockTypes(this);
    private PlayingField playingField;
    private final BlockDrawParameters p = new BlockDrawParameters();

    public PlayingFieldView(Context context) {
        super(context);
        init(context);
    }

    public PlayingFieldView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        soundService.init(context);

        final float f = getResources().getDisplayMetrics().density;
        int px = (int) (300 * f)-10;

        field = BitmapFactory.decodeResource(getResources(), R.drawable.field);
        rectDst = new Rect(0, 0, px, px);
        paint = new Paint();

        grey = ColorBlockDrawer.byRColor(this, R.color.colorGrey, R.color.colorGrey_i, R.color.colorGrey_ib);
    }

    @Override
    public void setPlayingField(PlayingField playingField) {
        this.playingField = playingField;
    }

    @Override
    public SoundService getSoundService() {
        return soundService;
    }

    @Override
    public void draw() {
        invalidate();
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        setBackgroundResource(R.drawable.field);

        canvas.drawBitmap(field, null, rectDst, paint);
        drawBlocks(canvas);
        super.onDraw(canvas);
    }

    private void drawBlocks(Canvas canvas) {
        p.setCanvas(canvas);
        p.setDragMode(true);
        p.setF(getResources().getDisplayMetrics().density);
        p.setBr(w / Game.blocks); // 60px
        final BlockDrawerStrategy m = getMatrixGet();
        for (int x = 0; x < Game.blocks; x++) {
            for (int y = 0; y < Game.blocks; y++) {
                m.get(x, y).draw(x * p.getBr(), y * p.getBr(), p);
            }
        }
    }

    private BlockDrawerStrategy getMatrixGet() {
        if (playingField.isGameOver()) {
            return new BlockDrawerStrategy() {
                @Override
                public IBlockDrawer get(int x, int y) {
                    if (playingField.get(x, y) > 0) return grey;
                    return empty;
                }
            };
        }
        final BlockDrawerStrategy std = getStdMatrixGet();
        return std;

    }

    private BlockDrawerStrategy getStdMatrixGet() {
        return new BlockDrawerStrategy() {
            @Override
            public IBlockDrawer get(int x, int y) {
                int b = playingField.get(x, y);
                if (b >= 1) return blockTypes.getBlockDrawer(b);
                return empty;
            }
        };
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        soundService.destroy();
    }
}
