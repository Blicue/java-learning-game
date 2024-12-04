package com.kingyu.flappybird.component;

import com.kingyu.flappybird.app.Game;
import com.kingyu.flappybird.util.Constant;
import com.kingyu.flappybird.util.GameUtil;
import com.kingyu.flappybird.util.MusicUtil;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * 小鸟类，实现小鸟的绘制与飞行逻辑。
 * 负责小鸟的运动、状态管理和碰撞检测。
 *
 * @author Kingyu
 */
public class Bird {

    private static final int IMG_COUNT = 8;
    private static final int STATE_COUNT = 4;
    private static final int RECT_DESCALE = 2;
    private static final int ACC_FLAP = 14;
    private static final double ACC_Y = 2.0;
    private static final int MAX_VEL_Y = 15;

    private final BufferedImage[][] birdImages;
    private final Rectangle birdCollisionRect;
    private final ScoreCounter counter;
    private final GameOverAnimation gameOverAnimation;

    private BufferedImage image;
    private final int x;
    private int y;
    private int velocity;
    private int wingState;
    private int state;
    private boolean keyFlag = true;

    private static int BIRD_WIDTH;

    private final int bottomBoundary;

    public static final int BIRD_NORMAL = 0;
    public static final int BIRD_UP = 1;
    public static final int BIRD_FALL = 2;
    public static final int BIRD_DEAD_FALL = 3;
    public static final int BIRD_DEAD = 4;

    /**
     * 构造器，初始化小鸟及相关资源。
     */
    public Bird() {
        counter = ScoreCounter.getInstance();
        gameOverAnimation = new GameOverAnimation();

        birdImages = new BufferedImage[STATE_COUNT][IMG_COUNT];
        for (int state = 0; state < STATE_COUNT; state++) {
            for (int index = 0; index < IMG_COUNT; index++) {
                birdImages[state][index] = GameUtil.loadBufferedImage(Constant.BIRDS_IMG_PATH[state][index]);
            }
        }

        BufferedImage defaultImage = Objects.requireNonNull(birdImages[0][0], "小鸟图片加载失败");
        BIRD_WIDTH = defaultImage.getWidth();
        int birdHeight = defaultImage.getHeight();

        x = Constant.FRAME_WIDTH / 4;
        y = Constant.FRAME_HEIGHT / 2;

        birdCollisionRect = new Rectangle(
                x - BIRD_WIDTH / 2 + RECT_DESCALE,
                y - birdHeight / 2 + RECT_DESCALE * 2,
                BIRD_WIDTH - RECT_DESCALE * 3,
                birdHeight - RECT_DESCALE * 4
        );

        bottomBoundary = Constant.FRAME_HEIGHT - GameBackground.GROUND_HEIGHT - (birdHeight / 2);
    }

    /**
     * 绘制小鸟和其他相关元素。
     *
     * @param g 绘图对象
     */
    public void draw(Graphics g) {
        updateMovement();

        int stateIndex = Math.min(state, BIRD_DEAD_FALL);
        int halfImgWidth = birdImages[stateIndex][0].getWidth() / 2;
        int halfImgHeight = birdImages[stateIndex][0].getHeight() / 2;

        g.drawImage(image, x - halfImgWidth, y - halfImgHeight, null);

        if (state == BIRD_DEAD) {
            gameOverAnimation.draw(g, this);
        } else if (state != BIRD_DEAD_FALL) {
            drawScore(g);
        }
    }

    private void updateMovement() {
        wingState++;
        int stateIndex = Math.min(state, BIRD_DEAD_FALL);
        image = birdImages[stateIndex][wingState / 10 % IMG_COUNT];

        if (state == BIRD_FALL || state == BIRD_DEAD_FALL) {
            applyGravity();
            if (birdCollisionRect.y > bottomBoundary) {
                if (state == BIRD_FALL) {
                    MusicUtil.playCrash();
                }
                die();
            }
        }
    }

    private void applyGravity() {
        if (velocity < MAX_VEL_Y) {
            velocity -= (int) ACC_Y;
        }
        y = Math.min(y - velocity, bottomBoundary);
        birdCollisionRect.y -= velocity;
    }

    private void die() {
        counter.saveScore();
        state = BIRD_DEAD;
        Game.setGameState(Game.STATE_OVER);
    }

    public void flap() {
        if (!keyIsReleased() || isDead()) {
            return;
        }
        MusicUtil.playFly();
        state = BIRD_UP;
        if (birdCollisionRect.y > Constant.TOP_BAR_HEIGHT) {
            velocity = ACC_FLAP;
            wingState = 0;
        }
        keyPressed();
    }

    public void fall() {
        if (isDead()) {
            return;
        }
        state = BIRD_FALL;
    }

    public void deadFall() {
        state = BIRD_DEAD_FALL;
        MusicUtil.playCrash();
        velocity = 0;
    }

    public boolean isDead() {
        return state == BIRD_DEAD_FALL || state == BIRD_DEAD;
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(Constant.CURRENT_SCORE_FONT);
        String score = Long.toString(counter.getCurrentScore());
        int x = (Constant.FRAME_WIDTH - GameUtil.getStringWidth(Constant.CURRENT_SCORE_FONT, score)) / 2;
        g.drawString(score, x, Constant.FRAME_HEIGHT / 10);
    }

    public void reset() {
        state = BIRD_NORMAL;
        y = Constant.FRAME_HEIGHT / 2;
        velocity = 0;

        int imgHeight = birdImages[state][0].getHeight();
        birdCollisionRect.y = y - imgHeight / 2 + RECT_DESCALE * 2;

        counter.reset();
    }

    public void keyPressed() {
        keyFlag = false;
    }

    public void keyReleased() {
        keyFlag = true;
    }

    public boolean keyIsReleased() {
        return keyFlag;
    }

    public long getCurrentScore() {
        return counter.getCurrentScore();
    }

    public long getBestScore() {
        return counter.getBestScore();
    }

    public int getBirdX() {
        return x;
    }

    public static int getBirdWidth() {
        return BIRD_WIDTH;
    }

    public Rectangle getBirdCollisionRect() {
        return birdCollisionRect;
    }
}
