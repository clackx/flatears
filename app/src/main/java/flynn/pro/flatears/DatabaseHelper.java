package flynn.pro.flatears;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by claqx on 08.08.16.
 * Code from https://gist.github.com/mikeplate/9173040
 */


class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "records.db";
    private static final int DATABASE_VERSION = 2;
    private static final String RECORDINGS_TABLE = "records";

    // Column Names
    public static final String KEY_ID = "_id";
    public static final String KEY_LINTIME = "linuxtime";
    public static final String KEY_DATE = "date";
    public static final String KEY_TIME = "time";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_CALLTYPE = "calltype";
    public static final String KEY_ANUM = "calling";
    public static final String KEY_BNUM = "called";
    public static final String KEY_SOURCE = "source";
    public static final String KEY_FORMAT = "format";
    public static final String KEY_DEVID = "deviceID";
    public static final String KEY_ANDROIDID = "androidID";
    public static final String KEY_LINK = "link";
    public static final String KEY_UPSTATUS = "doUpload";
    public static final String KEY_RESERVED = "reserved";

    private static DatabaseHelper sInstance;

    private static final String TAG = "DBHELPER";


    public static synchronized DatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String DATABASE_CREATE =
                "create table " + RECORDINGS_TABLE + " ("
                        + KEY_ID + " integer primary key autoincrement, "
                        + KEY_LINTIME + " TEXT, "
                        + KEY_DATE + " TEXT, "
                        + KEY_TIME + " TEXT, "
                        + KEY_CALLTYPE + " TEXT, "
                        + KEY_ANUM + " TEXT, "
                        + KEY_BNUM + " TEXT, "
                        + KEY_DURATION + " TEXT, "
                        + KEY_SOURCE + " TEXT, "
                        + KEY_FORMAT + " TEXT, "
                        + KEY_DEVID + " TEXT, "
                        + KEY_ANDROIDID + " TEXT, "
                        + KEY_UPSTATUS + " BOOL, "
                        + KEY_RESERVED + " TEXT, "
                        + KEY_LINK + " TEXT);";

        db.execSQL(DATABASE_CREATE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + RECORDINGS_TABLE);
            onCreate(db);
        }
    }
//
//    // Insert a post into the database
//    public void addPost(Post post) {
//        // Create and/or open the database for writing
//        SQLiteDatabase db = getWritableDatabase();
//
//        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
//        // consistency of the database.
//        db.beginTransaction();
//        try {
//            // The user might already exist in the database (i.e. the same user created multiple posts).
//            long userId = addOrUpdateUser(post.user);
//
//            ContentValues values = new ContentValues();
//            values.put(KEY_POST_USER_ID_FK, userId);
//            values.put(KEY_POST_TEXT, post.text);
//
//            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
//            db.insertOrThrow(TABLE_POSTS, null, values);
//            db.setTransactionSuccessful();
//        } catch (Exception e) {
//            Log.d(TAG, "Error while trying to add post to database");
//        } finally {
//            db.endTransaction();
//        }
//    }

    public void addInitial(String lintime, String anum, String devid, String androidid, String source, String link) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(KEY_LINTIME, lintime);
            cv.put(KEY_ANUM, anum);
            cv.put(KEY_DEVID, devid);
            cv.put(KEY_ANDROIDID, androidid);
            cv.put(KEY_FORMAT, "WAVe");
            cv.put(KEY_SOURCE, source);
            cv.put(KEY_LINK, link);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(RECORDINGS_TABLE, null, cv);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Ошибка при добавлении начальных значений");
        } finally {
            db.endTransaction();
        }
    }

    // Insert or update a user in the database
    // Since SQLite doesn't support "upsert" we need to fall back on an attempt to UPDATE (in case the
    // user already exists) optionally followed by an INSERT (in case the user does not already exist).
    // Unfortunately, there is a bug with the insertOnConflict method
    // (https://code.google.com/p/android/issues/detail?id=13045) so we need to fall back to the more
    // verbose option of querying for the user's primary key if we did an update.
