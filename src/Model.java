/**
 * Model representing the overall State of the game, without the clutter of
 * State held by the different Players. Communicates with the View to display
 * relevant information.
 */
public class Model {

    /** Represents the different Decisions that a Player can make. */
    public enum Decision{
        RESEARCH, ESPIONAGE, SABOTAGE, NUCLEAR, NONE
    }


}

