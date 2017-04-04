package ArmsRace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

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

	// CONSTANTS

    private final int DEFAULT_NUM_OPPONENTS = 2;
    private final int RESEARCH_TARGET = 20;
    static Random rand = new Random();
    private final Scanner in = new Scanner(System.in);

	// STATE

    private Player playerOne;
	private HashSet<Player> COMplayers = new HashSet<>();
	private ArrayList<Player> winners = new ArrayList<>();
    private int numOpponents = 0;

	/** 
	 *	Constructor. Initializes the base state for the game, including the 
	 *	name of the country representing the user, the number of opponents and 
	 *	the names of those opponents. It also initializes the opponent lists.
	 *	@param opponentNameList: a list of possible opponent names, randomly 
	 *			selected
	 */
    public Model(ArrayList<String> opponentNameList){
        rand.setSeed(1000);
        String name = null;

        while (name == null) {
            System.out.print("Enter your country's name: ");
            name = in.next();
            if (!name.matches("[a-zA-Z]+")){
                System.out.println("Invalid name. Pleae choose a name with " +
                                    "only alphabetical characters.");
                name = null;
            }
        }

		// The computer won't choose a name that the player has already chosen.
        if (opponentNameList.contains(name)){
            opponentNameList.remove(name);
        }
        playerOne = new Player(name, false);
        COMplayers.add(playerOne); // TEMPORARY, will be removed by end of constructor

        while (numOpponents == 0){
            System.out.print("Enter number of opponents: ");
            try{
                numOpponents = Integer.parseInt(in.next());
            }catch (NumberFormatException n){
                System.out.printf(  "Invalid number. Setting default: %d.\n",
                        DEFAULT_NUM_OPPONENTS);
                numOpponents = DEFAULT_NUM_OPPONENTS;
            }
            if (numOpponents < DEFAULT_NUM_OPPONENTS){
                System.out.printf("Number of opponents must be at least %d.\n",
                        DEFAULT_NUM_OPPONENTS);
                numOpponents = 0;
            }
        }

        System.out.print("Your opponents are: ");
        String currName;
        for (int x = 0; x < numOpponents; x++){
			if (opponentNameList.isEmpty()){ currName = "NULL"; } // too few names
            else {
				currName = opponentNameList.get(rand.nextInt(opponentNameList.size()));
            	opponentNameList.remove(currName);
			}
            COMplayers.add(new Player(currName, true));
            if ((numOpponents - x) == 2){
                System.out.printf("%s, and ", currName);
            }else if ((numOpponents - x) == 1){
                System.out.printf("%s.\n", currName);
            }else {
                System.out.print(String.format("%s, ", currName));
            }
        }

		// Initialize opponent lists.
		for (Player p : COMplayers){
			HashSet<Player> pOpponents = new HashSet<>();
			pOpponents.addAll(COMplayers);
			pOpponents.remove(p);
			p.addOpponents(pOpponents);
		}

        COMplayers.remove(playerOne);

    }

	/**
	 * Runs the game. Turns proceed in this order:
	 * 	1) Evaluate whether or not the game has been won. If it has, announce
	 *		the winner and quit.
	 *	2) Let the player take their turn, taking user input and then executing
	 *		whatever actions were chosen.
	 *	3) Execute the CPU's turns.
	 *	4) Announce the results, then repeat.
	 */
	public void runGame(){

		int userInput = -1;
		int curr_turn = 0;

        Decision d1 = Decision.NONE;
        Decision d2 = Decision.NONE;

        Player sabotageTarget;
        Player target;

		while (true){
			curr_turn++;

			// Evaluate which players have won, if any
            if (playerOne.getResearchPoints() >= RESEARCH_TARGET){
                winners.add(playerOne);
            }
			for (Player p : COMplayers){
				if (p.getResearchPoints() >= RESEARCH_TARGET){
					winners.add(p);
				}
			}
			// If any players have won, quit, UNLESS there's a tie
			if (winners.size() > 0){
				if (winners.size() > 1){
					System.out.println("There's a tie!");
					breaktie(winners);
				}
				System.out.println(String.format("We have a winner: %s!", winners.get(0).getID()));
				break;
			}

            // game turn UI (basic console text for now)
			System.out.println("\n------------------------------");
			System.out.println(String.format("-----------TURN %d-------------", curr_turn));
			System.out.println("------------------------------\n");

            System.out.println(String.format("CURRENT RESEARCH POINTS: %d", playerOne.getResearchPoints()));
            playerOne.printOpponentsValues();

			System.out.println("\tAvailable actions:");
			if (curr_turn == 5){ System.out.println("The NUCLEAR option is now available."); }
			if (curr_turn < 5){
				System.out.println("\tRESEARCH (0)\t|\tESPIONAGE (1)\t|\tSABOTAGE (2)");
			}else{
				System.out.println("\tRESEARCH (0)\t|\tESPIONAGE (1)\t|\tSABOTAGE (2)\t|\tNUCLEAR (3)");
			}
			
			// user selects actions for turn
            while (userInput < 0 || userInput > 3){
				System.out.println("Please choose your first action.");
				userInput = in.nextInt();
			}

            // those actions are set
			switch (userInput){
				case 0:
					d1 = Decision.RESEARCH;
                    break;
                case 1:
                    d1 = Decision.ESPIONAGE;
                    target = playerGetTarget();
                    playerOne.playerSetTarget(d1, target, true);
                    break;
                case 2:
                    d1 = Decision.SABOTAGE;
                    target = playerGetTarget();
                    playerOne.playerSetTarget(d1, target, true);
                    break;
                case 3:
                    d1 = Decision.NUCLEAR;
                    target = playerGetTarget();
                    playerOne.playerSetTarget(d1, target, true);
                    break;
			}

            if (userInput != 3){
                // user selects actions for turn
                userInput = -1;
                while (userInput < 0 || userInput > 2){
                    System.out.println("Please choose your second action.");
                    userInput = in.nextInt();
                }
                switch (userInput){
                    case 0:
                        d2 = Decision.RESEARCH;
                        break;
                    case 1:
                        d2 = Decision.ESPIONAGE;
                        target = playerGetTarget();
                        playerOne.playerSetTarget(d2, target, false);
                        break;
                    case 2:
                        d2 = Decision.SABOTAGE;
                        target = playerGetTarget();
                        playerOne.playerSetTarget(d2, target, false);
                        break;
                }

                playerOne.playerChooseDecision(d1, d2);
            }

            for (Player p : COMplayers){
                p.computerChooseDecision(curr_turn);
            }

            playerOne.passTurn();
            for (Player p : COMplayers){
                p.passTurn();
            }
            // REPORT (DEBUG)
            playerOne.debugPrint("-----------\nREPORT: POINTS\n-----------");
            playerOne.debugPrint(String.format("%s : %d", playerOne.getID(), playerOne.getResearchPoints()));
            for (Player p : COMplayers) {
                p.debugPrint(String.format("%s : %d", p.getID(), p.getResearchPoints()));
            }
            playerOne.debugPrint("-----------");

            userInput = -1;
            d1 = Decision.NONE;
            d2 = Decision.NONE;

		}

		System.out.println("The winner is: " + winners.get(0).getID() + "!");
		System.out.println("The game will now exit.");
	}

    /**
     * Prompts a Player to choose a target.
     * */
    public Player playerGetTarget(){
        String userInput = "";
        Player.Opponent target = null;

        System.out.println("------------------");
        System.out.println("AVAILABLE TARGETS:");
        for (Player p : COMplayers){
            System.out.println(p.getID());
        }
        System.out.println("------------------");

        // user selects target for turn
        while (target == null){
            System.out.println("Please choose your target.");
            userInput = in.next();
            target = playerOne.opponentLookup(userInput);
        }

        return target.getPlayer();

    }

    public void breaktie(ArrayList<Player> winners){


    }


}

