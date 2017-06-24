package example.chengyuhsiu.basicaudiotest;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MediaController.MediaPlayerControl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;


public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private ImageButton mBtnMediaPlayPause, mBtnMediaPrev, mBtnMediaNext;
    private ToggleButton mBtnMediaRepeat;


    private MediaPlayer mMediaPlayer = null;

    private Boolean mbIsInitialised = true;

    private final int REQUEST_PERMISSION_FOR_WRITE_EXTERNAL_STORAGE = 100;

    private String TAG="BasicAudioTest";

    private ArrayList<Song> songList;
    private ListView songView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnMediaPlayPause = (ImageButton)findViewById(R.id.btnMediaPlayPause);
        mBtnMediaPrev = (ImageButton)findViewById(R.id.btnPrev);
        mBtnMediaNext = (ImageButton)findViewById(R.id.btnNext);
        mBtnMediaRepeat = (ToggleButton) findViewById(R.id.btnRepeat);

        mBtnMediaPlayPause.setOnClickListener(btnMediaPlayPauseOnClick);
        mBtnMediaPrev.setOnClickListener(btnMediaPrevOnClick);
        mBtnMediaNext.setOnClickListener(btnMediaNextOnClick);
        mBtnMediaRepeat.setOnClickListener(btnMediaRepeatOnClick);

        askForWriteExternalStoragePermission();

        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();
        getSongList();

        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song song, Song song2) {
                return song.getTitle().compareTo(song2.getTitle());
            }
        });

        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 檢查收到的權限要求編號是否和我們送出的相同
        if (requestCode == REQUEST_PERMISSION_FOR_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "取得 WRITE_EXTERNAL_STORAGE 權限", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mMediaPlayer = new MediaPlayer();

        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.song);

        try {
            mMediaPlayer.setDataSource(this, uri);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "指定的音樂檔錯誤！", Toast.LENGTH_LONG)
                    .show();
        }

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);


    }

    @Override
    protected void onStop() {
        super.onStop();

        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.stop();
        mbIsInitialised = true;
        mBtnMediaPlayPause.setImageResource(android.R.drawable.ic_media_play);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        mediaPlayer.release();
        mediaPlayer = null;

        Toast.makeText(MainActivity.this, "發生錯誤，停止播放", Toast.LENGTH_LONG)
                .show();

        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.seekTo(0);
        mediaPlayer.start();

        Toast.makeText(MainActivity.this, "開始播放", Toast.LENGTH_LONG)
                .show();
    }

    private View.OnClickListener btnMediaPlayPauseOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mMediaPlayer.isPlaying()) {
                mBtnMediaPlayPause.setImageResource(android.R.drawable.ic_media_play);
                mMediaPlayer.pause();
            } else {
                mBtnMediaPlayPause.setImageResource(android.R.drawable.ic_media_pause);

                if (mbIsInitialised) {
                    mMediaPlayer.prepareAsync();
                    mbIsInitialised = false;
                } else
                    mMediaPlayer.start();
            }
        }
    };

    private View.OnClickListener btnMediaPrevOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

        }
    };

    private View.OnClickListener btnMediaNextOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

        }
    };

    private View.OnClickListener btnMediaRepeatOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.e(TAG, "Try to set repeat");
            if (((ToggleButton)v).isChecked()) {
                mMediaPlayer.setLooping(true);
                //Log.e(TAG, "set repeat true");
            } else {
                mMediaPlayer.setLooping(false);
                //Log.e(TAG, "set repeat false");
            }
        }
    };

    private void askForWriteExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            // 這項功能尚未取得使用者的同意
            // 開始執行徵詢使用者的流程
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    MainActivity.this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder altDlgBuilder =
                        new AlertDialog.Builder(MainActivity.this);
                altDlgBuilder.setTitle("提示");
                altDlgBuilder.setMessage("App需要讀寫SD卡中的資料。");
                altDlgBuilder.setIcon(android.R.drawable.ic_dialog_info);
                altDlgBuilder.setCancelable(false);
                altDlgBuilder.setPositiveButton("確定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // 顯示詢問使用者是否同意功能權限的對話盒
                                // 使用者答覆後會執行onRequestPermissionsResult()
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{
                                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_PERMISSION_FOR_WRITE_EXTERNAL_STORAGE);
                            }
                        });
                altDlgBuilder.show();
                return;
            } else {
                // 顯示詢問使用者是否同意功能權限的對話盒
                // 使用者答覆後會執行onRequestPermissionsResult()
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_FOR_WRITE_EXTERNAL_STORAGE);
                return;
            }
        }
    }

    public void getSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri mUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(mUri,null, null, null, null);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            } while (musicCursor.moveToNext());
        }
    }

    public void songPicked(View view) {
        Log.e(TAG, "song picked test");
        int curFile;
        curFile = Integer.parseInt(view.getTag().toString());
        Log.e(TAG, "song picked test" + curFile);
    }
}
