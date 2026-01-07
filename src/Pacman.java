import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

public class Pacman extends JPanel implements ActionListener, KeyListener {

    // ================= BLOCK =================
    class Block {
        int x, y, width, height;
        Image image;
        char direction = 'R';
        int velocityX = 0, velocityY = 0;

        Block(Image image, int x, int y, int w, int h) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }

        void updateDirection(char d) {
            direction = d;
            updateVelocity();
        }

        void updateVelocity() {
            int speed = tilesize / 4;
            velocityX = velocityY = 0;
            if (direction == 'U') velocityY = -speed;
            if (direction == 'D') velocityY = speed;
            if (direction == 'L') velocityX = -speed;
            if (direction == 'R') velocityX = speed;
        }
    }

    // ================= CONSTANTS =================
    private final int rowcount = 21;
    private final int columncount = 19;
    private final int tilesize = 32;
    private final int boardwidth = columncount * tilesize;
    private final int boardheight = rowcount * tilesize;

    // ================= IMAGES =================
    private Image wallImg, blueGhost, orangeGhost, pinkGhost, redGhost;
    private Image pacUp, pacDown, pacLeft, pacRight;

    // ================= OBJECTS =================
    HashSet<Block> walls = new HashSet<>();
    HashSet<Block> foods = new HashSet<>();
    HashSet<Block> ghosts = new HashSet<>();
    Block pacman;

    Timer timer;
    Random random = new Random();

    int score = 0;
    int lives = 3;
    boolean gameOver = false;
    boolean win = false;

    private final String[] tileMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXX XXXX XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXrXX X XXXX",
            "O       bpo       O",
            "XXXX X XXXXX X XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    // ================= CONSTRUCTOR =================
    public Pacman() {
        setPreferredSize(new Dimension(boardwidth, boardheight));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        wallImg = new ImageIcon(getClass().getResource("/images/wall.png")).getImage();
        blueGhost = new ImageIcon(getClass().getResource("/images/blueGhost.png")).getImage();
        orangeGhost = new ImageIcon(getClass().getResource("/images/orangeGhost.png")).getImage();
        pinkGhost = new ImageIcon(getClass().getResource("/images/pinkGhost.png")).getImage();
        redGhost = new ImageIcon(getClass().getResource("/images/redGhost.png")).getImage();

        pacUp = new ImageIcon(getClass().getResource("/images/pacmanUp.png")).getImage();
        pacDown = new ImageIcon(getClass().getResource("/images/pacmanDown.png")).getImage();
        pacLeft = new ImageIcon(getClass().getResource("/images/pacmanLeft.png")).getImage();
        pacRight = new ImageIcon(getClass().getResource("/images/pacmanRight.png")).getImage();

        loadMap();

        timer = new Timer(50, this);
        timer.start();

        requestFocusInWindow();
    }

    // ================= GAME LOOP =================
    public void move() {
        if (gameOver || win) return;

        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
            }
        }

        Block eaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                eaten = food;
                score += 10;
                break;
            }
        }
        if (eaten != null) foods.remove(eaten);
        if (foods.isEmpty()) win = true;

        for (Block ghost : ghosts) {
            if (random.nextInt(20) == 0) {
                if (pacman.x < ghost.x) ghost.updateDirection('L');
                else if (pacman.x > ghost.x) ghost.updateDirection('R');
                else if (pacman.y < ghost.y) ghost.updateDirection('U');
                else ghost.updateDirection('D');
            }

            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            for (Block wall : walls) {
                if (collision(ghost, wall)) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    ghost.updateDirection("UDLR".charAt(random.nextInt(4)));
                }
            }

            if (collision(pacman, ghost)) {
                lives--;
                resetPositions();
                if (lives == 0) gameOver = true;
            }
        }
    }

    private void resetPositions() {
        pacman.x = tilesize * 9;
        pacman.y = tilesize * 15;
        pacman.updateDirection('R');
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    // ================= KEY INPUT =================
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) { pacman.updateDirection('U'); pacman.image = pacUp; }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) { pacman.updateDirection('D'); pacman.image = pacDown; }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) { pacman.updateDirection('L'); pacman.image = pacLeft; }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) { pacman.updateDirection('R'); pacman.image = pacRight; }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    // ================= MAP =================
    private void loadMap() {
        for (int r = 0; r < rowcount; r++) {
            for (int c = 0; c < columncount; c++) {
                char ch = tileMap[r].charAt(c);
                int x = c * tilesize;
                int y = r * tilesize;

                if (ch == 'X') walls.add(new Block(wallImg, x, y, tilesize, tilesize));
                else if (ch == 'b') ghosts.add(new Block(blueGhost, x, y, tilesize, tilesize));
                else if (ch == 'o') ghosts.add(new Block(orangeGhost, x, y, tilesize, tilesize));
                else if (ch == 'p') ghosts.add(new Block(pinkGhost, x, y, tilesize, tilesize));
                else if (ch == 'r') ghosts.add(new Block(redGhost, x, y, tilesize, tilesize));
                else if (ch == 'P') pacman = new Block(pacRight, x, y, tilesize, tilesize);
                else if (ch == ' ') foods.add(new Block(null, x + 14, y + 14, 4, 4));
            }
        }
    }

    // ================= DRAW =================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (Block wall : walls)
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);

        for (Block food : foods)
            g.fillOval(food.x, food.y, food.width, food.height);

        for (Block ghost : ghosts)
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);

        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        g.setColor(Color.WHITE);
        g.drawString("Score: " + score + "   Lives: " + lives, 10, 20);

        if (gameOver) g.drawString("GAME OVER", boardwidth / 2 - 40, boardheight / 2);
        if (win) g.drawString("YOU WIN!", boardwidth / 2 - 30, boardheight / 2);
    }

    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    // ================= MAIN =================
    public static void main(String[] args) {
        JFrame frame = new JFrame("Pacman");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new Pacman());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
