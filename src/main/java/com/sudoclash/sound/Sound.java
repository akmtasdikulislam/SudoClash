package com.sudoclash.sound;

// The sound effects of the game: short melodies (files in src/main/resources/sounds).
// Actions that happen very often (correct numbers, wrong numbers, clicks) have several variants,
// and the game plays them in turn, so a long game never repeats one sound again and again.
public enum Sound {
    CLICK("click_1", "click_2"),
    CORRECT("correct_1", "correct_2", "correct_3", "correct_4"),
    WRONG("wrong_1", "wrong_2"),
    HINT("hint"),
    COMPLETE("complete"),
    WIN("win"),
    LOSE("lose"),
    REACTION("reaction"),
    YOUR_TURN("your_turn"),
    // One sound for each emoji reaction (see FaceOffScreen).
    LAUGH("laugh"),
    CLAP("clap"),
    HMM("hmm"),
    PARTY("party"),
    GASP("gasp"),
    AWW("aww"),
    NICE("nice"),
    ALMOST("almost"),
    GOOD_GAME("gg");

    private final String[] files;

    Sound(String... files) {
        this.files = files;
    }

    public int getVariantCount() {
        return files.length;
    }

    public String getResource(int variant) {
        return "/sounds/" + files[variant % files.length] + ".wav";
    }
}
