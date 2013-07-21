package net.garrapeta.jumplings.comms;

import java.util.List;

import net.garrapeta.jumplings.Score;

/**
 * Request model of the data received from the backend
 */
public class ResponseModel {
	public List<Score> localScores;
	public List<Score> globalScores;
}
