package client;

	// You may add files to the windowing package, but you must leave all files
	// that are already present unchanged, except for:
	// 		Main.java (this file)
	//		drawable/Drawables.java

	// Also, do not instantiate Image361 yourself.

import javafx.stage.*;
import windowing.Window361;
import windowing.drawable.Drawable;
import javafx.application.Application;

public class Main extends Application {
	private static String filename;
	public static void main(String[] args) {
		if(args.length != 0) {
			filename = args[0];  
		}
		launch(args);
	}
	@Override
	public void start(Stage primaryStage) {
		Window361 window = new Window361(primaryStage);
		Drawable drawable = window.getDrawable();
		
		Client client = new Client(drawable, filename);
		window.setPageTurner(client);
		client.nextPage();
		
		primaryStage.show();
	}

}
