/*
  This file is part of JDasher.

  JDasher is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  JDasher is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with JDasher; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

  Copyright (C) 2006      Christopher Smowton <cs448@cam.ac.uk>

  JDasher is a port derived from the Dasher project; for information on
  the project see www.dasher.org.uk; for information on JDasher itself
  and related projects see www.smowton.net/chris

*/

package dasher.net;

import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.StringBuffer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * This thread is spawned by NetDasherListener every time a connection
 * is accepted.
 * <p>
 * The thread is passed references to most of NetDasherListener's
 * internal data, and is responsible for interpreting the incoming
 * request and using these resources to service it appropriately.
 * <p>
 * Output should also be organised such that the client has received
 * a reply by the time this thread ends.
 * <p>
 * The input will arrive in XML format; output too should be written
 * as XML.
 * <p>
 * <b>Input XML</b>
 * <p>
 * The general format of XML which is expected is:
 * <p>
 * &lt;command type="message_type">%lt;attribute_1>value%lt;/attribute_1>&lt;attribute_2>value&lt;/attribute_2>...&lt;/command>
 * <p>
 * Where message_type can be any of:
 * <p>
 * <b>init</b> -- Starts a new session. Required attributes are 
 * <i>width</i> (Client's screen width in pixels) and <i>height</i> 
 * (Client's screen height in pixels).
 * <p>
 * <b>click</b> -- Reports a user mouse click at a given location.
 * Required parameters are <i>x</i> (mouse x co-ordinate) and <i>y</i>
 * (mouse y co-ordinate).
 * <p>
 * <b>changealphabet</b> -- Changes the alphabet currently in use.
 * Required parameter: <i>name</i> (Name of new alphabet)
 * <p>
 * <b>changecolours</b> -- Changes the colour scheme currently in use.
 * Required parameter: <i>name</i> (Name of new colour scheme)
 * <p>
 * <b>Output XML</b>
 * <p>
 * This class is responsible for producing XML reporting
 * errors in parsing input XML.
 * <p>
 * For a description of ordinary output returned when the input
 * XML was successfully parsed, see the documentation for NetScreen.
 */
public class NetDasherCommandInterpreterThread extends Thread {

	/**
	 * idWrapper class to obtain free session IDs.
	 */
	protected idWrapper freeID;
	
	/**
	 * Map indexing the available NetDasherConnectionHandlers by their associated
	 * session identifiers. 
	 */
	protected java.util.concurrent.ConcurrentHashMap<Integer, NetDashConnectionHandler> Dashers;
	
	/**
	 * Socket on which the incoming request arrived, and to which
	 * output should eventually be delivered.
	 */
	protected Socket DasherConnection;
	
	/**
	 * DocumentBuilderFactory to be used in the course of all
	 * XML parsing.
	 */
	protected DocumentBuilderFactory db;
	
	/**
	 * Data location any created Dasher should use to locate
	 * training text.
	 */
	protected String dataLocation;
	
	/**
	 * AlphIO instance to be supplied to any Dashers created
	 */
	protected dasher.CAlphIO alphIO;
	
	/**
	 * ColourIO instance to be supplied to any Dashers created
	 */
	protected dasher.CColourIO colourIO;
		
	/**
	 * Creates a new command interpreter and passes in working 
	 * data. Given these, the thread is ready to begin interpreting
	 * and servicing the command given on the supplied socket.
	 * <p>
	 * The actual interpreting will take place when the thread's
	 * start() method is called.  
	 * 
	 * @param Connection Socket from which the command should be read
	 * 		  and to which the answer should be written.
	 * @param dashs Map indexing Dashers by their session ID
	 * @param freeIDs Class capable of reliably supplying unique
	 * 	      free session identifiers. Must be thread-safe.
	 * @param docbuild DocumentBuilderFactory to use for XML reading and writing
	 * @param dataloc Data location where Dashers can find training text
	 * @param alphIO AlphIO object describing available alphabets
	 * @param colourIO ColourIO object describing available colour schemes
	 */
	public NetDasherCommandInterpreterThread(Socket Connection, java.util.concurrent.ConcurrentHashMap<Integer, NetDashConnectionHandler> dashs, idWrapper freeIDs, DocumentBuilderFactory docbuild, String dataloc, dasher.CAlphIO alphIO, dasher.CColourIO colourIO) {
		
		this.DasherConnection = Connection;
		this.Dashers = dashs;
		this.db = docbuild;
		this.dataLocation = dataloc;
		
		this.alphIO = alphIO;
		this.colourIO = colourIO;
		
		this.freeID = freeIDs;
			
	}
	
	/**
	 * Starts the thread. See the class summary for a description
	 * of the input the class accepts, and the output produced.
	 */
	public void run() {
	
		InputStream DasherInput;
		
		try {
			DasherInput = DasherConnection.getInputStream();
		}
		catch(IOException e) {
			System.out.printf("Could not retrieve connection stream: %s%n", e);
			try {
				DasherConnection.close();
			}
			catch(IOException ex) {
				// Do nothing; clearly it wasn't ever properly established.
			}
			return; // Try the next connection!
		}
		
		java.io.OutputStream output;
		try {
			output = DasherConnection.getOutputStream();
		}
		catch(IOException e) {
			System.out.printf("Couldn't get an output stream: %s%n", e);
			try {
				DasherConnection.close();
			}
			catch(IOException ex) {
				// Do nothing; clearly it wasn't ever properly established.
			}
			return; // Try the next connection!
		}

		java.io.InputStreamReader xmlreader;
		
		try {
			xmlreader = new java.io.InputStreamReader(DasherInput, "utf-8"); 
		}
		catch(Exception e) {
			System.out.printf("Error decoding UTF-8 input: %s%n", e);
			return;
		}
		
		StringBuffer inputBuffer = new StringBuffer();
		// Max command size: 1KB. This may be changed in the future.
		try {
		while(!inputBuffer.toString().endsWith("</command>")) {
			inputBuffer.append((char)xmlreader.read());
		}
		}
		
		catch(IOException e) {
			System.out.printf("Error whilst reading XML: %s%n", e);
		}
		
		Document dasherCommandDoc;
		
		try {
		
			dasherCommandDoc = db.newDocumentBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader(inputBuffer.toString())));
		
		}
		
		catch(Exception e) {
			System.out.printf("Error parsing input XML: %s%n", e);
			
			try {
				DasherConnection.close();
			}
			catch(IOException ex) {
				// Do nothing
			}
			
			return;
		}
		
		Element dasherCommand = (Element)dasherCommandDoc.getElementsByTagName("command").item(0);
								
		try {
			DasherConnection.shutdownInput();
		}
		catch(IOException e) {
			System.out.printf("Failed terminating socket input: %s%n", e);
			try {
				DasherConnection.close();
			}
			catch(IOException ex) {
				// Do nothing; clearly it wasn't ever properly established.
			}
			return; // Try the next connection!
		}


		try {
			if(dasherCommand.getAttribute("type").equals("init")) {

				// New session request
				int newSessionID = getFreeID();
				Dashers.put(newSessionID, new NetDashConnectionHandler(newSessionID, Integer.parseInt(dasherCommand.getElementsByTagName("width").item(0).getChildNodes().item(0).getNodeValue()), Integer.parseInt(dasherCommand.getElementsByTagName("height").item(0).getChildNodes().item(0).getNodeValue()), dataLocation, alphIO, colourIO));
				Dashers.get(newSessionID).Clicked(0, 0, output);
				
				System.out.printf("New session created for %s, session ID %d.%n", DasherConnection.getInetAddress().getHostAddress(), newSessionID);

			}
			else {

				int sessID = Integer.parseInt(dasherCommand.getElementsByTagName("sessid").item(0).getChildNodes().item(0).getNodeValue());

				NetDashConnectionHandler referencedDasher = Dashers.get(sessID);
				
				if(referencedDasher == null) throw new NoSuchSessionException();

				referencedDasher.getLock().lock();

				if(referencedDasher.isDestroyed()) {
					throw new NoSuchSessionException();
				}
				
				if(dasherCommand.getAttribute("type").equals("click")) {

					referencedDasher.Clicked(Integer.parseInt(dasherCommand.getElementsByTagName("x").item(0).getChildNodes().item(0).getNodeValue()), Integer.parseInt(dasherCommand.getElementsByTagName("y").item(0).getChildNodes().item(0).getNodeValue()), output);

				}
				else if(dasherCommand.getAttribute("type").equals("changealphabet")) {
					
					referencedDasher.changeAlphabet(dasherCommand.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue());
					referencedDasher.Draw(output);
					
				}

				else if(dasherCommand.getAttribute("type").equals("changecolours")) {
					
					referencedDasher.changeColours(dasherCommand.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue());
					referencedDasher.Draw(output);
					
				}

				referencedDasher.getLock().unlock();
			}
		}
		catch(NoSuchSessionException e) {
			System.out.printf("Invalid session ID specified or incorrect number of parameters: %s%n", e);
			try {
				// TODO: Return an error document here.
				writeError("Your session has expired. Please refresh the page to begin a new session.", output);
				
				DasherConnection.close();
			}
			catch(IOException ex) {
			}
			return; // Try the next connection!
		}
		
		try {
			DasherConnection.close();
		}
		catch(IOException e) {
			System.out.printf("Couldn't close a connection: %s%n", e);
			
			return; // Try the next connection!
		}
	}
	
	/**
	 * Uses freeID to produce a free ID
	 * 
	 * @return Free (unique) session identifier.
	 */
	protected int getFreeID() {
		
		return freeID.getFreeID();
		
	}
	
	/**
	 * Produces an XML document describing a given error and
	 * serializes the document to a given OutputStream, typically
	 * a network socket.
	 * <p>
	 * This method is for reporting errors encountered whilst
	 * trying to parse the client's request document; errors
	 * arising in Dasher itself should be dealt with by other means. 
	 * 
	 * @param errorString String to report to the user
	 * @param m_Output OutputStream to which XML should be serialized
	 */
	protected void writeError(String errorString, OutputStream m_Output) {
		// Create instance of DocumentBuilderFactory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// Get the DocumentBuilder
		DocumentBuilder parser;
		
		try {
			parser = factory.newDocumentBuilder();
		}
		catch(ParserConfigurationException e) {
			System.out.printf("Can't create XML error document: %s%n", e);
			return;
		}
		// Create blank DOM Document
		Document errorMessage = parser.newDocument();
		
		Element messageRoot = errorMessage.createElement("message");
		errorMessage.appendChild(messageRoot);
		
		Element error = errorMessage.createElement("error");
		messageRoot.appendChild(error);
		
		Text theMessage = errorMessage.createTextNode(errorString);
		error.appendChild(theMessage);
		
		TransformerFactory tranFactory = TransformerFactory.newInstance();
		Transformer aTransformer;
		try {
			aTransformer = tranFactory.newTransformer();
		}
		catch(TransformerConfigurationException e) {
			System.out.printf("Failed to create XML transformer: %s%n", e);
			return;
		}

		Source src = new DOMSource(errorMessage);
		Result dest = new StreamResult(m_Output);
		try {
			aTransformer.transform(src, dest);
		}
		catch(TransformerException e) {
			System.out.printf("Failed to output XML: %s%n", e);
			return;
		}
		
		try {
			m_Output.flush();
		}
		catch(java.io.IOException e) {
			System.out.printf("Failed to flush output: %s%n", e);
		}

		
	}

	
}

/**
 * Exception indicating that a NetDasherCommandInterpreterThread
 * found its request to reference a session ID which either does
 * not exist or has been destroyed.
 */
class NoSuchSessionException extends Exception {

	public static final long serialVersionUID = 0;
	
}