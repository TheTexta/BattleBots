package bots;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;

/*
 * Possible Moves
 * public static final int UP = 1;
	/**
	 * For bot to request a move down
	 *
	public static final int DOWN = 2;
	/**
	 * For bot to request a move left
	 *
	public static final int LEFT = 3;
	/**
	 * For bot to request a move right
	 *
	public static final int RIGHT = 4;
	/**
	 * For bot to request a bullet fired up
	 *
	public static final int FIREUP = 5;
	/**
	 * For bot to request a bullet fired down
	 *
	public static final int FIREDOWN = 6;
	/**
	 * For bot to request a bullet fired left
	 *
	public static final int FIRELEFT = 7;
	/**
	 * For bot to request a bullet fired right
	 *
	public static final int FIRERIGHT = 8;
	/**
	 * For bot to request a null move
	 *
	public static final int STAY = 9;
 */

public class DYoungBot extends Bot {
	private int[] moves = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

	/**
	 * Next message to send, or null if nothing to send.
	 */
	private String nextMessage = null;
	/**
	 * An array of trash talk messages.
	 */
	private String[] killMessages = { "Woohoo!!!", "In your face!", "Pwned", "Take that.", "Gotcha!", "Too easy.",
			"Hahahahahahahahahaha :-)" };
	/**
	 * Bot image
	 */
	Image current, up, down, right, left, image;
	/**
	 * My name (set when getName() first called)
	 */
	private String name = null;
	/**
	 * Counter for timing moves in different directions
	 */
	private int moveCount = 99;
	/**
	 * Next move to make
	 */
	private int move = BattleBotArena.UP;
	/**
	 * Counter to pause before sending a victory message
	 */
	private int msgCounter = 0;

	public String[] imageNames() {
		String[] paths = { "poop.png" }; // ***enter your list of image names here. Make sure images are put in images
											// package
		return paths;
	}

	/**
	 * Store the images loaded by the arena
	 */
	public void loadedImages(Image[] images) {
		if (images != null && images.length > 0)
			image = images[0];
	}

	public int getMove(BotInfo me, boolean shotOK, BotInfo[] liveBots, BotInfo[] deadBots, Bullet[] bullets) {
		// Integer to store the next move a bot will take.
		int nextMove;

		nextMove = BattleBotArena.STAY;

		int danger = getDanger(deadBots, bullets, me);
		if (danger == 3) {
			Bullet closeBullet = findClosestBullet(bullets, me);
			// TODO implement a system if the bot is touching a wall.
			// If i am on the same y axis as the bullet
			if (Math.abs(me.getY() - closeBullet.getY()) <= RADIUS) {
				// If i am higher then the bullet go up
				if (me.getY() > closeBullet.getY()) {
					nextMove = BattleBotArena.UP;
					System.out.println("UP");
				}
				// If i am lower then the bullet go down
				else {
					nextMove = BattleBotArena.DOWN;
					System.out.println("DOWN");
				}
			} else {
				// If i am furthe to the right of the bullet go right
				if (me.getX() > closeBullet.getX()){
					nextMove = BattleBotArena.RIGHT;
					System.out.println("RIGHT");
				// If i am further to the left of the bullet or in the center go left
				}else{
					nextMove = BattleBotArena.LEFT;
					System.out.println("LEFT");
				}
			}
		}

		/*
		 * Double[][] closest5 = closest5bullets(bullets, me);
		 * System.out.println("----------------------------");
		 * for (Double[] bullet : closest5) {
		 * System.out.println("X:" + bullet[0] + " Y:" + bullet[1] + " Distance:" +
		 * bullet[2]);
		 * 
		 * }
		 * System.out.println("----------------------------");
		 */
		return nextMove;

	}

	/**
	 * 
	 */
	public void newRound() {
		// ***not essential - you may do some initializing of your bot before round
		// begins
	}

	/**
	 * Send the message and then blank out the message string
	 */
	public String outgoingMessage() {
		String msg = nextMessage;
		nextMessage = null;
		return msg;
	}

	/**
	 * Construct and return my name
	 */
	public String getName() {

		return "DexBot";
	}

	/**
	 * Team "Arena"
	 */
	public String getTeamName() {
		return "DextersDestroyers";
	}

