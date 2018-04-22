package NoiseGenerator;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.*;

public class NoiseGenerator implements Initializable{
    @FXML private Canvas canvas;
    @FXML private Slider slider;

    Timer timer=new Timer();

    int n=100;
    double[] a, b, c, d;
    double x0=0, x1=6.28;
    double amp = 0.1;
    double y0, y1;
    double mse, psnr, avarage;
    double speed=0.1;

    GraphicsContext graphicsContext;
    Random rnd=new Random();

    double lerp(double a, double b, double t){
        return a+(b-a)*t;
    }

    double map(double x, double x0, double x1, double a, double b) {
        double t = (x - x0) / (x1 - x0);
        return lerp(a, b, t);
    }

    double min(double[] array) {
        double min = Double.MAX_VALUE;
        for(int i = 0; i < array.length; i++)
            if (array[i] < min) min = array[i];
        return min;
    }

    double max(double[] array) {
        double max = 0;
        for (int i = 0; i < array.length; i++)
            if (array[i] > max) max = array[i];
        return max;
    }

    void graph(double[] arr, double ymin, double ymax, double l, double t, double w, double h, Color color) {
        graphicsContext.setStroke(color);
        double ox = 0;
        double oy = 0;
        for(int i=0; i < n; i++) {
            double x = map(i, 0, n, l, l + w);
            double y = map(arr[i], ymin, ymax, t, t + h);
            if (i != 0) graphicsContext.strokeLine((float)ox, (float)oy, (float)x, (float)y);
            ox = x;
            oy = y;
        }
    }

    void paint(){
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        graph(a, -1, 1, 50, 50, 300, 75, Color.WHITE);
        graph(b, y0, y1, 50, 150, 300, 75, Color.GREEN);
        graph(c, y0, y1, 50, 250, 300, 75, Color.RED);
        graph(d, y0, y1, 50, 350, 300, 75, Color.BLUE);
    }

    void rechtPSNR(double[] t){
        double max=0;
        for(int i=0; i<n;i++)
            if(t[i]>max) max=t[i];
        psnr=20*Math.log10(max/Math.sqrt(mse));
    }

    void rechtAvarage(double[] t){
        avarage=0;
        for(int i=0; i<n;i++)
            avarage+=t[i];
        avarage/=n;
    }

    void rechtMSE(double[] t){
        mse=0;
        for(int i=0; i<n;i++)
            mse+=Math.pow(t[i]-avarage,2);
        mse=Math.sqrt(mse/n);
    }

    public void timer(){
        x0+=speed; x1+=speed;
        for (int i = 0; i < n; i++) {
            double x = map(i, 0, n - 1, x0, x1);
            a[i] = Math.sin(x);
            b[i] = a[i] + map(rnd.nextDouble(), 0, 1, -amp, +amp);
        }

        { //Устредненный фильтр
            c[0] = b[0];
            for (int i = 1; i < n - 1; i++) {
                c[i] = (b[i + 1] + b[i] + b[i - 1]) / 3;
            }
            c[n-1]=b[n-1];
        }

        { //Медианный фильтр
            int c=3;
            ArrayList<Double> t=new ArrayList<>();
            for(int i=0;i<c;i++) t.add(0.0);
            d[0] = b[0];
            for(int i=1; i<n-1; i++){
                t.set(0, b[i + 1]);
                t.set(1, b[i]);
                t.set(2, b[i - 1]);
                Collections.sort(t);
                d[i]=t.get(1);
            }
            d[n-1]=b[n-1];
        }

        double y0a = min(a);
        double y1a = max(a);
        double y0b = min(b);
        double y1b = max(b);

        y0 = Math.min(y0a, y0b);
        y1 = Math.max(y1a, y1b);

        paint();
        
        {
            rechtAvarage(a);
            rechtMSE(a);
            rechtPSNR(a);
            graphicsContext.setFill(Color.WHITE);
            graphicsContext.fillText(String.format("%.4f", mse), 405, 88);
            graphicsContext.fillText(String.format("%.4f", psnr), 410, 117);
            graphicsContext.fillText(String.format("%.4f", avarage), 420, 140);
        }
        {
            rechtAvarage(a);
            rechtMSE(b);
            rechtPSNR(b);
            graphicsContext.fillText(String.format("%.4f", mse), 405, 195);
            graphicsContext.fillText(String.format("%.4f", psnr), 410, 223);
            graphicsContext.fillText(String.format("%.4f", avarage), 420, 247);
        }
        {
            rechtAvarage(a);
            rechtMSE(c);
            rechtPSNR(c);
            graphicsContext.fillText(String.format("%.4f", mse), 405, 290);
            graphicsContext.fillText(String.format("%.4f", psnr), 410, 320);
            graphicsContext.fillText(String.format("%.4f", avarage), 420, 345);
        }
        {
            rechtAvarage(a);
            rechtMSE(d);
            rechtPSNR(d);
            graphicsContext.fillText(String.format("%.4f", mse), 405, 400);
            graphicsContext.fillText(String.format("%.4f", psnr), 410, 428);
            graphicsContext.fillText(String.format("%.4f", avarage), 420, 457);
        }
    }

    TimerTask timerTask=new TimerTask() {
        @Override
        public void run() {
            timer();
        }
    };

    @Override
    public void initialize(URL location, ResourceBundle resources){
        graphicsContext=canvas.getGraphicsContext2D();
        a = new double[n]; b = new double[n];
        c = new double[n]; d = new double[n];
        timer.schedule(timerTask, 0, 100);

        slider.valueProperty().addListener((observable, oldValue, newValue) -> speed=slider.getValue());
    }

    public void formClosed() {
        timer.cancel();
    }
}