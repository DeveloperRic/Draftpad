package com.rictacius.draftpad;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Victor Olaitan on 19/02/2017.
 */

public class Note {
    public UUID id;
    public String title;
    public String text;
    public Date created = new Date();
    public Date updated = new Date();

    public Note() {
    }
}
