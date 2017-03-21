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
    private final Random rand = new Random();
    private final Scanner in = new Scanner(System.in);

	// STATE

	private HashSet<Player> players = new HashSet<>();
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
        Player playerOne = new Player(name, false);
        players.add(playerOne);

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
			if (oppoentNameList.isEmpty()){ currname = "NULL"; } // too few names
            else {
				currName = opponentNames.get(rand.nextInt(opponentNameList.size()));
            	opponentNameList.remove(currName);
			}
            players.add(new Player(currName, true));
            if ((numOpponents - x) == 2){
                System.out.printf("%s, and");
            }else if ((numOpponents - x) == 1){
                System.out.printf("%s.\n", currName);
            }
            System.out.print(String.format("%s, ", currName));
        }
		System.out.println(".");

		// Initialize opponent lists.
		for (Player p : players){
			HashSet<Player> pOpponents = new HashSet<>();
			pOpponents.addAll(players);
			pOpponents.remove(p);
			p.addOpponents(pOpponents);
		}

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

		Decision d1;
		Decision d2;
		int userInput = 0;
		int curr_turn;

		while (true){
			curr_turn++;

			// Evaluate which players have won, if any
			for (Player p : players){
				if (p.getResearchPoints >= 10){
					winners.add(p);
				}
			}
			// If any players have won, quit, UNLESS there's a tie
			if (winners.size() > 0){
				if (winners.size() > 1){
					System.out.println("There's a tie!");
					breaktie(winners);
				}
				System.out.println("We have a winner!");
				break;
			}

			System.out.println("\n------------------------------");
			System.out.println(String.format("-----------TURN %d-------------", curr_turn));
			System.out.println("------------------------------\n");

			System.out.println("\tChoose an action.");
			if (curr_turn == 5){ System.out.println("The NUCLEAR option is now available."); }
			if (curr_turn < 5){
				System.out.println("\tRESEARCH\t|\tESPIONAGE\t|\tSABOTAGE");
				System.out.println("\t\t0\t\t|\t\t1\t\t|\t\t2");
			}else{
				System.out.println("\tRESEARCH\t|\tESPIONAGE\t|\tSABOTAGE\t|\tNUCLEAR");
				System.out.println("\t\t0\t\t|\t\t1\t\t|\t\t2\t\t|\t\t3");
			}
			
			while (userInput < 0 || userInput > 3){
				System.out.println("Please choose your first action.");
				userInput = in.nextInt();
			}

			switch (userInput){
				case 0:
					//TODO
			}

		}

		System.out.println("The winner is: " + winners.get(0).getID() + "!");
		System.out.println("The game will now exit.");
	}


}

