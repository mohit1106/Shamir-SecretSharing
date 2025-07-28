import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        String content = readAll("input.json").trim();
        
        List<String> jsonObjects = splitTopLevelObjects(content);
        if (jsonObjects.isEmpty()) {
            System.err.println("No JSON objects found in input.json");
            return;
        }

        int tc = 1;
        for (String js : jsonObjects) {
            TestCase t = parseTestCase(js);
            BigInteger c = recoverConstant(t);
            System.out.println("Test-case #" + (tc++) + " ⇒ c = " + c);
        }
    }

    private static String readAll(String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private static List<String> splitTopLevelObjects(String s) {
        s = s.trim();
        List<String> objs = new ArrayList<>();
        if (s.startsWith("[")) {
            s = s.substring(1, s.lastIndexOf(']'));
        }
        int depth = 0, start = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    objs.add(s.substring(start, i+1));
                    start = -1;
                }
            }
        }
        return objs;
    }

    static class TestCase {
        int n, k;
        TreeMap<Integer, BigInteger> shares = new TreeMap<>();
    }

    private static TestCase parseTestCase(String js) throws IOException {
        TestCase tc = new TestCase();
        try (BufferedReader br = new BufferedReader(new StringReader(js))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // match "n":  10
                if (line.matches("\"n\"\\s*:\\s*\\d+.*")) {
                    tc.n = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                }
                // match "k": 7
                else if (line.matches("\"k\"\\s*:\\s*\\d+.*")) {
                    tc.k = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                }
                // match "123": {
                else if (line.matches("\"\\d+\"\\s*:\\s*\\{")) {
                    int x = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                    String baseLine  = br.readLine().trim();
                    String valueLine = br.readLine().trim();
                    int base = Integer.parseInt(baseLine.replaceAll("[^0-9]", ""));
                    String val = valueLine.split(":")[1].replaceAll("[^A-Za-z0-9]", "");
                    BigInteger y = new BigInteger(val, base);
                    tc.shares.put(x, y);
                }
            }
        }
        return tc;
    }

    private static BigInteger recoverConstant(TestCase tc) {
        int k = tc.k;
        // pick smallest k x‑values
        List<Map.Entry<Integer,BigInteger>> sel =
            new ArrayList<>(tc.shares.entrySet()).subList(0, k);

        BigInteger[][] A = new BigInteger[k][k];
        BigInteger[]   Y = new BigInteger[k];
        for (int i = 0; i < k; i++) {
            BigInteger xi = BigInteger.valueOf(sel.get(i).getKey());
            Y[i] = sel.get(i).getValue();
            for (int j = 0; j < k; j++) {
                A[i][j] = xi.pow(j);
            }
        }
        BigInteger[] coeffs = gaussianElim(A, Y);
        return coeffs[0];  
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
