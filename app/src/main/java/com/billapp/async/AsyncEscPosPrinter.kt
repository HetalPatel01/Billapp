package com.billapp.async

import com.dantsu.escposprinter.EscPosPrinterSize
import com.dantsu.escposprinter.connection.DeviceConnection

class AsyncEscPosPrinter(
    printerConnection: DeviceConnection,
    printerDpi: Int,
    printerWidthMM: Float,
    printerNbrCharactersPerLine: Int
) :
    EscPosPrinterSize(printerDpi, printerWidthMM, printerNbrCharactersPerLine) {
    private val printerConnection: DeviceConnection = printerConnection
    var textsToPrint: Array<String?> = arrayOfNulls(0)
        private set

    fun getPrinterConnection(): DeviceConnection {
        return this.printerConnection
    }

    fun setTextsToPrint(textsToPrint: Array<String?>): AsyncEscPosPrinter {
        this.textsToPrint = textsToPrint
        return this
    }

    fun addTextToPrint(textToPrint: String?): AsyncEscPosPrinter {
        val tmp = arrayOfNulls<String>(textsToPrint.size + 1)
        System.arraycopy(
            this.textsToPrint, 0, tmp, 0,
            textsToPrint.size
        )
        tmp[textsToPrint.size] = textToPrint
        this.textsToPrint = tmp
        return this
    }

    fun internalGetTextsToPrint(): Array<String?> { // Renamed function
        return this.textsToPrint
    }
}
