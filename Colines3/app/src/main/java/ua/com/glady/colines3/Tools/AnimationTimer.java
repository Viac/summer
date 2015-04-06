package ua.com.glady.colines3.Tools;

/**
 * A simplest class that provides animation timer, used to animate int value
 * So it could be used to animate position, color, transparency and so on
 *
 * Need some refactoring if float animation required. Also, it could be extended to
 * achieve interesting results if add 'behaviour' method as function f(x, t). It could be
 * used, for instance, to speed up position with time, etc.
 *
 * Created by Slava on 30.03.2015.
 */
public class AnimationTimer {

    private long startTime; // The time animation starting. At the time animation is started, it is got by System.currentTimeMillis().
    private long startValue; // The beginning value when animation starting.

    private long duration; // The duration to change value from mStartValue to mEndValue.

    private long finishValue; // The final value when animation ending.
    private long finishTime; // The time animation ending. mEndTime = mStartTime + mDuration.

    public long getStartValue() {
        return startValue;
    }

    public void setStartValue(long startValue) {
        this.startValue = startValue;
    }

    public long getFinishValue() {
        return finishValue;
    }

    public void setFinishValue(long finishValue) {
        this.finishValue = finishValue;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
        this.finishTime = startTime + duration;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns animated value depending on time
     * @param currentTime - this animation doesn't use system timer, so it's up to client
     *                    how to handle time
     * @return value of animated value
     */
    public float getCurrentValue(long currentTime) {
        if (duration == 0)
            return finishValue;
        float currentValue = startValue + (currentTime) * (finishValue - startValue) / duration;
        if (currentTime > finishTime) {
            currentValue = finishTime;
        }
        return currentValue;
    }
}
