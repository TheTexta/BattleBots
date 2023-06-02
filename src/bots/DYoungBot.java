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
        {-1, 0}, {1, 0}, {0, -1}, {0, 1}
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
		int safetyRadius = 50;

		nextMove = BattleBotArena.STAY;

		int[][] matrix = genMatrix(me, bullets, liveBots, deadBots);

		List<int[]> objectPositions = checkGrid(matrix, 0, 0, GRID_WIDTH, GRID_HEIGHT, 4);
		objectPositions.addAll(checkGrid(matrix, me.getX() - safetyRadius, me.getY() - safetyRadius,
				me.getX() + safetyRadius + (2 * RADIUS),
				me.getY() + safetyRadius + (2 * RADIUS), 3));
		List<int[]> bulletPos = checkGrid(matrix, me.getX() - safetyRadius * 2, me.getY() - safetyRadius * 2,
				me.getX() + safetyRadius * 2 + (2 * RADIUS),
				me.getY() + safetyRadius * 2 + (2 * RADIUS), 5);
		Point vector = optimalMove(me, objectPositions, bulletPos, safetyRadius);

		if (Math.abs(vector.x) > Math.abs(vector.y)) {
			if (vector.x < 0) {
				nextMove = BattleBotArena.LEFT;
			} else {
				nextMove = BattleBotArena.RIGHT;
			}
		} else {
			if (vector.y < 0) {
				nextMove = BattleBotArena.UP;
			} else {
				nextMove = BattleBotArena.DOWN;
			}
		}

		System.out.println("Move: " + nextMove);

		return nextMove;

	}

	private Point optimalMove(BotInfo me, List<int[]> obstacles, List<int[]> bullets, int safeDistance) {
		Point position = new Point((int) me.getX() + RADIUS, (int) me.getY() + RADIUS);

		Point resultantVector = new Point(0, 0);

		if (!obstacles.isEmpty()) {
			for (int[] obstacle : obstacles) {
				Point obstacleVector = new Point(position.x - obstacle[0], position.y - obstacle[1]);
				double distance = getDistance(0, 0, obstacleVector.x, obstacleVector.y);
				double weighting = (1 / distance) * safeDistance;
				if (weighting < 1) {
					weighting = 0;
				}
				resultantVector.x += obstacleVector.x * weighting;
				resultantVector.y += obstacleVector.y * weighting;
			} /*
				 * resultantVector.x /= obstacles.size();
				 * resultantVector.y /= obstacles.size();
				 */
		}
		System.out.println("Object X: " + resultantVector.x + ", Object Y: " + resultantVector.y);
		System.out.println("Angle: " + (Math.atan2(resultantVector.y, resultantVector.x) * (180 / Math.PI)));

		Point bulletTotal = new Point(0, 0);
		if (!bullets.isEmpty()) {
			for (int[] bullet : bullets) {
				Point bulletVector = new Point(position.x - bullet[0], position.y - bullet[1]);
				// Same x axis
				if (Math.abs(bullet[1] - position.y) <= RADIUS + 5) {
					bulletVector.x = 0;
				} else if (Math.abs(bullet[0] - position.x) <= RADIUS + 5) {
					bulletVector.y = 0;
				} else if (getDistance(position.x, position.y, bullet[0], bullet[1]) < RADIUS + 5) {
					bulletVector.x *= 0.5;
					bulletVector.y *= 0.5;
				} else {
					// Bullet not on a trajectory to hit me therefor isnt that important
					bulletVector.x *= 0.1;
					bulletVector.y *= 0.1;
				}
				double distance = getDistance(0, 0, bulletVector.x, bulletVector.y);
				double weighting = (1 / distance) * (RADIUS + 30);
				bulletTotal.x += bulletVector.x * weighting;
				bulletTotal.y += bulletVector.y * weighting;
			}
		}


		resultantVector.x += bulletTotal.x;
		resultantVector.y += bulletTotal.y;

		// Generate edge vector
		Point edgeVector = edgeVector(position, safeDistance);
		// edgeVector.x *= 0.5;
		// edgeVector.y *= 0.5;

		// Generate and weight center vector
		Point centerVector = new Point((GRID_WIDTH / 2) - position.x, (GRID_HEIGHT / 2) - position.y);
		double centerWeighting = (getDistance(0, 0, centerVector.x, centerVector.y)) * 0.0001;
		centerVector.x *= centerWeighting;
		centerVector.y *= centerWeighting;

		// Add all additional vectors
		// Add edge vector (to keep bot away from walls)
		resultantVector.x += edgeVector.x;
		resultantVector.y += edgeVector.y;

		/*
		 * Add centerVector (attracts bot to center and gives it a direction
		 * when vectors around bot are at approx equilibrium) eg when an obstacle is
		 * above and bellow a bot. Could be updated to only take effect when resultant
		 * vector is below a certain magnitude.
		 */
		resultantVector.x += centerVector.x;
		resultantVector.y += centerVector.y;

		System.out.println("Bullet X: " + bulletTotal.x + ", Bullet Y: " + bulletTotal.y);
		System.out.println("Center X: " + centerVector.x + ", Center Y: " + centerVector.y);
		System.out.println("Edge X: " + edgeVector.x + ", Center Y: " + edgeVector.y);
		System.out.println("Final X: " + resultantVector.x + ", Final Y: " + resultantVector.y);
		

		return resultantVector;
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

	private static int[][] genMatrix(BotInfo me, Bullet[] bullets, BotInfo[] targets, BotInfo[] deadTargets) {
		int[][] matrix = new int[700][500];

		// Process bullets
		for (Bullet bullet : bullets) {
			int bulletX = (int) bullet.getX();
			int bulletY = (int) bullet.getY();

			if (bulletX >= 0 && bulletX < GRID_WIDTH && bulletY >= 0 && bulletY < GRID_HEIGHT) {
				matrix[bulletX][bulletY] = 5;

				int bulletXSpeed = (int) bullet.getXSpeed();
				int bulletYSpeed = (int) bullet.getYSpeed();

				// Handle bullet speed in different directions
				for (int i = 1; i <= 30; i++) {
					if (bulletXSpeed > 0 && bulletX + i < GRID_WIDTH) {
						matrix[bulletX + i][bulletY] = 2; // Bullet moving right
					}
					if (bulletXSpeed < 0 && bulletX - i >= 0) {
						matrix[bulletX - i][bulletY] = 2; // Bullet moving left
					}
					if (bulletYSpeed > 0 && bulletY + i < GRID_HEIGHT) {
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

			for (int i = 0; i < 20; i++) {
				for (int r = 0; r < 20; r++) {
					matrix[dTargetX + i][dTargetY + r] = 3; // Mark dead target
				}
			}
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

	private List<int[]> checkGrid(int[][] matrix, int startX, int startY, int endX, int endY, int value) {
		List<int[]> positions = new ArrayList<>();
		// Check boundaries and adjust if necessary
		endX = Math.min(endX, GRID_WIDTH - 1);
		endY = Math.min(endY, GRID_HEIGHT - 1);
		startX = Math.max(startX, 0);
		startY = Math.max(startY, 0);

		for (int x = startX; x < endX; x++) {
			for (int y = startY; y < endY; y++) {
				if (matrix[x][y] == value) {
					positions.add(new int[] { x, y });
				}
			}
		}

		return positions;
	}

	private List<int[]> checkGrid(int[][] matrix, double startX, double startY, double endX, double endY, int value) {
		return checkGrid(matrix, (int) startX, (int) startY, (int) endX, (int) endY, value);
	}

	private double getDistance(int x, int y, int x2, int y2) {
		return Math.hypot(x2 - x, y2 - y);
	}

	private void simplifyPoint(Point input){
		if (Math.abs(input.x)>Math.abs(input.y))
			input.y=0;
		else
			input.x=0;
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
			edgeVector.x += safeDistance - distanceToLeft; // Add repulsion from the left edge
		} else if (distanceToRight <= safeDistance) {
			edgeVector.x -= safeDistance - distanceToRight; // Add repulsion from the right edge
		}

		if (distanceToTop <= safeDistance) {
			edgeVector.y += safeDistance - distanceToTop; // Add repulsion from the top edge
		} else if (distanceToBottom <= safeDistance) {
			edgeVector.y -= safeDistance - distanceToBottom; // Add repulsion from the bottom edge
		}

		return edgeVector;
	}

}
