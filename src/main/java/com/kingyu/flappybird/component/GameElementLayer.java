package com.kingyu.flappybird.component;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import com.kingyu.flappybird.util.Constant;
import com.kingyu.flappybird.util.GameUtil;

/**
 * 游戏元素层，负责管理水管的生成逻辑，并绘制容器中的水管。
 * <p>
 * 该类负责水管的生成、绘制、碰撞检测及管理。
 * </p>
 *
 * @author Kingyu
 */
public class GameElementLayer {

    /**
     * 水管容器，存储当前所有生成的水管。
     */
    private final List<Pipe> pipes;

    /**
     * 构造方法，初始化水管容器。
     */
    public GameElementLayer() {
        pipes = new ArrayList<>();
    }

    /**
     * 绘制方法，遍历水管并绘制可见的水管，同时检查碰撞和生成新水管。
     *
     * @param g   图形上下文，用于绘制
     * @param bird 小鸟对象，用于碰撞检测
     */
    public void draw(Graphics g, Bird bird) {
        // 遍历水管容器，绘制可见的水管，不可见的归还对象池
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            if (pipe.isVisible()) {
                pipe.draw(g, bird);
            } else {
                Pipe remove = pipes.remove(i);
                PipePool.giveBack(remove);
                i--;  // 调整索引，避免跳过元素
            }
        }
        // 碰撞检测
        isCollideBird(bird);
        // 水管生成逻辑
        pipeBornLogic(bird);
    }

    /**
     * 水管生成的相关参数，具体包括垂直和水平间隔，以及水管高度的上下限。
     */
    public static final int VERTICAL_INTERVAL = Constant.FRAME_HEIGHT / 5;
    public static final int HORIZONTAL_INTERVAL = Constant.FRAME_HEIGHT >> 2;
    public static final int MIN_HEIGHT = Constant.FRAME_HEIGHT >> 3;
    public static final int MAX_HEIGHT = ((Constant.FRAME_HEIGHT) >> 3) * 5;

    /**
     * 水管生成逻辑：当最后一对水管完全显示到屏幕上时，添加新的水管对。
     * 水管成对地生成，且水管的间隔和高度是随机的。
     *
     * @param bird 小鸟对象，用于计算水管与小鸟的相对位置
     */
    private void pipeBornLogic(Bird bird) {
        if (bird.isDead()) {
            // 鸟已死，停止生成水管
            return;
        }

        if (pipes.isEmpty()) {
            // 若容器为空，则添加一对水管
            int topHeight = GameUtil.getRandomNumber(MIN_HEIGHT, MAX_HEIGHT + 1);

            Pipe top = PipePool.get("Pipe");
            top.setAttribute(Constant.FRAME_WIDTH, -Constant.TOP_PIPE_LENGTHENING,
                    topHeight + Constant.TOP_PIPE_LENGTHENING, Pipe.TYPE_TOP_NORMAL, true);

            Pipe bottom = PipePool.get("Pipe");
            bottom.setAttribute(Constant.FRAME_WIDTH, topHeight + VERTICAL_INTERVAL,
                    Constant.FRAME_HEIGHT - topHeight - VERTICAL_INTERVAL, Pipe.TYPE_BOTTOM_NORMAL, true);

            pipes.add(top);
            pipes.add(bottom);
        } else {
            // 判断最后一对水管是否已完全进入游戏窗口
            Pipe lastPipe = pipes.get(pipes.size() - 1);
            int currentDistance = lastPipe.getX() - bird.getBirdX() + Bird.getBirdWidth() / 2;
            final int scoreDistance = Pipe.PIPE_WIDTH * 2 + HORIZONTAL_INTERVAL;

            if (lastPipe.isInFrame()) {
                if (pipes.size() >= PipePool.FULL_PIPE - 2
                        && currentDistance <= scoreDistance + Pipe.PIPE_WIDTH * 3 / 2) {
                    ScoreCounter.getInstance().score(bird);
                }

                try {
                    int currentScore = (int) ScoreCounter.getInstance().getCurrentScore() + 1;
                    // 根据分数决定生成不同类型的水管
                    if (GameUtil.isInProbability(currentScore, 20)) {
                        if (GameUtil.isInProbability(1, 4)) {
                            addMovingHoverPipe(lastPipe);
                        } else {
                            addMovingNormalPipe(lastPipe);
                        }
                    } else {
                        if (GameUtil.isInProbability(1, 2)) {
                            addNormalPipe(lastPipe);
                        } else {
                            addHoverPipe(lastPipe);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 添加普通水管。
     *
     * @param lastPipe 传入最后一根水管对象，以获取新的水管位置
     */
    private void addNormalPipe(Pipe lastPipe) {
        int topHeight = GameUtil.getRandomNumber(MIN_HEIGHT, MAX_HEIGHT + 1);
        int x = lastPipe.getX() + HORIZONTAL_INTERVAL;

        Pipe top = PipePool.get("Pipe");
        top.setAttribute(x, -Constant.TOP_PIPE_LENGTHENING, topHeight + Constant.TOP_PIPE_LENGTHENING,
                Pipe.TYPE_TOP_NORMAL, true);

        Pipe bottom = PipePool.get("Pipe");
        bottom.setAttribute(x, topHeight + VERTICAL_INTERVAL, Constant.FRAME_HEIGHT - topHeight - VERTICAL_INTERVAL,
                Pipe.TYPE_BOTTOM_NORMAL, true);

        pipes.add(top);
        pipes.add(bottom);
    }

    /**
     * 添加悬浮水管。
     *
     * @param lastPipe 传入最后一根水管对象，以获取新的水管位置
     */
    private void addHoverPipe(Pipe lastPipe) {
        int topHoverHeight = GameUtil.getRandomNumber(Constant.FRAME_HEIGHT / 6, Constant.FRAME_HEIGHT / 4);
        int x = lastPipe.getX() + HORIZONTAL_INTERVAL;
        int y = GameUtil.getRandomNumber(Constant.FRAME_HEIGHT / 12, Constant.FRAME_HEIGHT / 6);

        int type = Pipe.TYPE_HOVER_NORMAL;

        Pipe topHover = PipePool.get("Pipe");
        topHover.setAttribute(x, y, topHoverHeight, type, true);

        int bottomHoverHeight = Constant.FRAME_HEIGHT - 2 * y - topHoverHeight - VERTICAL_INTERVAL;
        Pipe bottomHover = PipePool.get("Pipe");
        bottomHover.setAttribute(x, y + topHoverHeight + VERTICAL_INTERVAL, bottomHoverHeight, type, true);

        pipes.add(topHover);
        pipes.add(bottomHover);
    }

    /**
     * 添加移动的悬浮水管。
     *
     * @param lastPipe 传入最后一根水管对象，以获取新的水管位置
     */
    private void addMovingHoverPipe(Pipe lastPipe) {
        int topHoverHeight = GameUtil.getRandomNumber(Constant.FRAME_HEIGHT / 6, Constant.FRAME_HEIGHT / 4);
        int x = lastPipe.getX() + HORIZONTAL_INTERVAL;
        int y = GameUtil.getRandomNumber(Constant.FRAME_HEIGHT / 12, Constant.FRAME_HEIGHT / 6);

        int type = Pipe.TYPE_HOVER_HARD;

        Pipe topHover = PipePool.get("MovingPipe");
        topHover.setAttribute(x, y, topHoverHeight, type, true);

        int bottomHoverHeight = Constant.FRAME_HEIGHT - 2 * y - topHoverHeight - VERTICAL_INTERVAL;
        Pipe bottomHover = PipePool.get("MovingPipe");
        bottomHover.setAttribute(x, y + topHoverHeight + VERTICAL_INTERVAL, bottomHoverHeight, type, true);

        pipes.add(topHover);
        pipes.add(bottomHover);
    }

    /**
     * 添加移动的普通水管。
     *
     * @param lastPipe 传入最后一根水管对象，以获取新的水管位置
     */
    private void addMovingNormalPipe(Pipe lastPipe) {
        int topHeight = GameUtil.getRandomNumber(MIN_HEIGHT, MAX_HEIGHT + 1);
        int x = lastPipe.getX() + HORIZONTAL_INTERVAL;

        Pipe top = PipePool.get("MovingPipe");
        top.setAttribute(x, -Constant.TOP_PIPE_LENGTHENING, topHeight + Constant.TOP_PIPE_LENGTHENING,
                Pipe.TYPE_TOP_HARD, true);

        Pipe bottom = PipePool.get("MovingPipe");
        bottom.setAttribute(x, topHeight + VERTICAL_INTERVAL, Constant.FRAME_HEIGHT - topHeight - VERTICAL_INTERVAL,
                Pipe.TYPE_BOTTOM_HARD, true);

        pipes.add(top);
        pipes.add(bottom);
    }

    /**
     * 判断小鸟与水管是否发生碰撞。
     *
     * @param bird 小鸟对象，用于检测与水管的碰撞
     */
    public void isCollideBird(Bird bird) {
        if (bird.isDead()) {
            // 鸟已死，不再进行碰撞检测
            return;
        }

        for (Pipe pipe : pipes) {
            if (pipe.getPipeRect().intersects(bird.getBirdCollisionRect())) {
                bird.deadFall();
                return;
            }
        }
    }

    /**
     * 重置游戏元素层，归还水管并清空容器。
     */
    public void reset() {
        for (Pipe pipe : pipes) {
            PipePool.giveBack(pipe);
        }
        pipes.clear();
    }
}
