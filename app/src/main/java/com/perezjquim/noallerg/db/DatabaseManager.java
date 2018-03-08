package com.perezjquim.noallerg.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;

import java.io.File;

public abstract class DatabaseManager
{
    private static final String DB_NAME = "noallerg";
    private static final String MARKER_TABLE = "marker";
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
        db.execSQL("CREATE TABLE IF NOT EXISTS " + MARKER_TABLE + 
        		      " ("+
                                "`id` INTEGER NOT NULL PRIMARY KEY," +
                                "`title` VARCHAR(45) NOT NULL," +
                                "`subtitle` VARCHAR(45) NOT NULL,"+
                                "`latitude` DOUBLE NOT NULL,"+
                                "`longitude` DOUBLE NOT NULL"+
                                ")");
    }

    public static void clearDatabase()
    {
        db.execSQL("DELETE FROM " + MARKER_TABLE);
    }

    public static void insertMarker(String title, String subtitle, double latitude, double longitude)
    {
        try
        {
            insert("INSERT INTO " + MARKER_TABLE +" (title,subtitle,latitude,longitude) VALUES (?,?,?,?)",title,subtitle,""+latitude,""+longitude);
        }
        catch(InsertFailedException e)
        {
            e.printStackTrace();
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

    public static Cursor getMarkers()
    {
    	// Obtém os markers
    	return db.rawQuery("SELECT title,subtitle,latitude,longitude FROM marker",null);
    }
}
