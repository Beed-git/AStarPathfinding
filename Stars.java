import java.util.*;
import java.io.*;

class Stars {
	public static void main(String[] args) {
		// csv, start node, end node, circle raidus
		args = new String[] { "trap.csv","77","213","15" };
		
		// Checking Arg Inputs
		if (args.length != 4) {
			System.err.println("Error! Correct Usage: Stars <filename> <start_index> <end_index> <distance>");
		}
		else {
			// Parse arguments
			int start = Integer.parseInt(args[1]);
			int end = Integer.parseInt(args[2]);
			int distance = Integer.parseInt(args[3]);
			
			// Calculate path.
			var stars = new Stars(args[0]);
			var path = stars.calculatePath(start, end, distance);
			
			//Create window.
			int scale = 8;
			var frame = StarCanvas.createFrame("Stars", scale);
			
			// Create canvas
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

	public Star[] getStarArray() {
		return this.starArray;
	}

	public float getDistance(int startIndex, int endIndex) {
		return this.distArray[startIndex][endIndex];
	}

	private Integer[] calculatePath(int startIndex , int endIndex, int distance) {
		// Initial star.
		var initialStar = new StarCost(0.0f, startIndex, getDistance(startIndex, endIndex));

		// Calculate first stars neighbour
		this.visited[startIndex] = true;	
		var neighbours = calculateStarNeighbours(startIndex, distance);
		for (var n : neighbours) {
			this.frontier.add(new StarCost(getDistance(startIndex, n), n, getDistance(n, endIndex), initialStar));
		}
		
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
			
			System.out.println(this.frontier.size());
			
			neighbours = calculateStarNeighbours(star.getCurrentStarIndex(), distance);

			for (int n : neighbours) {
				if (this.visited[n]) {
					continue;
				}
				var cost = getDistance(star.getCurrentStarIndex(), n);
				var nextStar = new StarCost(cost, n, getDistance(n, endIndex), star);
				boolean hasSeenStar = false;
				for (int i = 0; i < this.frontier.size(); i++) {
					var frontierStar = this.frontier.get(i);
					if (frontierStar.getCurrentStarIndex() == n) {
						hasSeenStar = true;
						if (frontierStar.getTotalCost() > nextStar.getTotalCost()) {
							this.frontier.remove(i);
							this.frontier.add(nextStar);
						}
					}
				}

				if (!hasSeenStar) {
					this.frontier.add(nextStar);
				}
			}
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

	private static float[][] precompute(Star[] starArray) {
		float[][] precompArray = new float[starArray.length][starArray.length];

		for (int i = 0; i < starArray.length; i++) {
			for (int j = 0; j < starArray.length; j++) {
				if (i != j) {
					precompArray[i][j] = Stars.dist(starArray[i], starArray[j]);
					precompArray[j][i] = precompArray[i][j];
				}
				else {
					precompArray[i][j] = 0;
				}
			}
		}

		return precompArray;
	}

	private static float dist(Star star1, Star star2) {
		int min, max, approx;
		int x1 = (int)(star1.x * 100);
		int y1 = (int)(star1.y * 100);
		int x2 = (int)(star2.x * 100);
		int y2 = (int)(star2.y * 100);
		int dx = Math.abs(x1 - x2);
		int dy = Math.abs(y1 - y2);

		if ( dx < dy ) {
			min = dx;
			max = dy;
		} 
		else {
			min = dy;
			max = dx;
		}

		approx = ( max * 1007 ) + ( min * 441 );

		if ( max < ( min << 4 )) {
			approx -= ( max * 40 );
		}

		return (float)(( approx + 512 ) >> 10 ) / 100;
	}

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

		public int getCurrentStarIndex() {
			return this.path.get(path.size() - 1);
		}
		
		public ArrayList<Integer> getPath() {
			return this.path;
		}

		public float getTotalCost() {
			return this.distanceFromStart + this.distanceToEnd;
		}

		public static class StarComparator implements Comparator<StarCost>{
			@Override
			public int compare(Stars.StarCost s1, Stars.StarCost s2) {
				return (int)((s1.distanceFromStart - s2.distanceFromStart) * 100);
			}
		}
	}
}