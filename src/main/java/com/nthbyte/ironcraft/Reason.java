package com.nthbyte.ironcraft;

/**
 * The different reasons the game has stopped.
 */
public enum Reason {

    /**
     * The player has ran out of time to complete the game (You currently only get 7 minutes).
     */
    OUT_OF_TIME,

    /**
     * The player has completed a round of the game.
     */
    ROUND_COMPLETE,

    /**
     * The player has completed a specified amount of rounds to complete the game (Currently 3)
     */
    GAME_COMPLETE

}
