package mypackage;

import net.rim.device.api.system.Application;
import net.rim.device.api.ui.UiApplication;

public class BackgroundStart extends Application implements Runnable {
    public BackgroundStart() {

    run();
    }

    
/**
 * Entry point for application
 * @param args Command line arguments (not used)
 */ 
public static void main(String[] args)
{
  	
	BackgroundStart app = new BackgroundStart();   
}


public void run() {
	// TODO Auto-generated method stub
    Looper updateThread = new Looper(1000);
    updateThread.run();
}

}
