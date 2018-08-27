package com.example.ari.bt_ttt_app;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BT_CanvasView extends View { //you have to create a new java file and then insert the same file in the xml of the page in which you want the canvas
    Paint paint = new Paint();
    Paint paintx = new Paint();
    Paint painto = new Paint();
    Paint painto1 = new Paint();
    boolean oncewin = false;
    boolean oncedrawen = false;
    float[][] midx = new float[3][3];
    float[][] midy = new float[3][3];
    Context ctx;
    float canvasSide, cellSide;
    boolean touchEnabled = true;
    boolean oppontentRematch = false;
    boolean playerRematch = false;
    int cnt = 0;
    int[] time = {1000};
    BluetoothSocket bluetoothSocket;
    BluetoothDevice bluetoothDevice;
    String TAG = "BT_CanvasView";
    String p1Name = "";
    String p2Name = "";
    public static int[][] a = new int[3][3];
    public static int turn = 0;

    public static ConnectedThread connectedThread = null;

    public void init() {
        cnt = 0;
        time[0] = 1000;
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, r.getDisplayMetrics());
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                a[row][col] = 0;
                midx[row][col] = ((px / 6) + (col * (px / 3)));
                midy[row][col] = ((px / 6) + (row * (px / 3)));
            }
        }
        touchEnabled = true;
        oncedrawen = false;
        oncewin = false;
        turn = 0;
    }

    public BT_CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
        paint.setAntiAlias(true);
        paint.setStrokeWidth(10f);
        paint.setColor(Color.CYAN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);

        paintx.setStrokeWidth(15f);
        paintx.setColor(Color.CYAN);
        paintx.setStyle(Paint.Style.STROKE);
        paintx.setStrokeJoin(Paint.Join.ROUND);

        painto.setColor(Color.CYAN);
        painto.setStyle(Paint.Style.FILL);

        painto1.setColor(Color.rgb(57,84,166));
        painto1.setStyle(Paint.Style.FILL);

        bluetoothDevice = BT_TTT_names.mBluetoothDevice;
        bluetoothSocket = BT_TTT_names.mBluetoothSocket;

        connectedThread = new ConnectedThread(bluetoothSocket);
        connectedThread.start();

        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (playerRematch && oppontentRematch) {
            init();
            playerRematch = false;
            oppontentRematch = false;
        }
        super.onDraw(canvas);
        Resources r = getResources();
        float pxi = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, r.getDisplayMetrics());
        canvasSide = pxi;
        cellSide = canvasSide / 3;
        canvas.drawLine(cellSide, 0, cellSide, canvasSide, paint);
        canvas.drawLine(2 * cellSide, 0, 2 * cellSide, canvasSide, paint);
        canvas.drawLine(0, cellSide, canvasSide, cellSide, paint);
        canvas.drawLine(0, 2 * cellSide, canvasSide, 2 * cellSide, paint);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (a[row][col] == 1) {
                    canvas.drawLine((midx[row][col] - ((4 * cellSide) / 11)), (midy[row][col] - ((4 * cellSide) / 11)), (midx[row][col] + ((4 * cellSide) / 11)), (midy[row][col] + ((4 * cellSide) / 11)), paintx);
                    canvas.drawLine((midx[row][col] + ((4 * cellSide) / 11)), (midy[row][col] - ((4 * cellSide) / 11)), (midx[row][col] - ((4 * cellSide) / 11)), (midy[row][col] + ((4 * cellSide) / 11)), paintx);
                } else if (a[row][col] == 2) {
                    canvas.drawCircle(midx[row][col], midy[row][col], (4 * cellSide) / 11, painto);
                    canvas.drawCircle(midx[row][col], midy[row][col], (13 * cellSide) / 44, painto1);
                }
            }
        }
        check();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && touchEnabled) {
            float touchX = event.getX();
            float touchY = event.getY();
            if (touchX < canvasSide && touchX > 0 && touchY < canvasSide && touchX > 0) {
                int col = (int) (touchX / cellSide);
                int row = (int) (touchY / cellSide);
                if (a[row][col] == 0) {
                    a[row][col]++;
                    turn++;

                    if (turn % 2 == 0) {
                        a[row][col]++;
                    }

                    if (connectedThread != null) {
                        String status = "";
                        for (int i = 0; i < 3; i++) {
                            for (int j = 0; j < 3; j++) {
                                status = status.concat(String.valueOf(a[i][j]));
                            }
                        }
                        status = status.concat(";" + turn);
                        byte[] ByteArray = status.getBytes();
                        connectedThread.write(ByteArray);
                        if (turn == 1) {
                           p1Name = MainActivity.MyName.trim().toUpperCase();
                           p2Name = MainActivity.OpponentName.trim().toUpperCase();

                        } else if (turn == 2) {
                            p2Name = MainActivity.MyName.trim().toUpperCase();
                            p1Name = MainActivity.OpponentName.trim().toUpperCase();

                        }
                        touchEnabled = false;
                    }

                    if (!oncewin && !oncedrawen) {
                        invalidate();
                        check();
                    } else {
                        try {
                            connectedThread = null;
                            bluetoothSocket.close();
                        } catch (Exception e) {
                            Log.e(TAG, "exception " + e.getMessage());
                        }
                        Intent intent = new Intent();
                        BT_TTT.act_2p.setResult(3, intent);
                        BT_TTT.act_2p.finish();
                    }
                }
            }
        }
        return true;
    }

    public void showAlert(final String str) {
        touchEnabled = true;
        final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE: {
                        try {
                            String endMsg = "END";
                            byte[] ByteArray = endMsg.getBytes();
                            connectedThread.write(ByteArray);
                            connectedThread.cancel();
                            connectedThread = null;
                            bluetoothSocket.close();
                        } catch (Exception e) {
                            Log.d(TAG, "exception " + e.getMessage());
                        }
                        Intent intent = new Intent();
                        BT_TTT.act_2p.setResult(3, intent);
                        BT_TTT.act_2p.finish();
                        break;
                    }
                    case DialogInterface.BUTTON_NEGATIVE: {
                        String msg = "REMATCH";
                        byte[] ByteArray = msg.getBytes();
                        connectedThread.write(ByteArray);
                        Log.d(TAG, "REMATCH CALLED");
                        playerRematch = true;
                        init();
                        postInvalidate();
                        break;
                    }
                }
            }
        };
        BT_TTT.act_2p.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage(str).setPositiveButton("OK", dialogClickListener).setNegativeButton("REMATCH", dialogClickListener).show();
                    }
                }
        );
    }

    public void check() {
        if (!oncewin) {
            if (a[0][0] == a[0][1] && a[0][1] == a[0][2]) {
                if (a[0][0] == 1) {
                    showAlert( p1Name+ " wins! ");

                    oncewin = true;
                } else if (a[0][0] == 2) {
                    showAlert(p2Name + " wins! ");

                    oncewin = true;
                }
            }

            if (a[1][0] == a[1][1] && a[1][1] == a[1][2]) {
                if (a[1][0] == 1) {
                    showAlert(p1Name + " wins! ");

                    oncewin = true;
                } else if (a[1][0] == 2) {
                    showAlert(p2Name + " wins! ");

                    oncewin = true;
                }
            }

            if (a[2][0] == a[2][1] && a[2][1] == a[2][2]) {
                if (a[2][0] == 1) {
                    showAlert(p1Name + " wins! ");

                    oncewin = true;
                } else if (a[2][0] == 2) {
                    showAlert(p2Name + " wins! ");

                    oncewin = true;
                }
            }

            if (a[0][0] == a[1][0] && a[1][0] == a[2][0]) {
                if (a[0][0] == 1) {
                    showAlert(p1Name + " wins! ");

                    oncewin = true;
                } else if (a[0][0] == 2) {
                    showAlert(p2Name + " wins! ");

                    oncewin = true;
                }
            }

            if (a[0][1] == a[1][1] && a[1][1] == a[2][1]) {
                if (a[0][1] == 1) {
                    showAlert(p1Name + " wins! ");

                    oncewin = true;
                } else if (a[0][1] == 2) {
                    showAlert(p2Name + " wins! ");

                    oncewin = true;
                }
            }

            if (a[0][2] == a[1][2] && a[1][2] == a[2][2]) {
                if (a[0][2] == 1) {
                    showAlert(p1Name + " wins! ");

                    oncewin = true;
                } else if (a[0][2] == 2) {
                    showAlert(p2Name + " wins! ");

                    oncewin = true;
                }
            }

            if (a[0][0] == a[1][1] && a[1][1] == a[2][2]) {
                if (a[0][0] == 1) {
                    showAlert(p1Name + " wins! ");

                    oncewin = true;
                } else if (a[0][0] == 2) {
                    showAlert(p2Name + " wins! ");

                    oncewin = true;
                }
            }

            if (a[0][2] == a[1][1] && a[1][1] == a[2][0]) {
                if (a[0][2] == 1) {
                    showAlert(p1Name + " wins! ");

                    oncewin = true;
                } else if (a[0][2] == 2) {
                    showAlert(p2Name + " wins! ");

                    oncewin = true;
                }
            }

            if (turn == 9 && !oncewin) {
                showAlert("Match results in a draw!");
                oncedrawen = true;

            }
        }
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private int cnt = 0;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "Create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Obtain BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "tmp sockets not created", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            if (cnt == 0) {
                try {
                    byte[] ByteArray = MainActivity.MyName.getBytes();
                    connectedThread.write(ByteArray);
                    cnt++;
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }
            }
            byte[] buffer = new byte[1024];
            int bytes;
            // Listen InputStream while connected
            while (true) {
                try {
                    Log.i(TAG, "BEGIN Listening");
                    // Read from stream
                    String readMessage;
                    bytes = mmInStream.read(buffer);
                    readMessage = new String(buffer, 0, bytes);
                    Log.i(TAG, "Listening : " + readMessage);
                    if (readMessage.contains(";")) {
                        // Send bytes to UI Activity
                        a[0][0] = (readMessage.charAt(0) - 48);
                        a[0][1] = (readMessage.charAt(1) - 48);
                        a[0][2] = (readMessage.charAt(2) - 48);
                        a[1][0] = (readMessage.charAt(3) - 48);
                        a[1][1] = (readMessage.charAt(4) - 48);
                        a[1][2] = (readMessage.charAt(5) - 48);
                        a[2][0] = (readMessage.charAt(6) - 48);
                        a[2][1] = (readMessage.charAt(7) - 48);
                        a[2][2] = (readMessage.charAt(8) - 48);
                        turn = (readMessage.charAt(10) - 48);
                        String str = "" + a[0][0] + a[0][1] + a[0][2] + a[1][0] + a[1][1] + a[1][2] + a[2][0] + a[2][1] + a[2][2] + ";" + turn;

                        Log.i(TAG, "GOT : " + str);

                        touchEnabled = true;

                        if (!oncewin && !oncedrawen) {
                            postInvalidate();
                            check();
                        }
                    } else if (readMessage.equals("REMATCH")) {
                        Log.d(TAG, "rematch");
                        oppontentRematch = true;
                        init();
                        postInvalidate();
                        BT_TTT.act_2p.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(BT_TTT.act_2p, MainActivity.MyName + " vs " + MainActivity.OpponentName, Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
                    } else if (readMessage.equals("END")) {
                        break;
                    } else {
                        try {
                            Log.i(TAG, "Hello");
                            MainActivity.OpponentName = readMessage;
                            Log.i(TAG, MainActivity.MyName + " vs " + MainActivity.OpponentName);
                            BT_TTT.act_2p.runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(BT_TTT.act_2p, MainActivity.MyName + " vs " + MainActivity.OpponentName, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                        } catch (Exception e) {
                            Log.d(TAG, e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    //Log.e(TAG, "disconnected", e);
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            if (!oncewin && !oncedrawen) {
                try {
                    Log.d(TAG, "Writing ");
                    mmOutStream.write(buffer);
                } catch (IOException e) {
                    Log.e(TAG, "Exception during write", e);
                }
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}