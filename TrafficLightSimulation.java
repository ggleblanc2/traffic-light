package com.ggl.testing;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class TrafficLightSimulation implements Runnable {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new TrafficLightSimulation());
	}

	private Animation animation;

	private ControlPanel controlPanel;

	private DrawingPanel drawingPanel;

	private JFrame frame;

	private TrafficLightModel model;

	public TrafficLightSimulation() {
		this.model = new TrafficLightModel();
	}

	@Override
	public void run() {
		frame = new JFrame("Traffic Light Simulation");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				stopAnimation();
				frame.dispose();
				System.exit(0);
			}
		});

		drawingPanel = new DrawingPanel(model);
		frame.add(drawingPanel, BorderLayout.CENTER);

		controlPanel = new ControlPanel(this, model);
		frame.add(controlPanel.getPanel(), BorderLayout.AFTER_LINE_ENDS);

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void startAnimation() {
		if (animation == null) {
			animation = new Animation(this, model);
			new Thread(animation).start();
		}
	}

	public void resetAnimation() {
		if (animation != null) {
			animation.resetAnimation();
		}
	}

	public void pauseAnimation(boolean paused) {
		if (animation != null) {
			animation.setPaused(paused);
		}
	}

	private void stopAnimation() {
		if (animation != null) {
			animation.setRunning(false);
		}
	}

	public void clearTimerField() {
		controlPanel.clearTimerField();
	}

	public void setTimerField(int seconds) {
		controlPanel.setTimerField(seconds);
	}

	public void setStartButtonText(String text) {
		controlPanel.setStartButtonText(text);
	}

	public void repaint() {
		drawingPanel.repaint();
	}

	public class ControlPanel {

		private JButton startButton;

		private JPanel panel;

		private JTextField timerField;
		private JTextField redField;
		private JTextField yellowField;
		private JTextField greenField;

		private TrafficLightModel model;

		private TrafficLightSimulation frame;

		public ControlPanel(TrafficLightSimulation frame, 
				TrafficLightModel model) {
			this.frame = frame;
			this.model = model;
			createPartControl();
		}

		private void createPartControl() {
			panel = new JPanel(new FlowLayout());
			JPanel innerPanel = new JPanel(new GridBagLayout());
			innerPanel.setBorder(BorderFactory.createEmptyBorder(
					5, 5, 5, 5));
			Font normalFont = innerPanel.getFont().deriveFont(16f);
			Font titleFont = innerPanel.getFont().deriveFont(24f)
					.deriveFont(Font.BOLD);

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = new Insets(5, 5, 5, 5);

			createCountdownField(innerPanel, titleFont, gbc);
			createDelayFields(innerPanel, titleFont, normalFont, gbc);
			createSimulationButtons(innerPanel, normalFont, gbc);

			panel.add(innerPanel);
		}

		private void createCountdownField(JPanel innerPanel, Font titleFont, 
				GridBagConstraints gbc) {
			gbc.gridwidth = 2;
			JLabel countDownLabel = new JLabel("Countdown To Light Change");
			countDownLabel.setHorizontalAlignment(JLabel.CENTER);
			countDownLabel.setFont(titleFont);
			innerPanel.add(countDownLabel, gbc);

			gbc.gridy++;
			timerField = new JTextField(6);
			timerField.setEditable(false);
			timerField.setHorizontalAlignment(JTextField.CENTER);
			timerField.setFont(innerPanel.getFont().deriveFont(48f));
			innerPanel.add(timerField, gbc);
		}

		private void createDelayFields(JPanel innerPanel, Font titleFont, 
				Font normalFont, GridBagConstraints gbc) {
			gbc.gridy++;
			JLabel defaultLabel = new JLabel("Light Time In Seconds");
			defaultLabel.setFont(titleFont);
			defaultLabel.setHorizontalAlignment(JLabel.CENTER);
			innerPanel.add(defaultLabel, gbc);

			gbc.gridwidth = 1;
			gbc.gridy++;
			JLabel redLabel = new JLabel("Red");
			redLabel.setFont(normalFont);
			innerPanel.add(redLabel, gbc);

			gbc.gridx++;
			redField = new JTextField(10);
			redField.setFont(normalFont);
			redField.setText(Integer.toString(model.getLightDelay(0)));
			innerPanel.add(redField, gbc);

			gbc.gridx = 0;
			gbc.gridy++;
			JLabel yellowLabel = new JLabel("Yellow");
			yellowLabel.setFont(normalFont);
			innerPanel.add(yellowLabel, gbc);

			gbc.gridx++;
			yellowField = new JTextField(10);
			yellowField.setFont(normalFont);
			yellowField.setText(Integer.toString(model.getLightDelay(1)));
			innerPanel.add(yellowField, gbc);

			gbc.gridx = 0;
			gbc.gridy++;
			JLabel greenLabel = new JLabel("Green");
			greenLabel.setFont(normalFont);
			innerPanel.add(greenLabel, gbc);

			gbc.gridx++;
			greenField = new JTextField(10);
			greenField.setFont(normalFont);
			greenField.setText(Integer.toString(model.getLightDelay(2)));
			innerPanel.add(greenField, gbc);

			gbc.gridwidth = 2;
			gbc.gridx = 0;
			gbc.gridy++;
			JButton changeButton = new JButton("Change Time Values");
			changeButton.setFont(normalFont);
			changeButton.addActionListener(new ChangeTimeListener());
			innerPanel.add(changeButton, gbc);
		}

		private void createSimulationButtons(JPanel innerPanel, 
				Font normalFont, GridBagConstraints gbc) {
			gbc.insets = new Insets(45, 5, 5, 5);
			gbc.gridy++;
			startButton = new JButton("Start Simulation");
			startButton.setFont(normalFont);
			startButton.addActionListener(new StartStopAnimationListener(frame, model));
			innerPanel.add(startButton, gbc);
		}

		public void setStartButtonText(String text) {
			startButton.setText(text);
		}

		public void clearTimerField() {
			timerField.setText("");
		}

		public void setTimerField(int seconds) {
			timerField.setText(Integer.toString(seconds));
		}

		public JPanel getPanel() {
			return panel;
		}

		public class ChangeTimeListener implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent event) {
				int redValue = convertText(redField.getText());
				int yellowValue = convertText(yellowField.getText());
				int greenValue = convertText(greenField.getText());

				redValue = (redValue < 10) ? 10 : redValue;
				yellowValue = (yellowValue < 10) ? 10 : yellowValue;
				greenValue = (greenValue < 10) ? 10 : greenValue;

				model.setLightDelay(0, redValue);
				model.setLightDelay(1, yellowValue);
				model.setLightDelay(2, greenValue);

				redField.setText(Integer.toString(redValue));
				yellowField.setText(Integer.toString(yellowValue));
				greenField.setText(Integer.toString(greenValue));
			}

			private int convertText(String text) {
				try {
					return Math.abs(Integer.valueOf(text));
				} catch (NumberFormatException e) {
					return -1;
				}
			}

		}

	}

	public class StartStopAnimationListener implements ActionListener {
		
		private boolean firstTimeSwitch;

		private TrafficLightModel model;

		private TrafficLightSimulation frame;

		public StartStopAnimationListener(TrafficLightSimulation frame, 
				TrafficLightModel model) {
			this.frame = frame;
			this.model = model;
			this.firstTimeSwitch = true;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			JButton button = (JButton) event.getSource();
			String text = button.getText();
			if (text.equals("Stop Simulation")) {
				model.setAllLights(true);
				frame.pauseAnimation(true);
				frame.clearTimerField();
				frame.repaint();
				frame.setStartButtonText("Start Simulation");
			} else {
				if (firstTimeSwitch) {
					frame.startAnimation();
					firstTimeSwitch = false;
				}
				
				model.setAllLights(false);
				frame.resetAnimation();
				frame.pauseAnimation(false);
				frame.repaint();
				frame.setStartButtonText("Stop Simulation");
			}
		}

	}

	public class DrawingPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		private int width;
		private int height;
		private int radius;
		private int margin;

		private TrafficLightModel model;

		public DrawingPanel(TrafficLightModel model) {
			this.model = model;
			this.width = 180;
			this.height = 3 * width;
			this.radius = width * 9 / 20;
			this.margin = width / 10;

			this.setBackground(Color.WHITE);
			this.setBorder(BorderFactory.createLineBorder(Color.BLUE, 5));
			this.setPreferredSize(new Dimension(width + margin + margin, 
					height + margin + margin));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			Graphics2D g2d = (Graphics2D) g;
			g2d.setStroke(new BasicStroke(5f));

			int x = getWidth() / 2;
			int yStart = width / 2 + margin;
			int index = 0;
			int diameter = radius + radius;

			for (int y = yStart; y <= height; y += width) {
				fillOval(g2d, index, x, y, diameter);
				drawOval(g2d, x, y, diameter);
				index++;
			}
		}

		private void fillOval(Graphics2D g2d, int index, int x, int y, 
				int diameter) {
			if (model.isLightOn(index)) {
				g2d.setColor(model.getColor(index));
				g2d.fillOval(x - radius, y - radius, diameter, diameter);
			}
		}

		private void drawOval(Graphics2D g2d, int x, int y, int diameter) {
			g2d.setColor(Color.BLACK);
			g2d.drawOval(x - radius, y - radius, diameter, diameter);
		}

	}

	public class Animation implements Runnable {

		private volatile boolean running;
		private volatile boolean paused;

		private int index;
		private int size;
		private int delay;

		private TrafficLightModel model;

		private TrafficLightSimulation frame;

		public Animation(TrafficLightSimulation frame, TrafficLightModel model) {
			this.frame = frame;
			this.model = model;
			this.size = model.getLights().length;

			resetAnimation();

			this.running = true;
			this.paused = false;
		}

		public void resetAnimation() {
			this.index = size - 1;
			setLightOn(index);
			this.delay = model.getLightDelay(index);
		}

		@Override
		public void run() {
			long refresh = 1000L;
			while (running) {
				if (paused) {
					sleep(refresh);
				} else {
					countdownSeconds(refresh);
					changeLight(refresh);
				}
			}
		}

		private void countdownSeconds(long refresh) {
			for (int i = delay; i > 0; i--) {
				if (paused) {
					break;
				}
				setTimerField(i);
				sleep(refresh);
			}
		}

		private void changeLight(long refresh) {
			if (!paused) {
				index--;
				index = (index < 0) ? size - 1 : index;
				setLightOn(index);
				delay = model.getLightDelay(index);
			}
		}

		private void sleep(long delay) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		private void setLightOn(int index) {
			model.setAllLights(false);
			model.setLightOn(index, true);
			repaint();
		}

		private void setTimerField(final int seconds) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					frame.setTimerField(seconds);
					;
				}
			});
		}

		private void repaint() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					frame.repaint();
				}
			});
		}

		public synchronized void setPaused(boolean paused) {
			this.paused = paused;
		}

		public synchronized void setRunning(boolean running) {
			this.running = running;
			this.paused = true;
		}

	}

	public class TrafficLightModel {

		private TrafficLight[] lights;

		public TrafficLightModel() {
			this.lights = new TrafficLight[3];
			lights[0] = new TrafficLight(Color.RED, 30);
			lights[1] = new TrafficLight(Color.YELLOW, 10);
			lights[2] = new TrafficLight(Color.GREEN, 20);
		}

		public TrafficLight[] getLights() {
			return lights;
		}

		public void setLightDelay(int index, int delay) {
			lights[index].setDelay(delay);
		}

		public int getLightDelay(int index) {
			return lights[index].getDelay();
		}

		public void setAllLights(boolean lightOn) {
			for (int i = 0; i < lights.length; i++) {
				setLightOn(i, lightOn);
			}
		}

		public void setLightOn(int index, boolean lightOn) {
			lights[index].setLightOn(lightOn);
		}

		public boolean isLightOn(int index) {
			return lights[index].isLightOn();
		}

		public Color getColor(int index) {
			return lights[index].getColor();
		}

	}

	public class TrafficLight {

		private boolean lightOn;

		private int delay;

		private final Color color;

		public TrafficLight(Color color, int delay) {
			this.color = color;
			this.delay = delay;
			this.lightOn = true;
		}

		public boolean isLightOn() {
			return lightOn;
		}

		public void setLightOn(boolean lightOn) {
			this.lightOn = lightOn;
		}

		public int getDelay() {
			return delay;
		}

		public void setDelay(int delay) {
			this.delay = delay;
		}

		public Color getColor() {
			return color;
		}

	}

}
