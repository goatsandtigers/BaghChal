package com.goatsandtigers.goatsandtigers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.preference.PreferenceManager;

public class GameState {

    public static final int NUM_LEVELS = 7;
    private static final String KEY_LEVEL = "KEY_LEVEL";
    public static final Point UNUSED_GOAT_POINT = new Point(-1, -1);
    public static final Point TAKEN_GOAT_POINT = new Point(-2, -2);
    private static final String KEY_USER_PLAYS_AS_GOATS = "KEY_USER_PLAYS_AS_GOATS";
    private static int level = 1;
    private static Boolean userPlaysAsGoats;

    public static boolean isUserGoats(Context context) {
        if (userPlaysAsGoats == null) {
            userPlaysAsGoats = retrieveUserPlaysAsGoats(context);
        }
        return userPlaysAsGoats;
    }

    private static boolean retrieveUserPlaysAsGoats(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_USER_PLAYS_AS_GOATS, true);
    }

    public static void storePlayAsGoats(Context context, boolean userPlaysAsGoats) {
        GameState.userPlaysAsGoats = userPlaysAsGoats;
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_USER_PLAYS_AS_GOATS, userPlaysAsGoats).commit();
    }

    public static void restart(Context context) {
        level = 1;
        saveLevel(context);
        eraseSavedPiecePositions(context);
    }

    private static void saveLevel(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(KEY_LEVEL, level).commit();
    }

    public static int getLevel(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(KEY_LEVEL, 1);
    }

    public static void incrementLevel(Context context) {
        level++;
        if (level > NUM_LEVELS) {
            level = 1;
        }
        saveLevel(context);
        eraseSavedPiecePositions(context);
    }

    public static boolean isGameCompleted() {
        return level == 1;
    }

    public static int getTigerImageRes(Context context) {
        switch (getLevel(context)) {
        case 1:
            return R.drawable.i1_brownmoth;
        case 2:
            return R.drawable.i2_bunny;
        case 3:
            return R.drawable.i3_crab;
        case 4:
            return R.drawable.i4_snail;
        case 5:
            return R.drawable.i5_fishgold;
        case 6:
            return R.drawable.i6_fishblue;
        default:
            return R.drawable.i7_coolshark;
        }
    }

    public static void saveGoatPositions(Context context, Point goatBoardCoords[]) {
        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        for (int i = 0; i < goatBoardCoords.length; i++) {
            Point p = goatBoardCoords[i] != null ? goatBoardCoords[i] : UNUSED_GOAT_POINT;
            prefsEditor.putInt(buildKeyForGoatX(i), p.x);
            prefsEditor.putInt(buildKeyForGoatY(i), p.y);
            // TODO save which goats are taken
        }
        prefsEditor.commit();
    }

    public static String buildKeyForGoatX(int index) {
        return "KEY_GOAT_X_" + index;
    }

    public static String buildKeyForGoatY(int index) {
        return "KEY_GOAT_Y_" + index;
    }

    public static void saveTigerPositions(Context context, Point tigerBoardCoords[]) {
        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        for (int i = 0; i < tigerBoardCoords.length; i++) {
            String keyX = buildKeyForTigerX(i);
            String keyY = buildKeyForTigerY(i);
            prefsEditor.putInt(keyX, tigerBoardCoords[i].y);
            prefsEditor.putInt(keyY, tigerBoardCoords[i].x);
        }
        prefsEditor.commit();
    }

    public static String buildKeyForTigerX(int index) {
        return "KEY_TIGER_X_" + index;
    }

    public static String buildKeyForTigerY(int index) {
        return "KEY_TIGER_Y_" + index;
    }

    public static Point[] getSavedTigerPositions(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Point tigerPositions[] = new Point[4];
        Point defaultTigerPositions[] = getDefaultTigerPositions();
        for (int i = 0; i < 4; i++) {
            float x = prefs.getInt(buildKeyForTigerX(i), defaultTigerPositions[i].x);
            float y = prefs.getInt(buildKeyForTigerY(i), defaultTigerPositions[i].y);
            tigerPositions[i] = new Point((int) x, (int) y);
        }
        return tigerPositions;
    }

    private static Point[] getDefaultTigerPositions() {
        return new Point[] { new Point(0, 0), new Point(4, 0), new Point(0, 4), new Point(4, 4) };
    }

    public static Point[] getSavedGoatPositions(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Point goatPositions[] = new Point[20];
        for (int i = 0; i < 20; i++) {
            float x = prefs.getInt(buildKeyForGoatX(i), UNUSED_GOAT_POINT.x);
            float y = prefs.getInt(buildKeyForGoatY(i), UNUSED_GOAT_POINT.y);
            if (x == UNUSED_GOAT_POINT.x || y == UNUSED_GOAT_POINT.y) {
                goatPositions[i] = UNUSED_GOAT_POINT;
            } else {
                goatPositions[i] = new Point((int) x, (int) y);
            }
        }
        return goatPositions;
    }

    public static void eraseSavedPiecePositions(Context context) {
        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        for (int i = 0; i < 4; i++) {
            prefsEditor.remove(buildKeyForTigerX(i));
            prefsEditor.remove(buildKeyForTigerY(i));
        }
        for (int i = 0; i < 20; i++) {
            prefsEditor.remove(buildKeyForGoatX(i));
            prefsEditor.remove(buildKeyForGoatY(i));
        }
        prefsEditor.commit();
    }
}
