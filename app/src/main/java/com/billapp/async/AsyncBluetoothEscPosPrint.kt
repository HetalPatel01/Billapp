package com.billapp.async

import android.content.Context
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.exceptions.EscPosConnectionException


class AsyncBluetoothEscPosPrint : AsyncEscPosPrint {
    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, onPrintFinished: OnPrintFinished?) : super(
        context!!,
        onPrintFinished
    )

    override fun doInBackground(vararg printersData: AsyncEscPosPrinter?): PrinterStatus {
        if (printersData.size == 0) {
            return PrinterStatus(null, AsyncEscPosPrint.FINISH_NO_PRINTER)
        }

        val printerData: AsyncEscPosPrinter = printersData[0]!!
        val deviceConnection: DeviceConnection = printerData.getPrinterConnection()

        this.publishProgress(AsyncEscPosPrint.PROGRESS_CONNECTING)

        if (deviceConnection == null) {
            printersData[0] = AsyncEscPosPrinter(
                BluetoothPrintersConnections.selectFirstPaired()!!,
                printerData.getPrinterDpi(),
                printerData.getPrinterWidthMM(),
                printerData.getPrinterNbrCharactersPerLine()
            ) as Nothing
            printersData[0]!!.setTextsToPrint(printerData.internalGetTextsToPrint())
        } else {
            try {
                deviceConnection.connect()
            } catch (e: EscPosConnectionException) {
                e.printStackTrace()
            }
        }
        return super.doInBackground(printersData);
    }
}