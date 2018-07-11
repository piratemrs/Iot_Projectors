package com.AndrewT.IotProjectors;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.fangxu.allangleexpandablebutton.AllAngleExpandableButton;
import com.fangxu.allangleexpandablebutton.ButtonData;
import com.fangxu.allangleexpandablebutton.ButtonEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import abak.tr.com.boxedverticalseekbar.BoxedVertical;

import static com.AndrewT.IotProjectors.ApplicationProvider.showToast;


public class ClientActivity extends Activity {

    private ListView mList;
    private ArrayList<String> arrayList;
    // private ClientListAdapter mAdapter;
    private TcpClient mTcpClient;
    private boolean power_state = false;
    private boolean mute = false;
    private boolean freeze = false;
    private boolean inputToast = false;
    private boolean input2Toast = false;


    private final String commmands[] = {
            "PON",  // Power On  //0
            "POF",  // Power Off    //1
            "OFZ:0", // Freeze Off  //2
            "OFZ:1", // Freeze on   //3
            "OAS",  // auto setup   //4
            "IIS:RG1", //  input mode : RGB1    //5
            "IIS:VID", //  input mode : VID     //6
            "IIS:SVD", //  input mode : SVD     //7
            "AMT:0",   // Audio UNMute   //8
            "AMT:1",  // Audio Mute      //9
            //--------------// End Control Commands
            "VPM:NAT", // Picture Mode : NATURAL    //10
            "VPM:STD", // Picture Mode : STANDARD   //11
            "VPM:DYN", // Picture Mode : DYNAMI     //12
            "VPM:CIN", // Picture Mode : CINEMA     //13
            "VPM:GRA", // Picture Mode : GRAPHIC        //14
            "VPM:USR", // Picture Mode : USER       //15
            "VBR:",  // color Brightness ex  : VBR:p1p2p3  (000~063)    //16
            "VSR:",  // SHARPNESS 0~15 ex :VSR:p1p2p3       //17
            "AVL:",  // sound  volume ///        //18
            //-------------------// End Adjustment Mode
            "QPW",       // POWER STATUS        //19
            "QFZ",       // FREEZE STATUS       //20
            "QIN",      // INPUT SIGNAL STATUS      //21
            "QPM",     // PICTURE MODE STATUS       //22
            "QVB",    // BRIGHTNESS STATUS          //23
            "QVS",    // SATURATION STATUS          //24
            "QTM:0",  //INPUT AIR TEMP          //25
            "QTM:1",  // OUTPUT AIR TEMP        //26
            "QTM:2",  //OPTICAL MODULE TEMP     //27
            "QAM",  /// MUTE STATUS         //28
            "QAV"   // AUDIO VOLUME STATUS      //29
            , "DZU", "DZD"};  // EXTRA ZOOM UP/DOWN CONTROLS  //30    ,   //31

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        arrayList = new ArrayList<>();
/*              "VBR:",  // color Brightness ex  : VBR:p1p2p3  (000~063)    //16
                "VSR:",  // SHARPNESS 0~15 ex :VSR:p1p2p3       //17
                "AVL:p1p2p3 ",  // sound  volume ///        //18*/
        BoxedVertical bv = (BoxedVertical) findViewById(R.id.boxed_vertical);  //brightness slider

