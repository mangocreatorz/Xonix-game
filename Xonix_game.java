// XONIX GAME
// Created by Sergey Kirichenko
// Suleyman Demirel University
// 1EN03F
// All rights reserved
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.Random;
import static javafx.scene.input.KeyCode.ESCAPE;


public class Xonix_game extends Application {
    MediaPlayer mediaplayer;
    final int POINT_SIZE = 10;//size of field's element
    final int FIELD_WIDTH = 1600 / POINT_SIZE;//field width
    final int FIELD_HEIGHT = 900 / POINT_SIZE;//height
    final int FIELD_DX = 6;
    final int FIELD_DY = 28 + 28;
    final int LEFT = 37; // direction
    final int UP = 38;
    final int RIGHT = 39;
    final int DOWN = 40;
    final int SHOW_DELAY = 60; // delay for animation (similar to FPS)

    //logic colors
    final int COLOR_TEMP = 1; // temporary color
    final int COLOR_WATER = 0;//water color
    final int COLOR_LAND = 0x00a8a8;//land color
    final int COLOR_TRACK = 0x901290;//track color

    //real colors
    Color color_land = Color.rgb(0x00, 0xa8, 0xa8);
    Color color_track = Color.rgb(0x90, 0x12, 0x90);
    Color color_water = Color.rgb(0x00, 0x00, 0x00);

    final int PERCENT_OF_WATER_CAPTURE = 75;//percentage of needed field
    final String FORMAT_STRING = "Score: %d %20s %d %20s %2.0f%%";

    AnimationTimer timer;//timer

    Random random = new Random();//Random util

    Delay delay = new Delay();//delay object
    Field field = new Field();//field object
    Xonix xonix = new Xonix();//xonix object
    Balls balls = new Balls();//object with enemies
    Cube cube = new Cube();//outside cube
    GameOver gameover = new GameOver();//GAMEOVER object

    Canvas canvas;//canvas for drawing
    GraphicsContext gc;//class for drawing
    private Button retry_button;
    private Button backfromgame;
    private VBox context_menu;
    private Scene main_scene;
    private Button cont_button;
    private Button sound_buttonon;
    private Button sound_buttonoff;



