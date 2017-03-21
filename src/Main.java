/**
 * Created by Administrator on 2/20/2017.
 */
public class Main {

	ArrayList<String> opponents = new ArrayList();
	opponents.add("Russia");
	opponents.add("China");
	opponents.add("North Korea");
    private Model game = new Model(opponents);

	// TODO add name list for system argument
    public static void main(String[] args) {
		game.runGame();
    }
}
