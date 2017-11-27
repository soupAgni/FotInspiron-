package com.example.anastasia.fotinspiron_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserList extends AppCompatActivity implements SearchView.OnQueryTextListener {

    ArrayList<String> usernames;
    ArrayAdapter arrayAdapter;
    SearchView searchview;

     ListView userList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_user_list);

        searchview = findViewById(R.id.search);
         usernames = new ArrayList<String>();

         arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, usernames);

         userList = findViewById(R.id.lv_userslist);


         userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 Intent intent = new Intent(getApplicationContext(), UsersFeed.class);
                 intent.putExtra("username", usernames.get(i));
                 startActivity(intent);
             }
         });
        //getting the query from the parse users class
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        //You dont want it to get your own username
        query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.addAscendingOrder("username");

        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e == null){
                    if(objects.size() > 0){
                        for(ParseUser user : objects){
                            //Put every user into an array list
                            usernames.add(user.getUsername());
                        }
                        userList.setAdapter(arrayAdapter);
                    }
                }
            }
        });
        userList.setTextFilterEnabled(true);
        setupSearchView();
    }
    private void setupSearchView(){
        searchview.setIconifiedByDefault(false);
        searchview.setOnQueryTextListener(this);
        searchview.setSubmitButtonEnabled(true);
        searchview.setQueryHint("Search Here");
    }
    @Override
    public boolean onQueryTextChange(String newText){
        if(TextUtils.isEmpty(newText)){
            userList.clearTextFilter();
        }
        else{
            userList.setFilterText(newText.toString());
        }
        return true;
    }
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_users_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //allow user to share the image
        if(id == R.id.share){

            //to get image change intent of the app so that it can be accessed through another activity
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, 1);

        }
        if(id == R.id.logout){
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    Intent i = new Intent(UserList.this, FacebookLoginActivity.class);
                    startActivity(i);

                }
            });
            Log.i("onOptionsSelectedItem", "Log out");
            //Intent i = new Intent(UserList.this, FacebookLoginActivity.class);
           // startActivity(i);
        }
        if (id == R.id.profile){
            Intent main = new Intent(UserList.this,ProfileActivity.class);
            startActivity(main);
        }
        return super.onOptionsItemSelected(item);
    }

    //getting the image from the external activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmapImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                Log.i("AppInfo", "ImageRecieved");

                //in order to pass it into parse
                //(like the stream we get in the web when we download it)
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                //do not compress for now
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                //store the image as a byte array
                byte[] byteArray = stream.toByteArray();
                //convert to parse file before passing into parse
                ParseFile file = new ParseFile("image.png", byteArray);
                /*file.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null){
                            Log.i("fileSave: ", "Successful");
                        }
                        else{
                            Log.i("fileSave: ", "UnSuccessful");
                            e.printStackTrace();
                            Log.i("fileSave: ", "UnSuccessful");
                        }
                    }
                });*/
                ParseObject object = new ParseObject("Images");

                object.put("username", ParseUser.getCurrentUser().getUsername());
                object.put("images", file);

                /*ParseACL parseACL = new ParseACL();
                parseACL.setPublicReadAccess(true);
                object.setACL(parseACL);*/

                object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null){
                            Toast.makeText(getApplication().getBaseContext(), "Your image has been posted!", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Log.i("SaveInBg", "Couldnt save");
                            e.printStackTrace();
                            Toast.makeText(getApplication().getBaseContext(), "Error - Please try again", Toast.LENGTH_LONG).show();
                            Log.i("SaveInBg", "Couldnt save");

                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplication().getBaseContext(), "Error Before save in Bg", Toast.LENGTH_LONG).show();

            }

        }
    }

}
