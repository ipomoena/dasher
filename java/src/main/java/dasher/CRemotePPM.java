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

package dasher;

import java.util.LinkedList;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;

/**
 * Implements the client side of a remote language model. Whilst
 * named RemotePPM, there is no reason why the remote side shouldn't
 * operate some different language model.
 * <p>
 * For details on the server required, see the website at www.smowton.net/chris
 * <p>
 * For details of the PPM Language model see CPPMLanguageModel
 * <p>
 * For details of the general contract of a language model, see CLanguageModel.
 */
public class CRemotePPM extends CLanguageModel {

	class RemotePPMContext extends CContextBase {
		
		public RemotePPMContext(int handle) {
			contextHandle = handle;
		}
		
		public int contextHandle;
		
	}

	enum RemotePPMOperationType {
		CLONE_CONTEXT, CREATE_EMPTY_CONTEXT, ENTER_SYMBOL, GET_PROBS, RELEASE_CONTEXT
	}

	abstract class RemotePPMOperation {
		
		public int m_clientID;
		public RemotePPMOperationType m_opType; 
		
		public abstract byte[] toBytes();
		
	}

	class ReleaseContextOp extends RemotePPMOperation {
			
		public int m_contextHandle;
		
		public ReleaseContextOp(int contextHandle) {
			m_contextHandle = contextHandle;
			m_opType = RemotePPMOperationType.RELEASE_CONTEXT;
		}
		
		public byte[] toBytes() {
			byte[] asBytes = new byte[5];
			
			asBytes[0] = 4; // Operation Identifier
			
			intToBytes(asBytes, m_contextHandle, 1);
			
			return asBytes;
		}
		
	}

	class CreateEmptyContextOp extends RemotePPMOperation {
		
		public int requestedHandle;

		public CreateEmptyContextOp(int handle) {
			requestedHandle = handle;
			m_opType = RemotePPMOperationType.CREATE_EMPTY_CONTEXT;
		}
		
		public byte[] toBytes() {
			byte[] asBytes = new byte[5];
			
			asBytes[0] = 1; // Operation Identifier
			
			intToBytes(asBytes, requestedHandle, 1);
			
			return asBytes;
		}
		
	}

	class CloneContextOp extends RemotePPMOperation {
		
		public int requestedHandle;
		public int existingHandle;

		public CloneContextOp(int oldhandle, int newhandle) {
			requestedHandle = newhandle;
			existingHandle = oldhandle;
			m_opType = RemotePPMOperationType.CLONE_CONTEXT;
		}
		
		public byte[] toBytes() {
			
			byte[] asBytes = new byte[9];

			asBytes[0] = 0; // Operation Identifier

			intToBytes(asBytes, requestedHandle, 1);
			intToBytes(asBytes, existingHandle, 5);

			return asBytes;
		}
	}

	class EnterSymbolOp extends RemotePPMOperation {
		
		public int Symbol;
		public int m_contextHandle;
		public boolean learn;
		
		public EnterSymbolOp(int handle, int sym, boolean learn) {
			Symbol = sym;
			m_contextHandle = handle;
			this.learn = learn;
			m_opType = RemotePPMOperationType.ENTER_SYMBOL;
		}
		
		public byte[] toBytes() {

			byte[] asBytes = new byte[10];

			asBytes[0] = 2; // Operation Identifier

			intToBytes(asBytes, m_contextHandle, 1);
			intToBytes(asBytes, Symbol, 5);

			if(learn) {
				asBytes[9] = 1;
			}
			else {
				asBytes[9] = 0;
			}

			return asBytes;
		
		}
	}

	class GetProbsOp extends RemotePPMOperation {
		public int m_contextHandle;
		public long m_Norm;
		
		public GetProbsOp(int handle, long norm) {
			m_contextHandle = handle;
			m_Norm = norm;
			m_opType = RemotePPMOperationType.GET_PROBS;
		}
		
		public byte[] toBytes() {
			
			byte[] asBytes = new byte[13];

			asBytes[0] = 3; // Operation Identifier

			intToBytes(asBytes, m_contextHandle, 1);
			
			longToBytes(asBytes, m_Norm, 5);
			
			return asBytes;
		}
		
	}

	class MessageDispatcherThread extends Thread {
		
		private CRemotePPM myParent;
		private OutputStream networkStream;
		private String m_host;
		private int m_port;
		private int m_symbolCount;
		
		private boolean alive;
		
		public MessageDispatcherThread(CRemotePPM parent, int nSymbols, String host, int port) throws UnknownHostException, IOException {
			super();
			myParent = parent;
			m_host = host;
			m_port = port;
			m_symbolCount = nSymbols;
			
			this.setName("JDasher Network Dispatcher");
			
			alive = true;
		}
		
		public void die() {
			alive = false;
		}
		
