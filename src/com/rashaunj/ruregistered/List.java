package com.rashaunj.ruregistered;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.json.JSONException;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.example.ruregistered.R;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import parse.Course;
import parse.CourseList;
import parse.Section;
import parse.TrackedCourse;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class List extends SherlockActivity implements OnItemClickListener{
	
public static ArrayList<Course> mcourse = new ArrayList<Course>();
public ArrayList<String> Listing = new ArrayList<String>();
CourseListAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
   
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		adapter = new CourseListAdapter(List.this,
                R.layout.courselist_item, null);
		LoadData task = new LoadData();
		task.execute();
	}
	
	
	/**
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_list, menu);
		return true;
	}
	**/
	
	class LoadData extends AsyncTask<Void, Void, Void> {
		
	    ProgressDialog progressDialog;
		ListView list = (ListView) findViewById(R.id.courselist);
		Bundle extras = getIntent().getExtras();
		ImageView empty = (ImageView) findViewById(R.id.emptyjpg);
		TextView emptytxt = (TextView) findViewById(R.id.emptytext);
	    @Override
	    protected void onPreExecute()
	    {
	        progressDialog= ProgressDialog.show(List.this, "Please wait...","Retrieving Courses", true);            
	    };      
	    @Override
	    protected Void doInBackground(Void... params)
	    {   
			Bundle extras = getIntent().getExtras();
			try {

				CourseList.create(mcourse,extras.getString("major"),extras.getString("term"),extras.getString("campusCode"));			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	        return null;
	    }       
	    @Override
	    protected void onPostExecute(Void result)
	    {
		    if(mcourse.size()==0){
		    	empty.setVisibility(TextView.VISIBLE);
		    	emptytxt.setVisibility(TextView.VISIBLE);
		    }
			adapter = new CourseListAdapter(List.this,
	                R.layout.courselist_item, mcourse);
		    adapter.notifyDataSetChanged();//Sidenote: Always notify to prevent illegalstateexception
		    list.setAdapter(adapter);
			list.setOnItemClickListener(List.this);
			list.setOnItemLongClickListener(new OnItemLongClickListener() {
			     @Override
			     public boolean onItemLongClick(final AdapterView<?> parent, View v, final int position, long id) {
			    	 AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
			 				List.this);
			     	final int pos = position;

			 			alertDialogBuilder.setTitle("Tracker");
			  
			 			alertDialogBuilder
			 				.setMessage("Would you like to track all section(s) for this course?")
			 				.setCancelable(false)
			 				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
			 					Bundle in = getIntent().getExtras();
			 					public void onClick(DialogInterface dialog,int id) {

			 						Section allsections[] = mcourse.get(pos).sections;
			 						int count = 0;
			 						 String major = in.getString("major");
			 						 String term = in.getString("term");
			 						 String campus = in.getString("campus");
			 						 String level = "U";
			 						 String course = adapter.getItem(position).title;
			 						for (int i = 0;i<allsections.length;i++){
			 							 String section = allsections[i].index;
			 						     writeAll(major,campus,term,level,course,section);
			 						     count++;
			 						}
			 					    CharSequence text = count + " sections added to Course Tracker";
			 					    int duration = Toast.LENGTH_SHORT;

			 					    Toast toast = Toast.makeText(getBaseContext(), text, duration);
			 					    toast.show();
			 					}
			 				  })
			 				.setNegativeButton("No",new DialogInterface.OnClickListener() {
			 					public void onClick(DialogInterface dialog,int id) {

			 						dialog.cancel();
			 					}
			 				});
			  
			 				// create alert dialog
			 				AlertDialog alertDialog = alertDialogBuilder.create();
			  
			 				// show it
			 				alertDialog.show();
			    	 return true;
	
			     }
			 });
			ActionBar bar = getSupportActionBar();
			bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient));
			bar.setTitle("Available Courses");
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	        super.onPostExecute(result);
	        progressDialog.dismiss();
	    };
	 }

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Bundle i = getIntent().getExtras();
		String major = i.getString("major");
		String term = i.getString("term");
		String campus = i.getString("campusCode");
		String course = adapter.getItem(arg2).title;
		Intent intent = new Intent(this,CourseDetail.class);
		intent.putExtra("SecID", arg2);
		intent.putExtra("major",major);
		intent.putExtra("term", term);
		intent.putExtra("campus", campus);
		intent.putExtra("course", course);
		
		startActivity(intent);
	}
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	    	finish();
	        break;

	    }
	    return false;
	}

	@Override
	public void onResume(){
	    super.onResume();
	    mcourse.clear();
	    adapter.notifyDataSetChanged();
		LoadData task = new LoadData();
		task.execute();
	}
    @SuppressLint("WorldReadableFiles")
	public void writeAll(String major,String campus, String term, String level, String course, String section) {
   	 
	TrackedCourse in = new TrackedCourse(major,campus,term,course,section);

	Gson gson = new Gson();
	JsonElement json = gson.toJsonTree(in);

 


try {

    FileOutputStream fos = openFileOutput("tracker",
            Context.MODE_APPEND | Context.MODE_WORLD_READABLE);
    fos.write(json.toString().getBytes());
    fos.close();


    String storageState = Environment.getExternalStorageState();

    if (storageState.equals(Environment.MEDIA_MOUNTED)) {
    	//Properly format json table
      File file = new File(getExternalFilesDir(null), "RUTracker.json");
      	RandomAccessFile raf = new RandomAccessFile(file,"rw");
      	if(raf.length()==0){
      		raf.writeBytes("["+json.toString()+"]");
      	}
      	else{
          	raf.setLength(file.length()-1);
          	raf.seek(file.length());
          	raf.writeBytes(","+json.toString()+"]");
      	}
      	raf.close();


    }

} catch (Exception e) {

    e.printStackTrace();

}


    }
}