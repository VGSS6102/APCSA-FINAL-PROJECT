import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.chrono.ThaiBuddhistChronology;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import javax.swing.*;

/**
 * Game class
 * @author Andrii Dubinskyi (ADDITIONS)
 * @author Kenny Yip Coding (BASE)
 * @see https://www.youtube.com/watch?v=lB_J-VNMVpE
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

        // Constructor for each block
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

    private Image healFoodImage;

    
    // declare sets of all sprites 
    HashSet<Block> walls; 
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    HashSet<Block> heals;
    Block portalOne;
    Block portalTwo;
    Block pacman;

    Timer gameLoop; // declare a game loop that will update the game
    char[] directions = {'U', 'D', 'L', 'R'}; // list of possible directions for ghosts
    Random random = new Random(); 
    int score = 0; // score of current match
    int highScore = getHighScore(); // high score of all time
    int lives = 3; // starting lives
    boolean gameOver = false; // status flag determines if game is over
    // boolean blackOut = false; // status flag for lightswitch off (WIP)
    int room = 0; // starting room / current room 
    boolean followingPacman = false;
    int amountOfFood;

    /* DEFINE MAP LAYOUTS FOR EACH ROOM
        X = wall, O = skip, P = pac man, ' ' = food, H = heal item
        Ghosts: b = blue, o = orange, p = pink, r = red,
        Portals: @ = portal 1, # portal 2
    */
    private String[][] tileMap = {{
            "XXXXXXXXXXXXXXXXXXX", // LEVEL 1
            "X        X    H   X",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXX XXXX XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXrXX X XXXX",
            "@      XbpoX      #",
            "XXXX X XXXXX X XXXX",
            "OOOX X   H   X XOOO",
            "XXXX X XXXXX X XXXX",
            "X H      X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X H  X",
            "X XXXXXX X XXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX" 
        },
        {
            "XXXXXXXXXXXXXXXXXXX", // LEVEL 2
            "Xr               pX",
            "X   XX       XX   X",
            "X  XOOX     XOOX  X",
            "X  XOOO     OXXO  X",
            "X  XOOX     XOXX  X",
            "X   XX       XX   X",
            "X                 X",
            "X                 X",
            "X                 X",
            "X                 X",
            "X                 X",
            "X                 X",
            "X    X     XOOOX  X",
            "X    X     XOOOX  X",
            "X    X     XOOOX  X",
            "X    X      XOX   X",
            "X XOOX      XPX   X",
            "X  XX        X    X",
            "Xo               bX",
            "XXXXXXXXXXXXXXXXXXX" 
        },
        {
            "XXXXXXXXXXXXXXXXXXX", // LEVEL 3
            "XP    XOOOX       X",
            "XXXXX XXXXX XXXXX X",
            "XOOOX       XOOOX X",
            "XXXXXXXXXXXXXXXXX X",
            "X   X   X   X   X X",
            "X X X X X X X X X X",
            "X X   X   X   X   X",
            "X XXXXXXXXXXXXXXXXX",
            "X XXXpXOXXXXXoXOXXX",
            "X XXXOXOX   XOXOXXX",
            "X XXXOXOX   XOXOXXX",
            "X         H      O#",
            "XXXXXOXOX   XOXOXXX",
            "XOOOXOXrX   XOXbXrX",
            "XOOOXXXXXXXXXXXXXOX",
            "XXXXX        XXXX X",
            "@O     XXXXX      X",
            "XXXXX  X H X  XXXXX",
            "OOOOX         XOOOO",
            "OOOOXXXXXXXXXXXOOOO" 
        },
        {
            "XXXXXXXXXXXXXXXXXXX", // LEVEL 4
            "X        P        X",
            "X                 X",
            "X  X   X   X   X  X",
            "X  X   X   X   X  X",
            "X    X   X   X    X",
            "X    X H X   X    X",
            "X                 X",
            "XXXXXX XX XX XXXXXX",
            "X      X   X  H   X",
            "X XXXXXX   XXXXXX X",
            "X      X b X      X",
            "XXXXXX X H X XXXXXX",
            "Xo     X   X     oX",
            "X XXXXXXX XXXXXXX X",
            "X r    H        p X",
            "X   X  XXXXX  X   X",
            "X      X   X      X",
            "X   X  XX XX  X   X",
            "X          H      X",
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
        
        healFoodImage = new ImageIcon(getClass().getResource("./cherry2.png")).getImage();

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
        heals = new HashSet<Block>();
        

        // Go through each symbol of the tileMap
        for (int r = 0; r < rowCount; r++) {
            for (int c  = 0; c < columnCount; c++) {
                //read rows
                String row = tileMap[room][r];

                //read characters
                char tileMapChar = row.charAt(c);
                
                //calculate coordinates of character
                //it places each object to it's pixel coords, not their cell 
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
                else if (tileMapChar == '@') { // Create left portal
                    portalOne = new Block(null, x-tileSize-5, y, tileSize, tileSize);
                }
                else if (tileMapChar == '#') { // Create right portal
                    portalTwo = new Block(null, x+tileSize+5, y, tileSize, tileSize);
                }
                else if (tileMapChar == 'H') { // Crate healing cherry
                    Block heal = new Block(healFoodImage, x+tileSize/3, y+tileSize/3, tileSize/2, tileSize/2);
                    heals.add(heal);
                }
                else if (tileMapChar == ' ') { //place food on blank spaces
                    // draw a small rectangle in the center of the cell
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                    amountOfFood++;
                }
            }
        }
    }


    /**
     * Method responsible for updating the screen 
     * @param g Graphics class object
     */
    @Override
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
                g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
            }

            // draw healing cherries
            for (Block heal : heals) {
                if (lives < 2)
                g.drawImage(heal.image, heal.x, heal.y, heal.width, heal.height, null);
            }

            // draw stats: number of lives, score, and highscore at the top of the screen 
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.setColor(Color.RED);
            String stats = "x" + String.valueOf(lives) + " Score: " + String.valueOf(score) + " H: " + String.valueOf(highScore);
            if (gameLoop.isRunning() == false) stats += " PAUSED";
            if (followingPacman) stats += " !HUNT!";
            g.drawString(stats, tileSize/2, tileSize/2);
            
        }
        
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

        // Determine if player going through the portals
        // then teleport them to another portal
        if (collision(portalOne, pacman)) {
            pacman.x = portalTwo.x-tileSize-5;
            pacman.y = portalTwo.y;
        }
        else if (collision(portalTwo, pacman)){
            pacman.x = portalOne.x+tileSize+5;
            pacman.y = portalOne.y;
        }

        // check ghost collisions with player and their moves
        for (Block ghost : ghosts) { 
            if (collision(ghost, pacman)){
                lives -= 1;
                if (lives < 1) {
                    gameOver = true;
                    return;
                }
                resetPositions();
            }
            // Resolve issue of ghosts being stuck in their starting room
            if (ghost.y == tileSize*9 && ghost.direction != 'U' && ghost.direction != 'D'){
                ghost.updateDirection(directions[random.nextInt(2)]);
            }
            // Check if ghost at intersection
            if (intersection(ghost) && room <= 1 /*&& !followingPacman*/){
                if (random.nextInt(15) == 8)
                ghost.updateDirection(directions[random.nextInt(4)]);
            }
            // Execute following algorithm when following flag is on
            if (followingPacman && room != 2){
                ghost.speed = tileSize/8;
                followPacman(ghost);
            } else {ghost.speed = tileSize/4;}

            // Teleport ghosts
            if (collision(portalOne, ghost)) {
                ghost.x = portalTwo.x-tileSize-5;
                ghost.y = portalTwo.y;
            }
            else if (collision(portalTwo, ghost)){
                ghost.x = portalOne.x+tileSize+5;
                ghost.y = portalOne.y;
            }
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            for (Block wall : walls) { 
                if (collision(ghost, wall)){
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

        // check heals colisions with player
        Block healUsed = null;
        for (Block heal : heals){
            if (collision(pacman, heal) && lives < 3){
                healUsed = heal;
                lives += 1;
            }
        }
        heals.remove(healUsed);

        // Switch rooms if there's no food left in previous one
        if (foods.isEmpty()) {
            if ((room+1) <= tileMap.length-1){
                room++;
                followingPacman = false;
                loadMap();
                resetPositions();
            }
            else { // reset back when reached last room
                room = 0;
                currWallImage = wallImageB;
                followingPacman = false;
                loadMap();
                resetPositions();
            }
            
        }
        // make ghosts follow player when there's 10% of all food left 
        else if (foods.size() <= (int)((10.0/100.0)*amountOfFood)){
            followingPacman = true;
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
     * @param ghost Block class object
     * @return is there intersection or not
     */
    private boolean intersection(Block ghost){
        // boolean atIntersection = false;
        int ways = 4;
        for (Block wall : walls){
            if (wall.x == ghost.x+tileSize && wall.y == ghost.y){
                ways--;
                // System.out.println("to the right");
            } //from the right of the ghost
            else if (wall.x == ghost.x-tileSize && wall.y == ghost.y){
                ways--;
                // System.out.println("to the left");
            } // to the left of the ghost
            else if (wall.y == ghost.y+tileSize && wall.x == ghost.x){
                ways--;
                // System.out.println("down to");
            }// down to the ghost
            else if (wall.y == ghost.y-tileSize && wall.x == ghost.x){
                ways--;
                // System.out.println("up to");
            }// up to the ghost
        }
        return (ways > 2);
    }

    /**
     * Makes ghost to follow pacman through the map
     * @param ghost Object that would follow pacman
     */
    public void followPacman(Block ghost){
        // save stats before following pacman
        char prevDirection = ghost.direction;
        int prevDistance = distanceTo(ghost, pacman)/tileSize;
        ghost.speed = tileSize/8;

        // go towards pacman in the straight line
        ghost.updateDirection(relativeDirection(ghost, pacman));
        // System.out.println("Distance From Ghost - To Pacman: " + distanceTo(ghost, pacman));

        // check if distance is changing while moving towards pacman
        if (distanceTo(ghost, pacman)/tileSize > prevDistance){
            ghost.updateDirection(directions[random.nextInt(4)]);
        }

        else {
            ghost.updateDirection(prevDirection);  
        }
        
    }

    /**
     * Returns distance between two objects
     * @param a Block class object
     * @param b Block class object
     * @return int distance pixels
     */
    int distanceTo(Block a, Block b){
        int distance = Math.abs((int) Math.sqrt(Math.pow((b.x - a.x),2)+ Math.pow((b.y - a.y), 2)));
        return distance;
    }

    /**
     * Determines direction of one object towards another
     * @param from the one that rotates
     * @param to the one towards being rotated to
     * @return char direction
     */
    char relativeDirection(Block from, Block to){

        if (from.x < to.x){
            return 'R';
        }
        else if (from.x > to.x){
            return 'L';
        }
        
        else if (from.y < to.y){
            return 'D';
        }
        else {
            return 'U';
        }
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
        // set high score as a last score, if theres something with a file
        int highScore = score;
        
        // read text file containing highscore
        try {
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
        // edit file with highscore if current score is bigger than the highscore last recorded
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
        gameLoop.stop(); // stop game 

        // setup font
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.setColor(Color.RED);

        // display gameover text
        g.drawString("Game Over",boardWidth/3, boardHeight/2-50);

        // save new higscore and display it below
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
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_J){
            int i = amountOfFood;
            for (Block food : foods){
                if (i <= (int)((90.0/100.0)*amountOfFood)) break;
                foods.remove(food);
                i--;
            }
            System.out.println("Removed " + i + " food");
        }
    }


    /**
     * Listens for keypresses and
     * Updates directions of player
     */
    @Override
    public void keyReleased(KeyEvent e) {
        
        // restart game on Enter Space or Escape when its game over
        if (gameOver) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER|| e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                lives = 3;
                score = 0;
                room = 0;
                highScore = getHighScore();
                gameOver = false;
                followingPacman = false;
                loadMap();
                resetPositions();
                gameLoop.start();
            }
        }

        // Record keypresses WASD and arrows to operate  pacman
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

        /* Debug keybinds
        1. Changes rooms on "G"
        2. On/Off switch for following pacman
        3. Increase health
        4. Decrease health
        */ 
        else if (e.getKeyCode() == KeyEvent.VK_G){
            room = (room+1) > tileMap.length-1 ? 0 : room+1;
            followingPacman = false;
            loadMap();
            resetPositions();
        }
        else if (e.getKeyCode() == KeyEvent.VK_F){
            followingPacman = !followingPacman;

        }
        else if (e.getKeyCode() == KeyEvent.VK_Y){
            if (lives < 3)
            lives++;

        }
        else if (e.getKeyCode() == KeyEvent.VK_H){
            if (lives > 1)
            lives--;
            else gameOver = true;  
        }
        else if (e.getKeyCode() == KeyEvent.VK_P){
            if (gameLoop.isRunning()) { gameLoop.stop(); } 
            else gameLoop.start();
        }

        // Change texture of the player depending on  direction
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
