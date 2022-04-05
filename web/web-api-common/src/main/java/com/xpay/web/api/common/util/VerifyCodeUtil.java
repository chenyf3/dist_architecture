package com.xpay.web.api.common.util;

import com.xpay.common.utils.RandomUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 验证码工具类
 */
public class VerifyCodeUtil {
	private final static char[] CHAR_SEQUENCE = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S',
			'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7', '8', '9' };
	// 图片的宽度。
	private final static int IMAGE_WIDTH = 160;
	// 图片的高度。
	private final static int IMAGE_HEIGHT = 45;
	// 验证码字符个数
	private final static int RANDOM_CODE_COUNT = 4;
	// 验证码干扰线数
	private final static int DISTURB_LINE_COUNT = 150;


	public static CodeInfo createCode() {
		char[] codeArr = getRandomCode(RANDOM_CODE_COUNT);
		BufferedImage image = createImage(codeArr, IMAGE_WIDTH, IMAGE_HEIGHT, DISTURB_LINE_COUNT);
		ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", byteOutStream);
		} catch (IOException e) {
			throw new RuntimeException("验证码生成时，内存写入失败！", e);
		}

		CodeInfo info = new CodeInfo();
		info.setImgBase64(Base64.getEncoder().encodeToString(byteOutStream.toByteArray()));
		info.setCode(String.valueOf(codeArr));
		info.setCodeKey(RandomUtil.get32LenStr());
		return info;
	}

	private static char[] getRandomCode(int length){
		char[] codeArr = new char[length];
		Random random = new Random();
		for(int i=0; i<length; i++){
			int index = random.nextInt(CHAR_SEQUENCE.length);
			codeArr[i] = CHAR_SEQUENCE[index];
		}
		return codeArr;
	}

	private static BufferedImage createImage(char[] codeArr, int width, int height, int lineCount){
		int x = 0, fontHeight = 0, codeY = 0;
		int red = 0, green = 0, blue = 0;

		x = width / (codeArr.length + 1);// 每个字符的宽度
		fontHeight = height - 2;// 字体的高度
		codeY = height - 3;

		// 图像buffer
		BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graph = buffImg.createGraphics();
		// 将图像填充为白色
		graph.setColor(Color.WHITE);
		graph.fillRect(0, 0, width, height);
		// 创建字体
		ImgFontByte imgFont = new ImgFontByte();
		Font font = imgFont.getFont(fontHeight);
		graph.setFont(font);

		//生成干扰线
		Random random = new Random();
		for (int i = 0; i < lineCount; i++) {
			int xs = random.nextInt(width);
			int ys = random.nextInt(height);
			int xe = xs + random.nextInt(width / 8);
			int ye = ys + random.nextInt(height / 8);
			red = random.nextInt(255);
			green = random.nextInt(255);
			blue = random.nextInt(255);
			graph.setColor(new Color(red, green, blue));
			graph.drawLine(xs, ys, xe, ye);
		}

		//把验证码绘制到图片上去
		for (int i = 0; i < codeArr.length; i++) {
			String charStr = String.valueOf(codeArr[i]);

			// 产生随机的颜色值，让输出的每个字符的颜色值都将不同。
			red = random.nextInt(255);
			green = random.nextInt(255);
			blue = random.nextInt(255);
			graph.setColor(new Color(red, green, blue));
			graph.drawString(charStr, (i + 1) * x, codeY);
		}
		return buffImg;
	}

	static class ImgFontByte {
		public Font getFont(int fontHeight) {
			try {
				Font baseFont = Font.createFont(Font.ITALIC, new ByteArrayInputStream(hex2byte(getFontByteStr())));
				return baseFont.deriveFont(Font.PLAIN, fontHeight);
			} catch (Exception e) {
				return new Font("Consola", Font.PLAIN, fontHeight);
			}
		}

		private byte[] hex2byte(String str) {
			if (str == null)
				return null;
			str = str.trim();
			int len = str.length();
			if (len == 0 || len % 2 == 1)
				return null;
			byte[] b = new byte[len / 2];
			try {
				for (int i = 0; i < str.length(); i += 2) {
					b[i / 2] = (byte) Integer.decode("0x" + str.substring(i, i + 2)).intValue();
				}
				return b;
			} catch (Exception e) {
				return null;
			}
		}

		/**
		 * ttf字体文件的十六进制字符串
		 * 
		 * @return
		 */
		private String getFontByteStr() {
			return null;
		}
	}

	public static class CodeInfo {
		private String imgBase64;
		private String code;
		private String codeKey;

		public String getImgBase64() {
			return imgBase64;
		}

		public void setImgBase64(String imgBase64) {
			this.imgBase64 = imgBase64;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getCodeKey() {
			return codeKey;
		}

		public void setCodeKey(String codeKey) {
			this.codeKey = codeKey;
		}
	}
}
