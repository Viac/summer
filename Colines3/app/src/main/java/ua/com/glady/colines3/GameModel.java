package ua.com.glady.colines3;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Slava on 15.02.2015.
 */
public class GameModel {

    private INotifyEvent onScoreUpdated;

    private int[] colors = { Color.YELLOW, Color.BLUE, Color.GREEN };

    private ArrayList<Integer> stack;

    private int[] item;

    private final int WIDTH = 30;

    public void setX(int x) {
        this.x = x;
    }

    private int x = 0;

    public int canvasWidth = 0;

    private int fixedItem = 0;

    private int score = 1;

    public GameModel(){
        stack = new ArrayList<Integer>();
        item = new int[3];
        createNewItem();
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
        addItemToStack(x);
        createNewItem();
        this.x = 0;
    }

    public void paint(Canvas canvas){

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

        fixedItem = (canvasWidth - itemRightBound) / WIDTH;
        Log.d("glady", "  itemRightBound=" + itemRightBound + " FixedItem:" + fixedItem);

        if (fixedItem > stack.size())
            fixedItem = stack.size();

        for (int i = 0; i < fixedItem; i++){
            paint.setColor(stack.get(i));
            canvas.drawRect(canvasWidth - (i + 1) * WIDTH, 80, canvasWidth - i * WIDTH, 1000, paint);
        }

        for (int i = 0; i < item.length; i++){
            paint.setColor(item[i]);
            canvas.drawRect(xPaint + i * WIDTH, 80, xPaint + (i + 1) * WIDTH, 1000, paint);
        }

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