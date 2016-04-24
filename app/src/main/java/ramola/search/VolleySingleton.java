package ramola.search;


import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {
    private static VolleySingleton volleySingleton=null;
    private static RequestQueue requestQueue;
    private static ImageLoader imageLoader;
    private VolleySingleton(){
        requestQueue= Volley.newRequestQueue(MyApplication.getContext());
        imageLoader=new ImageLoader(requestQueue,new ImageLoader.ImageCache() {
            private LruCache<String,Bitmap> cache=new LruCache((int) (Runtime.getRuntime().maxMemory()/1024/8));
            @Override
            public Bitmap getBitmap(String s) {
                return cache.get(s);
            }

            @Override
            public void putBitmap(String s, Bitmap bitmap) {
             cache.put(s,bitmap);
            }
        });
    }
    public static VolleySingleton getInstance(){
        if(volleySingleton==null)
            volleySingleton=new VolleySingleton();
        return volleySingleton;
    }
    public   RequestQueue getRequestQueue(){
        return requestQueue;
    }
    public ImageLoader getImageLoader(){
        return imageLoader;
    }
}
