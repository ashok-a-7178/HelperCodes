package com.helpercodes.benchmark.metrics;

import com.helpercodes.benchmark.api.SearchResultItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record RankingMetrics(
        int intersection,
        double jaccard,
        double recallAtK,
        double spearman,
        double overlapTop1Pct,
        double overlapTop1To5Pct,
        double overlapTop5To10Pct) {

    public static RankingMetrics compare(List<SearchResultItem> a, List<SearchResultItem> b, int topK) {
        List<Integer> aIds = a.stream().limit(topK).map(SearchResultItem::docId).toList();
        List<Integer> bIds = b.stream().limit(topK).map(SearchResultItem::docId).toList();

        Set<Integer> aSet = new HashSet<>(aIds);
        Set<Integer> bSet = new HashSet<>(bIds);
        Set<Integer> inter = new HashSet<>(aSet);
        inter.retainAll(bSet);

        Set<Integer> union = new HashSet<>(aSet);
        union.addAll(bSet);

        int k = Math.max(1, Math.min(topK, Math.min(aIds.size(), bIds.size())));
        double recall = k == 0 ? 0 : inter.size() / (double) k;
        double jaccard = union.isEmpty() ? 0 : inter.size() / (double) union.size();

        double spearman = spearman(aIds, bIds);

        int top1End = Math.max(1, topK / 100);
        int top5End = Math.max(top1End + 1, topK * 5 / 100);
        int top10End = Math.max(top5End + 1, topK * 10 / 100);

        double top1 = overlap(aIds, bIds, 0, top1End);
        double top1To5 = overlap(aIds, bIds, top1End, top5End);
        double top5To10 = overlap(aIds, bIds, top5End, top10End);

        return new RankingMetrics(inter.size(), jaccard, recall, spearman, top1, top1To5, top5To10);
    }

    private static double overlap(List<Integer> a, List<Integer> b, int start, int end) {
        Set<Integer> as = slice(a, start, end);
        Set<Integer> bs = slice(b, start, end);
        if (as.isEmpty() && bs.isEmpty()) {
            return 0;
        }
        Set<Integer> inter = new HashSet<>(as);
        inter.retainAll(bs);
        Set<Integer> union = new HashSet<>(as);
        union.addAll(bs);
        return union.isEmpty() ? 0 : inter.size() / (double) union.size();
    }

    private static Set<Integer> slice(List<Integer> input, int start, int end) {
        if (start >= input.size()) {
            return Set.of();
        }
        int boundedEnd = Math.min(end, input.size());
        return new HashSet<>(input.subList(start, boundedEnd));
    }

    private static double spearman(List<Integer> aIds, List<Integer> bIds) {
        Map<Integer, Integer> aRanks = new HashMap<>();
        Map<Integer, Integer> bRanks = new HashMap<>();
        for (int i = 0; i < aIds.size(); i++) {
            aRanks.putIfAbsent(aIds.get(i), i + 1);
        }
        for (int i = 0; i < bIds.size(); i++) {
            bRanks.putIfAbsent(bIds.get(i), i + 1);
        }

        List<Double> diffs = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : aRanks.entrySet()) {
            Integer bRank = bRanks.get(e.getKey());
            if (bRank != null) {
                diffs.add((double) (e.getValue() - bRank));
            }
        }
        int n = diffs.size();
        if (n < 2) {
            return 0;
        }
        double sumSq = diffs.stream().mapToDouble(d -> d * d).sum();
        return 1.0 - ((6.0 * sumSq) / ((double) n * (n * n - 1)));
    }
}
