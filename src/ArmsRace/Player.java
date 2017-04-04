package ArmsRace;

import java.util.*;
import java.util.logging.Level;

import static ArmsRace.Main.LOGGER;

/**
 * Class representing a player in the AI Arms Race game. Available functions
 * vary depending on whether or not the player is controlled by a human or
 * controlled by the computer.
 *
 * @author Paul Galatic
 */
public class Player {

    //  CONSTANTS

    private Random seeder = Model.rand;
    private Random rand;
    private static final boolean DEBUG = true;

    private static final int TOTAL_TURNS = 10;
    private static final int TURN_NUCLEAR_AVAILABLE = 5;
    private static final int BASE_NUKE_DEFENSE = 0;
    private static final int BASE_WEIGHT = 2;
    private static final int BASE_ADD_THREAT = 2;
    private static final int BASE_ADD_EXTREME_THREAT = 5;
    private static final int BASE_NUCLEAR_THREAT_THRESHHOLD = 10;
    private static final double BASE_LOWER_ATTRIBUTE_FACTOR = 0.5;

    //  STATE

    private String ID;

    private boolean computer;

    private int researchPoints = 0;
    private ArrayList<Opponent> opponents;
    private Opponent espionageTargetOne;
    private Opponent espionageTargetTwo;
    private Opponent sabotageTargetOne;
    private Opponent sabotageTargetTwo;
    private Opponent nuclearTarget;
    private HashSet<Opponent> recentlySabotagedBy = new HashSet<>();
    private HashSet<Opponent> recentlyNukedBy = new HashSet<>();
    private HashSet<Opponent> recentlyNukeFailedBy = new HashSet<>();

    private Model.Decision decisionOne;
    private Model.Decision decisionTwo;

    /**
     * Constructor. Initializes state to provided values.
     *
     * @param ID: the name of the country this Player represents
     * @param computer: whether or not this player is a computer
     */
    public Player(String ID, boolean computer){
        this.ID = ID;
        this.computer = computer;
        this.rand = new Random();
        this.rand.setSeed(seeder.nextLong());
    }

    /**
     * As not all opponents can be added until they are generated, this
     * function is to be called by the Model once all Player objects are
     * initialized.
     *
     * @param opponents: the set of Players to add to this Player's list
     *      of adversaries
     */
    public void addOpponents(HashSet<Player> opp){
        if (opponents == null){ opponents = new ArrayList<>(); }
        for (Player p : opp){
            opponents.add(new Opponent(p));
        }
    }

    /**
     * Sets the Player's current Decisions to be whatever the human decides
     * they should be.
     *
     * @param d1: the Player's first decision
     * @param d2: the player's second decision 
     */
    public void playerChooseDecision(final Model.Decision d1, final Model.Decision d2){
        if (computer){
            throw new InputMismatchException("Calling playerChooseDecision() on a CPU player."){};
        }

        recentlySabotagedBy.clear();
        recentlyNukedBy.clear();
        recentlyNukeFailedBy.clear();

        decisionOne = d1;
        decisionTwo = d2;
    }

    /**
     * Set a player's target to whomever the Player decides to mess with.
     *
     * @param decision: the particular nature of the Player's action
     * @param target: the Player's target
     * @param firstAction: whether this is the Player's first or second action
     *                     (true if first, false if second)
     * */
    public void playerSetTarget(final Model.Decision decision, final Player target, boolean firstAction){
        if (firstAction) {
            switch (decision) {
                case ESPIONAGE:
                    espionageTargetOne = opponentLookup(target.ID);
                    break;
                case SABOTAGE:
                    sabotageTargetOne = opponentLookup(target.ID);
                    break;
                case NUCLEAR:
                    nuclearTarget = opponentLookup(target.ID);
                    break;
            }
        }else{
            switch (decision) {
                case ESPIONAGE:
                    espionageTargetTwo = opponentLookup(target.ID);
                    break;
                case SABOTAGE:
                    sabotageTargetTwo = opponentLookup(target.ID);
                    break;
            }
        }
    }

