package com.bas.auction.neg.award.service.tiebreaker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bas.auction.bid.dto.BidLine;

public interface CriteriaBasedTiebreaker {
	void breakTies(List<BidLine> rankTiedBidLines);

	default <T extends Comparable<? super T>> Comparator<Entry<?, T>> comparator() {
		return (x, y) -> {
			T o1 = x.getValue(), o2 = y.getValue();
			if (o1 == null && o2 == null)
				return 0;
			if (o2 == null)
				return 1;
			if (o1 == null)
				return -1;
			return o1.compareTo(o2);
		};
	};

	default <K, T extends Comparable<? super T>> Map<K, Integer> rankDescending(Map<K, T> map) {
		return rank(map, Collections.reverseOrder(comparator()));
	}

	default <K, T extends Comparable<? super T>> Map<K, Integer> rankAscending(Map<K, T> map) {
		return rank(map, comparator());
	}

	default <K, T extends Comparable<? super T>> Map<K, Integer> rank(Map<K, T> map,
			Comparator<Entry<?, T>> comparator) {
		List<Entry<K, T>> list = new ArrayList<>(map.entrySet());
		list.sort(comparator);
		Entry<K, T> curr = null;
		Map<K, Integer> ranks = new LinkedHashMap<>();
		int r = 0, i = 0;
		for (Entry<K, T> entry : list) {
			if (curr == null || comparator.compare(entry, curr) != 0)
				r = i;
			curr = entry;
			ranks.put(entry.getKey(), r);
			i++;
		}
		return ranks;
	}
}
