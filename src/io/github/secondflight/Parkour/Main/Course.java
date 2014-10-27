package io.github.secondflight.Parkour.Main;

import java.util.List;

import org.bukkit.block.Block;

public class Course {
	String name;
	
	int points;
	
	Block start;
	Block end;
	
	List<Block> checkpoints;
	
	public Course (String name, int points) {
		this.name = name;
		this.points = points;
	}
}
