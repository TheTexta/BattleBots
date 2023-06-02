package bots;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;
//import extra.MatrixPanel;
import java.awt.Point;
import java.io.DataInput;

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
	public long timeSinceShot = System.nanoTime();
	private static final int GRID_WIDTH = 700;
	private static final int GRID_HEIGHT = 500;
	static final int[][] DIRECTIONS = {
			{ -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 }
	};

	// TODO comment out above

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
		String[] paths = { "smiley.png" }; // ***enter your list of image names here. Make sure images are put in images
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
		int safetyRadius = 100;

		nextMove = BattleBotArena.STAY;

		Point position = new Point((int) me.getX() + RADIUS, (int) me.getY() + RADIUS);

		Point vector = new Point(0, 0);

		for (BotInfo bot : liveBots) {
			Point centeredBot = new Point((int) bot.getX() + RADIUS, (int) bot.getY() + RADIUS);
			Point obstacleVector = new Point((position.x - centeredBot.x), (position.y - centeredBot.y));
			double distance = getDistance(0, 0, obstacleVector.x, obstacleVector.y);
			double weighting = (1 / distance) * safetyRadius;
			if (weighting < 1) {
				weighting = 0;
			}
			vector.x += obstacleVector.x * weighting;
			vector.y += obstacleVector.y * weighting;
		}

		//System.out.println("Object X: " + vector.x + ", Object Y: " + vector.y);
		//System.out.println("Angle: " + (Math.atan2(vector.y, vector.x) * (180 / Math.PI)));

		System.out.println("My coords: "+position.x + " " + position.x);
		Point bulletTotal = new Point(0, 0);
		for (Bullet bullet : bullets) {
			Point toDodge = new Point(position.x - (int) bullet.getX(), position.y - (int) bullet.getY());

			double weight = (RADIUS + safetyRadius)
					/ getDistance(position.x, position.y, (int) bullet.getX(), (int) bullet.getY());
			if ((Math.abs(bullet.getY() - position.y) <= RADIUS+5) && (bullet.getXSpeed() != 0)) {
				toDodge.x = 0;
				toDodge.y = 1;
				weight = Math.pow(0.5,-Math.abs((RADIUS+5)/(Math.min(position.y-bullet.getY(),1))));
				System.out.println("X AXIS: " + weight);
			} else if ((Math.abs(bullet.getX() - position.x) <= RADIUS+5) && (bullet.getYSpeed() != 0)) {
				toDodge.y = 0;
				toDodge.x = 1;
				weight = Math.pow(0.5,-Math.abs((RADIUS+5)/(Math.min(position.x-bullet.getX(),1))));
				System.out.println("Y AXIS: " + weight);
			} else if (getDistance(position.x, position.y, (int) bullet.getX(), (int) bullet.getY()) < RADIUS + 5) {
				weight *= 0.5;
			} else {
				// Bullet not on a trajectory to hit me therefor isnt that important
				weight *= 0.001;
			}
			toDodge.x *= weight;
			toDodge.y *= weight;
			if (toDodge.x > 0 || toDodge.y > 0) {
				System.out.println(toDodge);
				System.out.println("Bullet Coords: "+bullet.getX() + " " + bullet.getY());
			}
			bulletTotal.x += toDodge.x;
			bulletTotal.y += toDodge.y;
		}

		vector.x += bulletTotal.x;
		vector.y += bulletTotal.y;

		// Generate edge vector
		Point edgeVector = edgeVector(position, safetyRadius);
		// edgeVector.x *= 0.5;
		// edgeVector.y *= 0.5;

		// Generate and weight center vector
		Point centerVector = new Point((GRID_WIDTH / 2) - position.x, (GRID_HEIGHT / 2) - position.y);
		double centerWeighting = (getDistance(0, 0, centerVector.x, centerVector.y)) * 0.0001;
		centerVector.x *= centerWeighting;
		centerVector.y *= centerWeighting;

		// Add all additional vectors
		// Add edge vector (to keep bot away from walls)
		vector.x += edgeVector.x;
		vector.y += edgeVector.y;

		/*
		 * Add centerVector (attracts bot to center and gives it a direction
		 * when vectors around bot are at approx equilibrium) eg when an obstacle is
		 * above and bellow a bot. Could be updated to only take effect when resultant
		 * vector is below a certain magnitude.
		 */
		vector.x += centerVector.x;
		vector.y += centerVector.y;

		//System.out.println("Bullet X: " + bulletTotal.x + ", Bullet Y: " + bulletTotal.y);
		System.out.println("Center X: " + centerVector.x + ", Center Y: " +centerVector.y);
		System.out.println("Edge X: " + edgeVector.x + ", Center Y: " + edgeVector.y);
		System.out.println("Final X: " + vector.x + ", Final Y: " + vector.y);

		if(vector.x==0 && vector.y==0){
			nextMove = BattleBotArena.STAY;
			System.out.println("STAY");
		}
		else if (Math.abs(vector.x) >= Math.abs(vector.y)) {
			if (vector.x < 0) {
				nextMove = BattleBotArena.LEFT;
			} else {
				nextMove = BattleBotArena.RIGHT;
			}
		} else if (Math.abs(vector.x) < Math.abs(vector.y)) {
			if (vector.y < 0) {
				nextMove = BattleBotArena.UP;
			} else {
				nextMove = BattleBotArena.DOWN;
			}
		}else{
			System.out.println("ERROR");
		}

		//System.out.println("Move: " + nextMove);

		return nextMove;

	}

	public void newRound() {
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

	private double getDistance(int x, int y, int x2, int y2) {
		return Math.hypot(x2 - x, y2 - y);
	}

	private Point edgeVector(Point position, int safeDistance) {
		// Calculate the distances from each edge
		int distanceToLeft = position.x - RADIUS;
		int distanceToRight = 700 - (position.x + RADIUS);
		int distanceToTop = position.y - RADIUS;
		int distanceToBottom = 500 - (position.y + RADIUS);

		// Initialize the resultant vector
		Point edgeVector = new Point(0, 0);

		// Check if the position is less than 50 units away from an edge
		if (distanceToLeft <= safeDistance) {
			edgeVector.x += (Math.pow(0.9, (distanceToLeft - safeDistance))); // Add repulsion from the left edge
		} else if (distanceToRight <= safeDistance) {
			edgeVector.x -= (Math.pow(0.9, (distanceToRight - safeDistance))); // Add repulsion from the right edge
		}

		if (distanceToTop <= safeDistance) {
			edgeVector.y += (Math.pow(0.9, (distanceToTop - safeDistance))); // Add repulsion from the top edge
		} else if (distanceToBottom <= safeDistance) {
			edgeVector.y -= (Math.pow(0.9, (distanceToBottom - safeDistance))); // Add repulsion from the bottom edge
		}

		return edgeVector;
	}

}
