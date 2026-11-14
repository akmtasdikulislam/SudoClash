package com.sudoclash.game;

public interface TimerListener {
    void onTick(int secondsLeft);

    void onTimeUp();
}
