package com.example.chordec.chordec.ListView;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
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

    private static final String TAG = CustomAdapter.class.getSimpleName();

    private static final int LIST_ITEM_XML
            = R.layout.activity_database_listitem;

    private static final int TITLE_MAXIMUM_LENGTH = 24;

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
            if(name.length() >= TITLE_MAXIMUM_LENGTH)
                chordName.setText(name.substring(0, TITLE_MAXIMUM_LENGTH));
            else
                chordName.setText(name);

            TextView chordDate = (TextView) convertView.findViewById(R.id.chordDate);
            chordDate.setText(chords.get(position).getChordDate());

            TextView chordDuration = (TextView) convertView.findViewById(R.id.chordDuration);
            chordDuration.setText(Constants.getDurationFormat(chords.get(position).getChordDuration()));

            TextView chordID = (TextView) convertView.findViewById(R.id.chordID);
            chordID.setText(String.valueOf(chords.get(position).getChordID()));

            // make round letters
            String color = Constants.COLORS[
                    (int) (Math.random() * Constants.COLORS.length)];

            TextDrawable roundLetter = TextDrawable.builder()
                    .beginConfig()
                        .toUpperCase()
                        .fontSize(80)
                        .withBorder(4)
                    .endConfig()
                    .buildRound(name.substring(0, 1), Color.parseColor(color));

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




}
