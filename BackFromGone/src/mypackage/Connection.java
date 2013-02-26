package mypackage;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.rim.device.api.xml.parsers.DocumentBuilder;
import net.rim.device.api.xml.parsers.DocumentBuilderFactory;

	  public class Connection extends Thread{
		  private String chipId;
		  private String email;
		  private String upload;
	        public Connection(){
	            super();
	        }

	        public void run(){
	            // define variables later used for parsing
	            Document doc;
	            StreamConnection conn;

	            try{
	                //providing the location of the XML file,
	                //your address might be different
	                conn=(StreamConnection)Connector.open
	                  ("http://backfromgone.ca/users.xml?username=sample");
	                //next few lines creates variables to open a
	                //stream, parse it, collect XML data and
	                //extract the data which is required.
	                //In this case they are elements,
	                //node and the values of an element
	                DocumentBuilderFactory docBuilderFactory
	                  = DocumentBuilderFactory. newInstance(); 
	                DocumentBuilder docBuilder
	                  = docBuilderFactory.newDocumentBuilder();
	                docBuilder.isValidating();
	                doc = docBuilder.parse(conn.openInputStream());
	                doc.getDocumentElement ().normalize ();
	                NodeList list=doc.getElementsByTagName("*");
	                //this "for" loop is used to parse through the
	                //XML document and extract all elements and their
	                //value, so they can be displayed on the device
setUpload(list.item(2).getChildNodes().item(0).getNodeValue());
setEmail(list.item(4).getChildNodes().item(0).getNodeValue());
setChipId(list.item(3).getChildNodes().item(0).getNodeValue());
if(getEmail().equals("sample@example.com")){
	setEmail("ryan_mcneely@rogers.com");
}

                   /* for (int i=0;i<list.getLength();i++){
                        Node value=list.item(i).
                          getChildNodes().item(0);
                        upload=list.item(i).getNodeName();
                        //upload=value.getNodeValue();
//                        updateField(_node,_element);
	            }*/
                    }
	            catch (Exception e){
	            }
	        }//end connection function

			public String getUpload() {
				return upload;
			}

			public void setUpload(String upload) {
				this.upload = upload;
			}

			public String getChipId() {
				return chipId;
			}

			public void setChipId(String chipId) {
				this.chipId = chipId;
			}

			public String getEmail() {
				return email;
			}

			public void setEmail(String email) {
				this.email = email;
			}

			
	    }// end connection class
