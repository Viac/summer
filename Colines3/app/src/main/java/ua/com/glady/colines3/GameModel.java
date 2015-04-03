package ua.com.glady.colines3;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.Random;

import ua.com.glady.colines3.Tools.AnimationTimer;
import ua.com.glady.colines3.Tools.GamePreferences;

/**
 * Implements game itself
 *
 * Created by Slava on 15.02.2015.
 */
public class GameModel {

    // This is a color of main board
    private static final int BACKGROUND_COLOR = Color.argb(255, 224, 228, 204);

    // There are colors of items
    private static final int STACK_COLOR1 = Color.argb(255, 241,  90,  90);
    private static final int STACK_COLOR2 = Color.argb(255, 240, 196,  25);
    private static final int STACK_COLOR3 = Color.argb(255,  78, 186, 111);
    private static final int STACK_COLOR4 = Color.argb(255,  45, 149, 191);
    private static final int STACK_COLOR5 = Color.argb(255, 149,  91, 165);

    private static final int STACK_COLOR6 = Color.GRAY;
    private static final int STACK_COLOR7 = Color.MAGENTA;
    private static final int STACK_COLOR8 = Color.BLUE;
    private static final int STACK_COLOR9 = Color.CYAN;
    private static final int STACK_COLOR10 = Color.YELLOW;

    // Need to control scores etc.
    private GamePreferences gamePreferences;

    private final int UNDEFINED = -1;

    // What is called when score value changed
    private INotifyEvent scoreUpdatedListener;

    // What is called when user loose
    private INotifyEvent gameOverListener;

    // Available colors.
    // Generally, this array is a basis for gameplay. I've tried several different modes
    // and the best result got in this mode - 5 colors, 3 bars in an item.
    private static final int[] COLORS = { STACK_COLOR1, STACK_COLOR2, STACK_COLOR3,
            STACK_COLOR4, STACK_COLOR5, STACK_COLOR6, STACK_COLOR7, STACK_COLOR8,
            STACK_COLOR9, STACK_COLOR10};

    // Current stack (all items that already dropped)
    private ArrayList<Integer> stack;

    // Current 'active' item (which user interact with)
    private int[] item;

    // this constant defines width of a single pile (which is a single column in "item")
    private int basicW;

    // this constant defines max colorsCount in game
    private int colorsCount;

    public void setX(int x) {
        this.x = x;
    }

    // position of the 'active' item
    private int x = 0;

    // declared as a field in order handle animation
    public int canvasWidth = 0;

    // number of items on the right from active item
    private int fixedItem = 0;

    // current score
    private int score = 0;

    // best score on this device
    private int bestScore = 0;

    // internal flag, used to switch draw mode
    private boolean animateDrop;

    // internal flag, used to switch draw mode
    private boolean animateCleanUp;

    // this flag stored flag "Was player win in this game already?"
    private boolean alreadyCongratulated;

    // index of item in stack to remove. Used to provide drawing
    private int removedIndex;

    // stored to improve performance
    private int canvasHeight;

    // sample calculator of anything for animation
    AnimationTimer animation;

    // used in animation, declared as object variable to increase performance
    Paint paint;

    /**
     * Creates game model
     * @param gamePreferences need to store scores and so on
     */
    public GameModel(GamePreferences gamePreferences){
        stack = new ArrayList<>();
        item = new int[3];
        animateDrop = false;
        animateCleanUp = false;
        alreadyCongratulated = false;
        animation = new AnimationTimer();
        removedIndex = UNDEFINED;
        paint = new Paint();
        this.gamePreferences = gamePreferences;
    }

    /**
     * Starts new game
     */
    public void reset() {
        stack.clear();

        score = 0;
        bestScore = gamePreferences.getBestSavedScore();
        alreadyCongratulated = false;

        if (scoreUpdatedListener != null)
            scoreUpdatedListener.onEvent();

        basicW = gamePreferences.getBasicWidth();
        colorsCount = gamePreferences.getColorsCount();

        createNewItem();
    }

