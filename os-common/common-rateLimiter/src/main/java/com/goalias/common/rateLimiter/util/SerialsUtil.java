package com.goalias.common.rateLimiter.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SerialsUtil {

	public static int serialInt = 1;

	private static final DecimalFormat format8 = new DecimalFormat("00000000");

	private static final DecimalFormat format12 = new DecimalFormat("000000000000");

	private static final BigInteger divisor;

	private static final BigInteger divisor12;

	static {
		divisor = BigInteger.valueOf(19999999L).multiply(BigInteger.valueOf(5));
		divisor12 = BigInteger.valueOf(190000000097L).multiply(BigInteger.valueOf(5));
	}

	public static String genSerialNo() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String strNow = sdf.format(new Date());

		// 生成3位随机数
		Random random = new Random();
		int intRandom = random.nextInt(999);

		String strRandom = String.valueOf(intRandom);
		int len = strRandom.length();
		for (int i = 0; i < (3 - len); i++) {
			strRandom = "0" + strRandom;
		}
		String serialStr = SerialsUtil.nextSerial();
		return (strNow + strRandom + serialStr);
	}

	public static synchronized String nextSerial() {
		int serial = serialInt++;
		if (serial > 999) {
			serialInt = 1;
			serial = 1;
		}
		String serialStr = serial + "";
		int len = serialStr.length();
		for (int i = 0; i < (3 - len); i++) {
			serialStr = "0" + serialStr;
		}

		return serialStr;
	}

	/**
	 * 生成一个12位随机数
	 * @param seed 种子值
	 * @return String 随机数
	 */
	public static String randomNum12(long seed) {
		// 被除数
		BigInteger dividend = BigDecimal.valueOf(seed).pow(5).toBigInteger();
		return format12.format(dividend.remainder(divisor12));
	}

	/**
	 * 生成一个8位随机数
	 * @param seed 种子值
	 * @return String 随机数
	 */
	public static String randomNum8(long seed) {
		// 被除数
		BigInteger dividend = BigDecimal.valueOf(seed).pow(5).toBigInteger();
		return format8.format(dividend.remainder(divisor));
	}

	/*
	 * 10进制转32进制(去除0,O,1,I)
	 */
	public static String from10To32(String numStr, int size) {
		long to = 32;
		long num = Long.parseLong(numStr);
		String jg = "";
		while (num != 0) {
            jg = switch ((int) (num % to)) {
                case 0 -> "B" + jg;
                case 1 -> "R" + jg;
                case 2 -> "6" + jg;
                case 3 -> "U" + jg;
                case 4 -> "M" + jg;
                case 5 -> "E" + jg;
                case 6 -> "H" + jg;
                case 7 -> "C" + jg;
                case 8 -> "G" + jg;
                case 9 -> "Q" + jg;
                case 10 -> "A" + jg;
                case 11 -> "8" + jg;
                case 12 -> "3" + jg;
                case 13 -> "S" + jg;
                case 14 -> "J" + jg;
                case 15 -> "Y" + jg;
                case 16 -> "7" + jg;
                case 17 -> "5" + jg;
                case 18 -> "W" + jg;
                case 19 -> "9" + jg;
                case 20 -> "F" + jg;
                case 21 -> "T" + jg;
                case 22 -> "D" + jg;
                case 23 -> "2" + jg;
                case 24 -> "P" + jg;
                case 25 -> "Z" + jg;
                case 26 -> "N" + jg;
                case 27 -> "K" + jg;
                case 28 -> "V" + jg;
                case 29 -> "X" + jg;
                case 30 -> "L" + jg;
                case 31 -> "4" + jg;
                default -> String.valueOf(num % to) + jg;
            };
			num = num / to;
		}
		if (jg.length() < size) {
			int loop = size - jg.length();
			for (int i = 0; i < loop; i++) {
				jg = "2" + jg;
			}
		}
		return jg;
	}

	/*
	 * 10进制转32进制(去除0,O,1,I)
	 */
	public static String from10To24(String numStr, int size) {
		long to = 24;
		long num = Long.parseLong(numStr);
		String jg = "";
		while (num != 0) {
            jg = switch ((int) (num % to)) {
                case 0 -> "B" + jg;
                case 1 -> "R" + jg;
                case 2 -> "U" + jg;
                case 3 -> "M" + jg;
                case 4 -> "E" + jg;
                case 5 -> "H" + jg;
                case 6 -> "C" + jg;
                case 7 -> "G" + jg;
                case 8 -> "Q" + jg;
                case 9 -> "A" + jg;
                case 10 -> "S" + jg;
                case 11 -> "J" + jg;
                case 12 -> "Y" + jg;
                case 13 -> "W" + jg;
                case 14 -> "F" + jg;
                case 15 -> "T" + jg;
                case 16 -> "D" + jg;
                case 17 -> "P" + jg;
                case 18 -> "Z" + jg;
                case 19 -> "N" + jg;
                case 20 -> "K" + jg;
                case 21 -> "V" + jg;
                case 22 -> "X" + jg;
                case 23 -> "L" + jg;
                default -> num % to + jg;
            };
			num = num / to;
		}
		if (jg.length() < size) {
			int loop = size - jg.length();
			for (int i = 0; i < loop; i++) {
				jg = "B" + jg;
			}
		}
		return jg;
	}

	public static String getUUID() {
		UUID uuid = UUID.randomUUID();
		String str = uuid.toString();
		// 去掉"-"符号
		String temp = str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23)
				+ str.substring(24);
		return temp;
	}

	public static String generateShortUUID() {
		String str = randomNum8(System.nanoTime());
		return from10To24(str, 6);
	}

	public static String generateFileUUID() {
		String str = randomNum12(System.nanoTime());
		return from10To32(str, 8);
	}

	public static String genToken() {
		return from10To32(randomNum12(System.currentTimeMillis()), 8) + from10To32(randomNum12(System.nanoTime()), 8);
	}

	public static void main(String[] args) {
		Set set = new HashSet();
		String str;
		for (int i = 0; i < 300; i++) {
			str = generateShortUUID();
			System.out.println(str);
			set.add(str);
		}
		System.out.println(set.size());
	}

}