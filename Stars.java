import java.util.*;
import java.io.*;

class Stars {
	public static void main(String[] args) {		
		// Checking Arg Inputs.
		if (args.length != 4) {
			System.err.println("Error! Correct Usage: Stars <filename> <start_index> <end_index> <distance>");
		}
		else {
			// Parse arguments.
			int start = Integer.parseInt(args[1]);
			int end = Integer.parseInt(args[2]);
			int distance = Integer.parseInt(args[3]);
			
			// Calculate path.
			try {
				var stars = new Stars(args[0]);
				var path = stars.calculatePath(start, end, distance);
				
				// Create window.
				int scale = 8;
				var frame = StarCanvas.createFrame("Stars", scale);
				
				// Create a new canvas.
				var canvas = new StarCanvas(scale);
				frame.getContentPane().add(canvas);

				// Setup canvas.
				canvas.setStars(stars.getStarArray());
				System.out.println(Arrays.toString(path));
				if (path == null) {
					System.out.println("No Path Found");
					canvas.invalidPath(true);
				} 
				else {
					canvas.setLineIndices(path);
				}

				// Draw frame.
				frame.setVisible(true);
			} catch (Exception ex) {
				ex.printStackTrace();
			}		
		}	
	}

	private Star[] starArray;
	private float[][] distArray;
	private ArrayList<StarCost> frontier;
	private boolean[] visited;

	// Initialise stars.
	public Stars(String starCSV) {
		this.starArray = Stars.read(starCSV);
		this.distArray = Stars.precompute(this.starArray);
		this.frontier = new ArrayList<>();
		this.visited = new boolean[this.starArray.length];
	}

	// Get the star array.
	public Star[] getStarArray() {
		return this.starArray;
	}

	// Gets the distance between two star indexes.
	public float getDistance(int startIndex, int endIndex) {
		return this.distArray[startIndex][endIndex];
	}

	// Calculates the most optimal path using A*.
	private Integer[] calculatePath(int startIndex , int endIndex, int distance) throws Exception {
		if (startIndex < 0 || startIndex >= starArray.length ||
			endIndex < 0 || endIndex >= starArray.length) {
			throw new Exception("Invalid start or end index: " + startIndex + "," + endIndex + "should be between 0 and " + starArray.length);
		}
		if (distance < 0) {
			throw new Exception("Distance must be above 0. Actual distance: " + distance);
		}

		// Initial star.
		var initialStar = new StarCost(0.0f, startIndex, getDistance(startIndex, endIndex));

		// Calculate first stars neighbour.
		this.visited[startIndex] = true;	
		var neighbours = calculateStarNeighbours(startIndex, distance);
		for (var n : neighbours) {
			this.frontier.add(new StarCost(getDistance(startIndex, n), n, getDistance(n, endIndex), initialStar));
		}
		
		// Calculate the path until the goal or no items left in the frontier.
		while (this.frontier.size() > 0) {
			this.frontier.sort(new StarCost.StarComparator());

			// Get lowest cost star and mark it as index.
			var star = frontier.get(0);
			this.visited[star.getCurrentStarIndex()] = true;

			// If we've found the goal return it.
			if (star.getCurrentStarIndex() == endIndex) {
				var path = star.getPath().toArray(new Integer[0]);
				return path;
			}
						
			neighbours = calculateStarNeighbours(star.getCurrentStarIndex(), distance);

			// For each neighbour which hasn't been visited.
			for (int n : neighbours) {
				if (this.visited[n]) {
					continue;
				}
				// Create a new StarCost node.
				var cost = getDistance(star.getCurrentStarIndex(), n);
				var nextStar = new StarCost(cost, n, getDistance(n, endIndex), star);
				boolean hasSeenStar = false;
				// Check each star in the frontier.
				for (int i = 0; i < this.frontier.size(); i++) {
					var frontierStar = this.frontier.get(i);
					// If the star already exists, check if this path is more efficient.
					if (frontierStar.getCurrentStarIndex() == n) {
						hasSeenStar = true;
						// If path is more efficient, replace the old star with the new one.
						if (frontierStar.getTotalCost() > nextStar.getTotalCost()) {
							this.frontier.remove(i);
							this.frontier.add(nextStar);
						}
					}
				}

				// If star doesn't exist in the frontier, add it.
				if (!hasSeenStar) {
					this.frontier.add(nextStar);
				}
			}
			
			// Removes node once it has finished being processed.
			this.frontier.remove(0);
		}

		return null;
	}