        bv.setOnBoxedPointsChangeListener(new BoxedVertical.OnValuesChangeListener() {
            @Override
            public void onPointsChanged(BoxedVertical boxedPoints, final int value) {
                System.out.println(value);
            }

            @Override
            public void onStartTrackingTouch(BoxedVertical boxedPoints) {
            }

            @Override
            public void onStopTrackingTouch(BoxedVertical boxedPoints) {
                if (mTcpClient != null) {
                    new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, commmands[16] + "0" + boxedPoints.getValue());
                    Log.d("Msg Sent :", "Message Sent");
                }
                showToast(String.valueOf(boxedPoints.getValue()));
            }
        });
        BoxedVertical bv2 = (BoxedVertical) findViewById(R.id.boxed_vertical2);  // audio volume

        bv2.setOnBoxedPointsChangeListener(new BoxedVertical.OnValuesChangeListener() {
            @Override
            public void onPointsChanged(BoxedVertical boxedPoints, final int value) {
                // System.out.println(value);
            }

            @Override
            public void onStartTrackingTouch(BoxedVertical boxedPoints) {
            }

            @Override
            public void onStopTrackingTouch(BoxedVertical boxedPoints) {
                if (mTcpClient != null) {
                    new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, commmands[18] + "0" + boxedPoints.getValue());
                    Log.d("Msg Sent :", "Message Sent");
                }
                showToast(String.valueOf(boxedPoints.getValue()));
            }
        });
        // final EditText editText = (EditText) findViewById(R.id.editText);
        //Button send = (Button) findViewById(R.id.send_button);
        AllAngleExpandableButton button = (AllAngleExpandableButton) findViewById(R.id.button_expandable_90_180);
        final List<ButtonData> buttonDatas = new ArrayList<>();
        int[] drawable = {R.drawable.svid, R.drawable.laptop, R.drawable.vid, R.drawable.svid};
        for (int i = 0; i < drawable.length; i++) {
            ButtonData buttonData = ButtonData.buildIconButton(this, drawable[i], 0);
            buttonData.setBackgroundColorId(this, R.color.Transparent);
            buttonDatas.add(buttonData);
        }
        button.setButtonDatas(buttonDatas);
        setListener(button);

        installButton0to360();

        //relate the listView from java to the one created in xml
        //mList = (ListView) findViewById(R.id.list);
        //mAdapter = new ClientListAdapter(this, arrayList);
        //mList.setAdapter(mAdapter);

       /* send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               // String message = editText.getText().toString();  // command here
                String message = commmands[0];
                //add the text in the arrayList
                arrayList.add("c: " + message);

                //sends the message to the server
                if (mTcpClient != null) {
                    new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,message);
                    Log.d("Msg Sent :","Message Sent");
                }

                //refresh the list
               // mAdapter.notifyDataSetChanged();
               // editText.setText("");
            }
        });
*/
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mTcpClient != null) {
            // disconnect
            new DisconnectTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (mTcpClient != null) {
            // if the client is connected, enable the connect button and disable the disconnect one
            menu.getItem(1).setEnabled(true);
            menu.getItem(0).setEnabled(false);
        } else {
            // if the client is disconnected, enable the disconnect button and disable the connect one
            menu.getItem(1).setEnabled(false);
            menu.getItem(0).setEnabled(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.connect:

                String projectorID = PreferencesManager.getInstance().getProjectorId();
                String projectorIP = PreferencesManager.getInstance().getProjectorIP();

                // check if we have the projectorID saved in the preferences, if not, notify the user to write one down
                if (projectorID != null && projectorIP != null) {
                    // connect to the server
                    new ConnectTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
                } else {
                    Toast.makeText(this, "Please got to preferences and set a Projector Id first!", Toast.LENGTH_LONG).show();

                }

                return true;
            case R.id.disconnect:

                if (mTcpClient == null) {
                    return true;
                }

                new DisconnectTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                return true;
            case R.id.preferences:

                startActivity(new Intent(this, PreferencesActivity.class));

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private static String hexToASCII(String hexValue) {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexValue.length(); i += 2) {
            String str = hexValue.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    private static String asciiToHex(String asciiValue) {
        char[] chars = asciiValue.toCharArray();
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            hex.append(Integer.toHexString((int) chars[i]));
        }
        return hex.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public void pwr(View view) {
        String message = commmands[0];
        // String message = editText.getText().toString();  // command here
        if (!power_state) {
            message = commmands[0];  //PON
            power_state = true;
        } else {
            message = commmands[1];  //POF
            power_state = false;
        }


        //sends the message to the server
        if (mTcpClient != null) {
            new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
            Log.d("Msg Sent :", "Message Sent");
        }
    }

    public void mute(View view) {
        String message = commmands[8];
        // String message = editText.getText().toString();  // command here
        if (!mute) {
            message = commmands[9];  //Mute
            mute = true;
            view.setBackgroundResource(R.drawable.mute);
        } else {
            message = commmands[8];  //unmute
            mute = false;
            view.setBackgroundResource(R.drawable.unmute);
        }

        //sends the message to the server
        if (mTcpClient != null) {
            new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);

        }
    }

    public void freeze(View view) {
        // Freeze Off  //2
        // Freeze on   //3
        String message = commmands[8];
        // String message = editText.getText().toString();  // command here
        if (!freeze) {
            message = commmands[3];  //freeze
            freeze = true;
        } else {
            message = commmands[2];  //unfreeze
            freeze = false;
        }


        //add the text in the arrayList
        // arrayList.add("c: " + message);

        //sends the message to the server
        if (mTcpClient != null) {
            new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
            Log.d("Msg Sent :", "Message Sent");
        }
    }

    /**
     * Sends a message using a background task to avoid doing long/network operations on the UI thread
     */
    public class SendMessageTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            // send the message
            byte[] start = new byte[]{0x02};
            byte[] middle = hexStringToByteArray(asciiToHex
                    (params[0]));
            byte[] end = new byte[]{0x03};

            // byte[] hex = new byte[start.length + middle.length + end.length];
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            try {
                outputStream.write(start);
                outputStream.write(middle);
                outputStream.write(end);

            } catch (IOException e) {
                e.printStackTrace();
            }

            byte hex[] = outputStream.toByteArray();
            byte[] message = new byte[]{(byte) 0x02, (byte) 0x50, (byte) 0x4f, (byte) 0x4e, (byte) 0x03};

            mTcpClient.sendMessage(hex);
            Log.d("Msg Sent2 :", String.valueOf(hex));

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            super.onPostExecute(nothing);
            // clear the data set
            arrayList.clear();
            // notify the adapter that the data set has changed.
            //    mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Disconnects using a background task to avoid doing long/network operations on the UI thread
     */
    public class DisconnectTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            // disconnect
            mTcpClient.stopClient();
            mTcpClient = null;

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            super.onPostExecute(nothing);
            // clear the data set
            arrayList.clear();
            // notify the adapter that the data set has changed.
            //  mAdapter.notifyDataSetChanged();
        }
    }

    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... message) {

            //we create a TCPClient object and
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            //in the arrayList we add the messaged received from server
            arrayList.add(values[0]);
            Log.d("received : ",values[0]);
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
            //  mAdapter.notifyDataSetChanged();
        }
    }

    private void setListener(AllAngleExpandableButton button) {
        button.setButtonEventListener(new ButtonEventListener() {
            @Override
            public void onButtonClicked(int index) {
                //   Toast.makeText(getApplicationContext(),"clicked index:" + index,Toast.LENGTH_SHORT).show();
                switch (index) {
                    case 1:
                        showToast("RG1 Selected");
                        if (mTcpClient != null) {
                            new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, commmands[5]);
                            Log.d("Msg Sent :", "Message Sent");
                        }
                        break;
                    case 2:
                        showToast("VID Selected");
                        if (mTcpClient != null) {
                            new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, commmands[6]);
                            Log.d("Msg Sent :", "Message Sent");
                        }
                        break;
                    case 3:
                        showToast("S-Video Selected");
                        if (mTcpClient != null) {
                            new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, commmands[7]);
                            Log.d("Msg Sent :", "Message Sent");
                        }
                        break;

                         /*   "IIS:RG1", //  input mode : RGB1    //5
                            "IIS:VID", //  input mode : VID     //6
                            "IIS:SVD", //  input mode : SVD     //7*/
                }
            }

            @Override
            public void onExpand() {
                if (!inputToast) {
                    showToast("Select Input Mode");
                    inputToast = true;
                }
            }

            @Override
            public void onCollapse() {
                // showToast("onCollapse");
            }
        });
    }

    private void setListener2(AllAngleExpandableButton button) {
        button.setButtonEventListener(new ButtonEventListener() {
            @Override
            public void onButtonClicked(int index) {
                // Toast.makeText(getApplicationContext(),"clicked index:" + index,Toast.LENGTH_SHORT).show();
                switch (index) {
                    case 1:
                        showToast("Picture Mode : NATURAL mode");
                        if (mTcpClient != null) {
                            new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, commmands[10]);
                            Log.d("Msg Sent :", "Message Sent");
                        }
                        break;
                    case 2:
                        showToast("Picture Mode : STANDARD");
                        if (mTcpClient != null) {
                            new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, commmands[11]);
                            Log.d("Msg Sent :", "Message Sent");
                        }
                        break;
                    case 3:
                        showToast("Picture Mode : DYNAMIC");
                        if (mTcpClient != null) {
                            new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, commmands[12]);
                            Log.d("Msg Sent :", "Message Sent");
                        }
                        break;
                    case 4:
                        showToast("Picture Mode : CINEMA");
                        if (mTcpClient != null) {
                            new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, commmands[13]);
                            Log.d("Msg Sent :", "Message Sent");
                        }
                        break;
                    case 5:
                        showToast("Picture Mode : GRAPHIC");
                        if (mTcpClient != null) {
                            new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, commmands[14]);
                            Log.d("Msg Sent :", "Message Sent");
                        }
                        break;
                    case 6:
                        showToast("Picture Mode : USER ");
                        if (mTcpClient != null) {
                            new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, commmands[15]);
                            Log.d("Msg Sent :", "Message Sent");
                        }
                        break;

