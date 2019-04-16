package source;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class aMAZE extends JPanel implements ActionListener {

    /**
	 * 
	 */// generation of UID for serialization / de-serialization
	private static final long serialVersionUID = -4075096311378088410L;
	
	private Dimension d;
    private final Font smallFont = new Font("Times Roman", Font.BOLD, 20);

    private Image ii;
    private final Color dotColor = (Color.yellow);
    private Color mazeColor;

    private boolean inGame = false;
    private boolean dying = false;
    
    // starting game level variables
    private final int BLOCK_SIZE = 24;
    private final int N_BLOCKS = 15;
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;
    private final int PAC_ANIM_DELAY = 2;
    private final int PACMAN_ANIM_COUNT = 4;
    private final int MAX_GHOSTS = 12;
    private final int PACMAN_SPEED = 6;

    private int pacAnimCount = PAC_ANIM_DELAY;
    private int pacAnimDir = 1;
    private int capBoyAnimPos = 0;
    private int N_GHOSTS = 6;
    private int pacsLeft, score;
    private int[] dx, dy;
    private int[] ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed;

    private Image ghost;
    private Image capBoySprt, capboy2up, capboy2left, capboy2right, capboy2down;
    private Image capboy3up, capboy3down, capboy3left, capboy3right;
    private Image capboy4up, capboy4down, capboy4left, capboy4right;

    // storing XY cords for player; storing delta changes in directions
    private int capboy_x, capboy_y, capboyd_x, capboyd_y;
    private int req_dx, req_dy, view_dx, view_dy;

    // 1 = left; 2 = top; 4 = right; 8 = bottom; 16 = point; can be added; (245)
    private final short levelData[] = {
        19, 26, 26, 26, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
        21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        21, 0, 0, 0, 17, 16, 16, 24, 16, 16, 16, 16, 16, 16, 20,
        17, 18, 18, 18, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 20,
        17, 16, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 16, 24, 20,
        25, 16, 16, 16, 24, 24, 28, 0, 25, 24, 24, 16, 20, 0, 21,
        1, 17, 16, 20, 0, 0, 0, 0, 0, 0, 0, 17, 20, 0, 21,
        1, 17, 16, 16, 18, 18, 22, 0, 19, 18, 18, 16, 20, 0, 21,
        1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
        1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
        1, 17, 16, 16, 16, 16, 16, 18, 16, 16, 16, 16, 20, 0, 21,
        1, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0, 21,
        1, 25, 24, 24, 24, 24, 24, 24, 24, 24, 16, 16, 16, 18, 20,
        9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 25, 24, 24, 24, 28
    };
    //**ADD NEXT LEVEL HERE**

    // auto - increment player velocity to maximum speed
    private final int validSpeeds[] = {1, 2, 3, 4, 6, 8};
    private final int maxSpeed = 6;

    private int currentSpeed = 3;
    private short[] screenData;
    private Timer timer;

    public aMAZE() {

        loadImages();
        initVariables();
        initBoard();
    }
    
    // adapter variables are dy && dx; check KeyInputs;
    private void initBoard() {
        
        addKeyListener(new TAdapter());

        setFocusable(true);

        setBackground(Color.black);
    }

    // maze settings && variables for maze
    private void initVariables() {

        screenData = new short[N_BLOCKS * N_BLOCKS];
        mazeColor = (Color.blue);
        d = new Dimension(400, 400);
        ghost_x = new int[MAX_GHOSTS];
        ghost_dx = new int[MAX_GHOSTS];
        ghost_y = new int[MAX_GHOSTS];
        ghost_dy = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];
        dx = new int[4];
        dy = new int[4];
        
        timer = new Timer(40, this);
        timer.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();

        initGame();
    }

    // counts capBoyAnimPos for sprite determination
    private void doAnim() {

        pacAnimCount--;
        //DELAY **NOT WORKING**
        if (pacAnimCount <= 0) {
            pacAnimCount = PAC_ANIM_DELAY;
            capBoyAnimPos = capBoyAnimPos + pacAnimDir;

            if (capBoyAnimPos == (PACMAN_ANIM_COUNT - 1) || capBoyAnimPos == 0) {
                pacAnimDir = -pacAnimDir;
            }
        }
    }

    // lets have fun;-)
    private void playGame(Graphics2D g2d) {

        if (dying) {

            death();

        } else {

            moveCapBoy();
            drawCapBoy(g2d);
            moveGhosts(g2d);
            checkMaze();
        }
    }

    // settings of the start-up window;
    private void startUpScreen(Graphics2D g2d) {

        g2d.setColor(new Color(0, 32, 48));
        g2d.fillRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50);
        // border color;
        g2d.setColor(Color.yellow);
        g2d.drawRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50);

        String S = "Hit S to play!";
        Font small = new Font("Courier", Font.BOLD, 20);
        FontMetrics metr = this.getFontMetrics(small);
        
        // text color;
        g2d.setColor(Color.yellow);
        g2d.setFont(small);
        g2d.drawString(S, (SCREEN_SIZE - metr.stringWidth(S)) / 2, SCREEN_SIZE / 2);
    }
    // addition to scores && score board display;
    private void drawScore(Graphics2D g) {

        int i;
        String s;

        g.setFont(smallFont);
        g.setColor(Color.green);
        s = "HiScore: " + score;
        g.drawString(s, SCREEN_SIZE / 2 + 96, SCREEN_SIZE + 16);

        for (i = 0; i < pacsLeft; i++) {
            g.drawImage(capboy3left, i * 28 + 8, SCREEN_SIZE + 1, this);
        }
    }

    private void checkMaze() {

        short i = 0;
        // any points left to eat?
        boolean finished = true;

        while (i < N_BLOCKS * N_BLOCKS && finished) {

            if ((screenData[i] & 48) != 0) {
                finished = false;
            }

            i++;
        }
        // congratz! now lets add another badBoi && more speed;
        if (finished) {

            score += 50;

            if (N_GHOSTS < MAX_GHOSTS) {
                N_GHOSTS++;
            }

            if (currentSpeed < maxSpeed) {
                currentSpeed++;
            }

            initLevel();
        }
    }

    // remove one life if collision() occurs;
    private void death() {

        pacsLeft--;

        if (pacsLeft == 0) {
            inGame = false;
        }

        continueLevel();
    }

    // randomize ghosts movement wouldn't work, became stuck in parts of maze;
    private void moveGhosts(Graphics2D g2d) {

        short i;
        int pos;
        int count;

        for (i = 0; i < N_GHOSTS; i++) {
        	// continuing after moving each cord;
            if (ghost_x[i] % BLOCK_SIZE == 0 && ghost_y[i] % BLOCK_SIZE == 0) {
            	// move ghost if position is blocked;
                pos = ghost_x[i] / BLOCK_SIZE + N_BLOCKS * (int) (ghost_y[i] / BLOCK_SIZE);

                count = 0;
                // for top, bottom, left, and right;
                if ((screenData[pos] & 1) == 0 && ghost_dx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 2) == 0 && ghost_dy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if ((screenData[pos] & 4) == 0 && ghost_dx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 8) == 0 && ghost_dy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {

                    if ((screenData[pos] & 15) == 15) {
                        ghost_dx[i] = 0;
                        ghost_dy[i] = 0;
                    } else {
                        ghost_dx[i] = -ghost_dx[i];
                        ghost_dy[i] = -ghost_dy[i];
                    }

                } else {

                    count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }

                    ghost_dx[i] = dx[count];
                    ghost_dy[i] = dy[count];
                }

            }

            ghost_x[i] = ghost_x[i] + (ghost_dx[i] * ghostSpeed[i]);
            ghost_y[i] = ghost_y[i] + (ghost_dy[i] * ghostSpeed[i]);
            drawGhost(g2d, ghost_x[i] + 1, ghost_y[i] + 1);
            // collision() code!
            if (capboy_x > (ghost_x[i] - 12) && capboy_x < (ghost_x[i] + 12)
                    && capboy_y > (ghost_y[i] - 12) && capboy_y < (ghost_y[i] + 12)
                    && inGame) {

                dying = true;
            }
        }
    }

    private void drawGhost(Graphics2D g2d, int x, int y) {
        g2d.drawImage(ghost, x, y, this);
    }

    private void moveCapBoy() {

        int pos;
        short ch;

        if (req_dx == -capboyd_x && req_dy == -capboyd_y) {
            capboyd_x = req_dx;
            capboyd_y = req_dy;
            view_dx = capboyd_x;
            view_dy = capboyd_y;
        }
        // if point is ate, remove from maze, add score;
        if (capboy_x % BLOCK_SIZE == 0 && capboy_y % BLOCK_SIZE == 0) {
            pos = capboy_x / BLOCK_SIZE + N_BLOCKS * (int) (capboy_y / BLOCK_SIZE);
            ch = screenData[pos];
            if ((ch & 16) != 0) {
                screenData[pos] = (short) (ch & 15);
                score++;
            }

            if (req_dx != 0 || req_dy != 0) {
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0)
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {
                    capboyd_x = req_dx;
                    capboyd_y = req_dy;
                    view_dx = capboyd_x;
                    view_dy = capboyd_y;
                }
            }

            // checking 4 any stand-still! stops movement if position is blocked;
            if ((capboyd_x == -1 && capboyd_y == 0 && (ch & 1) != 0)
                    || (capboyd_x == 1 && capboyd_y == 0 && (ch & 4) != 0)
                    || (capboyd_x == 0 && capboyd_y == -1 && (ch & 2) != 0)
                    || (capboyd_x == 0 && capboyd_y == 1 && (ch & 8) != 0)) {
                capboyd_x = 0;
                capboyd_y = 0;
            }
        }
        capboy_x = capboy_x + PACMAN_SPEED * capboyd_x;
        capboy_y = capboy_y + PACMAN_SPEED * capboyd_y;
    }
    
    // determines sprite animation based on movement **NOT WORKING**
    // based on top, left, bottom, && right;
    private void drawCapBoy(Graphics2D g2d) {

        if (view_dx == -1) {
            drawCapBoyLeft(g2d);
        } else if (view_dx == 1) {
            drawCapBoyRight(g2d);
        } else if (view_dy == -1) {
            drawCapBoyUp(g2d);
        } else {
            drawCapBoyDown(g2d);
        }
    }
    // #1 ****
    private void drawCapBoyUp(Graphics2D g2d) {

        switch (capBoyAnimPos) {
            case 1:
                g2d.drawImage(capboy2up, capboy_x + 1, capboy_y + 1, this);
                break;
            case 2:
                g2d.drawImage(capboy3up, capboy_x + 1, capboy_y + 1, this);
                break;
            case 3:
                g2d.drawImage(capboy4up, capboy_x + 1, capboy_y + 1, this);
                break;
            default:
                g2d.drawImage(capBoySprt, capboy_x + 1, capboy_y + 1, this);
                break;
        }
    }
    // #2 ****
    private void drawCapBoyDown(Graphics2D g2d) {

        switch (capBoyAnimPos) {
            case 1:
                g2d.drawImage(capboy2down, capboy_x + 1, capboy_y + 1, this);
                break;
            case 2:
                g2d.drawImage(capboy3down, capboy_x + 1, capboy_y + 1, this);
                break;
            case 3:
                g2d.drawImage(capboy4down, capboy_x + 1, capboy_y + 1, this);
                break;
            default:
                g2d.drawImage(capBoySprt, capboy_x + 1, capboy_y + 1, this);
                break;
        }
    }
    // #3 ****
    private void drawCapBoyLeft(Graphics2D g2d) {

        switch (capBoyAnimPos) {
            case 1:
                g2d.drawImage(capboy2left, capboy_x + 1, capboy_y + 1, this);
                break;
            case 2:
                g2d.drawImage(capboy3left, capboy_x + 1, capboy_y + 1, this);
                break;
            case 3:
                g2d.drawImage(capboy4left, capboy_x + 1, capboy_y + 1, this);
                break;
            default:
                g2d.drawImage(capBoySprt, capboy_x + 1, capboy_y + 1, this);
                break;
        }
    }
    // #4 ****
    private void drawCapBoyRight(Graphics2D g2d) {

        switch (capBoyAnimPos) {
            case 1:
                g2d.drawImage(capboy2right, capboy_x + 1, capboy_y + 1, this);
                break;
            case 2:
                g2d.drawImage(capboy3right, capboy_x + 1, capboy_y + 1, this);
                break;
            case 3:
                g2d.drawImage(capboy4right, capboy_x + 1, capboy_y + 1, this);
                break;
            default:
                g2d.drawImage(capBoySprt, capboy_x + 1, capboy_y + 1, this);
                break;
        }
    }

    // draws from screenData array; (1 = left; 2 = top; 4 = right; 8 = bottom; 16 = point;)
    // goes through all 225;
    private void drawMaze(Graphics2D g2d) {

        short i = 0;
        int x, y;

        for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {

                g2d.setColor(mazeColor);
                g2d.setStroke(new BasicStroke(2));

                // draws left border IF bit of a number is set;
                if ((screenData[i] & 1) != 0) { 
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 2) != 0) { 
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                }

                if ((screenData[i] & 4) != 0) { 
                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 8) != 0) { 
                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 16) != 0) { 
                    g2d.setColor(dotColor);
                    g2d.fillRect(x + 11, y + 11, 2, 2);
                }

                i++;
            }
        }
    }
    
    // startup of game -- define lives, # of ghosts, start score, velocity;
    private void initGame() {

        pacsLeft = 3;
        score = 0;
        initLevel();
        N_GHOSTS = 3;
        currentSpeed = 3;
    }

    private void initLevel() {

        int i;
        for (i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            screenData[i] = levelData[i];
        }

        continueLevel();
    }

    // congratz! now let's go faster, w/ more ghosts;
    private void continueLevel() {

        short i;
        int dx = 1;
        int random;

        for (i = 0; i < N_GHOSTS; i++) {

            ghost_y[i] = 4 * BLOCK_SIZE;
            ghost_x[i] = 4 * BLOCK_SIZE;
            ghost_dy[i] = 0;
            ghost_dx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            ghostSpeed[i] = validSpeeds[random];
        }

        capboy_x = 7 * BLOCK_SIZE;
        capboy_y = 11 * BLOCK_SIZE;
        capboyd_x = 0;
        capboyd_y = 0;
        req_dx = 0;
        req_dy = 0;
        view_dx = -1;
        view_dy = 0;
        dying = false;
    }
    
    // load up the grievers! && player;
    private void loadImages() {
        ghost = new ImageIcon("images/ghost.png").getImage();
        capBoySprt = new ImageIcon("images/capBoy.png").getImage();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);
    }
    
    // draw && fill window for maze
    private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, d.width, d.height);

        drawMaze(g2d);
        drawScore(g2d);
        doAnim();

        // separate start screen graphics from play screen;
        if (inGame) {
            playGame(g2d);
        } else {
            startUpScreen(g2d);
        }
        // private Image ii
        g2d.drawImage(ii, 5, 5, this);
        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }

    // KeyInputs coming at ya!;
    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();
            // move around with AWSD keys;
            if (inGame) {
                if (key == KeyEvent.VK_A) {
                    req_dx = -1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_D) {
                    req_dx = 1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_W) {
                    req_dx = 0;
                    req_dy = -1;
                } else if (key == KeyEvent.VK_S) {
                    req_dx = 0;
                    req_dy = 1;
                    // && ESCAPE to startUpScreen via ESC key;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false;
                } else if (key == KeyEvent.VK_PAUSE) {
                    if (timer.isRunning()) {
                        timer.stop();
                    } else {
                        timer.start();
                    }
                }
            } else {
            	// Starting game via S key;
                if (key == 'S' || key == 's') {
                    inGame = true;
                    initGame();
                }
            }
        }
        // override super classes -- prevented a few NameErrors thrown
        @Override
        public void keyReleased(KeyEvent e) {

            int key = e.getKeyCode();

            if (key == Event.LEFT || key == Event.RIGHT
                    || key == Event.UP || key == Event.DOWN) {
                req_dx = 0;
                req_dy = 0;
            }
        }
    }
    // override super classes -- prevented a few NameErrors thrown
    @Override
    public void actionPerformed(ActionEvent e) {

        repaint();
    }
}