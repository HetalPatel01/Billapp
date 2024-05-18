package com.quickgstbillprint.activity

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.billapp.R
import com.billapp.databinding.ActivityPrintNewBinding
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.quickgstbillprint.model.Bill

class PrintNewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrintNewBinding
    private val REQUEST_BLUETOOTH_PERMISSION = 1
    private val REQUEST_ENABLE_BT = 2
    private var selectedDevice: BluetoothConnection? = null
    var billsList = ArrayList<Bill>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_print_new)
        billsList = intent.getSerializableExtra("billsList") as ArrayList<Bill>
        initView()
    }

    private fun initView() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val isBluetoothEnabled = bluetoothAdapter?.isEnabled ?: false
        binding.sw.isChecked = isBluetoothEnabled

        binding.sw.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                checkBluetoothPermissionsAndEnable()
            } else {
                checkBluetoothPermissionsAndDisable()
            }
        }

        if (isBluetoothEnabled) {
            // Bluetooth is already enabled, you can browse for devices or take other actions
        }
    }

    private fun checkBluetoothPermissionsAndDisable() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_BLUETOOTH_PERMISSION
            )
        } else {
            disableBluetooth()
        }
    }

    private fun disableBluetooth() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                bluetoothAdapter.disable()
            }
        }
    }

    private fun checkBluetoothPermissionsAndEnable() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_BLUETOOTH_PERMISSION
            )
        } else {
            enableBluetooth()
        }
    }

    private fun enableBluetooth() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableBluetooth()
            } else {
                binding.sw.isChecked = false // Reset switch if permission is denied
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != Activity.RESULT_OK) {
                binding.sw.isChecked = false // Reset switch if Bluetooth enabling is canceled
            }
        }
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 2
    }
}
