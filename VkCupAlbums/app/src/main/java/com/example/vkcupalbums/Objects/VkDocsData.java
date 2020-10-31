package com.example.vkcupalbums.Objects;

import android.graphics.Bitmap;

import com.example.vkcupalbums.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VkDocsData {

    public static final int TEXT  = 1;
    public static final int ZIP   = 2;
    public static final int GIF   = 3;
    public static final int IMAGE = 4;
    public static final int AUDIO = 5;
    public static final int VIDEO = 6;
    public static final int BOOKS = 7;
    public static final int EXT   = 8;

    private int type;
    private int id;
    private String title;
    private int size;
    private String ext;
    private String  url;
    private String date;
    private String httpPhoto;

    private String typeSizeDate;
    private Bitmap bitmap = null;

    private static final String[] months = {"Января", "Февраля", "Марта", "Апреля", "Мая", "Июня", "Июля", "Августа", "Сентября", "Октября", "Ноября", "Декабря"};

    public VkDocsData(int id, int type, String title, int size, String ext, String url, String httpPhoto, int date) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.size = size;
        this.ext = ext;
        this.url = url;
        this.httpPhoto = httpPhoto;

        DateFormat dateFormat = new SimpleDateFormat("dd:MM:yyyy", Locale.getDefault());
        String dateText = dateFormat.format(new Date(date * 1000L));
        String[] split = dateText.split(":");
        String dateString = split[0] + " " + months[Integer.valueOf(split[1]) - 1] + " " + split[2];

        typeSizeDate = ext + " · " +  getSize(size) + " · " + dateString;
    }

    public String getHttpPhoto() { return httpPhoto; }

    public void setBitmap(Bitmap bitmap) { this.bitmap = bitmap; }

    private String getSize(int sizeData) {
        int s = (int) (sizeData / (1024 * 1024));
        String size = String.valueOf(s) + "mb";
        if (s == 0) {
            s = (int) sizeData / 1024;
            size = String.valueOf(s) + "kb";
            if (s == 0)
                size = String.valueOf(sizeData) + "byte";
        }
        return size;
    }

    public String getTypeSizeDate() { return typeSizeDate; }

    public Bitmap getBitmap() { return bitmap; }

    public int getImageResources() {
        if (type == TEXT)
            return R.mipmap.ic_placeholder_document_text_72;
        else if (type == BOOKS)
            return R.mipmap.ic_placeholder_document_book_72;
        else if (type == IMAGE || id == GIF)
            return R.mipmap.ic_placeholder_document_image_72;
        else if (type == AUDIO)
            return R.mipmap.ic_placeholder_document_music_72;
        else if (type == VIDEO)
            return R.mipmap.ic_placeholder_document_video_72;
        else if (type == ZIP)
            return R.mipmap.ic_placeholder_document_archive_72;
        return R.mipmap.ic_placeholder_document_other_72;
    }

    public void showDocs() {

    }

    public void setTitle(String title) { this.title = title; }

    public int getSize() { return size; }

    public int getId() { return id; }

    public int getType() { return type; }

    public String getDate() { return date; }

    public String getTitle() { return title; }

    public String getUrl() { return url; }

    public String getExt() { return ext; }
}
