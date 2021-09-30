package pAdicNumbers;

import Poly.Poly;

public class RootDoesntExist extends Error{
    public RootDoesntExist(Poly p, int prime){
        System.out.println("Polynomial " + p.toString() + " has no roots in Q_" + prime);
    }
}
