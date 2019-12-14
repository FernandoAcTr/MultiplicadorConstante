package sample;

public class Pseudoaleatorio {

    /**
     * Algoritmo para extraer los D digitos centrales de una semilla
     * @param y semilla para extraer los digitos
     * @param numDigitos los D digitos a extraer de la semilla
     * @return
     */
    private long extraerDigitos(long y, int numDigitos) {
        String aux = String.valueOf(y);
        if ((aux.length() % 2 == 0 && numDigitos % 2 != 0) || (aux.length() % 2 != 0 && numDigitos % 2 == 0)) {
            aux = "0" + aux;
        }

        int lado = (aux.length() - numDigitos) / 2;
        String central = aux.substring(lado, aux.length() - lado);
        return Long.valueOf(central);
    }

    /**
     * Obtiene un arreglo de numeros pseudoaleatorios por el metodo de multiplicador constante
     * @param semilla
     * @param cte
     * @param numAleatorios cantidad de pseudoaleatorios a generar
     * @return
     * @throws Exception
     */
    public double[] getAleatorios(int semilla, int cte, int numAleatorios) throws Exception {
        String auxSemilla = String.valueOf(semilla);
        String auxCte = String.valueOf(cte);

        if (auxSemilla.length() <= 3 || auxCte.length() <= 3) {
            throw new Exception("La semilla y constante deben tener mas de 3 digitos");
        }
        if (auxSemilla.length() != auxCte.length()) {
            throw new Exception("La semilla y constante deben tener la misma cantidad de digitos");
        }

        int numDigitos = auxSemilla.length();
        long y, x;
        double r;
        double[] aleatorios = new double[numAleatorios];
        x = semilla;
        for (int i = 0; i < numAleatorios; i++) {
            y = x * cte;
            x = extraerDigitos(y, numDigitos);
            r = x / Math.pow(10, numDigitos);
            aleatorios[i] = r;
        }

        return aleatorios;
    }

    /*-*********************************************************************************************************
     * Prueba de Medias
     *********************************************************************************************************-*/

    /**
     * Obtiene la media de los n primeos numeros pseudoaleatorios del arreglo
     * @param aleatorios arreglo de numeros pseudoaleatorios
     * @param n
     * @return
     */
    public double getMedia(double aleatorios[], int n) {
        double sum = 0;

        for (int i = 0; i < n; i++)
            sum += aleatorios[i];

        return sum / n;
    }

    /**
     * Obtiene el limite superior para comparar el estadistico de prueba
     * @param tableZ El valor Z de las tablas que se utilizara
     * @param n
     * @return
     */
    public double getMediaLS(double tableZ, int n){
        return 0.5 + tableZ * (1.0/Math.sqrt(12.0*n));
    }

    /**
     * Obtiene el limite inferior para comparar el estadistico de prueba
     * @param tableZ El valor Z de las tablas que se utilizara
     * @param n
     * @return
     */
    public double getMediaLI(double tableZ, int n){
        return 0.5 - tableZ * (1.0/Math.sqrt(12.0*n));
    }

    /*-*********************************************************************************************************
     * Prueba de Varianza
     *********************************************************************************************************-*/
    /**
     * Obtiene la varianza de los n primeros numeros pseudoaleatorios
     * @param aleatorios
     * @param media
     * @param n
     * @return
     */
    public double getVarianza(double aleatorios[], double media, int n) {
        double sum = 0;

        for (int i = 0; i < n; i++)
            sum += Math.pow(aleatorios[i] - media, 2);

        return sum / (n - 1);
    }

    /**
     * Obtiene el Limite superior para comparar el estadistico Chi cuadrada
     * @param tableChi valor de la Chi de alpha/2 con n-1 grados de libertad
     * @param n total de datos
     * @return
     */
    public double getLSVar(double tableChi, int n){
        return tableChi/(12*(n-1));
    }

    /**
     * Obtiene el Limite superior para comparar el estadistico Chi cuadrada
     * @param tableChi valor de la Chi de (1 - alpha/2) con n-1 grados de libertad
     * @param n total de datos
     * @return
     */
    public double getLIVar(double tableChi, int n){
        return tableChi/(12*(n-1));
    }

    /*-*********************************************************************************************************
     * Prueba de Uniformidad
     *********************************************************************************************************-*/
    /**
     * Obtiene la Chi cuadrada tomando los n primeros pseudoaleatorios, para la prueba de Uniformidad
     * @param aleatorios
     * @param n
     * @return
     */
    public double getSquareChi(double aleatorios[], int n) {
        double sum = 0;
        int numIntervalos = (int) Math.sqrt(n);
        double anchoInter = 1.0 / numIntervalos;
        int freqEsperada = n / numIntervalos;

        double intervalos[] = new double[numIntervalos + 1]; //+1 porque se incluye el 0 en la primer posicion

        //generamos los intervalos
        intervalos[0] = 0;

        for (int i = 1; i <= numIntervalos; i++)
            intervalos[i] = intervalos[i - 1] + anchoInter;

        //Por cada intervalo contamos los numeros que existen en el
        int freqObs;
        for (int i = 1; i <= numIntervalos; i++) {
            freqObs = 0;
            for (int k = 0; k < n; k++) {
                if (aleatorios[k] >= intervalos[i - 1] && aleatorios[k] < intervalos[i])
                    freqObs++;
            }

            sum += Math.pow(freqEsperada - freqObs, 2) / freqEsperada;
        }

        return sum;
    }


    /*-*********************************************************************************************************
     * Prueba de Independencia
     *********************************************************************************************************-*/
    /**
     * Obtiene el numero de corridas tomando los n primeros pseudoaleatorios, para la prueba de Independencia
     * @param aleatorios
     * @param n
     * @return
     */
    public int getCorridas(double aleatorios[], int n){
        int numCo = 1;
        byte[] cadena = new byte[n-1];

        for (int i = 1; i < n; i++) {
            if(aleatorios[i] > aleatorios[i-1])
                cadena[i-1] = 1;
            else
                cadena[i-1] = 0;
        }

        for (int i = 1; i < cadena.length; i++) {
            if(cadena[i] != cadena[i-1])
                numCo++;
        }

        return numCo;
    }

    /**
     * Obtiene el estadistico Z0 para prueba de Independencia
     * @param Co Numero de Corridas
     * @param n total de datos
     * @return
     */
    public double getZ0(int Co, int n){
        double MCo = (2*n-1)/3.0;
        double varCo = Math.sqrt((16*n-29)/90.0);
        return Math.abs((Co - MCo)/varCo);
    }
}