    /**
     * Runs a simple weighted probability algorithm to have the computer make
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

        int     currEspionageValue = 0,
                maxEspionageValue = 0,
                currSabotageValue = 0,
                maxSabotageValue = 0,
                currNuclearValue = 0,
                maxNuclearValue = 0;

        espionageTargetOne = null;
        espionageTargetTwo = null;
        sabotageTargetOne = null;
        sabotageTargetTwo = null;
        nuclearTarget = null;

        researchWeight += (TOTAL_TURNS - turn); // early / late game priority,
                                                // always a good choice

        for (Opponent o : opponents){
            // RE-EVALUATING OPPONENT THREAT LEVEL
            if (recentlySabotagedBy.contains(o)){
                recentlySabotagedBy.remove(o);
                o.updateThreatAmount(Model.Decision.SABOTAGE);
            }else if (recentlyNukedBy.contains(o)){
                recentlyNukedBy.remove(o);
                o.updateThreatAmount(Model.Decision.NUCLEAR);
            }else{
                o.updateThreatAmount(Model.Decision.NONE);
            }

            // ESPIONAGE DECISION PATH
            currEspionageValue = o.turnsSinceLastEspionage + o.threatLevel;
            espionageWeight += currEspionageValue;
            if (currEspionageValue > maxEspionageValue){
                maxEspionageValue = currEspionageValue;
                if (espionageTargetOne == null){
                    espionageTargetOne = o;
                }else if (espionageTargetTwo == null){
                    espionageTargetTwo = o;
                }else{
                    if (    espionageTargetOne.threatLevel +
                            espionageTargetOne.turnsSinceLastEspionage
                                            <
                            espionageTargetTwo.threatLevel +
                            espionageTargetTwo.turnsSinceLastEspionage){
                        espionageTargetOne = o;
                    }else{
                        espionageTargetTwo = o;
                    }
                }
            }

            // SABOTAGE DECISION PATH
            currSabotageValue = o.threatLevel;
            sabotageWeight += currSabotageValue;
            if (currSabotageValue > maxSabotageValue){
                maxSabotageValue = currSabotageValue;
                if (sabotageTargetOne == null){
                    sabotageTargetOne = o;
                }else if (sabotageTargetTwo == null){
                    sabotageTargetTwo = o;
                }else{
                    if (sabotageTargetOne.threatLevel < sabotageTargetTwo.threatLevel){
                        sabotageTargetOne = o;
                    }else{
                        sabotageTargetTwo = o;
                    }
                }
            }

            // NUCLEAR DECISION PATH
            if (    turn >= TURN_NUCLEAR_AVAILABLE &&
                    o.threatLevel > BASE_NUCLEAR_THREAT_THRESHHOLD){
                sabotageWeight = lowerWeight(sabotageWeight);
                currNuclearValue += o.threatLevel;
                nuclearWeight += currNuclearValue;
                if (currNuclearValue > maxNuclearValue){
                    maxNuclearValue = currNuclearValue;
                    nuclearTarget = o;
                }
            }
        }

        // negatives screw up the calculations
        researchWeight = researchWeight < 0 ? 0 : researchWeight;
        espionageWeight = espionageWeight < 0 ? 0 : espionageWeight;
        sabotageWeight = sabotageWeight < 0 ? 0 : sabotageWeight;
        nuclearWeight = nuclearWeight < 0 ? 0 : nuclearWeight;

        Map<Model.Decision, Double> options = new HashMap<>();
        options.put(Model.Decision.RESEARCH, researchWeight);
        options.put(Model.Decision.ESPIONAGE, espionageWeight);
        options.put(Model.Decision.SABOTAGE, sabotageWeight);
        options.put(Model.Decision.NUCLEAR, nuclearWeight);

        decisionOne = getWeightedRandom(options, rand);
        if (decisionOne == Model.Decision.NUCLEAR){
            decisionTwo = Model.Decision.NUCLEAR;
        }else{
            options.remove(Model.Decision.NUCLEAR);
            if (espionageTargetTwo == null){
                options.remove(Model.Decision.ESPIONAGE);
            }
            if (sabotageTargetTwo == null){
                options.remove(Model.Decision.SABOTAGE);
            }
            decisionTwo = getWeightedRandom(options, rand);
        }

    }

    /**
     * Algorithm to choose a weighted result. Credit to Martin L on
     * Stackoverflow.
     *
     * https://stackoverflow.com/questions/6737283/weighted-randomness-in-java
     *
     * @pre: param weights contains Decisions mapped to Doubles
     * @param weights: the map of weights and associated objects (Decisions)
     * @param random: the random number generator object
     */
    private <E> E getWeightedRandom(Map<E, Double> weights, Random random) {
        E result = null;
        double bestValue = Double.MAX_VALUE;

        for (E element : weights.keySet()) {
            double value = -Math.log(random.nextDouble()) / weights.get(element);

            if (value < bestValue) {
                bestValue = value;
                result = element;
            }
        }

        return result;
    }


