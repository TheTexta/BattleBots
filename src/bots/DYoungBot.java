package bots;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;
import java.awt.Point;
import java.awt.geom.Line2D;

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
	private String name = "Dexter";
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
		// Define the safety radius for obstacle avoidance
		int safetyRadius = 100;

		// Calculate the default move based on the current position
		int defaultMove = vectorMove((int) me.getX() + RADIUS, (int) me.getY() + RADIUS, liveBots, deadBots, bullets,
				safetyRadius);

		// Calculate moves in different directions for evaluation
		int moveRight = vectorMove((int) me.getX() + RADIUS + 1, (int) me.getY() + RADIUS, liveBots, deadBots, bullets,
				safetyRadius);
		int moveLeft = vectorMove((int) me.getX() + RADIUS - 1, (int) me.getY() + RADIUS, liveBots, deadBots, bullets,
				safetyRadius);
		int moveUp = vectorMove((int) me.getX() + RADIUS, (int) me.getY() + RADIUS + 1, liveBots, deadBots, bullets,
				safetyRadius);
		int moveDown = vectorMove((int) me.getX() + RADIUS, (int) me.getY() + RADIUS - 1, liveBots, deadBots, bullets,
				safetyRadius);

		// Set the default move initially
		int move = defaultMove;

		// Evaluate if a smart move is needed based on the default move and neighboring
		// moves. Smart move is activiated if the move the bot takes is followed by a
		// move back to its original location. this indicates that the bot will enter a
		// loop going back and forth between the two directions, so it is more optimal
		// for the bot to move in a different direction to the initial vectormove result
		// which doesnt take this into account.
		if (defaultMove + moveRight == 7 || defaultMove + moveLeft == 7) {
			System.out.println("Smart Move activated");
			// Left/Right loop
			if (moveUp == 2) {
				move = 2;
			} else {
				move = 1;
			}
		} else if (defaultMove + moveUp == 3 || defaultMove + moveDown == 3) {
			System.out.println("Smart Move activated");
			// Up/Down loop
			if (moveRight == 3) {
				move = 3;
			} else {
				move = 4;
			}
		}

		return move;

	}

	// Calculates the next move based on the position and obstacles
	/*
	 * The vectorMove method calculates the next move for the bot based on its
	 * position, live bots, dead bots, bullets, and a safety radius. It generates
	 * obstacle vectors and applies weighting for live and dead bots. It also
	 * calculates a dodge vector based on the bullets. The method generates edge and
	 * center vectors and adds them to the overall vector. Finally, it determines
	 * the next move based on the resulting vector.
	 */
	private int vectorMove(int x, int y, BotInfo[] liveBots, BotInfo[] deadBots, Bullet[] bullets, int safetyRadius) {
		// Initialize the nextMove variable
		int nextMove = 0;

		// Create a point object for the current position
		Point position = new Point(x, y);

		// Initialize the vector for calculating the move direction
		Point vector = new Point(0, 0);

		// Calculate obstacle vectors and apply weighting
		for (BotInfo bot : liveBots) {
			// Calculate the position of the bot and the vector between the current position
			// and the bot
			Point centeredBot = new Point((int) bot.getX() + RADIUS, (int) bot.getY() + RADIUS);
			Point obstacleVector = new Point((position.x - centeredBot.x), (position.y - centeredBot.y));

			// Calculate the distance between the position and the bot
			double distance = getDistance(0, 0, obstacleVector.x, obstacleVector.y);

			// Calculate the weighting based on the safety radius and distance
			double weighting = safetyRadius / distance;
			if (weighting < 1) {
				weighting = 0;
			}

			// Update the vector by adding the weighted obstacle vector
			vector.x += obstacleVector.x * weighting;
			vector.y += obstacleVector.y * weighting;
		}

		// Calculate obstacle vectors for dead bots
		for (BotInfo grave : deadBots) {
			// Calculate the position of the dead bot and the vector between the current
			// position and the dead bot
			Point centeredBot = new Point((int) grave.getX() + RADIUS, (int) grave.getY() + RADIUS);
			Point obstacleVector = new Point((position.x - centeredBot.x), (position.y - centeredBot.y));

			// Calculate the distance between the position and the dead bot
			double distance = getDistance(0, 0, obstacleVector.x, obstacleVector.y);

			// Calculate the weighting based on the safety radius and distance, with an
			// additional 0.75 factor for dead bots
			double weighting = safetyRadius / distance;
			weighting *= 0.75;
			if (weighting < 1) {
				weighting = 0;
			}

			// Update the vector by adding the weighted obstacle vector
			vector.x += obstacleVector.x * weighting;
			vector.y += obstacleVector.y * weighting;
		}

		// Calculate the dodge vector based on bullets
		Point bulletTotal = new Point(0, 0);
		for (Bullet bullet : bullets) {
			// Calculate the vector to dodge the bullet
			Point toDodge = new Point(position.x - (int) bullet.getX(), position.y - (int) bullet.getY());

			// Calculate the weight for dodging based on the distance to the bullet
			double weight = (RADIUS + safetyRadius)
					/ getDistance(position.x, position.y, (int) bullet.getX(), (int) bullet.getY());

			// Check if the bullet is on a trajectory to hit the bot
			if (checkCollision(position.x, position.y, (int) bullet.getX(), (int) bullet.getY(),
					(int) (bullet.getX() + Math.pow(bullet.getXSpeed(), 9)), (int) (bullet.getY()), 2)) {

				// Adjust the dodge vector based on the bullet's trajectory
				toDodge.x = 0;
				if (bullet.getY() >= position.y) {
					toDodge.y = -1;
					System.out.println("Going up");
				} else {
					toDodge.y = 1;
					System.out.println("Going down");
				}

				// Calculate the weight for dodging the bullet
				weight = calcBulletWeight(position, bullet, safetyRadius, true);

				System.out.println("X AXIS - Weight:" + weight);
			} else if (checkCollision(position.x, position.y, (int) bullet.getX(), (int) bullet.getY(),
					(int) (bullet.getX()), -(int) (bullet.getY() + Math.pow(bullet.getYSpeed(), 9)), 2)) {

				// Adjust the dodge vector based on the bullet's trajectory
				toDodge.y = 0;
				if (bullet.getX() <= position.x) {
					toDodge.x = 1;
				} else {
					toDodge.x = -1;
				}

				// Calculate the weight for dodging the bullet
				weight = calcBulletWeight(position, bullet, safetyRadius, false);
				System.out.println("Y AXIS " + weight);
			} else if (getDistance(position.x, position.y, (int) bullet.getX(), (int) bullet.getY()) < RADIUS + 5) {
				// Adjust the weight for dodging if the bullet is close to the bot's radius
				weight *= 2;
				System.out.println("Radius Dodge: " + toDodge);
				System.out.println("My position: " + position);
				System.out.println("Bullet position: " + bullet.getX() + " " + bullet.getY());
			} else {
				// Bullet not on a trajectory to hit me, so it isn't that important
				weight *= 0.001;
			}

			// Adjust the dodge vector based on the weight
			toDodge.x *= weight;
			toDodge.y *= weight;

			// Update the bulletTotal vector by adding the dodge vector
			bulletTotal.x += toDodge.x;
			bulletTotal.y += toDodge.y;
		}

		// Update the vector by adding the bulletTotal vector
		vector.x += bulletTotal.x;
		vector.y += bulletTotal.y;
		System.out.println("Dodging: " + vector);

		// Generate edge vector
		Point edgeVector = edgeVector(position, safetyRadius);

		// Generate and weight center vector
		Point centerVector = new Point((GRID_WIDTH / 2) - position.x, (GRID_HEIGHT / 2) - position.y);
		double centerWeighting = (getDistance(0, 0, centerVector.x, centerVector.y)) * 0.001;
		centerVector.x *= centerWeighting;
		centerVector.y *= centerWeighting;

		// Add all additional vectors
		// Add edge vector (to keep bot away from walls)
		vector.x += edgeVector.x;
		vector.y += edgeVector.y;

		/*
		 * Add centerVector (attracts bot to center and gives it a direction
		 * when vectors around bot are at approx equilibrium) eg when an obstacle is
		 * above and below a bot. Could be updated to only take effect when resultant
		 * vector is below a certain magnitude.
		 */
		vector.x += centerVector.x;
		vector.y += centerVector.y;

		// Determine the next move based on the resulting vector
		if (vector.x == 0 && vector.y == 0) {
			nextMove = BattleBotArena.STAY;
			// System.out.println("STAY");
		} else if (Math.abs(vector.x) >= Math.abs(vector.y)) {
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
		}

		if ((Math.abs(vector.x) < 20 && Math.abs(vector.y) < 20) && (System.nanoTime() - timeSinceShot) > 1000000000) {
			System.out.println("Moving to near bot");
			// If there is nothing to dodge of significance, move to attack the nearest bot.

			double nearestBotDistance = Double.MAX_VALUE;
			BotInfo nearestBot = null;

			// Find the nearest live bot
			for (BotInfo bot : liveBots) {
				double distance = getDistance(x, y, (int) bot.getX(), (int) bot.getY());
				if (distance < nearestBotDistance) {
					nearestBotDistance = distance;
					nearestBot = bot;
				}
			}

			if (nearestBot != null) {
				// Move towards the nearest bot on the same x/y axis
				if (Math.abs(x - nearestBot.getX()) > Math.abs(y - nearestBot.getY())) {
					if (x < nearestBot.getX()) {
						nextMove = BattleBotArena.RIGHT;
					} else {
						nextMove = BattleBotArena.LEFT;
					}
				} else {
					if (y < nearestBot.getY()) {
						nextMove = BattleBotArena.DOWN;
					} else {
						nextMove = BattleBotArena.UP;
					}
				}

				// Check if bot is on the same axis withn +/- Radius -1. If it is change the
				// nextmove to shoot at the bot.
				if (Math.abs(x - nearestBot.getX()) < RADIUS - 1) {
					timeSinceShot = System.nanoTime();
					if (y > nearestBot.getY()) {
						nextMove = BattleBotArena.FIREUP;
					} else {
						nextMove = BattleBotArena.FIREDOWN;
					}
				} else if (Math.abs(y - nearestBot.getY()) < RADIUS - 1) {
					timeSinceShot = System.nanoTime();
					if (x > nearestBot.getX()) {
						nextMove = BattleBotArena.FIRELEFT;
					} else {
						nextMove = BattleBotArena.FIRERIGHT;
					}
				}

			} else {
				// No live bots found, stay in place
				nextMove = BattleBotArena.STAY;
			}

		}

		return nextMove;
	}

	// Checks if a collision occurs between a line and a bullet's trajectory
	private boolean checkCollision(int x, int y, int bulletX, int bulletY, int bulletX2, int bulletY2,
			int safetyRadius) {
		// Define an offset for the line based on the safety radius and bot radius
		int offset = safetyRadius + RADIUS;

		// Create two lines representing the bot's edges
		Line2D line1 = new Line2D.Float(x - offset, y, x + offset, y);
		Line2D line2 = new Line2D.Float(x, y - offset, x, y + offset);

		// Create a line representing the bullet's path
		Line2D bulletPath = new Line2D.Float(bulletX, bulletY, bulletX2, bulletY2);

		// Check if the bullet's path intersects with either of the bot's edges
		return (line1.intersectsLine(bulletPath)) || (line2.intersectsLine(bulletPath));
	}

	// Calculates the weight of a bullet based on its distance to the bot and its
	// trajectory
	private double calcBulletWeight(Point position, Bullet bullet, int safeRad, boolean horizontal) {
		double distance;
		// Calculate the distance between the position and the bullet's y-coordinate
		if (horizontal) {
			distance = Math.abs(position.y - bullet.getY());
		} else {
			distance = Math.abs(position.x - bullet.getX());
		}
		System.out.println("Distance: " + distance);

		// Calculate the exponent term used in the weight calculation
		double exponent = -((double) RADIUS / (Math.max(distance, 1))) - 4;
		System.out.println("Exponent: " + exponent);

		// Calculate the weight based on the exponent and the base value of 0.5
		double weight = Math.pow(0.5, exponent);
		System.out.println("Weight: " + weight);

		double weightForImpactDistance;
		if (horizontal) {
			weightForImpactDistance = Math.abs(position.x - bullet.getX());
		} else {
			weightForImpactDistance = Math.abs(position.y - bullet.getY());
		}
		System.out.println("Weight for Impact Distance: " + weightForImpactDistance);

		double safetyRadiusDividedByDifference = safeRad / weightForImpactDistance;
		System.out.println("Safety Radius Divided by Difference: " + safetyRadiusDividedByDifference);

		// Multiply the weight by the safetyRadiusDividedByDifference to get the final
		// weight
		weight *= safetyRadiusDividedByDifference;
		System.out.println("Final Weight: " + weight);

		return weight;
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
		// Calculate the distance between two points using the Pythagorean theorem
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

		// Check if the position is less than safeDistance units away from an edge
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

		// Limit the magnitude of the edgeVector to 100 in both x and y components
		if (edgeVector.x > 100) {
			edgeVector.x = 100;
		} else if (edgeVector.x < -100) {
			edgeVector.x = -100;
		}
		if (edgeVector.y > 100) {
			edgeVector.y = 100;
		} else if (edgeVector.y < -100) {
			edgeVector.y = -100;
		}

		return edgeVector;
	}

}