	/**
	 * Draws the bot at x, y
	 * 
	 * @param g The Graphics object to draw on
	 * @param x Left coord
	 * @param y Top coord
	 */
	public void draw(Graphics g, int x, int y) {
		if (image != null)
			g.drawImage(image, x, y, Bot.RADIUS * 2, Bot.RADIUS * 2, null);
		else {
			g.setColor(Color.lightGray);
			g.fillOval(x, y, Bot.RADIUS * 2, Bot.RADIUS * 2);
		}
	}

	/**
	 * If the message is announcing a kill for me, schedule a trash talk message.
	 * 
	 * @param botNum ID of sender
	 * @param msg    Text of incoming message
	 */
	public void incomingMessage(int botNum, String msg) {
		if (botNum == BattleBotArena.SYSTEM_MSG && msg.matches(".*destroyed by " + getName() + ".*")) {
			int msgNum = (int) (Math.random() * killMessages.length);
			nextMessage = killMessages[msgNum];
			msgCounter = (int) (Math.random() * 30 + 30);
		}
	}

	// Get danger level. If above a certain threshold move the bot in a direction.
	public static int getDanger(BotInfo[] targets, Bullet[] bullets, BotInfo me) {
		BotInfo target = targets[0];
		for (int i = 1; i < targets.length; i++) {
			if (getDistance(targets[i], me) < getDistance(targets[i], me)) {
				target = targets[i];
			}

		}
		Bullet closeBullet = findClosestBullet(bullets, me);
		// TODO change the +5
		if (sameAxis(closeBullet, me) && getDistance(closeBullet, me) < RADIUS + 10)
			return 3;
		double[] targetInfo = botInfo(target, me);
		if (targetInfo[0] < 10 && sameAxis(target, me)) {
			return 2;
		}
		if (targetInfo[0] < 10) {
			return 1;
		}
		return 0;
	}

	// Find closest bullet from an array
	public static Bullet findClosestBullet(Bullet[] bullets, BotInfo me) {
		Bullet closeBullet = bullets[0];
		for (Bullet bullet : bullets) {
			if (getDistance(closeBullet, me) > getDistance(bullet, me))
				closeBullet = bullet;
		}
		System.out.println(
				"X:" + closeBullet.getX() + " Y:" + closeBullet.getY() + " Distance:" + getDistance(closeBullet, me));
		System.out.println(
			"ME: X:" + me.getX() + " Y:" + me.getY()
		);
		return closeBullet;
	}

	// Important info of a bot
	public static double[] botInfo(BotInfo target, BotInfo me) {
		return new double[] { getDistance(target, me), target.getX(), target.getY() };
	}

	// Returns pixel distance to an object
	public static double getDistance(BotInfo target, BotInfo me) {
		double distance = Math.sqrt(Math.pow((Math.abs(me.getX() - target.getX())), 2)
				+ Math.pow((Math.abs(me.getY() - target.getY())), 2));
		return distance;
	}

	// Returns pixel distance to an object
	public static double getDistance(Bullet target, BotInfo me) {
		double distance = Math.sqrt(Math.pow((Math.abs(me.getX() - target.getX())), 2)
				+ Math.pow((Math.abs(me.getY() - target.getY())), 2));
		return distance;
	}

	public static Double[][] closest5bullets(Bullet[] bullets, BotInfo me) {
		ArrayList<Double[]> positions = new ArrayList<>();
		for (int i = 0; i < bullets.length; i++) {
			Double[] temp = { bullets[i].getX(), bullets[i].getY(), getDistance(bullets[i], me) };
			positions.add(temp);
		}
		positions.sort(new ColumnComparator(2));
		Double[][] result = { positions.get(0), positions.get(1), positions.get(2), positions.get(3),
				positions.get(4) };
		return result;
	}

	// Custom comparator to compare arrays based on the specified column
	static class ColumnComparator implements Comparator<Double[]> {
		private final int columnIndex;

		public ColumnComparator(int columnIndex) {
			this.columnIndex = columnIndex;
		}

		@Override
		public int compare(Double[] array1, Double[] array2) {
			// Compare the values in the specified column
			return Double.compare(array1[columnIndex], array2[columnIndex]);
		}
	}

	// Check if bot is on same axis as player
	public static boolean sameAxis(BotInfo target, BotInfo me) {
		return (Math.abs(target.getX() - me.getX()) <= RADIUS || Math.abs(target.getY() - me.getY()) <= RADIUS);
	}

	// Check if bullet is on same axis as player
	public static boolean sameAxis(Bullet target, BotInfo me) {
		return (Math.abs(target.getX() - me.getX()) <= RADIUS || Math.abs(target.getY() - me.getY()) <= RADIUS);
	}

}
