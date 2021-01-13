import java.lang.System;
import java.lang.Math;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.Polygon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Main extends JPanel {

	private static int width = 1920;
	private static int height = 1080;

	private static BufferedImage fieldImage;

	private static double[] coordsToPixels = new double[]{10.0/width, 10.0/height};

	private static double[][] points = new double[][]{{2.5, 1.0}, {5.0, 5.0}, {7.5, 1.0}};
	private static double[] headings = new double[]{0.0, 0.0, 0.0};
	private static double[] derivatives = PathGenerator.convertHeadingsToDerivatives(headings);
	private static int pointSize = 25;
	private static double headingSize = 0.5;

	private boolean isMousePressed = false;
	private static int dragPoint = -1;
	private static int dragHeading = -1;

    private boolean deletePressed = false;

	public Main() {
		try {
			fieldImage = ImageIO.read(new File("field.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static int convertToPixels(double coords, int axis) {
		return (axis == 0 ? (int)Math.round(coords/coordsToPixels[axis]) : height-(int)Math.round(coords/coordsToPixels[axis]));
	}

	private static int[][] convertToPixels(double[][] coords) {
		int[][] pixels = new int[coords.length][coords[0].length];
		for (int i = 0; i < coords.length; i++) {
			pixels[i][0] = (int)Math.round(coords[i][0]/coordsToPixels[0]);
			pixels[i][1] = height-(int)Math.round(coords[i][1]/coordsToPixels[1]);
		}
		return pixels;
	}

	private static double convertToCoords(int pixel, int axis) {
		return (axis == 0 ? pixel*coordsToPixels[axis] : 10.0-(pixel*coordsToPixels[axis]));
	}

	private static double[][] convertToCoords(int[][] pixels) {
		double[][] coords = new double[pixels.length][pixels[0].length];
		for (int i = 0; i < pixels.length; i++) {
			coords[i][0] = pixels[i][0]*coordsToPixels[0];
			coords[i][1] = 10.0-(pixels[i][1]*coordsToPixels[1]);
		}
		return coords;
	}

	public void paint(Graphics g) {
		super.paint(g);
		g.drawImage(fieldImage, 0, 0, width, height, this);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (deletePressed) {
            if (isOnPoint((int)this.getMousePosition().getX(), (int)this.getMousePosition().getY(), true)) {
                double[][] newPoints = new double[points.length-1][points[0].length];
                double[] newHeadings = new double[headings.length-1];
                int a = 0;
                for (int i = 0; i < newPoints.length; i++) {
                    if (i == dragPoint) {
                        if (i == newPoints.length-1) {
                            break;
                        }
                        i++;
                        a = 1;
                    }
                    newPoints[i-a] = points[i];
                    newHeadings[i-a] = headings[i];
                }

                for (int i = 0; i < newPoints.length; i++) {
                    System.out.print("(");
                    for (int j = 0; j < newPoints[i].length; j++) {
                        System.out.print(newPoints[i][j]+(j == newPoints[i].length-1 ? "" : ", "));
                    }
                }

                dragPoint = -1;
                points = newPoints;
                headings = newHeadings;
                derivatives = PathGenerator.convertHeadingsToDerivatives(headings);
            }
        }

		if (dragPoint != -1) {
			int mouseX = (int)this.getMousePosition().getX();
			int mouseY = (int)this.getMousePosition().getY();
			points[dragPoint][0] = convertToCoords(mouseX, 0);
			points[dragPoint][1] = convertToCoords(mouseY, 1);
		}

		int[][] pointsToScreen = convertToPixels(points);
		g2d.setColor(Color.red);

        if (dragHeading != -1) {
            if (!isOnPoint((int)this.getMousePosition().getX(), (int)this.getMousePosition().getY(), false)) {
                derivatives[dragHeading] = (convertToCoords((int)this.getMousePosition().getY(), 1)-points[dragHeading][1])/(convertToCoords((int)this.getMousePosition().getX(), 0)-points[dragHeading][0]);
                headings[dragHeading] = Math.atan(derivatives[dragHeading]) * 180.0 / Math.PI;
            }
        }

		double[][] path = PathGenerator.generatePath(points, headings);

		double maxX = -999.0;
		double minX =  999.0;
		for (int i = 0; i < points.length; i++) {
			if (points[i][0] > maxX) {
				maxX = points[i][0];
			}
			if (points[i][0] < minX) {
				minX = points[i][0];
			}
		}

		Polygon p = new Polygon();
		for (int i = 0; i < path.length; i++) {
			if (path[i][0] >= minX && path[i][0] <= maxX) {
				p.addPoint(convertToPixels(path[i][0], 0), convertToPixels(path[i][1], 1));
			}
		}
		g2d.drawPolyline(p.xpoints, p.ypoints, p.npoints);

		for (int i = 0; i < pointsToScreen.length; i++) {
			if (i != pointsToScreen.length) {
				g2d.setColor(Color.blue);		
				g2d.drawLine(pointsToScreen[i][0], pointsToScreen[i][1], convertToPixels(points[i][0]+headingSize, 0), convertToPixels(points[i][1]+(derivatives[i]*headingSize), 1));
			}
			g2d.setColor(Color.black);		
			g2d.fillOval(pointsToScreen[i][0] - pointSize/2, pointsToScreen[i][1] - pointSize/2, pointSize, pointSize);
		}
	}

	private static boolean isOnPoint(int x, int y, boolean drag) {
		for (int i = 0; i < points.length; i++) {
			if (Math.sqrt(Math.pow(convertToPixels(points[i][0], 0)-x, 2) + Math.pow(convertToPixels(points[i][1], 1)-y, 2)) <= pointSize/2) {
				dragPoint = (drag ? i : dragPoint);
				return true;
			}
		}
		return false;
	}

    public static void main(String[] args) throws InterruptedException {
		JFrame gui = new JFrame();
		Main main = new Main();
        main.setFocusable(true);
        main.requestFocusInWindow();
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setSize(width, height);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		gui.setLocation(dim.width/2-gui.getSize().width/2, dim.height/2-gui.getSize().height/2);
        gui.setTitle("Jonathan's Path Generator v0.1");
        main.addMouseListener(new MouseAdapter() {
       		@Override
       		public void mousePressed(MouseEvent e) {
       			if (SwingUtilities.isLeftMouseButton(e)) {
	       			isOnPoint(e.getX(), e.getY(), true);
       			} else if (SwingUtilities.isRightMouseButton(e)) {
       				if (!isOnPoint(e.getX(), e.getY(), false)) {
	       				double[][] newPoints = new double[points.length+1][2];
	       				double[] newPoint = new double[] {convertToCoords(e.getX(), 0), convertToCoords(e.getY(), 1)};

	       				double[] newHeadings = new double[headings.length+1];

	       				int newPointIndex = -1;
	       				for (int i = 0; i < points.length; i++) {
	       					if (points[i][0] > newPoint[0]) {
	       						newPointIndex = i;
	       						break;
	       					}
	       				}

	       				newPointIndex = (newPointIndex != -1 ? newPointIndex : newPoints.length-1);

	       				newPoints[newPointIndex] = newPoint;
	       				newHeadings[newPointIndex] = 0.0;

	       				int a = 0;
	       				for (int i = 0; i < newPoints.length; i++) {
	       					if (i == newPointIndex) {
	       						if (i == newPoints.length-1) {
	       							break;
	       						}
	       						i++;
	       						a = 1;
	       					}
	       					newPoints[i] = points[i-a];
	       					newHeadings[i] = headings[i-a];
	       				}

	       				points = newPoints;
	       				headings = newHeadings;
	       				derivatives = PathGenerator.convertHeadingsToDerivatives(headings);
	       			} else {
	       				int oldDragPoint = main.dragPoint;
	       				isOnPoint(e.getX(), e.getY(), true);
	       				main.dragHeading = main.dragPoint;
	       				main.dragPoint = oldDragPoint;
	       			}
       			}
       			main.isMousePressed = true;
       		}

       		@Override
       		public void mouseReleased(MouseEvent e) {
   				main.dragPoint = -1;
   				main.dragHeading = -1;
       			main.isMousePressed = false;
       		}
        });

        main.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == 8) {
                    main.deletePressed = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == 8) {
                    main.deletePressed = false;
                }
            }
        });

        gui.getContentPane().add(main);
        gui.setVisible(true);

		while (true) {
			main.repaint();
			Thread.sleep(20);
		}
	}
}