/*                      "VPM:NAT", // Picture Mode : NATURAL    //10
                        "VPM:STD", // Picture Mode : STANDARD   //11
                        "VPM:DYN", // Picture Mode : DYNAMI     //12
                        "VPM:CIN", // Picture Mode : CINEMA     //13
                        "VPM:GRA", // Picture Mode : GRAPHIC    //14
                        "VPM:USR", // Picture Mode : USER       //15*/
                }
            }

            @Override
            public void onExpand() {
                if (!input2Toast) {
                    showToast("Select Picture Mode");
                    input2Toast = true;
                }
            }

            @Override
            public void onCollapse() {
                // showToast("onCollapse");
            }
        });
    }

    /*                      "VPM:NAT", // Picture Mode : NATURAL    //10
                            "VPM:STD", // Picture Mode : STANDARD   //11
                            "VPM:DYN", // Picture Mode : DYNAMI     //12
                            "VPM:CIN", // Picture Mode : CINEMA     //13
                            "VPM:GRA", // Picture Mode : GRAPHIC    //14
                            "VPM:USR", // Picture Mode : USER       //15*/
    private void installButton0to360() {
        final AllAngleExpandableButton button = (AllAngleExpandableButton) findViewById(R.id.button_expandable_0_360);
        final List<ButtonData> buttonDatas = new ArrayList<>();
        int[] drawable = {R.drawable.pic, R.drawable.nature, R.drawable.pic, R.drawable.dynamic, R.drawable.cinema, R.drawable.gfx, R.drawable.user};
        int[] color = {R.color.Transparent, R.color.Transparent, R.color.Transparent, R.color.Transparent, R.color.Transparent, R.color.Transparent, R.color.Transparent};
        for (int i = 0; i < 7; i++) {
            ButtonData buttonData;
            if (i == 0) {
                buttonData = ButtonData.buildIconButton(this, drawable[i], 15);
            } else {
                buttonData = ButtonData.buildIconButton(this, drawable[i], 0);
            }
            buttonData.setBackgroundColorId(this, color[i]);
            buttonDatas.add(buttonData);
        }
        button.setButtonDatas(buttonDatas);
        setListener2(button);
    }

}
