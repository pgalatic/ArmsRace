import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Created by Administrator on 2/20/2017.
 */
public class Main {

	private static ArrayList<String> opponents = new ArrayList<>();
	public static final Logger LOGGER = Logger.getLogger( Player.class.getName() );
	public static Handler HANDLER;

	// TODO add name list for system argument
    public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				File opponentFile = new File("../resources/opponents.txt");
				try {
					//HANDLER = new ConsoleHandler();
					//LOGGER.getLogger("").addHandler(HANDLER);
					Scanner in = new Scanner(opponentFile);
					while (in.hasNextLine()){
						opponents.add(in.nextLine());
					}
					Model game = new Model(opponents);
					game.runGame();
				} catch (IOException e){
					e.printStackTrace();
				}
			}
		});
    }
}
