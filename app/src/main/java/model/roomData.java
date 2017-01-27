package model;

import android.content.Context;

import ir.rooh110mojtaba.smartbuilding.R;

/**
 * Created by Mojtaba on 1/14/2017.
 */
public class roomData {

    private Context context;
    private long id;
    private String code;
    private String name;
    private float value;
    private String valueString;
    private String unit;
    private boolean isValueInteger;
    private String dataNameString;
    private String dataValueString;
    private String imgName;

    public roomData(Context context){
        this.context = context;
    }

    public void setId(long id){
        this.id = id;
    }

    public void setCode(String code){
        this.code = code;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setValue(float value){
        this.value = value;
    }

    public void setValue(float value, boolean isValueInteger){
        this.value = value;
        this.isValueInteger = isValueInteger;
    }

    public void setUnit(String unit){
        this.unit = unit;
    }

    public void setImgName(String imgName){
        this.imgName = imgName;
    }

    public long getId() {
        return id;
    }

    public float getValue(){
        return value;
    }

    public String getImgName(){
        if(imgName == null) {
            imgName = "dst_0";
        }
        return imgName;
    }

    public String getDataNameString(){
        this.dataNameString = name;
        return dataNameString;
    }

    public String getDataValueString(){
        if(isValueInteger) {
            valueString = String.valueOf((int) value);
        } else {
            valueString = String.valueOf(value);
        }
        if(code == "DST"){
            if(valueString == "0"){
                valueString = context.getResources().getString(R.string.doorCloseString);
            }else{
                valueString = context.getResources().getString(R.string.doorOpenString);
            }
        }
        this.dataValueString = valueString + " " + unit;
        return dataValueString;
    }

}
