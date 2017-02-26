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

    private final int DEFAULT_NUM_OPPONENTS = 2;

    private Random rand = new Random();
    private Scanner in = new Scanner(System.in);
    private HashSet<Player> players = new HashSet<>();
    private int numOpponents = 0;

    public Model(ArrayList<String> opponentNames){
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

        if (opponentNames.contains(name)){
            opponentNames.remove(name);
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
            currName = opponentNames.get(rand.nextInt(opponentNames.size()));
            opponentNames.remove(currName);
            players.add(new Player(currName, true));
            if ((numOpponents - x) == 2){
                System.out.printf("%s, and");
            }else if ((numOpponents - x) == 1){
                System.out.printf("%s.\n", currName);
            }
            System.out.print(String.format("%s, ", currName));
        }
    }

}

