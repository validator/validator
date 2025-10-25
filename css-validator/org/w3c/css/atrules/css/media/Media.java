package org.w3c.css.atrules.css.media;


import java.util.ArrayList;

public class Media {
    boolean only;
    boolean not;
    String media;
    ArrayList<MediaFeature> features;
    String _ts = null;

    public Media() {
    }

    public Media(String media) {
        this.media = media;
    }

    public void setOnly(boolean only) {
        this.only = only;
    }

    public boolean getOnly() {
        return not;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

    public boolean getNot() {
        return not;
    }

    public void setMedia(String media) {
        this.media = media;
        _ts = null;
    }

    public String getMedia() {
        return media;
    }

    public void addFeature(MediaFeature mf) {
        if (features == null) {
            features = new ArrayList<MediaFeature>();
        }
        features.add(mf);
        _ts = null;
    }

    public String toString() {
        // simple case, return the media string
        if (!only && !not && features == null) {
            return media;
        }
        if (_ts != null) {
            return _ts;
        }
        StringBuilder sb = new StringBuilder();
        String combinator;
        if (only) {
            sb.append("only ");
        } else if (not) {
            sb.append("not ");
        }
        // special case "media and (...)" or directly "(...)"
        if (media != null) {
            sb.append(media);
        }
        if (features != null) {
            for (MediaFeature mf : features) {
                combinator = mf.getCombinator();
                if (combinator != null) {
                    sb.append(' ').append(combinator);
                }
                sb.append(" (").append(mf.toString()).append(')');
            }
        }
        _ts = sb.toString();
        return _ts;
    }
}
