package com.perezjquim.noallerg.util;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;

public abstract class DatabaseManager
{
        private static final String DB_NAME = "noallerg";
        private static final String MARKER_TABLE = "marker";
        private static final String MARKER_COOR_TABLE = "marker_coor";
        private static final String MARKER_INFO_TABLE = "marker_info";
        private static SQLiteDatabase db;

        public static void initDatabase()
        {
            try
            {
                File dbFolder = new File(Environment.getExternalStorageDirectory(), "/noallerg");
                if(!dbFolder.exists())
                {
                    if (!dbFolder.mkdir())
                    { throw new Exception("Could not create database folder"); }
                }
                File dbFile = new File(dbFolder, DB_NAME);
                if(!dbFile.exists())
                {
                    dbFile.createNewFile();
                    db = SQLiteDatabase.openDatabase(
                            Environment.getExternalStorageDirectory() + "/noallerg/" + DB_NAME,
                            null,
                            SQLiteDatabase.CREATE_IF_NECESSARY);
                    createDatabase(dbFile.getAbsolutePath());
                }
                else
                {
                    db = SQLiteDatabase.openDatabase(
                            Environment.getExternalStorageDirectory() + "/noallerg/" + DB_NAME,
                            null,
                            SQLiteDatabase.CREATE_IF_NECESSARY);
                }
            }
            catch(Exception e)
            { e.printStackTrace(); }
        }

    private static void createDatabase(String path)
    {
        db.execSQL("CREATE TABLE `marker` (" +
                                 "  `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT)");

        db.execSQL("CREATE TABLE `marker_coor` ("+
                                "`marker_id` INTEGER NOT NULL PRIMARY KEY,"+
                                "`lat` DOUBLE NOT NULL,"+
                                "`long` DOUBLE NOT NULL,"+
                                "FOREIGN KEY (`marker_id`)"+
                                "REFERENCES `marker` (`id`)"+
                                ")");

        db.execSQL("CREATE TABLE `marker_info` ("+
                "`marker_id` INTEGER NOT NULL PRIMARY KEY,"+
                "`title` VARCHAR(45) NOT NULL,"+
                "`subtitle` VARCHAR(45) NOT NULL,"+
                "FOREIGN KEY (`marker_id`)"+
                "REFERENCES `marker` (`id`)"+
                ")");
    }
}
