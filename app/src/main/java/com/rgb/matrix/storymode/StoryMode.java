package com.rgb.matrix.storymode;

import android.content.Context;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.events.Event;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by joseph on 5/5/14.
 */
public class StoryMode {

    private final String levelDir;
    private final Context context;

    public StoryMode(Context context, String levelDir) {
        this.levelDir = levelDir;
        this.context = context;
    }

    public Level loadLevel(String levelName) {
        Level level = new Level();
        Yaml yaml = new Yaml();
        try {
            InputStream is = context.getAssets().open("levels/" + levelName + ".yml");

        } catch (IOException e) {
            e.printStackTrace();

        }

        return level;
    }

}
