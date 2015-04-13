package com.example.chordec.chordec.ListView;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseBooleanArray;
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


    private SparseBooleanArray mSelectedItemsIds;

    private class ViewHolder {
        TextView ChordName;
        TextView ChordDate;
        TextView ChordDuration;
        TextView ChordID;
        ImageView RoundedLetter;
    }

    public CustomAdapter(Context context, int layout, ArrayList<Chord> values) {
        super(context, layout, values);
        this.context = context;
        this.chords = values;
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewHolder holder;

        if(convertView == null) {
            convertView = inflater.inflate(LIST_ITEM_XML, parent, false);
            holder = new ViewHolder();

            holder.ChordName = (TextView) convertView.findViewById(R.id.chordName);
            holder.ChordDate = (TextView) convertView.findViewById(R.id.chordDate);
            holder.ChordDuration = (TextView) convertView.findViewById(R.id.chordDuration);
            holder.ChordID = (TextView) convertView.findViewById(R.id.chordID);
            holder.RoundedLetter = (ImageView) convertView.findViewById(R.id.roundedLetter);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder)  convertView.getTag();
        }

        String name = chords.get(position).getChordName();
        if(name.length() >= TITLE_MAXIMUM_LENGTH)
            holder.ChordName.setText(name.substring(0, TITLE_MAXIMUM_LENGTH));
        else
            holder.ChordName.setText(name);


        holder.ChordDate.setText(chords.get(position).getChordDate());

        holder.ChordDuration.setText(Constants.getDurationFormat(chords.get(position).getChordDuration()));

        holder.ChordID.setText(String.valueOf(chords.get(position).getChordID()));

        // make round letters
        String color = Constants.COLORS[position % Constants.COLORS.length];
                //(int) (Math.random() * Constants.COLORS.length)];

        TextDrawable roundLetter = TextDrawable.builder()
                .beginConfig()
                .toUpperCase()
                .fontSize(80)
                .withBorder(4)
                .endConfig()
                .buildRoundRect(name.substring(0, 1),
                        Color.parseColor(color),
                        60);

        holder.RoundedLetter.setImageDrawable(roundLetter);

        if(mSelectedItemsIds.get(position)) {
            convertView.setBackgroundColor(Color.parseColor(Constants.SELECTED_COLOUR));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
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

    @Override
    public void remove(Chord chord) {
        chords.remove(chord);
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);

        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

}
