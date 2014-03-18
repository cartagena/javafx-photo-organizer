package com.github.cartagena.organizer;

import static com.github.cartagena.organizer.MessagesUtil.end;
import static com.github.cartagena.organizer.MessagesUtil.error;
import static com.github.cartagena.organizer.MessagesUtil.starting;
import static com.github.cartagena.organizer.MessagesUtil.success;
import static com.github.cartagena.organizer.PicturesOrganizer.copyPictures;
import static com.github.cartagena.organizer.PicturesOrganizer.movePictures;
import static java.lang.String.format;
import static javafx.geometry.Pos.TOP_LEFT;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressBarBuilder;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ProgressIndicatorBuilder;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextAreaBuilder;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.GridPaneBuilder;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import com.github.cartagena.organizer.PicturesOrganizer.PicturesOrganizerListener;

public class PicturesOrganizerApplication extends Application {
    

	@Override
    public void start(final Stage primaryStage) {
    	
        final TextField sourceField = TextFieldBuilder.create()
    		.minWidth(700)
    		.build();

        final Button sourceChooser = ButtonBuilder.create()
			.text("...")
			.onAction(new EventHandler<ActionEvent>() {
	            @Override
	            public void handle(ActionEvent event) {
	                DirectoryChooser directoryChooser = new DirectoryChooser();
	                File selectedDirectory = directoryChooser.showDialog(primaryStage);
	                 
	                if(selectedDirectory != null) {
	                	sourceField.setText(selectedDirectory.getAbsolutePath());
	                }
	            }
	        })
	        .build();
        
        final TextField destinationField = new TextField();
        
        Button destinationChooser = ButtonBuilder.create()
			.text("...")
			.onAction(new EventHandler<ActionEvent>() {
	            @Override
	            public void handle(ActionEvent event) {
	                DirectoryChooser directoryChooser = new DirectoryChooser();
	                File selectedDirectory = directoryChooser.showDialog(primaryStage);
	                 
	                if(selectedDirectory != null) {
	                	destinationField.setText(selectedDirectory.getAbsolutePath());
	                }
	            }
	        })
	        .build();

        
        final CheckBox moveCheckbox = new CheckBox("Move pictures (will erease files from source directory)");
        
        // user feedback
        final ProgressBar progressBar = ProgressBarBuilder.create()
        	.progress(0.0)
        	.minWidth(700)
        	.visible(false)
        	.build();
        		
        final ProgressIndicator progressIndicator = ProgressIndicatorBuilder.create()
        	.progress(0.0)
        	.visible(false)
        	.build();
        		
        final TextArea progressTextArea = TextAreaBuilder.create()
        	.minWidth(700)
        	.minHeight(300)
        	.visible(false)
        	.build();
		
		// action button
		final Button processButton = ButtonBuilder.create()
			.defaultButton(true)
			.text("Init process!")
			.onAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(final ActionEvent event) {
					String sourcePath = sourceField.getText();
					String destinationPath = destinationField.getText();
					
					if(isEmpty(sourcePath) || isEmpty(destinationPath)) {
						progressTextArea.setVisible(true);
						progressTextArea.setText("You should fill the source and destination fields!");
						return;
					}
					
					((Button) event.getTarget()).setDisable(true);

					progressBar.setVisible(true);
					progressIndicator.setVisible(true);
					progressTextArea.setVisible(true);

					progressBar.progressProperty().unbind();
					progressIndicator.progressProperty().unbind();
					
					progressBar.setProgress(0);
	                progressIndicator.setProgress(0);
	                progressTextArea.setText("");
	                
	                Task<Void> worker = createWorker(sourcePath, destinationPath, moveCheckbox.isSelected());

	                progressBar.progressProperty().bind(worker.progressProperty());
	                progressIndicator.progressProperty().bind(worker.progressProperty());

	                worker.messageProperty().addListener(new ChangeListener<String>() {

						@Override
						public void changed(ObservableValue<? extends String> value, String oldValue, String newValue) {
							progressTextArea.appendText(value.getValue() + "\n");
						}
					});
	                
	                new Thread(worker).start();				
				}
			})
			.build();
		
		final Button newButton = ButtonBuilder.create()
			.cancelButton(true)
			.text("New process")
			.onAction(new EventHandler<ActionEvent>() {
	
				@Override
				public void handle(final ActionEvent event) {
					progressBar.setVisible(false);
					progressIndicator.setVisible(false);
					progressTextArea.setVisible(false);
	
					progressBar.progressProperty().unbind();
					progressIndicator.progressProperty().unbind();
					
					progressBar.setProgress(0.0);
					progressIndicator.setProgress(0.0);
	                progressTextArea.setText("");
	                
	                sourceField.setText("");
	                destinationField.setText("");
	                moveCheckbox.setSelected(false);
	                
	                processButton.setDisable(false);
				}
			})
			.build();
		
        
		GridPane butonGrid = GridPaneBuilder.create()
			.alignment(TOP_LEFT)
			.hgap(10)
			.vgap(10)
			.build(); 
		
		butonGrid.add(newButton, 0, 0);
		butonGrid.add(processButton, 1, 0);

		GridPane grid = GridPaneBuilder.create()
			.alignment(TOP_LEFT)
			.hgap(10)
			.vgap(10)
			.padding(new Insets(25, 25, 25, 25))
			.build();

		 
		primaryStage.setTitle("Pictures Organizer");
        primaryStage.setScene(SceneBuilder.create()
        		.root(grid)
        		.width(800)
        		.height(600)
        		.build());
		
        grid.add(new Label("Source directory:"), 0, 0);
        
        grid.add(sourceField, 0, 1);
        grid.add(sourceChooser, 1, 1);

        grid.add(new Label("Destination directory:"), 0, 2);
        
        grid.add(destinationField, 0, 3);
        grid.add(destinationChooser, 1, 3);
        
        grid.add(moveCheckbox, 0, 4);
		
		grid.add(butonGrid, 0, 6, 1, 2);

		grid.add(progressBar, 0, 7);
		grid.add(progressIndicator, 1, 7);
		
		grid.add(progressTextArea, 0, 8, 2, 1);
		
        
        primaryStage.show();
    }
    
    public Task<Void> createWorker(final String sourcePath, final String destinationPath, final boolean move) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
            	final PicturesOrganizerListener listener = new PicturesOrganizerListener() {
					
					@Override
					public void onProcess(int current, int total, String newPath, String originalPath) {
						updateProgress(current, total);
						updateMessage(format(success(move), originalPath, newPath));
					}
					
					@Override
					public void onProcessError(int current, int total, String originalPath) {
						updateProgress(current, total);
						updateMessage(format(error(move), originalPath));
					}					

					@Override
					public void onStart(int total) {
						updateMessage(format(starting(move), total));						
					}

					@Override
					public void onEnd(int total, int processed) {
						updateMessage(format(end(move), processed, total));
					}
				};
				
				if(move) {
					movePictures(sourcePath, destinationPath,  listener);
				} else {
					copyPictures(sourcePath, destinationPath,  listener);
				}
            	
                return null;
            }
        };
    }    
    
    public static void main(String[] args) {
        launch(args);
    }
    
}