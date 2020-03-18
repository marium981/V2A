package com.example.v2a;

public class Song {
    String name;
    String link;
    Song(String name, String link){
        this.name = name;
        this.link = link;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setLink(String l){
        this.link = l;
    }

    public String getName(){
        return name;
    }

    public String getLink(){
        return link;
    }
}
