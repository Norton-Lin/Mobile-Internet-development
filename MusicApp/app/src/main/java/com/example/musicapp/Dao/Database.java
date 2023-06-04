package com.example.musicapp.Dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.musicapp.Model.Music;
import java.util.List;

/**
 * @author Norton-Lin
 * @date 2023.6.4
 * @brief 数据库类
 */
public class Database extends SQLiteOpenHelper {
    private static final String db = "appdb";

    public Database(Context context) {
        super(context, db, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table if not exists music" +
                "(ID varchar(30) primary key," +
                "name varchar(30) not null," +
                "singer varchar(30) not null," +
                "picUrl varchar(100) not null," +
                "musicUrl varchar(100) not null)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void initMusicList(List<Music> musicList) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM music";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() > 0)// 数据库里不止一首歌
        {
            while (cursor.moveToNext()) {
                Music music = new Music();
                music.setId(cursor.getString(cursor.getColumnIndexOrThrow("ID")));
                music.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                music.setPicUrl(cursor.getString(cursor.getColumnIndexOrThrow("picUrl")));
                music.setMusicUrl(cursor.getString(cursor.getColumnIndexOrThrow("musicUrl")));
                musicList.add(music);
            }
        } else// 至少需要有一首歌 拿单向的18做保底
        {
            Music music = new Music();
            music.setId("29719172");
            music.setSinger("one direction");
            music.setPicUrl("http://p1.music.126.net/h97UzOq8a9YzuwjKxZxZOQ==/109951165969533755.jpg");
            music.setName("18");
            musicList.add(music);
        }
        cursor.close();
    }

    public void addMusic(Music music) {
        SQLiteDatabase db = getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("ID", music.getId());
        values.put("name", music.getName());
        values.put("singer", music.getSinger());
        values.put("picUrl", music.getPicUrl());
        values.put("musicUrl", music.getMusicUrl());
        db.insert("music", null, values);
    }
}
