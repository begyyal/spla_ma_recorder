package begyyal.splatoon;

import java.io.IOException;

import begyyal.splatoon.function.PropValidator;
import begyyal.splatoon.gui.StageOrganizer;
import begyyal.splatoon.processor.Recorder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Entrypoint extends Application {

    private Recorder rec;

    public static void main(String args[]) {
	if (PropValidator.exec())
	    launch(args);
    }

    @Override
    public void start(Stage stage) {
	try {
	    this.rec = Recorder.newi();
	    var dataBundle = rec.run();
	    StageOrganizer.newi(dataBundle).process(stage);
	} catch (Exception e) {
	    System.out.println("[ERROR] Error occured in JavaFX app thread.");
	    e.getMessage();
	    e.printStackTrace();
	    Platform.exit();
	}
    }

    @Override
    public void stop() {
	try {
	    this.rec.close();
	} catch (IOException e) {
	    System.out.println("[ERROR] Error occured in the closing process.");
	    e.getMessage();
	    e.printStackTrace();
	}
    }
}