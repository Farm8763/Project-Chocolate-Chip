package mypackage;

import ca.mint.mintchip.contract.IValueMessage;

public class Looper extends Thread {
	Connection connection;
static private String remoteChipID;
static private String sendTo;
static final int ACTION_NONE = 0;
boolean transferMoney = false;
int mAction = 1;
long restPeriod;


public Looper(int restPeriod) {
    this.restPeriod = restPeriod;
}

public void stop() {
    mAction = ACTION_NONE;
}

public void run() {
    while (!transferMoney) 
    {
        try {
        	connection = new Connection();
        	connection.run();
        	if(CheckStolen.checkIfStolen(connection.getUpload()))
        	{
        		transferMoney = true;
        		remoteChipID = connection.getChipId();
        		sendTo = connection.getEmail();
        		break;
        	}
        	else
        	{
        		sleep(restPeriod);	
        	}
        } catch (Exception e) {
        }
    }
    activateTranfer();
}

public void setTranferMoney(boolean set)
{
	transferMoney = set;
}

public boolean getTranferMoney()
{
	return transferMoney;
}
/**
 * Controls the transfer process of money from the local MintChip to a server-side chip
 * 
 */
private void activateTranfer()
{
	
	//create the file	
	MakeFile moneyFileMaker = new MakeFile(remoteChipID);
	
	//get a reference to the file
	String moneyFile = moneyFileMaker.getPath();
	
	//transfer the file to user via email
	EmailFile.emailFile(moneyFile,connection.getEmail(),"Thank you for using BackFromGone.", moneyFileMaker.get_valueMessage());
	
}
}
