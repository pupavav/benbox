package com.xoninja.benbox;




import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.xoninja.benbox.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NuevaNota extends Activity {

	private static final String PREFERENCES_FILENAME = "DBKeys";
	private NotesDbAdapter mDbHelper;
	private FileIO ioHelper = new FileIO();
	 EditText tituloNota;
	 EditText contenidoNota;
	 final static private String APP_KEY = "l4vyzwpfgt6hkjk";
	 final static private String APP_SECRET = "u1dhenxq7o3xicr";
	 final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nuevanota);
        
        tituloNota= (EditText) findViewById(R.id.titulonota);
        contenidoNota = (EditText) findViewById(R.id.contenidonota);
        Button guardar = (Button) findViewById(R.id.button1);
        
        mDbHelper = new NotesDbAdapter(this);
        
        guardar.setOnClickListener(new OnClickListener(){
          	public void onClick(View v){
          		guardarNota(tituloNota.getText().toString(),contenidoNota.getText().toString());
          		//upload(tituloNota.getText().toString(),contenidoNota.getText().toString());
				new UploadTask().execute(tituloNota.getText().toString(), contenidoNota.getText().toString());
          		
          		       
          	}

    		private void guardarNota(String titulo, String contenido) {
    			 //String noteName = "Note " + mNoteNumber++;
    			    mDbHelper.open();
    		        long result = mDbHelper.createNote(titulo, contenido);
    		        if (result!= -1)
    		        {
    		        	Intent i = new Intent(getApplicationContext(), Dashboard.class);
    		        	startActivity(i);
    		        	
    		        }
    		        else
    		        	Toast.makeText(NuevaNota.this, "FAIL", Toast.LENGTH_LONG).show();
    		        
    		}
          });
        
        
       }
        
      

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nuevanota, menu);
        return true;
    }
    
    private void upload(String nombreFichero, String contenidoFichero) throws IOException{
    	
    	DropboxAPI<AndroidAuthSession> mDBApi;
    	/*AccessTokenPair access = getStoredKeys();
       	mDBApi.getSession().setAccessTokenPair(access);*/
    	AndroidAuthSession session = buildSession();
    	mDBApi = new DropboxAPI<AndroidAuthSession>(session);
    	// Uploading content.

    	FileInputStream inputStream = null;
    	try {
    	   // File file = new File("/notes/"+ nombreFichero + ".txt");
    	   
    	   ContextWrapper ctx = new ContextWrapper(this);
    	   String directorio =  ctx.getFilesDir().toString();
    	   Log.d("directorio",directorio);
    	   File file = new File(directorio + nombreFichero + ".txt");
    	    Writer out = new OutputStreamWriter(new FileOutputStream(file));
    	    try {
    	      out.write(contenidoFichero);
    	    } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
    	        out.close();
    	    }
    	    
    	   inputStream = new FileInputStream(file);
    	    com.dropbox.client2.DropboxAPI.Entry newEntry = mDBApi.putFile(nombreFichero + ".txt", inputStream,
    	            file.length(), null, null);
    	    Log.i("DbExampleLog", "The uploaded file's rev is: " + newEntry.rev);
    	} catch (DropboxUnlinkedException e) {
    	    // User has unlinked, ask them to link again here.
    	    Log.e("DbExampleLog", "User has unlinked.");
    	} catch (DropboxException e) {
    	    Log.e("DbExampleLog", "Something went wrong while uploading.");
    	} catch (FileNotFoundException e) {
    	    Log.e("DbExampleLog", "File not found.");
    	} finally {
    	    if (inputStream != null) {
    	        try {
    	            inputStream.close();
    	        } catch (IOException e) {}
    	    }
    	}
    }



	private String[] getStoredKeys() {
		// TODO Auto-generated method stub
		SharedPreferences dbtoken = getSharedPreferences(PREFERENCES_FILENAME, MODE_MULTI_PROCESS);
    	//SharedPreferences.Editor prefEditor = dbtoken.edit();
    	//AccessTokenPair token = new AccessTokenPair(dbtoken.getString("tokenskey", null), dbtoken.getString("tokenssecret", null));
		String[] token = new String[2];
		token[0] = dbtoken.getString("tokenskey", null);
		token[1] = dbtoken.getString("tokenssecret", null);
		
    	return token;

	}
	
	 private AndroidAuthSession buildSession() {
	        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
	        AndroidAuthSession session;

	        String[] stored = getStoredKeys();
	        if (stored != null) {
	            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
	            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
	        } else {
	            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
	        }

	        return session;
	    }
	 
	 class UploadTask extends AsyncTask<String, Integer, String> {

		 @Override

		 protected void onPreExecute() {

		    super.onPreExecute();
		   //displayProgressBar("Downloading...");
		 }
		 @Override
		 protected String doInBackground(String... params) {

		 	String titulo = params[0];
		 	String contenido = params[1];
		 	try {
				upload(titulo, contenido);
				/*Toast toast = Toast.makeText(getApplicationContext(), "Done!", Toast.LENGTH_SHORT);
				toast.show();*/
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    /*String url=params[0];
		    // Dummy code
		    for (int i = 0; i <= 100; i += 5) {
		      try {
		        Thread.sleep(50);
		      } catch (InterruptedException e) {
		        e.printStackTrace();
		      }
		       publishProgress(i);
		    }*/
		    return "All Done!";
		 }

		 @Override
		 protected void onProgressUpdate(Integer... values) {
		    super.onProgressUpdate(values);
		   // updateProgressBar(values[0]);
		 }
		 @Override
		 protected void onPostExecute(String result) {
		    super.onPostExecute(result);
		    //(dismissProgressBar();
		 }
		 }
}

