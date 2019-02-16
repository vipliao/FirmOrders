package com.firm.orders.base.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.UUID;

public class JavaUuidGenerater {
	public static String[] chars = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
			"o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8",
			"9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z" };

	public static String generateShortUuid() {
		StringBuffer shortBuffer = new StringBuffer();
		String uuid = UUID.randomUUID().toString().replace("-", "");
		for (int i = 0; i < 8; i++) {
			String str = uuid.substring(i * 4, i * 4 + 4);
			int x = Integer.parseInt(str, 16);
			shortBuffer.append(chars[x % 0x3E]);
		}
		return shortBuffer.toString();

	}
	public static String generateUuid() throws Exception {
		MessageDigest e = MessageDigest.getInstance("MD5");
		UUID uuid = UUID.randomUUID();
		String guidStr = uuid.toString();
		e.update(guidStr.getBytes(), 0, guidStr.length());
		return (new BigInteger(1, e.digest())).toString(16);
	     
	   }
	public static void main(String[] args) throws Exception {
		System.out.println(JavaUuidGenerater.generateUuid());;
	}

}
