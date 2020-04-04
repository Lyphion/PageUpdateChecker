package me.lyphium.pageupdatechecker.parser;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PageUpdate implements Serializable {

    private static final long serialVersionUID = -2345864838910431089L;

    private final int id;
    private final String url;

    private long lastUpdate;
    private String content;

    public PageUpdate(int id, String url, long lastUpdate, String content) {
        this.id = id;
        this.url = url;
        this.lastUpdate = lastUpdate;
        this.content = content;
    }

    @Override
    public String toString() {
        return "PageUpdate{" +
                "url='" + url + '\'' +
                ", lastUpdate=" + lastUpdate +
                ", content='" + content + '\'' +
                '}';
    }

}