    /**
     * Simulates the passing of a turn, and changes the State of the Player
     * based on what decisions were chosen.
     */
    public void passTurn(){

        // passive research gain
        researchPoints++;

        // assert that we have valid targets
        if (decisionOne == Model.Decision.ESPIONAGE){
            assert(espionageTargetOne != null);
        }else if (decisionOne == Model.Decision.SABOTAGE){
            assert(sabotageTargetOne != null);
        }else if (decisionOne == Model.Decision.NUCLEAR){
            assert(nuclearTarget != null);
        }

        // skip nuclear in this branch because it takes up both actions
        if (decisionTwo == Model.Decision.ESPIONAGE){
            assert(espionageTargetTwo != null);
        }else if (decisionTwo == Model.Decision.SABOTAGE){
            assert(sabotageTargetTwo != null);
        }

        // If nuclear, do this event chain
        // Otherwise, go on to execute the first decision, then the second

        if (    decisionOne == Model.Decision.NUCLEAR &&
                decisionTwo == Model.Decision.NUCLEAR){
            debugPrint(String.format("%s chose NUCLEAR: %s", this.ID, nuclearTarget.getID()));
            nuclearTarget.player.nukedBy(this.ID);
        }else{
            // EXECUTE DECISION ONE
            switch (decisionOne){
                case RESEARCH:
                    debugPrint(String.format("%s chose RESEARCH ", this.ID));
                    int i = rand.nextInt(4);
                    if (i > 0){ researchPoints++; }
                    else if (!computer){ System.out.println("RESEARCH FAILED!"); }
                    else { debugPrint(String.format("%s: FAILED RESEARCH", this.ID)); }
                    break;
                case ESPIONAGE:
                    debugPrint(String.format("%s chose ESPIONAGE: %s", this.ID, espionageTargetOne.getID()));
                    espionageTargetOne.lastKnownResearchPoints =
                            espionageTargetOne.player.getResearchPoints();
                    espionageTargetOne = null;
                    break;
                case SABOTAGE:
                    debugPrint(String.format("%s chose SABOTAGE: %s", this.ID, sabotageTargetOne.getID()));
                    sabotageTargetOne.player.sabotagedBy(ID);
                    sabotageTargetOne = null;
                    break;
                default:
                    throw new InputMismatchException("Bad decisionOne for " + ID){};
            }

            // EXECUTE DECISION TWO
            switch (decisionTwo){
                case RESEARCH:
                    debugPrint(String.format("%s chose RESEARCH", this.ID));
                    int i = rand.nextInt(4);
                    if (i > 0){ researchPoints++; }
                    else if (!computer){ System.out.println("RESEARCH FAILED!"); }
                    else { debugPrint(String.format("%s: FAILED RESEARCH", this.ID)); }
                    break;
                case ESPIONAGE:
                    debugPrint(String.format("%s chose ESPIONAGE: %s", this.ID, espionageTargetTwo.getID()));
                    espionageTargetTwo.lastKnownResearchPoints =
                            espionageTargetTwo.player.getResearchPoints();
                    espionageTargetTwo = null;
                    break;
                case SABOTAGE:
                    debugPrint(String.format("%s chose SABOTAGE: %s", this.ID, sabotageTargetTwo.getID()));
                    sabotageTargetTwo.player.sabotagedBy(ID);
                    sabotageTargetTwo = null;
                    break;
                default:
                    throw new InputMismatchException("Bad decisionTwo for " + ID){};

            }


        }



    }

