package ua.com.glady.colines3;

import android.content.SharedPreferences;
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

    private static final String BEST_SCORE_PREFERENCES_KEY = "GameBestScore";

    private SharedPreferences sPreferences;

    private final int UNDEFINED = -1;

    private INotifyEvent onScoreUpdated;

    private INotifyEvent onGameOver;

    private int[] colors = {
            Color.argb(255, 228,  68,  36),
            Color.argb(255, 103, 188, 219),
            Color.argb(255, 162, 171,  88),
            Color.argb(255, 255, 255, 255)
    };

    private ArrayList<Integer> stack;

    private int[] item;

    private int basicWidth;

    public void setX(int x) {
        this.x = x;
    }

    private int x = 0;

    public int canvasWidth = 0;

    private int fixedItem = 0;

    private int score = 0;

    private int bestScore = 0;

    private boolean animateDrop;

    private boolean animateCleanUp;

    private int removedIndex;

    private int canvasHeight; // used to improve performance

    AnimationTimer animation;

    Paint paint; // used in animation, declared as object variable to increase performance

    public GameModel(SharedPreferences sPreferences){
        stack = new ArrayList<>();
        item = new int[3];
        createNewItem();
        animateDrop = false;
        animateCleanUp = false;
        animation = new AnimationTimer();
        removedIndex = UNDEFINED;
        paint = new Paint();
        this.sPreferences = sPreferences;
    }

    public void reset() {
        stack.clear();

        score = 0;
        bestScore = sPreferences.getInt(BEST_SCORE_PREFERENCES_KEY, 0);

        if (onScoreUpdated != null)
            onScoreUpdated.onEvent();

        // todo: some another for landscape
        basicWidth = 20;

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
            animation.setStartValue(2* basicWidth);
            animation.setFinishValue(0);
            animation.setDuration(70);

            waitAnimationFinish();

            animateCleanUp = false;

            stack.remove(removedIndex);
            stack.remove(removedIndex - 1);

            score++;
            if (score > bestScore)
                bestScore = score;
            if (onScoreUpdated != null)
                onScoreUpdated.onEvent();
            removedIndex = getIndexToRemove();
        }
    }

    public void addItemToStack() {
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

        int stackLeft = canvasWidth - stack.size() * basicWidth;

        if (x < (stackLeft - item.length * basicWidth)){
            animation.setFinishValue(canvasWidth - stack.size() * basicWidth - item.length * basicWidth);
        } else
        {
            int itemRightBound = x + item.length * basicWidth;
            // items that on the right of our current position
            int fixedItem = (canvasWidth - itemRightBound) / basicWidth;
            animation.setFinishValue(fixedItem * basicWidth - item.length * basicWidth);
        }

        animation.setDuration((animation.getFinishValue() - animation.getStartValue()) / 2); // good speed

        waitAnimationFinish();

        animateDrop = false;

        addItemToStack();

        // Loose!
        if ((stack.size() * basicWidth) >= (canvasWidth - item.length * basicWidth)){
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
//        canvas.drawColor(Color.argb(255, 204, 204, 204));

        canvas.drawColor(Color.argb(255, 224,228,204));



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
            if ((xPaint + basicWidth * item.length) > canvasWidth)
                xPaint = canvasWidth - basicWidth * item.length;

            // int stackLeftBound = canvasWidth - stack.size() * basicWidth;

            int itemRightBound = xPaint + item.length * basicWidth;

            // items that on the right of our current position
            fixedItem = (canvasWidth - itemRightBound) / basicWidth;

            if (fixedItem > stack.size())
                fixedItem = stack.size(); // todo: -1?

            // drawing items that on the right of our current position
            for (int i = 0; i < fixedItem; i++){
                if (stack.size() > 0) {
                    paint.setColor(stack.get(i));
                    canvas.drawRect(canvasWidth - (i + 1) * basicWidth, 0, canvasWidth - i * basicWidth, canvasHeight, paint);
                }
            }

            // drawing 'active' item

            if (this.animateDrop) {
                float animationX = animation.getCurrentValue(System.currentTimeMillis() - animation.getStartTime());
                for (int i = 0; i < item.length; i++){
                    paint.setColor(item[i]);
                    canvas.drawRect(animationX + i * basicWidth, 0, animationX + (i + 1) * basicWidth, canvasHeight, paint);
                }
            }
            else {
                // normal drawing
                for (int i = 0; i < item.length; i++){
                    paint.setColor(item[i]);
                    canvas.drawRect(xPaint + i * basicWidth, 0, xPaint + (i + 1) * basicWidth, canvasHeight, paint);
                }

            }

            // drawing items that on the left of our current position
            int itemsToDraw = stack.size() - fixedItem;
            if (itemsToDraw > 0){
                for (int i = fixedItem; i < stack.size(); i++){
                    paint.setColor(stack.get(i));
                    canvas.drawRect(xPaint - ((i - fixedItem) + 1) * basicWidth, 0, xPaint - (i - fixedItem) * basicWidth, canvasHeight, paint);
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
                canvas.drawRect(canvasWidth - (i + 1) * basicWidth, 0, canvasWidth - i * basicWidth, canvasHeight, paint);
            }

            // first collapsed items
            if (i == removedIndex) {
                canvas.drawRect(canvasWidth - (removedIndex - 1) * basicWidth - currentW, 0, canvasWidth - (removedIndex - 1) * basicWidth, canvasHeight, paint);
            }

            // all the rest
            if (i > removedIndex){
                canvas.drawRect(canvasWidth - (i - 2) * basicWidth - currentW, 0, canvasWidth - (i-1) * basicWidth - currentW, canvasHeight, paint);
            }

        }
    }

    public int getScore() {
        return score;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void saveBestScore() {
        SharedPreferences.Editor ed = sPreferences.edit();
        ed.putInt(BEST_SCORE_PREFERENCES_KEY, bestScore);
        ed.commit();
    }


    public void setOnScoreUpdated(INotifyEvent onScoreUpdated) {
        this.onScoreUpdated = onScoreUpdated;
    }

    public void setOnGameOver(INotifyEvent onGameOver) {
        this.onGameOver = onGameOver;
    }
}