//    public long addOrUpdateUser(User user) {
//        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
//        SQLiteDatabase db = getWritableDatabase();
//        long userId = -1;
//
//        db.beginTransaction();
//        try {
//            ContentValues values = new ContentValues();
//            values.put(KEY_USER_NAME, user.userName);
//            values.put(KEY_USER_PROFILE_PICTURE_URL, user.profilePictureUrl);
//
//            // First try to update the user in case the user already exists in the database
//            // This assumes userNames are unique
//            int rows = db.update(TABLE_USERS, values, KEY_USER_NAME + "= ?", new String[]{user.userName});
//
//            // Check if update succeeded
//            if (rows == 1) {
//                // Get the primary key of the user we just updated
//                String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
//                        KEY_USER_ID, TABLE_USERS, KEY_USER_NAME);
//                Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{String.valueOf(user.userName)});
//                try {
//                    if (cursor.moveToFirst()) {
//                        userId = cursor.getInt(0);
//                        db.setTransactionSuccessful();
//                    }
//                } finally {
//                    if (cursor != null && !cursor.isClosed()) {
//                        cursor.close();
//                    }
//                }
//            } else {
//                // user with this userName did not already exist, so insert new user
//                userId = db.insertOrThrow(TABLE_USERS, null, values);
//                db.setTransactionSuccessful();
//            }
//        } catch (Exception e) {
//            Log.d(TAG, "Error while trying to add or update user");
//        } finally {
//            db.endTransaction();
//        }
//        return userId;
//    }


    public void updateLast(ContentValues cv) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // :: GOTO LAST RAW
            Cursor cursor = db.rawQuery("SELECT _ID FROM records", null);
            //DatabaseUtils.queryNumEntries(db, "table_name");
            //cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
            cursor.moveToLast();
            String index = cursor.getString(cursor.getColumnIndex(KEY_ID));
            int rows = db.update(RECORDINGS_TABLE, cv, KEY_ID + "=" + index, null); //new String[]{index}'
            if (rows == 1) db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "ОШИБКА ОБНОВЛЕНИЯ ДАННЫХ "+e);
        } finally {
            db.endTransaction();
        }
    }

    public void setState (String link, String state) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_UPSTATUS, state);
        db.beginTransaction();
        try {
            String whereClause = "link = \""+link+"\"";
            Cursor cursor = db.query(RECORDINGS_TABLE, null, whereClause, null, null, null, null);
            cursor.moveToLast();
            String index = cursor.getString(cursor.getColumnIndex(KEY_ID));
            int rows = db.update(RECORDINGS_TABLE, cv, KEY_ID + "=" + index, null);
            if (rows == 1) db.setTransactionSuccessful();
            Log.i(TAG, "Файлу "+link+" установлен статус "+state);
        } catch (Exception e) {
            Log.d(TAG, "ОШИБКА ОБНОВЛЕНИЯ ДАННЫХ "+e);
        } finally {
            db.endTransaction();
        }
    }

    public Map <String, String> getStates() {
        Map< String, String > map = new HashMap< String, String >();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT link, doUpload FROM records", null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    map.put(cursor.getString(cursor.getColumnIndex(KEY_LINK)), cursor.getString(cursor.getColumnIndex(KEY_UPSTATUS)));
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "ОШИБКА при попытке получения всех статусов из таблицы");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return map;
    }


    // Get all posts in the database
//    public List<Post> getAllPosts() {
//        List<Post> posts = new ArrayList<>();
//
//        // SELECT * FROM POSTS
//        // LEFT OUTER JOIN USERS
//        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
//        String POSTS_SELECT_QUERY =
//                String.format("SELECT * FROM %s LEFT OUTER JOIN %s ON %s.%s = %s.%s",
//                        TABLE_POSTS,
//                        TABLE_USERS,
//                        TABLE_POSTS, KEY_POST_USER_ID_FK,
//                        TABLE_USERS, KEY_USER_ID);
//
//        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
//        // disk space scenarios)
//        SQLiteDatabase db = getReadableDatabase();
//        Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);
//        try {
//            if (cursor.moveToFirst()) {
//                do {
//                    User newUser = new User();
//                    newUser.userName = cursor.getString(cursor.getColumnIndex(KEY_USER_NAME));
//                    newUser.profilePictureUrl = cursor.getString(cursor.getColumnIndex(KEY_USER_PROFILE_PICTURE_URL));
//
//                    Post newPost = new Post();
//                    newPost.text = cursor.getString(cursor.getColumnIndex(KEY_POST_TEXT));
//                    newPost.user = newUser;
//                    posts.add(newPost);
//                } while(cursor.moveToNext());
//            }
//        } catch (Exception e) {
//            Log.d(TAG, "Error while trying to get posts from database");
//        } finally {
//            if (cursor != null && !cursor.isClosed()) {
//                cursor.close();
//            }
//        }
//        return posts;
//    }

    // Update the user's profile picture url
//    public int updateUserProfilePicture(User user) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(KEY_USER_PROFILE_PICTURE_URL, user.profilePictureUrl);
//
//        // Updating profile picture url for user with that userName
//        return db.update(TABLE_USERS, values, KEY_USER_NAME + " = ?",
//                new String[] { String.valueOf(user.userName) });
//    }

    // Delete all posts and users in the database
    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            db.delete(RECORDINGS_TABLE, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Ошибка удаления таблицы");
        } finally {
            db.endTransaction();
        }
    }

}