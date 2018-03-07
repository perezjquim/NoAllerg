package com.perezjquim.noallerg.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.util.Log;

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
            }
            db = SQLiteDatabase.openDatabase(
                    Environment.getExternalStorageDirectory() + "/noallerg/" + DB_NAME,
                    null,
                    SQLiteDatabase.CREATE_IF_NECESSARY);
            createDatabase();
        }
        catch(Exception e)
        { e.printStackTrace(); }
    }

    private static void createDatabase()
    {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + MARKER_TABLE + " ("+
                                 "  `id` INTEGER NOT NULL PRIMARY KEY)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + MARKER_COOR_TABLE + " ("+
                                "`marker_id` INTEGER NOT NULL PRIMARY KEY,"+
                                "`lat` DOUBLE NOT NULL,"+
                                "`long` DOUBLE NOT NULL,"+
                                "FOREIGN KEY (`marker_id`)"+
                                "REFERENCES `marker` (`id`)"+
                                ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + MARKER_INFO_TABLE + " ("+
                "`marker_id` INTEGER NOT NULL PRIMARY KEY,"+
                "`title` VARCHAR(45) NOT NULL,"+
                "`subtitle` VARCHAR(45) NOT NULL,"+
                "FOREIGN KEY (`marker_id`)"+
                "REFERENCES `marker` (`id`)"+
                ")");
    }

    public static void clearDatabase()
    {
        db.execSQL("DELETE FROM " + MARKER_TABLE);
        db.execSQL("DELETE FROM " + MARKER_COOR_TABLE);
        db.execSQL("DELETE FROM " + MARKER_INFO_TABLE);
    }

    public static void insertMarker(String title, String subtitle, double latitude, double longitude)
    {
        db.beginTransaction();
        try
        {
            insert("INSERT INTO " + MARKER_TABLE +" VALUES (NULL)");
            insert("INSERT INTO " + MARKER_COOR_TABLE +" (lat,long) VALUES (?,?)",""+latitude,""+longitude);
            insert("INSERT INTO " + MARKER_INFO_TABLE +" (title,subtitle) VALUES (?,?)",title,subtitle);
            db.setTransactionSuccessful();
        }
        catch(InsertFailedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            db.endTransaction();
        }
    }

    public static void beginTransaction()
    { db.beginTransaction(); }

    public static void insert(String sql, String ... args) throws InsertFailedException
    {
        SQLiteStatement statement = db.compileStatement(sql);
        statement.bindAllArgsAsStrings(args);
        long state = statement.executeInsert();
        if (state == -1)
            throw new InsertFailedException(args);
    }
}
