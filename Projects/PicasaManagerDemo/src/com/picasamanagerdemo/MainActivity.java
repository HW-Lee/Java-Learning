package com.picasamanagerdemo;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String TAG = "Picasa";
    private int MAX_PHOTOS = 10;
    private String searchUserName;
    private Button btn_go;
    // Make the task start
    private Button btn_toggle_size;
    // toggle button used to choose size
    private Button btn_toggle_func;
    // toggle button used to choose function
    private Button is_loading_if_null;
    // toggle button used to set isLoadingIfNull
    private Button retrieve_type;
    // toggle button used to choose retrieve type
    private EditText search_account;
    // value of targetUser
    private EditText max_photo;
    // value of MAX_PHOTOS
    private EditText get_recent_from;
    // value of offset
    private EditText get_recent_photo_num;
    // value of photoNum
    private ListView imgList;
    // Show the photo informations
    private LinearLayout show_image_back;
    // Control the preview
    private ImgListAdapter adapter;
    // self-defined adapter used to show photo informations
    private ProgressDialog refreshDialog;
    // Avoid errors from occurring while the task is being executed

    private PicasaManager mPicasa;

    private ArrayList<PicasaPhotoInfo> PhotoArray = new ArrayList<PicasaPhotoInfo>();
    private ArrayList<PicasaPhotoInfo> PhotoArrayTemp = new ArrayList<PicasaPhotoInfo>();

    private int sizeStatus = 0;
    // Flag that brings information of which size of photo we want to preview
    // 0: Original
    // 1: Large
    // 2: Medium
    // 3: Small

    private int funcStatus = 0;
    // Flag that brings information of which function we want to implement
    // 0: Get recently uploaded photo
    // 1: Get photo album by album

    private int isSuccess = 0;
    // Flag that brings information of the AsyncTask
    // +1: Task Finished
    // +0: Unknown Error
    // -1: Task Failed

    private boolean isLoadingIfNull = false;
    // If isLoadingIfNull is true
    // the photo.imageBitmap will be refreshed and shown on listview

    private boolean retrieveType = false;
    // Flag that refers to which type is chosen
    // true: Videos
    // false: Photos

    private String targetSrc = "";
    private static final boolean DEBUG = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mPicasa = new PicasaManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateArray();
    }

    private void initView() {
        // Access some pointers needed to use
        search_account = (EditText) findViewById(R.id.search_account);
        max_photo = (EditText) findViewById(R.id.max_photo);
        get_recent_from = (EditText) findViewById(R.id.get_recent_from);
        get_recent_photo_num = (EditText) findViewById(R.id.get_recent_photo_num);
        btn_go = (Button) findViewById(R.id.btn_go);
        btn_toggle_size = (Button) findViewById(R.id.btn_toggle_size);
        btn_toggle_func = (Button) findViewById(R.id.btn_toggle_func);
        is_loading_if_null = (Button) findViewById(R.id.is_loading_if_null);
        retrieve_type = (Button) findViewById(R.id.retrieve_type);
        imgList = (ListView) findViewById(R.id.imgList);
        show_image_back = (LinearLayout) findViewById(R.id.show_image_back);

        // Set the default value
        show_image_back.setVisibility(View.GONE);
        search_account.setText("114726426617208665057");
        search_account.setText("tony123930");
        search_account.setText("minisfay");
        max_photo.setText("10");
        max_photo.setVisibility(View.INVISIBLE);
        get_recent_from.setHint("photo(s)");
        get_recent_photo_num.setHint("photo(s)");
        get_recent_photo_num.setText("100");

        // Set listeners
        btn_go.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PicasaTask().execute();
            }
        });
        btn_toggle_size.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                sizeStatus = (++sizeStatus) % 4;
                switch (sizeStatus) {
                case (0):
                    btn_toggle_size.setText("Original");
                    Toast.makeText(MainActivity.this, "Preview Size: Original", Toast.LENGTH_SHORT).show();
                    break;
                case (1):
                    btn_toggle_size.setText("Large");
                    Toast.makeText(MainActivity.this, "Preview Size: Large", Toast.LENGTH_SHORT).show();
                    break;
                case (2):
                    btn_toggle_size.setText("Medium");
                    Toast.makeText(MainActivity.this, "Preview Size: Medium", Toast.LENGTH_SHORT).show();
                    break;
                case (3):
                    btn_toggle_size.setText("Small");
                    Toast.makeText(MainActivity.this, "Preview Size: Small", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        });
        btn_toggle_func.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                funcStatus = (++funcStatus) % 2;
                switch (funcStatus) {
                case (0):
                    btn_toggle_func.setText("Recent");
                    max_photo.setVisibility(View.INVISIBLE);
                    get_recent_photo_num.setVisibility(View.VISIBLE);
                    get_recent_from.setHint("photo(s)");
                    get_recent_photo_num.setHint("photo(s)");
                    Toast.makeText(MainActivity.this, "Mode: get recently uploaded photo", Toast.LENGTH_SHORT).show();
                    break;
                case (1):
                    btn_toggle_func.setText("Albums");
                    max_photo.setVisibility(View.VISIBLE);
                    get_recent_photo_num.setVisibility(View.INVISIBLE);
                    max_photo.setHint("photo(s)");
                    get_recent_from.setHint("album(s)");
                    Toast.makeText(MainActivity.this, "Mode: get photo album by album", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        });
        is_loading_if_null.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                isLoadingIfNull = !isLoadingIfNull;
                is_loading_if_null.setText("isLoadingIfNull = " + isLoadingIfNull);
            }
        });
        retrieve_type.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                retrieveType = !retrieveType;
                if (retrieveType)
                    retrieve_type.setText("Retrieving Videos");
                else
                    retrieve_type.setText("Retrieving Photos");
            }
        });
        adapter = new ImgListAdapter(MainActivity.this, PhotoArray);
        imgList.setOnItemClickListener(showImageSrc);

        // Set adapter
        imgList.setAdapter(adapter);

        setOnTouch();
    }

    private void setOnTouch() {
        search_account.setOnTouchListener(isLockTouch);
        max_photo.setOnTouchListener(isLockTouch);
        get_recent_from.setOnTouchListener(isLockTouch);
        get_recent_photo_num.setOnTouchListener(isLockTouch);
        btn_go.setOnTouchListener(isLockTouch);
        btn_toggle_size.setOnTouchListener(isLockTouch);
        btn_toggle_func.setOnTouchListener(isLockTouch);
        imgList.setOnTouchListener(isLockTouch);
        show_image_back.setOnTouchListener(togglePreview);
    }

    private AdapterView.OnItemClickListener showImageSrc = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListView listview = (ListView) parent;
            PicasaPhotoInfo photoInfo = (PicasaPhotoInfo) listview.getItemAtPosition(position);
            String message = "";

            switch (sizeStatus) {
            case (0):
                targetSrc = photoInfo.getImageSrc();
                // return default size, original.
                message = "Open Original Image";
                break;
            case (1):
                targetSrc = photoInfo.getImageSrc(PicasaPhotoInfo.LARGE);
                // return large size.
                message = "Open Large Image";
                break;
            case (2):
                targetSrc = photoInfo.getImageSrc(PicasaPhotoInfo.MEDIUM);
                // return medium size.
                message = "Open Medium Image";
                break;
            case (3):
                targetSrc = photoInfo.getImageSrc(PicasaPhotoInfo.SMALL);
                // return small size, which is alwqys used as thumbnail picture.
                message = "Open Small Image";
                break;
            }
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            Log.d(TAG, message + ": " + targetSrc);
            new PreviewImage().execute(photoInfo);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private class PicasaTask extends AsyncTask<String, Void, String> {

        Long startTime;
        Long spentTime;

        @Override
        protected void onPreExecute() {
            refreshDialog = ProgressDialog.show(MainActivity.this, "Getting Data", "Getting Informations...", true,
                    true, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            PicasaTask.this.cancel(true);
                        }
                    });
            InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(search_account.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            try {
                if (Integer.parseInt(max_photo.getText().toString()) > 0)
                    MAX_PHOTOS = Integer.parseInt(max_photo.getText().toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            searchUserName = search_account.getText().toString();
        }

        @Override
        protected String doInBackground(String... arg0) {
            Log.d(TAG, "doInBackground");
            /*
             * Getting Data
             */
            clearTemp();
            startTime = System.currentTimeMillis();
            mPicasa.setMaxPhoto(MAX_PHOTOS);
            mPicasa.setTargetUser(searchUserName);
            mPicasa.getAlbumInfo();
            try {
                if (funcStatus == 0) {
                    int from = 0;
                    int num = MAX_PHOTOS;
                    try {
                        num = Integer.parseInt(get_recent_photo_num.getText().toString());
                    } catch (Exception e) {
                    }
                    try {
                        from = Integer.parseInt(get_recent_from.getText().toString());
                    } catch (Exception e) {
                    }
                    Log.d(TAG, "getRecentPhoto(" + from + ", " + num + ")");
                    if (retrieveType)
                        PhotoArrayTemp.addAll(mPicasa.getRecentVideo(from, num));
                    else
                        PhotoArrayTemp.addAll(mPicasa.getRecentPhoto(from, num));
                } else {
                    int from = 0;
                    try {
                        from = Integer.parseInt(get_recent_from.getText().toString());
                    } catch (Exception e) {
                    }
                    mPicasa.setMaxPhoto(MAX_PHOTOS);
                    if (retrieveType)
                        PhotoArrayTemp.addAll(mPicasa.getVideoTillMaximum(from));
                    else
                        PhotoArrayTemp.addAll(mPicasa.getPhotoTillMaximum(from));
                    Log.d(TAG, "getPhotoTillMaximum(" + from + ")");
                }
                isSuccess = 1;
                spentTime = System.currentTimeMillis() - startTime;
            } catch (Exception e) {
                Log.d(TAG, "Connected failed");
                isSuccess = -1;
                spentTime = System.currentTimeMillis() - startTime;
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(Void... progresses) {
            refreshDialog.setMessage("Loading Complete");
        }

        @Override
        protected void onPostExecute(String result) {
            if (isSuccess != 0) {
                String text = "";
                if (isSuccess == 1) {
                    text = "Finished: spent " + spentTime / 1000.0 + " s";
                    Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
                    Log.d(TAG, text);
                } else if (isSuccess == -1) {
                    text = "Failed: spent " + spentTime / 1000.0 + " s";
                    Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
                    Log.d(TAG, text);
                } else
                    Toast.makeText(MainActivity.this, "Maximum photos reached", Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(MainActivity.this, "Something wrong", Toast.LENGTH_LONG).show();
            updateArray();
            isSuccess = 0;
            refreshDialog.dismiss();
        }

    }

    private void clearTemp() {
        PhotoArrayTemp.clear();
    }

    private void updateArray() {
        PhotoArray.clear();
        PhotoArray.addAll(PhotoArrayTemp);
        adapter.notifyDataSetChanged();

        String tag = "PhotoNo.";
        for (int i = 0; i < PhotoArray.size(); i++) {
            PicasaPhotoInfo photoInfo = PhotoArray.get(i);
            if (DEBUG) {
                if (funcStatus == 1)
                    Log.d(tag + i + ";" + TAG, photoInfo.getAlbumNameToString());
                Log.d(tag + i + ";" + TAG, photoInfo.getImageSrcToString());
            }
        }
        Log.d(TAG, "Retrieved Array Length : " + PhotoArray.size());
    }

    private class PreviewImage extends AsyncTask<PicasaPhotoInfo, Void, String[]> {

        ImageView imgview = (ImageView) findViewById(R.id.show_image);
        Bitmap bitmap;

        @Override
        protected void onPreExecute() {
            refreshDialog = ProgressDialog.show(MainActivity.this, "Getting Picture", "Loading pictures...", true,
                    true, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            PreviewImage.this.cancel(true);
                            show_image_back.setVisibility(View.GONE);
                        }
                    });
            show_image_back.setVisibility(View.VISIBLE);
            imgview.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String[] doInBackground(PicasaPhotoInfo... param) {
            PicasaPhotoInfo photo = param[0];
            String[] result = null;
            if (retrieveType)
                try {
                    result = mPicasa.getVideoSrc(photo);
                } catch (ClientProtocolException e) {
                    Log.e(TAG, "ClientProtocolException");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e(TAG, "IOException");
                    e.printStackTrace();
                }
            photo.getImageBitmap(isLoadingIfNull);
            // If isLoadingIfNull is true, the photo.imageBitmap will be
            // refreshed and shown on listview
            bitmap = PicasaPhotoInfo.getImageByURL(targetSrc);
            targetSrc = "";
            return result;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                for (String resultE : result)
                    Log.d(TAG, "get Src :\n" + resultE);
            }
            refreshDialog.dismiss();
            imgview.setImageBitmap(bitmap);
            imgview.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (show_image_back.getVisibility() == View.GONE) {
                ConfirmExit();
                return true;
            } else {
                show_image_back.setVisibility(View.GONE);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void ConfirmExit() {
        AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
        ad.setTitle("Exit");
        ad.setMessage("Do you want to exit?");
        ad.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                MainActivity.this.finish();
            }
        });
        ad.setNegativeButton("no", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                // Do nothing
            }
        });
        ad.show();
    }

    private View.OnTouchListener isLockTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (show_image_back.getVisibility() == View.GONE) {
                return false; // Unlock all actions
            } else {
                return true; // Lock all actions
            }
        }
    };

    private View.OnTouchListener togglePreview = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (show_image_back.getVisibility() == View.VISIBLE) {
                show_image_back.setVisibility(View.GONE);
                return true;
            }
            return false;
        }
    };
}
