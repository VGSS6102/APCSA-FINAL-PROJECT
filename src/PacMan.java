import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import javax.swing.*;

/**
 * Game class
 */
public class PacMan extends JPanel implements ActionListener, KeyListener{

    /**
     * Class that defines each element on the screen
     */
    class Block {
        // Sprite parameters 
        int x;
        int y;
        int width;
        int height;
        Image image;

        // Movement parameters
        int startX;
        int startY;
        char direction = 'U'; // U D L R 
        int velocityX = 0;
        int velocityY = 0;
        int speed = tileSize/4;

        Block(Image image, int x, int y , int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        /**
         * Update direction of movement of the sprite
         * @param direction letter representing direction 
         */
        void updateDirection(char direction) {
            char prevDirection = this.direction; //saves previous direction
            this.direction = direction; //sets new direction of movement of the sprite
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;

            // Check every wall element for colision with this sprite
            for(Block wall : walls) { 
                // Stop moving if this sprite collides with wall element
                if (collision(this, wall)){
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }
        

        /**
         * Update movement direction based on direction variable
         */
        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -speed; //each frame going 8 pixels
            }
            else if (this.direction == 'D') { 
                this.velocityX = 0;
                this.velocityY = speed;
            }
            else if (this.direction == 'L') {
                this.velocityX = -speed;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') { 
                this.velocityX = speed;
                this.velocityY = 0;
            }
            
        }

        /** 
         * Places this sprite into it's starting position
         */
        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    // define map parameters
    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    // define wall images
    // blue, purple, green, white
    private Image wallImageB;
    private Image wallImageP;
    private Image wallImageG;
    private Image wallImageW;
    private Image currWallImage;
    

    // define ghost images
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    // define player sprite directional images 
    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    
    // declare sets of all sprites 
    HashSet<Block> walls; 
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop; // declare a game loop that will update the game
    char[] directions = {'U', 'D', 'L', 'R'}; // list of possible directions for ghosts
    Random random = new Random(); 
    int score = 0; // score of current match
    int highScore = getHighScore(); // high score of all time
    int lives = 3; // starting lives
    boolean gameOver = false; // status flag determines if game is over
    boolean blackOut = false; // status flag for lightswitch off (WIP)
    int room = 0; // starting room / current room (WIP)

    /* DEFINE MAP LAYOUTS FOR EACH ROOM
        X = wall, O = skip, P = pac man, ' ' = food
        Ghosts: b = blue, o = orange, p = pink, r = red
    */
    private String[][] tileMap = {{
            "XXXXXXXXXXXXXXXXXXX", // LEVEL 1
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXX XXXX XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXrXX X XXXX",
            "O      XbpoX      O",
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
        },
        {
            "XXXXXXXXXXXXXXXXXXX", // LEVEL 2
            "X        X        X",
            "X XX X X X XXX XX X",
            "X                 X",
            "X XX X   XXX X XX X",
            "X    X       X    X",
            "X  X XXXX   XX X  X",
            "XOOX X       X XOOX",
            "XXXX X XXrXX X XXXX",
            "O      XbpoX      O",
            "XXXX X XXXXX X XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X X   X X X XX",
            "X    X   X   X    X",
            "X XXX XX X X  XXX X",
            "X        X        X",
            "XXXXXXXXXXXXXXXXXXX" 
        },
        {
            "XXXXXXXXXXXXXXXXXXX", // LEVEL 3
            "X        X        X",
            "X XX X X X XXX XX X",
            "X                 X",
            "X XX X   XXX X XX X",
            "X    X       X    X",
            "X  X XXXX   XX X  X",
            "XOOX X       X XOOX",
            "XXXX X XXrXX X XXXX",
            "O      XbpoX      O",
            "XXXX X XXXXX X XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X X   X X X XX",
            "X    X   X   X    X",
            "X XXX XX X X  XXX X",
            "X        X        X",
            "XXXXXXXXXXXXXXXXXXX" 
        },
        {
            "XXXXXXXXXXXXXXXXXXX", // LEVEL 4
            "X        X        X",
            "X XX X X X XXX XX X",
            "X                 X",
            "X XX X   XXX X XX X",
            "X    X       X    X",
            "X  X XXXX   XX X  X",
            "XOOX X       X XOOX",
            "XXXX X XXrXX X XXXX",
            "O      XbpoX      O",
            "XXXX X XXXXX X XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X X   X X X XX",
            "X    X   X   X    X",
            "X XXX XX X X  XXX X",
            "X        X        X",
            "XXXXXXXXXXXXXXXXXXX" 
        },
    };



    PacMan() {
        // setup a game window
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        // load images of each element on the screen
        wallImageB = new ImageIcon(getClass().getResource("./wallB.png")).getImage();
        wallImageP = new ImageIcon(getClass().getResource("./wallP.png")).getImage();
        wallImageG= new ImageIcon(getClass().getResource("./wallG.png")).getImage();
        wallImageW = new ImageIcon(getClass().getResource("./wallW.png")).getImage();
        currWallImage = wallImageB;


        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        // Draw a map
        loadMap();
        for (Block ghost : ghosts) {
            // set random direction of movement for ghosts from the start
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }

        // Setup a game loop with update delay of 50 miliseconds
        gameLoop = new Timer(50, this); //20fps
        gameLoop.start();
    }

    /**
     * Create a gaming area following a map of the game
     */
    public void loadMap() {
        // Initialize sets of elements to create
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();

        // Go through each 
        for (int r = 0; r < rowCount; r++) {
            for (int c  = 0; c < columnCount; c++) {
                //read rows
                String row = tileMap[room][r];

                //read characters
                char tileMapChar = row.charAt(c);
                
                //calculate coordinates of character
                int x = c*tileSize;
                int y = r*tileSize;


                // For each character place corresponding object
                if (tileMapChar == 'X') { //place wall 
                    if (room == 0) currWallImage = wallImageB;
                    else if (room == 1) currWallImage = wallImageG;
                    else if (room == 2) currWallImage = wallImageP;
                    else if (room == 3) currWallImage = wallImageW;
                    Block wall = new Block(currWallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'b') { //create blue ghost
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'o') { //create orange ghost
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'p') { // create pink ghost
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'r') { // create red ghost
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'P') { // Create player
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') { //place food
                    // draw a small rectangle in the center of the cell
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }
    }


    /**
     * Method responsible for updating the screen 
     * @param g Graphics class object
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    /**
     * Draw gameboard
     * @param g Graphics class object
     */
    public void draw(Graphics g) {

        // If game is over display a game over screen
        if (gameOver) {
            displayGameOver(g, score, room);

        }
        // if game is not over draw each element on their own position
        else {
            
            // draw player
            g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

            // draw food
            g.setColor(Color.WHITE);
            for (Block food : foods) {
                g.fillRect(food.x, food.y, food.width, food.height);
            }

            // draw ghosts
            for (Block ghost : ghosts) {
                g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
            }

            // draw walls
            for (Block wall : walls) {
                if (aroundPlayer(wall, pacman))
                g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
            }

            // draw stats: number of lives, score, and highscore at the top of the screen 
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.setColor(Color.RED);
            g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score) + " H: " + String.valueOf(highScore), tileSize/2, tileSize/2);
            
        }
        
    }
    
    private boolean aroundPlayer(Block object, Block player){
        int radius = 1;
        boolean around = (object.x <= player.x+radius && object.x >= player.x-radius)
        || (object.y <= player.y+radius && object.y >= player.y-radius);
        System.out.println("AroundPlayer: " + String.valueOf(around) + "\n PacmanX = " + player.x + " PacmanY = " + player.y "\n ObjectX = ");
        
        return around;
    }

    /**
     * Method to caclculate moves for all objects and their collisions
     */
    public void move() { 
        // Proceed moving player along direction they were moving
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        // check wall collisions with player and hold back of it happens
        for (Block wall : walls) { 
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }


        // check ghost collisions with player and their moves
        for (Block ghost : ghosts) { 
            // if (collision(ghost, pacman)){
            //     lives -= 1;
            //     if (lives < 1) {
            //         gameOver = true;
            //         return;
            //     }
            //     resetPositions();
            // }
            if (ghost.y == tileSize*9 && ghost.direction != 'U' && ghost.direction != 'D'){
                ghost.updateDirection(directions[random.nextInt(2)]);
            }
            if (intersection(ghost)){
                ghost.updateDirection(directions[random.nextInt(4)]);
            }
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            for (Block wall : walls) { 
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth){
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }
        }
        // check food colisions with player
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)){
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        // Switch rooms if there's no food left in previous one
        if (foods.isEmpty()) {
            if ((room+1) <= tileMap.length-1){
                room++;
                loadMap();
                resetPositions();
            }
            else { // reset back when reached last room
                room = 0;
                currWallImage = wallImageB;
                loadMap();
                resetPositions();
            }
            
        }

    }

    /**
     * Check if two objects are colliding with each other
     * @param a Block class object
     * @param b Block class object
     * @return true or false depending on collision
     */
    public boolean collision(Block a, Block b) {
        return  a.x < b.x + b.width && // check if object b at x pos of object a
                a.x + a.width > b.x && // check if object a at x pos of object b
                a.y < b.y + b.height && // check if object b at y pos of object a
                a.y + a.height > b.y; // check if object a at y pos of object b
    }


    /**
     * Looks for 3 or 4 ways ghost can go
     * @param ghost
     * @return is there intersection or not
     */
    private boolean intersection(Block ghost){
        // boolean atIntersection = false;
        int ways = 4;
        for (Block wall : walls){
        }
        return ways < 2;
    }

    /**
     * Reset positions of all moving object like player and ghosts
     */
    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;

        // reset each existing ghost
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }


