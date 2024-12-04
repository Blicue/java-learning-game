package com.kingyu.flappybird.app;

import com.kingyu.flappybird.component.GameElementLayer;
import com.kingyu.flappybird.component.Bird;
import com.kingyu.flappybird.component.GameBackground;
import com.kingyu.flappybird.component.GameForeground;
import com.kingyu.flappybird.component.WelcomeAnimation;

import static com.kingyu.flappybird.util.Constant.FRAME_HEIGHT;
import static com.kingyu.flappybird.util.Constant.FRAME_WIDTH;
import static com.kingyu.flappybird.util.Constant.FRAME_X;
import static com.kingyu.flappybird.util.Constant.FRAME_Y;
import static com.kingyu.flappybird.util.Constant.FPS;
import static com.kingyu.flappybird.util.Constant.GAME_TITLE;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.Serial;

/**
 * 游戏主体，管理游戏的组件和窗口绘制
 *
 * @author Kingyu
 */
public class Game extends Frame {

    @Serial
    private static final long serialVersionUID = 1L;

    private static int gameState;
    /**
     * 状态：游戏准备中
     */
    public static final int GAME_READY = 0;
    /**
     * 状态：游戏开始
     */
    public static final int GAME_START = 1;
    /**
     * 状态：游戏结束
     */
    public static final int STATE_OVER = 2;

    private GameBackground background;
    private GameForeground foreground;
    private Bird bird;
    /**
     * 游戏元素层，包括管道、地面、分数、等
     */
    private GameElementLayer gameElement;
    private WelcomeAnimation welcomeAnimation;


    public Game() {
        initFrame();
        setVisible(true);
        initGame();
    }

    private void initFrame() {
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setTitle(GAME_TITLE);
        setLocation(FRAME_X, FRAME_Y);
        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        addKeyListener(new BirdKeyListener());
    }

    class BirdKeyListener implements KeyListener {
        @Override
        public void keyPressed(KeyEvent e) {
            int keycode = e.getKeyCode();
            switch (gameState) {
                case GAME_READY -> {
                    if (keycode == KeyEvent.VK_SPACE) {
                        bird.flap();
                        bird.fall();
                        setGameState(GAME_START);
                    }
                }
                case GAME_START -> {
                    if (keycode == KeyEvent.VK_SPACE) {
                        bird.flap();
                        bird.fall();
                    }
                }
                case STATE_OVER -> {
                    if (keycode == KeyEvent.VK_SPACE) {
                        resetGame();
                    }
                }
                default -> {
                }
            }
        }

        private void resetGame() {
            setGameState(GAME_READY);
            gameElement.reset();
            bird.reset();
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int keycode = e.getKeyChar();
            if (keycode == KeyEvent.VK_SPACE) {
                bird.keyReleased();
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }
    }

    private void initGame() {
        background = new GameBackground();
        gameElement = new GameElementLayer();
        foreground = new GameForeground();
        welcomeAnimation = new WelcomeAnimation();
        bird = new Bird();
        setGameState(GAME_READY);

        new Thread(() -> {
            while (true) {
                repaint();
                try {
                    Thread.sleep(FPS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private final BufferedImage bufImg = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);

    @Override
    public void update(Graphics g) {
        Graphics bufG = bufImg.getGraphics();
        background.draw(bufG, bird);
        foreground.draw(bufG, bird);
        if (gameState == GAME_READY) {
            welcomeAnimation.draw(bufG);
        } else {
            gameElement.draw(bufG, bird);
        }
        bird.draw(bufG);
        g.drawImage(bufImg, 0, 0, null);
    }

    public static void setGameState(int gameState) {
        Game.gameState = gameState;
    }
}
