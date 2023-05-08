package bots;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;

import weka.core.*;
import weka.classifiers.*;
import weka.classifiers.trees.J48;

public class DYoungBot extends Bot {

	private Classifier classifier;

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

		nextMove = BattleBotArena.FIREUP;

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

		return "Dexter";
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
	public static int getDanger(BotInfo[] targets, BotInfo me) {
		double closestDistance = getDistance(targets[0], me);
		for (int i = 1; i < targets.length; i++) {
			if (getDistance(targets[i], me) < closestDistance)
				closestDistance = getDistance(targets[i], me);
		}
		if (closestDistance < 10)
			return 1;
		else
			return 0;
	}

	// Returns pixel distance to an object
	public static double getDistance(BotInfo target, BotInfo me) {
		double distance = Math.sqrt(Math.pow((Math.abs(me.getX() - target.getX())), 2)
				+ Math.pow((Math.abs(me.getY() - target.getY())), 2));
		return distance;
	}

}
