package com.xpay.web.portal.web.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片处理
 * @author fanfeihang
 */
public class ImageUtil {

    /**
     * 二维码图片默认宽度
     */
    private static final Integer QR_CODE_DEFAULT_LENGTH = 256;
    /**
     * 字体
     */
    private static final String TYPEFACE = "Microsoft YaHei";

    /**
     * 给图片下方增加文字描述
     *
     * @param str   图片文字
     * @param is    原图片输入流
     * @param width 定义生成图片宽度
     * @param height 定义生成图片高度
     * @throws IOException
     */
    public static ByteArrayOutputStream mergeStr(String str, InputStream is, int width, int height) throws IOException {
        Image image = ImageIO.read(is);

        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setBackground(Color.WHITE);
        g2.clearRect(0, 0, width, height);
        g2.drawImage(image, 0, 0, QR_CODE_DEFAULT_LENGTH, QR_CODE_DEFAULT_LENGTH, null);
        // 设置生成图片的文字样式
        Font font = new Font(TYPEFACE, Font.BOLD, 12);
        g2.setFont(font);
        g2.setPaint(Color.BLACK);

        // 设置字体在图片中的位置
        FontRenderContext context = g2.getFontRenderContext();
        Rectangle2D bounds = font.getStringBounds(str, context);
        double x = (width - bounds.getWidth()) / 2;
        double y = (height - bounds.getHeight()) + 10;

        // 防止生成的文字带有锯齿
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // 在图片上生成文字
        g2.drawString(str, (int) x, (int) y);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", bos);
        return bos;
    }
}