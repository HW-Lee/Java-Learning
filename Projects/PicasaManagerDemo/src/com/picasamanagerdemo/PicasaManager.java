package com.picasamanagerdemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.photos.AlbumFeed;
import com.google.gdata.data.photos.GphotoEntry;
import com.google.gdata.data.photos.PhotoEntry;

public class PicasaManager {

    private static String TAG = "PicasaManager";
    private String userName;
    private String userPass;
    private String targetUser = "";
    private String targetUserNick;

    private int albumNum = 0;
    private int MAX_PHOTOS = 100;
    private PicasawebService mService = new PicasawebService(TAG);

    /**
     * Data Structures
     */

    // PicasaAlbum <albumName, albumID>
    private HashMap<String, String> PicasaAlbum = new HashMap<String, String>();

    // PicasaAlbumName <albumName>
    private ArrayList<String> PicasaAlbumName = new ArrayList<String>();

    // VideoNameSet <VideoThumbnailSrc, VideoSrc[]>
    private HashMap<String, String[]> VideoSrcMap = new HashMap<String, String[]>();

    /**
     * Create object without login. <style>.note{color:red}</style>
     */
    public PicasaManager() {
        this(null, null);
    }

    /**
     * Create object trying login.
     */
    public PicasaManager(String userName, String userPass) {
        this.userName = userName;
        this.userPass = userPass;
        Connect();
    }

