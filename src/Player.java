import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Random;

/**
 * Class representing a player in the AI Arms Race game. Available functions
 * vary depending on whether or not the player is controlled by a human or
 * controlled by the computer.
 *
 * @author Paul Galatic
 */
public class Player {

    //  CONSTANTS

    final int TOTAL_TURNS = 10;
    final int RESEARCH_CAP = 10;
    final double BASE_WEIGHT = 5;
    final double BASE_ADD_ANIMOSITY = 2;

    final Random rand = new Random();

    //  STATE

    private String name;

    private boolean computer;
    private boolean victory = false;
    private boolean recentlySabotaged = false;
    private boolean recentlyNuked = false;

    private int researchPoints = 0;
    private HashSet<Opponent> opponents;
    private Opponent espionageTarget;
    private Opponent sabotageTarget;
    private Opponent nuclearTarget;

    private Model.Decision decisionOne;
    private Model.Decision decisionTwo;

    /** Constructor. Initializes state to provided values.
     *
     * @param name: the name of the country this Player represents
     * @param computer: whether or not this player is a computer
     */
    public Player(String name, boolean computer){
        this.name = name;
        this.computer = computer;
    }

    /** As not all opponents can be added until they are generated, this
     * function is to be called by the Model once all Player objects are
     * initialized.
     *
     * @param opponents: the set of Players to add to this Player's list
     *      of adversaries
     */
    public void addOpponents(HashSet<Player> opponents){
        if (opponents == null){ opponents = new HashSet<>(); }
        for (Player p : opponents){
            opponents.add(p);
        }
    }

    /** Sets the Player's current Decisions to be whatever the human decides
     * they should be.
     *
     * @param d1: the Player's first decision
     * @param d2: the player's second decision */
    public void playerChooseDecision(final Model.Decision d1, final Model.Decision d2){
        if (computer){
            throw new InputMismatchException("Calling playerChooseDecision() on a CPU player."){};
        }

        decisionOne = d1;
        decisionTwo = d2;
    }

    /** Runs a simple weighted probability algorithm to have the computer make
     * a "reasonable" next move based on what it knows about the condition of
     * its Player opponents. It sets the Decision variables based on this
     * evaluation.
     *
     * @param turn: the current turn. The algorithm makes use of the turn
     *      variable to adjust the weight of certain actions, as different
     *      actions are more relevant at different portions of the game.
     */
    public void computerChooseDecision(final int turn){
        if (!computer){
            throw new InputMismatchException("Calling computerChooseDecision() on a real player."){};
        }

        double  researchWeight = BASE_WEIGHT,
                espionageWeight = 0,
                sabotageWeight = 0,
                nuclearWeight = 0;
        int maxTurnsSinceLastEspionage = 0;
        espionageTarget = null;

        researchWeight += (TOTAL_TURNS - turn);
        for (Opponent o : opponents){
            espionageWeight += o.turnsSinceLastEspionage;
            if (o.turnsSinceLastEspionage > maxTurnsSinceLastEspionage){
                maxTurnsSinceLastEspionage = o.turnsSinceLastEspionage;
                espionageTarget = o;
            }
        }

    }


    /** Simulates the passing of a turn, and changes the State of the Player
     * based on what decisions were chosen.*/
    public void passTurn(){
        if (        decisionOne == Model.Decision.RESEARCH ||
                    decisionTwo == Model.Decision.RESEARCH) {
            researchPoints++;
            if (researchPoints >= RESEARCH_CAP) {
                victory = true;
            }
        }else if (  decisionOne == Model.Decision.ESPIONAGE ||
                    decisionTwo == Model.Decision.ESPIONAGE){
            espionageTarget.lastKnownResearchPoints = espionageTarget.player.getResearchPoints();
        }else if (  decisionOne == Model.Decision.SABOTAGE ||
                    decisionTwo == Model.Decision.SABOTAGE){
            sabotageTarget.player.getSabotaged();
        }else if (  decisionOne == Model.Decision.NUCLEAR &&
                    decisionTwo == Model.Decision.NUCLEAR){

        }
    }

    /** Returns the current level of a Player's research points. */
    public int getResearchPoints(){
        return researchPoints;
    }

    /** Run when the Player is sabotaged by another Player. */
    private void getSabotaged(){
        int result = rand.nextInt(2);
        if (result == 0){
            researchPoints--;
            recentlySabotaged = true;
        }
    }



    /** Returns the pair of Decision to the Model for reporting purposes. */
    public Model.Decision[] getDecisions(){
        Model.Decision[] decisions = {decisionOne, decisionTwo};
        return decisions;
    }

    /** A class representing an Opponent, which is distinct from a Player only
     * insofar as the computer having to make decisions about which Players to
     * prioritize spying on, sabotaging, or launching nuclear weapons against.
     */
    private class Opponent{

        private int turnsSinceLastEspionage = 0;
        private int lastKnownResearchPoints = 0;
        private int animosityLevel = 0;
        private int threatLevel = 0;
        private Player player;

        private Opponent(Player p){
            this.player = p;
        }

        private void resetAnimosity(){
            animosityLevel = 0;
        }

        private void resetThreat(){
            threatLevel = 0;
        }

    }

}
