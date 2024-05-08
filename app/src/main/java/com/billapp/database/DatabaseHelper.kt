package com.billapp.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.billapp.model.Bill

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "BillsDatabase"
        private const val TABLE_NAME = "bills"
        private const val COLUMN_ID = "id"
        private const val COLUMN_QUANTITY = "quantity"
        private const val COLUMN_RATE = "rate"
        private const val COLUMN_TOTAL = "total"
        private const val COLUMN_DETAILS = "details"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_QUANTITY REAL, $COLUMN_RATE REAL, $COLUMN_TOTAL REAL, $COLUMN_DETAILS TEXT)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addBill(bill: Bill) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_QUANTITY, bill.quantity)
        values.put(COLUMN_RATE, bill.rate)
        values.put(COLUMN_TOTAL, bill.total)
        values.put(COLUMN_DETAILS, bill.details)
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    @SuppressLint("Range")
    fun getAllBills(): ArrayList<Bill> {
        val billsList = ArrayList<Bill>()
        val selectQuery = "SELECT * FROM $TABLE_NAME"
        val db = this.readableDatabase
        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var quantity: Double
        var rate: Double
        var total: Double
        var details: String
        var id: Int

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                quantity = cursor.getDouble(cursor.getColumnIndex(COLUMN_QUANTITY))
                rate = cursor.getDouble(cursor.getColumnIndex(COLUMN_RATE))
                total = cursor.getDouble(cursor.getColumnIndex(COLUMN_TOTAL))
                details = cursor.getString(cursor.getColumnIndex(COLUMN_DETAILS))
                val bill = Bill(id, quantity, rate, total, details)
                billsList.add(bill)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return billsList
    }

    fun updateBillDetails(billId: Int, details: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_DETAILS, details)
        db.update(TABLE_NAME, values, "$COLUMN_ID=?", arrayOf(billId.toString()))
        db.close()
    }
    fun clearDatabase() {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, null, null)
        db.close()
    }
}