	// Calculate the star indices within a distance of a star.
	private Integer[] calculateStarNeighbours(int starIndex, int distance) {
		// Get every star within distance.
		ArrayList<Integer> neighbours = new ArrayList<>();

		for (int i = 0; i < distArray[starIndex].length; i++) {
			float dist = getDistance(starIndex, i);
			if (dist <= distance && i != starIndex) {
				neighbours.add(i);
			}
		}

		return neighbours.toArray(new Integer[0]);
	}

	// Reads a CSV.
	private static Star[] read(String csv) {
		ArrayList<Star> starList = new ArrayList<>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(csv));
			int i = 0;
			for (String input = br.readLine(); input != null; input = br.readLine()) {
				String[] line = input.split(",", 2);
				Star star = new Star(Float.parseFloat(line[0]), Float.parseFloat(line[1]), i);
				starList.add(star);
				i++;
			}

			br.close();
		}
		catch(Exception ex){
			System.err.println(ex);
		}

		return starList.toArray(new Star[0]);
	}

	// Pre computes all the distances between all the points.
	private static float[][] precompute(Star[] starArray) {
		float[][] precompArray = new float[starArray.length][starArray.length];

		// For each star calculate the distance between every other star.
		for (int i = 0; i < starArray.length; i++) {
			for (int j = 0; j < starArray.length; j++) {
				// If the points are not the same, calculate the distance and add to the array.
				if (i != j) {
					precompArray[i][j] = Stars.dist(starArray[i], starArray[j]);
					precompArray[j][i] = precompArray[i][j];
				}
				// If the points are the same, the distance will be 0.
				else {
					precompArray[i][j] = 0;
				}
			}
		}

		return precompArray;
	}
	
	// Calculates distance between two points.
	private static float dist(Star star1, Star star2) {
		int min, max, approx;

		// Calculates difference of x and y of points.
		int x1 = (int)(star1.x * 100);
		int y1 = (int)(star1.y * 100);
		int x2 = (int)(star2.x * 100);
		int y2 = (int)(star2.y * 100);
		int dx = Math.abs(x1 - x2);
		int dy = Math.abs(y1 - y2);

		// Chooses appropriate min and max values.
		if ( dx < dy ) {
			min = dx;
			max = dy;
		} 
		else {
			min = dy;
			max = dx;
		}
		
		// It works
		approx = ( max * 1007 ) + ( min * 441 );

		if ( max < ( min << 4 )) {
			approx -= ( max * 40 );
		}

		return (float)(( approx + 512 ) >> 10 ) / 100;
	}

	// Represents a star and it's cost(s).
	private static class StarCost {
		private ArrayList<Integer> path;
		private float distanceToEnd;
		private float distanceFromStart;

		// Constructor for initial stars.
		public StarCost(float cost, int starIndex, float distanceToEnd) {
			this.distanceFromStart = cost;
			this.distanceToEnd = distanceToEnd;
			this.path = new ArrayList<>();
			this.path.add(starIndex);
		}

		// Constructor for frontier.
		public StarCost(float cost, int starIndex, float distanceToEnd, StarCost prev) {
			this.path = new ArrayList<>();
			this.distanceFromStart = cost + prev.distanceFromStart;
			this.distanceToEnd = distanceToEnd;
			for (var p : prev.path) {
				this.path.add(p);
			}
			this.path.add(starIndex);
		}

		// Gets the index for the most recent star.
		public int getCurrentStarIndex() {
			return this.path.get(path.size() - 1);
		}
		
		// Gets the path.
		public ArrayList<Integer> getPath() {
			return this.path;
		}

		// Gets the total cost of the StarCost.
		public float getTotalCost() {
			return this.distanceFromStart + this.distanceToEnd;
		}

		// Compares between two star cost objects.
		public static class StarComparator implements Comparator<StarCost>{
			@Override
			public int compare(Stars.StarCost s1, Stars.StarCost s2) {
				return (int)((s1.distanceFromStart - s2.distanceFromStart) * 100);
			}
		}
	}
}