    @Override
    public void start(Stage window) throws Exception {
        //main layout for game
        BorderPane game_root = new BorderPane();


        //game scene and css
        Scene game_scene = new Scene(game_root, POINT_SIZE * FIELD_WIDTH, POINT_SIZE * FIELD_HEIGHT + FIELD_DY);
        game_scene.getStylesheets().add(getClass().getResource("game.css").toString());

        //canvas for drawing
        canvas = new Canvas(POINT_SIZE * FIELD_WIDTH + FIELD_DX, POINT_SIZE * FIELD_HEIGHT + FIELD_DY);
        // getting Graphics Contest for drawing
        gc = canvas.getGraphicsContext2D();
        canvas.setFocusTraversable(true);

        cont_button = new Button("Continue");
        cont_button.setMinWidth(200);
        cont_button.setMinHeight(50);
        cont_button.getStyleClass().addAll("cont_button");
        cont_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                context_menu.setVisible(false);
                timer.start();
            }
        });


        retry_button = new Button("Retry");
        retry_button.setMinWidth(200);
        retry_button.setMinHeight(50);
        retry_button.getStyleClass().addAll("retry_button");

        backfromgame = new Button("To menu");
        backfromgame.setMinHeight(50);
        backfromgame.setMinWidth(200);
        backfromgame.getStyleClass().addAll("backfromgame");
        backfromgame.setOnAction(e-> window.setScene(main_scene));

        sound_buttonon = new Button("Sound ON");
        sound_buttonon.setMinHeight(50);
        sound_buttonon.setMinWidth(200);
        sound_buttonon.getStyleClass().addAll("backfromgame");
        sound_buttonon.setOnAction(e-> mediaplayer.play());

        sound_buttonoff = new Button("Sound OFF");
        sound_buttonoff.setMinHeight(50);
        sound_buttonoff.setMinWidth(200);
        sound_buttonoff.getStyleClass().addAll("backfromgame");
        sound_buttonoff.setOnAction(e-> mediaplayer.stop());

        game_root.getChildren().addAll(canvas, backfromgame);//добавляем канву на панель

        // action on button in game, when we have "died"
        retry_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                System.out.println("button!");
                field.reset();//reseting all objects on fiels
                field.init();//setting default parameters for field
                xonix.reset();
                xonix.init();//setting default parameters for field
                cube.init();//setting default parameters for outside cube
                balls.reset();//increasting amount of balls (enemies)
                gameover.reset();
                timer.start();//starting timer
                context_menu.setVisible(false);


            }
        });

        // MENU WHICH IS SHOWED WHEN GAME IS OVER OR WHEN ESCAPE BUTTON PRESSED
        context_menu = new VBox();
        game_root.getChildren().addAll(context_menu);
        context_menu.getChildren().addAll(cont_button,retry_button,sound_buttonon,sound_buttonoff,backfromgame);
        context_menu.setSpacing(30);
        context_menu.setVisible(false);
        context_menu.setTranslateX(700);
        context_menu.setTranslateY(400);


        //setting action on canvas
        canvas.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                KeyCode key = e.getCode();//getting "code" from keyboard
                if (key.equals(KeyCode.UP)) {
                    xonix.setDirection(UP);//setting direction for xonix
                }
                if (key.equals(KeyCode.DOWN)) {
                    xonix.setDirection(DOWN);
                    context_menu.setVisible(false);
                }
                if (key.equals(KeyCode.LEFT)) {
                    xonix.setDirection(LEFT);
                    context_menu.setVisible(false);
                }
                if (key.equals(KeyCode.RIGHT)) {
                    xonix.setDirection(RIGHT);
                    context_menu.setVisible(false);
                    timer.start();
                }
                if (key.equals(KeyCode.P)) {
                   // window.setScene(main_scene);
                    timer.stop();
                }
                if (key.equals(KeyCode.O)) {
                    timer.start();
                }
                if (key.equals((ESCAPE))) {
                    timer.stop();
                    context_menu.setVisible(true);
                }
            }
        });

        //creating timer
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                go();//в таймере постоянно запускается функция go
            }
        };
        timer.start();//starting timer for game



        //MAIN MENU BUTTONS
        Button start_button = new Button("Start game");
        start_button.setTranslateY(380);
        start_button.setPrefSize(350, 70);
        start_button.getStyleClass().add("button");
        start_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            window.setScene(game_scene);
            timer.start();
            context_menu.setVisible(false);
            }
        });
        start_button.setTranslateX(1000);

        Button info_button = new Button("How to play");
        info_button.getStyleClass().addAll("button");
        info_button.setPrefSize(350,70);
        info_button.setTranslateX(1000);
        info_button.setTranslateY(390);

        Button exit_button = new Button("Exit");
        exit_button.setTranslateY(400);
        exit_button.setTranslateX(1000);
        exit_button.getStyleClass().add("button");
        exit_button.setPrefSize(350, 70);



        // I used stackpane to make a background because gif working with stackpane much better than default root
        VBox buttons_vBox = new VBox();
        StackPane stackPane = new StackPane();
        stackPane.setPrefSize(1920, 1080);
        stackPane.getStyleClass().add("stackpane");
        stackPane.getChildren().add(buttons_vBox);
        buttons_vBox.setSpacing(30.0);
        buttons_vBox.getChildren().addAll(start_button,info_button,exit_button);
        buttons_vBox.setTranslateX(100);

        // gridpane for stackpane
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.add(stackPane, 0, 0);
        main_scene = new Scene(gridPane, 1600, 900);
        main_scene.getStylesheets().add(getClass().getResource("main_menu.css").toString());

        // MUSIC PARAMETERS
        Media musicFile = new Media("file:///E:/Off_Limits.mp3");
        mediaplayer = new MediaPlayer(musicFile);
        mediaplayer.setAutoPlay(true);
        mediaplayer.setVolume(0.3);
        mediaplayer.setAutoPlay(true);
        mediaplayer.setCycleCount(100000);


        // EXIT SCENE PARAMETERS
        VBox vbox_exit = new VBox();
        vbox_exit.setSpacing(10);
        GridPane gridpane_exit = new GridPane();
        gridpane_exit.setAlignment(Pos.CENTER);
        gridpane_exit.add(vbox_exit, 0, 0);

        Button yes_button = new Button("Yes");
        yes_button.setPrefSize(300, 60);
        yes_button.setTranslateY(20);
        yes_button.getStyleClass().add("Exit_Buttons");
        yes_button.setOnAction(e -> System.exit(0));

        Button no_button = new Button("No");
        no_button.setTranslateY(20);
        //vBox.setSpacing(10);
        no_button.setPrefSize(300, 60);
        no_button.getStyleClass().add("Exit_Buttons");
        no_button.setOnAction(e -> window.setScene(main_scene));
        vbox_exit.getChildren().addAll(yes_button, no_button);
        buttons_vBox.getStyleClass().add("VBOX");
        Scene exit_scene = new Scene(gridpane_exit, 1600, 900);
        exit_scene.getStylesheets().add(getClass().getResource("exit.css").toString());


        // HOW TO PLAY PARAMETERS
        Pane game_info_pane = new Pane();
        Scene game_info_scene = new Scene(game_info_pane,1600,900);
        Button back = new Button("Back");
        back.setMinHeight(35);
        back.setMinWidth(100);
        back.setTranslateX(1450);
        back.setTranslateY(850);
        back.getStyleClass().addAll("backbutton");
        game_info_pane.getChildren().addAll(back);
        game_info_scene.getStylesheets().add(getClass().getResource("info.css").toString());

        //ACTIONS ON MAIN BUTTONS
        exit_button.setOnAction(e -> window.setScene(exit_scene));
        info_button.setOnAction(e->window.setScene(game_info_scene));
        back.setOnAction(e->window.setScene(main_scene));

        // WINDOW BASIC SETTINGS
        window.setTitle("Xonix");
        window.setResizable(false);
        window.setFullScreen(false);
        window.setScene(main_scene);
        window.getIcons().add(new Image(getClass().getResourceAsStream("ds.png")));
        window.initStyle(StageStyle.UNDECORATED);
        window.show();

    }








    @Override
    public void stop() {//method to stop the game
        timer.stop();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {//главная функция
        launch(args);//запускается JavaFX программа
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    //method which is converting colors into real game colors
    Color makeColorFromInt(int color) {
        switch (color) {
            case COLOR_LAND:
                return color_land;
            case COLOR_TRACK:
                return color_track;
            case COLOR_WATER:
                return color_water;
        }
        return Color.rgb(0, 0, 0);
    }

    // Main Function, main cycle of game engine
    void go() { //

        if (gameover.isGameOver()) {//if game is over
            timer.stop();//animation is stopped
            context_menu.setVisible(true);


        }
        xonix.move();//moving xonix
        balls.move();//moving balls
        cube.move();//moving outstide cube

        //for secure, getting Graphics Contest again
        gc = canvas.getGraphicsContext2D();

        //DRAWINGS FOR ALL OBJECTS
        field.paint(gc);//drawing field for Graphics Context
        xonix.paint(gc);//drawing xonix for Graphics Context
        balls.paint(gc);//drawing all balls (enemies)
        cube.paint(gc);//drawing outside cube
        gameover.paint(gc);//drawing GAME OVER

        gc.setFill(Color.BLACK);//filling Graphics Context in black color
        //drawing statistics
        gc.fillRect(0, POINT_SIZE * FIELD_HEIGHT,
                POINT_SIZE * FIELD_WIDTH + FIELD_DX,
                POINT_SIZE * FIELD_HEIGHT + FIELD_DY);

        gc.setFont(Font.font("", FontWeight.BOLD, FontPosture.REGULAR, 21));//Font for statistic
        gc.setFill(Color.WHITE);//setting white color
        //setting alignment for statistic
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        //showing all text
        gc.fillText(String.format(FORMAT_STRING, field.getCountScore(), "HP:", xonix.getCountLives()>0?xonix.getCountLives():0, "Full:", field.getCurrentPercent()), (POINT_SIZE * FIELD_WIDTH + FIELD_DX) / 2, POINT_SIZE * FIELD_HEIGHT + FIELD_DY / 2);

        delay.wait(SHOW_DELAY);//delay properties

        if (xonix.isSelfCrosed() || balls.isHitTrackOrXonix() || cube.isHitXonix()) {
            //If xonix have crossed himself, balls hit xonix and other scenarios

            xonix.decreaseCountLives();//decreasing amount of lives by situation

            if (xonix.getCountLives() > 0) {//if amount of lives < 0
                xonix.init();//setting default properties for xonix
                field.clearTrack();//clear out field
                delay.wait(SHOW_DELAY * 10);//freeze for some time
            }
        }

        if (field.getCurrentPercent() >= PERCENT_OF_WATER_CAPTURE) {//если процент осушенной воды достиг нужного уровня то... If percentage of "deleted" area near 75
            field.init();//setting default properties for our field
            xonix.init();//setting default properties for xonix
            cube.init();//setting default properties for outside cube
            balls.add();//INCREASING amount of enemies
            delay.wait(SHOW_DELAY * 10);//getting frozen for some time
        }

    }

    //Field class
    class Field {

        private final int WATER_AREA = (FIELD_WIDTH - 4) * (FIELD_HEIGHT - 4);//Size of water
        private int[][] field = new int[FIELD_WIDTH][FIELD_HEIGHT];//Array with field information
        private float currentWaterArea;//amount of water left
        private int countScore = 0;//Amount of score

        Field() {
            init();//setting field
        }

        //setting field method
        void init() {

            //scanning our field
            for (int y = 0; y < FIELD_HEIGHT; y++) {
                for (int x = 0; x < FIELD_WIDTH; x++) //calculating position
                {
                    field[x][y] = (x < 2 || x > FIELD_WIDTH - 3 || y < 2 || y > FIELD_HEIGHT - 3) ? COLOR_LAND : COLOR_WATER;
                }
            }
            currentWaterArea = WATER_AREA;//From begin all water have default properties
        }

        void reset(){
            countScore=0;
        }
        //getting colors
        int getColor(int x, int y) {
            //if coordinates of dot aren't in field, return water color
            if (x < 0 || y < 0 || x > FIELD_WIDTH - 1 || y > FIELD_HEIGHT - 1) {
                return COLOR_WATER;
            }

            return field[x][y];//returning color from array
        }

        //setting color
        void setColor(int x, int y, int color) {
            field[x][y] = color;
        }

        //returning out "achievments"
        int getCountScore() {
            return countScore;
        }

        //calculating percentage of "dry" field
        float getCurrentPercent() {
            return 100f - currentWaterArea / WATER_AREA * 100;
        }

        //clearing xonix's track (line behind xonix)
        void clearTrack() {
            //scanning field
            for (int y = 0; y < FIELD_HEIGHT; y++) {
                for (int x = 0; x < FIELD_WIDTH; x++) // if  current dot is in current color, setting this dot water color
                {
                    if (field[x][y] == COLOR_TRACK) {
                        field[x][y] = COLOR_WATER;
                    }
                }
            }
        }

        //not constant fill, just for checking out field
        void fillTemporary(int x, int y) {
            // If current dot have water color, just quitting
            if (field[x][y] > COLOR_WATER) {
                return;
            }
            //Setting this dot temp color
            field[x][y] = COLOR_TEMP; // filling temporary color

            //for all near dots too...

                for (int dx = -1; dx < 2; dx++) {
                    for (int dy = -1; dy < 2; dy++) {
                        fillTemporary(x + dx, y + dy);//fillings this dots too
                    }
                }

            }



        //trying ro fill needed field
        void tryToFill() {

            currentWaterArea = 0;//resetting percentage of water

            //Filling all field near enemy by temporary color
            for (Ball ball : balls.getBalls()) {
                fillTemporary(ball.getX(), ball.getY());
            }
            //Scanning all filed again..
            for (int y = 0; y < FIELD_HEIGHT; y++) {
                for (int x = 0; x < FIELD_WIDTH; x++) {
                    if (field[x][y] == COLOR_TRACK || field[x][y] == COLOR_WATER) {//If current dot is track or water...
                        field[x][y] = COLOR_LAND;//making current dot land's dot
                        countScore += 10;// increasing amount of our score by 10 points
                    }

                    if (field[x][y] == COLOR_TEMP) {//If current dot have temporary color
                        field[x][y] = COLOR_WATER;//setting by water's color
                        currentWaterArea++;//calculating amount of water
                    }
                }
            }
        }

        //drawing method fot field
        void paint(GraphicsContext g) {
            //Scanning field
            for (int y = 0; y < FIELD_HEIGHT; y++) {
                for (int x = 0; x < FIELD_WIDTH; x++) {
                    g.setFill(makeColorFromInt(field[x][y]));//setting color
                    g.fillRect(x * POINT_SIZE, y * POINT_SIZE, POINT_SIZE, POINT_SIZE);//drawing "XONIX"
                }
            }
        }
    }

    //xonix class
    class Xonix {

        private int x, y, //coordinates
                direction, //direction
                countLives = 3;//amount of lives
        private boolean isWater, isSelfCross;//booleans for situations

        //constructor
        Xonix() {
            init();
        }

        //initializing xonix
        void init() {
            //up
            y = 0;
            //middle
            x = FIELD_WIDTH / 2;
            //no direction
            direction = 0;
            isWater = false;//
        }

        void reset(){
            countLives = 3;
        }
        //getting x coordinate
        int getX() {
            return x;
        }

        //getting y coordinate
        int getY() {
            return y;
        }

        //getting amount of lives
        int getCountLives() {
            return countLives;
        }

        //decreasing amount of lives
        void decreaseCountLives() {
            countLives--;
        }

        //setting direction
        void setDirection(int direction) {
            this.direction = direction;
        }

        //move method
        void move() {

            //moves to...
            if (direction == LEFT) {
                x--;//left
            }
            if (direction == RIGHT) {
                x++;//right
            }
            if (direction == UP) {
                y--;//up
            }
            if (direction == DOWN) {
                y++;//down
            }
            //checking if xonix out of border
            if (x < 0) {
                x = 0;
            }
            if (y < 0) {
                y = 0;
            }
            if (y > FIELD_HEIGHT - 1) {
                y = FIELD_HEIGHT - 1;
            }
            if (x > FIELD_WIDTH - 1) {
                x = FIELD_WIDTH - 1;
            }

            isSelfCross = field.getColor(x, y) == COLOR_TRACK;//checking self-crossing

            if (field.getColor(x, y) == COLOR_LAND && isWater) {//If "we" was on water and came to "dry" field
                direction = 0;//stopping
                isWater = false;//for now, we aren't on water
                field.tryToFill();//calculating of "dry" field
            }

            if (field.getColor(x, y) == COLOR_WATER) {//If under xonix we have water...
                isWater = true;//trigger, that we are on water
                field.setColor(x, y, COLOR_TRACK);//setting track's color
            }
        }

        //retunt trigger of self-crossing
        boolean isSelfCrosed() {
            return isSelfCross;
        }

        //drawing xonix
        void paint(GraphicsContext g) {

            //Xonix fills by different colors base on current situation
            g.setFill((field.getColor(x, y) == COLOR_LAND) ? makeColorFromInt(COLOR_TRACK) : Color.WHITE);//Color of outside
            g.fillRect(x * POINT_SIZE, y * POINT_SIZE, POINT_SIZE, POINT_SIZE);//наружный квадрат
            g.setFill((field.getColor(x, y) == COLOR_LAND) ? Color.WHITE : makeColorFromInt(COLOR_TRACK));//Inside color
            g.fillRect(x * POINT_SIZE + 3, y * POINT_SIZE + 3, POINT_SIZE - 6, POINT_SIZE - 6);//inside square
        }
    }

    //ENEMIES
    class Balls {

        private ArrayList<Ball> balls = new ArrayList<Ball>();//List of enemies

        //constructor
        Balls() {
            add();//adding enemy
        }

        void reset() {
            balls.clear();
            add();
        }

        //Method to add enemy
        void add() {
            balls.add(new Ball());
        }//adding enemy to list

        //move method for "balls"
        void move() {
            for (Ball ball : balls) //For each method for balls
            {
                ball.move(); //moves them
            }
        }

        //return all enemies
        ArrayList<Ball> getBalls() {
            return balls;
        }

        //checking is enemy have crossed the track oo xonix
        boolean isHitTrackOrXonix() {
            //calculating all balls (enemies)
            for (Ball ball : balls) {
                if (ball.isHitTrackOrXonix()) {
                    return true;//checking hits for every ball
                }            //if no hit = return fall
            }
            return false;
        }

        //drawing all enemies
        //Calculating and drawing
        void paint(GraphicsContext g) {
            for (Ball ball : balls) {
                ball.paint(g);
            }
        }

    }

    //Ball class
    class Ball {

        private int x, y, dx, dy;//coordinates

        //constructor
        Ball() {

            do {//loop
                //getting random coordinates for ball
                x = random.nextInt(FIELD_WIDTH);
                y = random.nextInt(FIELD_HEIGHT);
            } while (field.getColor(x, y) > COLOR_WATER);

            //setting random direction
            dx = random.nextBoolean() ? 1 : -1;
            dy = random.nextBoolean() ? 1 : -1;
        }

        //Refreshing direction of ball
        void updateDXandDY() {
            if (field.getColor(x + dx, y) == COLOR_LAND) {
                dx = -dx;
            }
            if (field.getColor(x, y + dy) == COLOR_LAND) {
                dy = -dy;
            }
        }

        //move method
        void move() {

            updateDXandDY();//refreshing direction

            x += dx;
            y += dy;
        }

        //getting coordinates of ball
        int getX() {
            return x;
        }

        int getY() {
            return y;
        }

        //trigger, about is a ball hits a track
        boolean isHitTrackOrXonix() {

            updateDXandDY();//refreshing info
            //return true if xonix not behind
            if (field.getColor(x + dx, y + dy) == COLOR_TRACK) {
                return true;
            }
            //same
            if (x + dx == xonix.getX() && y + dy == xonix.getY()) {
                return true;
            }
            return false;
        }

        //drawing
        void paint(GraphicsContext g) {

            g.setFill(Color.WHITE);//setting white color
            g.fillOval(x * POINT_SIZE, y * POINT_SIZE, POINT_SIZE, POINT_SIZE);//drawing outside color
            g.setFill(makeColorFromInt(COLOR_LAND));//setting land color
            g.fillOval(x * POINT_SIZE + 2, y * POINT_SIZE + 2, POINT_SIZE - 4, POINT_SIZE - 4);//drawing inside circle
        }
    }

    //Outside cube class
    class Cube {

        private int x, y, dx, dy;//coordinates for direction

        //constructor
        Cube() {
            init();
        }

        //initializing
        void init() {
            x = dx = dy = 1;
        }

        //to avoid dorders
        void updateDXandDY() {
            if (field.getColor(x + dx, y) == COLOR_WATER) {
                dx = -dx;
            }
            if (field.getColor(x, y + dy) == COLOR_WATER) {
                dy = -dy;
            }
        }

        //moving method
        void move() {
            updateDXandDY();//refreshing direction
            x += dx;
            y += dy;
        }

        //IsHitXonix trigger
        boolean isHitXonix() {
            updateDXandDY();//refreshing direction
            if (x + dx == xonix.getX() && y + dy == xonix.getY()) {
                return true;
            }
            return false;
        }

        //draw method
        void paint(GraphicsContext g) {

            g.setFill(makeColorFromInt(COLOR_WATER));//water color
            g.fillRect(x * POINT_SIZE, y * POINT_SIZE, POINT_SIZE, POINT_SIZE);//drawing outside method
            g.setFill(makeColorFromInt(COLOR_LAND));//land color
            g.fillRect(x * POINT_SIZE + 2, y * POINT_SIZE + 2, POINT_SIZE - 4, POINT_SIZE - 4);//drawing inside square
        }
    }

    //delay class
    class Delay {

        //By time we can stop the animation
        void wait(int milliseconds) {
            try {
                Thread.sleep(milliseconds);//"sleep" based on milliseconds
            } catch (Exception e) {
                e.printStackTrace(); //exception for some "troubles"
            }
        }
    }

    //gameover class
    class GameOver {

        private final String GAME_OVER_MSG = "GAME OVER";//sign
        private boolean gameOver;//GAMEOVER trigger

        //return trigger
        boolean isGameOver() {
            return gameOver;
        }

        void reset(){
            gameOver=false;
        }

        //Drawing method
        void paint(GraphicsContext g) {

            if (xonix.getCountLives() <= 0) {//If lives = 0..
                gameOver = true;//setting trigger
                //font ad etc
                g.setFont(Font.font("", FontWeight.BOLD, FontPosture.REGULAR, 60));
                g.setFill(Color.WHITE);//setting white color
                //trying to align text
                g.setTextAlign(TextAlignment.CENTER);
                g.setTextBaseline(VPos.CENTER);
                //showint text
                g.fillText(GAME_OVER_MSG, (POINT_SIZE * FIELD_WIDTH + FIELD_DX) / 2, POINT_SIZE * FIELD_HEIGHT / 3);
            }
        }
    }
}


