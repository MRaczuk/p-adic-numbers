/*  The MIT License (MIT)

Copyright © 2021 <Maciej Raczuk>

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the “Software”),
to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is furnished
to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package Poly;

import pAdicNumbers.PAdic;

import java.util.ArrayList;

/**
 * Class representing elements of Z[X].
 */
public class Poly {
    private final ArrayList<Integer> coeff; //coeffs of polynomial from a_0 to a_n.
    private final int deg;
    
    public Poly(ArrayList<Integer> coeff){
        int deg = -1;
        for (int i = 0; i < coeff.size(); i++) if (coeff.get(i) != 0) deg = i;
        this.deg = deg;
        if (deg < 0) this.coeff = null;
        else this.coeff = (ArrayList<Integer>) coeff.clone();
    }

    public Poly(int[] coeff){
        int deg = -1;
        for (int i = 0; i < coeff.length ; i++) if (coeff[i] != 0) deg = i;
        this.deg = deg;
        if (deg < 0) this.coeff = null;
        else {
            ArrayList<Integer> ALcoeff = new ArrayList<Integer>();
            for (int c : coeff) ALcoeff.add(c);
            this.coeff = ALcoeff;
        }
    }
    
    public boolean isZero(){
        return deg < 0;
    }
    
    public int getDeg(){
        return deg;
    }
    
    public Poly add(Poly a){
        ArrayList<Integer> newCoeff = new ArrayList<Integer>();
        for (int i = 0; i <= Math.max(a.deg, this.deg); i++){
            int add = 0;
            if (i <= a.deg) add += a.coeff.get(i); 
            if (i <= this.deg) add += this.coeff.get(i);
            newCoeff.add(add);
        }
        return new Poly(newCoeff);
    }
    
    public int evaluate(int n, int mod){
        if (this.isZero()) return 0;
        n %= mod;
        int out = 0;
        for (int i = this.deg; i >= 0; i--){
            out *= n;
            out += this.coeff.get(i);
            out %= mod;
        }
        return out;
    }

    public int evaluate(int n){
        if (this.isZero()) return 0;
        int out = 0;
        for (int i = this.deg; i >= 0; i--){
            out *= n;
            out += this.coeff.get(i);
        }
        return out;
    }

    public PAdic evaluate(PAdic n){
        if (this.isZero()) return new PAdic(0, n.getP());
        PAdic out = new PAdic(0, n.getP());
        for (int i = this.deg; i >= 0; i--){
            out = out.mul(n);
            out = out.add(this.coeff.get(i));
        }
        return out;
    }
    
    public Poly derivative(){//
        ArrayList<Integer> derArr = (ArrayList<Integer>) this.coeff.clone();
        derArr.remove(0);
        for (int i = 0; i < derArr.size(); i++){
            derArr.set(i, (i + 1) * derArr.get(i));
        }
        return new Poly(derArr);
    }
    
    @Override
    public String toString() {
        if (this.isZero()) return "0";
        StringBuilder s = new StringBuilder();
        for (int i = 0; i <= this.deg; i++){
            if (this.coeff.get(i) == 0) continue;
            if (i != 0) s.append(" + ");
            if (i != 0 && this.coeff.get(i) != 1) s.append(this.coeff.get(i).toString()).append("x^").append(i);
            else if (i != 0) s.append("x^").append(i);
            else s.append(this.coeff.get(i).toString());
        }
        return s.toString();
    }
}
