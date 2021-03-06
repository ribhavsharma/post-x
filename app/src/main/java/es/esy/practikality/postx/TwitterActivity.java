package es.esy.practikality.postx;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.Image;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class TwitterActivity extends AppCompatActivity {
    //private static int xyz = 1;
    //private static int yyy = 1;
    private static final int PICK_FROM_GALLERY = 2;
    ImageView Mybackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_template);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Mybackground = (ImageView) findViewById(R.id.profile_picture_of_tweet);
        Typeface helvetica = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeue.ttf");
        Typeface stilu_Regular = Typeface.createFromAsset(getAssets(), "fonts/stilu-Regular.otf");
        Typeface stilu_Light = Typeface.createFromAsset(getAssets(), "fonts/stilu-Light.otf");
        TextView tv1 = (TextView) findViewById(R.id.username);
        TextView tv2 = (TextView) findViewById(R.id.fullname);
        TextView caption = (TextView) findViewById(R.id.caption);
        TextView hashtags_text_view = (TextView) findViewById(R.id.hashtags_in_tweet);
        tv1.setTypeface(helvetica);
        caption.setTypeface(stilu_Light);
        tv2.setTypeface(stilu_Regular);
        hashtags_text_view.setTypeface(helvetica);
        SharedPreferences sharedPreferences = getSharedPreferences("postx", Context.MODE_PRIVATE);
        String username = "@"+sharedPreferences.getString("username", "UserName");
        tv1.setText(username);
        tv2.setText(sharedPreferences.getString("fullname", "Full Name"));
        caption.setText(sharedPreferences.getString("caption_post", "No Caption Found!"));
        String hashtags_stored = sharedPreferences.getString("hashtags","none_found");
        if(!hashtags_stored.equals("none_found")){
            String[] hashtags_array = hashtags_stored.split(" ");
            String final_hashtags = "";
            for (String tag : hashtags_array){
                final_hashtags += "#" + tag + " ";
            }
            hashtags_text_view.setText(final_hashtags);
        }
    }
    public void adjustSize(View view) {
        TextView tv1 = (TextView) findViewById(R.id.caption);
        EditText et = (EditText) findViewById(R.id.font_size);
        String fontSize = et.getText().toString();
        if (fontSize.length() > 0) {
            float font = Integer.parseInt(fontSize);
            tv1.setTextSize(font);
        }
    }
    public void Gallery(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICK_FROM_GALLERY);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case PICK_FROM_GALLERY:
                    Uri selectedImage = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), selectedImage);
                        Mybackground.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        Log.i("TAG", "Some exception " + e);
                    }
                    break;
            }
    }

    public void share(View view) {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.tweet_body);
        linearLayout.setDrawingCacheEnabled(true);
        linearLayout.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(linearLayout.getDrawingCache());
        linearLayout.setDrawingCacheEnabled(true);
        Toast.makeText(getApplicationContext(), "Just making those finishing touches", Toast.LENGTH_SHORT).show();

        try {
            File cachePath = new File(getApplicationContext().getCacheDir(), "images");
            cachePath.mkdirs(); // don't forget to make the directory
            FileOutputStream stream = new FileOutputStream(cachePath + "/image.png"); // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("File not found error");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO Exception error");
        }
        File imagePath = new File(getApplicationContext().getCacheDir(), "images");
        File newFile = new File(imagePath, "image.png");

        Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "es.esy.practikality.postx", newFile);

        if (contentUri != null) {
            Date dt = Calendar.getInstance().getTime();
            MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,dt.toString(),"Post X by Practikality");
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Shared with #PostXbyPractikality");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Shared with #PostXbyPractikality");
            startActivity(Intent.createChooser(shareIntent, "Choose an app"));
            finish();
        }
    }
}
