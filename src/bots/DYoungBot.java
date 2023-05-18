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

		// Update the matrix in the MatrixPanel
		panel.setMatrix(matrix);
		panel.repaint();
		System.out.print("Print!");

		return chooseNextMove(matrix, me);

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
				for (int i = 1; i <= 16; i++) {
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

	public static void moveBot(int[][] matrix, int botX, int botY) {
		final int RADIUS = 20;
		final double BOT_SPEED = 1.5;
		final int BULLET_SPEED = 4;

		// Check if there are any bullets within the hit radius
		for (int i = -RADIUS; i <= RADIUS; i++) {
			for (int j = -RADIUS; j <= RADIUS; j++) {
				int checkX = botX + i;
				int checkY = botY + j;

				if (checkX >= 0 && checkX < 700 && checkY >= 0 && checkY < 500) {
					if (matrix[checkX][checkY] == 2) {
						// Bullet found, calculate the direction to move
						double distance = Math.sqrt(i * i + j * j);
						double moveX = (i / distance) * BOT_SPEED;
						double moveY = (j / distance) * BOT_SPEED;

						// Try to move the bot horizontally
						if (Math.abs(moveX) >= 1.0) {
							int moveSteps = (int) Math.round(moveX);
							int newX = botX + moveSteps;
							if (newX >= 0 && newX < 700) {
								if (matrix[newX][botY] == 0) {
									matrix[botX][botY] = 0; // Clear the current bot position
									botX = newX;
									matrix[botX][botY] = 1; // Mark the new bot position
								}
							}
						}

						// Try to move the bot vertically
						if (Math.abs(moveY) >= 1.0) {
							int moveSteps = (int) Math.round(moveY);
							int newY = botY + moveSteps;
							if (newY >= 0 && newY < 500) {
								if (matrix[botX][newY] == 0) {
									matrix[botX][botY] = 0; // Clear the current bot position
									botY = newY;
									matrix[botX][botY] = 1; // Mark the new bot position
								}
							}
						}
					}
				}
			}
		}
	}

}
