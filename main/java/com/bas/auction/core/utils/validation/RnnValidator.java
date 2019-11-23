package com.bas.auction.core.utils.validation;

import java.util.Arrays;
import java.util.List;

public class RnnValidator {
	public boolean isValid(String RNN) {
		if (RNN == null || RNN.length() != 12)
			return false;
		List<String> isk = Arrays.asList("000000000000", "111111111111", "222222222222", "333333333333", "444444444444",
				"555555555555", "666666666666", "777777777777", "888888888888", "999999999999");
		if (isk.contains(RNN))
			return false;
		int[] nnArr;
		try {
			nnArr = getNNDigits(RNN);
		} catch (NumberFormatException e) {
			return false;
		}

		int k = 0;
		for (int i = 1; i <= 10; i++) {
			int s = 0;
			int t = i - 1;
			for (int j = 1; j <= 11; j++) {
				++t;
				if (t == 11)
					t = 1;
				s = s + t * nnArr[j - 1];
			}
			k = s % 11;
			if (k < 10)
				break;
		}

		return k == nnArr[11];
	}

	private int[] getNNDigits(String nn) {
		int[] nnArr = new int[nn.length()];
		for (int i = 0; i < nn.length(); i++)
			nnArr[i] = Integer.parseInt(nn.substring(i, i + 1));
		return nnArr;
	}
}
