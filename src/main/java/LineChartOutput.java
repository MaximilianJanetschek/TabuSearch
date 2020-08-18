/**
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
 
 
public class LineChartOutput extends Application {
 
    @Override public void start(Stage stage) {
        stage.setTitle("Tabu Solutions");
        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Iteration");
        //creating the chart
        final LineChart<Number,Number> lineChart = 
                new LineChart<Number,Number>(xAxis,yAxis);
                
        lineChart.setTitle("Development of Soltuion Value in Tabu Search");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("Overall Tabu");
        //getting the data for the series
        ArrayList<PastSolutionsClass> data = new ArrayList<>();
        data = Documentation.getSafedFeasibleSolutions();
      
        //populating the series with data
        for ( int index = 0; index < data.size(); index++) {	
        	series.getData().add(new XYChart.Data<Integer,Double>(data.get(index).iterationFound,data.get(index).tourProfit));
        }
        
        //define Additional series
        XYChart.Series series2 = new XYChart.Series();
        series2.setName("AMP 1");
        //populating the series with data
        series2.getData().add(new XYChart.Data<Integer, Integer>(1, 10));
        series2.getData().add(new XYChart.Data<Integer, Integer>(2,20));
        series2.getData().add(new XYChart.Data<Integer, Integer>(3, 30));
        series2.getData().add(new XYChart.Data<Integer, Integer>(4, 100));
        series2.getData().add(new XYChart.Data<Integer, Integer>(5, 110));
        series2.getData().add(new XYChart.Data<Integer, Integer>(6, 140));
        
        lineChart.getData().add(series);
        lineChart.getData().add(series2);
        Scene scene  = new Scene(lineChart,800,600);

        
        stage.setScene(scene);
        saveAsPng(scene, "LineChart");
        //stage.show();
        Platform.exit();
        //System.exit(0);

    }
    

    
    
    
    public void saveAsPng(Scene scene, String path) {
    	WritableImage image = scene.snapshot(null);
     
        // TODO: probably use a file chooser here
        File file = new File(path);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
            // TODO: handle exception here
        }
    }
    
    


    
    
}
 **/