    /**
     * Run when the Player is sabotaged by another Player. This function is
     * inefficient, but the number of Opponents should be kept low enough
     * for that not to matter.
     *
     * @param ID: for Opponent lookup purposes, to know who to blame
     */
    private void sabotagedBy(String ID){
        int result = rand.nextInt(2);
        if (result == 0){
            researchPoints--;
        }else{
            researchPoints -= 2;
        }
        recentlySabotagedBy.add(opponentLookup(ID));
    }

    /**
     * Run when the Player is nuked by another Player.
     *
     * @param ID: for Opponent lookup purposes, to know who to blame
     */
    private void nukedBy(String ID){
        Opponent attacker = opponentLookup(ID);

        int defenseChance = researchPoints + BASE_NUKE_DEFENSE;
        int attackChance = attacker.player.getResearchPoints();

        int strikeLanded = rand.nextInt(attackChance + defenseChance);
        if (strikeLanded > defenseChance){
            researchPoints /= 2;
            recentlyNukedBy.add(attacker);
        }else{
            recentlyNukeFailedBy.add(attacker);
        }
    }

    /** Lowers the weight of an attribute. */
    private double lowerWeight(double weight){
        return weight * BASE_LOWER_ATTRIBUTE_FACTOR;
    }

    /** Looks up an Opponent based on their name. */
    public Opponent opponentLookup(String ID){
        Opponent result = null;
        for (Opponent o : opponents){
            if (o.player.ID.equals(ID)){
                result = o;
                break;
            }
        }
        return result;
    }

    /** Returns the current level of a Player's research points. */
    public int getResearchPoints(){
        return researchPoints;
    }

	/** Returns the ID of the Player. */
	public String getID(){
		return ID;
	}

    /** Returns the pair of Decision to the Model for reporting purposes. */
    public Model.Decision[] getDecisions(){
        Model.Decision[] decisions = {decisionOne, decisionTwo};
        return decisions;
    }

    public void printOpponentsValues(){
        System.out.println("--LAST KNOWN RESEARCH VALUES--");
        for (Opponent o : opponents){
            System.out.println(String.format("%s: %d", o.getID(), o.lastKnownResearchPoints));
        }
        System.out.println();
    }

    public void damageReport(){
        if (!recentlySabotagedBy.isEmpty() || !recentlyNukedBy.isEmpty() || !recentlyNukeFailedBy.isEmpty()) {
            System.out.println("--WARNING: RECENTLY ATTACKED!--");
            for (Opponent o : recentlySabotagedBy){
                System.out.println(String.format("SABOTAGED BY: %s", o.getID()));
            }
            for (Opponent o : recentlyNukedBy){
                System.out.println(String.format("NUKED BY: %s", o.getID()));
            }
            for (Opponent o : recentlyNukeFailedBy){
                System.out.println(String.format("ATTEMPTED NUCLEAR STRIKE BY: %s", o.getID()));
            }
            System.out.println("-------------------------------");
        }
    }

    public void debugPrint(String msg){
        if (DEBUG){
            System.out.println(msg);
        }
    }

    @Override
    public boolean equals(Object o){
        if (!(o instanceof Player)){
            return false;
        }
        if (((Player)o).ID.equals(ID)){
            return true;
        }

        return false;
    }

    @Override
    public String toString(){
        return ID;
    }

    /** A class representing an Opponent, which is distinct from a Player only
     * insofar as the computer having to make decisions about which Players to
     * prioritize spying on, sabotaging, or launching nuclear weapons against.
     */
    public class Opponent{

        private final int BASE_THREAT = 3;

        private int turnsSinceLastEspionage = 0;
        private int lastKnownResearchPoints = 0;
        private int threatLevel = BASE_THREAT;
        private Player player;

        private Opponent(Player p){
            this.player = p;
        }

        private void updateThreatAmount(Model.Decision d){
            switch (d){
                case SABOTAGE:
                    threatLevel += BASE_ADD_THREAT;
                    break;
                case NUCLEAR:
                    threatLevel += BASE_ADD_EXTREME_THREAT;
                    break;
            }
            threatLevel += (lastKnownResearchPoints - researchPoints);
        }

        //private void threatDecay(){ threatLevel = (threatLevel * 5) / 6; }

        private void resetThreat(){ threatLevel = 0; }

        public String getID(){ return player.getID(); }

        public Player getPlayer(){ return player; }

    }

}
