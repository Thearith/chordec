package com.example.chordec.chordec.ListView;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.example.chordec.chordec.Database.Chord;
import com.example.chordec.chordec.Helper.Constants;
import com.example.chordec.chordec.R;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by thearith on 10/4/15.
 */
public class CustomAdapter extends ArrayAdapter<Chord> {

    private static final int LIST_ITEM_XML
            = R.layout.activity_database_listitem;

    private static final int MILLISECONDS_RATE = 1000;
    private static final int HOURS_RATE = 3600;
    private static final int MINUTES_RATE = 60;

    private final Context context;
    private final ArrayList<Chord> chords;

    public CustomAdapter(Context context, int layout, ArrayList<Chord> values) {
        super(context, layout, values);
        this.context = context;
        this.chords = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(convertView == null) {
            convertView = inflater.inflate(LIST_ITEM_XML, parent, false);

            // insert view
            String name = chords.get(position).getChordName();

            TextView chordName = (TextView) convertView.findViewById(R.id.chordName);
            chordName.setText(name);

            TextView chordDate = (TextView) convertView.findViewById(R.id.chordDate);
            chordDate.setText(getDateFormat(chords.get(position).getChordDate()));

            TextView chordDuration = (TextView) convertView.findViewById(R.id.chordDuration);
            chordDuration.setText(getDurationFormat(chords.get(position).getChordDuration()));

            TextView chordID = (TextView) convertView.findViewById(R.id.chordID);
            chordID.setText(chords.get(position).getChordID());

            // make round letters
            String color = Constants.COLORS[(int)
                    Math.random() * Constants.COLORS.length];

            TextDrawable roundLetter = TextDrawable.builder()
                    .buildRoundRect(name.substring(0, 0), Color.parseColor(color), 10);

            ImageView roundedLetter = (ImageView) convertView.findViewById(R.id.roundedLetter);
            roundedLetter.setImageDrawable(roundLetter);

        }

        return convertView;
    }

    @Override
    public int getCount() {
        return chords.size();
    }

    @Override
    public Chord getItem(int position) {
        return chords.get(position);

    }


    /*
    * Helper function -- formatting
    * */

    private String getDateFormat(long milliseconds) {
        Date date = new Date(milliseconds);
        SimpleDateFormat ft = new SimpleDateFormat("MMMM dd yyyy | HH:mm a");

        return ft.format(date);
    }

    private String getDurationFormat(int duration) {

        int seconds = duration / MILLISECONDS_RATE;

        int hour = seconds / HOURS_RATE;
        int minute = (seconds - hour*HOURS_RATE) / MINUTES_RATE;
        int second = (seconds - minute*MINUTES_RATE);

        return hour == 0 ?
                (minute + ":" + second) :
                (hour + ":" + minute + ":" + second);
    }


}
