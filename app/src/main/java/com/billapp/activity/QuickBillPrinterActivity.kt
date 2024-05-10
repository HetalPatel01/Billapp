package com.billapp.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.billapp.R
import com.billapp.async.AsyncBluetoothEscPosPrint
import com.billapp.async.AsyncEscPosPrint
import com.billapp.async.AsyncEscPosPrinter
import com.billapp.databinding.ActivityQuickBillPrinterBinding
import com.billapp.model.Bill
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import java.text.SimpleDateFormat

class QuickBillPrinterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuickBillPrinterBinding
    private val REQUEST_BLUETOOTH_PERMISSION = 1
    private var onBluetoothPermissionsGranted: OnBluetoothPermissionsGranted? = null
    private var selectedDevice: BluetoothConnection? = null
    var billsList = ArrayList<Bill>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_quick_bill_printer)
        billsList = intent.getSerializableExtra("billsList") as ArrayList<Bill>

        println("billll::>" + billsList)

        initView()
    }

    private fun initView() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val isBluetoothEnabled = bluetoothAdapter?.isEnabled ?: false
        binding.sw.isChecked = isBluetoothEnabled
        binding.progressBar.visibility = View.VISIBLE
        binding.sw.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Switch activated, check Bluetooth permissions and enable Bluetooth
                checkBluetoothPermissionsAndEnable(object : OnBluetoothPermissionsGranted {
                    override fun onPermissionsGranted() {
                        browseBluetoothDevice()
                    }
                })
            } else {
                // Switch deactivated, disable Bluetooth
                disableBluetooth()
            }
        }

        // If Bluetooth is already enabled, browse for devices
        if (isBluetoothEnabled) {
            browseBluetoothDevice()
        }
    }


    private fun checkBluetoothPermissionsAndEnable(onBluetoothPermissionsGranted: OnBluetoothPermissionsGranted) {
        this.onBluetoothPermissionsGranted = onBluetoothPermissionsGranted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_BLUETOOTH_PERMISSION
            )
        } else {
            // Permission granted, callback
            onBluetoothPermissionsGranted.onPermissionsGranted()
        }
    }

    private fun enableBluetooth() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled) {
                // Bluetooth is not enabled, prompt user to enable it
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getAsyncEscPosPrinter(printerConnection: DeviceConnection?): AsyncEscPosPrinter {
        val format: SimpleDateFormat = SimpleDateFormat("'on' yyyy-MM-dd 'at' HH:mm:ss")
        val printer = AsyncEscPosPrinter(printerConnection!!, 203, 48f, 32)

        val billText = buildString {
            append("[C]<img>${
                PrinterTextParserImg.bitmapToHexadecimalString(
                    printer,
                    resources.getDrawableForDensity(
                        android.R.drawable.ic_dialog_info,
                        DisplayMetrics.DENSITY_MEDIUM
                    )
                )
            }</img>")
            append("[L]")
            append("[C]<u><font size='big'>ORDER DETAILS</font></u>")
            append("[L]")

            // Add each bill item to the text
            billsList.forEachIndexed { index, bill ->
                append("[L]")
                append("[L]<b>${bill.details}</b>[R]${bill.total}€")
                append("[L]  + Quantity : ${bill.quantity}")
                append("[L]  + Rate : ${bill.rate}")
            }

            append("[C]================================")
            // Add total price and tax
            val totalPrice = billsList.sumByDouble { it.total }
            val tax = totalPrice * 0.15 // Assuming tax rate is 15%, adjust as needed
            append("[R]TOTAL PRICE :[R]${String.format("%.2f", totalPrice)}€")
            append("[R]TAX :[R]${String.format("%.2f", tax)}€")

            append("[L]")
            append("[C]================================")
        }

        return printer.addTextToPrint(billText)
    }


    private fun browseBluetoothDevice() {

        val bluetoothDevicesList = BluetoothPrintersConnections().list
        println("Bluetooth Devices List: $bluetoothDevicesList")

        val items = bluetoothDevicesList?.map {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            it.device.name
        } ?: emptyList()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        binding.listBluetoothPrinters.adapter = adapter
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, "No Bluetooth Available", Toast.LENGTH_SHORT).show()

        // Handle item click events if needed
        binding.listBluetoothPrinters.setOnItemClickListener { parent, view, position, id ->
            val selectedPrinterName = items[position]
            val selectedDevice = bluetoothDevicesList?.getOrNull(position)
            println("Selected Printer: $selectedPrinterName")
            printBluetooth()
        }

    }


    private fun disableBluetooth() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled) {
                // Bluetooth is enabled, disable it
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                bluetoothAdapter.disable()
            }
        }
    }

    fun printBluetooth() {
        checkBluetoothPermissionsAndEnable(object : OnBluetoothPermissionsGranted {
            override fun onPermissionsGranted() {
                AsyncBluetoothEscPosPrint(
                    this@QuickBillPrinterActivity,
                    object : AsyncEscPosPrint.OnPrintFinished() {
                        override fun onError(
                            asyncEscPosPrinter: AsyncEscPosPrinter?,
                            codeException: Int
                        ) {
                            Log.e(
                                "Async.OnPrintFinished",
                                "AsyncEscPosPrint.OnPrintFinished : An error occurred !"
                            )
                        }

                        override fun onSuccess(asyncEscPosPrinter: AsyncEscPosPrinter?) {
                            Log.i(
                                "Async.OnPrintFinished",
                                "AsyncEscPosPrint.OnPrintFinished : Print is finished !"
                            )
                        }
                    }
                ).execute(arrayOf(getAsyncEscPosPrinter(selectedDevice)))
            }
        })
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable Bluetooth
                enableBluetooth()
                onBluetoothPermissionsGranted?.onPermissionsGranted()
            } else {
                // Permission denied, handle accordingly
                // You can show a message to the user or take other actions
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // User did not enable Bluetooth, you can inform the user or take other actions
            }
        }
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 2
    }

    interface OnBluetoothPermissionsGranted {
        fun onPermissionsGranted()
    }
}
