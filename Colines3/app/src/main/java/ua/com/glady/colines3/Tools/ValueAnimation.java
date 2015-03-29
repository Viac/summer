package ua.com.glady.colines3.Tools;

/**
 * Created by Slava on 30.03.2015.
 */
public class ValueAnimation {

    public long beginValue; // The beginning value when animation starting.
    public long endValue; // The final value when animation ending.

    public long animDuration; // The duration to change value from mStartValue to mEndValue.

    public long animStartTime; // The time animation starting. At the time animation is started, it is got by System.currentTimeMillis().
    public long animFinishTime; // The time animation ending. mEndTime = mStartTime + mDuration.

    public float getCurrentValue(long currentTime) {
        if (animDuration == 0)
            return endValue;
        float currentValue = beginValue + (currentTime) * (endValue - beginValue) / animDuration;
        if (currentTime > animFinishTime) {
            currentValue = animFinishTime;
        }
        return currentValue;
    }
}
