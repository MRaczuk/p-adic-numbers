package pAdicNumbers;

import Poly.Poly;

import java.math.BigInteger;
import java.util.LinkedList;

/**
 * Class representing p-adic numbers.
 * The numbers are represented in the form m = n * p^(val)
 * where val is p-adic valuation of m.
 * (val = Integer.MAX_VALUE for m = 0)
 */
public class PAdic {
    private final BigInteger num;
    private final int val;
    private final int p;
    private static int precision = 1000; //< Number of relevant digits (in Q_p)
    //Please note that precision parameter determines computation precision only
    //and that creating p-adic numbers with higher than set precision is always
    //possible via constructors. Also it does not influence already created numbers.
    
    private static BigInteger almostZero;
    private static boolean updateNeeded = true;
    
    //Please note that (so far ?) constructors don't check whether given parameters
    //are correct / perform only partial validation of arguments, thus
    //incorrect parameters yield undefined behaviour
    
    /**
     * Constructs p-adic number from string with its digits
     * @param s non-empty string with digits in Q_p of a given numbers
     * @param p prime
     */
    public PAdic(String s, int p){
        int val = 0;
        boolean nonZeroOccured = false;
        boolean dotOccured = false;
        int stop = 0;
        for (int i = s.length() - 1; i >= 0; i--){
            char current = s.charAt(i);
            if (Character.isLetterOrDigit(current)){
                if (!nonZeroOccured && current != '0') {
                    nonZeroOccured = true;
                    stop = i;
                    val += s.length() - 1 - i;
                    if (dotOccured) val -= 1;
                }
                if (PAdic.charToByte(current) >= p) 
                    throw new RuntimeException("Cannot parse string: " + s + " to p-adic integer.");
            }
            else if (current == '.' && !dotOccured) {
                val -= s.length() - 1 - i;
                dotOccured = true;
            }
            else throw new RuntimeException("Cannot parse string: " + s + " to p-adic integer.");
        }
        BigInteger num = BigInteger.ZERO;
        for (int i = 0; i <= stop; i++){
            if (s.charAt(i) == '.') continue;
            num = num.multiply(BigInteger.valueOf(p));
            num = num.add(BigInteger.valueOf(PAdic.charToByte(s.charAt(i))));
        }
        this.num = num;
        if (num.equals(BigInteger.ZERO)) this.val = Integer.MAX_VALUE;
        else this.val = val;
        this.p = p;
    }

    /**
     * Constructs p-adic number from n not divisible by p
     * and it's p-adic valuation val (or n = 0 and whatever)
     * @param n  BigInteger
     * @param val p-adic valuation
     * @param p prime
     */
    public PAdic(BigInteger n, int val, int p){
        num = n;
        if (num.equals(BigInteger.ZERO)) this.val = Integer.MAX_VALUE;
        else this.val = val;
        this.p = p;
    }

    /**
     * Constructs p-adic number from BigInteger
     * @param n value of p-adic number
     * @param p prime
     */
    public PAdic(BigInteger n, int p){
        this.p = p;
        if (n.equals(BigInteger.ZERO)) {this.val = Integer.MAX_VALUE; num = n; return;}
        int val1 = 0;
        BigInteger bigP = BigInteger.valueOf(p);
        while (n.mod(bigP).equals(BigInteger.ZERO)){
            val1++;
            n = n.divide(bigP);
        }
        num = n;
        if (num.equals(BigInteger.ZERO)) val1 = Integer.MAX_VALUE;
        this.val = val1;
    }

    /**
     * Constructs p-adic number from integer
     * @param m value of p-adic number
     * @param p prime
     */
    public PAdic(int m, int p){
        this(BigInteger.valueOf(m), p);
    }