		public void run() {
			
			Socket sock;
			
			try {
				sock = new Socket(m_host, m_port);
			}
			catch(Exception e) {
				System.out.printf("Could not start MessageDispatcherThread: %s%n", e);
				myParent.notifyNetworkFailure(0);
				return;
			}
			try {
				networkStream = sock.getOutputStream();
			}
			catch(IOException e) {
				System.out.printf("Could not retrieve output stream in MessageDispatcherThread: %s%n", e);
				myParent.notifyNetworkFailure(1);
				return;
			}
			
			MessageListenerThread listener;
					
			try {
				listener = new MessageListenerThread(myParent, sock.getInputStream());
				listener.start();
			}
			catch(IOException e) {
				System.out.printf("Could not retrieve input stream in MessageDispatcherThread: %s%n", e);
				myParent.notifyNetworkFailure(2);
				return;
			}
			
			myParent.SetBoolParameter(Ebp_parameters.BP_CONNECT_LOCK, false);
			// We've successfully connected; unset the connecting flag!
			
			byte[] alphSize = new byte[4];
			CRemotePPM.intToBytes(alphSize, m_symbolCount, 0);
			
			try {
				networkStream.write(alphSize);
			}
			catch(IOException e) {
				System.out.printf("Failed writing alphabet size to the server: %s%n", e);
				myParent.notifyNetworkFailure(2);
				return;
			}
			
			while(alive) {
						
				myParent.transmitMessages(networkStream);
				try {
					Thread.sleep(100);
					// FIXME improve this behaviour
				}
				catch(InterruptedException e) {
					System.out.printf("Message dispatcher thread exited.%n");
					myParent.notifyNetworkFailure(3);
					break; // Stop the thread
				}
			}
			
			// We've left the loop -- this means we've been ordered to terminate.
			
			try {
				sock.close();
			}
			catch(IOException e) {
				System.out.printf("Dispatcher could not close socket: %s%n", e);
			}
			
			// This should cause the listener to error out and terminate too.
			
		}
	}

	class MessageListenerThread extends Thread {
		
		private InputStream networkStream;
		private CRemotePPM m_parent;
		private boolean alive = true;
		
		public MessageListenerThread(CRemotePPM parent, InputStream in) {
			m_parent = parent;
			networkStream = in;
			
			this.setName("JDasher Network Listener");
		}
		
		public void run() {
			
			 while(alive) {
				 int arraySize = 0;
				 
				 byte arraySizeAsBytes[] = new byte[4];
				 
				 try {
					 networkStream.read(arraySizeAsBytes);
				 }
				 catch(Exception e) {
					 System.out.printf("Exception reading from InputStream: %s%n", e);
					 break;
				 }
				 
				 arraySize = intFromBytes(arraySizeAsBytes);
				 
				 long[] newProbs = new long[arraySize];
				 
				 for(int i = 0; i < arraySize; i++) {
					 
					 byte[] nextLongAsBytes = new byte[8];
					 try {
						 networkStream.read(nextLongAsBytes);
					 }
					 catch(Exception e) {
						 System.out.printf("Exception reading from InputStream: %s%n", e);
						 alive = false; // The socket has most likely closed; time to die.
						 break;
					 }
					 			 
					 newProbs[i] = longFromBytes(nextLongAsBytes);
					 
				 }
				 
				 if(alive == false) break; // Avoid sending a spurior probsArrived.
				 
				 m_parent.ProbsArrived(newProbs);
			 }
			
		}
		
		private int intFromBytes(byte[] intBytes) {
			int result = 0;

			for(int i = 0; i < 4; i++) {
				int nextbyte = (int)intBytes[i];
				if(nextbyte < 0) nextbyte += 256;

				result |= (nextbyte << ((3 - i)*8));
			}

			return result;
		}

		private long longFromBytes(byte[] longBytes) {
			long result = 0;

			for(int i = 0; i < 8; i++) {
				long nextbyte = (long)longBytes[i];
				if(nextbyte < 0) nextbyte += 256;

				result |= (nextbyte << ((7 - i)*8));
			}

			return result;
		}
	}
	
	public static void intToBytes(byte[] destination, int input, int offset) {
		for(int i = 0; i < 4; i++) {
			destination[i + offset] = (byte)((input >>> (8 * (3 - i))) & 0xFF); 
		}
	}
	
	public static void longToBytes(byte[] destination, long input, int offset) {
		for(int i = 0; i < 8; i++) {
			destination[i + offset] = (byte)((input >>> (8 * (7 - i))) & 0xFF); 
		}
	}
	
	private int lastHandle;
	
	private MessageDispatcherThread dispatch;
	
	private LinkedList<RemotePPMOperation> pendingMessageQueue;
	
	private LinkedList<ProbsListener> eventListeners;
	
