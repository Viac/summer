package ua.com.glady.colines3;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.Random;

import ua.com.glady.colines3.Tools.AnimationTimer;

/**
 * Implements game itself
 *
 * Created by Slava on 15.02.2015.
 */
public class GameModel {

    private final int UNDEFINED = -1;

    private INotifyEvent onScoreUpdated;

    private INotifyEvent onGameOver;

    private int[] colors = { Color.YELLOW, Color.BLUE, Color.GREEN };

    private ArrayList<Integer> stack;

    private int[] item;

    private final int WIDTH = 20;

    public void setX(int x) {
        this.x = x;
    }

    private int x = 0;

    public int canvasWidth = 0;

    private int fixedItem = 0;

    private int score = 0;

    private boolean animateDrop;

    private boolean animateCleanUp;

    private int removedIndex;

    private int canvasHeight; // used to improve performance

    AnimationTimer animation;

    Paint paint; // used in animation, declared as object variable to increase performance

    public GameModel(){
        stack = new ArrayList<>();
        item = new int[3];
        createNewItem();
        animateDrop = false;
        animateCleanUp = false;
        animation = new AnimationTimer();
        removedIndex = UNDEFINED;
        paint = new Paint();
    }

    public void reset(){
        stack.clear();
        score = 0;
        createNewItem();
    }

    public void createNewItem(){

        Random r = new Random();

        int index = r.nextInt(colors.length);

        item[0] = colors[index];

        boolean found = false;
        while (! found){
            index = r.nextInt(colors.length);
            if (colors[index] != item[0])
                found = true;
        }
        item[1] = colors[index];

        found = false;
        while (! found){
            index = r.nextInt(colors.length);
            if (colors[index] != item[1])
                found = true;
        }
        item[2] = colors[index];

    }

    private int getIndexToRemove(){
        for (int i = 0; i < stack.size(); i++){
            if ((i > 0) && (stack.get(i).equals(stack.get(i - 1))))
                return i;
        }
        return -1;
    }

    public void cleanupStack(){
        removedIndex = getIndexToRemove();
        if (removedIndex == UNDEFINED)
            return;
        while (removedIndex != UNDEFINED){
            animateCleanUp = true;

            animation.setStartTime(System.currentTimeMillis());
            animation.setStartValue(2*WIDTH);
            animation.setFinishValue(0);
            animation.setDuration(70);

            waitAnimationFinish();

            animateCleanUp = false;

            stack.remove(removedIndex);
            stack.remove(removedIndex - 1);

            score++;
            if (onScoreUpdated != null)
                onScoreUpdated.onEvent();
            removedIndex = getIndexToRemove();
        }
    }

    public void addItemToStack(){
        if (fixedItem >= stack.size()) {
            stack.add(item[2]);
            stack.add(item[1]);
            stack.add(item[0]);
        } else {
            stack.add(fixedItem, item[2]);
            stack.add(fixedItem + 1, item[1]);
            stack.add(fixedItem + 2, item[0]);
        }
        cleanupStack();
    }


    public void drop(int x){
        animateDrop = true;

        animation.setStartTime(System.currentTimeMillis());
        animation.setStartValue(x);

        int stackLeft = canvasWidth - stack.size() * WIDTH;

        if (x < (stackLeft - item.length * WIDTH)){
            animation.setFinishValue(canvasWidth - stack.size() * WIDTH - item.length * WIDTH);
        } else
        {
            int itemRightBound = x + item.length * WIDTH;
            // items that on the right of our current position
            int fixedItem = (canvasWidth - itemRightBound) / WIDTH;
            animation.setFinishValue(fixedItem * WIDTH - item.length * WIDTH);
        }

        animation.setDuration((animation.getFinishValue() - animation.getStartValue()) / 2); // good speed

        waitAnimationFinish();

        animateDrop = false;

        addItemToStack();

        // Loose!
        if ((stack.size() * WIDTH) >= (canvasWidth - item.length * WIDTH)){
            if (onGameOver != null){
                onGameOver.onEvent();
            }
        }

        createNewItem();
        this.x = 0;
    }

