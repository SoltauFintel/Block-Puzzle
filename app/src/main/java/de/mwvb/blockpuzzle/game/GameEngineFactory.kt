package de.mwvb.blockpuzzle.game

import de.mwvb.blockpuzzle.persistence.IPersistence

class GameEngineFactory {

    fun create(view: IGameView, per : IPersistence): Game {
       

            return Game(view)

    }
}