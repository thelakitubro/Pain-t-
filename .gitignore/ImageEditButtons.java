package paint;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;
import static paint.Paint.image;
import static paint.Paint.uRStack;


public class ImageEditButtons extends Buttons{
    
    Button moveImage, rotateImage;
    double rotateAngle;
    
    public ImageEditButtons() {
        moveImage = new Button("Move Image");
        rotateImage = new Button("Rotate Image");
        rotateAngle = 0;
    }
    
    /**
     * Set up buttons.
     * Sets up mouse events for all of the buttons in this class.
     * The setup is all grouped together in order to refer to the canvas and 
     * graphics context quicker.
     * 
     * @param canvas     The recipient of the mouse events of the buttons.
     * @param graphCont  Uses the initialized graphCont for consistency and line-saving.
     */
    public void setUpButtons(Canvas canvas, GraphicsContext graphCont) {
        //Drag image
        moveImage.setOnAction(e -> {
            displayButton(moveImage);
            setMouseNull(canvas);
            canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    posX = event.getX();
                    posY = event.getY();
                }
            });

            canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                }
            });

            canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    uRStack.saveToStack(canvas);
                    Paint.imgMinX = event.getX() - posY;
                    Paint.imgMinY = event.getY() - posY;
                    graphCont.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                    graphCont.drawImage(image, Paint.imgMinX, Paint.imgMinY, canvas.getWidth(), canvas.getHeight());
                }
            });
        });
        
        //Rotate image 90 degrees
        rotateImage.setOnAction(e -> {
            displayButton(rotateImage);
            uRStack.saveToStack(canvas);
            if(rotateAngle < 360) {
                rotateAngle += 90;
                
            } else {
                rotateAngle = 90;
            }
            Rotate rotate = new Rotate(rotateAngle, canvas.getWidth() / 2, canvas.getHeight() / 2);
            graphCont.save();
            graphCont.setTransform(rotate.getMxx(), rotate.getMyx(), rotate.getMxy(), rotate.getMyy(), rotate.getTx(), rotate.getTy());
            graphCont.drawImage(image, 0, 0, Paint.imageWidth, Paint.imageHeight);
            graphCont.restore();
        });
    }
    
    /**
     * Set up tool tips.
     * Sets up the tool tips for the buttons in this class.
     * 
     * @see Explanations for each button
     */
    public void setToolTips() {
        moveImage.setTooltip(new Tooltip("Shift image in any direction"));
        rotateImage.setTooltip(new Tooltip("Rotate the image 90 degrees clockwise"));
    }
}
