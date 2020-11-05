package paint;

import java.util.Stack;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;


public class UndoRedoStack {
    
    Stack<WritableImage> undoStack;
    Stack<WritableImage> redoStack;
    WritableImage wim;
    
    public UndoRedoStack(){
        undoStack = new Stack<WritableImage>();
        redoStack = new Stack<WritableImage>();
    }
    
    /**
     * Save to Stack.
     * Takes a snapshot of the canvas. Saves the snapshot into the
     * undo and redo stacks.
     * 
     * @param canvas Used to snapshot current progress
     */
    public void saveToStack(Canvas canvas) {
        SnapshotParameters snap = new SnapshotParameters();
        wim = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        undoStack.push(canvas.snapshot(snap, wim));
    }

    /**Undo action.
     * Takes the snapshot of what the canvas looked like
     * before an edit and sets it as the new canvas.
     * 
     * @param canvas Addresses method saveToStack
     * @param graphCont Directly changes drawing on canvas
     */
    public void undo(Canvas canvas, GraphicsContext graphCont) {
        SnapshotParameters snap = new SnapshotParameters();
        if (undoStack.empty()) {
            System.out.println("No action to undo");
        } else {
            redoStack.push(canvas.snapshot(snap, wim));
            graphCont.drawImage((undoStack.pop()), 0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }

    /**Redo action.
     * Takes the snapshot of what the canvas just
     * became and sets it as the new canvas.
     * 
     * @param canvas Addresses method saveToStack
     * @param graphCont Directly changes drawing on canvas
     */
    public void redo(Canvas canvas, GraphicsContext graphCont) {
        if (redoStack.empty()) {
            System.out.println("No action to redo");
        } else {
            saveToStack(canvas);
            graphCont.drawImage((redoStack.pop()), 0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }
}
