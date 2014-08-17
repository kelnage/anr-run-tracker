/*
 *  Copyright (C) 2013 Nick Moore
 *
 *  This file is part of ANR Run Tracker
 *
 *  ANR Run Tracker is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.org.nickmoore.runtrack.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.org.nickmoore.runtrack.R;
import uk.org.nickmoore.runtrack.database.DatabaseManager;

/**
 *
 */
public class BackupActivity extends Activity implements View.OnClickListener {
    private FilenameFilter backupFilter = new FilenameFilter() {
        Pattern pattern = Pattern.compile("^" + DatabaseManager.DATABASE_NAME + "_(\\d+)_\\d{8}$");

        @Override
        public boolean accept(File file, String s) {
            Matcher matcher = pattern.matcher(s);
            return matcher.matches() &&
                    Integer.parseInt(matcher.group(1)) <= DatabaseManager.DATABASE_VERSION;
        }
    };

    private TextView backupStatus;
    private Button backup;
    private Button restore;
    private File backupDirectory;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dateFormat = new SimpleDateFormat("yyyyMMdd");
        File externalStorage = Environment.getExternalStorageDirectory();
        backupDirectory = new File(externalStorage.getAbsolutePath() +
                "/backups/apps/ANR-Run-Tracker");
        if(!backupDirectory.exists()) {
            backupDirectory.mkdirs();
        }
        setContentView(R.layout.backup);
        backupStatus = (TextView) findViewById(R.id.backup_status);
        backup = (Button) findViewById(R.id.backup);
        backup.setOnClickListener(this);
        restore = (Button) findViewById(R.id.restore);
        restore.setOnClickListener(this);
        updateBackupStatus();
    }

    @Override
    public void onClick(View view) {
        if(view.equals(backup)) {
            final File currentDatabase = getDatabasePath(DatabaseManager.DATABASE_NAME);
            if(currentDatabase == null) {
                Toast.makeText(this, R.string.no_database, Toast.LENGTH_SHORT).show();
                return;
            }
            final File newBackup = new File(backupDirectory,
                    DatabaseManager.DATABASE_NAME + "_" + DatabaseManager.DATABASE_VERSION  + "_" +
                            dateFormat.format(new Date()));
            if(newBackup.exists()) {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.overwrite_backup)
                        .setTitle(R.string.overwrite_backup_title)
                        .setPositiveButton(R.string.overwrite, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                try {
                                    backupDatabase(currentDatabase, newBackup);
                                } catch(IOException ex) {
                                    Log.e(getClass().getSimpleName(),
                                            "Could not overwrite backup file: " + ex.getMessage());
                                    Toast.makeText(getApplication(), R.string.backup_failure,
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            } else {
                try {
                    backupDatabase(currentDatabase, newBackup);
                } catch(IOException ex) {
                    Log.e(getClass().getSimpleName(), "Could not create backup file: " +
                            ex.getMessage());
                    Toast.makeText(this, R.string.backup_failure, Toast.LENGTH_LONG).show();
                }
            }
        } else if(view.equals(restore)) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.overwrite_restore)
                    .setTitle(R.string.overwrite_restore_title)
                    .setPositiveButton(R.string.overwrite, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            File currentDatabase = getDatabasePath(DatabaseManager.DATABASE_NAME);
                            File backupFile = updateBackupStatus();
                            if (!backupFile.canRead()) {
                                Toast.makeText(getApplication(),
                                        getString(R.string.unreadable_backup, backupFile.getName()),
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            try {
                                restoreBackup(currentDatabase, backupFile);
                            } catch (IOException ex) {
                                Log.e(getClass().getSimpleName(),
                                        "Could not overwrite current database: " + ex.getMessage());
                                Toast.makeText(getApplication(), R.string.restore_failure,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    private boolean isValidDatabase(File databaseFile) {
        SQLiteDatabase database = SQLiteDatabase.openDatabase(databaseFile.getPath(), null,
                SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = database.rawQuery("PRAGMA integrity_check", null);
        cursor.moveToFirst();
        String firstLine = cursor.getString(0);
        Log.i(getClass().getSimpleName(), firstLine);
        while(cursor.moveToNext()) {
            Log.i(getClass().getSimpleName(), cursor.getString(0));
        }
        database.close();
        return Pattern.matches("\\w*ok\\w*", firstLine);
    }

    private void copyFile(File srcFile, File dstFile) throws IOException {
        FileChannel src = new FileInputStream(srcFile).getChannel();
        FileChannel dst = new FileOutputStream(dstFile).getChannel();
        dst.transferFrom(src, 0, src.size());
        src.close();
        dst.close();
    }

    private void restoreBackup(File currentDatabase, File backupDatabase) throws IOException {
        int backupVersion = Integer.parseInt(backupDatabase.getName().split("_")[1]);
        if (!isValidDatabase(backupDatabase)) {
            Toast.makeText(getApplication(), R.string.invalid_backup,
                    Toast.LENGTH_LONG).show();
            return;
        }
        copyFile(backupDatabase, currentDatabase);
        Toast.makeText(this, R.string.restore_success, Toast.LENGTH_LONG).show();
    }

    private void backupDatabase(File currentDatabase, File backupDatabase) throws IOException {
        copyFile(currentDatabase, backupDatabase);
        if (!isValidDatabase(backupDatabase)) {
            Toast.makeText(this, R.string.invalid_backup, Toast.LENGTH_LONG);
            backupDatabase.renameTo(new File(backupDatabase.getParentFile(),
                    backupDatabase.getName() + "_invalid"));
        }
        else {
            Toast.makeText(this, R.string.backup_success, Toast.LENGTH_LONG).show();
        }
        updateBackupStatus();
    }

    private File updateBackupStatus() {
        File latestFile = null;
        File[] backupFiles = backupDirectory.listFiles(backupFilter);
        if(backupFiles.length > 0) {
            Date latest = null;
            for(File file: backupFiles) {
                try {
                    Date date = dateFormat.parse(file.getName().split("_")[2]);
                    if(latest == null || date.after(latest)) {
                        latest = date;
                        latestFile = file;
                    }
                } catch(ParseException ex) {
                    // oh well...
                }
            }
            if(latest != null) {
                backupStatus.setText(getString(R.string.recent_backup,
                        DateFormat.getDateInstance().format(latest)));
                restore.setEnabled(true);
            } else {
                backupStatus.setText(getString(R.string.no_backups, getString(R.string.app_name)));
                restore.setEnabled(false);
            }
        } else {
            backupStatus.setText(getString(R.string.no_backups, getString(R.string.app_name)));
            restore.setEnabled(false);
        }
        return latestFile;
    }
}
