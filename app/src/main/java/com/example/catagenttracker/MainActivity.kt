package com.example.catagenttracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.provider.ContactsContract
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.work.*
import com.example.catagenttracker.RouteTrackingService.Companion.EXTRA_SECRET_CAT_AGENT_ID
import com.example.catagenttracker.worker.CatFurGroomingWorker
import com.example.catagenttracker.worker.CatLitterBoxSittingWorker
import com.example.catagenttracker.worker.CatSitUpWorker
import com.example.catagenttracker.worker.CatStretchingWorker
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val workManager = WorkManager.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//network constraint tells the work manager to wait for internet before executing work
        val networkConstraints= Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val catAgentId ="CatAgent1"
        val catStretchingRequest= OneTimeWorkRequest.Builder(CatLitterBoxSittingWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getCatAgentIdInputData(CatStretchingWorker.INPUT_DATA_CAT_AGENT_ID, catAgentId))
            .build()

        val catFurGroomingRequest= OneTimeWorkRequest.Builder(CatFurGroomingWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getCatAgentIdInputData(CatFurGroomingWorker.INPUT_DATA_CAT_AGENT_ID, catAgentId))
            .build()
        val catLitterBoxSittingRequest= OneTimeWorkRequest.Builder(CatLitterBoxSittingWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getCatAgentIdInputData(CatLitterBoxSittingWorker.INPUT_DATA_CAT_AGENT_ID,catAgentId))
            .build()
        val catSitUpRequest= OneTimeWorkRequest.Builder(CatSitUpWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getCatAgentIdInputData(CatSitUpWorker.INPUT_DATA_CAT_AGENT_ID, catAgentId))
            .build()

        workManager.beginWith(catStretchingRequest)
            .then(catFurGroomingRequest)
            .then(catLitterBoxSittingRequest)
            .then(catSitUpRequest)
            .enqueue()
        workManager.getWorkInfoByIdLiveData(catStretchingRequest.id)
            .observe(this, Observer { info->
                if (info.state.isFinished){
                    showResult("Agent done stretching")
                }
            })
        workManager.getWorkInfoByIdLiveData(catFurGroomingRequest.id)
            .observe(this, Observer { info ->
                if (info.state.isFinished){
                    showResult("Agent Done grooming its fur")
                }

            })
        workManager.getWorkInfoByIdLiveData(catLitterBoxSittingRequest.id)
            .observe(this, Observer { info->
                if (info.state.isFinished){
                    showResult("Agent done sitting in litter box")
                }
            })
        workManager.getWorkInfoByIdLiveData(catSitUpRequest.id)
            .observe(this, Observer { info->
                if (info.state.isFinished){
                    showResult("Agent done sitting up. Ready to go!")
                    launchTrackingService()
                }
            })

    }

    //this function constructs the input data for you with cat agent id
    private fun getCatAgentIdInputData(catAgentIdKey: String,
                                       catAgentIdValue: String)=
    Data.Builder().putString(catAgentIdKey, catAgentIdValue)
        .build()

    private fun showResult(message: String){
        Toast.makeText(this, message, LENGTH_SHORT).show()
    }
    private fun launchTrackingService(){
        RouteTrackingService.trackingCompletion.observe(this, Observer {
            agentId-> showResult("Agent $agentId arrived")
        })
        val serviceIntent= Intent(this, RouteTrackingService::class.java).apply {
            putExtra(EXTRA_SECRET_CAT_AGENT_ID, "007")
        }
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}