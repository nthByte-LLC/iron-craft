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
     * The player has completed the game.
     */
    GAME_COMPLETE;
}
