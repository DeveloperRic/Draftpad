package com.rictacius.draftpad;

import android.content.Context;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import xyz.victorolaitan.easyjson.DatabaseHelper;
import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.EasyJSONException;
import xyz.victorolaitan.easyjson.JSONElement;

/**
 * Created by Victor on 12/12/2017.
 */

class ActivityManager {
    private static DashboardClass dashboardClass;
    private static File appDataFile;
    private static EasyJSON appData;
    private static DraftpadSettings settings;
    private static NoteManager noteManager;

    static ErrorCode init(Context context) {
        appDataFile = new File(context.getFilesDir(), "appData.txt");
        try {
            if (appDataFile.exists()) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(appDataFile));
                    System.out.println(reader.readLine());

                    appData = EasyJSON.open(appDataFile);

                    settings = new DraftpadSettings(appData.search("settings"));
                    Pair<NoteManager, ErrorCode> result = NoteManager.createNoteManager(context);
                    if (result.second == ErrorCode.NO_ERROR) {
                        noteManager = result.first;
                        return ErrorCode.NO_ERROR;
                    } else {
                        return result.second;
                    }
                } catch (IOException | EasyJSONException e) {
                    e.printStackTrace();
                    return ErrorCode.FILE_NO_ACCESS;
                }
            } else {
                appData = EasyJSON.create(appDataFile);
                settings = new DraftpadSettings(appData);
                Pair<NoteManager, ErrorCode> result = NoteManager.createNoteManagerFromLegacy();
                if (result.second == ErrorCode.NO_ERROR) {
                    noteManager = result.first;
                } else {
                    return result.second;
                }
                if (!requestSave()) {
                    return ErrorCode.SAVE_ERROR;
                }
            }
            return ErrorCode.NO_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
            return ErrorCode.UNKNOWN_ERROR;
        }
    }

    static boolean requestSave() {
        boolean saved = true;
        try {
            if (noteManager != null) {
                saved = noteManager.saveAll();
            }
            settings.requestSave();
            appData.save();
            BufferedReader reader = new BufferedReader(new FileReader(appDataFile));
            System.out.println(reader.readLine());
        } catch (EasyJSONException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ignored) {
        }
        return saved;
    }

    static Dashboard getDashboardActivity() {
        return dashboardClass.activity;
    }

    static NoteManager getNoteManager() {
        return noteManager;
    }

    static void setDashboardClass(Dashboard instance) {
        dashboardClass = new DashboardClass(instance);
    }

    public static class DashboardClass {
        Dashboard activity;

        DashboardClass(Dashboard instance) {
            activity = instance;
        }

    }

    static DraftpadSettings getSettings() {
        return settings;
    }

    public static class DraftpadSettings {
        private JSONElement settingsData;

        public boolean sortNotes = true;

        DraftpadSettings(EasyJSON appData) {
            settingsData = appData.putStructure("settings");
        }

        DraftpadSettings(JSONElement settingsData) throws EasyJSONException {
            this.settingsData = settingsData;
            DatabaseHelper.deserializeInstance(this, settingsData);
        }

        void requestSave() throws EasyJSONException {
            settingsData.combine(DatabaseHelper.serializeInstance(this));
        }

        void toggleSortNotes() {
            sortNotes = !sortNotes;
        }

    }

}