    private void Connect() {
        Log.d(TAG, "Start Connection");
        try {
            if (userName != null && userPass != null) {
                Log.d(TAG, "setUserCredentials");
                mService.setUserCredentials(userName, userPass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set up the user account who you want to retrieve data from.
     * 
     * @param targetUser
     *            User name/User ID
     */
    public void setTargetUser(String targetUser) {
        this.targetUser = targetUser;
    }

    /**
     * Set up the number limitation of the retrieved photos/videos.
     * 
     * @param MAX_PHOTOS
     *            The value of limitation
     */
    public void setMaxPhoto(int MAX_PHOTOS) {
        this.MAX_PHOTOS = MAX_PHOTOS;
    }

    /**
     * After calling the function 'getAlbumInfo()', you can get album names by
     * calling it, or you will get nothing.
     * 
     * @return Album names in an array list
     */
    public ArrayList<String> getAlbumNameArray() {
        return PicasaAlbumName;
    }

    /**
     * Used for initializing/refreshing the album information.<br/>
     * <font class="note">Please do not use on the UI thread, and suggest
     * that using it at beginning.</font>
     * 
     * @return HashMap with the key of album name and the value of album id
     */
    public HashMap<String, String> getAlbumInfo() {
        PicasaAlbum.clear();
        PicasaAlbumName.clear();
        if (targetUser == null)
            return null;
        URL feedUrl;
        try {
            feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/" + targetUser + "?kind=album");
            com.google.gdata.data.photos.UserFeed mUserFeed = mService.getFeed(feedUrl,
                    com.google.gdata.data.photos.UserFeed.class);
            targetUserNick = mUserFeed.getNickname();
            Log.d(TAG, "userName: " + targetUserNick);
            albumNum = mUserFeed.getTotalResults();
            Log.d(TAG, "total results: " + albumNum);

            for (GphotoEntry<?> albumEntry : mUserFeed.getEntries()) {
                String albumEntryTitle = albumEntry.getTitle().getPlainText();
                String albumID = albumEntry.getGphotoId();
                PicasaAlbum.put(albumEntryTitle, albumID);
                PicasaAlbumName.add(albumEntryTitle);
                Log.d(TAG, "Add album: " + albumEntryTitle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return PicasaAlbum;
    }

    /**
     * Retrieving all photos from the designated album.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @param albumName
     *            Name of the album.
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getPhotoByAlbumName(String albumName) {
        return getPhotoByAlbumName(albumName, -1);
    }
    
    /**
     * Retrieving all videos from the designated album.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @param albumName
     *            Name of the album.
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getVideoByAlbumName(String albumName) {
        return getPhotoByAlbumName(albumName, -1);
    }

    private ArrayList<PicasaPhotoInfo> getPhotoByAlbumName(String albumName, int photoNum, int photoOffset, String type) {
        int limitation = photoNum;
        ArrayList<PicasaPhotoInfo> PicasaPhoto = new ArrayList<PicasaPhotoInfo>();
        if (photoNum == 0)
            return PicasaPhoto;
        else if (photoNum == -1)
            limitation = Integer.MAX_VALUE; // Quasi-infinity
        else
            Log.d(TAG, "getPhotoByAlbumName with photoNum " + limitation);
        String targetID = PicasaAlbum.get(albumName);
        try {
            URL albumfeedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/" + targetUser + "/albumid/"
                    + targetID);
            AlbumFeed mAlbumFeed = mService.getFeed(albumfeedUrl, AlbumFeed.class);
            Log.d(TAG, "number of photos: " + mAlbumFeed.getEntries().size());

            int count = 0;
            for (GphotoEntry<?> photo : mAlbumFeed.getEntries()) {
                if (count < photoOffset)
                    count++;
                else {
                    PhotoEntry photoEntry = new PhotoEntry(photo);
                    String photoName = photoEntry.getTitle().getPlainText();
                    String imageSrc = photoEntry.getMediaThumbnails().get(0).getUrl();

                    boolean condition;
                    if (type.equals("photo"))
                        condition = photoEntry.getVideoStatus() == null; // Retrieve_photos
                    else
                        condition = photoEntry.getVideoStatus() != null; // Retrieve_videos

                    if (condition) {
                        String[] imageSrcArr = imageSrc.split("/");
                        imageSrc = "";
                        for (int i = 0; i < imageSrcArr.length - 1; i++)
                            if (i != imageSrcArr.length - 2)
                                imageSrc += imageSrcArr[i] + "/";
                            else
                                imageSrc += imageSrcArr[i + 1];
                        PicasaPhoto.add(new PicasaPhotoInfo(albumName, photoName, imageSrc, photoEntry, photoEntry
                                .getSize()));

                        if (PicasaPhoto.size() == limitation) {
                            Log.d(TAG, "Reached photoNum");
                            return PicasaPhoto;
                        }
                    }
                }
            }
            Log.d(TAG, "Get Completely");
            return PicasaPhoto;
        } catch (Exception e) {
            e.printStackTrace();
            return PicasaPhoto;
        }
    }

    /**
     * Retrieving some photos from the designated album.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @param albumName
     *            Name of the album.
     * @param photoNum
     *            Number of photos you want to retrieve.
     * @param photoOffset
     *            Number of photos you want to neglect while retrieving.
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getPhotoByAlbumName(String albumName, int photoNum, int photoOffset) {
        return getPhotoByAlbumName(albumName, photoNum, photoOffset, "photo");
    }

    /**
     * Retrieving some videos from the designated album.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @param albumName
     *            Name of the album.
     * @param videoNum
     *            Number of videos you want to retrieve.
     * @param videoOffset
     *            Number of videos you want to neglect while retrieving.
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getVideoByAlbumName(String albumName, int videoNum, int videoOffset) {
        return getPhotoByAlbumName(albumName, videoNum, videoOffset, "video");
    }

    /**
     * Retrieving some photos from the designated album.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @param albumName
     *            Name of the album.
     * @param photoNum
     *            Number of photos you want to retrieve.
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getPhotoByAlbumName(String albumName, int photoNum) {
        return getPhotoByAlbumName(albumName, photoNum, 0);
    }

    /**
     * Retrieving some videos from the designated album.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @param albumName
     *            Name of the album.
     * @param videoNum
     *            Number of videos you want to retrieve.
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getVideoByAlbumName(String albumName, int videoNum) {
        return getVideoByAlbumName(albumName, videoNum, 0);
    }

    /**
     * Retrieving some photos from some designated albums.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @param offset
     *            Number of albums you want to neglect while retrieving.
     * @param PicasaAlbumName
     *            Names of the albums.
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getPhotoTillMaximum(int offset, ArrayList<String> PicasaAlbumName) {

        int photoNum = 0;
        int count = 0;

        ArrayList<PicasaPhotoInfo> PicasaPhotoChain = new ArrayList<PicasaPhotoInfo>();
        for (String albumName : PicasaAlbumName) {
            if (count < offset)
                count++;
            else {
                PicasaPhotoChain.addAll(getPhotoByAlbumName(albumName, MAX_PHOTOS - photoNum));
                photoNum = PicasaPhotoChain.size();
            }
        }
        return PicasaPhotoChain;
    }

    /**
     * Retrieving some videos from some designated albums.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @param offset
     *            Number of albums you want to neglect while retrieving.
     * @param PicasaAlbumName
     *            Names of the albums.
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getVideoTillMaximum(int offset, ArrayList<String> PicasaAlbumName) {

        int photoNum = 0;
        int count = 0;

        ArrayList<PicasaPhotoInfo> PicasaPhotoChain = new ArrayList<PicasaPhotoInfo>();
        for (String albumName : PicasaAlbumName) {
            if (count < offset)
                count++;
            else {
                PicasaPhotoChain.addAll(getVideoByAlbumName(albumName, MAX_PHOTOS - photoNum));
                photoNum = PicasaPhotoChain.size();
            }
        }
        return PicasaPhotoChain;
    }

    /**
     * Retrieving some photos from search account.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @param offset
     *            Number of albums you want to neglect while retrieving.
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getPhotoTillMaximum(int offset) {
        if (this.PicasaAlbumName.size() == 0)
            getAlbumInfo();
        return getPhotoTillMaximum(offset, this.PicasaAlbumName);
    }

    /**
     * Retrieving some videos from search account.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @param offset
     *            Number of albums you want to neglect while retrieving.
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getVideoTillMaximum(int offset) {
        if (this.PicasaAlbumName.size() == 0)
            getAlbumInfo();
        return getVideoTillMaximum(offset, this.PicasaAlbumName);
    }

    /**
     * Retrieving photos from the designated album until retrieving entirely or
     * number of the data reaching maximum.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @param albumName
     *            Name of the album.
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getPhotoTillMaximum(String albumName) {
        int offset = Integer.MAX_VALUE; // Quasi-infinity
        for (int i = 0; i < PicasaAlbumName.size(); i++)
            if (PicasaAlbumName.get(i).equals(albumName)) {
                offset = i;
                break;
            }
        if (offset == Integer.MAX_VALUE)
            Log.d(TAG, "Search nothing");
        return getPhotoTillMaximum(offset);
    }

    /**
     * Retrieving videos from the designated album until retrieving entirely or
     * number of the data reaching maximum.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @param albumName
     *            Name of the album.
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getVideoTillMaximum(String albumName) {
        int offset = Integer.MAX_VALUE; // Quasi-infinity
        for (int i = 0; i < PicasaAlbumName.size(); i++)
            if (PicasaAlbumName.get(i).equals(albumName)) {
                offset = i;
                break;
            }
        if (offset == Integer.MAX_VALUE)
            Log.d(TAG, "Search nothing");
        return getVideoTillMaximum(offset);
    }

    /**
     * Retrieving photos from some designated albums until the number reaching
     * maximum.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @param PicasaAlbumName
     *            Name of the albums.
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getPhotoTillMaximum(ArrayList<String> PicasaAlbumName) {
        return getPhotoTillMaximum(0, PicasaAlbumName);
    }

    /**
     * Retrieving videos from some designated albums until the number reaching
     * maximum.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @param PicasaAlbumName
     *            Name of the albums.
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getVideoTillMaximum(ArrayList<String> PicasaAlbumName) {
        return getVideoTillMaximum(0, PicasaAlbumName);
    }

    /**
     * Retrieving photos from search account until the number reaching maximum.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getPhotoTillMaximum() {
        return getPhotoTillMaximum(0);
    }

    /**
     * Retrieving videos from search account until the number reaching maximum.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getVideoTillMaximum() {
        return getVideoTillMaximum(0);
    }

    /*
     * Just get the most recently uploaded photos from the "offset"th photo
     */
    private ArrayList<PicasaPhotoInfo> getRecentPhoto(int offset, int photoNum, String type) {
        ArrayList<PicasaPhotoInfo> recentlyPhotoArray = new ArrayList<PicasaPhotoInfo>();
        int count = 0;
        if (photoNum <= 0)
            return null;
        try {
            URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/" + targetUser + "?kind=photo");
            AlbumFeed mAlbumFeed = mService.getFeed(feedUrl, AlbumFeed.class);
            Log.d(TAG, "Recent photos number: " + mAlbumFeed.getEntries().size());
            for (GphotoEntry<?> photo : mAlbumFeed.getEntries()) {
                if (count < offset)
                    count++;
                else {
                    PhotoEntry photoEntry = new PhotoEntry(photo);
                    String photoName = photoEntry.getTitle().getPlainText();

                    boolean condition;
                    if (type.equals("photo"))
                        condition = photoEntry.getVideoStatus() == null; // Retrieve_photos
                    else
                        condition = photoEntry.getVideoStatus() != null; // Retrieve_videos

                    if (condition) {
                        String imageSrc = photoEntry.getMediaThumbnails().get(0).getUrl();
                        String[] imageSrcArr = imageSrc.split("/");
                        imageSrc = "";

                        for (int i = 0; i < imageSrcArr.length - 1; i++)
                            // Image Src Parsing
                            if (i != imageSrcArr.length - 2)
                                imageSrc += imageSrcArr[i] + "/";
                            else
                                imageSrc += imageSrcArr[i + 1];

                        String name = null;
                        for (String albumName : PicasaAlbumName)
                            if (PicasaAlbum.get(albumName).equals(photoEntry.getAlbumId()))
                                name = albumName;
                        recentlyPhotoArray.add(new PicasaPhotoInfo(name, photoName, imageSrc, photoEntry, photoEntry
                                .getSize()));
                        if (recentlyPhotoArray.size() == photoNum) {
                            Log.d(TAG, "Reached photoNum");
                            return recentlyPhotoArray;
                        }
                    }
                }
            }
            Log.d(TAG, "Array length : " + recentlyPhotoArray.size());
            return recentlyPhotoArray;
        } catch (Exception e) {
            e.printStackTrace();
            return recentlyPhotoArray;
        }
    }

    /**
     * Retrieving some photos which are the most recently uploaded.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @param offset
     *            Number of photos you want to neglect while retrieving.
     * @param photoNum
     *            Number of photos you want to retrieve.
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getRecentPhoto(int offset, int photoNum) {
        return getRecentPhoto(offset, photoNum, "photo");
    }

    /**
     * Retrieving some videos which are the most recently uploaded.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @param offset
     *            Number of videos you want to neglect while retrieving.
     * @param photoNum
     *            Number of videos you want to retrieve.
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getRecentVideo(int offset, int videoNum) {
        return getRecentPhoto(offset, videoNum, "video");
    }

    /**
     * Retrieving all photos which are the most recently uploaded.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getAllRecentPhoto() {
        return getRecentPhoto(0, Integer.MAX_VALUE);
    }

    /**
     * Retrieving all videos which are the most recently uploaded.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @return An array list contains retrieved data.
     */
    public ArrayList<PicasaPhotoInfo> getAllRecentVideo() {
        return getRecentVideo(0, Integer.MAX_VALUE);
    }

    /**
     * Parsing the source of video.<br/>
     * <font class="note">Please do not use on the UI thread.</font>
     * 
     * @param info
     *            PicasaPhotoInfo variable which is video.
     * @return Two strings in a string array, with the fist one and the second.
     *         one are x-flash and other-format respectively.
     * @throws ClientProtocolException
     * @throws IOException
     */
    public String[] getVideoSrc(PicasaPhotoInfo info) throws ClientProtocolException, IOException {
        PhotoEntry photoEntry = info.getPhotoEntry();
        if (photoEntry.getVideoStatus() != null) { // Video_Parser
            if (VideoSrcMap.containsKey(info.getImageSrc())) {
                Log.i(TAG, "Video Hit");
                return VideoSrcMap.get(info.getImageSrc());
            } else {
                Log.i(TAG, "Video Miss");
                String htmlCode = getHtmlCode(photoEntry.getHtmlLink().getHref());
                Log.v(TAG, "HTML Get");

                String regExp = "http://redirector\\.googlevideo\\.com/[^\"]+" + ".*?"
                        + "http://redirector\\.googlevideo\\.com/[^\"]+" + ".*?"
                        + "https://lh[\\d]\\.googleusercontent\\.com/.*?[^\"\']+";
                Pattern pattern = Pattern.compile(regExp);
                Matcher matcher = pattern.matcher(htmlCode);
                Log.v(TAG, "Pattern Matched");
                ArrayList<String> dataArr = new ArrayList<String>();

                while (matcher.find()) {
                    String matchString = matcher.group();
                    String regExpThumbnail = "https://lh[\\d]\\.googleusercontent\\.com/.*?[^\"\']+";
                    String regExpVideo = "http://redirector\\.googlevideo\\.com[^\"]+";
                    Pattern patternThumbnail = Pattern.compile(regExpThumbnail);
                    Pattern patternVideo = Pattern.compile(regExpVideo);
                    Matcher matcherData = patternThumbnail.matcher(matchString);
                    Log.v(TAG, "Thumbnail Matched");

                    if (matcherData.find())
                        dataArr.add(matcherData.group().replaceAll("/s[\\d]+/", "/"));
                    matcherData = patternVideo.matcher(matchString);
                    Log.v(TAG, "Source Matched");

                    if (matcherData.find()) // Get the first two elements
                        dataArr.add(matcherData.group());
                    if (matcherData.find())
                        dataArr.add(matcherData.group());

                }
                for (int i = 0; i < dataArr.size() / 3; i++) {
                    String[] Arr = { dataArr.get(3 * i + 1), dataArr.get(3 * i + 2) };
                    VideoSrcMap.put(dataArr.get(3 * i), Arr);
                }
                if (VideoSrcMap.containsKey(info.getImageSrc())) {
                    Log.i(TAG, "Src Hit");
                    return VideoSrcMap.get(info.getImageSrc());
                } else {
                    for (String key : VideoSrcMap.keySet()) {
                        String keyProcessed = key.split("/")[key.split("/").length - 1];
                        keyProcessed = keyProcessed.split("\\.")[keyProcessed.split("\\.").length - 2];
                        String nameCompared = info.getPhotoName();
                        nameCompared = nameCompared.split("\\.")[nameCompared.split("\\.").length - 2];
                        Log.d(TAG, "nameCompared : " + nameCompared);
                        Log.d(TAG, "keyProcessed : " + keyProcessed);
                        if (nameCompared.equals(keyProcessed)) {
                            Log.i(TAG, "Name Hit");
                            return VideoSrcMap.get(key);
                        }
                    }
                    Log.i(TAG, "Name Miss");
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    /**
     * Reset the VideoSrcMap.
     */
    public void clearVideoSrc() {
        VideoSrcMap.clear();
    }

    private String getHtmlCode(String href) throws ClientProtocolException, IOException {
        Log.d(TAG, "Href : " + href);
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(href);
        HttpResponse response = client.execute(get);

        String html = "";
        InputStream in = response.getEntity().getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder str = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            str.append(line);
        }
        in.close();
        html = str.toString();
        return html;
    }

}
