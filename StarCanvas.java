import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.geom.Line2D;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class StarCanvas extends JComponent {
	public static final int SIZE = 100;
	public static final int CANVAS_OFFSET = 5;

	// Constants for colours.;
	private static final Color BLACK_COLOR = new Color(40, 44, 52);
	private static final Color ORANGE_COLOR = new Color(228, 86, 73);
	private static final Color GREEN_COLOR = new Color(80, 161, 79);
	private static final Color WHITE_COLOR = new Color(250, 250, 250);

	private int scale;
	private boolean invalidPath;
	private Star[] stars;
	private Integer[] indices;

	public StarCanvas(int scale) {
		this.scale = scale;
		this.invalidPath = false;
	}

	// Creates a JFrame window.
	public static JFrame createFrame(String title, int scale) {
		var frame = new JFrame();
		frame.setResizable(false);
		frame.setTitle(title);
		frame.setPreferredSize(new Dimension((SIZE + (CANVAS_OFFSET * 2)) * scale, (SIZE + (CANVAS_OFFSET * 2)) * scale));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		return frame;
	}

	// Sets the stars to be drawn.
	public void setStars(Star[] stars) {
		this.stars = stars;
	}

	// Set the points between stars to draw (path).
	public void setLineIndices(Integer[] indices) {
		this.indices = indices;
	}

	// Set to true if a path was not found.
	public void invalidPath(boolean b) {
		this.invalidPath = b;
	}

	// Draws the stars and path.
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		var g2d = (Graphics2D)g;
		int canvasSize = (SIZE + (CANVAS_OFFSET * 2)) * scale;

		// If the path was invalid, draw text showing that no path was found.
		if (this.invalidPath) {
			// Background rectangle.
			g.setColor(ORANGE_COLOR);
			g.fillRect(0, 0, canvasSize, canvasSize);

			// Font setup.
			int fontSize = 10 * scale;
			var font = g2d
				.getFont()
				.deriveFont(Font.BOLD, fontSize);
			
			g2d.setFont(font);

			// Draw text.
			g2d.setColor(WHITE_COLOR);
			g2d.drawString("No Path Found!", fontSize, SIZE / 2 * scale - fontSize / 2);

			return;
		}

		// Fill background.
		g.setColor(BLACK_COLOR);
		g.fillRect(0, 0, canvasSize, canvasSize);

		// If there are no stars don't draw anything.
		if (this.stars == null) {
			return;
		}

		// Draws lines.
		// There needs to be at least 2 indices.
		g.setColor(GREEN_COLOR);
		g2d.setStroke(new BasicStroke((scale + 1) / 2));
		if (indices != null && indices.length > 1) {
			// Draw each line.
			for (int i = 1; i < indices.length; i++) {
				var s1 = stars[indices[i]];
				var s2 = stars[indices[i - 1]];
				DrawLine(g2d, s1.x, s1.y, s2.x, s2.y);
			}
		}

		// Draw the stars.
		g.setColor(ORANGE_COLOR);
		for (var s : this.stars) {
			DrawPoint(g2d, s.x, s.y);
		}
	} 

	private void DrawPoint(Graphics g2d, float x, float y) {
		y = SIZE - y + CANVAS_OFFSET;
		x += CANVAS_OFFSET;

		g2d.fillOval(
			(int)(x * scale), 
			(int)(y * scale), 
			scale, 
			scale);
	}

	private void DrawLine(Graphics2D g2d, float x1, float y1, float x2, float y2) {
		int offset = scale / 2;

		y1 = SIZE - y1 + CANVAS_OFFSET; 
		y2 = SIZE - y2 + CANVAS_OFFSET;
		x1 += CANVAS_OFFSET;
		x2 += CANVAS_OFFSET;

		g2d.draw(
			new Line2D.Float(
			(int)(x1 * scale + offset),
			(int)(y1 * scale + offset),
			(int)(x2 * scale + offset),
			(int)(y2 * scale + offset)
		));
	}
}