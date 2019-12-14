package utils;

import sample.Pseudoaleatorio;

import java.text.DecimalFormat;

public class Pruebas {
    public static double pseudos[] = { 0.363, 0.919,
                                      0.171, 0.151, 0.515, 0.424, 0.595, 0.747, 0.252, 0.676, 0.262,
                                      0.000, 0.252, 0.929, 0.181, 0.181, 0.434, 0.353, 0.535, 0.717,
                                      0.141, 0.494, 0.020, 0.737, 0.878, 0.363, 0.383, 0.111, 0.989,
                                      0.343};

    public static byte[] corridas = {1,0,0,1,0,1,1,0,1,0,0,1,1,0,0,1,0,1,1,0,1,0,1,1,0,1,0,1,0};

    public static void main(String[] args){
        DecimalFormat formatter = new DecimalFormat("##.####");
        Pseudoaleatorio pseudo = new Pseudoaleatorio();

        double media = pseudo.getMedia(Pruebas.pseudos, 30);
        double LSmedia = pseudo.getMediaLS(1.96, 30);
        double LImedia = pseudo.getMediaLI(1.96, 30);

        double var = pseudo.getVarianza(Pruebas.pseudos, media, 30);
        double LSvar = pseudo.getLSVar(45.722, 30);
        double LIvar = pseudo.getLIVar(16.047, 30);

        double chi = pseudo.getSquareChi(Pruebas.pseudos, 30);

        int corridas = pseudo.getCorridas(Pruebas.pseudos, 30);
        double Zo = pseudo.getZ0(corridas, 30);

        System.out.println("media: " + formatter.format(media));
        System.out.println("LS media: " + formatter.format(LSmedia));
        System.out.println("LI media: " + formatter.format( LImedia));
        System.out.println();
        System.out.println("Varianza: : " + formatter.format(var));
        System.out.println("LS varianza: " + formatter.format(LSvar));
        System.out.println("LI varianza: " + formatter.format(LIvar));
        System.out.println();
        System.out.println("Chi: " + formatter.format(chi));
        System.out.println();
        System.out.println("Corridas: : " + corridas);
        System.out.println("Z0: : " + formatter.format(Zo));

//        try {
//            double ale[] = pseudo.getAleatorios(7895, 2585, 100000);
//            for (Double a : ale)
//                System.out.println(a);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
