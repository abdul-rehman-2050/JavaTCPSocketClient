package com.fypsolution.tcpclient.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.EventListener;
import java.util.Timer;
import java.util.TimerTask;

public class TCPClient implements Runnable {

	private Socket clientSocket;
	private PrintWriter outToServer;
	private BufferedReader inFromServer;
	private TCPClientListner mTcpClientListner;
	private Thread readingThread;
	private Timer watchdogTimer;
	private WatchdogTimerTask mWatchdogTimerTask;

	/*
	 * Creates an unconnected socket
	 * 
	 */
	public TCPClient(TCPClientListner tcpListner) {
		// Default Constructor
		clientSocket = new Socket();
		outToServer = null;
		inFromServer = null;
		this.mTcpClientListner = tcpListner;

	}

	public void initWatchdogTimer() {
		watchdogTimer = new Timer("WatchdogTimer");
		mWatchdogTimerTask = new WatchdogTimerTask();
		watchdogTimer.scheduleAtFixedRate(mWatchdogTimerTask, 1000, 4000);

	}

	public void cancelWatchdogTimer() {
		watchdogTimer.cancel();
		mWatchdogTimerTask.cancel();
		watchdogTimer = null;
		mWatchdogTimerTask = null;

	}

	public boolean attemptToConnect(String ipAdd, int portNum, int timeOut) {
		if (clientSocket.isConnected()) {
			return true;
		}

		clientSocket = new Socket();
		try {
			clientSocket.connect(new InetSocketAddress(ipAdd, portNum), timeOut);
			if (inFromServer != null) {
				inFromServer.close();
			}
			if (outToServer != null) {
				outToServer.close();
			}
			inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outToServer = new PrintWriter(clientSocket.getOutputStream(), true);

			if (clientSocket.isConnected()) {
				mTcpClientListner.onConnectionListner();
				return true;
			}
		} catch (IOException e) {
			mTcpClientListner.onExceptionListner(e);
			return false;
		}

		mTcpClientListner.onExceptionListner(new Exception("Unknown Connection Refusal Exception"));
		return false;
	}

	/*
	 * Will write a line to outputstream of client socket
	 * 
	 * 
	 * 
	 */
	public void writeLine(String lineToWrite) {
		outToServer.println(lineToWrite);
		mTcpClientListner.onDataSentListner(lineToWrite);
	}

	/*
	 * Check if server is connected or not
	 * 
	 * @return boolean state of connection true if connected, otherwise false
	 */
	public boolean isConnected() {
		if (clientSocket == null) {
			return false;

		} else if (clientSocket.isConnected()) {
			return true;
		}
		return false;
	}

	/*
	 * This method will properly close all streams and connections
	 * 
	 */

	public void closeConnection() {

		try {
			inFromServer.close();
			outToServer.close();
			clientSocket.close();
			inFromServer = null;
			outToServer = null;
			clientSocket = null;
			mTcpClientListner.onDisconnectListner();
			cancelWatchdogTimer();
		} catch (Exception e) {
			mTcpClientListner.onExceptionListner(e);
		}

	}

	/*
	 * Start Reading untill the socket is connected
	 * 
	 * 
	 */

	public void startReadingThread() {
		if (readingThread != null) {
			readingThread = null;
		}
		readingThread = new Thread(this, "Socket Reading Thread");
		readingThread.start();
		initWatchdogTimer();

	}

	@Override
	public void run() {
		while (isConnected()) {
			String inputLine = null;
			try {
				inputLine = inFromServer.readLine();

			} catch (Exception e) {
				mTcpClientListner.onExceptionListner(e);
			}

			if (inputLine != null) {
				mTcpClientListner.onDataReceivedListner(inputLine);
				mWatchdogTimerTask.disableKilling();

			}

		}
		this.closeConnection();

	}

	public void killThread() {
		if (readingThread != null && readingThread.isAlive()) {
			closeConnection();
			readingThread.stop();
			readingThread = null;
		}
	}

	/*
	 * This class will monitor one flag and if that flag is ture it will simply turn
	 * the main readingthread off
	 * 
	 * 
	 */
	public class WatchdogTimerTask extends TimerTask {

		private boolean wantToKill = false;

		public void enableKilling() {
			wantToKill = true;
		}

		public void disableKilling() {
			wantToKill = false;
		}

		public boolean isKillingEnabled() {
			return wantToKill;
		}

		@Override
		public void run() {
			try {
				if (isKillingEnabled())
					killThread();
				enableKilling();
			} catch (NullPointerException ex) {
				this.cancel();
			}

		}

	}

	/**
	 * @author Strange Lab
	 *
	 */
	public interface TCPClientListner extends EventListener {

		public void onConnectionListner();

		public void onDisconnectListner();

		public void onDataReceivedListner(String lineStr);

		public void onDataSentListner(String transmittedData);

		public void onExceptionListner(Exception ex);

	}

}
