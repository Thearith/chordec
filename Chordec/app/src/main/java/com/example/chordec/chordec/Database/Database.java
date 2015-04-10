package com.example.chordec.chordec.Database;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by thearith on 4/4/15.
 */
public class Database extends SQLiteOpenHelper{

    private static final String TAG = Database.class.getSimpleName();

    private static final String DATABASE_NAME = "chordec.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_CHORDS = "chords";
    private static final String CHORD_ID = "chord_id";
    private static final String CHORD_NAME = "chord_name";
    private static final String CHORD_SCORE = "chord_score";
    private static final String CHORD_DURATION = "chord_duration";
    private static final String CHORD_DATE = "chord_date";
    private static final String CHORD_FILE_PATH = "chord_file_path";

    private static final int    CHORD_ID_INDEX = 0;
    private static final int    CHORD_NAME_INDEX = 1;
    private static final int    CHORD_SCORE_INDEX = 2;
    private static final int    CHORD_DURATION_INDEX = 3;
    private static final int    CHORD_DATE_INDEX = 4;
    private static final int    CHORD_FILE_PATH_INDEX = 5;

    private static final String TABLE_CREATE = "create table "
            + TABLE_CHORDS  + "(" +
            CHORD_ID            + " integer primary key autoincrement, " +
            CHORD_NAME          + " text not null, "    +
            CHORD_SCORE         + " text not null, "    +
            CHORD_DURATION      + " integer not null, " +
            CHORD_DATE_INDEX    + " integer not null, " +
            CHORD_FILE_PATH + " text not null);";

    private static final String TABLE_CHORD_LOAD =
            "SELECT * FROM " + TABLE_CHORDS + " ORDER BY " + CHORD_ID;

    private static final String TABLE_CHORD_COUNT =
            "SELECT * FROM " + TABLE_CHORDS;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHORDS);
        onCreate(database);
    }

    public void insertChord(Chord chord) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(CHORD_ID, chord.getChordID());
        values.put(CHORD_NAME, chord.getChordName());
        values.put(CHORD_SCORE, chord.getChordScore());
        values.put(CHORD_DURATION, chord.getChordDuration());
        values.put(CHORD_DATE, chord.getChordDate());
        values.put(CHORD_FILE_PATH, chord.getChordPath());

        database.insert(TABLE_CHORDS, null, values);
        database.close();

        Log.d(TAG, "Chord with id " + chord.getChordID() + " is inserted into db");
    }

    public int getNextChordId() {
        return getNumChords();
    }

    public int getNumChords() {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor =
                database.rawQuery(TABLE_CHORD_COUNT, null);

        return cursor.getCount();
    }

    public int getSumDurations() {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(TABLE_CHORD_LOAD, null);
        int sum = 0;

        if(cursor.moveToFirst()) {
            do {
                int chordDuration = Integer.parseInt(cursor.getString(CHORD_DURATION_INDEX));
                sum += chordDuration;

            }while(cursor.moveToNext());
        }

        return sum;
    }

    public Chord getChord(int chordID) {

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(TABLE_CHORD_LOAD, null);
        Chord chord = new Chord();
        chord.setChordID(chordID);

        if(cursor.moveToFirst()) {
            do {
                int chordId = Integer.parseInt(cursor.getString(CHORD_ID_INDEX));
                if(chordId == chordID) {

                    String chordName = cursor.getString(CHORD_NAME_INDEX);
                    String chordScore = cursor.getString(CHORD_SCORE_INDEX);
                    int chordDuration = Integer.parseInt(cursor.getString(CHORD_DURATION_INDEX));
                    int chordDate = Integer.parseInt(cursor.getString(CHORD_DATE_INDEX));
                    String chordPath = cursor.getString(CHORD_FILE_PATH_INDEX);

                    chord.setChordName(chordName);
                    chord.setChordScore(chordScore);
                    chord.setChordDuration(chordDuration);
                    chord.setChordDate(chordDate);
                    chord.setChordPath(chordPath);

                    break;
                }

            } while(cursor.moveToNext());
        }

        database.close();

        return chord;

    }

    public void deleteChord(int chordID) {
        SQLiteDatabase database = this.getWritableDatabase();

        String query = "DELETE FROM " + TABLE_CHORDS + " WHERE " + CHORD_ID + " = " + chordID;
        database.execSQL(query);

        database.close();

        Log.d(TAG, "Chord with id " + chordID + " is deleted from db");
    }

    public ArrayList<Chord> getChords() {
        ArrayList<Chord> chords = new ArrayList<Chord>();

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(TABLE_CHORD_LOAD, null);

        if(cursor.moveToFirst()) {
            do {
                Chord chord = new Chord();

                int chordID = Integer.parseInt(cursor.getString(CHORD_ID_INDEX));
                String chordName = cursor.getString(CHORD_NAME_INDEX);
                String chordScore = cursor.getString(CHORD_SCORE_INDEX);
                int chordTime = Integer.parseInt(cursor.getString(CHORD_DURATION_INDEX));
                int chordDate = Integer.parseInt(cursor.getString(CHORD_DATE_INDEX));
                String chordPath = cursor.getString(CHORD_FILE_PATH_INDEX);

                chord.setChordID(chordID);
                chord.setChordName(chordName);
                chord.setChordScore(chordScore);
                chord.setChordDuration(chordTime);
                chord.setChordDate(chordDate);
                chord.setChordPath(chordPath);

                chords.add(chord);

            } while(cursor.moveToNext());
        }

        database.close();

        return chords;
    }


}
