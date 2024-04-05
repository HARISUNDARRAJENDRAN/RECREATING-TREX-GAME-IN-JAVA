import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TRexGame extends JPanel implements KeyListener, Runnable {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;
    private static final int GROUND_HEIGHT = 50;
    private static final int TREX_WIDTH = 40;
    private static final int TREX_HEIGHT = 40;
    private static final int CACTUS_WIDTH = 20;
    private static final int CACTUS_HEIGHT = 40;
    private static final int BIRD_WIDTH = 40;
    private static final int BIRD_HEIGHT = 30;
    private static final int INITIAL_OBSTACLE_DELAY = 100;
    private static final int GRAVITY = 1; // Adjust gravity (increase to fall faster)

    private boolean isJumping = false;
    private int trexY = HEIGHT - GROUND_HEIGHT - TREX_HEIGHT;
    private int trexSpeedY = 0;
    private List<Obstacle> obstacles = new ArrayList<>();
    private Random random = new Random();
    private boolean isGameOver = false;
    private int score = 0;
    private BufferedImage trexImage;
    private BufferedImage cactusImage;

    public TRexGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);
        loadImages();
        new Thread(this).start();
    }

    private void loadImages() {
        try {
            trexImage = ImageIO.read(new File("trex.png")); // Load the dinosaur image
            cactusImage = ImageIO.read(new File("cacti.png")); // Load the cactus image
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw ground
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, HEIGHT - GROUND_HEIGHT, WIDTH, GROUND_HEIGHT);
        // Draw T-Rex image
        if (trexImage != null) {
            g.drawImage(trexImage, 100, trexY, TREX_WIDTH, TREX_HEIGHT, null);
        }
        // Draw obstacles
        for (Obstacle obstacle : obstacles) {
            obstacle.draw(g);
        }
        // Draw score
        g.setColor(Color.BLACK);
        g.drawString("Score: " + score, 10, 20);
        // Draw game over message
        if (isGameOver) {
            g.drawString("Game Over! Press Space to restart.", WIDTH / 2 - 100, HEIGHT / 2);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_SPACE && !isJumping && !isGameOver) {
            isJumping = true;
            trexSpeedY = -15; // Increase initial jump velocity
        } else if (key == KeyEvent.VK_SPACE && isGameOver) {
            restartGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    private void restartGame() {
        obstacles.clear();
        isGameOver = false;
        trexY = HEIGHT - GROUND_HEIGHT - TREX_HEIGHT;
        score = 0;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(20); // Decrease frame update interval for faster game speed
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!isGameOver) {
                if (random.nextInt(100) < 3) {
                    int type = random.nextInt(2); // Generate a random type of obstacle
                    if (type == 0) {
                        obstacles.add(new CactusObstacle(WIDTH, HEIGHT - GROUND_HEIGHT - CACTUS_HEIGHT));
                    } else {
                        obstacles.add(new BirdObstacle(WIDTH, HEIGHT - GROUND_HEIGHT - BIRD_HEIGHT));
                    }
                }
                for (Obstacle obstacle : obstacles) {
                    obstacle.move();
                    if (obstacle.x < 0) {
                        obstacles.remove(obstacle);
                        score++;
                        break;
                    }
                    if (obstacle.intersects(100, trexY, TREX_WIDTH, TREX_HEIGHT)) {
                        isGameOver = true;
                        break;
                    }
                }
                if (isJumping) {
                    trexSpeedY += GRAVITY; // Increase the vertical speed (gravity)
                    trexY += trexSpeedY;
                    if (trexY >= HEIGHT - GROUND_HEIGHT - TREX_HEIGHT) {
                        isJumping = false;
                        trexY = HEIGHT - GROUND_HEIGHT - TREX_HEIGHT;
                        trexSpeedY = 0;
                    }
                }
                repaint();
            }
        }
    }

    private abstract class Obstacle {
        int x, y;

        public Obstacle(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public abstract void move();

        public abstract void draw(Graphics g);

        public abstract boolean intersects(int trexX, int trexY, int trexWidth, int trexHeight);
    }

    private class CactusObstacle extends Obstacle {
        public CactusObstacle(int x, int y) {
            super(x, y);
        }

        @Override
        public void move() {
            x -= 7; // Increase obstacle speed
        }

        @Override
        public void draw(Graphics g) {
            if (cactusImage != null) {
                g.drawImage(cactusImage, x, y, CACTUS_WIDTH, CACTUS_HEIGHT, null);
            }
        }

        @Override
        public boolean intersects(int trexX, int trexY, int trexWidth, int trexHeight) {
            Rectangle obstacleRect = new Rectangle(x, y, CACTUS_WIDTH, CACTUS_HEIGHT);
            Rectangle trexRect = new Rectangle(trexX, trexY, trexWidth, trexHeight);
            return obstacleRect.intersects(trexRect);
        }
    }

    private class BirdObstacle extends Obstacle {
        public BirdObstacle(int x, int y) {
            super(x, y);
        }

        @Override
        public void move() {
            x -= 7; // Increase obstacle speed
        }

        @Override
        public void draw(Graphics g) {
            g.setColor(Color.BLUE);
            g.fillRect(x, y, BIRD_WIDTH, BIRD_HEIGHT);
        }

        @Override
        public boolean intersects(int trexX, int trexY, int trexWidth, int trexHeight) {
            Rectangle obstacleRect = new Rectangle(x, y, BIRD_WIDTH, BIRD_HEIGHT);
            Rectangle trexRect = new Rectangle(trexX, trexY, trexWidth, trexHeight);
            return obstacleRect.intersects(trexRect);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("T-Rex Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.getContentPane().add(new TRexGame());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
