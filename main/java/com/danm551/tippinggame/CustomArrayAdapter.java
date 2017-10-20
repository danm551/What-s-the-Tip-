package com.danm551.tippinggame;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class CustomArrayAdapter extends ArrayAdapter<String>{
    final private String FONT_RIGHTEOUS = "font_righteous_regular.ttf";
    private Context mContext;
    private int mResourceId;
    private String[] array;
    private int options;

    CustomArrayAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull String[] objects, int o) {
        super(context, resource, objects);

        mContext = context;
        mResourceId = resource;
        array = objects;
        options = o;
    }

    /**
     * Custom ArrayAdapter that sets TextView parameters for list
     * @param position Position in the list array
     * @param convertView Unused
     * @param parent Unused
     * @return The view associated with the array element
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View mView = convertView;

        if(mView == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mView = inflater.inflate(mResourceId, null);
        }

        Typeface mTypeface = Typeface.createFromAsset(mContext.getAssets(), FONT_RIGHTEOUS);
        TextView mTextView = (TextView) mView.findViewById(R.id.customListView);

        switch(options) {
            case 0:
                mTextView.setTypeface(mTypeface);
                mTextView.setText(array[position]);
                mTextView.setTextSize(20);
                mTextView.setTextColor(Color.BLACK);
                mTextView.setPadding(30,30,30,30);
                break;
            case 1:
                mTextView.setTypeface(mTypeface);
                mTextView.setText(array[position]);
                mTextView.setTextSize(20);
                mTextView.setTextColor(Color.WHITE);
                mTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                break;
        }

        return mView;
    }
}
