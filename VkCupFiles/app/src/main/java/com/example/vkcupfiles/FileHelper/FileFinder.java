package com.example.vkcupfiles.FileHelper;

import android.os.Environment;

import java.io.File;
import java.util.Vector;

public class FileFinder extends Thread {
    private Vector<String> dataText;
    private Vector<String> fileView;
    private Vector<String> books;
    private Vector<String> video;
    private Vector<String> audio;
    private Vector<String> zip;
    private Vector<String> other;
    private Vector<String> image;
    private Vector<File> dir = new Vector<>();
    private OnFileFound onFileFound;

    public FileFinder(OnFileFound onFileFound) {
        String[] user = {Environment.DIRECTORY_ALARMS, Environment.DIRECTORY_DCIM, Environment.DIRECTORY_DOWNLOADS, Environment.DIRECTORY_DOCUMENTS,
                         Environment.DIRECTORY_MOVIES, Environment.DIRECTORY_MUSIC, Environment.DIRECTORY_RINGTONES};
        for (String dirs : user)
            dir.addElement(Environment.getExternalStoragePublicDirectory(dirs));
        /*dir.addElement(Environment.getDataDirectory());
        dir.addElement(Environment.getDownloadCacheDirectory());
        dir.addElement(Environment.getRootDirectory());
        dir.addElement(Environment.getExternalStorageDirectory());*/

        this.onFileFound = onFileFound;

        dataText = new Vector<>();
        dataText.addElement("txt"); // text
        dataText.addElement("rtf");
        dataText.addElement("cpp");
        dataText.addElement("h");
        dataText.addElement("pas");
        dataText.addElement("dat");
        dataText.addElement("java");
        dataText.addElement("py");
        dataText.addElement("js");
        dataText.addElement("c");

        fileView = new Vector<>();
        fileView.addElement("doc"); // microsoft and other
        dataText.addElement("xml");
        fileView.addElement("docx");
        fileView.addElement("html");
        fileView.addElement("pdf");
        fileView.addElement("odt");
        fileView.addElement("pptx");
        fileView.addElement("odt");
        fileView.addElement("mobi");

        books = new Vector<>();
        books.addElement("fb2"); // books
        books.addElement("epub");
        books.addElement("mobi");
        books.addElement("djvu");

        video = new Vector<>();
        video.addElement("avi");
        video.addElement("flv");
        video.addElement("mp4");
        video.addElement("3gp");
        video.addElement("wmv");
        video.addElement("avc");
        video.addElement("webp");

        audio = new Vector<>();
        audio.addElement("mp3");
        audio.addElement("aac");
        audio.addElement("wma");
        audio.addElement("waw");
        audio.addElement("real");
        audio.addElement("ogg");
        audio.addElement("real");

        zip = new Vector<>();
        zip.addElement("zip");
        zip.addElement("apk");
        zip.addElement("rar");

        image = new Vector<>();
        image.addElement("gif");
        image.addElement("png");
        image.addElement("jpg");

    }

    @Override public void run() {
        try {
            for (File file : dir)
                if (file.listFiles() != null && file.listFiles().length != 0)
                    findFile(file);
        } catch (InterruptedException ex) {
            //
        }
        onFileFound.onOver();
    }

    private static final int wait = 100;

    private void findFile(File dir) throws InterruptedException {
        for (File file : dir.listFiles())
            if (file.isDirectory()) {
                findFile(file);
            } else if (file.getName().length() >= 4){
                String name = file.getName();
                String type = name.substring(name.length() - 3);
                for (int i = name.length() - 1; i >= 0; i--)
                    if (name.charAt(i) == '.') {
                        type = name.substring(i + 1);
                        if (i != 0)
                            name = name.substring(0, i);
                        break;
                    }
                FileData fileData = null;
                String size;
                if (dataText.contains(type)) {
                    fileData = new FileData(file.getAbsolutePath(), name, type);
                } else if (fileView.contains(type)) {
                    fileData = new FileData(file.getAbsolutePath(), name, type);
                } else if (books.contains(type)) {
                    fileData = new FileData(file.getAbsolutePath(), name, type);
                } else if (video.contains(type)) {
                    fileData = new FileData(file.getAbsolutePath(), name, type);
                } else if (audio.contains(type)) {
                    fileData = new FileData(file.getAbsolutePath(), name, type);
                } else if (zip.contains(type)) {
                    fileData = new FileData(file.getAbsolutePath(), name, type);
                } else if (image.contains(type)) {
                    fileData = new FileData(file.getAbsolutePath(), name, type);
                }

                if (fileData != null) {
                    int s = (int) (file.length() / (1024 * 1024));
                    size = String.valueOf(s) + "mb";
                    if (s == 0) {
                        s = (int) file.length() / 1024;
                        size = String.valueOf(s) + "kb";
                        if (s == 0)
                            size = String.valueOf(file.length()) + "byte";
                    }
                    fileData.setSize(size);
                    onFileFound.onFileFounded(fileData);
                    sleep(wait);
                }

                /*else {
                    for (int i = name.length() - 1; i >= 0; i--)
                        if (name.charAt(i) == '.') {
                            type1 = name.substring(i);
                            onFileFound.onFileFounded(file.getAbsolutePath(), name, type1);
                            sleep(wait);
                        }
                }*/
            }
    }
}
