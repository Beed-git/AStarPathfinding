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

	public static JFrame createFrame(String title, int scale) {
		var frame = new JFrame();
		frame.setResizable(false);
		frame.setTitle(title);
		frame.setPreferredSize(new Dimension(SIZE * scale, SIZE * scale));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		return frame;
	}

	public void setStars(Star[] stars) {
		this.stars = stars;
	}

	public void setLineIndices(Integer[] indices) {
		this.indices = indices;
	}

	public void invalidPath(boolean b) {
		this.invalidPath = b;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		var g2d = (Graphics2D)g;

		if (this.invalidPath) {

			g.setColor(ORANGE_COLOR);
			g.fillRect(0, 0, SIZE * scale, SIZE * scale);

			int fontSize = 10 * scale;
			var font = g2d
				.getFont()
				.deriveFont(Font.BOLD, fontSize);
			
			g2d.setFont(font);

			g2d.setColor(WHITE_COLOR);
			g2d.drawString("No Path Found!", fontSize, SIZE / 2 * scale - fontSize / 2);

			return;
		}

		// Fill background.
		g.setColor(BLACK_COLOR);
		g.fillRect(0, 0, SIZE * scale, SIZE * scale);

		int offset = scale / 2;

		// If there are no stars don't draw anything.
		if (this.stars == null) {
			return;
		}

		// Draws lines.
		// There needs to be at least 2 indices.
		g.setColor(GREEN_COLOR);
		g2d.setStroke(new BasicStroke((scale + 1) / 2));
		if (indices != null && indices.length > 1) {
			for (int i = 1; i < indices.length; i++) {
				var s1 = stars[indices[i]];
				var s2 = stars[indices[i - 1]];
				g2d.draw(
					new Line2D.Float(
					(int)(s1.x * scale + offset),
					(int)(s1.y * scale + offset),
					(int)(s2.x * scale + offset),
					(int)(s2.y * scale + offset)
				));
			}
		}

		// Draw the stars.
		g.setColor(ORANGE_COLOR);
		for (var s : this.stars) {
			g.fillOval(
				(int)(s.x * scale), 
				(int)(s.y * scale), 
				scale, 
				scale);
		}
	} 
}