    /**
     * Constructs p-adic number from rational number n/m. 
     * @param m numerator
     * @param n denominator
     * @param p prime
     */
    public PAdic(int n, int m, int p){
        PAdic x = (new PAdic(n, p)).div(new PAdic(m, p));
        this.num = x.num;
        this.val = x.val;
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

    /**
     * Returns a sum of this and given p-adic number.
     * IMPLEMENTATION:
     * We can see that it is easy to add 2 non divisible by p elements of Z_p
     * In general case of Q_p we can just multiply the arguments by proper
     * power of p and easily calculate p-adic valuation of the result.
     * @param a p-adic number
     * @return sum
     */
    public PAdic add(PAdic a){
        if (p != a.p) throw new FieldError(p, a.p);
        int diff = val - a.val;
        BigInteger shift = BigInteger.valueOf(p).modPow(BigInteger.valueOf(Math.abs(diff)), this.getAlmostZero());
        BigInteger sumNum;
        if (diff < 0) sumNum = num.add(a.num.multiply(shift));
        else sumNum = a.num.add(num.multiply(shift));
        int sumVal = Math.min(val, a.val);
        if (sumVal == Integer.MAX_VALUE) return new PAdic(0, p);
        if (sumNum.compareTo(BigInteger.ZERO) == 0) return new PAdic(0, p);
        while (sumNum.mod(BigInteger.valueOf(p)).signum() == 0){
            sumNum = sumNum.divide(BigInteger.valueOf(p));
            sumVal ++;
            if (sumVal == Integer.MAX_VALUE) return new PAdic(0, p);
        }
        return new PAdic(sumNum, sumVal, p);
    }
    
    public PAdic add(int a){
        return this.add(new PAdic(a, p));
    }

    /**
     * Returns a product of this and given p-adic number.
     * IMPLEMENTATION:
     * Similarly to addition we can just multiply nums and
     * then calculation of p-adic valuation of the result is straightforward
     * from its definition.
     * @param a p-adic number
     * @return product
     */
    public PAdic mul(PAdic a){
        BigInteger prodNum = this.num.multiply(a.num);
        int prodVal = this.val + a.val;
        prodNum = prodNum.mod(this.getAlmostZero());
        return new PAdic(prodNum, prodVal, p);
    }

    public PAdic mul(int a){
        return this.mul(new PAdic(a, p));
    }

    /**
     * Regular substraction is sufficient after choosing
     * sufficiently large power of p (p^N ≈ 0 for large N)
     * @return negation
     */
    public PAdic neg(){
        BigInteger negNum = this.num;
        negNum = this.getAlmostZero().subtract(negNum);
        PAdic out = new PAdic(negNum, this.val, p);
        return new PAdic(negNum, this.val, p);
    }
    
    public PAdic sub(PAdic a){
        return this.add(a.neg());
    }
    public PAdic sub(int a){
        return this.sub(new PAdic(a, p));
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

    private int pow(int base, int exp){
        if (exp == 0) return 1;
        if (exp % 2 == 0) return pow((base * base), exp / 2);
        return  (base * pow((base * base), exp / 2) );
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

    /**
     * Firstly we calculate U_0 - inverse mod p of non-divisible by p
     * element of Z_p and then we raise it to inverse mod p^(2^n)
     * using recurrence relation U_{n_1} = U_n(2 - a*U_n), where a
     * is number to be inverted. After that valuation of the result
     * can be easily calculated
     * @return inverse
     */
    public PAdic inv(){
        int n = logFloor2(precision) + 1;
        BigInteger TWO = BigInteger.valueOf(2);
        BigInteger pPow = BigInteger.valueOf(p);
        BigInteger inv = BigInteger.valueOf(this.inverseModulo(num.mod(BigInteger.valueOf(p)).intValue()));
        for (int i = 0; i < n; i++){
            pPow = pPow.pow(2);
            inv = inv.multiply(TWO.subtract(num.multiply(inv))).mod(pPow);
        }
        int invVal = -val;
        return new PAdic(inv, invVal, p);
    }
    
    public PAdic div(PAdic a){
        if (a.num.equals(BigInteger.ZERO)) throw new RuntimeException("Division by zero");
        return this.mul(a.inv());
    }

    public PAdic div(int a){
        return this.div(new PAdic(a, p));
    }
    
    public static PAdic Newton(Poly p, int x, int prime){
        PAdic prev = new PAdic(x, prime), curr = new PAdic(0, prime);
        Poly q = p.derivative();
        if (q.evaluate(x) == 0) throw new ArithmeticException("Division by 0");
        PAdic zero = new PAdic(0, 7);
        for (int i = 0; i < PAdic.precision; i++){
            curr = prev.sub(p.evaluate(prev).div(q.evaluate(prev)));
            prev = curr;
        }
        return curr;
    }
    
    /**
     * Searches for arbitrary root of given polynomial using
     * Generalized Hensel Lemma.
     * @param p polynomial
     * @param n highest n for which GHL is checked before giving up
     * @param prime prime         
     * @return arbitrary root or null in case of giving up. Please note that returining null doesn't
     * mean that the root doesn't exist. 
     */
    public static PAdic GHL(Poly p, int n, int prime){
        Poly q = p.derivative();
        int pow = 1, x = 1;
        LinkedList<Integer> prev = new LinkedList<Integer>();
        LinkedList<Integer> curr = new LinkedList<Integer>();
        curr.add(0);
        for (int i = 0; i <= n; i++){
            for (int k = 0; k < 2; k++) {
                prev = curr;
                curr = new LinkedList<Integer>();
                for (int r : prev) for (int j = 0; j < prime; j++){ 
                    if (p.evaluate(r + j * pow, pow * prime) == 0) curr.add(r + j * pow);
                }
                pow *= prime;
            }
            if (prev.isEmpty()) throw new RootDoesntExist(p, prime);
            for (int r : prev){
                int v = q.evaluate(r, x * prime);
                if (v % (x * prime) != 0 && v % x == 0) return Newton(p, r, prime);
            }
            x *= prime;
        }
        return null;
    }

    /**
     * Prints digits of p-adic number with respect to given set of representatives. Incorrect
     * representatives set results in UB.
     * @param digits number of digits to be written
     * @param representatives (array of size p of elements of Z_p, i-th representative is to be congruent to i mod p) 
     */
    public void printRepresented(int digits, PAdic[] representatives){
        if (p > 31) { System.out.println("Error, p > 31"); return; }
        for (int i = 0; i < representatives.length; i++){
            representatives[i] = new PAdic(representatives[i].num.mod(this.getAlmostZero()), this.p); 
        }
        StringBuilder s = new StringBuilder();
        BigInteger copy = num;
        BigInteger bigP = BigInteger.valueOf(p);
        BigInteger shift = bigP.modPow(BigInteger.valueOf(representatives[0].val) , this.getAlmostZero());
        int k = val;
        for (int i = 0; i < k && i < digits; i++) {s.append('0'); digits--;}
        for (int i = 0; i < digits; i++){
            BigInteger rem = copy.mod(bigP);
            if (rem.compareTo(BigInteger.valueOf(10)) < 0) s.append(rem.toString());
            else s.append((char) ('A' + rem.intValue() - 10));
            if (copy.compareTo(this.getAlmostZero()) < 0) copy = copy.add(this.getAlmostZero());
            if (rem.equals(BigInteger.ZERO)) copy = copy.subtract(representatives[0].num.multiply(shift)).divide(bigP);
            else copy = copy.subtract(representatives[rem.intValue()].num).divide(bigP);
            k++;
            if (k == 0) s.append('.');
        }
        s.reverse();
        System.out.println(s);
    }

    /**
     * Returnes Teichmuller representatives for Q_this.p
     * @param p prime
     * @return Teichmuller representatives
     */
    public static PAdic[] getTeichmullerRepresentatives(int p){
        PAdic[] out = new PAdic[p];
        int[] coeff = new int[p + 1];
        coeff[1] = -1;
        coeff[p] = 1;
        Poly poly = new Poly(coeff);
        for (int i = 0; i < p; i++) out[i] = Newton(poly, i, p);
        return out;
    }
    
    public void print(int digits){
        if (p > 31) { System.out.println("Error, p > 31"); return; }
        StringBuilder s = new StringBuilder();
        BigInteger copy = num;
        BigInteger bigP = BigInteger.valueOf(p);
        int k = val;
        for (int i = 0; i < k && i < digits; i++) {s.append('0'); digits--;}
        for (int i = 0; i < digits; i++){
            BigInteger rem = copy.mod(bigP);
            if (rem.compareTo(BigInteger.valueOf(10)) < 0) s.append(rem.toString());
            else s.append((char) ('A' + rem.intValue() - 10));
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
        if (p > 31) return "p > 31";
        StringBuilder s = new StringBuilder();
        BigInteger copy = num;
        BigInteger bigP = BigInteger.valueOf(p);
        int k = val;
        int digits = 284;
        for (int i = 0; i < k && i < digits; i++) {s.append('0'); digits--;}
        for (int i = 0; i < digits; i++){
            BigInteger rem = copy.mod(bigP);
            if (rem.compareTo(BigInteger.valueOf(10)) < 0) s.append(rem.toString());
            else s.append((char) ('A' + rem.intValue() - 10));
            copy = copy.subtract(rem).divide(bigP);
            k++;
            if (k == 0) s.append('.');
            if (copy.equals(BigInteger.ZERO)){
                if (k == 0) s.append('0');
                break;
            }
        }
        s.reverse();
        return s.toString();
    }

    public void print(){
        print(20);
    }
}

