package com.example.volley_example;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public class etcToolBox {
    static public byte[] bitmapToByteArray( Bitmap bitmap ) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, stream) ;
        byte[] byteArray = stream.toByteArray() ;
        return byteArray;
    }
}
