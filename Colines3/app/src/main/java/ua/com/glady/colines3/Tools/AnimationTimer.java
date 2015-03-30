package ua.com.glady.colines3.Tools;

/**
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

    public long getFinishTime() {
        return finishTime;
    }

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
