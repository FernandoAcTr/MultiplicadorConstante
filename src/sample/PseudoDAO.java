package sample;

public class PseudoDAO {

    private int index;
    private double aleatorio;

    public PseudoDAO(int index, double aleatorio) {
        this.index = index;
        this.aleatorio = aleatorio;
    }

    public int getIndex() {
        return index;
    }

    public double getAleatorio() {
        return aleatorio;
    }
}
