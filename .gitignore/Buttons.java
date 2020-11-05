package paint;

import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;


public class Buttons {
    
    String currentBtn;
    double posX, posY;
    
    public Buttons() {
        currentBtn = "no button";
    }
    
    /**
     * Display Button.
     * Prints a statement to the output describing button parameter.
     * The context of the statement is that the button is a tool used
     * to edit the canvas.
     *
     * @param btn The button that was last selected
     * @see A statement that says the name of the button
     */
    public void displayButton(Button btn) {
        currentBtn = btn.getText();
        System.out.println("Currently using " + currentBtn);
    }
    
    /**Nullify mouse events.
     * Cancels out mouse events on a canvas.
     * This is used to change what the mouse does after buttons are pressed.
     * 
     * @param canvas The object having its mouse events nullified
     */
    public void setMouseNull(Canvas canvas) {
        canvas.setOnMousePressed(null);
        canvas.setOnMouseDragged(null);
        canvas.setOnMouseReleased(null);
    }
}
