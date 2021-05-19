package com.example.awss3imageupload;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Util {
    public static void showToast(Context context, String text){
        LayoutInflater inflater= LayoutInflater.from(context);
        View layout=inflater.inflate(R.layout.toast,null,false);
        Toast toast=new Toast(context);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.setDuration(Toast.LENGTH_LONG);
        TextView toastText=(TextView)layout.findViewById(R.id.toast_auth_text);
        toastText.setText(text);
        toast.setView(layout);
        toast.show();
    }
}
