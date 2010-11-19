package org.pentaho.di.www;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.pentaho.di.core.logging.LogWriter;

/**
 * This singleton keeps a repository of all the server sockets.
 *  
 * @author matt
 *
 */
public class SocketRepository {
	
	private static LogWriter log = LogWriter.getInstance();
	
	/**
	 * This map contains a link between a (clustered) transformation and their used server sockets
	 */
	private Map<Integer, SocketRepositoryEntry> socketMap;

	
	public SocketRepository() {
		socketMap = new HashMap<Integer, SocketRepositoryEntry>();
	}
	
	public synchronized ServerSocket openServerSocket(int port, String user) throws IOException {
		SocketRepositoryEntry entry = socketMap.get(port);
		if (entry==null) {
			
			ServerSocket serverSocket = new ServerSocket();
	        serverSocket.setPerformancePreferences(1,2,3); // order of importance: bandwidth, latency, connection time 
	        serverSocket.setReuseAddress(true);
	        
	        // It happens in high-paced environments where lots of sockets are opened and closed that the operating
	        // system keeps a lock on a socket.  Because of this we have to wait at least for one minute and on some platforms up to 2 minutes.
	        // Let's take 5 to make sure we can get a socket connection and we still get into trouble.
	        //
	        // mc: It sucks and blows at the same time that we have to do this but I couldn't find another solution.
	        //
	        try {
	        	serverSocket.bind(new InetSocketAddress(port));
	        } catch(BindException e) {
	        	long totalWait=0L;
	        	long startTime = System.currentTimeMillis();
	        	
	        	IOException ioException = null;
	    		log.logDetailed("Carte socket repository", "Starting a retry loop to bind the server socket on port "+port+".  We retry for 5 minutes until the socket clears in your operating system.");
	        	while (!serverSocket.isBound() && totalWait<300000) {
		        	try {
			        	totalWait=System.currentTimeMillis()-startTime;
		        		log.logDetailed("Carte socket repository", "Retry binding the server socket on port "+port+" after a "+(totalWait/1000)+" seconds wait...");
			        	Thread.sleep(10000); // wait 10 seconds, try again...
			        	serverSocket.bind(new InetSocketAddress(port), 100);
		        	} catch(IOException ioe) {
		        		ioException = ioe;
		        	} catch (Exception ex) {
		        		throw new IOException(ex.getMessage());
		        	}

		        	totalWait=System.currentTimeMillis()-startTime;
	        	}
	        	if (!serverSocket.isBound()) {
	        		throw ioException;
	        	}
	        	log.logDetailed("Carte socket repository", "Succesfully bound the server socket on port "+port+" after "+(totalWait/1000)+" seconds.");
	        }
			entry = new SocketRepositoryEntry(port, serverSocket, true, user);
			
	        // Store the entry in the map too!
	        //
	        socketMap.put(port, entry);
		} else {
			// Verify that the socket is not in use...
			//
			if (entry.isInUse()) { 
				throw new IOException("Server socket on port "+port+" is already in use by ["+entry.getUser()+"]");
			}
			entry.setInUse(true);
		}
		return entry.getServerSocket();
	}
	
	/**
	 * We don't actually ever close a server socket, we re-use them as much as possible.
	 * 
	 * @param port
	 * @throws IOException
	 */
	public synchronized void releaseSocket(int port) throws IOException {
		SocketRepositoryEntry entry = socketMap.get(port);
		if (entry==null) {
			throw new IOException("Port to close was not found in the Carte socket repository!");
		}
		entry.setInUse(false);
	}
	
	/**
	 * @return the socketMap
	 */
	public Map<Integer, SocketRepositoryEntry> getSocketMap() {
		return socketMap;
	}

	/**
	 * @param socketMap the socketMap to set
	 */
	public void setSocketMap(Map<Integer, SocketRepositoryEntry> socketMap) {
		this.socketMap = socketMap;
	}

	/**
	 * Closes all sockets on application end...
	 * @throws IOException in case there is an error
	 */
	public synchronized void closeAll() throws IOException {
		for (Iterator<SocketRepositoryEntry> iterator = socketMap.values().iterator(); iterator.hasNext();) {
			SocketRepositoryEntry entry = iterator.next();
			ServerSocket serverSocket = entry.getServerSocket();
			serverSocket.close();
		}
	}
	
	
}
