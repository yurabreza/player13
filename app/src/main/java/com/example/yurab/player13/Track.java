package com.example.yurab.player13;

/**
 * Created by yurab on 27.03.2016.
 */
public final class Track {
    private int id;
    private String title;
    private String artist;
    private String duration;
    private String path;

    public Track(int id,String title,String artist,String duration,String path){
        setId(id);
        setTitle(title);
        setArtist(artist);
        setDuration(duration);
        setPath(path);
    }



    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDuration() {
        return duration;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
