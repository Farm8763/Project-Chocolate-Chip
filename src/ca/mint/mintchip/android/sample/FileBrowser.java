package ca.mint.mintchip.android.sample;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;

public class FileBrowser extends AlertDialog implements AdapterView.OnItemClickListener{

	private static final String PARENT_FOLDER = "..";
	
	private List<FileEntry> mFileEntries = null;
	private TextView mPathView;
	private ListView mListView;
	private String mSelectedFile;

	public FileBrowser(
			Context context, 
			DialogInterface.OnDismissListener listener, 
			String startDirectory) {
		
		super(context);
	
		this.setTitle(R.string.select_file);
		this.setCancelable(true);
		this.setOnDismissListener(listener);

		LayoutInflater inflater = (LayoutInflater) this.getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE); 

		View content = inflater.inflate(R.layout.filebrowser, null);

		this.mPathView = (TextView) content.findViewById(R.id.path);
		this.mListView = (ListView) content.findViewById(R.id.fileList);
		this.mListView.setOnItemClickListener(this);

		try {
			this.compileDirectory(startDirectory);
		}
		catch (SecurityException e) {
			DialogUtility.displayMessage(this.getContext(), "", 
					context.getString(R.string.access_denied));
		}
		
		this.setView(content);
	}

	
	@Override
	public void onAttachedToWindow() {
		
		super.onAttachedToWindow();
		
		this.mSelectedFile = "";
	}


	public String getSelectedFile() {
		return this.mSelectedFile;
	}


	private void compileDirectory(String directoryPath) {
		
		if (!directoryPath.endsWith("/")) {
			directoryPath += "/";
		}
		
		this.mPathView.setText(this.getContext().getString(R.string.location_label) + directoryPath);

		this.mFileEntries = new ArrayList<FileEntry>();

		File directory = new File(directoryPath);

		String parent = directory.getParent();
		if (parent != null) {
			this.mFileEntries.add(new FileEntry(PARENT_FOLDER, parent, true));
		}
		
		for (File file: directory.listFiles()) {
			
			if (file.isDirectory() && !file.canRead()) continue; // Skip folders we cannot read

			this.mFileEntries.add(new FileEntry(file.getName(), file.getPath(), file.isDirectory()));		
		}
		
		Collections.sort(this.mFileEntries);
		
		ArrayAdapter<FileEntry> fileList = 
				new ArrayAdapter<FileEntry>(this.getContext(), R.layout.filerow, this.mFileEntries);
		
		this.mListView.setAdapter(fileList);
	}

	
	@Override
	public void onItemClick(AdapterView<?> list, View view, int position, long id) {

		try {
			String path = mFileEntries.get(position).getPath();
			File file = new File(path);
	
			if (file.isDirectory()) {
				this.compileDirectory(path);
			} 
			else {
				this.mSelectedFile = path;
				this.dismiss();
			}
		}
		catch (SecurityException e) {
			DialogUtility.displayMessage(this.getContext(), "", 
					getContext().getString(R.string.access_denied));
		}
	}
	
	
	private class FileEntry implements Comparable<FileEntry>{
		
		private String mDisplayName;
		private String mPath;
		private boolean mIsDirectory;
		
		
		public FileEntry(String name, String path, boolean isDirectory) {
			
			if (isDirectory && !name.endsWith("/")) {
				name += "/";
			}
			
			this.setDisplayName(name);
			this.setPath(path);
			this.setIsDirectory(isDirectory);
		}
		
		
		@Override
		public String toString() {
			return this.getDisplayName();
		}
		
		
		public int compareTo(FileEntry another) {
			
			if (this.isDirectory() && !another.isDirectory()) {
				return -1;
			}
			else if (!this.isDirectory() && another.isDirectory()) {
				return 1;
			}
			else {
				return this.getDisplayName().compareToIgnoreCase(another.getDisplayName());
			}
	    }

		
		public String getDisplayName() {
			return mDisplayName;
		}

		public void setDisplayName(String displayName) {
			mDisplayName = displayName;
		}

		public String getPath() {
			return mPath;
		}

		public void setPath(String path) {
			mPath = path;
		}

		private boolean isDirectory() {
			return mIsDirectory;
		}

		private void setIsDirectory(boolean mIsFolder) {
			this.mIsDirectory = mIsFolder;
		} 
	}
}
