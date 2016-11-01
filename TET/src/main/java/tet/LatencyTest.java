//package tet;
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.FlowLayout;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.RenderingHints;
//import java.awt.Toolkit;
//import java.awt.geom.Ellipse2D;
//import java.util.Random;
//
//import javax.swing.JButton;
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//
//@SuppressWarnings("serial")
//public class LatencyTest extends JPanel {
//	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//	Color transparent = new Color(0, 0, 0, 0);
//	Graphics2D g2d;
//	Ellipse2D.Double circle;
//
//	public LatencyTest() {
//		JFrame f = new JFrame();
//		circle = new Ellipse2D.Double(screenSize.getWidth() / 2 - 40, screenSize.getHeight() / 2 - 40, 40, 40);
//		setOpaque(false);
//
//		JButton btnStart = new JButton("Start");
//		btnStart.addActionListener(ae -> {
//			startTest();
//		});
//
//		JButton btnStop = new JButton("Stop");
//		btnStop.addActionListener(ae -> {
//			stopTest();
//		});
//
//		JButton btnClose = new JButton("Close");
//		btnClose.addActionListener(ae -> {
//			f.dispose();
//		});
//
//		setLayout(new FlowLayout());
//		add(btnStart);
//		add(btnStop);
//		add(btnClose);
//		f.add(this);
//		f.setSize(screenSize);
//		f.setUndecorated(true);
//		f.getContentPane().setBackground(Color.BLACK);
//		f.setVisible(true);
//		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//	}
//
//	private void startTest() {
//		new TETMain();
//		Thread t = new Thread() {
//			@Override
//			public void run() {
//				Random rand = new Random();
//				for (int i = 0; i <= 9; i++) {					
//					int randX = rand.nextInt(1880);
//					int randY = rand.nextInt(1040);
//					int randDelay = rand.nextInt(2000) + 1000;
//					circle = new Ellipse2D.Double(randX, randY, 40, 40);
//					repaint();
//					try {
//						sleep(randDelay);
//					} catch (InterruptedException ex) {
//					}
//				}
//			}
//		};
//		t.start();
//	}
//
//	private void stopTest() {
//
//	}
//
//	@Override
//	protected void paintComponent(Graphics g) {
//		super.paintComponent(g);
//		g2d = (Graphics2D) g;
//		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g2d.setColor(Color.ORANGE.brighter());
//		g2d.fill(circle);
//	}
//
//	public static void main(String[] args) {
//		new LatencyTest();
//	}
//}
