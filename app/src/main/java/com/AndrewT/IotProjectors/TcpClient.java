package com.AndrewT.IotProjectors;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static com.AndrewT.IotProjectors.ClientActivity.hexStringToByteArray;


public class TcpClient {
    String projectorIP = PreferencesManager.getInstance().getProjectorIP();
    public final static String SERVER_IP = "192.168.1.20";
    public static final int SERVER_PORT = 5000;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    private Socket socket;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(byte [] message) {
            DataOutputStream os = null;
            try {
                os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String sendapduString2 = "PON";//get info

           // byte[] sendapdu2 = hexStringToByteArray(sendapduString2);
            try {
                send(os, message);
            }catch (Exception e){
                e.printStackTrace();
            }

    }
    public void sendMessage(String  message) {
        if (mBufferOut != null && !mBufferOut.checkError()) {
            mBufferOut.println(message);
            mBufferOut.flush();
        }
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        // send mesage that we are closing the connection
        sendMessage("Connection CLosed");

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    public void run() {

        mRun = true;


            //here you must put your computer's IP address.
        InetAddress serverAddr = null;

        try {
          
            serverAddr = InetAddress.getByName(SERVER_IP);
            Log.e("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, SERVER_PORT);

            DataInputStream is = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            String sendapduString2 = "someapducommand";//get info

            byte[] sendapdu2 = hexStringToByteArray(sendapduString2);
            send(os, sendapdu2);
            System.out.println("Data sent to Server ; Message = " );
            while (true)
            {
                // Receive Server Response
                byte[] byteData = receive(is);
                String responseData = new String(byteData);
                System.out.println("Server Response = " + responseData.trim());
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }



       /*     try {


                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // send login name

                //in this while the client listens for the messages sent by the server
                while (mRun) {

                    mServerMessage = mBufferIn.readLine();

                    if (mServerMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(mServerMessage);
                    }

                }

                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);

            }
*/
    }

    public static byte[] receive(DataInputStream is) throws Exception
    {
        try
        {
            byte[] inputData = new byte[1024];
            is.read(inputData);
            return inputData;
        }
        catch (Exception exception)
        {
            throw exception;
        }
    }


    public static void send(DataOutputStream os, byte[] byteData) throws Exception
    {
        try
        {
            os.write(byteData);
            os.flush();
        }
        catch (Exception exception)
        {
            throw exception;
        }
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}
