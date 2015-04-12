package com.example.chordec.chordec.Database;

/**
 * Created by thearith on 4/4/15.
 */
public class Chord {

    private int chordID;
    private String chordName;
    private int chordDuration;
    private String chordDate;

    public String getChordDate() {
        return chordDate;
    }

    public void setChordDate(String chordDate) {
        this.chordDate = chordDate;
    }

    private String chordScore;
    private String chordPath;

    public int getChordID() {
        return chordID;
    }

    public String getChordScore() {
        return chordScore;
    }

    public String getChordName() {
        return chordName;
    }

    public void setChordName(String chordName) {
        this.chordName = chordName;
    }

    public String getChordPath() {
        return chordPath;

    }

    public void setChordID(int chordID) {
        this.chordID = chordID;
    }

    public void setChordScore(String chordScore) {
        this.chordScore = chordScore;
    }

    public void setChordPath(String chordPath) {
        this.chordPath = chordPath;
    }

    public int getChordDuration() {
        return chordDuration;
    }

    public void setChordDuration(int duration) {
        this.chordDuration = duration;
    }
}
