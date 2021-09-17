package pAdicNumbers;

import java.math.BigInteger;

public class PAdicc {
    private final BigInteger num;
    private final int val;
    private final int p;
    private static int precision = 1000;
    private static BigInteger almostZero;
    private static boolean updateNeeded = true;

    public PAdicc(String s, int val, int p){
        num = new BigInteger(s);
        this.val = val;
        this.p = p;
    }

    public PAdicc(BigInteger n, int val, int p){
        num = n;
        if (num.equals(BigInteger.ZERO)) this.val = Integer.MAX_VALUE;
        else this.val = val;
        this.p = p;
    }

    public PAdicc(BigInteger n, int p){
        int val1 = 0;
        BigInteger bigP = BigInteger.valueOf(p);
        while (n.mod(bigP).equals(BigInteger.ZERO)){
            val1++;
            n = n.divide(bigP);
        }
        num = n;
        if (num.equals(BigInteger.ZERO)) val1 = Integer.MAX_VALUE;
        this.val = val1;
        this.p = p;
    }

    private static byte charToByte(char c){
        if (c >= '0' && c <= '9') return (byte) (c - '0');
        if (c >= 'A' && c <= 'Z') return (byte) (c - 'A' + 10);
        else throw new RuntimeException("Couldn't convert character: " + c + " to byte.");
    }

    public int getP() {
        return p;
    }

    public int getVal(){
        return val;
    }

    public double norm(){
        return Math.pow(p, -val);
    }

    public static int getPrecision(){
        return precision;
    }

    public static void setPrecision(int n){
        if (n <= 0) throw new RuntimeException("Non-positive precision");
        if (n > precision) updateNeeded = true;
        precision = n;
    }
    
    private BigInteger getAlmostZero(){
        if (updateNeeded || !almostZero.mod(BigInteger.valueOf(p)).equals(BigInteger.ZERO)) {
            almostZero = BigInteger.valueOf(p).pow(precision);
            updateNeeded = false;
        }
        return almostZero;
    }
    
    public PAdicc add(PAdicc a){
        if (p != a.p) throw new FieldError(p, a.p);
        int diff = val - a.val;
        BigInteger shift = BigInteger.valueOf((int) Math.pow(p, Math.abs(diff)));
        BigInteger sumNum;
        if (diff < 0) sumNum = num.add(a.num.multiply(shift));
        else sumNum = a.num.add(num.multiply(shift));
        int sumVal = Math.min(val, a.val);
        while (sumNum.mod(BigInteger.valueOf(p)).signum() == 0){
            sumNum = sumNum.divide(BigInteger.valueOf(p));
            sumVal ++;
        }
        sumNum = sumNum.mod(this.getAlmostZero());
        return new PAdicc(sumNum, sumVal, p);
    }
    
    public PAdicc mul(PAdicc a){
        BigInteger prodNum = this.num.multiply(a.num);
        int prodVal = this.val + a.val;
        prodNum = prodNum.mod(this.getAlmostZero());
        return new PAdicc(prodNum, prodVal, p);
    }
    
    public PAdicc neg(){
        BigInteger negNum = this.num;
        negNum = this.getAlmostZero().subtract(negNum);
        return new PAdicc(negNum, this.val, p);
    }
    
    public PAdicc sub(PAdicc a){
        return this.add(a.neg());
    }
    
    public int[] getDigits(){
        BigInteger copy = num;
        BigInteger bigP = BigInteger.valueOf(p);
        int[] digits = new int[precision];
        for (int i = 0; i < precision; i++){
            BigInteger rem = copy.mod(bigP);
            digits[i] = rem.intValue(); 
            copy = copy.subtract(rem).divide(bigP);
        }
        return digits;
    }
    
    private int powModulo(int base, int exp){
        base = base % p;
        if (exp == 0) return 1;
        if (exp % 2 == 0) return powModulo((base * base) % p, exp / 2) % p;
        return  (base * powModulo((base * base) % p, exp / 2) ) % p;
    }
    
    private int inverseModulo(int n){
        if (n % p == 0) throw new RuntimeException("Zero division");
        return powModulo(n, p - 2);
    }
    
    private int logFloor2(int n){
        int out = 0;
        while (n > 1){
            n = n / 2;
            out++;
        }
        return out;
    }
    
    public PAdicc inv(){
        int n = logFloor2(precision) + 1;
        BigInteger TWO = BigInteger.valueOf(2);
        BigInteger pPow = BigInteger.valueOf(p);
        BigInteger inv = BigInteger.valueOf(this.inverseModulo(num.mod(BigInteger.valueOf(p)).intValue()));
        for (int i = 0; i < n; i++){
            pPow = pPow.pow(2);
            inv = inv.multiply(TWO.subtract(num.multiply(inv))).mod(pPow);
        }
        int invVal = -val;
        return new PAdicc(inv, invVal, p);
    }
    
    public PAdicc div(PAdicc a){
        if (a.num.equals(BigInteger.ZERO)) throw new RuntimeException("Division by zero");
        return this.mul(a.inv());
    }
    
    public void print(int digits){
        if (p > 7) { System.out.println("Error, p > 7"); return; }
        StringBuilder s = new StringBuilder();
        BigInteger copy = num;
        BigInteger bigP = BigInteger.valueOf(p);
        int k = val;
        for (int i = 0; i < k && i < digits; i++) {s.append('0'); digits--;}
        for (int i = 0; i < digits; i++){
            BigInteger rem = copy.mod(bigP);
            s.append(rem.toString());
            copy = copy.subtract(rem).divide(bigP);
            k++;
            if (k == 0) s.append('.');
        }
        s.reverse();
        System.out.println(s);
    }

    @Override
    public String toString() {
        if (num.equals(BigInteger.ZERO)) return "0";
        StringBuilder s = new StringBuilder();
        BigInteger copy = num;
        BigInteger bigP = BigInteger.valueOf(p);
        int k = val;
        int digits = 84;
        for (int i = 0; i < k && i < digits; i++) {s.append('0'); digits--;}
        for (int i = 0; i < digits; i++){
            BigInteger rem = copy.mod(bigP);
            s.append(rem.toString());
            copy = copy.subtract(rem).divide(bigP);
            k++;
            if (k == 0) s.append('.');
            if (copy.equals(BigInteger.ZERO)){
                if (k == 0) s.append('0');
                break;
            }
        }
        s.reverse();
        while (s.charAt(0) == '0' && s.charAt(1) != '.'){
            s.deleteCharAt(0);
        }
        return s.toString();
    }

    public void print(){
        print(20);
    }
}

