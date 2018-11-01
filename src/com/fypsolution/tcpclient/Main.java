package com.fypsolution.tcpclient;

import java.io.IOException;

import com.fypsolution.tcpclient.network.TCPClient;
import com.fypsolution.tcpclient.network.TCPClient.TCPClientListner;

public class Main implements TCPClientListner {

	public void printMenu() {
		System.out.println("Enter your desired Method");
		System.out.println("1. Connect to Server");
		System.out.println("2. Disconnect to Server");
		System.out.println("3. Start Reading from Server");
		System.out.println("4. Quit");
	}

	public static void main(String[] args) {

		char val = 0;
		Main mn = new Main();
		mn.printMenu();
		TCPClient mClient = new TCPClient(mn);

		while (val != '4') {
			try {
				val = (char) System.in.read();

				if (val == '1') {
					mClient.attemptToConnect("192.168.43.50", 80, 5000);
				} else if (val == '2') {

					mClient.closeConnection();
				} else if (val == '3') {
					mClient.startReadingThread();

				} else if (val == '5') {
					mClient.killThread();
				}
				System.out.println("" + val);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	@Override
	public void onConnectionListner() {
		System.out.println("Connection Established");

	}

	@Override
	public void onDisconnectListner() {
		System.out.println("Connection Lost");

	}

	@Override
	public void onDataReceivedListner(String lineStr) {
		System.out.println("Server Says:" + lineStr);

	}

	@Override
	public void onDataSentListner(String transmittedData) {
		System.out.println(transmittedData + "... Sent to Server");

	}

	@Override
	public void onExceptionListner(Exception ex) {
		System.out.println("Error Occured:" + ex.getMessage());

	}

}
