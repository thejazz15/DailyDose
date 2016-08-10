package com.thejazz.dailydose;

/**
 * Created by TheJazz on 04/08/16.
 */
public class TvShow {
    private String id;
    private String name;
    private String img_url;

    public TvShow(String name, String img_url, String id){
        this.id = id;
        this.name = name;
        this.img_url = img_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgUrl() {
        return img_url;
    }

    public void setImgUrl(String img_url) {
        this.img_url = img_url;
    }
}
