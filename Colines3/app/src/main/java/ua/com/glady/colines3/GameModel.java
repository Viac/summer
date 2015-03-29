package ua.com.glady.colines3;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.Random;

import ua.com.glady.colines3.Tools.ValueAnimation;

/**
 * Created by Slava on 15.02.2015.
 */
public class GameModel {

    private INotifyEvent onScoreUpdated;

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

    ValueAnimation animation;

    INotifyEvent onAnimationBegin;

    INotifyEvent onAnimationEnd;

    public GameModel(){
        stack = new ArrayList<Integer>();
        item = new int[3];
        createNewItem();
        animateDrop = false;
        animation = new ValueAnimation();
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
        int indexToRemove = getIndexToRemove();
        while (indexToRemove != -1){
            // TODO: animation
            stack.remove(indexToRemove);
            stack.remove(indexToRemove - 1);
            score++;
            if (onScoreUpdated != null)
                onScoreUpdated.onEvent();
            indexToRemove = getIndexToRemove();
        }
    }

    public void addItemToStack(int x){
        if (fixedItem >= stack.size()) {
            stack.add(new Integer(item[2]));
            stack.add(new Integer(item[1]));
            stack.add(new Integer(item[0]));
        } else {
            stack.add(fixedItem, new Integer(item[2]));
            stack.add(fixedItem + 1, new Integer(item[1]));
            stack.add(fixedItem + 2, new Integer(item[0]));
        }
        cleanupStack();
    }


    public void drop(int x){
        animateDrop = true;

//        if (onAnimationBegin != null)
//            onAnimationBegin.onEvent();


        animation.animStartTime = System.currentTimeMillis();
        animation.beginValue = x;

        int stackLeft = canvasWidth - stack.size() * WIDTH;

        if (x < (stackLeft - item.length * WIDTH)){
            animation.endValue = canvasWidth - stack.size() * WIDTH - item.length * WIDTH;
        } else
        {
            int itemRightBound = x + item.length * WIDTH;
            // items that on the right of our current position
            fixedItem = (canvasWidth - itemRightBound) / WIDTH;
            animation.endValue = fixedItem * WIDTH - item.length * WIDTH;
        }


        animation.animDuration = (animation.endValue - animation.beginValue) / 2; // good speed
        animation.animFinishTime = System.currentTimeMillis() + animation.animDuration; // time to fall

        while ((System.currentTimeMillis() - animation.animStartTime) < animation.animDuration){
            // do nothing, animation painted

            // todo: security watcher!
        }


        addItemToStack(x);
        animateDrop = false;

//        if (onAnimationBegin != null)
//            onAnimationBegin.onEvent();


        createNewItem();
        this.x = 0;
    }

    public void paint(Canvas canvas){

        // clear canvas
        canvas.drawColor(Color.GRAY);

        // also possible
        // Canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        canvasWidth = canvas.getWidth();

        Paint paint = new Paint();

        int xPaint = x;

        // fix left bound
        if (xPaint < 0)
            xPaint = 0;
        // fix right bound
        if ((xPaint + WIDTH * 3) > canvasWidth)
            xPaint = canvasWidth - WIDTH * 3;

        int stackLeftBound = canvasWidth - stack.size() * WIDTH;

        int itemRightBound = xPaint + 3 * WIDTH;

        // items that on the right of our current position
        fixedItem = (canvasWidth - itemRightBound) / WIDTH;

        // todo: slow operation?
        // Log.d("glady", "  itemRightBound=" + itemRightBound + " FixedItem:" + fixedItem);

        if (fixedItem > stack.size())
            fixedItem = stack.size();

        // drawing items that on the right of our current position
        for (int i = 0; i < fixedItem; i++){
            paint.setColor(stack.get(i));
            canvas.drawRect(canvasWidth - (i + 1) * WIDTH, 80, canvasWidth - i * WIDTH, 1000, paint);
        }

        // drawing 'active' item

        if (this.animateDrop) {
            float animationX = animation.getCurrentValue(System.currentTimeMillis() - animation.animStartTime);
            for (int i = 0; i < item.length; i++){
                paint.setColor(item[i]);
                canvas.drawRect(animationX + i * WIDTH, 80, animationX + (i + 1) * WIDTH, 1000, paint);
            }
        }
        else {
            // normal drawing
            for (int i = 0; i < item.length; i++){
                paint.setColor(item[i]);
                canvas.drawRect(xPaint + i * WIDTH, 80, xPaint + (i + 1) * WIDTH, 1000, paint);
            }

        }


        // drawing items that on the left of our current position
        int itemsToDraw = stack.size() - fixedItem;
        if (itemsToDraw > 0){
            for (int i = fixedItem; i < stack.size(); i++){
                paint.setColor(stack.get(i));
                canvas.drawRect(xPaint - ((i - fixedItem) + 1) * WIDTH, 80, xPaint - (i - fixedItem) * WIDTH, 1000, paint);
            }
        }

    }

    public int getScore() {
        return score;
    }

    public void setOnScoreUpdated(INotifyEvent onScoreUpdated) {
        this.onScoreUpdated = onScoreUpdated;
    }
}