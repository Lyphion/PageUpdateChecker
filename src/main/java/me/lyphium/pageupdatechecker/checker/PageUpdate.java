package me.lyphium.pageupdatechecker.checker;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PageUpdate implements Serializable {

    private static final long serialVersionUID = -2345864838910431089L;

    private final int id;
    private final String name;
    private final String url;

    private long lastUpdate;
    private String content;

    public PageUpdate(int id, String name, String url) {
        this(id, name, url, 0, "");
    }

    public PageUpdate(int id, String name, String url, long lastUpdate, String content) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.lastUpdate = lastUpdate;
        this.content = content;
    }

    @Override
    public String toString() {
        return "PageUpdate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", lastUpdate=" + lastUpdate +
                ", content='" + content + '\'' +
                '}';
    }

}