package de.mwvb.blockpuzzle.gamepiece;

import de.mwvb.blockpuzzle.block.BlockTypes;

/**
 * Next game piece strategy
 */
public interface INextGamePiece {

    GamePiece next(BlockTypes blockTypes);

    void reset();

    void load();
}
