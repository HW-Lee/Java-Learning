package com.picasamanagerdemo;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.google.gdata.data.photos.PhotoEntry;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class PicasaPhotoInfo {
    private Bitmap imageBitmap;
    private String albumName;
    private String photoName;
    private String imageSrc;
    private PhotoEntry photoEntry;
    private Long imageSize;

    public static final String SMALL = "s72/";
    public static final String MEDIUM = "s144/";
    public static final String LARGE = "s288/";

    public PicasaPhotoInfo(Bitmap imageBitmap, String albumName, String photoName, String imageSrc,
            PhotoEntry photoEntry, Long imageSize) {
        this.imageBitmap = imageBitmap;
        this.albumName = albumName;
        this.photoName = photoName;
        this.imageSrc = imageSrc;
        this.photoEntry = photoEntry;
        this.imageSize = imageSize;
    }

    public PicasaPhotoInfo(Bitmap imageBitmap, String albumName, String photoName, String imageSrc, Long imageSize) {
        this(imageBitmap, albumName, photoName, imageSrc, null, imageSize);
    }

    /*
     * public PicasaPhotoInfo(null, albumName, imageSrc, imageSize)
     */
    public PicasaPhotoInfo(String albumName, String photoName, String imageSrc, PhotoEntry photoEntry, Long imageSize) {
        this(null, albumName, photoName, imageSrc, photoEntry, imageSize);
    }
    
    public PicasaPhotoInfo(String albumName, String photoName, String imageSrc, Long imageSize) {
        this(null, albumName, photoName, imageSrc, null, imageSize);
    }

    public Bitmap getImageBitmap(boolean isLoadingIfNull) {
        if (imageBitmap == null && isLoadingIfNull)
            imageBitmap = getImageByURL(imageSrc);
        return imageBitmap;
    }

    public Bitmap getImageBitmap() {
        return getImageBitmap(false);
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getPhotoName() {
        return photoName;
    }

    public PhotoEntry getPhotoEntry() {
        return photoEntry;
    }

    public String getAlbumNameToString() {
        return "Album: " + albumName;
    }

    public String getImageSrc() {
        return imageSrc;
    }

    public String getImageSrcToString() {
        return "Image Src: " + imageSrc;
    }

    public Long getImageSize() {
        return imageSize;
    }

    public String getImageSizeToString() {
        return "Image Size: " + imageSize + "Bytes";
    }

    public String getImageSrc(String Size) {
        if (Size.equals(LARGE) || Size.equals(MEDIUM) || Size.equals(SMALL)) {
            String[] SrcArray = imageSrc.split("/");
            String ImageFileName = SrcArray[SrcArray.length - 1];
            SrcArray = imageSrc.split(ImageFileName);
            String outputSrc = "";
            for (int i = 0; i < SrcArray.length; i++) {
                outputSrc += SrcArray[i];
            }
            outputSrc += Size + ImageFileName;
            return outputSrc;
        } else
            return imageSrc;
    }

    public static Bitmap getImageByURL(String imgurl) {
        URL url = null;
        try {
            url = new URL(imgurl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (url == null)
            return null;
        return getImageByURL(url);
    }

    public static Bitmap getImageByURL(URL url) {
        try {
            URLConnection connection = url.openConnection();

            HttpURLConnection httpConn = (HttpURLConnection) connection;
            httpConn.setRequestMethod("GET");
            httpConn.connect();

            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpConn.getInputStream();

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                return bitmap;
            }
            return null;
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
