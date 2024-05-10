package com.billapp.async

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import com.dantsu.escposprinter.EscPosCharsetEncoding
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException
import com.dantsu.escposprinter.exceptions.EscPosConnectionException
import com.dantsu.escposprinter.exceptions.EscPosEncodingException
import com.dantsu.escposprinter.exceptions.EscPosParserException
import java.lang.ref.WeakReference


abstract class AsyncEscPosPrint @JvmOverloads constructor(
    context: Context,
    protected var onPrintFinished: OnPrintFinished? = null
) :
    AsyncTask<Array<out AsyncEscPosPrinter?>, Int, AsyncEscPosPrint.PrinterStatus>() {
    protected var dialog: ProgressDialog? = null
    protected var weakContext: WeakReference<Context> = WeakReference(context)

    override fun doInBackground(vararg printersData: Array<out AsyncEscPosPrinter?>): PrinterStatus {
        if (printersData.isEmpty() || printersData[0].isNullOrEmpty()) {
            return PrinterStatus(null, FINISH_NO_PRINTER)
        }

        this.publishProgress(PROGRESS_CONNECTING)

        val printerData: AsyncEscPosPrinter? = printersData[0]?.get(0)

        if (printerData == null) {
            return PrinterStatus(null, FINISH_NO_PRINTER)
        }

        try {
            val deviceConnection: DeviceConnection = printerData.getPrinterConnection()
                ?: return PrinterStatus(null, FINISH_NO_PRINTER)

            val printer: EscPosPrinter = EscPosPrinter(
                deviceConnection,
                printerData.getPrinterDpi(),
                printerData.getPrinterWidthMM(),
                printerData.getPrinterNbrCharactersPerLine(),
                EscPosCharsetEncoding("windows-1252", 16)
            )

            this.publishProgress(PROGRESS_PRINTING)

            val textsToPrint: Array<String?> = printerData.internalGetTextsToPrint()

            for (textToPrint in textsToPrint) {
                printer.printFormattedTextAndCut(textToPrint)
                Thread.sleep(500)
            }

            this.publishProgress(PROGRESS_PRINTED)
        } catch (e: EscPosConnectionException) {
            e.printStackTrace()
            return PrinterStatus(printerData, FINISH_PRINTER_DISCONNECTED)
        } catch (e: EscPosParserException) {
            e.printStackTrace()
            return PrinterStatus(printerData, FINISH_PARSER_ERROR)
        } catch (e: EscPosEncodingException) {
            e.printStackTrace()
            return PrinterStatus(printerData, FINISH_ENCODING_ERROR)
        } catch (e: EscPosBarcodeException) {
            e.printStackTrace()
            return PrinterStatus(printerData, FINISH_BARCODE_ERROR)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return PrinterStatus(printerData, FINISH_SUCCESS)
    }

    override fun onPreExecute() {
        if (this.dialog == null) {
            val context = weakContext.get() ?: return

            this.dialog = ProgressDialog(context)
            dialog!!.setTitle("Printing in progress...")
            dialog!!.setMessage("...")
            dialog!!.setProgressNumberFormat("%1d / %2d")
            dialog!!.setCancelable(false)
            dialog!!.isIndeterminate = false
            dialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            dialog!!.show()
        }
    }

    override fun onProgressUpdate(vararg progress: Int?) {
        when (progress[0]) {
            PROGRESS_CONNECTING -> dialog!!.setMessage("Connecting printer...")
            PROGRESS_CONNECTED -> dialog!!.setMessage("Printer is connected...")
            PROGRESS_PRINTING -> dialog!!.setMessage("Printer is printing...")
            PROGRESS_PRINTED -> dialog!!.setMessage("Printer has finished...")
        }
        dialog!!.progress = progress[0] ?: 0
        dialog!!.max = 4
    }

    override fun onPostExecute(result: PrinterStatus) {
        dialog!!.dismiss()
        this.dialog = null

        val context = weakContext.get() ?: return

        when (result.printerStatus) {
            FINISH_SUCCESS -> AlertDialog.Builder(context)
                .setTitle("Success")
                .setMessage("Congratulation ! The texts are printed !")
                .show()

            FINISH_NO_PRINTER -> AlertDialog.Builder(context)
                .setTitle("No printer")
                .setMessage("The application can't find any printer connected.")
                .show()

            FINISH_PRINTER_DISCONNECTED -> AlertDialog.Builder(context)
                .setTitle("Broken connection")
                .setMessage("Unable to connect the printer.")
                .show()

            FINISH_PARSER_ERROR -> AlertDialog.Builder(context)
                .setTitle("Invalid formatted text")
                .setMessage("It seems to be an invalid syntax problem.")
                .show()

            FINISH_ENCODING_ERROR -> AlertDialog.Builder(context)
                .setTitle("Bad selected encoding")
                .setMessage("The selected encoding character returning an error.")
                .show()

            FINISH_BARCODE_ERROR -> AlertDialog.Builder(context)
                .setTitle("Invalid barcode")
                .setMessage("Data send to be converted to barcode or QR code seems to be invalid.")
                .show()
        }
        if (this.onPrintFinished != null) {
            if (result.printerStatus == FINISH_SUCCESS) {
                onPrintFinished!!.onSuccess(result.getAsyncEscPosPrinter())
            } else {
                onPrintFinished!!.onError(
                    result.getAsyncEscPosPrinter(),
                    result.printerStatus
                )
            }
        }
    }

    class PrinterStatus(private val asyncEscPosPrinter: AsyncEscPosPrinter?, val printerStatus: Int) {
        fun getAsyncEscPosPrinter(): AsyncEscPosPrinter? {
            return asyncEscPosPrinter
        }
    }

    abstract class OnPrintFinished {
        abstract fun onError(asyncEscPosPrinter: AsyncEscPosPrinter?, codeException: Int)
        abstract fun onSuccess(asyncEscPosPrinter: AsyncEscPosPrinter?)
    }

    companion object {
        const val FINISH_SUCCESS: Int = 1
        const val FINISH_NO_PRINTER: Int = 2
        const val FINISH_PRINTER_DISCONNECTED: Int = 3
        const val FINISH_PARSER_ERROR: Int = 4
        const val FINISH_ENCODING_ERROR: Int = 5
        const val FINISH_BARCODE_ERROR: Int = 6

        const val PROGRESS_CONNECTING: Int = 1
        const val PROGRESS_CONNECTED: Int = 2
        const val PROGRESS_PRINTING: Int = 3
        const val PROGRESS_PRINTED: Int = 4
    }

    abstract fun doInBackground(vararg printersData: AsyncEscPosPrinter?): PrinterStatus
}