	public CRemotePPM(CEventHandler EventHandler, CSettingsStore SettingsStore, CSymbolAlphabet SymbolAlphabet) {
		
		super(EventHandler, SettingsStore, SymbolAlphabet); // Constructor of CLanguageModel
		
		pendingMessageQueue = new LinkedList<RemotePPMOperation>();
		eventListeners = new LinkedList<ProbsListener>();
				
		try {
			dispatch = new MessageDispatcherThread(this, SymbolAlphabet.GetSize(), GetStringParameter(Esp_parameters.SP_LM_HOST), 2111);
			dispatch.start();
		}
		catch(Exception e) {
			System.out.printf("Failed to start MessageDispatcherThread: %s%n", e);
		}
	}
	
	public CContextBase CloneContext(CContextBase Context) {
		
		/* We ask the server to create a new context with a handle we choose.
		 * This is so that the method can return instantly and still pass
		 * out a valid handle.
		 */
		
		CloneContextOp newOp = new CloneContextOp(((RemotePPMContext)Context).contextHandle, GetUnusedHandle());
		enQueueMessage(newOp);
		
		return new RemotePPMContext(newOp.requestedHandle);
		
	}

	public CContextBase CreateEmptyContext() {
		
		/* As above */
		
		CreateEmptyContextOp newOp = new CreateEmptyContextOp(GetUnusedHandle());
		enQueueMessage(newOp);
		
		return new RemotePPMContext(newOp.requestedHandle);
		
	}

	public void EnterSymbol(CContextBase context, int Symbol) {
		
		/* Notify the server of a symbol to add to the specified context */
		
		EnterSymbolOp newOp = new EnterSymbolOp(((RemotePPMContext)context).contextHandle, Symbol, false);
		enQueueMessage(newOp);
		
	}

	public int GetMemory() {
		
		return 0;
		
	}

	public void PromptForProbs(CContextBase Context, long iNorm) {
		
		GetProbsOp newOp = new GetProbsOp(((RemotePPMContext)Context).contextHandle, iNorm);
		enQueueMessage(newOp);
		
	}
	
	public void ProbsArrived(long[] probs) {
		
		for(ProbsListener p : eventListeners) {
			p.probsArrived(probs);
		}
	}
	
	public void RegisterProbsListener(ProbsListener it) {
		
		eventListeners.add(it);
		
	}
	
	public long[] GetProbs(CContextBase Context, long iNorm) {
						
		return null; // Not how it works for this class.
		
	}

	public void LearnSymbol(CContextBase context, int Symbol) {

		/* Similar to EnterSymbol, but the symbol is learned by the language
		 * model and is used to alter its future predictions.
		 */
		
		// System.out.printf("%d, %n", Symbol);
		EnterSymbolOp newOp = new EnterSymbolOp(((RemotePPMContext)context).contextHandle, Symbol, true);
		enQueueMessage(newOp);
		
	}

	public void ReleaseContext(CContextBase Context) {
		
		/* Just releases the context being used, informing the server it can dispose
		 * of the object and reclaim its memory.
		 */
		
		ReleaseContextOp newOp = new ReleaseContextOp(((RemotePPMContext)Context).contextHandle);
		enQueueMessage(newOp);
		
	}
	
	private int GetUnusedHandle() {
		
		/* Handles are client-specific, so we can just use serial numbers,
		 * assuming we won't ever need more than MAXINT handles.
		 */
		
		lastHandle++;
		return lastHandle;
	}
	
	private synchronized void enQueueMessage(RemotePPMOperation message) {
				
		//Add to the queue
		pendingMessageQueue.add(message);
		notifyAll();
	}
	
	public synchronized void transmitMessages(OutputStream out) {
		for(RemotePPMOperation op : this.pendingMessageQueue) {
			try {
				out.write(op.toBytes());
			}
			catch(IOException e) {
				System.out.printf("Failed to write to OutputStream: %s%n", e);
			}
		}
		
		pendingMessageQueue.clear();
	}	
	
	public boolean isRemote() {
		return true;
	}
	
	public void UnregisterComponent() {
		
		dispatch.die(); // Stop our helper threads.
		super.UnregisterComponent();
		
	}
	
	public void notifyNetworkFailure(int errorCode) {
		if(errorCode < 3) {
			// Failure on connection
			
			CMessageEvent message = new CMessageEvent("Could not connect to host; switching to local PPM.", 0, 1);
			InsertEvent(message);
			
		}
		
		else {
			
			// Connection dropped mid-conversation.
			CMessageEvent message = new CMessageEvent("Connection to PPM server lost; switching to local PPM.", 0, 1);
			InsertEvent(message);
				
		}
		
		SetBoolParameter(Ebp_parameters.BP_CONNECT_LOCK, false); // In case we never successfully connected.
		SetLongParameter(Elp_parameters.LP_LANGUAGE_MODEL_ID, 0); // Set to local PPM
	}
}