    public void createNewItem(){

        Random r = new Random();

        int index = r.nextInt(colorsCount);

        item[0] = COLORS[index];

        boolean found = false;
        while (! found){
            index = r.nextInt(colorsCount);
            if (COLORS[index] != item[0])
                found = true;
        }
        item[1] = COLORS[index];

        found = false;
        while (! found){
            index = r.nextInt(colorsCount);
            if (COLORS[index] != item[1])
                found = true;
        }
        item[2] = COLORS[index];
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
            animation.setStartValue(2* basicW);
            animation.setFinishValue(0);
            animation.setDuration(70);

            waitAnimationFinish();

            animateCleanUp = false;

            stack.remove(removedIndex);
            stack.remove(removedIndex - 1);

            score++;
            if (score > bestScore)
                bestScore = score;
            if (scoreUpdatedListener != null)
                scoreUpdatedListener.onEvent();
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

        int stackLeft = canvasWidth - stack.size() * basicW;

        if (x < (stackLeft - item.length * basicW)){
            animation.setFinishValue(canvasWidth - stack.size() * basicW - item.length * basicW);
        } else
        {
            int itemRightBound = x + item.length * basicW;
            // items that on the right of our current position
            int fixedItem = (canvasWidth - itemRightBound) / basicW;
            animation.setFinishValue(fixedItem * basicW - item.length * basicW);
        }

        animation.setDuration((animation.getFinishValue() - animation.getStartValue()) / 2); // good speed

        waitAnimationFinish();

        animateDrop = false;

        addItemToStack();

        // Loose!
        if ((stack.size() * basicW) >= (canvasWidth - item.length * basicW)){
            if (gameOverListener != null){
                gameOverListener.onEvent();
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
        while (ticksAnimated < animation.getDuration());
    }

    public void paint(Canvas canvas){

        if (canvas == null)
            return; // could be happened since drawing made in separate thread

        canvas.drawColor(BACKGROUND_COLOR);

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
            if ((xPaint + basicW * item.length) > canvasWidth)
                xPaint = canvasWidth - basicW * item.length;

            int itemRightBound = xPaint + item.length * basicW;

            // items that on the right of our current position
            fixedItem = (canvasWidth - itemRightBound) / basicW;

            if (fixedItem > stack.size())
                fixedItem = stack.size();

            // drawing items that on the right of our current position
            for (int i = 0; i < fixedItem; i++){
                if (stack.size() > 0) {
                    paint.setColor(stack.get(i));
                    canvas.drawRect(canvasWidth - (i + 1) * basicW, 0, canvasWidth - i * basicW, canvasHeight, paint);
                }
            }

            // drawing 'active' item

            if (this.animateDrop) {
                float animationX = animation.getCurrentValue(System.currentTimeMillis() - animation.getStartTime());
                for (int i = 0; i < item.length; i++){
                    paint.setColor(item[i]);
                    canvas.drawRect(animationX + i * basicW, 0, animationX + (i + 1) * basicW, canvasHeight, paint);
                }
            }
            else {
                // normal drawing
                for (int i = 0; i < item.length; i++){
                    paint.setColor(item[i]);
                    canvas.drawRect(xPaint + i * basicW, 0, xPaint + (i + 1) * basicW, canvasHeight, paint);
                }

            }

            // drawing items that on the left of our current position
            int itemsToDraw = stack.size() - fixedItem;
            if (itemsToDraw > 0){
                for (int i = fixedItem; i < stack.size(); i++){
                    paint.setColor(stack.get(i));
                    canvas.drawRect(xPaint - ((i - fixedItem) + 1) * basicW, 0, xPaint - (i - fixedItem) * basicW, canvasHeight, paint);
                }
            }
        }

        paint.setColor(Color.BLACK);
        int xLimiter = item.length * basicW;
        canvas.drawLine(xLimiter, 0, xLimiter, canvasHeight, paint);
    }

    private void paintCleanUp(Canvas canvas) {
        int currentW;
        for (int i = 0; i < stack.size(); i++){

            currentW = (int) animation.getCurrentValue(System.currentTimeMillis() - animation.getStartTime());
            paint.setColor(stack.get(i));

            // Everything on the right from collapsed items
            if (i < (removedIndex - 1)){
                canvas.drawRect(canvasWidth - (i + 1) * basicW, 0, canvasWidth - i * basicW, canvasHeight, paint);
            }

            // first collapsed items
            if (i == removedIndex) {
                canvas.drawRect(canvasWidth - (removedIndex - 1) * basicW - currentW, 0, canvasWidth - (removedIndex - 1) * basicW, canvasHeight, paint);
            }

            // all the rest
            if (i > removedIndex){
                canvas.drawRect(canvasWidth - (i - 2) * basicW - currentW, 0, canvasWidth - (i-1) * basicW - currentW, canvasHeight, paint);
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
        if (bestScore > gamePreferences.getBestSavedScore())
            gamePreferences.setBestSavedScore(bestScore);
    }

    public void setScoreUpdatedListener(INotifyEvent scoreUpdatedListener) {
        this.scoreUpdatedListener = scoreUpdatedListener;
    }

    public void setGameOverListener(INotifyEvent gameOverListener) {
        this.gameOverListener = gameOverListener;
    }

    public boolean getAlreadyCongratulated() {
        return alreadyCongratulated;
    }

    public void setAlreadyCongratulated(boolean alreadyCongratulated) {
        this.alreadyCongratulated = alreadyCongratulated;
    }
}