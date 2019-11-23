package com.bas.auction.core.utils.validation;

import java.util.Arrays;
import java.util.List;

public class IinBinValidator {

	public boolean isValid(String nn) {
		if (nn == null || nn.length() != 12)
			return false;
		List<String> isk = Arrays.asList("000000000000", "111111111111", "222222222222", "333333333333", "444444444444",
				"555555555555", "666666666666", "777777777777", "888888888888", "999999999999");
		if (isk.contains(nn))
			return false;
		int[] nnArr;
		try {
			nnArr = getNNDigits(nn);
		} catch (NumberFormatException e) {
			return false;
		}

		int s = 0;
		for (int i = 0; i < 11; i++)
			s = s + (i + 1) * nnArr[i];

		int k = s % 11;
		if (k == 10) {
			s = 0;
			for (int i = 0; i < 11; i++) {
				int t = (i + 3) % 11;
				if (t == 0)
					t = 11;
				s = s + t * nnArr[i];
			}
			k = s % 11;
			if (k == 10)
				return false;
			return k == nnArr[11];
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
