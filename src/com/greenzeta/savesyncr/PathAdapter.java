package com.greenzeta.savesyncr;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class PathAdapter extends ArrayAdapter<Path> {
 
    int resource;
    String response;
    Context context;
    private final static String TAG = "PathAdapter";
    
    //Initialize adapter
    public PathAdapter(Context context, int resource, List<Path> items) {
        super(context, resource, items);
        this.resource=resource;
 
    }
     
     
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
    	View view;
        TextView textTitle;
        TextView textTimer;
        final ImageView image;
    	
        LinearLayout alertView;
        //Get the current alert object
        final Path al = getItem(position);
         
        //Inflate the view
        if(convertView==null)
        {
        	Log.d("VIEW","isnull");
            alertView = new LinearLayout(getContext());
            Log.d("VIEW","inflater");
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi;
            Log.d("VIEW","LayoutInflater");
            vi = (LayoutInflater)getContext().getSystemService(inflater);
            Log.d("VIEW","inflate");
            vi.inflate(resource, alertView, true);
        }
        else
        {
        	Log.d("VIEW","not null");
            alertView = (LinearLayout) convertView;
        }
        
        //Get the text boxes from the listitem.xml file
        TextView alertText =(TextView)alertView.findViewById(R.id.txtAlertText);
        TextView alertDate =(TextView)alertView.findViewById(R.id.txtAlertDate);
        ImageButton button = (ImageButton)alertView.findViewById(R.id.pathRemove);
         
        //Assign the appropriate data from our alert object above
        alertText.setText(al.name);
        alertDate.setText(al.localpath);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "string: " + al.name);

                //Toast.makeText(context, "button clicked: " + al.name, Toast.LENGTH_SHORT).show();
            }
        });
         
        return alertView;
    }
 
}