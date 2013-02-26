package mypackage;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.blackberry.api.mail.*;
import ca.mint.mintchip.blackberry.sample.MintChipUtility;
import ca.mint.mintchip.contract.IValueMessage;
public class EmailFile 
{
	
	public static void emailFile(String path,String sendTo,String subject,IValueMessage message){
		//Get the Store from the default mail Session.
		Store store = Session.getDefaultInstance().getStore();

		//retrieve the sent folder
		Folder[] folders = store.list(Folder.SENT);
		Folder sentfolder = folders[0];

		//create a new message and store it in the sent folder
		Message msg = new Message(sentfolder);
		Address recipients[] = new Address[1];

//		byte[] data = message.getValue();
		byte[] data = new byte[256];
		Multipart multipart = new Multipart();
		SupportedAttachmentPart attach = new SupportedAttachmentPart( multipart,
				"application/x-example", path, data);
		
		//data for the content of the file
		String messageData = "This is a proof-of-concept email, the attached file would contain a complete .ecn file with all of the money stored on the stolen/lost MintChip. The actual file is on the phone. " + subject;

		TextBodyPart tbp = new TextBodyPart(multipart,messageData);
		
		multipart.addBodyPart(tbp);
		multipart.addBodyPart(attach);
		
		try {
		     recipients[0]= new Address(sendTo, sendTo);

		     //add the recipient list to the message
		     msg.addRecipients(Message.RecipientType.TO, recipients);

		     //set a subject for the message
		     msg.setSubject("BackFromGone: MintChip money recovered");

		     //sets the body of the message
		     msg.setContent(multipart);
		     //sets priority
		     msg.setPriority(Message.Priority.HIGH);

		     //send the message
		     Transport.send(msg);
		 }
		catch (Exception me) {
		     System.err.println(me);
		}
}
}
