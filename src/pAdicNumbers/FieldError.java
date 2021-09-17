package pAdicNumbers;

public class FieldError extends Error{
    public FieldError(int p, int q){
        System.out.println("Invalid field of argument. Expected: " + p + " Received: " + q);
    }
}