    private void waitAnimationFinish() {
        long ticksAnimated;
        do
            ticksAnimated = System.currentTimeMillis() - animation.getStartTime();
            // do nothing, animation painted
            // todo: security watcher!
        while (ticksAnimated < animation.getDuration());
    }

    public void paint(Canvas canvas){

        if (canvas == null)
            return; // could be happened since drawing made in separate thread

        // clear canvas
        canvas.drawColor(Color.GRAY);

        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();


        if (this.animateCleanUp){
            paintCleanUp(canvas);
        }
        else {
            int xPaint = x;

            // fix left bound
            if (xPaint < 0)
                xPaint = 0;
            // fix right bound
            if ((xPaint + WIDTH * 3) > canvasWidth)
                xPaint = canvasWidth - WIDTH * 3;

            // int stackLeftBound = canvasWidth - stack.size() * WIDTH;

            int itemRightBound = xPaint + 3 * WIDTH;

            // items that on the right of our current position
            fixedItem = (canvasWidth - itemRightBound) / WIDTH;

            if (fixedItem > stack.size())
                fixedItem = stack.size(); // todo: -1?

            // drawing items that on the right of our current position
            for (int i = 0; i < fixedItem; i++){
                if (stack.size() > 0) {
                    paint.setColor(stack.get(i));
                    canvas.drawRect(canvasWidth - (i + 1) * WIDTH, 0, canvasWidth - i * WIDTH, canvasHeight, paint);
                }
            }

            // drawing 'active' item

            if (this.animateDrop) {
                float animationX = animation.getCurrentValue(System.currentTimeMillis() - animation.getStartTime());
                for (int i = 0; i < item.length; i++){
                    paint.setColor(item[i]);
                    canvas.drawRect(animationX + i * WIDTH, 0, animationX + (i + 1) * WIDTH, canvasHeight, paint);
                }
            }
            else {
                // normal drawing
                for (int i = 0; i < item.length; i++){
                    paint.setColor(item[i]);
                    canvas.drawRect(xPaint + i * WIDTH, 0, xPaint + (i + 1) * WIDTH, canvasHeight, paint);
                }

            }

            // drawing items that on the left of our current position
            int itemsToDraw = stack.size() - fixedItem;
            if (itemsToDraw > 0){
                for (int i = fixedItem; i < stack.size(); i++){
                    paint.setColor(stack.get(i));
                    canvas.drawRect(xPaint - ((i - fixedItem) + 1) * WIDTH, 0, xPaint - (i - fixedItem) * WIDTH, canvasHeight, paint);
                }
            }
        }

    }

    private void paintCleanUp(Canvas canvas) {
        int currentW;
        for (int i = 0; i < stack.size(); i++){

            currentW = (int) animation.getCurrentValue(System.currentTimeMillis() - animation.getStartTime());
            paint.setColor(stack.get(i));

            // Everything on the right from collapsed items
            if (i < (removedIndex - 1)){
                canvas.drawRect(canvasWidth - (i + 1) * WIDTH, 0, canvasWidth - i * WIDTH, canvasHeight, paint);
            }

            // first collapsed items
            if (i == removedIndex) {
                canvas.drawRect(canvasWidth - (removedIndex - 1) * WIDTH - currentW, 0, canvasWidth - (removedIndex - 1) * WIDTH, canvasHeight, paint);
            }

            // all the rest
            if (i > removedIndex){
                canvas.drawRect(canvasWidth - (i - 2) * WIDTH - currentW, 0, canvasWidth - (i-1) * WIDTH - currentW, canvasHeight, paint);
            }

        }
    }

    public int getScore() {
        return score;
    }

    public void setOnScoreUpdated(INotifyEvent onScoreUpdated) {
        this.onScoreUpdated = onScoreUpdated;
    }

    public void setOnGameOver(INotifyEvent onGameOver) {
        this.onGameOver = onGameOver;
    }
}