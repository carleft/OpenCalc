package com.darkempire78.opencalculator.stat

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.darkempire78.opencalculator.R
import com.darkempire78.opencalculator.databinding.ActivityStatBinding
import jxl.Workbook
import jxl.write.Label
import jxl.write.WritableSheet
import jxl.write.WritableWorkbook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar


class StatActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "StatActivity"
    }

    private lateinit var binding: ActivityStatBinding
    private lateinit var db: AppDatabase
    private val viewTag = R.id.stat_id


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initDB()
        initDatePicker()
        initTimePicker()
//        //限制只输入数字
//        binding.workTimes.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    /**
     * 初始化数据库
     */
    private fun initDB() {
        db = Room.databaseBuilder(
            applicationContext, AppDatabase::class.java, "work_records_database"
        ).build()
    }

    /**
     * 初始化日期选择器
     */
    private fun initDatePicker() {
        binding.datePicker.setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            val curYear: Int = calendar.get(Calendar.YEAR)
            val curMonth: Int = calendar.get(Calendar.MONTH)
            val curDay: Int = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this@StatActivity, { _, year, month, dayOfMonth ->
//                    val selectedDate = year.toString() + "-" + (month + 1) + "-" + dayOfMonth
                    val selectedDate = date2Str(year, month, dayOfMonth)
                    binding.datePicker.apply {
                        setText(selectedDate)
                    }
                }, curYear, curMonth, curDay
            )
            datePickerDialog.show()
        }
    }

    /**
     * 初始化时间选择器
     */
    private fun initTimePicker() {
        binding.timePicker.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                this@StatActivity, { _, hourOfDay, minute ->
                    val selectedTime = hourOfDay.toString() + "小时" + minute + "分"
                    binding.timePicker.apply {
                        setText(selectedTime)
                        setTag(viewTag, hourOfDay * 60 + minute)
                    }
                }, 0, 0, true
            )
            timePickerDialog.show()
        }
    }

    /**
     * 保存记录到数据库中
     */
    fun saveData(view: View) {
        lifecycleScope.launch {
            val date =
                binding.datePicker.text?.toString().validateAndAssign("日期", this@StatActivity)
                    ?: return@launch
            val workstation =
                binding.workstationId.text?.toString().validateAndAssign(
                    fieldName = "工号",
                    activity = this@StatActivity
                )
                    ?: return@launch
            val duration = binding.timePicker.getTag(viewTag) as? Int ?: run {
                toast("时长不可为空！")
                return@launch
            }

            val times = binding.workTimes.text?.toString()?.toIntOrNull() ?: run {
                toast("工作次数不可为空！")
                return@launch
            }

            val isOvertime = binding.checkOvertime.isChecked
            val workContent = binding.workContent.text?.toString() ?: ""

            val workRecord = WorkRecord(
                date = date,
                workstation = workstation,
                duration = duration,
                isOvertime = isOvertime,
                content = workContent,
                times = times
            )
            Dispatchers.IO.invoke {
                db.workRecordDao().insert(workRecord)
            }
            toast("日期：$date, 工位：$workstation, 时长：$duration, 是否为加班：$isOvertime")
        }
    }

    /**
     * 输出Excel表格
     */
    fun outputExcel(view: View) {
        lifecycleScope.launch(Dispatchers.IO) {
            val file = File(this@StatActivity.getExternalFilesDir(null), "工作日志.xlsx")
            val workRecords = db.workRecordDao().getAllWorkRecords()
            // 创建 Excel 文件
            val workbook: WritableWorkbook = Workbook.createWorkbook(file)
            val sheet: WritableSheet = workbook.createSheet("Data", 0)

            // 添加表头
            val headers = arrayOf("日期", "工位", "工作时长", "工作次数", "班次", "工作内容", "白班时间", "白班次数", "夜班时间", "夜班次数", "总累计时间", "总累计次数")
            for ((index, header) in headers.withIndex()) {
                val label = Label(index, 0, header)
                sheet.addCell(label)
            }

            //按月分组记录
            val groupedRecords = mutableMapOf<String, MutableMap<String, MutableList<WorkRecord>>>()
            workRecords.forEach { record ->
                val date = record.date.str2Date()
                val year = date.get(Calendar.YEAR)
                val month = date.get(Calendar.MONTH) + 1
                val day = date.get(Calendar.DAY_OF_MONTH)

                //年Key
                var yearKey = "$year"
                //月Key
                var monthKey = "$year.${month.format2()}"
                if (day > 25) {
                    monthKey = if (month == 12) {
                        "${year + 1}.01"
                    } else {
                        "$year.${(month + 1).format2()}"
                    }

                    if (month == 12) {
                        yearKey += 1
                    }
                }

                groupedRecords.getOrPut(yearKey) { mutableMapOf() }.getOrPut(monthKey) { mutableListOf() }.add(record)
            }

            //写入数据到 Excel 表格中
            //行号
            var index = 0
            groupedRecords.forEach { yearMap ->
                //年白班时间累加
                var yearLightDurationAcc = 0
                //年白班次数累加
                var yearLightTimesAcc = 0
                //年夜班时间累加
                var yearNightDurationAcc = 0
                //年夜班次数累加
                var yearNightTimesAcc = 0
                //年总累计时间
                var yearDurationAcc = 0
                //年总累计次数
                var yearTimesAcc = 0
                yearMap.value.forEach { monthMap ->
                    //月白班时间累加
                    var monthLightDurationAcc = 0
                    //月白班次数累加
                    var monthLightTimesAcc = 0
                    //月夜班时间累加
                    var monthNightDurationAcc = 0
                    //月夜班次数累加
                    var monthNightTimesAcc = 0
                    //月总累计时间
                    var monthDurationAcc = 0
                    //月总累计次数
                    var monthTimesAcc = 0

                    monthMap.value.forEach { record ->
                        val row =  ++ index // 行号从1开始，因为第0行已经是表头了
                        Log.e(TAG, "key = ${monthMap.key}, record = $record, row = $row" )
                        //日期
                        sheet.addCell(Label(0, row, record.date))
                        //工位
                        sheet.addCell(Label(1, row, record.workstation))
                        //工作时长
                        sheet.addCell(Label(2, row, record.duration.formatTime()))
                        //工作次数
                        sheet.addCell(Label(3, row, record.times.toString()))
                        //班次
                        sheet.addCell(Label(4, row, if (record.isOvertime) "夜班" else "白班"))
                        //工作内容
                        sheet.addCell(Label(5, row, record.content))
                        //TODO:累加
                        //白班时间
                        monthLightDurationAcc += if (record.isOvertime) 0 else record.duration
                        sheet.addCell(Label(6, row, if (record.isOvertime) "" else monthLightDurationAcc.formatTime()))
                        //白班次数
                        monthLightTimesAcc += if (record.isOvertime) 0 else record.times
                        sheet.addCell(Label(7, row, if (record.isOvertime) "" else monthLightTimesAcc.toString()))
                        //夜班时间
                        monthNightDurationAcc += if (record.isOvertime) record.duration else 0
                        sheet.addCell(Label(8, row, if (record.isOvertime) monthNightDurationAcc.formatTime() else ""))
                        //夜班次数
                        monthNightTimesAcc += if (record.isOvertime) record.times else 0
                        sheet.addCell(Label(9, row, if (record.isOvertime) monthNightTimesAcc.toString() else ""))
                        //总累计时间
                        monthDurationAcc += record.duration
                        sheet.addCell(Label(10, row, monthDurationAcc.formatTime()))
                        //总累计次数
                        monthTimesAcc += record.times
                        sheet.addCell(Label(11, row, monthTimesAcc.toString()))
                    }
                    //输出月累计
                    index += 2
                    val row = index
                    //日期
                    sheet.addCell(Label(0, row, "${monthMap.key}月总结"))
                    //工位
                    sheet.addCell(Label(1, row, ""))
                    //工作时长
                    sheet.addCell(Label(2, row, monthDurationAcc.formatTime()))
                    //工作次数
                    sheet.addCell(Label(3, row, monthTimesAcc.toString()))
                    //班次
                    sheet.addCell(Label(4, row, ""))
                    //工作内容
                    sheet.addCell(Label(5, row, ""))
                    //白班时间
                    sheet.addCell(Label(6, row, monthLightDurationAcc.formatTime()))
                    //白班次数
                    sheet.addCell(Label(7, row, monthLightTimesAcc.toString()))
                    //夜班时间
                    sheet.addCell(Label(8, row, monthNightDurationAcc.formatTime()))
                    //夜班次数
                    sheet.addCell(Label(9, row, monthNightTimesAcc.toString()))
                    //总累计时间
                    sheet.addCell(Label(10, row, monthDurationAcc.formatTime()))
                    //总累计次数
                    sheet.addCell(Label(11, row, monthTimesAcc.toString()))
                    index ++

                    //年度累加
                    yearLightDurationAcc += monthLightDurationAcc
                    yearLightTimesAcc += monthLightTimesAcc
                    yearNightDurationAcc += monthNightDurationAcc
                    yearNightTimesAcc += monthNightTimesAcc
                    yearDurationAcc += monthDurationAcc
                    yearTimesAcc += monthTimesAcc
                }
                //输出年度累计
                index += 2
                val row = index
                //日期
                sheet.addCell(Label(0, row, "${yearMap.key}全年总结"))
                //工位
                sheet.addCell(Label(1, row, ""))
                //工作时长
                sheet.addCell(Label(2, row, yearDurationAcc.formatTime()))
                //工作次数
                sheet.addCell(Label(3, row, yearTimesAcc.toString()))
                //班次
                sheet.addCell(Label(4, row, ""))
                //工作内容
                sheet.addCell(Label(5, row, ""))
                //白班时间
                sheet.addCell(Label(6, row, yearLightDurationAcc.formatTime()))
                //白班次数
                sheet.addCell(Label(7, row, yearLightTimesAcc.toString()))
                //夜班时间
                sheet.addCell(Label(8, row, yearNightDurationAcc.formatTime()))
                //夜班次数
                sheet.addCell(Label(9, row, yearNightTimesAcc.toString()))
                //总累计时间
                sheet.addCell(Label(10, row, yearDurationAcc.formatTime()))
                //总累计次数
                sheet.addCell(Label(11, row, yearTimesAcc.toString()))
                index ++

            }



            // 写入数据后关闭工作簿
            workbook.write()
            workbook.close()

            Dispatchers.Main.invoke {
                toast("成功输出至：${file.absoluteFile}")
            }
        }
    }

    fun openExcelPath(view: View) {
        val filePath = File(this@StatActivity.getExternalFilesDir(null), "") ?: return
        val uri = FileProvider.getUriForFile(
            this@StatActivity,
            applicationContext.packageName + ".fileProvider",
            filePath
        )
        // 创建一个Intent，指定ACTION_VIEW操作以打开Excel文件
        val intent = Intent(Intent.ACTION_VIEW)
        // 设置文件的类型为Excel
        intent.setDataAndType(uri, "application/vnd.ms-excel")
        // 添加Flag以确保Intent在外部应用中启动
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

}