    /**
     * Opens a highscore text file and returns it's value
     * @return highScore
     */
    public int getHighScore(){

        int highScore = score;
        
        try {
            // FileReader reader = new FileReader("highscore.txt");
            // BufferedReader bufferedReader = new BufferedReader(reader);
            File myFile = new File("highscore.txt");
            Scanner myReader = new Scanner(myFile);
 
            String line = myReader.nextLine();
            System.out.println(line);
            highScore = Integer.parseInt(line);      
            
            myReader.close();

            
        } catch (IOException e) {
        }

        return highScore;
    }

    /**
     * Write current score into highscore text file if score is higher than highscore
     * @param score current score
     */
    public void saveHighScore(int score){
        if (score > highScore){
            try {
                FileWriter writer = new FileWriter("highscore.txt", false);
                writer.write(String.valueOf(score));
                writer.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Shows a blank GameOver screen with score, highscore, and room
     * @param g Graphics class objec
     * @param score int variable for score
     * @param room index number of the room
     */
    public void displayGameOver(Graphics g, int score, int room) {
        gameLoop.stop();
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.setColor(Color.RED);
        g.drawString("Game Over",boardWidth/3, boardHeight/2-50);

        saveHighScore(score);
        g.drawString("Score: " + String.valueOf(score) + " High: " + String.valueOf(getHighScore()), boardWidth/5, boardHeight/2+25);
    }

    /**
     * Listens for any keypress 
     * and updates the screen until game is over
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}


    /**
     * Listens for keypresses and
     * Updates directions of player
     */
    @Override
    public void keyReleased(KeyEvent e) {
        
        if (gameOver) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER|| e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                loadMap();
                resetPositions();
                lives = 3;
                score = 0;
                room = 0;
                highScore = getHighScore();
                blackOut = false;
                gameOver = false;
                gameLoop.start();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
            pacman.updateDirection('U');
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
            pacman.updateDirection('D');
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
            pacman.updateDirection('L');
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
            pacman.updateDirection('R');
        }
        else if (e.getKeyCode() == KeyEvent.VK_G){
            room = (room+1) > tileMap.length-1 ? 0 : room+1;
            loadMap();
            resetPositions();
        }

        if (pacman.direction == 'U'){
            pacman.image = pacmanUpImage;
        }
        else if (pacman.direction == 'D'){
            pacman.image = pacmanDownImage;
        }
        else if (pacman.direction == 'L'){
            pacman.image = pacmanLeftImage;
        }
        else if (pacman.direction == 'R'){
            pacman.image = pacmanRightImage;
        }

    }
}
