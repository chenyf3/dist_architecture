package com.xpay.common.utils;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * 图片处理工具类
 */
public class ImageUtil {

    /**
     * 图片压缩
     * @param inputStream   源图片数据
     * @param scale         宽高等比压缩比例，[0,1]之间的数值，越接近1，越接近原图尺寸，如原始尺寸是：8000 x 6000，scale是0.3，则压缩后尺寸是：2400 x 1800
     * @param quality       新图片质量，[0,1]之间的数值，越接近1，质量越高(越清晰)
     * @return
     */
    public static byte[] compress(InputStream inputStream, double scale, double quality) {
        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(inputStream)
                    .scale(scale)
                    .outputQuality(quality)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);
            return outputStream.toByteArray();
        }catch(Exception e){
            throw new RuntimeException("图片压缩异常", e);
        }
    }

    /**
     * 图片压缩
     * @param image
     * @param scale
     * @param quality
     * @return
     */
    public static byte[] compress(BufferedImage image, double scale, double quality) {
        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(image)
                    .scale(scale)
                    .outputQuality(quality)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);
            return outputStream.toByteArray();
        }catch(Exception e){
            throw new RuntimeException("图片压缩异常", e);
        }
    }

    /**
     * 图片尺寸调整
     * @param inputStream       源图片数据
     * @param width             新图片宽
     * @param height            新图片高
     * @param keepRatio         是否维持原图片的宽高比例
     * @param quality           新图片质量，[0,1]之间的数值，越接近1，质量越高(越清晰)
     * @return
     */
    public static byte[] resize(InputStream inputStream, int width, int height, boolean keepRatio, double quality) {
        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(inputStream)
                    .size(width, height)
                    .keepAspectRatio(keepRatio)
                    .outputQuality(quality)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);
            return outputStream.toByteArray();
        }catch(Exception e){
            throw new RuntimeException("图片尺寸调整异常", e);
        }
    }

    /**
     * 图片裁切(按中心位置)
     * @param inputStream   源图片数据
     * @param centerWidth   裁切源图片的中心位置长度
     * @param centerHeight  裁切源图片的中心位置高度
     * @param width         新图片宽
     * @param height        新图片高
     * @param keepRatio     是否维持原图片的宽高比例
     * @param quality       新图片质量，[0,1]之间的数值，越接近1，质量越高(越清晰)
     * @return
     */
    public static byte[] centerCut(InputStream inputStream, int centerWidth, int centerHeight,
                                   int width, int height, boolean keepRatio, double quality) {
        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(inputStream)
                    .sourceRegion(Positions.CENTER, centerWidth, centerHeight)
                    .size(width, height)
                    .keepAspectRatio(keepRatio)
                    .outputQuality(quality)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);
            return outputStream.toByteArray();
        }catch(Exception e){
            throw new RuntimeException("图片中心裁切异常", e);
        }
    }

    /**
     * 图片裁切（按坐标数值）
     * @param inputStream   源图片数据
     * @param x             起始x坐标
     * @param y             起始y坐标
     * @param x1            结束x坐标
     * @param y1            结束y坐标
     * @param width         新图片宽
     * @param height        新图片高
     * @param keepRatio     是否维持原图片的宽高比例
     * @param quality       新图片质量，[0,1]之间的数值，越接近1，质量越高(越清晰)
     * @return
     */
    public static byte[] coordinateCut(InputStream inputStream,
                                       int x, int y, int x1, int y1,
                                       int width, int height, boolean keepRatio, double quality) {
        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(inputStream)
                    .sourceRegion(x, y, x1, y1)
                    .size(width, height)
                    .keepAspectRatio(keepRatio)
                    .outputQuality(quality)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);
            return outputStream.toByteArray();
        }catch(Exception e){
            throw new RuntimeException("图片坐标裁切异常", e);
        }
    }

    /**
     * 图片旋转
     * @param inputStream   源图片数据
     * @param scale         宽高等比压缩比例，[0,1]之间的数值
     * @param angle         旋转角度，正数表示顺时针旋转，负数表示逆时针旋转
     * @param quality       新图片质量，[0,1]之间的数值，越接近1，质量越高(越清晰)
     * @return
     */
    public static byte[] rotate(InputStream inputStream, double scale, int angle, double quality) {
        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(inputStream)
                    .scale(scale)
                    .rotate(angle)
                    .outputQuality(quality)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);
            return outputStream.toByteArray();
        }catch(Exception e){
            throw new RuntimeException("图片旋转异常", e);
        }
    }

    /**
     * 调整大小后旋转图片
     * @param inputStream   源图片数据
     * @param width         新图片宽
     * @param height        新图片高
     * @param keepRatio     是否维持原图片的宽高比例
     * @param angle         旋转角度，正数表示顺时针旋转，负数表示逆时针旋转
     * @param quality       新图片质量，[0,1]之间的数值，越接近1，质量越高(越清晰)
     * @return
     */
    public static byte[] rotate(InputStream inputStream, int width, int height, boolean keepRatio, int angle, double quality) {
        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(inputStream)
                    .size(width, height)
                    .keepAspectRatio(keepRatio)
                    .rotate(angle)
                    .outputQuality(quality)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);
            return outputStream.toByteArray();
        }catch(Exception e){
            throw new RuntimeException("图片旋转异常", e);
        }
    }
}
