import java.util.ArrayList;

/**
 * Created by Administrator on 2/20/2017.
 */
public class Main {

	private static ArrayList<String> opponents = new ArrayList<>();

	// TODO add name list for system argument
    public static void main(String[] args) {
		opponents.add("Russia");
		opponents.add("China");
		opponents.add("North Korea");
		Model game = new Model(opponents);
		game.runGame();
    }
}
