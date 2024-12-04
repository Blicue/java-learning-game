package com.kingyu.flappybird.util;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * 音乐工具类，支持播放 WAV 格式音频文件。
 * <p>
 * 示例用法：
 * <pre>
 *     MusicUtil.playFly();
 *     MusicUtil.playCrash();
 *     MusicUtil.playScore();
 * </pre>
 * </p>
 *
 * @author Kingyu
 */
public class MusicUtil {

    private static Clip flyClip;
    private static Clip crashClip;
    private static Clip scoreClip;

    private MusicUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 加载音频剪辑。
     *
     * @param filePath 音频文件路径
     * @return Clip 对象，如果加载失败则返回 null
     */
    private static Clip loadAudioClip(String filePath) {
        Clip clip = null;
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(filePath))) {
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(audioStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("加载音频文件失败: " + filePath + "，错误信息: " + e.getMessage());
        }
        return clip;
    }

    /**
     * 播放音频剪辑。
     *
     * @param clip Clip 对象
     */
    private static void playClip(Clip clip) {
        if (clip == null) {
            return;
        }
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();
    }

    /**
     * 播放飞行音效。
     */
    public static void playFly() {
        if (flyClip == null) {
            flyClip = loadAudioClip("resources/wav/fly.wav");
        }
        playClip(flyClip);
    }

    /**
     * 播放碰撞音效。
     */
    public static void playCrash() {
        if (crashClip == null) {
            crashClip = loadAudioClip("resources/wav/crash.wav");
        }
        playClip(crashClip);
    }

    /**
     * 播放得分音效。
     */
    public static void playScore() {
        if (scoreClip == null) {
            scoreClip = loadAudioClip("resources/wav/score.wav");
        }
        playClip(scoreClip);
    }
}
