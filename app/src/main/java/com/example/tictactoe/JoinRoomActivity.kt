package com.example.tictactoe

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import android.app.AlertDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.os.Looper
import android.os.Handler


class JoinRoomActivity : AppCompatActivity() {

    private val TAG = "JoinRoomActivity"
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private lateinit var deviceListAdapter: ArrayAdapter<String>
    private val discoveredDevices = mutableSetOf<BluetoothDevice>()
    private lateinit var statusTextView: TextView
    private lateinit var ticTacToeButtons: List<Button>
    private var currentPlayer = 'X'  // This player's symbol
    private lateinit var boardState: CharArray
    private val RESET_CODE = 99
    private var decisionDialog: AlertDialog? = null
    private lateinit var dbHelper: DatabaseHelper
    private var myturn=10

    private val MY_UUID =
        UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")  // Standard UUID for SPP
    private var bluetoothSocket: BluetoothSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_room)
        dbHelper = DatabaseHelper(this)

        val scanButton: Button = findViewById(R.id.scanButton)
        val deviceListView: ListView = findViewById(R.id.deviceListView)
        statusTextView = findViewById(R.id.statusTextView)
        val resetButton: Button = findViewById(R.id.resetButton)
        deviceListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        deviceListView.adapter = deviceListAdapter
        ticTacToeButtons = listOf(
            findViewById(R.id.button0), findViewById(R.id.button1), findViewById(R.id.button2),
            findViewById(R.id.button3), findViewById(R.id.button4), findViewById(R.id.button5),
            findViewById(R.id.button6), findViewById(R.id.button7), findViewById(R.id.button8)
        )

        boardState = CharArray(9) { ' ' }  // Initialize empty board

        scanButton.setOnClickListener {
            startDeviceDiscovery()
        }
        resetButton.setOnClickListener {
            resetBoard()
            sendReset()
        }
        deviceListView.setOnItemClickListener { _, _, position, _ ->
            val device = discoveredDevices.elementAt(position)
            connectToDevice(device)
        }

        ticTacToeButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (boardState[index] == ' ') {  // Check if the cell is empty
                    makeMove(index, currentPlayer)
                    sendMove(index)
                }
            }
        }
    }

    private fun makeMove(index: Int, player: Char) {
        boardState[index] = player
        ticTacToeButtons[index].text = player.toString()
        checkWinCondition()
        currentPlayer = if (currentPlayer == 'X') 'O' else 'X'  // Switch turns
    }

    private fun startDeviceDiscovery() {
        if (bluetoothAdapter?.isEnabled == true) {
            Log.d(TAG, "Starting device discovery...")
            statusTextView.text = "Scanning for devices..."

            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(deviceReceiver, filter)
            bluetoothAdapter.startDiscovery()
        } else {
            Log.d(TAG, "Bluetooth is not enabled.")
            statusTextView.text = "Bluetooth is not enabled."
        }
    }

    private val deviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (discoveredDevices.add(it)) {  // Add only if new
                        val deviceInfo = "${it.name ?: "Unknown"} - ${it.address}"
                        deviceListAdapter.add(deviceInfo)
                        Log.d(TAG, "Discovered device: $deviceInfo")
                        statusTextView.text = "Found: $deviceInfo"
                    }
                }
            }
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        Log.d(TAG, "Connecting to ${device.name ?: "Unknown"} - ${device.address}")
        statusTextView.text = "Connecting to ${device.name ?: "Unknown"}..."

        Thread {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
                bluetoothAdapter?.cancelDiscovery()  // Stop discovery to avoid interference

                bluetoothSocket?.connect()
                runOnUiThread {
                    Log.d(TAG, "Connected to ${device.name}")
                    statusTextView.text = "Connected to ${device.name}"
                    resetBoard()
                    showFirstMoveDialog()
                }
                receiveMove()  // Start receiving moves after connection
            } catch (e: IOException) {
                Log.e(TAG, "Connection failed: ${e.message}")
                runOnUiThread {
                    statusTextView.text = "Connection failed: ${e.message}"
                }
            }
        }.start()
    }

    private fun sendMove(index: Int) {
        try {
            val outputStream: OutputStream? = bluetoothSocket?.outputStream
            outputStream?.write(index)
            Log.d(TAG, "Move sent: $index")
            statusTextView.text = "Move sent: $index"
        } catch (e: IOException) {
            Log.e(TAG, "Error sending move: ${e.message}")
            statusTextView.text = "Error sending move: ${e.message}"
        }
        checkWinCondition()
        disableBoard()
    }

    private fun receiveMove() {
        Thread {
            try {
                val inputStream: InputStream? = bluetoothSocket?.inputStream
                val buffer = ByteArray(1024)
                var bytesRead: Int

                while (true) {
                    bytesRead = inputStream?.read(buffer) ?: -1
                    if (bytesRead > 0) {
                        val receivedCode = buffer[0].toInt()
                        runOnUiThread {
                            when (receivedCode) {
                                RESET_CODE -> {
                                    Log.d(TAG, "Reset received")
                                    statusTextView.text = "Board reset by opponent"
                                    resetBoard()  // Reset board when signal is received
                                }
                                111 -> {  // Opponent chose to go first
                                    Log.d(TAG, "Decision received: Opponent goes first")
                                    currentPlayer = 'X'  // Opponent is 'O'
                                    statusTextView.text = "Opponent goes first (Player O)"
                                    disableBoard()  // Wait for opponent's move
                                    decisionDialog?.dismiss()  // Dismiss decision dialog
                                }
                                50 -> {  // Opponent chose for you to go first
                                    Log.d(TAG, "Decision received: You go first")
                                    currentPlayer = 'X'  // You are 'X'
                                    statusTextView.text = "You go first (Player X)"
                                    enableBoard()  // Allow player to make the first move
                                    decisionDialog?.dismiss()  // Dismiss decision dialog
                                }
                                else -> {
                                    Log.d(TAG, "Move received: $receivedCode")
                                    makeMove(receivedCode, currentPlayer)  // Update board with received move
                                    enableBoard()
                                }
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error receiving move: ${e.message}")
                runOnUiThread {
                    statusTextView.text = "Error receiving move: ${e.message}"
                }
            }
        }.start()
    }



    private fun checkWinCondition() {
        val winningCombinations = arrayOf(
            intArrayOf(0, 1, 2),  // Top row
            intArrayOf(3, 4, 5),  // Middle row
            intArrayOf(6, 7, 8),  // Bottom row
            intArrayOf(0, 3, 6),  // Left column
            intArrayOf(1, 4, 7),  // Middle column
            intArrayOf(2, 5, 8),  // Right column
            intArrayOf(0, 4, 8),  // Left diagonal
            intArrayOf(2, 4, 6)   // Right diagonal
        )

        val winMessages = arrayOf(
            "with the top row",
            "with the middle row",
            "with the bottom row",
            "with the left column",
            "with the middle column",
            "with the right column",
            "with the left diagonal",
            "with the right diagonal"
        )

        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        for (i in winningCombinations.indices) {
            val (a, b, c) = winningCombinations[i]
            if (boardState[a] != ' ' && boardState[a] == boardState[b] && boardState[a] == boardState[c]) {
                // We have a winner!
                val winner = boardState[a]
                val winMessage = winMessages[i]
                statusTextView.text = "Player $winner wins $winMessage!"

                // Insert game result into the database
                if ((winner == 'X' && myturn == 1) || (winner == 'O' && myturn == 0)) {
                    dbHelper.insertGameData(currentDate, "Me", "vsHuman")
                } else {
                    dbHelper.insertGameData(currentDate, "Opponent", "vsHuman")
                }

                // Reset the board after a delay of 5 seconds
                Handler(Looper.getMainLooper()).postDelayed({
                    resetBoard()
                    showFirstMoveDialog()
                }, 5000) // 5-second delay

                return
            }
        }

        // Check for draw
        if (!boardState.contains(' ')) {
            statusTextView.text = "It's a draw!"
            dbHelper.insertGameData(currentDate, "draw", "vsHuman")

            // Reset the board after a delay of 5 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                resetBoard()
                showFirstMoveDialog()
            }, 5000) // 5-second delay
        }
    }

    private fun disableBoard() {
        // Disable all buttons to stop the game
        ticTacToeButtons.forEach { it.isEnabled = false }
    }
    private fun enableBoard() {
        ticTacToeButtons.forEach { it.isEnabled = boardState[it.id - R.id.button0] == ' ' }  // Enable only if empty
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(deviceReceiver)
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing socket: ${e.message}")
        }
    }
    private fun resetBoard() {
        // Reset the board state to empty
        boardState.fill(' ')

        // Clear the text on all buttons
        ticTacToeButtons.forEach { button ->
            button.text = ""
            button.isEnabled = true  // Enable all buttons
        }

//        // Reset the current player to 'X'
//        currentPlayer = 'X'
//
//        // Update the status text
//        statusTextView.text = "Player X's turn"
    }
    private fun sendReset() {
        try {
            val outputStream: OutputStream? = bluetoothSocket?.outputStream
            outputStream?.write(RESET_CODE)  // Send the reset code
            Log.d(TAG, "Reset sent")
            statusTextView.text = "Reset sent"
        } catch (e: IOException) {
            Log.e(TAG, "Error sending reset: ${e.message}")
            statusTextView.text = "Error sending reset: ${e.message}"
        }
    }
    private fun showFirstMoveDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Who goes first?")

        // Options for the dialog
        builder.setItems(arrayOf("Me", "Opponent")) { dialog, which ->
            when (which) {
                0 -> { // "Me" option
                    currentPlayer = 'X'  // You go first
                    statusTextView.text = "You go first (Player X)"
                    myturn=1
                    enableBoard()  // Allow making moves
                    sendDecision(111)  // Send decision: player goes first
                }
                1 -> { // "Opponent" option
                    currentPlayer = 'X'  // Opponent goes first
                    statusTextView.text = "Opponent goes first (Player O)"
                    myturn=0
                    disableBoard()  // Wait for opponent's move
                    sendDecision(50)  // Send decision: opponent goes first
                }
            }
            dialog.dismiss()  // Dismiss dialog after selection
        }

        builder.setCancelable(false)
        decisionDialog = builder.create()  // Store dialog reference
        decisionDialog?.show()
    }


    private fun sendDecision(decisionCode: Int) {
        try {
            val outputStream: OutputStream? = bluetoothSocket?.outputStream
            outputStream?.write(decisionCode)  // Send the decision code
            Log.d(TAG, "Decision sent: $decisionCode")
            statusTextView.text = "Decision sent: ${if (decisionCode == 111) "You go first" else "Opponent goes first"}"
        } catch (e: IOException) {
            Log.e(TAG, "Error sending decision: ${e.message}")
            statusTextView.text = "Error sending decision: ${e.message}"
        }
    }

}
