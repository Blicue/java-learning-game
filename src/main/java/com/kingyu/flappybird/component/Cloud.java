package com.kingyu.flappybird.component;

import com.kingyu.flappybird.util.Constant;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * 云朵类，实现云朵的绘制和运动逻辑。
 * <p>
 * 云朵会根据游戏状态改变移动速度，并支持图片的随机缩放。
 * </p>
 *
 * @author Kingyu
 */
public class Cloud {

    /**
     * 云朵的移动速度。
     */
    private final int speed;

    /**
     * 云朵的横坐标。
     */
    private int x;

    /**
     * 云朵的纵坐标。
     */
    private final int y;

    /**
     * 云朵的图像资源。
     */
    private final BufferedImage img;

    /**
     * 缩放后的宽度。
     */
    private final int scaledImageWidth;

    /**
     * 缩放后的高度。
     */
    private final int scaledImageHeight;

    /**
     * 构造器，初始化云朵对象。
     *
     * @param img 云朵图片
     * @param x   云朵初始横坐标
     * @param y   云朵初始纵坐标
     */
    public Cloud(BufferedImage img, int x, int y) {
        Objects.requireNonNull(img, "云朵图片不能为空");
        this.img = img;
        this.x = x;
        this.y = y;
        this.speed = Constant.GAME_SPEED * 2;

        // 云朵图片缩放比例：1.0 ~ 2.0
        double scale = 1 + Math.random();
        this.scaledImageWidth = (int) (scale * img.getWidth());
        this.scaledImageHeight = (int) (scale * img.getHeight());
    }

    /**
     * 绘制云朵。
     *
     * @param g    画笔对象
     * @param bird 当前鸟的状态
     */
    public void draw(Graphics g, Bird bird) {
        if (g == null || bird == null) {
            throw new IllegalArgumentException("参数 g 和 bird 不能为空");
        }

        int currentSpeed = bird.isDead() ? 1 : speed;
        x -= currentSpeed;

        g.drawImage(img, x, y, scaledImageWidth, scaledImageHeight, null);
    }

    /**
     * 判断云朵是否飞出屏幕。
     *
     * @return 如果飞出屏幕则返回 true，否则返回 false
     */
    public boolean isOutFrame() {
        return x < -scaledImageWidth;
    }
}
