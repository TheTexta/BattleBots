package bots;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JFrame;

import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;
import extra.MatrixPanel;

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
	MatrixPanel panel = new MatrixPanel(new int[700][500]);
	public static boolean start = false;

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

		int[][] matrix = genMatrix(me, bullets, liveBots, deadBots);

		// check for incoming bullet trajectories
		int[] dangerPos = checkGrid(matrix, me.getX() - 1, me.getY() - 1, me.getX() + 1 + (2 * RADIUS),
				me.getY() + 1 + (2 * RADIUS), 2);
		if (dangerPos != null) {
			System.out.println("DANGER!");
			// If the bullet is to the left of me
			if (dangerPos[0] < me.getX()) {
				if (dangerPos[1] > me.getY() - RADIUS) {
					nextMove = BattleBotArena.UP;
				} else {
					nextMove = BattleBotArena.DOWN;
				}
			} // If the bullet is to the right of me
			else if (dangerPos[0] > Math.floor(me.getX() + (RADIUS * 2))) {
				// If i am lower then the bullet
				if (dangerPos[1] > me.getY() - RADIUS) {
					nextMove = BattleBotArena.UP;
				} else {
					nextMove = BattleBotArena.DOWN;
				}
			} // If the bullet is above me
			else if (dangerPos[1] <= me.getY()) {
				if (dangerPos[0] > me.getX() + RADIUS) {
					nextMove = BattleBotArena.LEFT;
				} else {
					nextMove = BattleBotArena.RIGHT;
				}
			} // If the bullet is below me
			else if (dangerPos[1] >= Math.floor(me.getY() + (RADIUS * 2))) {
				if (dangerPos[0] > me.getX() + RADIUS) {
					nextMove = BattleBotArena.LEFT;
				} else {
					nextMove = BattleBotArena.RIGHT;
				}
			}
		} else if (me.getX() < 15 || me.getX() + (2 * RADIUS) > 685 || me.getY() < 15 || me.getY() + (2 * RADIUS) > 485) {
			// No danger. Check for nearby walls to move away from
			if (me.getX() < 15) {
				nextMove = BattleBotArena.RIGHT;
			} else if (me.getX() + (2 * RADIUS) > 685) {
				nextMove = BattleBotArena.LEFT;
			} else if (me.getY() < 15) {
				nextMove = BattleBotArena.DOWN;
			} else if (me.getY() + (2 * RADIUS) > 485) {
				nextMove = BattleBotArena.UP;
			}
			System.out.println(me.getX() + " " + me.getY());
		} else {
			// Continue with current plan if available
		}

		// Update the matrix in the MatrixPanel
		panel.setMatrix(matrix);
		panel.repaint();

		return nextMove;

	}

	/**
	 * 
	 */
	public void newRound() {
		// ***not essential - you may do some initializing of your bot before round
		// begins
		if (!start) {
			panel.createAndShowGUI();
			start = true;
		}

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

	public static int[][] genMatrix(BotInfo me, Bullet[] bullets, BotInfo[] targets, BotInfo[] deadTargets) {
		int[][] matrix = new int[700][500];

		// Process bullets
		for (Bullet bullet : bullets) {
			int bulletX = (int) bullet.getX();
			int bulletY = (int) bullet.getY();

			if (bulletX >= 0 && bulletX < 700 && bulletY >= 0 && bulletY < 500) {
				matrix[bulletX][bulletY] = 5;

				int bulletXSpeed = (int) bullet.getXSpeed();
				int bulletYSpeed = (int) bullet.getYSpeed();

				// Handle bullet speed in different directions
				// TODO calc exact trajectory distance. currently 28
				for (int i = 1; i <= 30; i++) {
					if (bulletXSpeed > 0 && bulletX + i < 700) {
						matrix[bulletX + i][bulletY] = 2; // Bullet moving right
					}
					if (bulletXSpeed < 0 && bulletX - i >= 0) {
						matrix[bulletX - i][bulletY] = 2; // Bullet moving left
					}
					if (bulletYSpeed > 0 && bulletY + i < 500) {
						matrix[bulletX][bulletY + i] = 2; // Bullet moving down
					}
					if (bulletYSpeed < 0 && bulletY - i >= 0) {
						matrix[bulletX][bulletY - i] = 2; // Bullet moving up
					}
				}
			}
		}

		// Process targets
		for (BotInfo target : targets) {
			int targetX = (int) target.getX();
			int targetY = (int) target.getY();
			for (int i = 0; i < 20; i++) {
				for (int r = 0; r < 20; r++) {
					matrix[targetX + i][targetY + r] = 4; // Mark target area
				}
			}
		}

		// Process dead targets
		for (BotInfo dTarget : deadTargets) {
			int dTargetX = (int) dTarget.getX();
			int dTargetY = (int) dTarget.getY();
			matrix[dTargetX][dTargetY] = 3; // Mark dead target
		}

		// Mark current bot position
		int meX = (int) me.getX();
		int meY = (int) me.getY();
		for (int i = 0; i < 20; i++) {
			for (int r = 0; r < 20; r++) {
				matrix[meX + i][meY + r] = 1; // Mark bot position
			}
		}
		return matrix;
	}

	public int[] checkGrid(int[][] matrix, int startX, int startY, int endX, int endY, int value) {
		for (int x = 0; x < (endX - startX); x++) {
			for (int y = 0; y < (endY - startY); y++) {
				if (matrix[x + startX][y + startY] == value) {
					return new int[] { x + startX, y + startY };
				}
			}
		}
		return null;
	}

	public int[] checkGrid(int[][] matrix, double startX, double startY, double endX, double endY, int value) {
		return checkGrid(matrix, (int) startX, (int) startY, (int) endX, (int) endY, value);
	}

}
