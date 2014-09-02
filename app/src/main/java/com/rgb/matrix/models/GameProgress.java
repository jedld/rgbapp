package com.rgb.matrix.models;

import android.content.Context;

import com.dayosoft.tiletron.app.MainActivity;
import com.google.android.gms.games.Games;
import com.rgb.matrix.Utils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by joseph on 9/2/14.
 */
public class GameProgress {

    private final MainActivity context;
    private static GameProgress instance;

    HashSet<String> cachedAchievements = new HashSet<String>();

    public static GameProgress getInstance(MainActivity context) {
        if (instance == null) {
            instance = new GameProgress(context);
        }
        return instance;
    }

    protected GameProgress(MainActivity context) {
        this.context = context;
        Utils.restoreAchievementState(context);
    }

    public void saveAchievement(String achievement) {
        cachedAchievements.add(achievement);
        Utils.saveAchievementState(context, cachedAchievements);
    }

    public void replayAchievements() {
        if (context.isSignedIn()) {
            for (String achievement : cachedAchievements) {
                Games.Achievements.unlock(context.getApiClient(), achievement);
            }
            cachedAchievements = new HashSet<String>();
            Utils.saveAchievementState(context, cachedAchievements);
        }
    }
}
