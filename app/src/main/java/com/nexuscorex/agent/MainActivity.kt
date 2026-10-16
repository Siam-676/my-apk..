package com.nexuscorex.agent

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import android.view.Gravity

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#05060B"))
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(this).apply {
            text = "NEXUS CORE X — ANDROID AGENT"
            textSize = 20f
            setTextColor(Color.parseColor("#22D3EE"))
        }

        val status = TextView(this).apply {
            text = "Hybrid LAN/WAN Enforcer + Per-App VPN Firewall Active on Port 5055"
            textSize = 13f
            setTextColor(Color.parseColor("#94A3B8"))
            setPadding(0, 24, 0, 48)
        }

        val btn = Button(this).apply {
            text = "Authorize VPN Firewall & Start Agent"
            setOnClickListener {
                val vpnIntent = VpnService.prepare(this@MainActivity)
                if (vpnIntent != null) {
                    startActivityForResult(vpnIntent, 100)
                } else {
                    onActivityResult(100, RESULT_OK, null)
                }
            }
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(btn)
        setContentView(layout)

        startService(Intent(this, NexusHybridAgentService::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 100 && resultCode == RESULT_OK) {
            startService(Intent(this, NexusHybridAgentService::class.java))
        }
    }
}
