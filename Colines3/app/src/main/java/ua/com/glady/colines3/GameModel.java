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

    private static final int ANIMATION_COLLAPSE_DURATION = 70;

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

    // Each new item consists of this number of piles.
    private static final int PILES_IN_ITEM = 3;

    public static final int WIN_SCORE = 1001;

    // Need to control scores etc.
    private final GamePreferences gamePreferences;

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
    private final ArrayList<Integer> stack;

    // Current 'active' item (which user interact with)
    private int[] item;

    // this constant defines width of a single pile (which is a single column in "item")
    private int basicW;

    // count of the piles in full loaded stack
    private int stackSize;

    // this constant defines max colorsCount in game
    private int colorsCount;

    public void setX(int x) {
        this.x = x;
    }

    // position of the 'active' item
    private int x = 0;

    // declared as a field in order handle animation
    private int canvasWidth = 0;

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
    private final AnimationTimer animation;

    // used in animation, declared as object variable to increase performance
    private final Paint paint;

    /**
     * Creates game model
     * @param gamePreferences need to store scores and so on
     */
    public GameModel(GamePreferences gamePreferences){
        stack = new ArrayList<>();
        item = new int[PILES_IN_ITEM];
        animateDrop = false;
        animateCleanUp = false;
        alreadyCongratulated = false;
        animation = new AnimationTimer();
        removedIndex = UNDEFINED;
        paint = new Paint();
        this.gamePreferences = gamePreferences;
        stackSize = gamePreferences.getStackSize();
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

        // Actually this fields made as variables to get ability to test gameplay in very different
        // modes.
        colorsCount = gamePreferences.getColorsCount();

        createNewItem();
    }

    /**
     * Replaced Item colors with new set
     */
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

    /**
     * Returns first index of item that could be collapsed. I.e. next pile has same color
     * @return index in stack
     */
    private int getIndexToRemove(){
        for (int i = 0; i < stack.size(); i++){
            if ((i > 0) && (stack.get(i).equals(stack.get(i - 1))))
                return i;
        }
        return -1;
    }

    /**
     * Performs collapse of the neighbor piles with equal colors, until no such piles
     * would be present in the stack.
     *
     * Ex: Before (R - red, G- green, B - blue)
     *
     * RGGBRBGRBBRB -> RBRBGB
     */
    public void cleanupStack(){
        removedIndex = getIndexToRemove();

        // No more neighbor with same color
        if (removedIndex == UNDEFINED)
            return;

        // Still need to continue
        while (removedIndex != UNDEFINED){
            animateCleanUp = true;

            // Here we set animation of dropped item flag. So the next small time we only
            // watch how pies are collapsed
            animation.setStartTime(System.currentTimeMillis());
            animation.setStartValue(2* basicW);
            animation.setFinishValue(0);
            animation.setDuration(ANIMATION_COLLAPSE_DURATION);

            waitAnimationFinish();

            animateCleanUp = false;

            // 2 items removed in fact
            stack.remove(removedIndex);
            stack.remove(removedIndex - 1);

            // and the score is updated
            score++;
            if (score > bestScore)
                bestScore = score;
            if (scoreUpdatedListener != null)
                scoreUpdatedListener.onEvent();
            removedIndex = getIndexToRemove();
        }
    }

    /**
     * After active item was dropped (touch released), stack is recalculated
     */
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

    /**
     * Performs action 'used drops the active item'
     * @param x where active item was located when touch released
     */
    public void drop(int x){

        // Here we starts animation of dropping. So we can only see how the item falls to the right
        showDropAnimation(x);

        addItemToStack();

        // Here we catch event "user loosed the game"
        if (stack.size() >= stackSize){
            if (gameOverListener != null){
                gameOverListener.onEvent();
            }
        }

        // and finally creating new item
        createNewItem();
        this.x = 0;
    }

    /**
     * Shows animation of dropped item from position x to proper place on the screen
     * If item dropped outside of stack than this position is a stack top
     * Otherwise item places inside the stack
     * @param x start position of drop
     */
    private void showDropAnimation(int x) {
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

        // Value 2 here sets fall velocity to more or less nice value
        animation.setDuration((animation.getFinishValue() - animation.getStartValue()) / 2);

        waitAnimationFinish();

        animateDrop = false;
    }

    /**
     * Does nothing until animation timer responds that animation done
     */
    private void waitAnimationFinish() {
        long ticksAnimated;
        do
            ticksAnimated = System.currentTimeMillis() - animation.getStartTime();
            // do nothing, animation painted
        while (ticksAnimated < animation.getDuration());
    }

    /**
     * Main method to paint current game state on the screen
     * @param canvas where to paint game
     */
    public void paint(Canvas canvas){

        // This could be happened since drawing made in separate thread, so we can't handle it
        // as exception
        if (canvas == null)
            return;

        // Clear all previous data
        canvas.drawColor(BACKGROUND_COLOR);

        // Measuring canvas to improve performance
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();

        basicW = canvasWidth / stackSize;

        // It was easier to move stack cleanup animation to own method, since it has too
        // different behaviour
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

                paint.setColor(Color.argb(255, 238, 238, 238));
                canvas.drawRect(animationX, 0, animationX + 3 * basicW, 20, paint);
            }
            else {
                // normal drawing

                for (int i = 0; i < item.length; i++){
                    paint.setColor(item[i]);
                    canvas.drawRect(xPaint + i * basicW, 0, xPaint + (i + 1) * basicW, canvasHeight, paint);
                }

                paint.setColor(Color.argb(255, 238, 238, 238));
                canvas.drawRect(xPaint, 0, xPaint + 3 * basicW, 20, paint);
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
    }

    /**
     * Paints game state in 'cleanup animation' mode
     * @param canvas draw destination
     */
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

    public boolean isNotCongratulated() {
        return !alreadyCongratulated;
    }

    public void setAlreadyCongratulated(boolean alreadyCongratulated) {
        this.alreadyCongratulated = alreadyCongratulated;
    }
}