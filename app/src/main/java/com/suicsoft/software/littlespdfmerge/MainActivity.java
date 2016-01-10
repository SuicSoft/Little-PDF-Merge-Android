/*
 * Copyright (c) 2015 SuicSoft / SuiciStudios(tm).
 *
 * Author : Suici Doga (suiciwd@gmail.com , suiciwd@gmail.com)  / contributors
 *
 * Contact : suiciwd@gmail.com , suiciwd@outlook.com , https://gitter.im/SuicSoft/SuicSoft
 *
 * Website : http://suicsoft.com , http://suicsoft.github.io
 *
 * App created and programmed by SuicSoft, designed by SuiciStudios (SuicSoft).
 *
 * License :
 *
 *
 *
 */

package com.suicsoft.software.littlespdfmerge;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BadPdfFormatException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

interface IDisposable {
    void Dispose() throws IOException;
}

/**
 * Class used to merge pdf files. C# (Mono / .NET using iTextSharp (C# port of java iText library)) version can be found at https://github.com/SuicSoft/Little-PDF-Merge/
 */
class Combiner
        implements IDisposable {

    /**
     * If the object is disposed the value will be true, else it will be false
     */
    public boolean Disposed;
    /**
     * The document
     */
    public com.itextpdf.text.Document Document;
    /**
     * The copy
     */

    public PdfCopy Copy;

    public ByteArrayOutputStream ms;

    public String Output = Environment.getExternalStorageDirectory() + "merged.pdf";
    /**
     * Activity used to show messages. If this value is null messages will be written to the logcat which can be viewed using 'adb logcat' or Android Studio.
     */
    public Activity activity = null;

    public Combiner() throws DocumentException {
        this.ms = new ByteArrayOutputStream();
        this.Document = new com.itextpdf.text.Document();
        this.Copy = new com.itextpdf.text.pdf.PdfCopy(this.Document, ms);
        this.Document.open();
    }

    public void AddFile(String file) throws IOException, BadPdfFormatException {
        PdfReader reader = new PdfReader(file);
        //Copy the pages the new PDF document.
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            com.itextpdf.text.Rectangle size = reader.getPageSizeWithRotation(i);
            //Set the page size.
            Document.setPageSize(size);
            //Create a new page.
            this.Document.newPage();
            PdfImportedPage page = Copy.getImportedPage(reader, i);
            //Add the extracted page.
            Copy.addPage(page);
        }
        reader.close();
    }

    //region IDisposable
    // Dispose() calls Dispose(true)
    public void Dispose() throws IOException {
        Dispose(true);
    }

    // The bulk of the clean-up code is implemented in Dispose(bool)
    protected void Dispose(boolean disposing) throws IOException {
        Disposed = true;
        if (disposing) {
            // Add producer as Little's PDF Merge using itext
            PdfString l = new PdfString("SuicSoft Little's PDF Merge for Android 0.1 (http://www.suicsoft.com) using iText");
            Copy.getInfo().put(PdfName.PRODUCER, l);
            Copy.getInfo().put(PdfName.CREATOR, l);
            l = null;
            // Dispose all resources that implement IDisposable
            if (Copy != null) {
                Copy.close();
                Copy = null;
            }
            if (Document != null) {
                Document.close();
                Document = null;
            }
            if (ms != null) {
                ms.close();
                File mPath = new File(Environment.getExternalStorageDirectory() + "/");
                final FileDialog fileDialog = new FileDialog(activity, mPath);
                fileDialog.setSelectDirectoryOption(true);
                fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                    public void fileSelected(File file) throws IOException {
                        try {
                            ms.writeTo(new FileOutputStream(file.getAbsolutePath()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                fileDialog.showDialog();
                ms = null;
            }
        }
        // free native resources if there are any.
    }
    //endregion
}

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    ArrayList<String> list = new ArrayList<String>();
    ListView listview;
    ArrayAdapter adapter;
    SharedPreferences preferences;
    SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        setContentView(R.layout.activity_main);
        preferences = getSharedPreferences("lpm", MODE_PRIVATE);
        edit = preferences.edit();
        listview = (ListView) findViewById(R.id.listView);
        listview.setAdapter(adapter);
        File mPath = new File(Environment.getExternalStorageDirectory() + "/");
        final FileDialog fileDialog = new FileDialog(this, mPath);
        fileDialog.setFileEndsWith(".pdf");
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                list.add(file.toString());
                adapter.notifyDataSetChanged();
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest.Builder adRequest = new AdRequest.Builder();
        adRequest.addTestDevice("833A940671CD2A6BFB88CB8E65DF669D"); //Nexus 9 which is used to debug Little's PDF Merge.
        if (preferences.getInt("merge", 0) > 15) {
            Log.v("Ads", "Ads have been removed since you are now a long term user");
            mAdView.setVisibility(View.GONE); //Remove ads after the 15th merge
            Toast.makeText(getApplicationContext(), "Ads have been removed automatically", Toast.LENGTH_LONG).show();
        } else
            mAdView.loadAd(adRequest.build());
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileDialog.showDialog();
            }
        });
        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_24dp, getApplicationContext().getTheme()));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.merge) {
            Combiner merge = null;
            try {
                merge = new Combiner();
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            for (String s : list) {
                try {
                    merge.AddFile(s);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (BadPdfFormatException e) {
                    e.printStackTrace();
                }
            }
            try {
                merge.activity = this;
                merge.Dispose();
            } catch (IOException e) {
                e.printStackTrace();
            }
            edit.putInt("merge", preferences.getInt("merge", 0) + 1); //Add one merge to the merge count.
            edit.commit(); //Commit the changes.
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            startActivity(new Intent(MainActivity.this, com.suicsoft.software.littlespdfmerge.SettingsActivity.class));
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
            Uri uri = Uri.parse("http://suicsoft.com"); // missing 'http://' will cause crash
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);


        } else if (id == R.id.nav_send) {
            Uri uri = Uri.parse("https://github.com/SuicSoft/Little-PDF-Merge/issues/"); // missing 'http://' will cause crash
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
