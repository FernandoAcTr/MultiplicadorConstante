package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class Controller implements Initializable {

    @FXML
    private JFXTextField txtSeed;

    @FXML
    private JFXTextField txtConstant;

    @FXML
    private JFXTextField txtAleatorios;

    @FXML
    private JFXButton btnGenerate;

    @FXML
    private Label lblMedia;

    @FXML
    private Label lblVar;

    @FXML
    private Label lblChi;

    @FXML
    private Label lblCo;

    @FXML
    private Label lblLSMedia;

    @FXML
    private Label lblLIMedia;

    @FXML
    private Label lblLSVar;

    @FXML
    private Label lblLIVar;

    @FXML
    private Label lblZ0;

    @FXML
    private MenuItem mnuConfTableZ, mnuConfTableChiLS, mnuConfTableChiLI;

    @FXML
    private TableView<PseudoDAO> tblData;

    @FXML
    private ScatterChart<Number, Number> chart;

    Pseudoaleatorio pseudo;
    ObservableList<PseudoDAO> listPseudos;

    DecimalFormat formatter;
    double aleatorios[];

    boolean graphic = true; //para controlar la cancelacion del hilo de graficacion

    double tableZ, tableChiLS, tableChiLI;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initData();
        initComponents();
        initTable();
    }

    private void initData(){
        pseudo = new Pseudoaleatorio();
        listPseudos = FXCollections.observableArrayList();
        formatter = new DecimalFormat("##.###");
        tableZ = 1.96; //valor de Z con significancia de 0.05
        tableChiLS = 45.722; //valor de Chi cuadrada para GL = 29 (30 datos) y alfa/2 = 0.025 (alfa = 0.05)
        tableChiLI = 16.047; //valor de Chi cuadrada para GL = 29 (30 datos) y 1 - alfa/2 = 0.975 (alfa = 0.05)
    }

    private void initComponents() {
        btnGenerate.setOnAction(event -> {
            int seed = Integer.valueOf(txtSeed.getText());
            int constant = Integer.valueOf(txtConstant.getText());
            int numAleatorios = Integer.valueOf(txtAleatorios.getText());
            graphic = false;
            setPseudoaleatorios(seed, constant, numAleatorios);
            graphicData(seed, constant, numAleatorios);
            calculateData(numAleatorios);
        });

        mnuConfTableZ.setOnAction(event -> {
            tableZ = showInputDialog("Estadistico Z⍺/₂","Ingresa el valor de Z⍺/₂ de las tablas", "Este valor " +
                    "será usado para calcular \nel límite inferior y el limite superior de la prueba de medias", tableZ);
        });

        mnuConfTableChiLS.setOnAction( event -> {
            tableChiLS = showInputDialog("Estadistico X^2(⍺/₂)","Ingresa el valor de X^2(⍺/₂) de las tablas", "Este valor " +
                    "será usado para calcular \nel límite superior de la prueba de varianza", tableChiLS);
        });

        mnuConfTableChiLI.setOnAction(event -> {
            tableChiLI = showInputDialog("Estadistico X^2(1-⍺/₂)","Ingresa el valor de X^2(1-⍺/₂) de las tablas", "Este valor " +
                    "será usado para calcular \nel límite inferior de la prueba de varianza", tableChiLI);
        });

        txtSeed.setOnKeyTyped(keyTyppedEvent);
        txtConstant.setOnKeyTyped(keyTyppedEvent);
        txtAleatorios.setOnKeyTyped(keyTyppedEvent);
    }

    private void initTable() {
        TableColumn<PseudoDAO, Integer> colIndex = new TableColumn<>("Index");
        TableColumn<PseudoDAO, Double> colPseudo = new TableColumn<>("Pseudoaleatorio");

        colIndex.setCellValueFactory(new PropertyValueFactory<>("index"));
        colPseudo.setCellValueFactory(new PropertyValueFactory<>("aleatorio"));
        colPseudo.setPrefWidth(200);

        tblData.getColumns().addAll(colIndex, colPseudo);
        tblData.setItems(listPseudos);
    }

    private void setPseudoaleatorios(int seed, int constant, int numAleatorios) {
        try {
            aleatorios = pseudo.getAleatorios(seed, constant, numAleatorios);
            graphic = true;
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    private void graphicData(int seed, int constant, int numAleatorios) {
        if (aleatorios == null)
            return;

        chart.getData().clear();
        listPseudos.clear();

        XYChart.Series serie = new XYChart.Series<>();

        //Se mete el proceso de graficacion en un hilo porque es bastante pesado
        Task<Void> graphicTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (graphic)
                    for (int i = 0; i < aleatorios.length; i++) {
                        //Agregar los datos a la serie para la grafica
                        serie.getData().add(new XYChart.Data<>(aleatorios[i] + "", aleatorios[i]));

                        //Agregar los aleatorios al observableList para la tabla
                        listPseudos.add(new PseudoDAO(i + 1, aleatorios[i]));
                    }
                return null;
            }
        };

        graphicTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                chart.getData().add(serie);
            }
        });

        Thread t = new Thread(graphicTask);
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }

    private void calculateData(int numAleatorios) {
        if (aleatorios == null)
            return;

        //Hilo para calcular la media
        Task<Double> mediaTask = new Task<Double>() {
            @Override
            protected Double call() throws Exception {
                double media = pseudo.getMedia(aleatorios, numAleatorios);
                return media;
            }
        };

        //Hilo para calcular la varianza
        Task<Double> varTask = new Task<Double>() {
            @Override
            protected Double call() throws Exception {
                double var = pseudo.getVarianza(aleatorios, mediaTask.get(), numAleatorios);
                return var;
            }
        };

        //Hilo para calcular la CHI cuadrada
        Task<Double> squeareChiTask = new Task<Double>() {
            @Override
            protected Double call() throws Exception {
                double chi = pseudo.getSquareChi(aleatorios, numAleatorios);
                return chi;
            }
        };

        mediaTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                double media = 0;
                try {
                    media = mediaTask.get();
                    double LS = pseudo.getMediaLS(tableZ, numAleatorios);
                    double LI = pseudo.getMediaLI(tableZ, numAleatorios);
                    lblLSMedia.setText(formatter.format(LS));
                    lblLIMedia.setText(formatter.format(LI));
                    lblMedia.setText(formatter.format(media));
                    new Thread(varTask).start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });


        varTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                double var = 0;
                try {
                    var = varTask.get();
                    double LS = pseudo.getLSVar(tableChiLS, numAleatorios);
                    double LI = pseudo.getLIVar(tableChiLI, numAleatorios);
                    lblLSVar.setText(formatter.format(LS));
                    lblLIVar.setText(formatter.format(LI));
                    lblVar.setText(formatter.format(var));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });


        squeareChiTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                double chi = 0;
                try {
                    chi = squeareChiTask.get();
                    lblChi.setText(formatter.format(chi));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        //Calcular las corridas
        int corridas = pseudo.getCorridas(aleatorios, numAleatorios);
        double Z0 = pseudo.getZ0(corridas, numAleatorios);
        lblZ0.setText(formatter.format(Z0));
        lblCo.setText(corridas + "");


        new Thread(mediaTask).start();
        new Thread(squeareChiTask).start();
    }

    private EventHandler<KeyEvent> keyTyppedEvent = new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent event) {
            if (!Character.isDigit(event.getCharacter().charAt(0)))
                event.consume();
        }
    };

    private double showInputDialog(String title, String header, String content, double defValue){
        TextInputDialog input = new TextInputDialog(defValue+"");
        input.setTitle(title);
        input.setHeaderText(header);
        input.setContentText(content);

        Optional<String> result = input.showAndWait();
        if(result.isPresent())
            return Double.valueOf(result.get());

        return defValue;
    }
}
