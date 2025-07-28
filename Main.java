import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        // read & split JSON objects
        String content = readAll("input.json").trim();
        List<String> jsonObjs = splitTopLevelObjects(content);

        int tc = 1;
        for (String js : jsonObjs) {
            TestCase t = parseTestCase(js);
            Result r = reconstructWithErrorDetection(t);
            System.out.println("Test-case #" + (tc++) + " ⇒ secret c = " + r.secret);
            if (!r.invalidShares.isEmpty()) {
                System.out.println("  Invalid shares: " + r.invalidShares);
            }
        }
    }

    private static String readAll(String fn) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new FileReader(fn))) {
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private static List<String> splitTopLevelObjects(String s) {
        s = s.trim();
        if (s.startsWith("[")) {
            s = s.substring(1, s.lastIndexOf(']'));
        }
        List<String> out = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') {
                if (depth++ == 0) start = i;
            } else if (c == '}') {
                if (--depth == 0 && start != -1) {
                    out.add(s.substring(start, i+1));
                    start = -1;
                }
            }
        }
        return out;
    }

    static class TestCase {
        int n, k;
        TreeMap<Integer, BigInteger> shares = new TreeMap<>();
    }

    static class Result {
        BigInteger secret;
        List<Integer> invalidShares;
        Result(BigInteger s, List<Integer> bad) {
            secret = s; invalidShares = bad;
        }
    }

    private static TestCase parseTestCase(String js) throws IOException {
        TestCase tc = new TestCase();
        BufferedReader br = new BufferedReader(new StringReader(js));
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.matches("\"n\"\\s*:\\s*\\d+.*")) {
                tc.n = Integer.parseInt(line.replaceAll("[^0-9]", ""));
            } else if (line.matches("\"k\"\\s*:\\s*\\d+.*")) {
                tc.k = Integer.parseInt(line.replaceAll("[^0-9]", ""));
            } else if (line.matches("\"\\d+\"\\s*:\\s*\\{")) {
                int x = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                String baseLine  = br.readLine().trim();
                String valueLine = br.readLine().trim();
                int base = Integer.parseInt(baseLine.replaceAll("[^0-9]", ""));
                String val = valueLine.split(":")[1].replaceAll("[^A-Za-z0-9]", "");
                tc.shares.put(x, new BigInteger(val, base));
            }
        }
        return tc;
    }

    private static Result reconstructWithErrorDetection(TestCase tc) {
        int n = tc.n, k = tc.k;
        List<Integer> xs = new ArrayList<>(tc.shares.keySet());
        Map<BigInteger, Integer> freq = new HashMap<>();
        Map<Integer, Integer> goodCount = new HashMap<>();
        for (int x: xs) goodCount.put(x, 0);

        int[] idx = new int[k];
        for (int i = 0; i < k; i++) idx[i] = i;
        while (true) {
            BigInteger[] Ys = new BigInteger[k];
            BigInteger[][] A = new BigInteger[k][k];
            for (int i = 0; i < k; i++) {
                int x = xs.get(idx[i]);
                BigInteger xi = BigInteger.valueOf(x);
                Ys[i] = tc.shares.get(x);
                for (int j = 0; j < k; j++) {
                    A[i][j] = xi.pow(j);
                }
            }
            BigInteger c0 = gaussianElim(A, Ys)[0];
            freq.merge(c0, 1, Integer::sum);

            int p = k-1;
            while (p >= 0 && idx[p] == n - k + p) p--;
            if (p < 0) break;
            idx[p]++;
            for (int j = p+1; j < k; j++) idx[j] = idx[j-1] + 1;
        }

        BigInteger secret = Collections.max(freq.entrySet(),
            Comparator.comparingInt(Map.Entry::getValue)).getKey();

        Arrays.setAll(idx, i -> i);
        while (true) {
            BigInteger[][] A = new BigInteger[k][k];
            BigInteger[] Ys = new BigInteger[k];
            for (int i = 0; i < k; i++) {
                int x = xs.get(idx[i]);
                BigInteger xi = BigInteger.valueOf(x);
                Ys[i] = tc.shares.get(x);
                for (int j = 0; j < k; j++) {
                    A[i][j] = xi.pow(j);
                }
            }
            BigInteger c0 = gaussianElim(A, Ys)[0];
            if (c0.equals(secret)) {
                for (int i = 0; i < k; i++) {
                    goodCount.merge(xs.get(idx[i]), 1, Integer::sum);
                }
            }
            int p = k-1;
            while (p >= 0 && idx[p] == n - k + p) p--;
            if (p < 0) break;
            idx[p]++;
            for (int j = p+1; j < k; j++) idx[j] = idx[j-1] + 1;
        }

        List<Integer> invalid = new ArrayList<>();
        for (var e : goodCount.entrySet()) {
            if (e.getValue() == 0) invalid.add(e.getKey());
        }

        return new Result(secret, invalid);
    }

    private static BigInteger[] gaussianElim(BigInteger[][] A, BigInteger[] Y) {
        int n = Y.length;
        for (int i = 0; i < n; i++) {
            if (A[i][i].equals(BigInteger.ZERO)) {
                for (int r = i+1; r < n; r++) {
                    if (!A[r][i].equals(BigInteger.ZERO)) {
                        BigInteger[] tmp = A[i]; A[i] = A[r]; A[r] = tmp;
                        BigInteger ty = Y[i];   Y[i] = Y[r];   Y[r] = ty;
                        break;
                    }
                }
            }
            for (int r = i+1; r < n; r++) {
                BigInteger f = A[r][i].divide(A[i][i]);
                for (int c = i; c < n; c++) {
                    A[r][c] = A[r][c].subtract(f.multiply(A[i][c]));
                }
                Y[r] = Y[r].subtract(f.multiply(Y[i]));
            }
        }
        BigInteger[] X = new BigInteger[n];
        for (int i = n-1; i >= 0; i--) {
            BigInteger sum = BigInteger.ZERO;
            for (int j = i+1; j < n; j++) {
                sum = sum.add(A[i][j].multiply(X[j]));
            }
            X[i] = (Y[i].subtract(sum)).divide(A[i][i]);
        }
        return X;
    }
}
