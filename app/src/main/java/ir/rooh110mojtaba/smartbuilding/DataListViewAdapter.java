package ir.rooh110mojtaba.smartbuilding;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import model.roomData;

/**
 * Created by Mojtaba on 1/14/2017.
 */
public class DataListViewAdapter extends BaseAdapter {

    private ArrayList<roomData> allRoomData;
    private Context context;
    private float fontSize = 30;

    public DataListViewAdapter(ArrayList<roomData> allRoomData, Context context){
        this.allRoomData = allRoomData;
        this.context = context;
    }

    public void setFontSize(float fontSize){
        this.fontSize = fontSize;
    }

    @Override
    public int getCount(){
        if(allRoomData == null){
            return 0;
        }else {
            return allRoomData.size();
        }
    }

    @Override
    public Object getItem(int position){
        return allRoomData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return allRoomData.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.data_list_view, parent, false);

        ImageView dataImageImg = (ImageView) view.findViewById(R.id.dataImageView);
        dataImageImg.setImageResource(context.getResources().getIdentifier(allRoomData.get(position).getImgName(),
                "mipmap",context.getPackageName()));

        TextView dataStringTxt = (TextView) view.findViewById(R.id.dataNameStringTextView);
        dataStringTxt.setTextSize(fontSize);
        dataStringTxt.setText(allRoomData.get(position).getDataNameString());

        TextView dataValueStringTxt = (TextView) view.findViewById(R.id.dataValueStringTextView);
        dataValueStringTxt.setTextSize(fontSize*4/5);
        dataValueStringTxt.setText(allRoomData.get(position).getDataValueString());

        return view;
    }

}
